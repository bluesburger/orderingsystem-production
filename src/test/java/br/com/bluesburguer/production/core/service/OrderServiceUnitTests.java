package br.com.bluesburguer.production.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.service.OrderService;
import br.com.bluesburguer.production.infra.adapters.order.OrderClient;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";

	@Mock
	OrderClient orderClient;
	
	@Mock
	OrderDto orderDto;
	
	@InjectMocks
	OrderService orderService;
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnNull_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderClient.updateStepAndFase(ORDER_ID, step, fase)).thenReturn(null);
		
		// when
		assertThat(orderService.update(ORDER_ID, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnNullId_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(null);
		when(orderClient.updateStepAndFase(ORDER_ID, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(ORDER_ID, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnValidId_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(ORDER_ID);
		when(orderClient.updateStepAndFase(ORDER_ID, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(ORDER_ID, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnValidIdAndValidStep_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(ORDER_ID);
		when(orderDto.getStep()).thenReturn(step);
		when(orderClient.updateStepAndFase(ORDER_ID, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(ORDER_ID, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldUpdateOrderStepAndFase_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(ORDER_ID);
		when(orderDto.getStep()).thenReturn(step);
		when(orderDto.getFase()).thenReturn(fase);
		when(orderClient.updateStepAndFase(ORDER_ID, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(ORDER_ID, step, fase))
			.isTrue();
	}
}
