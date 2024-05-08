package br.com.bluesburguer.production.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.bluesburguer.production.adapters.in.order.dto.OrderDto;
import br.com.bluesburguer.production.adapters.out.order.OrderClient;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTests {

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
		when(orderClient.updateStepAndFase(1L, step, fase)).thenReturn(null);
		
		// when
		assertThat(orderService.update(1L, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnNullId_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(null);
		when(orderClient.updateStepAndFase(1L, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(1L, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnValidId_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(1L);
		when(orderClient.updateStepAndFase(1L, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(1L, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldReturnNull_WhenServerReturnValidIdAndValidStep_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(1L);
		when(orderDto.getStep()).thenReturn(step);
		when(orderClient.updateStepAndFase(1L, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(1L, step, fase))
			.isFalse();
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldUpdateOrderStepAndFase_WithAllPossibleParameters(Step step, Fase fase) {
		
		// given
		when(orderDto.getId()).thenReturn(1L);
		when(orderDto.getStep()).thenReturn(step);
		when(orderDto.getFase()).thenReturn(fase);
		when(orderClient.updateStepAndFase(1L, step, fase)).thenReturn(orderDto);
		
		// when
		assertThat(orderService.update(1L, step, fase))
			.isTrue();
	}
}
