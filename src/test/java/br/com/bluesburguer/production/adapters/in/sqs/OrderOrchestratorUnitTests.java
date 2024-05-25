package br.com.bluesburguer.production.adapters.in.sqs;

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

import br.com.bluesburguer.production.application.dto.cobranca.CobrancaFalhouDto;
import br.com.bluesburguer.production.application.dto.cobranca.CobrancaRealizadaDto;
import br.com.bluesburguer.production.application.dto.entrega.EntregaAgendadaDto;
import br.com.bluesburguer.production.application.dto.entrega.EntregaEfetuadaDto;
import br.com.bluesburguer.production.application.dto.entrega.EntregaFalhouDto;
import br.com.bluesburguer.production.application.dto.notafiscal.NotaFiscalEmitidaDto;
import br.com.bluesburguer.production.application.dto.notafiscal.NotaFiscalFalhouNaEmissaoDto;
import br.com.bluesburguer.production.application.dto.pedido.PedidoCanceladoDto;
import br.com.bluesburguer.production.application.dto.pedido.PedidoConfirmadoDto;
import br.com.bluesburguer.production.application.dto.pedido.PedidoRegistradoDto;
import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.OrderOrchestrator;
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
	class Pedido {
		
		@Nested
		class Registrado {
			@Test
			void shouldHandleOrderRegistered_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.REGISTERED)).thenReturn(true);
				var order = PedidoRegistradoDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.REGISTERED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderRegistered_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.REGISTERED)).thenReturn(false);
				var order = PedidoRegistradoDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.REGISTERED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderRegistered_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() {
				// given
				PedidoRegistradoDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Confirmado {
			@Test
			void shouldHandleOrderConfirmed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CONFIRMED)).thenReturn(true);
				var order = PedidoConfirmadoDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CONFIRMED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderConfirmed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CONFIRMED)).thenReturn(false);
				var order = PedidoConfirmadoDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CONFIRMED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderConfirmed_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() {
				// given
				PedidoConfirmadoDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Canceled {
			@Test
			void shouldHandleOrderCanceled_AndAckEventWhenOrderUpdatedWithSuccess() throws JsonProcessingException {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CANCELED)).thenReturn(true);
				var order = PedidoCanceladoDto.builder().step(Step.ORDER).orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CANCELED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderCanceled_AndUnAckEventWhenOrderNotUpdatedWithSuccess() throws JsonProcessingException {
				// given
				when(orderPort.update(ORDER_ID, Step.ORDER, Fase.CANCELED)).thenReturn(false);
				var order = PedidoCanceladoDto.builder().step(Step.ORDER).orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.ORDER, Fase.CANCELED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderCanceled_AndUnackEventWhenOrderIsMalformed_BecauseIsEmpty() throws JsonProcessingException {
				// given
				PedidoCanceladoDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Cobranca {
		
		@Nested
		class Realizada {
			@Test
			void shouldHandleOrderPaid_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED)).thenReturn(true);
				var order = CobrancaRealizadaDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderPaid_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED)).thenReturn(false);
				var order = CobrancaRealizadaDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.CONFIRMED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderPaid_AndUnAckEventWhenOrderIsMalformed() {
				// given
				CobrancaRealizadaDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Falhou {
			@Test
			void shouldHandleOrderFailedOnPayment_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.FAILED)).thenReturn(true);
				var order = CobrancaFalhouDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.FAILED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderFailedOnPayment_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.CHARGE, Fase.FAILED)).thenReturn(false);
				var order = CobrancaFalhouDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.CHARGE, Fase.FAILED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderFailedOnPayment_AndUnAckEventWhenOrderIsMalformed() {
				// given
				CobrancaRealizadaDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
	
	@Nested
	class Entrega {
		
		@Nested
		class Agendada {
			@Test
			void shouldHandleOrderScheduled_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED)).thenReturn(true);
				var order = EntregaAgendadaDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderScheduled_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED)).thenReturn(false);
				var order = EntregaAgendadaDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.REGISTERED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderScheduled_AndUnAckEventWhenOrderIsMalformed() {
				// given
				EntregaAgendadaDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Falhou {
			@Test
			void shouldHandleOrderFailedDelivery_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.FAILED)).thenReturn(true);
				var order = EntregaFalhouDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.FAILED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderFailedDelivery_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.DELIVERY, Fase.FAILED)).thenReturn(false);
				var order = EntregaFalhouDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.DELIVERY, Fase.FAILED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderFailedDelivery_AndUnAckEventWhenOrderIsMalformed() {
				// given
				EntregaFalhouDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
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
	}
	
	@Nested
	class NotaFiscal {
		
		@Nested
		class Emitida {
			@Test
			void shouldHandleOrderInvoiceIssued_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED)).thenReturn(true);
				var order = NotaFiscalEmitidaDto.builder().orderId(ORDER_ID).build();
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
				var order = NotaFiscalEmitidaDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.CONFIRMED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderInvoiceIssued_AndUnAckEventWhenOrderIsMalformed() {
				// given
				NotaFiscalEmitidaDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
		
		@Nested
		class Falhou {
			@Test
			void shouldHandleOrderInvoiceFailed_AndAckEventWhenOrderUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.FAILED)).thenReturn(true);
				var order = NotaFiscalFalhouNaEmissaoDto.builder().orderId(ORDER_ID).build();
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.FAILED);
				verify(ack).acknowledge();
			}
			
			@Test
			void shouldHandleOrderInvoiceFailed_AndUnAckEventWhenOrderNotUpdatedWithSuccess() {
				// given
				when(orderPort.update(ORDER_ID, Step.INVOICE, Fase.FAILED)).thenReturn(false);
				var order = NotaFiscalFalhouNaEmissaoDto.builder().orderId(ORDER_ID).build();
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort).update(ORDER_ID, Step.INVOICE, Fase.FAILED);
				verify(ack, never()).acknowledge();
			}
			
			@Test
			void shouldHandleOrderInvoiceFailed_AndUnAckEventWhenOrderIsMalformed() {
				// given
				NotaFiscalFalhouNaEmissaoDto order = null;
				
				// when
				consumer.handle(order, ack);
				
				// then
				verify(orderPort, never()).update(anyString(), any(Step.class), any(Fase.class));
				verify(ack, never()).acknowledge();
			}
		}
	}
}
