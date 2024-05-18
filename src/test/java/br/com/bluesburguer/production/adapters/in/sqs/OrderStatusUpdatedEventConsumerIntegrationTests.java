package br.com.bluesburguer.production.adapters.in.sqs;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderCanceled;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivered;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderDelivering;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderEvent;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderInProduction;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderProduced;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;

class OrderStatusUpdatedEventConsumerIntegrationTests extends SqsBaseIntegrationSupport {

	@Autowired
	AmazonSQSAsync SQS;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	OrderPort orderPort;
	
	@Value("${queue.order.paid:order-paid.fifo}")
	private String queueOrderPaid;
	
	@Value("${queue.order.in-production:order-in-production.fifo}")
	private String queueOrderInProduction;
	
	@Value("${queue.order.produced:order-produced.fifo}")
	private String queueOrderProduced;
	
	@Value("${queue.order.delivering:order-delivering.fifo}")
	private String queueOrderDelivering;
	
	@Value("${queue.order.delivered:order-delivered.fifo}")
	private String queueOrderDelivered;
	
	@Value("${queue.order.canceled:order-canceled.fifo}")
	private String queueOrderCanceled;
	
	@BeforeEach
	void setUp() {
		doReturn(true)
			.when(orderPort)
			.update(anyString(), any(Step.class), any(Fase.class));
	}
	
	@ParameterizedTest
	@MethodSource("generateOrders")
	void shouldSendOrderEvent_Consume_AndUpdateStatusSuccessfully(OrderEvent orderEvent) throws JsonProcessingException {
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
	
	public String defineQueueUrl(OrderEvent orderEvent) {
		if (orderEvent instanceof OrderPaid) {
			return queueOrderPaid;
		}
		
		if (orderEvent instanceof OrderInProduction) {
			return queueOrderInProduction;
		}
		
		if (orderEvent instanceof OrderProduced) {
			return queueOrderProduced;
		}
		
		if (orderEvent instanceof OrderDelivering) {
			return queueOrderDelivering;
		}
		
		if (orderEvent instanceof OrderDelivered) {
			return queueOrderDelivered;
		}
		
		if (orderEvent instanceof OrderCanceled) {
			return queueOrderCanceled;
		}
		
		return "";
	}
	
	private static Stream<OrderEvent> generateOrders() {
		String orderId = "556f2b18-bda4-4d05-934f-7c0063d78f48";
		return Stream.of(
				OrderPaid.builder(), 
				OrderInProduction.builder(),
				OrderProduced.builder(),
				OrderDelivering.builder(),
				OrderDelivered.builder(),
				OrderCanceled.builder().step(Step.KITCHEN)
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
