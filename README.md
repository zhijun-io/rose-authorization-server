# rose-authorization-server

[![CI](https://github.com/zhijun-io/rose-authorization-server/actions/workflows/ci.yml/badge.svg)](https://github.com/zhijun-io/rose-authorization-server/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F.svg)](https://spring.io/projects/spring-boot)
[![GHCR](https://img.shields.io/badge/ghcr.io-zhijun--io%2Frose--authorization--server-blue)](https://github.com/zhijun-io/rose-authorization-server/pkgs/container/rose-authorization-server)

本地 / 集成测试用的 **OIDC Authorization Server**（**勿用于生产**）。

基于 Spring Boot 4.1 与 Spring Security OAuth2 Authorization Server，提供可运行的 IdP 与 Testcontainers 封装。适合在开发机或
CI 中验证登录、授权码、Refresh Token、自定义 claims（如 `roles`）等流程。

## 特性

- **开箱即用的 OIDC IdP**：Authorization Code、Refresh Token、Client Credentials、Token Exchange
- **YAML 配置用户与 Client**：自定义 attributes / claims（如 `roles`、`email`）
- **登录 UI**：Thymeleaf 表单登录，便于浏览器联调
- **启动时打印 Client 配置片段**：可直接粘贴到 Boot OAuth2 Client
- **健康检查**：`/actuator/health`，方便 Testcontainers / Docker wait
- **Testcontainers 封装**：固定镜像、端口、issuer、默认 client 常量，支持 config / TLS / mTLS 与 OAuth2 属性 Map
- **TLS / mTLS 样例**：YAML 叠加配置 + 证书生成脚本
- **Docker Compose**：HTTP / HTTPS / mTLS 示例

## 快速开始

### 从源码运行

```bash
./mvnw -ntp clean package -DskipTests
java -jar target/rose-authorization-server.jar --config=configs/config.yml
```

常用参数：

| 参数 | 说明 |
|---|---|
| `--config=<path>` | 指定 YAML 配置文件 |
| `--print-sample-config` | 打印完整示例配置 |
| `--help` / `-h` | 查看帮助 |

启动成功后访问：

- Issuer / 登录页：http://localhost:9000
- OIDC 元数据：http://localhost:9000/.well-known/openid-configuration
- 健康检查：http://localhost:9000/actuator/health

控制台会打印可用用户，以及可粘贴到客户端的 `spring.security.oauth2.client.*` 片段。

### 浏览器登录

打开 http://localhost:9000 进入登录页。使用 `configs/config.yml` 时可用 `alice` / `alice`（或 `bob` / `bob`）；未挂载自定义用户时默认
`user` / `password`。对接你自己的 OAuth2 Client 见[在客户端应用中使用](#在客户端应用中使用)。

## 配置

配置前缀：`rose.authorization-server`。示例见 [`configs/config.yml`](configs/config.yml)：

```yaml
rose:
  authorization-server:
    users:
      - username: alice
        password: alice
        attributes:
          email: alice@example.com
          roles:
            - viewer
            - editor
            - admin
      - username: bob
        password: bob
        attributes:
          email: bob@example.com
          roles:
            - viewer
            - editor
```

完整字段（JWK、自定义 clients、token TTL 等）可通过 `--print-sample-config` 查看。

JWK：未配置 `jwk` 时启动时随机生成签名密钥；`jwk.random: false` 会加载仓库内置的固定私钥
`src/main/resources/keys/private.pem`（含对应的 `public.pem`），仅适用于本地，切勿在任何共享环境复用。

## 在客户端应用中使用

### 配置 OAuth2 Client

1. 启动 Authorization Server，从控制台复制打印出的 client 配置（或使用下方默认片段）。
2. 确保 scope 包含 `openid`。
3. `issuer-uri` 指向 Authorization Server（本地一般为 `http://127.0.0.1:9000`）。

默认 Client 对应配置：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          rose-authorization-server:
            provider: rose-authorization-server
            client-id: default-client-id
            client-secret: default-client-secret
            client-name: rose Authorization Server
            scope:
              - openid
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          rose-authorization-server:
            issuer-uri: http://127.0.0.1:9000
```

Client 的 `client-id` / `secret` / `redirect-uri` / `scope` 须与 Authorization Server 侧
`rose.authorization-server.clients`（或默认 Client）一致。默认 redirect 已包含：

- `http://localhost:8080`
- `http://localhost:8080/login/oauth2/code/rose-authorization-server`

且默认关闭严格 redirect 校验（`validateRedirectUri=false`），便于本地联调。

### 从 `roles` claim 映射权限

Authorization Server 可将用户 `attributes.roles` 写入 ID Token。客户端可提供 `OidcUserService`，把 `roles` 映射为 Spring Security 角色：

```java
@Bean
OidcUserService oidcUserService() {
    var oidcUserService = new OidcUserService();
    oidcUserService.setOidcUserMapper((oidcUserRequest, oidcUserInfo) -> {
        var roles = oidcUserRequest.getIdToken().getClaimAsStringList("roles");
        var authorities = AuthorityUtils.createAuthorityList();
        if (roles != null) {
            roles.stream()
                    .map(r -> "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return new DefaultOidcUser(authorities, oidcUserRequest.getIdToken(), oidcUserInfo);
    });
    return oidcUserService;
}
```

随后可在请求或方法安全中校验：

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/document/**").hasAnyRole("viewer", "editor", "admin")
                    .requestMatchers("/admin/**").hasRole("admin")
                    .anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults())
            .build();
}
```

完整可运行配置见 [`configs/config.yml`](configs/config.yml)，其中 `redirect-uris`
已包含 `http://localhost:8080/login/oauth2/code/rose-authorization-server`。

## 在测试中使用（Testcontainers）

封装类 `RoseAuthorizationServerContainer` 随本 artifact 发布（已钉死镜像、健康检查与默认 client 常量）。Testcontainers 不随本
artifact 传递，需自行加入：

```xml
<dependency>
  <groupId>io.zhijun.rose</groupId>
  <artifactId>rose-authorization-server</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
```

> 使用 Spring Boot BOM 时 Testcontainers 版本已托管，否则请补 `<version>`。

```java
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class OAuth2ClientIT {

    @Container
    static RoseAuthorizationServerContainer authServer = new RoseAuthorizationServerContainer();

    @DynamicPropertySource
    static void oauth2Properties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.security.oauth2.client.provider.rose-authorization-server.issuer-uri",
                authServer::getIssuerUri);
    }

    @Test
    void openIdConfigurationIsAvailable() {
        RestClient.create()
                .get()
                .uri(authServer.getOpenIdConfigurationUri())
                .retrieve()
                .toBodilessEntity();
    }
}
```

常用 API / 常量：

| API                                                                      | 说明                                                 |
|--------------------------------------------------------------------------|------------------------------------------------------|
| `getIssuerUri()`                                                         | 动态映射后的 issuer（HTTP/HTTPS）                    |
| `getOpenIdConfigurationUri()`                                            | OIDC discovery URL                                   |
| `getHttpPort()`                                                          | 映射后的宿主机端口                                   |
| `withConfig(Path)` / `withConfig(MountableFile)` / `withClasspathConfig` | 挂载自定义 YAML                                      |
| `withTls(cert, key)`                                                     | HTTPS + insecure health wait                         |
| `withMutualTls(cert, key, clientCa)`                                     | mTLS（`client-auth=NEED`；就绪探测改用启动日志）     |
| `getReadyProbe()`                                                        | `HTTP_HEALTH` / `HTTPS_HEALTH` / `STARTUP_LOG`       |
| `oauth2ClientProperties(...)`                                            | 生成 Boot OAuth2 client 属性 Map                     |
| `DEFAULT_IMAGE_NAME`                                                     | `ghcr.io/zhijun-io/rose-authorization-server:latest` |
| `DEFAULT_CLIENT_ID` / `DEFAULT_CLIENT_SECRET`                            | 与默认 Client 一致                                   |
| `DEFAULT_PROVIDER_ID`                                                    | `rose-authorization-server`                          |
| `PORT`                                                                   | 容器内端口 `9000`                                    |

> 拉取镜像前请确认 GHCR 包对你的环境可见（org 包需设为 Public，或已 `docker login ghcr.io`）。

## Docker

构建并运行：

```bash
./mvnw -ntp -B clean package -DskipTests
docker build -t ghcr.io/zhijun-io/rose-authorization-server:local .
docker run --rm -p 9000:9000 ghcr.io/zhijun-io/rose-authorization-server:local
```

使用已发布镜像：

```bash
docker run --rm -p 9000:9000 ghcr.io/zhijun-io/rose-authorization-server:latest
```

挂载自定义配置：

```bash
docker run --rm -p 9000:9000 \
  -v "$PWD/configs/config.yml:/config/config.yml:ro" \
  ghcr.io/zhijun-io/rose-authorization-server:latest \
  --config=/config/config.yml
```

CI 会在 `main` 推送时构建并发布 `linux/amd64` 与 `linux/arm64` 镜像。

## Docker Compose

见 [`docker/README.md`](docker/README.md)。

```bash
# HTTP
docker compose -f docker/docker-compose.yml up

# HTTPS（先生成证书）
./scripts/generate-tls-certs.sh
docker compose -f docker/docker-compose.tls.yml up

# mTLS
docker compose -f docker/docker-compose.mtls.yml up
```

## TLS / mTLS

本项目基于 Spring Boot SSL Bundle。仓库提供现成样例（**仅本地 / 测试**）：

| 文件                                                                   | 说明                        |
|------------------------------------------------------------------------|-----------------------------|
| [`configs/tls/application-tls.yml`](configs/tls/application-tls.yml)   | HTTPS only                  |
| [`configs/tls/application-mtls.yml`](configs/tls/application-mtls.yml) | mTLS（`client-auth: NEED`） |
| [`scripts/generate-tls-certs.sh`](scripts/generate-tls-certs.sh)       | 生成自签 PEM                |
| [`configs/tls/README.md`](configs/tls/README.md)                       | 详细步骤                    |

```bash
./scripts/generate-tls-certs.sh
./mvnw -ntp -B clean package -DskipTests
java -jar target/rose-authorization-server.jar \
  --config=configs/config.yml \
  --spring.config.additional-location=optional:file:configs/tls/application-tls.yml
```

CLI 会放行 `--spring.*` / `--server.*` 等 Boot 属性，便于 Compose 与 TLS 叠加配置。

启用 TLS 后，客户端 `issuer-uri` 需为 `https://...`，并信任 `configs/tls/certs/server.crt`（或仅在测试中关闭校验）。

参考：

- [Securing Spring Boot Applications With SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl)
- [用 OpenSSL 生成自签证书](https://stackoverflow.com/questions/10175812/how-can-i-generate-a-self-signed-ssl-certificate-using-openssl)

## 健康检查与发现

| 端点 | 说明 |
|---|---|
| `GET /actuator/health` | 存活 / 就绪 |
| `GET /.well-known/openid-configuration` | OIDC Discovery |
| `GET /oauth2/jwks` | JWK Set |

## 文档

| 文档                                           | 说明                |
|------------------------------------------------|---------------------|
| [docker/README.md](docker/README.md)           | Docker Compose 示例 |
| [configs/tls/README.md](configs/tls/README.md) | TLS / mTLS 样例     |
| [LICENSE](LICENSE)                             | Apache License 2.0  |

## 许可

Copyright © zhijun-io contributors. Licensed under the [Apache License 2.0](LICENSE).
