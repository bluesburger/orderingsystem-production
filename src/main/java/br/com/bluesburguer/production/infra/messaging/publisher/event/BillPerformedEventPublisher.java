package br.com.bluesburguer.production.infra.messaging.publisher.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.infra.messaging.event.BillPerformedEvent;
import br.com.bluesburguer.production.infra.messaging.publisher.OrderEventPublisher;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class BillPerformedEventPublisher extends OrderEventPublisher<BillPerformedEvent> {

	public BillPerformedEventPublisher(@Value("${queue.bill.performed-event:bill-performed-event.fifo}") String queueName) {
		super(queueName);
	}

}
