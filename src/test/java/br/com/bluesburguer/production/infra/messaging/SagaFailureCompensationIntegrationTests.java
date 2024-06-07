package br.com.bluesburguer.production.infra.messaging;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import br.com.bluesburguer.production.application.dto.OrderItemRequest;
import br.com.bluesburguer.production.application.dto.OrderRequest;
import br.com.bluesburguer.production.application.dto.UserRequest;
import br.com.bluesburguer.production.domain.entity.Cpf;
import br.com.bluesburguer.production.domain.entity.Email;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.adapter.OrderClient;
import br.com.bluesburguer.production.infra.messaging.event.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderStockFailedEvent;
import br.com.bluesburguer.production.infra.messaging.event.PerformBillingFailedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.event.IOrderEventPublisher;
import br.com.bluesburguer.production.support.OrderMocks;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;

@ExtendWith(SpringExtension.class)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@WireMockTest
class SagaFailureCompensationIntegrationTests extends SqsBaseIntegrationSupport {
	
	private final AmazonSQS sqs;
	private final SqsQueueSupport<OrderCommand> sqsQueueSupport;
	
	private final OrderClient orderClient;
	
	private final IOrderEventPublisher<OrderStockFailedEvent> orderStockFailedPublisher;
	private final IOrderEventPublisher<PerformBillingFailedEvent> performBillingFailedPublisher;
	private final IOrderEventPublisher<IssueInvoiceFailedEvent> issueInvoiceFailedPublisher;
	
	@Value("${queue.cancel-order-command}")
	private String queueCancelOrderCommand;
	
	@Value("${queue.cancel-order-stock-command}")
	private String queueCancelOrderStockCommand;
	
	@Value("${queue.cancel-billing-command}")
	private String queueCancelBillingCommand;
	
	@RegisterExtension
	static WireMockExtension wme = WireMockExtension.newInstance() //
			.options(wireMockConfig() // configuração padrão
					.port(8000) // porta definida nas configurações de testes
					.notifier(new ConsoleNotifier(true)) // saída no console
			).proxyMode(true).build();
	
	@AfterEach
	void tearDown() {
		pruneQueue(queueCancelOrderCommand);
		pruneQueue(queueCancelOrderStockCommand);
		pruneQueue(queueCancelBillingCommand);
	}
	
	private String mockCreateNewOrder() throws JsonProcessingException {
		var step = Step.ORDER;
		var fase = Fase.CREATED;		
		String orderId = createNewOrder();
		mockOrderClient(wme, orderId, step, fase);
		return orderId;
	}
		
	@Test
	@DisplayName("Dado um pedido que falhou na reserva de estoque, quando StockService publicar OrderStockFailedEvent, então ProductionService deve executar saga de cancelamento")
	void givenOrderStockFailedEvent_WhenConsume_ThenOrderShouldBeUpdatedToStepDELIVERYAndFaseFailed() throws JsonProcessingException {
		String orderId = mockCreateNewOrder();

		var event = new OrderStockFailedEvent(orderId);
		assertThat(orderStockFailedPublisher.publish(event))
			.isPresent();
		
		queueHasCommandQuantity(queueCancelOrderCommand, 1);
	}
	
	@Test
	@DisplayName("Dado um pedido que falhou na execução do pagamento, quando PaymentService publicar PerformBillingFailedEvent, então ProductionService deve executar saga de cancelamento")
	void givenPerformBillingFailedEvent_WhenConsume_ThenShouldExecuteCompensationSaga() throws JsonProcessingException {
		String orderId = mockCreateNewOrder();

		var event = new PerformBillingFailedEvent(orderId);
		assertThat(performBillingFailedPublisher.publish(event))
			.isPresent();
		
		queueHasCommandQuantity(queueCancelOrderStockCommand, 1);
		queueHasCommandQuantity(queueCancelOrderCommand, 1);
	}
	
	@Test
	@DisplayName("Dado um pedido que falhou na emissão da nota fiscal, quando NotaFiscalService publicar IssueInvoiceFailedEvent, então ProductionService deve executar saga de cancelamento")
	void givenIssueInvoiceFailedEvent_WhenConsume_ThenShouldExecuteCompensationSaga() throws JsonProcessingException {
		String orderId = mockCreateNewOrder();

		var event = new IssueInvoiceFailedEvent(orderId);
		assertThat(issueInvoiceFailedPublisher.publish(event))
			.isPresent();
		
		queueHasCommandQuantity(queueCancelOrderStockCommand, 1);
		queueHasCommandQuantity(queueCancelBillingCommand, 1);
		queueHasCommandQuantity(queueCancelOrderCommand, 1);
	}
	
	private String createNewOrder() throws JsonProcessingException {
		var orderRequest = new OrderRequest(
				List.of(new OrderItemRequest(1L, 1)), 
				new UserRequest(1L, new Cpf(OrderMocks.mockCpf()), new Email(OrderMocks.mockEmail())));
		
		mockOrderClientCreateNewOrder(wme, "asd-asd-asd-asd-asd", orderRequest);
		
		var response = orderClient.createNewOrder(orderRequest);
		return response.headers().get("Location").stream().findFirst()
				.orElseThrow(() -> new RuntimeException("Id do pedido não encontrado na resposta"));
	}
	
	private Integer numberOfMessagesInQueue(String queueName) {
        GetQueueAttributesResult attributes = sqs
                .getQueueAttributes(queueName, of("All"));

        return Integer.parseInt(
                attributes.getAttributes().get("ApproximateNumberOfMessages")
        );
    }
	
	private Integer numberOfMessagesNotVisibleInQueue(String queueName) {
        GetQueueAttributesResult attributes = sqs
                .getQueueAttributes(queueName, of("All"));

        return Integer.parseInt(
            attributes.getAttributes().get("ApproximateNumberOfMessagesNotVisible")
        );
    }
	
	private void queueHasCommandQuantity(String queueName, int quantity) {
		await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
        	var queue = sqsQueueSupport.buildQueueUrl(queueName);
            assertThat(numberOfMessagesInQueue(queue)).isEqualTo(quantity);
            assertThat(numberOfMessagesNotVisibleInQueue(queue)).isZero();
        });
	}
	
	private void pruneQueue(String queueName) {
		var queue = sqsQueueSupport.buildQueueUrl(queueName);
		sqs.purgeQueue(new PurgeQueueRequest(queue));
	}
}
