package com.mongodb.kitchensink.controller;

import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.mongodb.kitchensink.constant.KitchensinkConstant.EMAIL_VALIDATION_REGEX;

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
    public ResponseEntity<MemberDTO> getMemberByEmail(@PathVariable @Valid @Email(regexp = EMAIL_VALIDATION_REGEX) String email) {
        return ResponseEntity.ok(memberService.findByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDTO> createMember(@RequestBody @Valid MemberCO memberCO, HttpServletRequest request) {
        MemberDTO saveMember = memberService.save(memberCO);
        URI userByEmail = URI.create(request.getRequestURL().append("/").append(saveMember.email()).toString());

        return ResponseEntity.created(userByEmail).body(saveMember);
    }

    @PatchMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name.equals(#email)")
    public ResponseEntity<MemberDTO> update(@PathVariable @Valid @Email(regexp = EMAIL_VALIDATION_REGEX) String email, @RequestBody UpdateMemberCO updateMemberCO) {
        return ResponseEntity.ok(memberService.update(updateMemberCO, email));
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or authentication.name.equals(#email)")
    public ResponseEntity<Void> deleteMember(@PathVariable @Valid @Email(regexp = EMAIL_VALIDATION_REGEX) String email) {
        memberService.delete(email);

        return ResponseEntity.noContent().build();
    }
}
