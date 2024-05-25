package br.com.bluesburguer.production.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import br.com.bluesburguer.production.OrderingsystemProductionApplication;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;
import wiremock.org.eclipse.jetty.http.HttpStatus;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(
		classes = { OrderingsystemProductionApplication.class },
		properties = { 
				"cloud.aws.credentials.access-key=AKIAIOSFODNN7EXAMPLE",
				"cloud.aws.credentials.secret-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
				"spring.main.allow-bean-definition-overriding=true",
				"spring.cloud.bus.enabled=false",
				"spring.cloud.consul.enabled=false", 
				"spring.cloud.consul.discovery.enabled=false",
				"cloud.aws.region.use-default-aws-region-chain=true",
				"cloud.aws.stack.auto=false",
				"cloud.aws.region.auto=false",
				"cloud.aws.stack=false",
				"cloud.aws.sqs.listener.auto-startup=false"
		},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({ "test" })
@ContextConfiguration(classes = OrderingsystemProductionApplication.class)
public abstract class ApplicationIntegrationSupport {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static void mockOrderClientGetById(WireMockExtension wme, OrderDto orderDto) throws JsonProcessingException {
		var orderJson = mockOrderAsJson(orderDto);
		wme.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/order/" + orderDto.getId()))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK_200)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(orderJson)));
	}
	
	public static void mockOrderClientUpdateStepAndFase(WireMockExtension wme, OrderDto orderDto) throws JsonProcessingException {
		var orderJson = mockOrderAsJson(orderDto);
		var urlPath = String.format("/api/order/%s/%s/%s", orderDto.getId(), orderDto.getStep(), orderDto.getFase());
		wme.stubFor(WireMock.put(WireMock.urlPathEqualTo(urlPath))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK_200)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(orderJson)));
	}
	
	public static String mockOrderAsJson(OrderDto orderDto) throws JsonProcessingException {
		return mapper.writeValueAsString(orderDto);
	}
}
