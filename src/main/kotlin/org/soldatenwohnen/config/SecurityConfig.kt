package org.soldatenwohnen.config

import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

//@Configuration
//class SecurityConfig {
//	@Bean
//	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//		http.authorizeHttpRequests { authorizationConfigurer ->
//			authorizationConfigurer.anyRequest().authenticated()
//		}
//		http.userDetailsService(userDetailsService())
//		return http.build()
//	}
//
//	@Bean
//	fun userDetailsService(): UserDetailsService {
//		val user =
//			User.withUsername("jonathan")
//				.password(passwordEncoder().encode("test"))
//				.authorities("read")
//				.build()
//
//		return InMemoryUserDetailsManager(user)
//	}
//
//	@Bean
//	fun passwordEncoder(): PasswordEncoder {
//		return BCryptPasswordEncoder()
//	}
//}


	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
		http
			.authorizeHttpRequests { requests ->
				requests
					.requestMatchers(
						"/",
						"/htmx-test",
						"/wohnungen",
						"/wohnungen/**",
						"/kunden",
						"/kunden/**",
						"/css/**",
						"/webjars/**",
						"/favicon.ico"
					).permitAll()
					.anyRequest().authenticated()
			}
			.csrf { csrfConfigurer -> csrfConfigurer.disable() }
			.formLogin { }
			.httpBasic { }
			.build()
