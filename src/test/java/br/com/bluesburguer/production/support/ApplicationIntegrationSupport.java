package br.com.bluesburguer.production.support;

import java.util.List;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;

import br.com.bluesburguer.production.OrderingsystemProductionApplication;
import br.com.bluesburguer.production.application.dto.OrderDto;
import br.com.bluesburguer.production.application.dto.OrderItemDto;
import br.com.bluesburguer.production.application.dto.OrderRequest;
import br.com.bluesburguer.production.application.dto.UserDto;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
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
	
	public static void mockOrderClientCreateNewOrder(WireMockExtension wme, String uri, OrderRequest orderRequest) throws JsonProcessingException {
		wme.stubFor(WireMock.post(WireMock.urlPathEqualTo("/api/order"))
				.withRequestBody(new EqualToPattern(mapper.writeValueAsString(orderRequest)))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK_200)
						.withHeader("Location", uri)));
	}
	
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
	
	protected OrderDto mockOrderClient(WireMockExtension wme, String orderId, Step step, Fase fase) throws JsonProcessingException {
		var items = List.of(new OrderItemDto(1L, 1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(orderId, step, fase, items, user);

		mockOrderClientGetById(wme, orderDto);
		mockOrderClientUpdateStepAndFase(wme, orderDto);

		return orderDto;
	}
}
