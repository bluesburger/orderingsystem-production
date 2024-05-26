package br.com.bluesburguer.production.infra.adapters.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.user.dto.UserDto;
import br.com.bluesburguer.production.support.OrderMocks;

@ExtendWith(MockitoExtension.class)
class OrderDtoUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	private static final String ORDER_ID_2 = "006ff58b-1bcc-43a1-a45c-5730b635ebc9";

	@Test
	void whenInstance_thenShouldHaveValues() {
		var step = Step.ORDER;
		var fase = Fase.REGISTERED;
		
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(ORDER_ID, step, fase, items, user);
		assertThat(orderDto)
			.hasFieldOrPropertyWithValue("id", ORDER_ID)
			.hasFieldOrPropertyWithValue("step", step)
			.hasFieldOrPropertyWithValue("fase", fase)
			.hasFieldOrPropertyWithValue("items", items)
			.hasFieldOrPropertyWithValue("user", user)
			.hasToString(String.format("OrderDto(id=%s, step=%s, fase=%s, items=%s, user=%s)", ORDER_ID, step, fase, items, user))
			.hasSameHashCodeAs(orderDto);
	}
	
	@Test
	void whenInstance_thenCouldGetFields() {
		var step = Step.ORDER;
		var fase = Fase.REGISTERED;
		
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(ORDER_ID, step, fase, items, user);
		assertThat(orderDto.getId()).isEqualTo(ORDER_ID);
		assertThat(orderDto.getStep()).isEqualTo(step);
		assertThat(orderDto.getFase()).isEqualTo(fase);
		assertThat(orderDto.getItems()).isEqualTo(items);
		assertThat(orderDto.getUser()).isEqualTo(user);
	}
	
	@Test
	void whenInstance_thenCouldSetId() {
		var orderDto = new OrderDto(ORDER_ID, Step.DELIVERY, Fase.REGISTERED, List.of(), null);
		assertThat(orderDto.getId()).isEqualTo(ORDER_ID);
		orderDto.setId(ORDER_ID_2);
		assertThat(orderDto.getId()).isEqualTo(ORDER_ID_2);
	}
	
	@Test
	void whenInstance_thenCouldSetStep() {
		var orderDto = new OrderDto(ORDER_ID, Step.DELIVERY, Fase.REGISTERED, List.of(), null);
		assertThat(orderDto.getStep()).isEqualTo(Step.DELIVERY);
		orderDto.setStep(Step.KITCHEN);
		assertThat(orderDto.getStep()).isEqualTo(Step.KITCHEN);
	}
	
	@Test
	void whenInstance_thenCouldSetFase() {
		var orderDto = new OrderDto(ORDER_ID, Step.DELIVERY, Fase.REGISTERED, List.of(), null);
		assertThat(orderDto.getFase()).isEqualTo(Fase.REGISTERED);
		orderDto.setFase(Fase.CONFIRMED);
		assertThat(orderDto.getFase()).isEqualTo(Fase.CONFIRMED);
	}
	
	@Test
	void whenInstance_thenCouldSetItems() {
		var orderDto = new OrderDto(ORDER_ID, Step.DELIVERY, Fase.REGISTERED, List.of(), null);
		assertThat(orderDto.getItems()).isEqualTo(List.of());
		var newItem = List.of(new OrderItemDto(1L, 1));
		orderDto.setItems(newItem);
		assertThat(orderDto.getItems()).isEqualTo(newItem);
	}
	
	@Test
	void whenInstance_thenCouldSetUser() {
		var orderDto = new OrderDto(ORDER_ID, Step.DELIVERY, Fase.REGISTERED, List.of(), null);
		assertThat(orderDto.getUser()).isNull();
		var newUser = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		orderDto.setUser(newUser);
		assertThat(orderDto.getUser()).isEqualTo(newUser);
	}
}
