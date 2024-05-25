package br.com.bluesburguer.production.adapters.in.sqs.dto.notafiscal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.notafiscal.NotaFiscalFalhouNaEmissaoDto;

class NotaFiscalFalhouNaEmissaoDtoUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldBuildWithOrderId() {
		var order = NotaFiscalFalhouNaEmissaoDto.builder().orderId(ORDER_ID).build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("NotaFiscalFalhouNaEmissaoDto(super=OrderEventDto(orderId=%s))", ORDER_ID));
	}
	
	@Test
	void shoulInstanceWithOrderId() {
		var order = new NotaFiscalFalhouNaEmissaoDto(ORDER_ID);
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("NotaFiscalFalhouNaEmissaoDto(super=OrderEventDto(orderId=%s))", ORDER_ID));
	}

	@Test
	void shouldConstructFromJson() throws JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\"}", ORDER_ID);
		NotaFiscalFalhouNaEmissaoDto order = mapper.readValue(json, NotaFiscalFalhouNaEmissaoDto.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}

}
