package org.soldatenwohnen.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
	@Bean
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http.authorizeHttpRequests { authorizationConfigurer ->
			authorizationConfigurer.anyRequest().authenticated()
		}
		http.userDetailsService(userDetailsService())
		return http.build()
	}

	@Bean
	fun userDetailsService(): UserDetailsService {
		val user =
			User.withUsername("jonathan")
				.password(passwordEncoder().encode("test"))
				.authorities("read")
				.build()

		return InMemoryUserDetailsManager(user)
	}

	@Bean
	fun passwordEncoder(): PasswordEncoder {
		return BCryptPasswordEncoder()
	}
}


//	@Bean
//	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
//		http
//			.authorizeHttpRequests { requests ->
//				requests
//					.requestMatchers(
//						"/",
//						"/htmx-test",
//						"/wohnungen",
//						"/wohnungen/**",
//						"/kunden",
//						"/kunden/**",
//						"/css/**",
//						"/webjars/**",
//						"/favicon.ico"
//					).permitAll()
//					.anyRequest().authenticated()
//			}
//			.csrf { it.disable() }
//			.formLogin { }
//			.httpBasic { }
//			.build()
