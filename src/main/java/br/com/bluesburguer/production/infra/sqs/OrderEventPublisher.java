package br.com.bluesburguer.production.infra.sqs;

import java.util.Optional;

import br.com.bluesburguer.production.application.dto.OrderEventDto;

public interface OrderEventPublisher<T extends OrderEventDto> {

	Optional<String> publish(T event);
}
