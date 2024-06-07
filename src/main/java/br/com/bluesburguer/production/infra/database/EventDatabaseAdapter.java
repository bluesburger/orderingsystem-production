package br.com.bluesburguer.production.infra.database;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.bluesburguer.production.infra.database.entity.EventEntity;
import br.com.bluesburguer.production.infra.messaging.OrderEvent;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventDatabaseAdapter {

	private final EventRepository eventRepository;
	
	@Transactional(value = TxType.REQUIRES_NEW)
	public EventEntity save(OrderEvent orderEvent) {
		var entity = new EventEntity(orderEvent.getOrderId(), orderEvent.getEventName());
		return eventRepository.save(entity);
	}

	public List<EventEntity> findByOrderId(String orderId) {
		return eventRepository.findByOrderId(orderId);
	}
}
