package br.com.bluesburguer.production.application.sqs.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class InvoiceIssueEventUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldBuildWithOrderId() {
		var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("NotaFiscalEmitidaDto(super=OrderEventDto(orderId=%s))", ORDER_ID));
	}
	
	@Test
	void shoulInstanceWithOrderId() {
		var order = new InvoiceIssueEvent(ORDER_ID);
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("NotaFiscalEmitidaDto(super=OrderEventDto(orderId=%s))", ORDER_ID));
	}

	@Test
	void shouldConstructFromJson() throws JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\"}", ORDER_ID);
		InvoiceIssueEvent order = mapper.readValue(json, InvoiceIssueEvent.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}

}
