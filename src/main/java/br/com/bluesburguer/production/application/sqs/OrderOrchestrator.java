package br.com.bluesburguer.production.application.sqs;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

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
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "cloud.aws.sqs.listener.auto-startup", havingValue = "true"
)
@Transactional
public class OrderOrchestrator {
	
	private final OrderPort orderPort;
	private final EventDatabaseAdapter eventDatabaseAdapter;
	
	// Pedido
	@SqsListener(value = "${queue.order.registered:order-registered}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload PedidoRegistradoDto event, Acknowledgment ack) {
		log.info("Event received on queue order-registered: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.ORDER, Fase.REGISTERED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.confirmed:order-confirmed}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload PedidoConfirmadoDto event, Acknowledgment ack) {
		log.info("Event received on queue order-confirmed: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.ORDER, Fase.CONFIRMED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.canceled:order-canceled}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(PedidoCanceladoDto event, Acknowledgment ack) throws JsonProcessingException {
		log.info("Event received on queue order-canceled: {}", event);
    	if (ObjectUtils.isNotEmpty(event) && update(event, Step.ORDER, Fase.CANCELED)) {
    		eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }

	// Cobranca
	@SqsListener(value = "${queue.order.paid:order-paid}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload CobrancaRealizadaDto event, Acknowledgment ack) {
		log.info("Event received on queue order-paid: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.CHARGE, Fase.CONFIRMED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.failed-on-payment:order-failed-on-payment}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload CobrancaFalhouDto event, Acknowledgment ack) {
		log.info("Event received on queue order-failed-paid: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.CHARGE, Fase.FAILED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	// Entrega
	@SqsListener(value = "${queue.order.scheduled:order-scheduled}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload EntregaAgendadaDto event, Acknowledgment ack) {
		log.info("Event received on queue order-scheduled: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.DELIVERY, Fase.REGISTERED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.failed-delivery:order-failed-delivery}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload EntregaFalhouDto event, Acknowledgment ack) {
		log.info("Event received on queue order-failed-delivery: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.DELIVERY, Fase.FAILED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.performed-delivery:order-performed-delivery}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
	public void handle(@Payload EntregaEfetuadaDto event, Acknowledgment ack) {
		log.info("Event received on queue order-performed-delivery: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.DELIVERY, Fase.CONFIRMED)) {
			eventDatabaseAdapter.save(event);
			ack.acknowledge();
		}
	}
	
	// NotaFiscal
	@SqsListener(value = "${queue.order.invoice-issued:order-invoice-issued}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload NotaFiscalEmitidaDto event, Acknowledgment ack) {
		log.info("Event received on queue order-invoice-issued: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.INVOICE, Fase.CONFIRMED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	@SqsListener(value = "${queue.order.invoice-failed-issued:order-invoice-failed-issued}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void handle(@Payload NotaFiscalFalhouNaEmissaoDto event, Acknowledgment ack) {
		log.info("Event received on queue order-invoice-failed-issued: {}", event);
		
		if (ObjectUtils.isNotEmpty(event) && update(event, Step.INVOICE, Fase.FAILED)) {
			eventDatabaseAdapter.save(event);
    		ack.acknowledge();
    	}
    }
	
	private boolean update(OrderEventDto order, Step step, Fase fase) {
		if (orderPort.update(order.getOrderId(), step, fase)) {
    		log.info("Order status updated to step {} and fase {}", step, fase);
    		return true;
    	}
		return false;
	}
}
