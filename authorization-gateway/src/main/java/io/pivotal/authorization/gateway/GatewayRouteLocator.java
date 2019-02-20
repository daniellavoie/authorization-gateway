package io.pivotal.authorization.gateway;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.Route.AsyncBuilder;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import io.pivotal.authorization.gateway.route.EndpointEntry;
import io.pivotal.authorization.gateway.route.ServiceEntry;
import reactor.core.publisher.Flux;

@Component
public class GatewayRouteLocator implements RouteLocator {
	private RoutesConfiguration routesConfiguration;
	private RouteLocatorBuilder routeLocatorBuilder;
	private JwtDecoder jwtDecoder;

	private Flux<Route> routes;

	public GatewayRouteLocator(RoutesConfiguration routesConfiguration, RouteLocatorBuilder routeLocatorBuilder,
			JwtDecoder jwtDecoder) {
		this.routesConfiguration = routesConfiguration;
		this.routeLocatorBuilder = routeLocatorBuilder;
		this.jwtDecoder = jwtDecoder;

		refreshRoutes();
	}

	@Override
	public Flux<Route> getRoutes() {
		return routes;
	}

	private void refreshRoutes() {
		List<ServiceEntry> services = routesConfiguration.getServiceEntries();

		Builder builder = routeLocatorBuilder.routes();

		for (ServiceEntry serviceEntry : services) {
			for (EndpointEntry endpointEntry : serviceEntry.getEndpoints()) {
				builder = builder.route(serviceEntry.getServiceName() + "-" + endpointEntry.getEndpointName(),
						spec -> buildSpec(spec, endpointEntry));
			}
		}

		routes = builder.build().getRoutes();

	}

	private AsyncBuilder buildSpec(PredicateSpec spec, EndpointEntry endpointEntry) {
		return spec.host(endpointEntry.getHost()).and().path(endpointEntry.getPath()).and()
				.method(endpointEntry.getMethod()).uri(endpointEntry.getRedirectHost()).predicate(exchange -> {
					// TODO - Regex matching.
					return true;
				}).filter(new AuthorizationFilter(jwtDecoder,
						Arrays.stream(endpointEntry.getScopes()).collect(Collectors.toList())));
	}
}
