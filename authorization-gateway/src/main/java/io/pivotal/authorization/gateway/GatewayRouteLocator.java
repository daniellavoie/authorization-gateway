package io.pivotal.authorization.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.Route.AsyncBuilder;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class GatewayRouteLocator implements RouteLocator {
	private RoutesConfiguration routesConfiguration;
	private RouteLocatorBuilder routeLocatorBuilder;

	private Flux<Route> routes;

	public GatewayRouteLocator(RoutesConfiguration routesConfiguration, RouteLocatorBuilder routeLocatorBuilder) {
		this.routesConfiguration = routesConfiguration;
		this.routeLocatorBuilder = routeLocatorBuilder;

		refreshRoutes();
	}

	@Override
	public Flux<Route> getRoutes() {
		return routes;
	}

	private void refreshRoutes() {
		List<ServiceEntry> services = Optional.ofNullable(routesConfiguration.getRoutes())
				.orElseGet(() -> new HashMap<>()).keySet().stream().map(key -> key.substring(0, key.indexOf(".")))
				.distinct().map(this::buildServiceEntry).collect(Collectors.toList());

		Builder builder = routeLocatorBuilder.routes();

		for (ServiceEntry serviceEntry : services) {
			for (EndpointEntry endpointEntry : serviceEntry.getEndpoints()) {
				builder = builder.route(serviceEntry.getServiceName() + "-" + endpointEntry.getEndpointName(),
						spec -> buildSpec(spec, endpointEntry));
			}
		}

		routes = builder.build().getRoutes();

	}

	private EndpointEntry buildEndpointEntry(String serviceName, String endpointName) {
		return new EndpointEntry(endpointName,
				routesConfiguration.getRoutes().get(serviceName + "." + endpointName + ".host"),
				routesConfiguration.getRoutes().get(serviceName + "." + endpointName + ".path"),
				routesConfiguration.getRoutes().get(serviceName + "." + endpointName + ".method"),
				Arrays.stream(
						routesConfiguration.getRoutes().get(serviceName + "." + endpointName + ".scopes").split(","))
						.map(scope -> scope.trim()).collect(Collectors.toList()).toArray(new String[] {}),
				routesConfiguration.getRoutes().get(serviceName + "." + endpointName + ".redirect-host"));
	}

	private ServiceEntry buildServiceEntry(String serviceName) {
		return new ServiceEntry(serviceName,
				routesConfiguration.getRoutes().keySet().stream().filter(key -> key.startsWith(serviceName + "."))
						.map(this::mapEndpointKey).map(this::mapEndpointName).distinct()
						.map(endpointName -> buildEndpointEntry(serviceName, endpointName))
						.collect(Collectors.toList()));
	}

	private AsyncBuilder buildSpec(PredicateSpec spec, EndpointEntry endpointEntry) {
		return spec.host(endpointEntry.getHost()).and().path(endpointEntry.getPath()).and()
				.method(endpointEntry.getMethod()).uri(endpointEntry.getRedirectHost()).predicate(exchange -> {
					// TODO - Regex matching.
					// exchange.getRequest().get;
					return true;
				});
	}

	private String mapEndpointKey(String serviceKey) {
		return serviceKey.substring(serviceKey.indexOf(".") + 1);
	}

	private String mapEndpointName(String endpointKey) {
		return endpointKey.substring(0, endpointKey.indexOf("."));
	}

	private class ServiceEntry {
		private final String serviceName;
		private final List<EndpointEntry> endpoints;

		public ServiceEntry(String serviceName, List<EndpointEntry> endpoints) {
			this.serviceName = serviceName;
			this.endpoints = endpoints;
		}

		public String getServiceName() {
			return serviceName;
		}

		public List<EndpointEntry> getEndpoints() {
			return endpoints;
		}
	}

	private class EndpointEntry {
		private final String endpointName;
		private final String host;
		private final String path;
		private final String method;
		private final String[] scopes;
		private final String redirectHost;

		public EndpointEntry(String endpointName, String host, String path, String method, String[] scopes,
				String redirectHost) {
			this.endpointName = endpointName;
			this.host = host;
			this.path = path;
			this.method = method;
			this.scopes = scopes;
			this.redirectHost = redirectHost;
		}

		public String getEndpointName() {
			return endpointName;
		}

		public String getHost() {
			return host;
		}

		public String getPath() {
			return path;
		}

		public String getMethod() {
			return method;
		}

		public String[] getScopes() {
			return scopes;
		}

		public String getRedirectHost() {
			return redirectHost;
		}

	}

}
