package br.com.bluesburguer.production.application.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.events.BillPerformedEvent;
import br.com.bluesburguer.production.application.sqs.events.InvoiceIssueEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderCreatedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderOrderedEvent;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;

@ExtendWith(MockitoExtension.class)
class OrderOrchestratorUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	@Mock
	OrderPort orderPort;
	
	@Spy
	ObjectMapper mapper = new ObjectMapper();
	
	@InjectMocks
	OrderOrchestrator consumer;
	
	@Mock
	Acknowledgment ack;
	
	@Nested
	class Order {
		
		@Nested
		class Created {
			@Test
			void shouldHandleOrderCreated_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.REGISTERED)).thenReturn(true);
				var order = OrderCreatedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.REGISTERED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderCreated_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.REGISTERED)).thenReturn(false);
				var order = OrderCreatedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.REGISTERED);
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
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Stock {
			@Test
			void shouldHandleOrderOrdered_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CONFIRMED)).thenReturn(true);
				var order = OrderOrderedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CONFIRMED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderOrdered_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CONFIRMED)).thenReturn(false);
				var order = OrderOrderedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CONFIRMED);
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
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Bill {
		
		@Nested
		class Performed {
			@Test
			void shouldHandleBillPerformed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED)).thenReturn(true);
				var order = BillPerformedEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
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
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Invoice {
		
		@Nested
		class Issue {
			@Test
			void shouldHandleInvoiceIssue_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED)).thenReturn(true);
				var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleInvoiceIssued_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED)).thenReturn(false);
				var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleInvoiceIssue_AndUnAckEventWhenOrderIsMalformed() {
				// given
				InvoiceIssueEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
/*
		@Nested
		class Efetuada {
			@Test
			void shouldHandleOrderPerformedDelivery_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED)).thenReturn(true);
				var order = EntregaEfetuadaDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderPerformedDelivery_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED)).thenReturn(false);
				var order = EntregaEfetuadaDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.CONFIRMED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderPerformedDelivery_AndUnAckEventWhenOrderIsMalformed() {
				// given
				EntregaEfetuadaDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
*/
	}
	
	@Nested
	class Stock {
		
		@Nested
		class Succcess {
			@Test
			void shouldHandleOrderInvoiceIssued_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(true);
				var order = InvoiceIssueEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
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
}
