package br.com.bluesburguer.production.domain.repository;

import java.util.Optional;

import br.com.bluesburguer.production.infra.messaging.OrderCommand;

public interface IOrderCommandPublisher<T extends OrderCommand> {

	Optional<String> publish(T command);
}
