package br.com.bluesburguer.production.infra.sqs.events;

import java.util.Optional;

import br.com.bluesburguer.production.application.sqs.events.OrderEvent;

public interface IOrderEventPublisher<T extends OrderEvent> {

	Optional<String> publish(T event);
}
