package io.pivotal.authorization.gateway;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import io.pivotal.authorization.gateway.route.EndpointEntry;
import io.pivotal.authorization.gateway.route.ServiceEntry;

@Configuration
@ConfigurationProperties("authorization.gateway")
public class RoutesConfiguration {
	private Map<String, String> routes;

	public Map<String, String> getRoutes() {
		return routes;
	}

	public void setRoutes(Map<String, String> routes) {
		this.routes = routes;
	}

	public List<ServiceEntry> getServiceEntries() {
		return Optional.ofNullable(routes).orElseGet(() -> new HashMap<>()).keySet().stream()
				.map(key -> key.substring(0, key.indexOf("."))).distinct().map(this::buildServiceEntry)
				.collect(Collectors.toList());
	}

	private EndpointEntry buildEndpointEntry(String serviceName, String endpointName) {
		return new EndpointEntry(endpointName,
				routes.get(serviceName + "." + endpointName + ".host"),
				routes.get(serviceName + "." + endpointName + ".path"),
				routes.get(serviceName + "." + endpointName + ".method"),
				Arrays.stream(
						routes.get(serviceName + "." + endpointName + ".scopes").split(","))
						.map(scope -> scope.trim()).collect(Collectors.toList()).toArray(new String[] {}),
				routes.get(serviceName + "." + endpointName + ".redirect-host"));
	}

	private ServiceEntry buildServiceEntry(String serviceName) {
		return new ServiceEntry(serviceName,
				routes.keySet().stream().filter(key -> key.startsWith(serviceName + "."))
						.map(this::mapEndpointKey).map(this::mapEndpointName).distinct()
						.map(endpointName -> buildEndpointEntry(serviceName, endpointName))
						.collect(Collectors.toList()));
	}

	private String mapEndpointKey(String serviceKey) {
		return serviceKey.substring(serviceKey.indexOf(".") + 1);
	}

	private String mapEndpointName(String endpointKey) {
		return endpointKey.substring(0, endpointKey.indexOf("."));
	}
}
