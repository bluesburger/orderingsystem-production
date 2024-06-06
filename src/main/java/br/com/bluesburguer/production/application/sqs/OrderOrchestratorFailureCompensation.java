package br.com.bluesburguer.production.application.sqs;

import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.production.application.sqs.commands.CancelBillingCommand;
import br.com.bluesburguer.production.application.sqs.commands.CancelOrderCommand;
import br.com.bluesburguer.production.application.sqs.commands.CancelOrderStockCommand;
import br.com.bluesburguer.production.application.sqs.events.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderStockFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.PerformBillingFailedEvent;
import br.com.bluesburguer.production.infra.sqs.commands.IOrderCommandPublisher;
import br.com.bluesburguer.production.infra.sqs.events.IOrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderOrchestratorFailureCompensation {
	
	private final IOrderCommandPublisher<CancelOrderCommand> cancelOrderPublisher;
	private final IOrderCommandPublisher<CancelOrderStockCommand> cancelOrderStockPublisher;
	private final IOrderCommandPublisher<CancelBillingCommand> cancelBillingPublisher;
	
	private final IOrderEventPublisher<OrderStockFailedEvent> orderStockFailedEventPublisher;
	private final IOrderEventPublisher<PerformBillingFailedEvent> performBillingFailedEventPublisher;

	// FIXME: incluir persistência de eventos
	
	@SqsListener(value = "${queue.order.stock-failed-event:order-stock-failed-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(OrderStockFailedEvent event, Acknowledgment ack) throws JsonProcessingException {
		if (Objects.nonNull(event)) {
			log.info("Event received on queue: {}", event.getEventName());
			
			var command = new CancelOrderCommand(event.getOrderId());
			if (cancelOrderPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	@SqsListener(value = "${queue.order.perform-billing-failed-event:perform-billing-failed-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload PerformBillingFailedEvent event, Acknowledgment ack) {
		if (Objects.nonNull(event)) {
			log.info("Event received on queue: {}", event.getEventName());

			var command = new CancelOrderStockCommand(event.getOrderId());
			if (cancelOrderStockPublisher.publish(command).isPresent()) {
				var compensationEvent = new OrderStockFailedEvent(event.getOrderId());
				if (orderStockFailedEventPublisher.publish(compensationEvent).isPresent()) {
					ack.acknowledge();
				}
			}
		}
	}

	@SqsListener(value = "${queue.issue.invoice-failed-event:order-failed-delivery.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload IssueInvoiceFailedEvent event, Acknowledgment ack) {
		if (Objects.nonNull(event)) {
			log.info("Event received on queue: {}", event.getEventName());
		
			var command = new CancelBillingCommand(event.getOrderId());
			if (cancelBillingPublisher.publish(command).isPresent()) {
				var compensationEvent = new PerformBillingFailedEvent(event.getOrderId());
				if (performBillingFailedEventPublisher.publish(compensationEvent).isPresent()) {
					ack.acknowledge();
				}
			}
		}
	}
}
