package br.com.bluesburguer.production.adapters.in.sqs.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class OrderDeliveredUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldPrintToStringWithOrderId() {
		var order = OrderDelivered.builder().orderId(ORDER_ID).build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo("OrderDelivered(super=OrderEvent(orderId=1))");
	}

	@Test
	void shouldConstructFromJson() throws JsonMappingException, JsonProcessingException {
		String json = "{\"orderId\":1}";
		OrderDelivered order = mapper.readValue(json, OrderDelivered.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", 1L);
	}
}
