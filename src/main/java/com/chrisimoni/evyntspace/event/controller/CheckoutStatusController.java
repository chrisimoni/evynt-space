package com.chrisimoni.evyntspace.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/checkout")
public class CheckoutStatusController {
    @GetMapping("/success")
    public String showSuccessPage() {
        return "success";
    }

    @GetMapping("/cancel")
    public String showCancelPage() {
        return "cancel";
    }
}
