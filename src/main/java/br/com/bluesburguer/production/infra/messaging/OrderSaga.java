package br.com.bluesburguer.production.infra.messaging;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.messaging.event.BillPerformedEvent;
import br.com.bluesburguer.production.infra.messaging.event.IssueInvoiceEvent;
import br.com.bluesburguer.production.infra.messaging.event.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderCreatedEvent;
import br.com.bluesburguer.production.infra.messaging.event.OrderOrderedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSaga extends RouteBuilder {

	private final UpdateOrderUseCase updateOrderUseCase;
	
//	private final OrderClient orderClient;
//	private final PaymentClient paymentClient;
	
	@Value("${cloud.aws.endpoint.uri}")
	private String sqsUrl;
	
	@Value("${cloud.aws.account-id}")
	private String accountId;
	
	@Override
	public void configure() throws Exception {
		from(defineUri("order-created-event.fifo"))
	        .routeId("Rota de pedido")
	        .log(LoggingLevel.WARN, "${body}")
	        .convertBodyTo(String.class)
	        .bean(OrderSaga.class, "handleOrderCreatedEvent(${body})")
	        .to(defineUri("queue-order-stock-command.fifo"));
		
		from(defineUri("order-ordered-event.fifo"))
			.routeId("Rota de estoque")
			.log(LoggingLevel.WARN, "${body}")
			.choice()
	            .when(body().contains("RESERVED"))
	            	.convertBodyTo(String.class)
			        .bean(OrderSaga.class, "handleOrderOrderedEvent(${body})")
	            	.log(LoggingLevel.WARN, "estoque reservado")
					// usuário passou a requisitar o pagamento no TOTEM após selecionar o modelo de pagamento
	                // .to(defineUri("queue-perform-billing-command.fifo"))
	            	// .bean(OrderSaga.class, "handlePerformBillingCommand(${body})")
			        .bean(OrderSaga.class, "handleOrderPaidEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido pago")
	                .to(defineUri("queue-invoice-command.fifo"))
	            .otherwise()
	            	.log(LoggingLevel.WARN, "reserva de estoque falhou")
	            	.convertBodyTo(String.class)
	            	.bean(OrderSaga.class, "handleOrderOrderedFailedEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido atualizado para status de falha");
		
		from(defineUri("bill-performed-event.fifo"))
			.routeId("Rota de pagamento")
			.log(LoggingLevel.WARN, "${body}")
			.choice()
				.when(body().contains("PENDING"))
					.convertBodyTo(String.class)
			        .bean(OrderSaga.class, "handleOrderPaymentRequestEvent(${body})")
	            	.log(LoggingLevel.WARN, "pagamento solicitado")
	            .when(body().contains("PAID"))
		            .convertBodyTo(String.class)
			        .bean(OrderSaga.class, "handleOrderPaidEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido pago")
	                .to(defineUri("queue-invoice-command.fifo"))
	            .otherwise()
	              	.log(LoggingLevel.WARN, "pagamento de pedido falhou")
	              	.convertBodyTo(String.class)
	            	.bean(OrderSaga.class, "handleOrderPaidFailedEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido atualizado para status de falha");
		
		from(defineUri("invoice-issued-event.fifo"))
			.routeId("Rota de nota fiscal")
			.log(LoggingLevel.WARN, "${body}")
			.choice()
				.when(body().contains("PENDING"))
		            .convertBodyTo(String.class)
			        .bean(OrderSaga.class, "handleIssueInvoicePendingEvent(${body})")
		        	.log(LoggingLevel.WARN, "nota fiscal solicitada")
	            .when(body().contains("ISSUED"))
		            .convertBodyTo(String.class)
			        .bean(OrderSaga.class, "handleIssueInvoiceEvent(${body})")
	            	.log(LoggingLevel.WARN, "nota fiscal gerada")
	                .to(defineUri("queue-schedule-order-command.fifo"))
	            .otherwise()
	            	.log(LoggingLevel.WARN, "geração de nota fiscal falhou")
	              	.convertBodyTo(String.class)
	            	.bean(OrderSaga.class, "handleIssueInvoiceFailedEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido atualizado para status de falha");
		
		from(defineUri("order-scheduled-event.fifo"))
			.routeId("Rota de entrega")
			.log(LoggingLevel.WARN, "${body}")
			.choice()
	            .when(body().contains("SCHEDULED"))
	            	.log(LoggingLevel.WARN, "entrega agendada")
	            	.bean(OrderSaga.class, "handleOrderSheculedEvent(${body})")
	            .otherwise()
	              	.log(LoggingLevel.WARN, "agendamento de entrega falhou")
	              	.convertBodyTo(String.class)
	            	.bean(OrderSaga.class, "handleOrderSheculedFailedEvent(${body})")
	            	.log(LoggingLevel.WARN, "pedido atualizado para status de falha");
	}
	
	void handleOrderCreatedEvent(String json) {
		var event = to(json, OrderCreatedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.ORDER, Fase.CREATED);
	}
	
	void handleOrderOrderedEvent(String json) {
		var event = to(json, OrderOrderedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.DELIVERY, Fase.CREATED);
	}
/*
	void handlePerformBillingCommand(String json) {
		var event = to(json, OrderOrderedEvent.class);
		var orderDto = orderClient.getById(event.getOrderId());
		
		String orderStatus = "PAID";
		String orderCreatedTime = LocalDateTime.now().toString();
		String userId = String.valueOf(orderDto.getUser().getId());
		String paymentMethod = "PIX"; // por enquanto fixo devido a api de pagamentos dar suporte apenas a esse formato
		paymentClient.makePayment(new PaymentRequest(new PaymentRequest.Data(event.getOrderId(), orderStatus, orderCreatedTime, userId, paymentMethod)));
	}
*/
	
	void handleOrderOrderedFailedEvent(String json) {
		var event = to(json, OrderOrderedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.DELIVERY, Fase.FAILED);
	}
	
	void handleOrderPaidEvent(String json) {
		var event = to(json, BillPerformedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.CHARGE, Fase.CONFIRMED);
	}
	
	void handleOrderPaymentRequestEvent(String json) {
		var event = to(json, BillPerformedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.CHARGE, Fase.REGISTERED);
	}
	
	void handleOrderPaidFailedEvent(String json) {
		var event = to(json, BillPerformedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.CHARGE, Fase.FAILED);
	}
	
	void handleIssueInvoiceEvent(String json) {
		var event = to(json, IssueInvoiceEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.INVOICE, Fase.CONFIRMED);
	}
	
	void handleIssueInvoicePendingEvent(String json) {
		var event = to(json, IssueInvoiceEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.INVOICE, Fase.REGISTERED);
	}
	
	void handleIssueInvoiceFailedEvent(String json) {
		var event = to(json, IssueInvoiceFailedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.INVOICE, Fase.FAILED);
	}
	
	void handleOrderSheculedEvent(String json) {
		var event = to(json, OrderOrderedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.ORDER, Fase.CONFIRMED);
	}
	
	void handleOrderSheculedFailedEvent(String json) {
		var event = to(json, OrderOrderedEvent.class);
		updateOrderUseCase.update(event.getOrderId(), Step.DELIVERY, Fase.FAILED);
	}
	
	private <T> T to(String json, Class<T> aClass) {
		return new Gson().fromJson(json, aClass);
	}
	
	private String defineUri(String queueName) {
		return String.format(new StringBuilder("aws2-sqs://%s?queueUrl=%s/%s/%s?")
				.append("?amazonSQSClient=#myClient")
				.append("&autoCreateQueue=true")
				.append("&useDefaultCredentialsProvider=true")
				.append("&useProfileCredentialsProvider=true")
				.append("&useSessionCredentials=false")
				.append("&messageGroupIdStrategy=useConstant")
//				.append("&delay=500000")
				.toString(), queueName, sqsUrl, accountId, queueName);
	}
}
