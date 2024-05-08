package br.com.bluesburguer.production.adapters.in.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderDtoUnitTests {

	@Test
	void shouldInstance() {
		assertThat(new OrderDto())
			.hasFieldOrProperty("id")
			.hasFieldOrProperty("step")
			.hasFieldOrProperty("fase")
			.hasFieldOrPropertyWithValue("items", new ArrayList<>())
			.hasFieldOrProperty("user");
	}
}
