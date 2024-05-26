package br.com.bluesburguer.production.application.sqs;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
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
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;

class OrderOrchestratorIntegrationTests extends SqsBaseIntegrationSupport {

	@Autowired
	AmazonSQSAsync SQS;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	OrderPort orderPort;
	
	// Pedido
	@Value("${queue.order.registered:order-registered}")
	private String queueOrderRegistered;
	
	@Value("${queue.order.confirmed:order-confirmed}")
	private String queueOrderConfirmed;
	
	@Value("${queue.order.canceled:order-canceled}")
	private String queueOrderCanceled;
	
	// Cobrança
	@Value("${queue.order.paid:order-paid}")
	private String queueOrderPaid;
	
	@Value("${queue.order.failed-on-payment:order-failed-on-payment}")
	private String queueOrderFailedOnPayment;
	
	// Entrega
	@Value("${queue.order.scheduled:order-scheduled}")
	private String queueOrderScheduled;
	
	@Value("${queue.order.failed-delivery:order-failed-delivery}")
	private String queueOrderFailedOnSchedule;
	
	@Value("${queue.order.performed-delivery:order-performed-delivery}")
	private String queueOrderScheduledPerformed;
	
	// Nota Fiscal
	@Value("${queue.order.invoice-issued:order-invoice-issued}")
	private String queueOrderInvoiceIssued;
	
	@Value("${queue.order.invoice-failed-issued:order-invoice-failed-issued}")
	private String queueOrderInvoiceFailedIssued;
	
	@BeforeEach
	void setUp() {
		doReturn(true)
			.when(orderPort)
			.update(anyString(), any(Step.class), any(Fase.class));
	}
	
	@ParameterizedTest
	@MethodSource("generateOrders")
	void shouldSendOrderEvent_Consume_AndUpdateStatusSuccessfully(OrderEventDto orderEvent) throws JsonProcessingException {
		sendToQueue(orderEvent);
	}
	
	void sendToQueue(OrderEventDto orderEvent) throws JsonProcessingException {
		// given
		var queueUrl = SQS.getQueueUrl(defineQueueUrl(orderEvent)).getQueueUrl();
		
		// when
		SQS.sendMessage(new SendMessageRequest()
				.withQueueUrl(queueUrl)
				.withMessageBody(objectMapper.writeValueAsString(orderEvent)));
		
		// then
		await().atMost(3, SECONDS).untilAsserted(() -> {
            assertThat(numberOfMessagesInQueue(queueUrl)).isZero();
            assertThat(numberOfMessagesNotVisibleInQueue(queueUrl)).isZero();
        });
	}
	
	class UnrecognizedDto extends OrderEventDto {
		private static final long serialVersionUID = -2879485837938440488L;
	}
	
	@Test
	void shouldThrowException_WhenUnrecognizedQueueIsParameterized() {
		UnrecognizedDto unrecognizedDto = new UnrecognizedDto();
		assertThrows(QueueDoesNotExistException.class, () -> defineQueueUrl(unrecognizedDto), "Queue not covered by integration tests");
	}
	
	public String defineQueueUrl(OrderEventDto orderEvent) {
		// Pedido
		if (orderEvent instanceof PedidoRegistradoDto) {
			return queueOrderRegistered;
		}
		
		if (orderEvent instanceof PedidoConfirmadoDto) {
			return queueOrderRegistered;
		}
		
		if (orderEvent instanceof PedidoCanceladoDto) {
			return queueOrderCanceled;
		}
		
		// Cobrança
		if (orderEvent instanceof CobrancaRealizadaDto) {
			return queueOrderPaid;
		}
		
		if (orderEvent instanceof CobrancaFalhouDto) {
			return queueOrderFailedOnPayment;
		}
		
		// Entrega
		if (orderEvent instanceof EntregaAgendadaDto) {
			return queueOrderScheduled;
		}
		
		if (orderEvent instanceof EntregaFalhouDto) {
			return queueOrderFailedOnSchedule;
		}
		
		if (orderEvent instanceof EntregaEfetuadaDto) {
			return queueOrderScheduledPerformed;
		}
		
		// Nota Fiscal
		if (orderEvent instanceof NotaFiscalEmitidaDto) {
			return queueOrderInvoiceIssued;
		}
		
		if (orderEvent instanceof NotaFiscalFalhouNaEmissaoDto) {
			return queueOrderInvoiceFailedIssued;
		}
		
		throw new QueueDoesNotExistException("Queue not covered by integration tests");
	}
	
	private static Stream<OrderEventDto> generateOrders() {
		String orderId = "556f2b18-bda4-4d05-934f-7c0063d78f48";
		return Stream.of(
				PedidoRegistradoDto.builder(), 
				PedidoConfirmadoDto.builder(),
				PedidoCanceladoDto.builder().step(Step.KITCHEN),
				
				CobrancaRealizadaDto.builder(),
				CobrancaFalhouDto.builder(),
				
				EntregaAgendadaDto.builder(),
				EntregaFalhouDto.builder(),
				EntregaEfetuadaDto.builder(),
				
				NotaFiscalEmitidaDto.builder(),
				NotaFiscalFalhouNaEmissaoDto.builder()
			).map(b -> b.orderId(orderId).build());
	}
	
	private Integer numberOfMessagesInQueue(String consumerQueueName) {
        GetQueueAttributesResult attributes = SQS
                .getQueueAttributes(consumerQueueName, List.of("All"));

        return Integer.parseInt(
                attributes.getAttributes().get("ApproximateNumberOfMessages")
        );
    }

    private Integer numberOfMessagesNotVisibleInQueue(String consumerQueueName) {
        GetQueueAttributesResult attributes = SQS
                .getQueueAttributes(consumerQueueName, List.of("All"));

        return Integer.parseInt(
            attributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible")
        );
    }
}
