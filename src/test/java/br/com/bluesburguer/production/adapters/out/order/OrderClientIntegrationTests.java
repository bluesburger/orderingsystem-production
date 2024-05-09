package br.com.bluesburguer.production.adapters.out.order;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import br.com.bluesburguer.production.adapters.in.order.dto.OrderDto;
import br.com.bluesburguer.production.adapters.in.order.dto.OrderItemDto;
import br.com.bluesburguer.production.adapters.in.user.dto.UserDto;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.support.ApplicationIntegrationSupport;
import br.com.bluesburguer.production.support.OrderMocks;

@WireMockTest
class OrderClientIntegrationTests extends ApplicationIntegrationSupport {
	
	@Autowired
	private OrderClient orderClient;
	
	@RegisterExtension
	static WireMockExtension wme = WireMockExtension.newInstance()
		.options(wireMockConfig().notifier(new ConsoleNotifier(true)))
		.proxyMode(true)
		.build();

	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void whenGetOrder_thenOrderShouldBeReturned(Step step, Fase fase) throws JsonProcessingException {
		// given
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(1L, step, fase, items, user);
		OrderMocks.mockOrderClientGetById(wme, orderDto);

		// when
		var orderResponse = orderClient.getById(1L);
		
		// then
		assertThat(orderResponse).isEqualTo(orderDto);
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void whenPutOrderStepAndFase_thenUpdatedOrderShouldBeReturned(Step step, Fase fase) throws JsonProcessingException {
		// given
		var items = List.of(new OrderItemDto(1L,  1));
		var user = new UserDto(1L, OrderMocks.mockCpf(), OrderMocks.mockEmail());
		var orderDto = new OrderDto(1L, step, fase, items, user);
		
		OrderMocks.mockOrderClientUpdateStepAndFase(wme, orderDto);
		
		// when
		var orderResponse = orderClient.updateStepAndFase(orderDto.getId(), step, fase);
		
		// then
		assertThat(orderResponse).isEqualTo(orderDto);
	}
}
