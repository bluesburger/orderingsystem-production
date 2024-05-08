package br.com.bluesburguer.orderingsystem.production;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;

public class OrderPaidTest {
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void constructFromJson() throws JsonMappingException, JsonProcessingException {
		String json = "{\"orderId\":1}";
		OrderPaid orderPaid = mapper.readValue(json, OrderPaid.class);
		
		assertThat(orderPaid).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", 1L);
	}
}
