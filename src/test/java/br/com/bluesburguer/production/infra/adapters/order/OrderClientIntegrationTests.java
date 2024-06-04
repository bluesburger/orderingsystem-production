package br.com.bluesburguer.production.infra.adapters.order;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderDto;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderItemDto;
import br.com.bluesburguer.production.infra.adapters.user.dto.UserDto;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;

@WireMockTest
class OrderClientIntegrationTests extends SqsBaseIntegrationSupport {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	@Autowired
	private OrderClient orderClient;
	
	@RegisterExtension
	static WireMockExtension wme = WireMockExtension.newInstance()
		.options(wireMockConfig()
				.port(8000)
//				.notifier(new ConsoleNotifier(true))
		)
		.proxyMode(true)
		.build();

	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void whenGetOrder_thenOrderShouldBeReturned(Step step, Fase fase) throws JsonProcessingException {
		// given
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(ORDER_ID, step, fase, items, user);
		
		mockOrderClientGetById(wme, orderDto);

		// when
		var orderResponse = orderClient.getById(orderDto.getId());
		
		// then
		assertThat(orderResponse).isEqualTo(orderDto);
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void whenPutOrderStepAndFase_thenUpdatedOrderShouldBeReturned(Step step, Fase fase) throws JsonProcessingException {
		// given
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(ORDER_ID, step, fase, items, user);
		
		mockOrderClientUpdateStepAndFase(wme, orderDto);
		
		// when
		var orderResponse = orderClient.updateStepAndFase(orderDto.getId(), step, fase);
		
		// then
		assertThat(orderResponse).isEqualTo(orderDto);
	}
}
