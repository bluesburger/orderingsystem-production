package br.com.bluesburguer.production.adapters.in.sqs;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;

import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;

@ExtendWith(MockitoExtension.class)
class OrderStatusUpdatedEventConsumerUnitTests {
	
	private static final String ORDER_EVENT_STR = "{\"orderId\":1}";
	private static final String CANCELED_EVENT_FORMAT = "{\"orderId\":1, \"step\": \"%s\"}";
	
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
		void shouldHandleOrderPaid_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.KITCHEN, Fase.PENDING)).thenReturn(true);
			
			// when
			consumer.handleOrderPaid(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.KITCHEN, Fase.PENDING);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderPaid_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.KITCHEN, Fase.PENDING)).thenReturn(false);
			
			// when
			consumer.handleOrderPaid(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.KITCHEN, Fase.PENDING);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderPaid_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderPaid("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class InProduction {
		@Test
		void shouldHandleOrderInProduction_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.KITCHEN, Fase.IN_PROGRESS)).thenReturn(true);
			
			// when
			consumer.handleOrderInProduction(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.KITCHEN, Fase.IN_PROGRESS);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderInProduction_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.KITCHEN, Fase.IN_PROGRESS)).thenReturn(false);
			
			// when
			consumer.handleOrderInProduction(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.KITCHEN, Fase.IN_PROGRESS);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderInProduction_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderInProduction("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Produced {
		@Test
		void shouldHandleOrderProduced_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.PENDING)).thenReturn(true);
			
			// when
			consumer.handleOrderProduced(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.PENDING);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderProduced_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.PENDING)).thenReturn(false);
			
			// when
			consumer.handleOrderProduced(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.PENDING);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderProduced_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderProduced("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Delivering {
		@Test
		void shouldHandleOrderDelivering_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.IN_PROGRESS)).thenReturn(true);
			
			// when
			consumer.handleOrderDelivering(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.IN_PROGRESS);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivering_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.IN_PROGRESS)).thenReturn(false);
			
			// when
			consumer.handleOrderDelivering(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.IN_PROGRESS);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivering_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderDelivering("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Delivered {
		@Test
		void shouldHandleOrderDelivered_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.DONE)).thenReturn(true);
			
			// when
			consumer.handleOrderDelivered(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.DONE);
			verify(ack).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivered_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
			// given
			when(orderPort.update(1L, Step.DELIVERY, Fase.DONE)).thenReturn(false);
			
			// when
			consumer.handleOrderDelivered(ORDER_EVENT_STR, ack);
			
			// then
			verify(orderPort).update(1L, Step.DELIVERY, Fase.DONE);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderDelivered_AndUnAckEventWhenOrderIsMalformed() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderDelivered("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
	
	@Nested
	class Canceled {
		@ParameterizedTest
		@EnumSource(Step.class)
		void shouldHandleOrderCanceled_AndAckEventWhenOrderUpdatedWithSuccess(Step step) throws JsonProcessingException {
			// given
			when(orderPort.update(1L, step, Fase.CANCELED)).thenReturn(true);
			
			// when
			consumer.handleOrderCanceled(String.format(CANCELED_EVENT_FORMAT, step), ack);
			
			// then
			verify(orderPort).update(1L, step, Fase.CANCELED);
			verify(ack).acknowledge();
		}
		
		@ParameterizedTest
		@EnumSource(Step.class)
		void shouldHandleOrderCanceled_AndUnAckEventWhenOrderNotUpdatedWithSuccess(Step step) throws JsonProcessingException {
			// given
			when(orderPort.update(1L, step, Fase.CANCELED)).thenReturn(false);
			
			// when
			consumer.handleOrderCanceled(String.format(CANCELED_EVENT_FORMAT, step), ack);
			
			// then
			verify(orderPort).update(1L, step, Fase.CANCELED);
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderCanceled_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderCanceled("{}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
		
		@ParameterizedTest
		@EnumSource(Step.class)
		void shouldHandleOrderCanceled_AndUnackEventWhenOrderIsMalformed_BecauseHasNoOrderId(Step step) throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderCanceled("{\"step\": \"" + step + "\"}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
		
		@Test
		void shouldHandleOrderCanceled_AndUnackEventWhenOrderIsMalformed_BecauseHasNoStep() throws JsonProcessingException {
			
			// when
			assertThrows(ValueInstantiationException.class, () -> consumer.handleOrderCanceled("{\"orderId\": \"1\"}", ack));
			
			// then
			verify(orderPort, never()).update(anyLong(), any(Step.class), any(Fase.class));
			verify(ack, never()).acknowledge();
		}
	}
}
