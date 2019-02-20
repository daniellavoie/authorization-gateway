package io.pivotal.authorization.gateway;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

public class AuthorizationFilter implements GatewayFilter {
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);

	private JwtDecoder jwtDecoder;
	private List<String> scopes;

	public AuthorizationFilter(JwtDecoder jwtDecoder, List<String> scopes) {
		this.jwtDecoder = jwtDecoder;
		this.scopes = scopes;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		return Mono.create(consumer -> filterCallback(consumer, exchange));
	}

	private Optional<Jwt> decodeJwt(String token) {
		try {
			return Optional.of(jwtDecoder.decode(token));
		} catch (JwtException jwtEx) {
			String errorMessage = "Failed to decode jwt token";

			if (LOGGER.isDebugEnabled()) {
				LOGGER.error(errorMessage, jwtEx);
			} else {
				LOGGER.warn("{}. Error : {}", errorMessage, jwtEx.getMessage());
			}

			return Optional.empty();
		}
	}

	private Optional<String> extractJwtToken(List<String> authorizationHeaders) {
		return authorizationHeaders.stream().filter(authorizationHeader -> authorizationHeader.startsWith("bearer "))
				.map(authorizationHeader -> authorizationHeader.substring(7)).findFirst();
	}

	private void filterCallback(MonoSink<Void> consumer, ServerWebExchange exchange) {
		try {
			Optional<String> optionalToken = Optional
					.ofNullable(exchange.getRequest().getHeaders().get("Authorization"))
					.flatMap(headers -> extractJwtToken(headers));

			if (!optionalToken.isPresent()) {
				consumer.error(new HttpServerErrorException(HttpStatus.FORBIDDEN));
				return;
			}

			// Check for a JWT
			Optional<Jwt> optionalJwt = decodeJwt(optionalToken.get());

			if (!optionalJwt.isPresent()) {
				consumer.error(new HttpServerErrorException(HttpStatus.UNAUTHORIZED));
				return;
			}

			// Validate the scopes.
			Jwt jwt = optionalJwt.get();
			List<String> claims = jwt.getClaimAsStringList("scope");
			if (claims == null) {
				LOGGER.error("No claims found within jwt token.");

				consumer.error(new HttpServerErrorException(HttpStatus.UNAUTHORIZED));
				return;
			}

			if (scopes.stream().filter(endpointScope -> claims.contains(endpointScope)).findFirst().isPresent()) {
				consumer.success();
			} else {
				consumer.error(new HttpServerErrorException(HttpStatus.UNAUTHORIZED));
			}
		} catch (Exception ex) {
			LOGGER.error("Failed to process filter.", ex);

			consumer.error(ex);
		}
	}
}
