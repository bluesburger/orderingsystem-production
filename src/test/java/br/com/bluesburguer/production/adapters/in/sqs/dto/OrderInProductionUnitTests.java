package br.com.bluesburguer.production.adapters.in.sqs.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class OrderInProductionUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldPrintToStringWithOrderId() {
		var order = OrderInProduction.builder().orderId(ORDER_ID).build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("OrderInProduction(super=OrderEvent(orderId=%s))", ORDER_ID));
	}

	@Test
	void shouldConstructFromJson() throws JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\"}", ORDER_ID);
		OrderInProduction order = mapper.readValue(json, OrderInProduction.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}
}
