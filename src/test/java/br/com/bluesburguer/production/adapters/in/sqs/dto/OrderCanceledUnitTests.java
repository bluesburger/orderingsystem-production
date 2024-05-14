package br.com.bluesburguer.production.adapters.in.sqs.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.core.domain.Step;

class OrderCanceledUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldPrintToStringWithOrderId() {
		var order = OrderCanceled.builder()
				.orderId(ORDER_ID)
				.step(Step.KITCHEN)
				.build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("OrderCanceled(super=OrderEvent(orderId=%s), step=KITCHEN)", ORDER_ID));
	}

	@ParameterizedTest
	@EnumSource(Step.class)
	void shouldConstructFromJson(Step step) throws JsonMappingException, JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\", \"step\": \"%s\"}", ORDER_ID, step);
		OrderCanceled order = mapper.readValue(json, OrderCanceled.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}
}
