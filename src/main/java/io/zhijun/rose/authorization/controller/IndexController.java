package io.zhijun.rose.authorization.controller;

import io.zhijun.rose.authorization.authentication.CustomUserDetailsService;
import io.zhijun.rose.authorization.config.ConfigurationPrinter;
import io.zhijun.rose.authorization.oauth2.client.CustomRegisteredClientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class IndexController {
    private final CustomUserDetailsService userDetailsService;
    private final CustomRegisteredClientRepository registeredClientRepository;

    IndexController(CustomUserDetailsService userDetailsService, CustomRegisteredClientRepository registeredClientRepository) {
        this.userDetailsService = userDetailsService;
        this.registeredClientRepository = registeredClientRepository;
    }

    @GetMapping({"/"})
    String landingPage(Model model, Authentication authentication) {
        model.addAttribute("users", this.userDetailsService.getUsers());
        model.addAttribute("clients", this.registeredClientRepository.getRegisteredClients());
        model.addAttribute("authentication", authentication);
        model.addAttribute("helpMessage", ConfigurationPrinter.getHelpString());
        return "index";
    }
}
