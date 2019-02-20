package io.pivotal.authorization.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration
public class JwtConfig {
	@Bean
	public JwtDecoder jwtDecoder(GatewayJwtConfiguration gatewayJwtConfiguration) {
		return JwtDecoders.fromOidcIssuerLocation(gatewayJwtConfiguration.getIssuerUri());
	}
}
