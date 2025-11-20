package com.chrisimoni.evyntspace.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthRedirectController {

    @GetMapping("/oauth2/redirect")
    public ResponseEntity<Map<String, String>> oauth2Redirect(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken,
            @RequestParam(required = false) String error) {

        Map<String, String> response = new HashMap<>();

        if (error != null) {
            response.put("status", "error");
            response.put("message", error);
            return ResponseEntity.badRequest().body(response);
        }

        response.put("status", "success");
        response.put("message", "OAuth2 authentication successful");

        if (accessToken != null) {
            response.put("accessToken", accessToken);
        }

        if (refreshToken != null) {
            response.put("refreshToken", refreshToken);
        }

        return ResponseEntity.ok(response);
    }
}
