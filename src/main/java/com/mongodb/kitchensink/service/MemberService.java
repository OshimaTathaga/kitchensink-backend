package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.model.AuthMember;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByEmail(username)
                .map(AuthMember::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with email '%s' not found".formatted(username)));
    }

    public MemberDTO findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberDTO::from)
                .orElseThrow(() -> new KitchenSinkException("User with email '%s' not found".formatted(email)));
    }

    public List<MemberDTO> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberDTO::from)
                .toList();
    }

    public MemberDTO save(MemberCO memberCO) {
        Member savedMember = memberRepository.save(memberCO.to(passwordEncoder));
        return MemberDTO.from(savedMember);
    }

    public void delete(String email) {
        memberRepository.deleteByEmail(email);
    }


}
