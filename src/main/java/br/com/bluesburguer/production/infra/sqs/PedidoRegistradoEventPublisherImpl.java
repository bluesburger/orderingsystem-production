package br.com.bluesburguer.production.infra.sqs;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.production.application.dto.pedido.PedidoRegistradoDto;
import lombok.ToString;

@ToString(callSuper = true)
@Service
public class PedidoRegistradoEventPublisherImpl extends OrderEventPublisherImpl<PedidoRegistradoDto> {

	protected PedidoRegistradoEventPublisherImpl() {
		super("order-registered");
	}
}
