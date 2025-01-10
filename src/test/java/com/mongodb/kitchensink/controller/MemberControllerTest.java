package com.mongodb.kitchensink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.error.ErrorCode;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.model.co.MemberCO;
import com.mongodb.kitchensink.model.co.UpdateMemberCO;
import com.mongodb.kitchensink.model.dto.MemberDTO;
import com.mongodb.kitchensink.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetMembers() throws Exception {
        MemberDTO member1 = MemberDTO.builder().id("1").email("memeber1@example.com").build();
        MemberDTO member2 = MemberDTO.builder().id("2").email("memeber2@example.com").build();

        when(memberService.findAll()).thenReturn(List.of(member1, member2));

        mockMvc.perform(get("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].email").value("memeber1@example.com"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].email").value("memeber2@example.com"));
    }

    @Test
    void shouldGetMemberByEmail() throws Exception {
        String email = "member@example.com";
        MemberDTO member = MemberDTO.builder().id("1").email(email).build();

        when(memberService.findByEmail(email)).thenReturn(member);

        mockMvc.perform(get("/api/members/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.email").value("member@example.com"));
    }

    @Test
    void shouldGetNotFoundIfNoMemberReturned() throws Exception {
        String email = "member@example.com";

        when(memberService.findByEmail(email)).thenThrow(KitchenSinkException.builder().errorCode(ErrorCode.NOT_FOUND).message("User not found").build());

        mockMvc.perform(get("/api/members/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void shouldCreateMember() throws Exception {
        MemberCO memberCO = MemberCO.builder()
                .email("member@example.com")
                .name("Some Member")
                .password("strong-password")
                .phoneNumber("+91XXXXXXXXXX")
                .build();
        MemberDTO memberDTO = MemberDTO.builder()
                .id("1a")
                .email("member@example.com")
                .name("Some Member")
                .phoneNumber("+91XXXXXXXXXX")
                .roles(List.of("SOME_ROLE"))
                .build();
        ArgumentCaptor<MemberCO> captor = ArgumentCaptor.forClass(MemberCO.class);

        when(memberService.save(captor.capture())).thenReturn(memberDTO);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberCO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/members/member@example.com"))
                .andExpect(jsonPath("$.id").value("1a"))
                .andExpect(jsonPath("$.email").value("member@example.com"))
                .andExpect(jsonPath("$.name").value("Some Member"))
                .andExpect(jsonPath("$.phoneNumber").value("+91XXXXXXXXXX"))
                .andExpect(jsonPath("$.roles[0]").value("SOME_ROLE"));

        assertThat(captor.getValue())
                .extracting(MemberCO::email, MemberCO::name, MemberCO::phoneNumber, MemberCO::password)
                .containsExactly("member@example.com", "Some Member", "+91XXXXXXXXXX", "strong-password");
    }

    @Test
    void shouldUpdateMember() throws Exception {
        String email = "test@example.com";
        UpdateMemberCO updateMemberCO = UpdateMemberCO.builder().name("Updated Test User").build();
        MemberDTO updatedMemberDTO = MemberDTO.builder()
                .id("1b")
                .email(email)
                .name("Updated Test User")
                .build();
        ArgumentCaptor<UpdateMemberCO> captor = ArgumentCaptor.forClass(UpdateMemberCO.class);

        when(memberService.update(captor.capture(), eq(email))).thenReturn(updatedMemberDTO);

        mockMvc.perform(patch("/api/members/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMemberCO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1b"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Updated Test User"));
    }

    @Test
    void shouldDeleteMember() throws Exception {
        String email = "test@example.com";

        mockMvc.perform(delete("/api/members/{email}", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(memberService, times(1)).delete(email);
    }
}
