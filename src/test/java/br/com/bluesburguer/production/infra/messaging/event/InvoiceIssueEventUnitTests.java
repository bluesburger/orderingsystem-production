package br.com.bluesburguer.production.infra.messaging.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class InvoiceIssueEventUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	ObjectMapper mapper = new ObjectMapper();

	@Test
	void shouldBuildWithOrderId() {
		var order = IssueInvoiceEvent.builder().orderId(ORDER_ID).build();
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("InvoiceIssueEvent(super=OrderEvent(orderId=%s))", ORDER_ID));
	}
	
	@Test
	void shoulInstanceWithOrderId() {
		var order = new IssueInvoiceEvent(ORDER_ID);
		
		assertThat(order).isNotNull();
		assertThat(order.toString()).isNotNull()
			.isEqualTo(String.format("InvoiceIssueEvent(super=OrderEvent(orderId=%s))", ORDER_ID));
	}

	@Test
	void shouldConstructFromJson() throws JsonProcessingException {
		String json = String.format("{\"orderId\":\"%s\"}", ORDER_ID);
		IssueInvoiceEvent order = mapper.readValue(json, IssueInvoiceEvent.class);
		
		assertThat(order).isNotNull()
			.hasFieldOrPropertyWithValue("orderId", ORDER_ID);
	}

}
