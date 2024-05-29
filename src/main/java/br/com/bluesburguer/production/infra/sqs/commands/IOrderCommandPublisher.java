package br.com.bluesburguer.production.infra.sqs.commands;

import java.util.Optional;

import br.com.bluesburguer.production.application.sqs.commands.OrderCommand;

public interface IOrderCommandPublisher<T extends OrderCommand> {

	Optional<String> publish(T command);
}
