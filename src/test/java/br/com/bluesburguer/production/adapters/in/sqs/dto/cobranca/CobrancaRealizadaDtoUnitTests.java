package br.com.bluesburguer.production.adapters.in.sqs.dto.cobranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.cobranca.CobrancaRealizadaDto;

class CobrancaRealizadaDtoUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldConstructFromJson() throws JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\"}", ORDER_ID);
		CobrancaRealizadaDto orderPaid = mapper.readValue(json, CobrancaRealizadaDto.class);
		
		assertThat(orderPaid).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}
	
	@Test
	void shouldViewToStringWithOrderId() {
		var orderPaid = CobrancaRealizadaDto.builder().orderId(ORDER_ID).build();
		assertThat(orderPaid).isNotNull()
			.hasToString(String.format("CobrancaRealizadaDto(super=OrderEventDto(orderId=%s))", ORDER_ID));
	}
}
