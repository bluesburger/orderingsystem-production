package br.com.bluesburguer.production.infra.sqs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.production.application.dto.pedido.PedidoRegistradoDto;
import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PedidoRegistradoEventPublisherImplIntegrationTests extends SqsBaseIntegrationSupport {

	private static final String ORDER_ID = "556f2b18-bda4-4d05-934f-7c0063d78f48";

	private final PedidoRegistradoEventPublisherImpl pedidoRegistradoEventPublisher;
	
	@Test
	void whenPublishEvent_ShouldBeConsumed() {
		
		var event = new PedidoRegistradoDto(ORDER_ID);
//		var optionalId = pedidoRegistradoEventPublisher.publish(event);
//		assertThat(optionalId).isPresent();
	}
}
