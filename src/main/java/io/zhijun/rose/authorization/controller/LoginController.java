package io.zhijun.rose.authorization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class LoginController {
    @GetMapping({"/login"})
    String loginPage(@RequestParam(value = "continue", required = false) String cont) {
        return cont!=null ? "redirect:/":"login";
    }
}