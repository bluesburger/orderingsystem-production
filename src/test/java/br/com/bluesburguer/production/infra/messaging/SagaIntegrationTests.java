package br.com.bluesburguer.production.infra.messaging;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import br.com.bluesburguer.production.application.dto.OrderItemRequest;
import br.com.bluesburguer.production.application.dto.OrderRequest;
import br.com.bluesburguer.production.application.dto.UserRequest;
import br.com.bluesburguer.production.domain.entity.Cpf;
import br.com.bluesburguer.production.domain.entity.Email;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapter.OrderClient;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.database.entity.EventEntity;
import br.com.bluesburguer.production.infra.messaging.event.BillPerformedEvent;
import br.com.bluesburguer.production.infra.messaging.event.InvoiceIssueEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderCreatedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderOrderedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderScheduledEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.event.IOrderEventPublisher;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;

@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@TestMethodOrder(OrderAnnotation.class)
@WireMockTest
class SagaIntegrationTests extends SqsBaseIntegrationSupport {

	private final AmazonSQS sqs;

	private final SqsQueueSupport<OrderCommand> sqsQueueSupport;

	private final OrderClient orderClient;
	private final EventDatabaseAdapter eventAdapter;

	private final IOrderEventPublisher<OrderCreatedEvent> orderCreatedEventPublisher;
	private final IOrderEventPublisher<OrderOrderedEvent> orderOrderedEventPublisher;
	private final IOrderEventPublisher<BillPerformedEvent> billPerformedEventPublisher;
	private final IOrderEventPublisher<InvoiceIssueEvent> invoiceIssueEventPublisher;
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

	@Value("${queue.order-confirmed-command}")
	private String queueOrderConfirmedCommand;

	@RegisterExtension
	static WireMockExtension wme = WireMockExtension.newInstance() //
			.options(wireMockConfig() // configuração padrão
					.port(8000) // porta definida nas configurações de testes
//					.notifier(new ConsoleNotifier(true)) // saída no console
			).proxyMode(true).build();

	@Test
	@Order(1)
	@DisplayName("Dado um novo pedido criado, quando UpdateOrder publicar OrderCreatedEvent, então Production deve atualizar step para ORDER e fase para CREATED")
	void givenOrderCreatedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepOrderAndFaseRegistered()
			throws InterruptedException, JsonProcessingException {
		// given
		ORDER_ID = createNewOrder();

		var step = Step.ORDER;
		var fase = Fase.CREATED;
		mockOrderClient(wme, ORDER_ID, step, fase);

		// when
		var event = new OrderCreatedEvent(ORDER_ID);
		assertThat(orderCreatedEventPublisher.publish(event)).isPresent();

		// then
		verifyStatusWereUpdated(ORDER_ID, step, fase);

		assertThat(eventAdapter.findByOrderId(ORDER_ID)) //
				.isNotEmpty() // não é vazio
				.hasSize(1) // possui apenas 1 evento persistido
				.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME)); // evento de pedido criado

		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
			var queue = sqsQueueSupport.buildQueueUrl(queueOrderStockCommand);
			assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
			assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
		});
	}

	@Test
	@Order(2)
	@DisplayName("Dado um pedido aguardando encomenda, quando StockService publicar evento de pedido encomendado, então ProductionService deve atualizar step para DELIVERY e fase para CREATED")
	void givenOrderOrderedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDeliveryAndFaseCreated()
			throws InterruptedException, JsonProcessingException {
		// given
		var step = Step.DELIVERY;
		var fase = Fase.CREATED;
		mockOrderClient(wme, ORDER_ID, step, fase);

		// when
		var event = new OrderOrderedEvent(ORDER_ID);
		assertThat(orderOrderedEventPublisher.publish(event)).isPresent();

		// then
		verifyStatusWereUpdated(ORDER_ID, step, fase);

		assertThat(eventAdapter.findByOrderId(ORDER_ID)).isNotEmpty() //
				.hasSize(2) // qtd de eventos necessários
				.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME));

		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
			var queue = sqsQueueSupport.buildQueueUrl(queuePerformBillingCommand);
			assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
			assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
		});
	}

	@Test
	@Order(3)
	@DisplayName("Dado um pedido com cobrança pendente, quando PaymentService publicar evento de cobança realizada, então ProductionService deve atualizar step para CHARGE e fase para CONFIRMED")
	void givenBillPending_WhenPublishBillPerformedEvent_ThenOrderShouldBeUpdatedToStepChargeAndFaseConfirmed()
			throws InterruptedException, JsonProcessingException {
		// given
		var step = Step.CHARGE;
		var fase = Fase.CONFIRMED;
		mockOrderClient(wme, ORDER_ID, step, fase);

		// when
		var event = new BillPerformedEvent(ORDER_ID);
		assertThat(billPerformedEventPublisher.publish(event)).isPresent();

		// then
		verifyStatusWereUpdated(ORDER_ID, step, fase);

		assertThat(eventAdapter.findByOrderId(ORDER_ID)).isNotEmpty() //
				.hasSize(3) // qtd de eventos necessários
				.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME));

		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
			var queue = sqsQueueSupport.buildQueueUrl(queueIssueInvoiceCommand);
			assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
			assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
		});
	}

	@Test
	@Order(4)
	@DisplayName("Dado um pedido com cobrança realizada, quando NotaFiscalService publicar evento InvoiceIssueEvent, então ProductionService deve atualizar step para INVOICE e fase para CONFIRMED")
	void givenBillPerformed_WhenPublishInvoiceIssueEvent_ThenOrderShouldBeUpdatedToStepInvoiceAndFaseConfirmed()
			throws InterruptedException, JsonProcessingException {
		// given
		var step = Step.INVOICE;
		var fase = Fase.CONFIRMED;
		mockOrderClient(wme, ORDER_ID, step, fase);

		// when
		var event = new InvoiceIssueEvent(ORDER_ID);
		assertThat(invoiceIssueEventPublisher.publish(event)).isPresent();

		// then
		verifyStatusWereUpdated(ORDER_ID, step, fase);

		assertThat(eventAdapter.findByOrderId(ORDER_ID)).isNotEmpty() //
				.hasSize(4) // qtd de eventos necessários
				.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(InvoiceIssueEvent.EVENT_NAME));

		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
			var queue = sqsQueueSupport.buildQueueUrl(queueScheduleOrderCommand);
			assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
			assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
		});
	}

	@Test
	@Order(5)
	@DisplayName("Dado um pedido aguardando agendamento de entrega, quando StockService publicar evento de entrega agendada, então ProductionService deve atualizar step para DELIVERY e fase para CREATED")
	void givenOrderScheduledEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDeliveryAndFaseCreated()
			throws InterruptedException, JsonProcessingException {
		// given
		var step = Step.DELIVERY;
		var fase = Fase.CONFIRMED;
		mockOrderClient(wme, ORDER_ID, step, fase);

		// when
		var event = new OrderScheduledEvent(ORDER_ID);
		assertThat(orderScheduledEventPublisher.publish(event)).isPresent();

		// then
		verifyStatusWereUpdated(ORDER_ID, step, fase);

		assertThat(eventAdapter.findByOrderId(ORDER_ID)).isNotEmpty() //
				.hasSize(5) // qtd de eventos necessários
				.haveExactly(1, hasEventByName(OrderCreatedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(OrderOrderedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(BillPerformedEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(InvoiceIssueEvent.EVENT_NAME))
				.haveExactly(1, hasEventByName(OrderScheduledEvent.EVENT_NAME));

		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
			var queue = sqsQueueSupport.buildQueueUrl(queueOrderConfirmedCommand);
			assertThat(numberOfMessagesInQueue(queue)).isEqualTo(1);
			assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
		});
	}

	private Condition<EventEntity> hasEventByName(String eventName) {
		return new Condition<>(e -> e.getEventName().equals(eventName),
				String.format("Evento %s não encontrado", eventName));
	}

	private String createNewOrder() throws JsonProcessingException {
		var orderRequest = new OrderRequest(List.of(new OrderItemRequest(1L, 1)),
				new UserRequest(1L, new Cpf(OrderMocks.mockCpf()), new Email(OrderMocks.mockEmail())));

		mockOrderClientCreateNewOrder(wme, "asd-asd-asd-asd-asd", orderRequest);

		var response = orderClient.createNewOrder(orderRequest);
		return response.headers().get("Location").stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Id do pedido não encontrado na resposta"));
	}

	private void verifyStatusWereUpdated(String orderId, Step step, Fase fase) throws InterruptedException {
		lock.await(2L, TimeUnit.SECONDS);
		assertThat(orderClient.getById(orderId)).isNotNull() //
				.hasFieldOrPropertyWithValue("step", step) //
				.hasFieldOrPropertyWithValue("fase", fase); //
	}

	private Integer numberOfMessagesInQueue(String queueName) {
		GetQueueAttributesResult attributes = sqs.getQueueAttributes(queueName, of("All"));

		return Integer.parseInt(attributes.getAttributes().get("ApproximateNumberOfMessages"));
	}

	private Integer numberOfMessagesNotVisibleInQueue(String queueName) {
		GetQueueAttributesResult attributes = sqs.getQueueAttributes(queueName, of("All"));

		return Integer.parseInt(attributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible"));
	}
}
