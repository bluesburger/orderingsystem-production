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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.events.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderStockFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.PerformBillingFailedEvent;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;

@ExtendWith(MockitoExtension.class)
class OrderOrchestratorRollbackUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	@Mock
	OrderPort orderPort;
	
	@Spy
	ObjectMapper mapper = new ObjectMapper();
	
	@InjectMocks
	OrderOrchestratorRollback consumer;
	
	@Mock
	Acknowledgment ack;
	
	@Nested
	class Bill {
		
		@Nested
		class Falhou {
			@Test
			void shouldHandlePerformBillingFailed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.FAILED)).thenReturn(true);
				var order = PerformBillingFailedEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.FAILED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandlePerformBillingFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.FAILED)).thenReturn(false);
				var order = PerformBillingFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.FAILED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandlePerformBillingFailed_AndUnAckEventWhenOrderIsMalformed() {
				// given
				PerformBillingFailedEvent order = null;
				
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
		class Failed {
			@Test
			void shouldHandleIssueInvoiceFailed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.FAILED)).thenReturn(true);
				var order = IssueInvoiceFailedEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.FAILED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleIssueInvoiceFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.FAILED)).thenReturn(false);
				var order = IssueInvoiceFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.FAILED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleIssueInvoiceFailed_AndUnAckEventWhenOrderIsMalformed() {
				// given
				IssueInvoiceFailedEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
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
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(true);
				var order = OrderStockFailedEvent.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderStockFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
				// given
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(false);
				var order = OrderStockFailedEvent.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderInvoiceIssued_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
				// given
				OrderStockFailedEvent order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
}
