package com.mongodb.kitchensink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.kitchensink.error.ErrorCode;
import com.mongodb.kitchensink.error.KitchenSinkException;
import com.mongodb.kitchensink.error.RestExceptionHandling;
import com.mongodb.kitchensink.service.JwtService;
import com.mongodb.kitchensink.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableConfigurationProperties(AppConfigProperties.class)
public class AuthConfiguration {

    private final RestExceptionHandling restExceptionHandling;
    private final AppConfigProperties appConfigProperties;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        return http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers(HttpMethod.POST, "/api/members").anonymous()
                                .anyRequest().authenticated()
                )
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, AuthorizationFilter.class)
                .exceptionHandling(customizer -> customizer
                        .authenticationEntryPoint((request, response, authException) -> restExceptionHandling.setErrorResponse(response, authException, ErrorCode.UNAUTHENTICATED))
                        .accessDeniedHandler((request, response, authException) -> restExceptionHandling.setErrorResponse(response, authException, ErrorCode.UNAUTHORIZED))
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        return http
//                .csrf(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/actuator/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(customizer -> customizer
                        .successHandler((request, response, authentication) -> {
                            try {
                                response.setStatus(HttpServletResponse.SC_OK);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                JwtService.JwtTokenResponse value = jwtService.generateJwtToken(authentication);
                                objectMapper.writeValue(response.getWriter(), value);
                            } catch (KitchenSinkException e) {
                                restExceptionHandling.setErrorResponse(response, e);
                            }
                        })
                        .failureHandler((request, response, exception) -> restExceptionHandling.setErrorResponse(response, exception, ErrorCode.UNAUTHENTICATED))
                )
                .build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, MemberService memberService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(memberService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(appConfigProperties.allowedOrigins());
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
