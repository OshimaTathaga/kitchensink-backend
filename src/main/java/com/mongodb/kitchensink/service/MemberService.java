package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new KitchenSinkException("User with email '%s' not found".formatted(email)));
    }
}
