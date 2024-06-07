package br.com.bluesburguer.production.infra.messaging.listener;

import java.util.Objects;

import org.springframework.cloud.aws.messaging.listener.Acknowledgment;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import br.com.bluesburguer.production.domain.usecase.UpdateOrderUseCase;
import br.com.bluesburguer.production.infra.database.EventDatabaseAdapter;
import br.com.bluesburguer.production.infra.messaging.OrderEvent;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@RequiredArgsConstructor
public abstract class MessageListener<E extends OrderEvent> {
	
	private final UpdateOrderUseCase orderPort;
	private final EventDatabaseAdapter eventDatabaseAdapter;

	abstract void handle(E event, Acknowledgment ack);
	
	boolean execute(OrderEvent event, Step step, Fase fase) {
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
