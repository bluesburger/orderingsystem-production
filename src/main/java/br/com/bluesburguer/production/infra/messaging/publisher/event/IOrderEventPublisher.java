package br.com.bluesburguer.production.infra.messaging.publisher.event;

import java.util.Optional;

import br.com.bluesburguer.production.infra.messaging.OrderEvent;

public interface IOrderEventPublisher<T extends OrderEvent> {

	Optional<String> publish(T event);
}
