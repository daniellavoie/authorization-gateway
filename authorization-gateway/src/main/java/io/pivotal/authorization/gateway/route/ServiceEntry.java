package io.pivotal.authorization.gateway.route;

import java.util.List;

public class ServiceEntry {
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
