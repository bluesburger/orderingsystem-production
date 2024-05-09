package br.com.bluesburguer.production.adapters.out.order;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderEvent;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderInProduction;
import br.com.bluesburguer.production.adapters.in.sqs.dto.OrderPaid;
import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;
import br.com.bluesburguer.production.ports.OrderPort;
import br.com.bluesburguer.production.utils.BaseIntegrationTest;

class OrderClientIntegrationTests extends BaseIntegrationTest {
	
	@Autowired
	AmazonSQSAsync SQS;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@MockBean
	OrderPort orderPort;
	
	@Value("${queue.order.paid:order-paid.fifo}")
	private String queueOrderPaid;
	
	@BeforeEach
	void setUp() {
		doReturn(true)
			.when(orderPort)
			.update(anyLong(), any(Step.class), any(Fase.class));
	}
	
	@Test
	void shouldSendOrderPaidEvent_AndWhereConsumedSuccessfully() throws JsonProcessingException {
		// given
		var orderPaid = OrderPaid.builder().orderId(1L).build();
		var queueUrl = SQS.getQueueUrl(queueOrderPaid).getQueueUrl();
		
		// when
		sendSqsMessage(queueUrl, orderPaid);
		
		// validation
        validateQueueConsume(queueUrl);
	}
	
	@Test
	void shouldSendOrderInProductionEvent_AndWhereConsumedSuccessfully() throws JsonProcessingException {
		// given
		var orderPaid = OrderInProduction.builder().orderId(1L).build();
		var queueUrl = SQS.getQueueUrl(queueOrderPaid).getQueueUrl();
		
		// when
		sendSqsMessage(queueUrl, orderPaid);
		
		// validation
        validateQueueConsume(queueUrl);
	}
	
	private void sendSqsMessage(String queueUrl, OrderEvent orderEvent) throws JsonProcessingException {
		SQS.sendMessage(new SendMessageRequest()
				.withQueueUrl(queueUrl)
				.withMessageBody(objectMapper.writeValueAsString(orderEvent)));
	}
	
	private void validateQueueConsume(String queueUrl) {
		await().atMost(3, SECONDS).untilAsserted(() -> {
            assertThat(numberOfMessagesInQueue(queueUrl)).isZero();
            assertThat(numberOfMessagesNotVisibleInQueue(queueUrl)).isZero();
        });
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
