package br.com.bluesburguer.production.support;

import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import br.com.bluesburguer.production.adapters.in.order.dto.OrderDto;
import wiremock.org.eclipse.jetty.http.HttpStatus;

public class OrderMocks {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static void mockOrderClientGetById(WireMockExtension wme, OrderDto orderDto) throws JsonProcessingException {
		var orderJson = mapper.writeValueAsString(orderDto);
		wme.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/order/" + orderDto.getId()))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK_200)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(orderJson)));
	}
	
	public static void mockOrderClientUpdateStepAndFase(WireMockExtension wme, OrderDto orderDto) throws JsonProcessingException {
		var orderJson = mapper.writeValueAsString(orderDto);
		var urlPath = String.format("/api/order/%s/%s/%s", orderDto.getId(), orderDto.getStep(), orderDto.getFase());
		wme.stubFor(WireMock.put(WireMock.urlPathEqualTo(urlPath))
				.willReturn(WireMock.aResponse()
						.withStatus(HttpStatus.OK_200)
						.withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
						.withBody(orderJson)));
	}
	
	public static String mockCpf() {
		return "997.307.080-10";
	}

	public static String mockEmail() {
		return "email.usuario@server.com";
	}
}
