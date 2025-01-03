package com.mongodb.kitchensink.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getMembers() {
        return Map.of("hi", "hello");
    }
}
