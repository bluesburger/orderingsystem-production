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
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldPrintToStringWithOrderId() {
		var order = OrderCanceled.builder()
				.orderId(1L)
				.step(Step.KITCHEN)
				.build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo("OrderCanceled(super=OrderEvent(orderId=1), step=KITCHEN)");
	}

	@ParameterizedTest
	@EnumSource(Step.class)
	void shouldConstructFromJson(Step step) throws JsonMappingException, JsonProcessingException {
		String json = "{\"orderId\":1, \"step\": \"" + step + "\"}";
		OrderCanceled order = mapper.readValue(json, OrderCanceled.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", 1L);
	}
}
