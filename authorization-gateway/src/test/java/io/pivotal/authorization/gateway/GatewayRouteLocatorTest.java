package io.pivotal.authorization.gateway;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthorizationGatewayApplication.class)
public class GatewayRouteLocatorTest {
	@Autowired
	private GatewayRouteLocator gatewayRouteLocator;

	@Test
	public void testRoutes() {
		Assert.assertNotNull(gatewayRouteLocator.getRoutes().collectList().block());
	}
}
