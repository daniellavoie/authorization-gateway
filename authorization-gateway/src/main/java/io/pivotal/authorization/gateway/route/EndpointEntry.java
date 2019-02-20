package io.pivotal.authorization.gateway.route;

public class EndpointEntry {
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
