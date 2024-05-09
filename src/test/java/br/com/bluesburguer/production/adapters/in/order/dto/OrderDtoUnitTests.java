package br.com.bluesburguer.production.adapters.in.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bluesburguer.production.adapters.in.user.dto.UserDto;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.support.OrderMocks;

@ExtendWith(MockitoExtension.class)
class OrderDtoUnitTests {

	@Test
	void whenInstance_thenShouldHaveValues() {
		var orderId = 1L;
		var step = Step.DELIVERY;
		var fase = Fase.PENDING;
		
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(orderId, step, fase, items, user);
		assertThat(orderDto)
			.hasFieldOrPropertyWithValue("id", 1L)
			.hasFieldOrPropertyWithValue("step", step)
			.hasFieldOrPropertyWithValue("fase", fase)
			.hasFieldOrPropertyWithValue("items", items)
			.hasFieldOrPropertyWithValue("user", user)
			.hasToString(String.format("OrderDto(id=%d, step=%s, fase=%s, items=%s, user=%s)", orderId, step, fase, items, user))
			.hasSameHashCodeAs(orderDto);
	}
	
	@Test
	void whenInstance_thenCouldGetFields() {
		var orderId = 1L;
		var step = Step.DELIVERY;
		var fase = Fase.PENDING;
		
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(orderId, step, fase, items, user);
		assertThat(orderDto.getId()).isEqualTo(orderId);
		assertThat(orderDto.getStep()).isEqualTo(step);
		assertThat(orderDto.getFase()).isEqualTo(fase);
		assertThat(orderDto.getItems()).isEqualTo(items);
		assertThat(orderDto.getUser()).isEqualTo(user);
	}
	
	@Test
	void whenInstance_thenCouldSetId() {
		var orderDto = new OrderDto(1L, Step.DELIVERY, Fase.PENDING, List.of(), null);
		assertThat(orderDto.getId()).isEqualTo(1L);
		orderDto.setId(2L);
		assertThat(orderDto.getId()).isEqualTo(2L);
	}
	
	@Test
	void whenInstance_thenCouldSetStep() {
		var orderDto = new OrderDto(1L, Step.DELIVERY, Fase.PENDING, List.of(), null);
		assertThat(orderDto.getStep()).isEqualTo(Step.DELIVERY);
		orderDto.setStep(Step.KITCHEN);
		assertThat(orderDto.getStep()).isEqualTo(Step.KITCHEN);
	}
	
	@Test
	void whenInstance_thenCouldSetFase() {
		var orderDto = new OrderDto(1L, Step.DELIVERY, Fase.PENDING, List.of(), null);
		assertThat(orderDto.getFase()).isEqualTo(Fase.PENDING);
		orderDto.setFase(Fase.IN_PROGRESS);
		assertThat(orderDto.getFase()).isEqualTo(Fase.IN_PROGRESS);
	}
	
	@Test
	void whenInstance_thenCouldSetItems() {
		var orderDto = new OrderDto(1L, Step.DELIVERY, Fase.PENDING, List.of(), null);
		assertThat(orderDto.getItems()).isEqualTo(List.of());
		var newItem = List.of(new OrderItemDto(1L, 1));
		orderDto.setItems(newItem);
		assertThat(orderDto.getItems()).isEqualTo(newItem);
	}
	
	@Test
	void whenInstance_thenCouldSetUser() {
		var orderDto = new OrderDto(1L, Step.DELIVERY, Fase.PENDING, List.of(), null);
		assertThat(orderDto.getUser()).isNull();
		var newUser = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		orderDto.setUser(newUser);
		assertThat(orderDto.getUser()).isEqualTo(newUser);
	}
}
