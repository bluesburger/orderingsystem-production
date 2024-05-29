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

import br.com.bluesburguer.production.application.sqs.events.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderStockFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.PerformBillingFailedEvent;
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
import br.com.bluesburguer.production.infra.sqs.events.IOrderEventPublisher;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;

@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestMethodOrder(OrderAnnotation.class)
class SagaFailureCompensationIntegrationTests extends SqsBaseIntegrationSupport {
	
	private final OrderClient orderClient;
	private final EventDatabaseAdapter eventAdapter;
	
	private final IOrderEventPublisher<OrderStockFailedEvent> orderStockFailedPublisher;
	private final IOrderEventPublisher<PerformBillingFailedEvent> performBillingFailedPublisher;
	private final IOrderEventPublisher<IssueInvoiceFailedEvent> issueInvoiceFailedPublisher;
	
	private final CountDownLatch lock = new CountDownLatch(1);
	
	private static String ORDER_ID;
		
	@Test
	@Order(1)
	@DisplayName("Dado um pedido que falhou na reserva, quando OrderService publicar OrderStockFailedEvent, então Production deve atualizar step para DELIVERY e fase para FAILED")
	void givenOrderStockFailedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDELIVERYAndFaseFailed() throws InterruptedException {
		ORDER_ID = createNewOrder();

		var event = new OrderStockFailedEvent(ORDER_ID);
		assertThat(orderStockFailedPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.DELIVERY, Fase.FAILED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(OrderStockFailedEvent.EVENT_NAME));
	}
	
	@Test
	@Order(2)
	@DisplayName("Dado um pedido que falhou na reserva, quando OrderService publicar OrderStockFailedEvent, então Production deve atualizar step para CHARGE e fase para FAILED")
	void givenPerformBillingFailedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepChargeAndFaseFailed() throws InterruptedException {
		ORDER_ID = createNewOrder();

		var event = new PerformBillingFailedEvent(ORDER_ID);
		assertThat(performBillingFailedPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.CHARGE, Fase.FAILED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(PerformBillingFailedEvent.EVENT_NAME));
	}
	
	@Test
	@Order(3)
	@DisplayName("Dado um pedido que falhou na reserva, quando OrderService publicar OrderStockFailedEvent, então Production deve atualizar step para INVOICE e fase para FAILED")
	void givenIssueInvoiceFailedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepInvoiceAndFaseFailed() throws InterruptedException {
		ORDER_ID = createNewOrder();

		var event = new IssueInvoiceFailedEvent(ORDER_ID);
		assertThat(issueInvoiceFailedPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.INVOICE, Fase.FAILED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(IssueInvoiceFailedEvent.EVENT_NAME));
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
