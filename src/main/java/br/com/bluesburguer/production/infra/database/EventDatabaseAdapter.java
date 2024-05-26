package br.com.bluesburguer.production.infra.database;

import org.springframework.stereotype.Component;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
import br.com.bluesburguer.production.infra.database.entity.EventEntity;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventDatabaseAdapter {

	private final EventRepository eventRepository;
	
	public EventEntity save(OrderEventDto orderEvent) {
		var entity = new EventEntity(orderEvent.getOrderId(), orderEvent.getClass().getName());
		return eventRepository.save(entity);
	}
}
