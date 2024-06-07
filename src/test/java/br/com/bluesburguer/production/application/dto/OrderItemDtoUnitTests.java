package br.com.bluesburguer.production.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemDtoUnitTests {

	@Test
	void whenInstance_thenShouldHaveValues() {
		long id = 1L;
		int quantity = 1;
		var orderItemDto = new OrderItemDto(id, quantity);
		assertThat(orderItemDto)
			.hasFieldOrPropertyWithValue("id", id)
			.hasFieldOrPropertyWithValue("quantity", quantity)
			.hasToString(String.format("OrderItemDto(id=%d, quantity=%d)", id, quantity))
			.hasSameHashCodeAs(orderItemDto);
	}
	
	@Test
	void whenInstance_thenCouldSetQuantity() {
		long id = 1L;
		int quantity = 1;
		var orderItemDto = new OrderItemDto(id, quantity);
		assertThat(orderItemDto.getQuantity()).isEqualTo(1);
		orderItemDto.setQuantity(2);
		assertThat(orderItemDto.getQuantity()).isEqualTo(2);
	}
}
