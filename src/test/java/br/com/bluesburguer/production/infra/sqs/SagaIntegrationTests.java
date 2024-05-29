package br.com.bluesburguer.production.infra.sqs;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;

import br.com.bluesburguer.production.application.sqs.commands.IssueInvoiceCommand;
import br.com.bluesburguer.production.application.sqs.commands.OrderStockCommand;
import br.com.bluesburguer.production.application.sqs.commands.PerformBillingCommand;
import br.com.bluesburguer.production.application.sqs.commands.ScheduleOrderCommand;
import br.com.bluesburguer.production.application.sqs.events.BillPerformedEvent;
import br.com.bluesburguer.production.application.sqs.events.InvoiceIssueEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderCreatedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderOrderedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderScheduledEvent;
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
import br.com.bluesburguer.production.infra.sqs.commands.IOrderCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.events.IOrderEventPublisher;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestMethodOrder(OrderAnnotation.class)
class SagaIntegrationTests extends SqsBaseIntegrationSupport {
	
	private final AmazonSQS sqs;
	
	private final SqsQueueSupport sqsQueueSupport;
	
	private final OrderClient orderClient;
	private final EventDatabaseAdapter eventAdapter;
	
	private final IOrderEventPublisher<OrderCreatedEvent> orderCreatedEventPublisher;
	private final IOrderCommandPublisher<OrderStockCommand> orderStockCommandPublisher;
	private final IOrderEventPublisher<OrderOrderedEvent> orderOrderedEventPublisher;
	private final IOrderCommandPublisher<PerformBillingCommand> performBillingCommandPublisher;
	private final IOrderEventPublisher<BillPerformedEvent> billPerformedEventPublisher;
	private final IOrderCommandPublisher<IssueInvoiceCommand> issueInvoiceCommandPublisher;
	private final IOrderEventPublisher<InvoiceIssueEvent> invoiceIssueEventPublisher;
	private final IOrderCommandPublisher<ScheduleOrderCommand> scheduleOrderCommandPublisher;
	private final IOrderEventPublisher<OrderScheduledEvent> orderScheduledEventPublisher;
	
	private final CountDownLatch lock = new CountDownLatch(1);
	
	private static String ORDER_ID;
	
	@Value("${queue.order-stock-command}")
	private String queueOrderStockCommand;
	
	@Value("${queue.perform-billing-command}")
	private String queuePerformBillingCommand;
	
	@Value("${queue.invoice-command}")
	private String queueIssueInvoiceCommand;
	
	@Value("${queue.schedule-order-command}")
	private String queueScheduleOrderCommand;
	
	@Test
	@Order(1)
	@DisplayName("Dado um novo pedido criado, quando OrderService publicar OrderCreatedEvent, então Production deve atualizar step para ORDER e fase para CREATED")
	void givenOrderCreatedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepOrderAndFaseRegistered() throws InterruptedException {
		ORDER_ID = createNewOrder();

		var event = new OrderCreatedEvent(ORDER_ID);
		assertThat(orderCreatedEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.ORDER, Fase.CREATED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME));
	}
	
	@Test
	@Order(2)
	@DisplayName("Dado um pedido criado, quando ProductionService publicar OrderStockCommand, então StockService deve executar reserva de estoque")
	void givenCreatedOrder_WhenPublishOrderStockCommand_ThenShouldWaitForStockService() {

		var command = new OrderStockCommand(ORDER_ID);
		assertThat(orderStockCommandPublisher.publish(command))
			.isPresent();
		
		// validation
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
        	var queue = sqsQueueSupport.buildQueueUrl(queueOrderStockCommand);
            assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
            assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
        });
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(1)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME));
		// TODO: persistir evento quando publicar command?
	}
	
	@Test
	@Order(3)
	@DisplayName("Dado um pedido aguardando encomenda, quando StockService publicar evento de pedido encomendado, então ProductionService deve atualizar step para DELIVERY e fase para CREATED")
	void givenOrderOrderedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDeliveryAndFaseCreated() throws InterruptedException {
		var event = new OrderOrderedEvent(ORDER_ID);
		assertThat(orderOrderedEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.DELIVERY, Fase.CREATED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(2)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME));
	}
	
	@Test
	@Order(4)
	@DisplayName("Dado um pedido encomendado, quando ProductionService publicar PerformBillingCommand, então PaymentService deve tratar o pagamento da cobrança")
	void givenOrderedOrder_WhenPublishPerformBillingCommand_ThenShouldWaitForPaymentService() {

		var command = new PerformBillingCommand(ORDER_ID);
		assertThat(performBillingCommandPublisher.publish(command))
			.isPresent();
		
		// validation
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
        	var queue = sqsQueueSupport.buildQueueUrl(queuePerformBillingCommand);
            assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
            assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
        });
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(2)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME));
		// TODO: persistir evento quando publicar command?
	}
	
	@Test
	@Order(5)
	@DisplayName("Dado um pedido com cobrança pendente, quando PaymentService publicar evento de cobança realizada, então ProductionService deve atualizar step para CHARGE e fase para CONFIRMED")
	void givenBillPending_WhenPublishBillPerformedEvent_ThenOrderShouldBeUpdatedToStepChargeAndFaseConfirmed() throws InterruptedException {
		var event = new BillPerformedEvent(ORDER_ID);
		assertThat(billPerformedEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(3)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME));
	}
	
	@Test
	@Order(6)
	@DisplayName("Dado um pedido pago, quando ProductionService publicar IssueInvoiceCommand, então NotaFiscalService deve tratar o pagamento da cobrança")
	void givenPaidOrder_WhenPublishIssueInvoiceCommand_ThenShouldWaitForNotaFiscalService() {

		var command = new IssueInvoiceCommand(ORDER_ID);
		assertThat(issueInvoiceCommandPublisher.publish(command))
			.isPresent();
		
		// validation
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
        	var queue = sqsQueueSupport.buildQueueUrl(queueIssueInvoiceCommand);
            assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
            assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
        });
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(3)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME));
		// TODO: persistir evento quando publicar command?
	}
	
	@Test
	@Order(7)
	@DisplayName("Dado um pedido com cobrança realizada, quando NotaFiscalService publicar evento InvoiceIssueEvent, então ProductionService deve atualizar step para INVOICE e fase para CONFIRMED")
	void givenBillPerformed_WhenPublishInvoiceIssueEvent_ThenOrderShouldBeUpdatedToStepInvoiceAndFaseConfirmed() throws InterruptedException {
		var event = new InvoiceIssueEvent(ORDER_ID);
		assertThat(invoiceIssueEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(4)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(InvoiceIssueEvent.EVENT_NAME));
	}
	
	@Test
	@Order(8)
	@DisplayName("Dado um pedido com nota fiscal emitida, quando ProductionService publicar ScheduleOrderCommand, então StockService deve agendar a entrega do pedido")
	void givenIssuedInvoiceOrder_WhenPublishScheduleOrderCommand_ThenShouldWaitForStockService() {

		var command = new ScheduleOrderCommand(ORDER_ID);
		assertThat(scheduleOrderCommandPublisher.publish(command))
			.isPresent();
		
		// validation
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
        	var queue = sqsQueueSupport.buildQueueUrl(queueScheduleOrderCommand);
            assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
            assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
        });
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(4)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(InvoiceIssueEvent.EVENT_NAME));
		// TODO: persistir evento quando publicar command?
	}
	
	@Test
	@Order(9)
	@DisplayName("Dado um pedido aguardando agendamento de entrega, quando StockService publicar evento de entrega agendada, então ProductionService deve atualizar step para DELIVERY e fase para CREATED")
	void givenOrderScheduledEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDeliveryAndFaseCreated() throws InterruptedException {
		var event = new OrderScheduledEvent(ORDER_ID);
		assertThat(orderScheduledEventPublisher.publish(event))
			.isPresent();
		
		verifyStatusWereUpdated(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED);
		
		assertThat(eventAdapter.findByOrderId(ORDER_ID))
			.isNotEmpty()
			.hasSize(5)
			.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(InvoiceIssueEvent.EVENT_NAME))
			.haveExactly(1, hasEventByName(OrderScheduledEvent.EVENT_NAME));
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
	
	private Integer numberOfMessagesInQueue(String queueName) {
        GetQueueAttributesResult attributes = sqs
                .getQueueAttributes(queueName, of("All"));

        return Integer.parseInt(
                attributes.getAttributes().get("ApproximateNumberOfMessages")
        );
    }
	
	private Integer numberOfMessagesNotVisibleInQueue(String queueName) {
        GetQueueAttributesResult attributes = sqs
                .getQueueAttributes(queueName, of("All"));

        return Integer.parseInt(
            attributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible")
        );
    }
}
