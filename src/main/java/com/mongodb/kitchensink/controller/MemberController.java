package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MemberDTO>> getMembers() {
        return ResponseEntity.ok(memberService.findAll());
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name.equals(#email)")
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable String email) {
        return ResponseEntity.ok(memberService.findByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDTO> createMember(@RequestBody MemberCO memberCO, HttpServletRequest request) {
        MemberDTO saveMember = memberService.save(memberCO);
        URI userByEmail = URI.create(request.getRequestURL().append("/").append(saveMember.email()).toString());

        return ResponseEntity.created(userByEmail).body(saveMember);
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name.equals(#email)")
    public ResponseEntity<Void> deleteMember(@PathVariable String email) {
        memberService.delete(email);

        return ResponseEntity.noContent().build();
    }
}
