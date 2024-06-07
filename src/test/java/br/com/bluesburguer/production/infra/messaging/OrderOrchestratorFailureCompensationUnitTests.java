package br.com.bluesburguer.production.infra.messaging;

import static org.mockito.ArgumentMatchers.any;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.messaging.command.CancelBillingCommand;
import br.com.bluesburguer.production.infra.messaging.command.CancelOrderCommand;
import br.com.bluesburguer.production.infra.messaging.command.CancelOrderStockCommand;
import br.com.bluesburguer.production.infra.messaging.command.IssueInvoiceCommand;
import br.com.bluesburguer.production.infra.messaging.command.OrderConfirmedCommand;
import br.com.bluesburguer.production.infra.messaging.command.OrderStockCommand;
import br.com.bluesburguer.production.infra.messaging.command.PerformBillingCommand;
import br.com.bluesburguer.production.infra.messaging.command.ScheduleOrderCommand;
import br.com.bluesburguer.production.infra.messaging.event.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderStockFailedEvent;
import br.com.bluesburguer.production.infra.messaging.event.PerformBillingFailedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.command.CancelBillingCommandPublisher;
import br.com.bluesburguer.production.infra.messaging.publisher.command.CancelOrderCommandPublisher;
import br.com.bluesburguer.production.infra.messaging.publisher.command.CancelOrderStockCommandPublisher;
import br.com.bluesburguer.production.infra.messaging.publisher.event.OrderStockFailedEventPublisher;
import br.com.bluesburguer.production.infra.messaging.publisher.event.PerformBillingFailedEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderOrchestratorFailureCompensationUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	private static final String PUBLISHED_ID = "asd-asd-asd-asd-asd";

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
	CancelOrderCommandPublisher cancelOrderPublisher;
	
	@Mock
	CancelOrderStockCommandPublisher cancelOrderStockPublisher;
	
	@Mock
	CancelBillingCommandPublisher cancelBillingPublisher;
	
	@Mock
	OrderStockFailedEventPublisher orderStockFailedEventPublisher;

	@Mock
	PerformBillingFailedEventPublisher performBillingFailedEventPublisher;

	OrderOrchestratorFailureCompensation consumer;

	@BeforeEach
	void initService() {
		consumer = new OrderOrchestratorFailureCompensation(cancelOrderPublisher, cancelOrderStockPublisher,
				cancelBillingPublisher, orderStockFailedEventPublisher, performBillingFailedEventPublisher);
	}
	
	@Nested
	class Bill {
		
		@Nested
		class Failed {
			@Test
			void shouldHandlePerformBillingFailed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				var order = PerformBillingFailedEvent.builder().orderId(ORDER_ID).build();
				when(cancelOrderStockPublisher.publish(any(CancelOrderStockCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
				when(orderStockFailedEventPublisher.publish(any(OrderStockFailedEvent.class))).thenReturn(Optional.of(PUBLISHED_ID));
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderStockPublisher).publish(any(CancelOrderStockCommand.class));
				verify(orderStockFailedEventPublisher).publish(any(OrderStockFailedEvent.class));
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandlePerformBillingFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				var order = PerformBillingFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderStockPublisher).publish(any(CancelOrderStockCommand.class));
				verify(orderStockFailedEventPublisher, never()).publish(any(OrderStockFailedEvent.class));
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandlePerformBillingFailed_AndUnAckEventWhenOrderIsMalformed() {
				// given
				PerformBillingFailedEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderStockPublisher, never()).publish(any(CancelOrderStockCommand.class));
				verify(orderStockFailedEventPublisher, never()).publish(any(OrderStockFailedEvent.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Invoice {
		
		@Nested
		class Failed {
			@Test
			void shouldHandleIssueInvoiceFailed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				var order = IssueInvoiceFailedEvent.builder().orderId(ORDER_ID).build();
				when(cancelBillingPublisher.publish(any(CancelBillingCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
				when(performBillingFailedEventPublisher.publish(any(PerformBillingFailedEvent.class))).thenReturn(Optional.of(PUBLISHED_ID));
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelBillingPublisher).publish(any(CancelBillingCommand.class));
				verify(performBillingFailedEventPublisher).publish(any(PerformBillingFailedEvent.class));
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleIssueInvoiceFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				var order = IssueInvoiceFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelBillingPublisher).publish(any(CancelBillingCommand.class));
				verify(performBillingFailedEventPublisher, never()).publish(any(PerformBillingFailedEvent.class));
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleIssueInvoiceFailed_AndUnAckEventWhenOrderIsMalformed() {
				// given
				IssueInvoiceFailedEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelBillingPublisher, never()).publish(any(CancelBillingCommand.class));
				verify(performBillingFailedEventPublisher, never()).publish(any(PerformBillingFailedEvent.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Stock {
		
		@Nested
		class Failed {
			@Test
			void shouldHandleOrderStockFailed_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
				// given
				var order = OrderStockFailedEvent.builder().orderId(ORDER_ID).build();
				when(cancelOrderPublisher.publish(any(CancelOrderCommand.class))).thenReturn(Optional.of(PUBLISHED_ID));
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderPublisher).publish(any(CancelOrderCommand.class));
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderStockFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
				// given
				var order = OrderStockFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderPublisher).publish(any(CancelOrderCommand.class));
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderInvoiceIssued_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
				// given
				OrderStockFailedEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(cancelOrderPublisher, never()).publish(any(CancelOrderCommand.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
}
