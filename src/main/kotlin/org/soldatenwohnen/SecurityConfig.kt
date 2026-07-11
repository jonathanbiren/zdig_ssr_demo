//package org.soldatenwohnen
//
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.security.config.annotation.web.builders.HttpSecurity
//import org.springframework.security.web.SecurityFilterChain
//
//@Configuration
//class SecurityConfig {
//    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
//        http
//            .authorizeHttpRequests { requests ->
//                requests
//                    .requestMatchers(
//                        "/",
//                        "/htmx-test",
//                        "/wohnungen",
//                        "/wohnungen/**",
//                         "/kunden",
//                         "/kunden/**",
//                        "/css/**",
//                        "/webjars/**",
//                        "/favicon.ico"
//                    ).permitAll()
//                    .anyRequest().authenticated()
//            }
//            .csrf { it.disable() }
//            .formLogin { }
//            .httpBasic { }
//            .build()
//}
