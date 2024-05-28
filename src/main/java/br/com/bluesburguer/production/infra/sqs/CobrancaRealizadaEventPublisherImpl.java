package br.com.bluesburguer.production.infra.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.cobranca.CobrancaRealizadaDto;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class CobrancaRealizadaEventPublisherImpl extends OrderEventPublisherImpl<CobrancaRealizadaDto> {

	public CobrancaRealizadaEventPublisherImpl(@Value("${queue.order.paid:order-paid}") String queueName) {
		super(queueName);
	}

}
