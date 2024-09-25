package com.example.edgeservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.WebFilter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
		return new WebSessionServerOAuth2AuthorizedClientRepository();
	}

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
		return http
				.authorizeExchange(exchange -> exchange
						.pathMatchers("/actuator/**").permitAll()
						.pathMatchers("/", "/*.css", "/*.js", "/favicon.ico").permitAll()
						.pathMatchers(HttpMethod.GET, "/books/**").permitAll()
						.anyExchange().authenticated()
				)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
				.oauth2Login(Customizer.withDefaults())
				.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)))
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
						.csrfTokenRequestHandler(new SpaServerCsrfTokenRequestHandler())
				)
				.build();
	}

	private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
		var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
		return oidcLogoutSuccessHandler;
	}

	@Bean
	WebFilter csrfWebFilter() {
		// Required because of https://github.com/spring-projects/spring-security/issues/5766
		return (exchange, chain) -> {
			exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
				Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
				return csrfToken != null ? csrfToken.then() : Mono.empty();
			}));
			return chain.filter(exchange);
		};
	}

	static final class SpaServerCsrfTokenRequestHandler extends ServerCsrfTokenRequestAttributeHandler {
		private final ServerCsrfTokenRequestAttributeHandler delegate = new XorServerCsrfTokenRequestAttributeHandler();

		@Override
		public void handle(ServerWebExchange exchange, Mono<CsrfToken> csrfToken) {
			/*
			 * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of the CsrfToken when it is rendered in the response body.
			 */
			this.delegate.handle(exchange, csrfToken);
		}

		@Override
		public Mono<String> resolveCsrfTokenValue(ServerWebExchange exchange, CsrfToken csrfToken) {
			final var hasHeader = exchange.getRequest().getHeaders().get(csrfToken.getHeaderName()).stream().filter(StringUtils::hasText).count() > 0;
			return hasHeader ? super.resolveCsrfTokenValue(exchange, csrfToken) : this.delegate.resolveCsrfTokenValue(exchange, csrfToken);
		}
	}
}