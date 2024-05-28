package br.com.bluesburguer.production.infra.sqs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.bluesburguer.production.application.dto.cobranca.CobrancaRealizadaDto;
import br.com.bluesburguer.production.application.dto.entrega.EntregaAgendadaDto;
import br.com.bluesburguer.production.application.dto.notafiscal.NotaFiscalEmitidaDto;
import br.com.bluesburguer.production.application.dto.pedido.PedidoConfirmadoDto;
import br.com.bluesburguer.production.application.dto.pedido.PedidoRegistradoDto;
import br.com.bluesburguer.production.domain.entity.Cpf;
import br.com.bluesburguer.production.domain.entity.Email;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapters.order.OrderClient;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderItemRequest;
import br.com.bluesburguer.production.infra.adapters.order.dto.OrderRequest;
import br.com.bluesburguer.production.infra.adapters.order.dto.UserRequest;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.database.entity.EventEntity;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestMethodOrder(OrderAnnotation.class)
class SagaIntegrationTests extends SqsBaseIntegrationSupport {
	
	private final OrderClient orderClient;
	private final EventDatabaseAdapter eventAdapter;
	
	private final OrderEventPublisher<PedidoRegistradoDto> pedidoRegistradoEventPublisher;
	private final OrderEventPublisher<CobrancaRealizadaDto> cobrancaRealizadaEventPublisher;
	private final OrderEventPublisher<EntregaAgendadaDto> entregaAgendadaEventPublisher;
	private final OrderEventPublisher<NotaFiscalEmitidaDto> notaFiscalEmitidaEventPublisher;
	private final OrderEventPublisher<PedidoConfirmadoDto> pedidoConfirmadoEventPublisher;
	
	private final CountDownLatch lock = new CountDownLatch(1);
	
	private static String ORDER_ID;
	
	@Test
	@Order(1)
	@DisplayName("Dado um novo pedido criado, quando publicar evento de pedido registrado, então deve atualizar step para ORDER e fase para REGISTERED")
	void givenNovoPedido_WhenPublishEventPedidoRegistrado_ThenOrderShouldBeUpdatedToStepOrderAndFaseRegistered() throws InterruptedException {
		ORDER_ID = createNewOrder();

		var event = new PedidoRegistradoDto(ORDER_ID);
		assertThat(pedidoRegistradoEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.ORDER, Fase.REGISTERED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(PedidoRegistradoDto.EVENT_NAME));
	}
	
	// FIXME: givenPedidoCriado_WhenPublishEventReservaRealizada_ThenFaseShouldBeUpdatedToRegistered
	
	@Test
	@Order(2)
	@DisplayName("Dado um pedido registrado, quando publicar evento de cobrança realizada, então deve atualizar step para CHARGE e fase para CONFIRMED")
	void givenReservaRealizada_WhenPublishEventCobrancaRealizada_ThenOrderShouldBeUpdatedToStepChargeAndFaseConfirmed() throws InterruptedException {
		var event = new CobrancaRealizadaDto(ORDER_ID);
		assertThat(cobrancaRealizadaEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(2)
			.haveExactly(1, hasEventByName(PedidoRegistradoDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(CobrancaRealizadaDto.EVENT_NAME));
	}
	
	@Test
	@Order(3)
	@DisplayName("Dado um pedido com cobrança realizada, quando publicar evento de entrega agendada, então deve atualizar step para DELIVERY e fase para REGISTERED")
	void givenCobrancaRealizada_WhenPublishEventEntregaAgendada_ThenOrderShouldBeUpdatedToStepDeliveryAndFaseRegistered() throws InterruptedException {
		var event = new EntregaAgendadaDto(ORDER_ID);
		assertThat(entregaAgendadaEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.DELIVERY, Fase.REGISTERED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(3)
			.haveExactly(1, hasEventByName(PedidoRegistradoDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(CobrancaRealizadaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(EntregaAgendadaDto.EVENT_NAME));
	}
	
	@Test
	@Order(4)
	@DisplayName("Dado um pedido com entrega agendada, quando publicar evento de nota fiscal emitida, então deve atualizar step para INVOICE e fase para CONFIRMED")
	void givenEntregaAgendada_WhenPublishEventNotaFiscalEmitidaDto_ThenOrderShouldBeUpdatedToStepInvoiceAndFaseConfirmed() throws InterruptedException {
		var event = new NotaFiscalEmitidaDto(ORDER_ID);
		assertThat(notaFiscalEmitidaEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(4)
			.haveExactly(1, hasEventByName(PedidoRegistradoDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(CobrancaRealizadaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(EntregaAgendadaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(NotaFiscalEmitidaDto.EVENT_NAME));
	}
	
	@Test
	@Order(5)
	@DisplayName("Dado um pedido com nota fiscal emitida, quando publicar evento de pedido confirmado, então deve atualizar step para ORDER e fase para CONFIRMED")
	void givenNotaFiscalEmitida_WhenPublishEventPedidoConfirmado_ThenFaseShouldBeUpdatedToRegistered() throws InterruptedException {
		var event = new PedidoConfirmadoDto(ORDER_ID);
		assertThat(pedidoConfirmadoEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.ORDER, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(5)
			.haveExactly(1, hasEventByName(PedidoRegistradoDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(CobrancaRealizadaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(EntregaAgendadaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(NotaFiscalEmitidaDto.EVENT_NAME))
			.haveExactly(1, hasEventByName(PedidoConfirmadoDto.EVENT_NAME));
	}
	
	private Condition<EventEntity> hasEventByName(String eventName) {
		return new Condition<>(e -> e.getEventName().equals(eventName), String.format("Evento %s não encontrado", eventName));
	}
	
	private String createNewOrder() {
		var orderRequest = new OrderRequest(
				List.of(new OrderItemRequest(1L, 1)), 
				new UserRequest(1L, new Cpf(OrderMocks.mockCpf()), new Email(OrderMocks.mockEmail())));
		var response = orderClient.createNewOrder(orderRequest);
		return response.headers().get("Location").stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Id do pedido não encontrado na resposta"));
	}
	
	private void verifyStatusWereUpdated(String orderId, Step step, Fase fase) throws InterruptedException {
		lock.await(2L, TimeUnit.SECONDS);
		assertThat(orderClient.getById(orderId)).isNotNull()
			.hasFieldOrPropertyWithValue("step", step)
			.hasFieldOrPropertyWithValue("fase", fase);
	}
}
