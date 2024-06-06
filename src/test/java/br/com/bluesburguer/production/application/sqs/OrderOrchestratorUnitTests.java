package br.com.bluesburguer.production.application.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.commands.IssueInvoiceCommand;
import br.com.bluesburguer.production.application.sqs.commands.OrderConfirmedCommand;
import br.com.bluesburguer.production.application.sqs.commands.OrderStockCommand;
import br.com.bluesburguer.production.application.sqs.commands.PerformBillingCommand;
import br.com.bluesburguer.production.application.sqs.commands.ScheduleOrderCommand;
import br.com.bluesburguer.production.application.sqs.events.BillPerformedEvent;
import br.com.bluesburguer.production.application.sqs.events.InvoiceIssueEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderCreatedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderOrderedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderScheduledEvent;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.sqs.SqsQueueSupport;
import br.com.bluesburguer.production.infra.sqs.commands.IssueInvoiceCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.commands.OrderConfirmedCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.commands.OrderStockCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.commands.PerformBillingCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.commands.ScheduleOrderCommandPublisher;

@ExtendWith(MockitoExtension.class)
class OrderOrchestratorUnitTests {

	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	private static final String PUBLISHED_ID = "asd-asd-asd-asd-asd";

	@Mock
	OrderPort orderPort;

	@Spy
	ObjectMapper mapper = new ObjectMapper();

	@Mock
	Acknowledgment ack;

	@Mock
	EventDatabaseAdapter eventDatabaseAdapter;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	SqsQueueSupport<OrderStockCommand> sqsQueueSupportOrderStockCommand;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	SqsQueueSupport<PerformBillingCommand> sqsQueueSupportPerformBillingCommand;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	SqsQueueSupport<IssueInvoiceCommand> sqsQueueSupportIssueInvoiceCommand;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	SqsQueueSupport<ScheduleOrderCommand> sqsQueueSupportScheduleOrderCommand;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	SqsQueueSupport<OrderConfirmedCommand> sqsQueueSupportOrderConfirmedCommand;

	@Mock
	AmazonSQS amazonSqs;

	@Mock
	OrderStockCommandPublisher orderStockCommandPublisher;

	@Mock
	PerformBillingCommandPublisher performBillingCommandPublisher;

	@Mock
	IssueInvoiceCommandPublisher issueInvoiceCommandPublisher;

	@Mock
	ScheduleOrderCommandPublisher scheduleOrderCommandPublisher;

	@Mock
	OrderConfirmedCommandPublisher orderConfirmedCommandPublisher;

	OrderOrchestrator consumer;

	@BeforeEach
	void initService() {
		consumer = new OrderOrchestrator(orderPort, eventDatabaseAdapter, orderStockCommandPublisher,
				performBillingCommandPublisher, issueInvoiceCommandPublisher, scheduleOrderCommandPublisher,
				orderConfirmedCommandPublisher);
	}

	@Nested
	class Order {
		@Test
		void shouldHandleOrderCreated_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CREATED)).thenReturn(true);
			when(orderStockCommandPublisher.publish(any(OrderStockCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
			var order = OrderCreatedEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CREATED);
			verify(orderStockCommandPublisher).publish(any(OrderStockCommand.class));
			verify(ack).acknowledge();
		}

		@Test
		void shouldHandleOrderCreated_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CREATED)).thenReturn(false);
			var order = OrderCreatedEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CREATED);
			verify(orderStockCommandPublisher, never()).publish(any(OrderStockCommand.class));
			verify(ack, never()).acknowledge();
		}

		@Test
		void shouldHandleOrderCreated_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() {
			// given
			OrderCreatedEvent order = null;

			// when
			consumer.handle(order, ack);

			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(orderStockCommandPublisher, never()).publish(any(OrderStockCommand.class));
			verify(ack, never()).acknowledge();
		}
	}

	@Nested
	class Stock {
		@Test
		void shouldHandleOrderOrdered_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CREATED)).thenReturn(true);
			when(performBillingCommandPublisher.publish(any(PerformBillingCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
			var order = OrderOrderedEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CREATED);
			verify(performBillingCommandPublisher).publish(any(PerformBillingCommand.class));
			verify(ack).acknowledge();
		}

		@Test
		void shouldHandleOrderOrdered_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CREATED)).thenReturn(false);
			var order = OrderOrderedEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CREATED);
			verify(orderStockCommandPublisher, never()).publish(any(OrderStockCommand.class));
			verify(ack, never()).acknowledge();
		}

		@Test
		void shouldHandleOrderOrdered_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() {
			// given
			OrderOrderedEvent order = null;

			// when
			consumer.handle(order, ack);

			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(orderStockCommandPublisher, never()).publish(any(OrderStockCommand.class));
			verify(ack, never()).acknowledge();
		}
	}

	@Nested
	class Bill {
		@Test
		void shouldHandleBillPerformed_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED)).thenReturn(true);
			when(issueInvoiceCommandPublisher.publish(any())).thenReturn(Optional.of(PUBLISHED_ID));
			
			var order = BillPerformedEvent.builder().orderId(ORDER_ID).build();
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
			verify(issueInvoiceCommandPublisher).publish(any(IssueInvoiceCommand.class));
			verify(ack).acknowledge();
		}

		@Test
		void shouldHandleBillPerformed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED)).thenReturn(false);
			var order = BillPerformedEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
			verify(issueInvoiceCommandPublisher, never()).publish(any());
			verify(ack, never()).acknowledge();
		}

		@Test
		void shouldHandleBillPerformed_AndUnAckEventWhenOrderIsMalformed() {
			// given
			BillPerformedEvent order = null;

			// when
			consumer.handle(order, ack);

			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(issueInvoiceCommandPublisher, never()).publish(any(IssueInvoiceCommand.class));
			verify(ack, never()).acknowledge();
		}
	}

	@Nested
	class Schedule {
		@Test
		void shouldHandleInvoiceIssue_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED)).thenReturn(true);
			when(orderConfirmedCommandPublisher.publish(any(OrderConfirmedCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
			var order = OrderScheduledEvent.builder().orderId(ORDER_ID).build();
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED);
			verify(orderConfirmedCommandPublisher).publish(any(OrderConfirmedCommand.class));
			verify(ack).acknowledge();
		}

		@Test
		void shouldHandleInvoiceIssued_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED)).thenReturn(false);
			var order = OrderScheduledEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED);
			verify(orderConfirmedCommandPublisher, never()).publish(any(OrderConfirmedCommand.class));
			verify(ack, never()).acknowledge();
		}

		@Test
		void shouldHandleInvoiceIssue_AndUnAckEventWhenOrderIsMalformed() {
			// given
			OrderScheduledEvent order = null;

			// when
			consumer.handle(order, ack);

			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(scheduleOrderCommandPublisher, never()).publish(any(ScheduleOrderCommand.class));
			verify(ack, never()).acknowledge();
		}
	}

	@Nested
	class Invoice {
		@Test
		void shouldHandleOrderInvoiceIssued_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(true);
			when(scheduleOrderCommandPublisher.publish(any(ScheduleOrderCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
			var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
			verify(scheduleOrderCommandPublisher).publish(any(ScheduleOrderCommand.class));
			verify(ack).acknowledge();
		}

		@Test
		void shouldHandleOrderInvoiceIssued_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(false);
			var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
			verify(ack, never()).acknowledge();
		}

		@Test
		void shouldHandleOrderInvoiceIssued_AndUnAckEventWhenOrderIsMalformed() {
			// given
			InvoiceIssueEvent order = null;

			// when
			consumer.handle(order, ack);

			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
}
