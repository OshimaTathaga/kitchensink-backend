package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.mapper.MemberMapper;
import com.mongodb.kitchensink.model.AuthMember;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.mongodb.kitchensink.error.ErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;
    @Value("${app.default-user-role}")
    private final String defaultUserRole;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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
        Member savedMember = memberRepository.save(memberCO.to(passwordEncoder, List.of(defaultUserRole)));
        return MemberDTO.from(savedMember);
    }

    public void delete(String email) {
        memberRepository.deleteByEmail(email);
    }

    public MemberDTO update(UpdateMemberCO updateMemberCO, String email) {
        return memberRepository.findByEmail(email)
                .map(member -> {
                    memberMapper.updateMember(updateMemberCO, member);
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
