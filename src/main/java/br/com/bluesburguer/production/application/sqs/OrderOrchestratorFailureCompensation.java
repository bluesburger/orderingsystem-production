package br.com.bluesburguer.production.application.sqs;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.events.IssueInvoiceFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderStockFailedEvent;
import br.com.bluesburguer.production.application.sqs.events.PerformBillingFailedEvent;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderOrchestratorFailureCompensation {

	private final OrderPort orderPort;
	private final EventDatabaseAdapter eventDatabaseAdapter;

	@SqsListener(value = "${queue.order.stock-failed-event:queue.order.stock-failed-event}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(OrderStockFailedEvent event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue: {}", event.getEventName());

		if (execute(event, Step.DELIVERY, Fase.FAILED)) {
			ack.acknowledge();
		}
	}

	@SqsListener(value = "${queue.order.perform-billing-failed-event:queue.order.perform-billing-failed-event}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload PerformBillingFailedEvent event, Acknowledgment ack) {
		log.info("Event received on queue: {}", event.getEventName());

		if (execute(event, Step.CHARGE, Fase.FAILED)) {
			ack.acknowledge();
		}
	}

	@SqsListener(value = "${queue.issue.invoice-failed-event:order-failed-delivery}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload IssueInvoiceFailedEvent event, Acknowledgment ack) {
		log.info("Event received on queue: {}", event.getEventName());

		if (execute(event, Step.INVOICE, Fase.FAILED)) {
			ack.acknowledge();
		}
	}

	private boolean execute(OrderEvent event, Step step, Fase fase) {
		try {
			if (ObjectUtils.isNotEmpty(event) && update(event, step, fase)) {
				eventDatabaseAdapter.save(event);
				return true;
			}
		} catch (Exception e) {
			update(event, step, Fase.FAILED);
			log.error("An error occurred", e);
		}
		return false;
	}

	private boolean update(OrderEvent order, Step step, Fase fase) {
		return update(order, step, fase, false);
	}

	private boolean update(OrderEvent order, Step step, Fase fase, boolean rollback) {
		if (orderPort.update(order.getOrderId(), step, fase)) {
			if (rollback) {
				log.warn("Order status {} rolled back to step {} and fase {}", order.getOrderId(), step, fase);
			} else {
				log.info("Order status {} updated to step {} and fase {}", order.getOrderId(), step, fase);
			}
			return true;
		}
		return false;
	}
}
