package br.com.bluesburguer.production.infra.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.pedido.PedidoConfirmadoDto;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PedidoConfirmadoEventPublisherImpl extends OrderEventPublisherImpl<PedidoConfirmadoDto> {

	public PedidoConfirmadoEventPublisherImpl(@Value("${queue.order.confirmed:order-confirmed}") String queueName) {
		super(queueName);
	}

}
