package de.menkalian.aquila.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository


@Configuration
@EnableWebFluxSecurity
class EndpointSecurityConfiguration {

    @Bean
    fun filterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http.authorizeExchange()
            .pathMatchers(HttpMethod.GET, "/login/**", "/oauth2/authorization/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/user/me").authenticated()
            .pathMatchers(HttpMethod.GET).permitAll()
            .pathMatchers(HttpMethod.POST, "/plugins/all").permitAll()
            .anyExchange().authenticated()
            .and()
            .csrf()
            .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse().apply { setHeaderName("X-CSRF-TOKEN") })
            .disable()
            //.and()
            .oauth2Login(Customizer.withDefaults())
        return http.build()
    }
}