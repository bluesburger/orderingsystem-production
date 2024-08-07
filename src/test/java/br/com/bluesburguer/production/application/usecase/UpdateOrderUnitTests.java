package br.com.bluesburguer.production.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bluesburguer.production.application.dto.OrderDto;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapter.OrderClient;

@ExtendWith(MockitoExtension.class)
class UpdateOrderUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";

	@Mock
	OrderClient orderClient;
	
	@Mock
	OrderDto orderDto;
	
	@InjectMocks
	UpdateOrder orderService;
	
	@Test
	void shouldReturnOrderDto_WhenGetById() {
		when(orderClient.getById(ORDER_ID)).thenReturn(orderDto);
		
		assertThat(orderService.getById(ORDER_ID))
			.isEqualTo(orderDto);
	}
	
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
