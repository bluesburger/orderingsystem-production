package br.com.bluesburguer.production.application.sqs;

import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.ports.OrderPort;
import br.com.bluesburguer.production.application.sqs.commands.IssueInvoiceCommand;
import br.com.bluesburguer.production.application.sqs.commands.OrderConfirmedCommand;
import br.com.bluesburguer.production.application.sqs.commands.OrderStockCommand;
import br.com.bluesburguer.production.application.sqs.commands.PerformBillingCommand;
import br.com.bluesburguer.production.application.sqs.commands.ScheduleOrderCommand;
import br.com.bluesburguer.production.application.sqs.events.BillPerformedEvent;
import br.com.bluesburguer.production.application.sqs.events.InvoiceIssueEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderCreatedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderOrderedEvent;
import br.com.bluesburguer.production.application.sqs.events.OrderScheduledEvent;
import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.sqs.commands.IOrderCommandPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true")
public class OrderOrchestrator {

	private final OrderPort orderPort;
	private final EventDatabaseAdapter eventDatabaseAdapter;
	
	private final IOrderCommandPublisher<OrderStockCommand> orderStockCommandPublisher;
	private final IOrderCommandPublisher<PerformBillingCommand> performBillingCommandPublisher;
	private final IOrderCommandPublisher<IssueInvoiceCommand> issueInvoiceCommandPublisher;
	private final IOrderCommandPublisher<ScheduleOrderCommand> scheduleOrderCommandPublisher;
	private final IOrderCommandPublisher<OrderConfirmedCommand> orderConfirmedCommandPublisher;

	// Pedido
	@SqsListener(value = "${queue.order.created-event:order-created-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload OrderCreatedEvent event, Acknowledgment ack) {
		if (execute(event, Step.ORDER, Fase.CREATED)) {
			var command = new OrderStockCommand(event.getOrderId());
			if (orderStockCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	// Estoque
	@SqsListener(value = "${queue.order.ordered-event:order-ordered-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload OrderOrderedEvent event, Acknowledgment ack) {
		if (execute(event, Step.DELIVERY, Fase.CREATED)) {
			var command = new PerformBillingCommand(event.getOrderId());
			if (performBillingCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	// Cobrança
	@SqsListener(value = "${queue.bill.performed-event:bill-performed-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload BillPerformedEvent event, Acknowledgment ack) {
		if (execute(event, Step.CHARGE, Fase.CONFIRMED)) {
			var command = new IssueInvoiceCommand(event.getOrderId());
			if (issueInvoiceCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	// NotaFiscal
	@SqsListener(value = "${queue.invoice-issued-event:invoice-issued-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload InvoiceIssueEvent event, Acknowledgment ack) {
		if (execute(event, Step.INVOICE, Fase.CONFIRMED)) {
			var command = new ScheduleOrderCommand(event.getOrderId());
			if (scheduleOrderCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	// Entrega
	@SqsListener(value = "${queue.order.scheduled-event:order-scheduled-event.fifo}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload OrderScheduledEvent event, Acknowledgment ack) {
		if (execute(event, Step.DELIVERY, Fase.CONFIRMED)) {
			var command = new OrderConfirmedCommand(event.getOrderId());
			if (orderConfirmedCommandPublisher.publish(command).isPresent()) {
				ack.acknowledge();
			}
		}
	}

	private boolean execute(OrderEvent event, Step step, Fase fase) {
		if (Objects.nonNull(event)) {
			log.info("Event received on queue: {}", event.getEventName());
			
			try {
				if (orderPort.update(event.getOrderId(), step, fase)) {
					eventDatabaseAdapter.save(event);
					return true;
				}
			} catch (Exception e) {
				log.error("An error occurred", e);
			}
			orderPort.update(event.getOrderId(), step, Fase.FAILED);
		}
		return false;
	}
}
