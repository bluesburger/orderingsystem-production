package br.com.bluesburguer.production.adapters.in.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderItemDtoUnitTests {

	@Test
	void shouldInstance() {
		assertThat(new OrderItemDto(1L, 1))
			.hasFieldOrPropertyWithValue("id", 1L)
			.hasFieldOrPropertyWithValue("quantity", 1);
			
	}
}
