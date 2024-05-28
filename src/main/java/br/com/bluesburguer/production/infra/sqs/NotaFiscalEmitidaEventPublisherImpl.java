package br.com.bluesburguer.production.infra.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.notafiscal.NotaFiscalEmitidaDto;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class NotaFiscalEmitidaEventPublisherImpl extends OrderEventPublisherImpl<NotaFiscalEmitidaDto> {

	public NotaFiscalEmitidaEventPublisherImpl(@Value("${queue.order.invoice-issued:order-invoice-issued}") String queueName) {
		super(queueName);
	}

}
