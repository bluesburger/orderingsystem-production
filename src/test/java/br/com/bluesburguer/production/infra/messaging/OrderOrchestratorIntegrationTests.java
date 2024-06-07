package br.com.bluesburguer.production.infra.messaging;

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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.messaging.event.BillPerformedEvent;
import br.com.bluesburguer.production.infra.messaging.event.InvoiceIssueEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderCreatedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderOrderedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderScheduledEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderStockFailedEvent;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;

class OrderOrchestratorIntegrationTests extends SqsBaseIntegrationSupport {

	@Autowired
	AmazonSQSAsync SQS;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	SqsQueueSupport<OrderEvent> sqsQueueSupport;
	
	@MockBean
	UpdateOrderUseCase orderPort;
	
	@Value("${queue.order.created-event:order-created-event}")
	private String queueOrderCreatedEvent;
	
	@Value("${queue.order.ordered-event:order-ordered-event}")
	private String queueOrderOrderedEvent;
	
	@Value("${queue.bill.performed-event:bill-performed-event}")
	private String queueBillPerformedEvent;
	
	@Value("${queue.invoice-issued-event:invoice-issued-event}")
	private String queueInvoiceIssueEvent;
	
	@Value("${queue.order.scheduled-event:order-scheduled-event}")
	private String queueOrderScheduledEvent;
	
	@Value("${queue.order.stock-failed-event:order-stock-failed-event.fifo}")
	private String queueOrderStockFailedEvent;
	
	@BeforeEach
	void setUp() {
		doReturn(true)
			.when(orderPort)
			.update(anyString(), any(Step.class), any(Fase.class));
	}
	
	@ParameterizedTest
	@MethodSource("generateOrderEvents")
	void shouldSendOrderEvent_Consume_AndUpdateStatusSuccessfully(OrderEvent orderEvent) throws JsonProcessingException {
		sendToQueue(orderEvent);
	}
	
	void sendToQueue(OrderEvent orderEvent) throws JsonProcessingException {
		// given
		var queueName = defineQueueUrl(orderEvent);
		var queueUrl = SQS.getQueueUrl(queueName).getQueueUrl();

		var request = sqsQueueSupport.createRequest(orderEvent, queueName, "order-events");
		
		// when
		SQS.sendMessage(request);
		
		// then
		await().atMost(3, SECONDS).untilAsserted(() -> {
            assertThat(numberOfMessagesInQueue(queueUrl)).isZero();
            assertThat(numberOfMessagesNotVisibleInQueue(queueUrl)).isZero();
        });
	}
	
	class UnrecognizedDto extends OrderEvent {
		private static final long serialVersionUID = -2879485837938440488L;

		@Override
		public String getEventName() {
			return "UNRECOGNIZED_EVENT";
		}
	}
	
	@Test
	void shouldThrowException_WhenUnrecognizedQueueIsParameterized() {
		UnrecognizedDto unrecognizedDto = new UnrecognizedDto();
		assertThrows(QueueDoesNotExistException.class, () -> defineQueueUrl(unrecognizedDto), "Queue not covered by integration tests");
	}
	
	public String defineQueueUrl(OrderEvent orderEvent) {
		// Pedido
		if (orderEvent instanceof OrderCreatedEvent) {
			return queueOrderCreatedEvent;
		}
		
		if (orderEvent instanceof OrderOrderedEvent) {
			return queueOrderOrderedEvent;
		}
		
		if (orderEvent instanceof BillPerformedEvent) {
			return queueBillPerformedEvent;
		}
		
		if (orderEvent instanceof InvoiceIssueEvent) {
			return queueInvoiceIssueEvent;
		}
		
		if (orderEvent instanceof OrderScheduledEvent) {
			return queueOrderScheduledEvent;
		}
		
		if (orderEvent instanceof OrderStockFailedEvent) {
			return queueOrderStockFailedEvent;
		}
		
		throw new QueueDoesNotExistException("Queue not covered by integration tests");
	}
	
	private static Stream<OrderEvent> generateOrderEvents() {
		String orderId = "556f2b18-bda4-4d05-934f-7c0063d78f48";
		return Stream.of(
				OrderCreatedEvent.builder(), 
				OrderOrderedEvent.builder(),
				BillPerformedEvent.builder(),
				InvoiceIssueEvent.builder(),
				OrderScheduledEvent.builder(),
				OrderStockFailedEvent.builder()
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
