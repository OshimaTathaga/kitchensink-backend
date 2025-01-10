package com.mongodb.kitchensink.service;

import com.mongodb.kitchensink.config.AppConfigProperties;
import com.mongodb.kitchensink.document.Member;
import com.mongodb.kitchensink.error.ErrorCode;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.mapper.MemberMapper;
import com.mongodb.kitchensink.mapper.MemberMapperImpl;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.SerializationUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MemberMapper memberMapper = new MemberMapperImpl();

    private String defaultUserRole = "ROLE_USER";

    private MemberService memberService;

    private Member member;
    private MemberCO memberCO;
    private UpdateMemberCO updateMemberCO;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository, passwordEncoder, memberMapper, new AppConfigProperties(List.of(defaultUserRole), null, null));

        member = Member.builder()
                .id("1")
                .email("test@example.com")
                .name("Test User")
                .roles(List.of("ROLE_USER"))
                .phoneNumber("1234567890")
                .build();

        memberCO = MemberCO.builder()
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .phoneNumber("1234567890")
                .build();

        updateMemberCO = UpdateMemberCO.builder()
                .name("Updated Test User")
                .password("newPassword")
                .phoneNumber("0987654321")
                .build();
    }

    @Test
    void shouldFindByEmail() {
        String email = "test@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        MemberDTO result = memberService.findByEmail(email);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("1");
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.roles()).containsExactly("ROLE_USER");
        assertThat(result.phoneNumber()).isEqualTo("1234567890");
    }

    @Test
    void shouldFindByEmailNotFound() {
        String email = "notfound@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        KitchenSinkException exception = assertThrows(KitchenSinkException.class, () -> memberService.findByEmail(email));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("User with email 'notfound@example.com' not found");
    }

    @Test
    void shouldSave() {
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        MemberDTO result = memberService.save(memberCO);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.roles()).containsExactly("ROLE_USER");
        assertThat(result.phoneNumber()).isEqualTo("1234567890");

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldUpdate() {
        String email = "test@example.com";
        Member clonedMember = SerializationUtils.clone(member);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(clonedMember));
        when(memberRepository.save(any(Member.class))).thenReturn(clonedMember);

        MemberDTO result = memberService.update(updateMemberCO, email);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.name()).isEqualTo("Updated Test User");
        assertThat(result.phoneNumber()).isEqualTo("0987654321");

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void shouldUpdateNotFound() {
        String email = "notfound@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        KitchenSinkException exception = assertThrows(KitchenSinkException.class, () -> memberService.update(updateMemberCO, email));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(exception.getMessage()).contains("User with email 'notfound@example.com' not found");
    }

    @Test
    void shouldUpdateRoles() {
        String email = "test@example.com";
        Member clonedMember = SerializationUtils.clone(member);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(clonedMember));
        when(memberRepository.save(any(Member.class))).thenReturn(clonedMember);

        MemberDTO result = memberService.updateRoles(List.of("ROLE_ADMIN"), email);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.roles()).isEqualTo(List.of("ROLE_ADMIN"));
    }

    @Test
    void shouldDelete() {
        String email = "test@example.com";

        memberService.delete(email);

        verify(memberRepository).deleteByEmail(email);
    }

    @Test
    void shouldLoadUserByUsernameForSpring() {
        String username = "test@example.com";
        when(memberRepository.findByEmail(username)).thenReturn(Optional.of(member));

        UserDetails result = memberService.loadUserByUsername(username);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
        assertThat(result.getAuthorities()).hasSize(1).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_USER");
    }

    @Test
    void shouldLoadUserByUsernameNotFound() {
        String username = "notfound@example.com";
        when(memberRepository.findByEmail(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> memberService.loadUserByUsername(username));
    }
}
