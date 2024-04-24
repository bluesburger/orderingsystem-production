package br.com.bluesburguer.orderingsystem.production.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Status;
import br.com.bluesburguer.orderingsystem.order.domain.Step;

class OrderStatusUpdatedUnitTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();    

	@Test
	void shouldInstance_WithDefaultDateTime() {
		var id = UUID.randomUUID();
		var status = new Status(Step.KITCHEN, Fase.IN_PROGRESS);
		var orderStatus = new OrderStatusUpdated(id, status);
		
		assertThat(orderStatus).isNotNull()
			.hasFieldOrPropertyWithValue("newStatus", status)
			.hasNoNullFieldsOrProperties();
	}
	
	@Test
	void shouldSerialize() throws JsonProcessingException {
		var id = UUID.randomUUID();
		var status = new Status(Step.KITCHEN, Fase.IN_PROGRESS);
		var orderStatus = new OrderStatusUpdated(id, status);
		
		var json = OBJECT_MAPPER.writeValueAsString(orderStatus);
		
		assertThat(json)
			.isNotNull()
			.isNotEmpty();
	}
	
	@Test
	public void givenUsingApache_whenGeneratingRandomAlphanumericString_thenCorrect() {
	    String generatedString = RandomStringUtils.randomAlphanumeric(10);

	    System.out.println(generatedString);
	}
}
