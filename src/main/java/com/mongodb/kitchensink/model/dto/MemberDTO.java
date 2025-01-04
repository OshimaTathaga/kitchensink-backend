package com.mongodb.kitchensink.model.dto;

import com.mongodb.kitchensink.document.Member;
import lombok.Builder;

import java.util.List;

@Builder
public record MemberDTO(String id, String email, String name, List<String> roles, String phoneNumber) {
    public static MemberDTO from(Member member) {
        return MemberDTO
                .builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .phoneNumber(member.getPhoneNumber())
                .roles(member.getRoles())
                .build();
    }
}
