package org.soldatenwohnen

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(
                        "/",
                        "/wohnungen",
                        "/wohnungen/**",
                        "/css/**",
                        "/webjars/**",
                        "/favicon.ico"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { }
            .httpBasic { }
            .build()
}
