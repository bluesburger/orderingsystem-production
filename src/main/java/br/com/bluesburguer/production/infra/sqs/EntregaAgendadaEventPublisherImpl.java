package br.com.bluesburguer.production.infra.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.entrega.EntregaAgendadaDto;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class EntregaAgendadaEventPublisherImpl extends OrderEventPublisherImpl<EntregaAgendadaDto> {

	public EntregaAgendadaEventPublisherImpl(@Value("${queue.order.scheduled:order-scheduled}") String queueName) {
		super(queueName);
	}

}
