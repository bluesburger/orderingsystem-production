package br.com.bluesburguer.production.adapters.in.sqs.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class OrderPaidUnitTests {
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldConstructFromJson() throws JsonMappingException, JsonProcessingException {
		String json = "{\"orderId\":1}";
		OrderPaid orderPaid = mapper.readValue(json, OrderPaid.class);
		
		assertThat(orderPaid).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", 1L);
	}
	
	@Test
	void shouldViewToStringWithOrderId() {
		var orderPaid = OrderPaid.builder().orderId(1L).build();
		assertThat(orderPaid).isNotNull()
			.hasToString("OrderPaid(super=OrderEvent(orderId=1))");
	}
}
