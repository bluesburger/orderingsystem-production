package br.com.bluesburguer.production.infra.sqs.events;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.sqs.events.BillPerformedEvent;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class BillPerformedEventPublisher extends OrderEventPublisher<BillPerformedEvent> {

	public BillPerformedEventPublisher(@Value("${queue.bill.performed-event:bill-performed-event}") String queueName) {
		super(queueName);
	}

}
