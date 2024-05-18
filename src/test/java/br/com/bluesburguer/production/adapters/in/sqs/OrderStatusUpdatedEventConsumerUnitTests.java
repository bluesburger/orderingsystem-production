package br.com.bluesburguer.production.adapters.in.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderCanceled;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivered;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivering;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderInProduction;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderProduced;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;

@ExtendWith(MockitoExtension.class)
class OrderStatusUpdatedEventConsumerUnitTests {
	
	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";
	
	@Mock
	OrderPort orderPort;
	
	@Spy
	ObjectMapper mapper = new ObjectMapper();
	
	@InjectMocks
	OrderStatusUpdatedEventConsumer consumer;
	
	@Mock
	Acknowledgment ack;
	
	@Nested
	class Paid {
		@Test
		void shouldHandleOrderPaid_AndAckEventWhenOrderUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.KITCHEN, Fase.PENDING)).thenReturn(true);
			var order = OrderPaid.builder().orderId(ORDER_ID).build();
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.KITCHEN, Fase.PENDING);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderPaid_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
			// given
			when(orderPort.update(ORDER_ID, Step.KITCHEN, Fase.PENDING)).thenReturn(false);
			var order = OrderPaid.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.KITCHEN, Fase.PENDING);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderPaid_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			// given
			OrderPaid order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class InProduction {
		@Test
		void shouldHandleOrderInProduction_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.KITCHEN, Fase.IN_PROGRESS)).thenReturn(true);
			var order = OrderInProduction.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.KITCHEN, Fase.IN_PROGRESS);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderInProduction_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.KITCHEN, Fase.IN_PROGRESS)).thenReturn(false);
			var order = OrderInProduction.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.KITCHEN, Fase.IN_PROGRESS);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderInProduction_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			// given
			OrderInProduction order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Produced {
		@Test
		void shouldHandleOrderProduced_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.PENDING)).thenReturn(true);
			var order = OrderProduced.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.PENDING);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderProduced_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.PENDING)).thenReturn(false);
			var order = OrderProduced.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.PENDING);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderProduced_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			// given
			OrderProduced order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Delivering {
		@Test
		void shouldHandleOrderDelivering_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.IN_PROGRESS)).thenReturn(true);
			var order = OrderDelivering.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.IN_PROGRESS);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivering_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.IN_PROGRESS)).thenReturn(false);
			var order = OrderDelivering.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.IN_PROGRESS);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivering_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			// given
			OrderDelivering order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Delivered {
		@Test
		void shouldHandleOrderDelivered_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.DONE)).thenReturn(true);
			var order = OrderDelivered.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.DONE);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivered_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.DONE)).thenReturn(false);
			var order = OrderDelivered.builder().orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.DONE);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivered_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			// given
			OrderDelivered order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Canceled {
		@ParameterizedTest
		@EnumSource(Step.class)
		void shouldHandleOrderCanceled_AndAckEventWhenOrderUpdatedWithSuccess(Step step) throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, step, Fase.CANCELED)).thenReturn(true);
			var order = OrderCanceled.builder().step(step).orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, step, Fase.CANCELED);
			verify(ack).acknowledge();
		}
		
		@ParameterizedTest
		@EnumSource(Step.class)
		void shouldHandleOrderCanceled_AndUnAckEventWhenOrderNotUpdatedWithSuccess(Step step) throws JsonProcessingException {
			// given
			when(orderPort.update(ORDER_ID, step, Fase.CANCELED)).thenReturn(false);
			var order = OrderCanceled.builder().step(step).orderId(ORDER_ID).build();
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort).update(ORDER_ID, step, Fase.CANCELED);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderCanceled_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() throws JsonProcessingException {
			// given
			OrderCanceled order = null;
			
			// when
			consumer.handle(order, ack);
			
			// then
			verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
}
