package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.config.AppConfigProperties;
import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.model.AuthMember;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.kitchensink.constant.KitchensinkConstant.*;
import static com.mongodb.kitchensink.error.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfigProperties appConfigProperties;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user: '{}'.", username);
        return memberRepository.findByEmail(username)
                .map(AuthMember::new)
                .orElseThrow(() -> new UsernameNotFoundException("User with email '%s' not found".formatted(username)));
    }

    public MemberDTO findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberDTO::from)
                .orElseThrow(() ->
                        KitchenSinkException
                                .builder()
                                .errorCode(NOT_FOUND)
                                .message("User with email '%s' not found".formatted(email))
                                .build()
                );
    }

    public List<MemberDTO> findAll() {
        return memberRepository.findAll()
                .stream()
                .map(MemberDTO::from)
                .toList();
    }

    public MemberDTO save(MemberCO memberCO) {
        Member savedMember = memberRepository.save(memberCO.to(passwordEncoder, appConfigProperties.defaultUserRoles()));
        return MemberDTO.from(savedMember);
    }

    public void delete(String email) {
        memberRepository.deleteByEmail(email);
    }

    public MemberDTO update(UpdateMemberCO updateMemberCO, String email) {
        return memberRepository.findByEmail(email)
                .map(member -> {
                    updateMemberCO.merge(passwordEncoder, member);
                    return memberRepository.save(member);
                })
                .map(MemberDTO::from)
                .orElseThrow(() -> KitchenSinkException
                        .builder()
                        .errorCode(NOT_FOUND)
                        .message("User with email '%s' not found".formatted(email))
                        .build()
                );
    }

    public MemberDTO updateRoles(List<String> roles, String email) {
        log.info("For user: '{}', updating roles '{}'.", email, roles);

        return memberRepository.findByEmail(email)
                .map(member -> {
                    member.setRoles(roles);
                    return memberRepository.save(member);
                })
                .map(MemberDTO::from)
                .orElseThrow(() -> KitchenSinkException
                        .builder()
                        .errorCode(NOT_FOUND)
                        .message("User with email '%s' not found".formatted(email))
                        .build()
                );
    }
}
