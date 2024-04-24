package br.com.bluesburguer.orderingsystem.production.infra.sqs;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.orderingsystem.order.domain.Fase;
import br.com.bluesburguer.orderingsystem.order.domain.Status;
import br.com.bluesburguer.orderingsystem.order.domain.Step;
import br.com.bluesburguer.orderingsystem.production.utils.BaseIntegrationTest;

class OrderStatusUpdatedEventPublisherTest extends BaseIntegrationTest {
	
	@Autowired
	private OrderStatusUpdatedEventPublisher publisher;

	@Test
	void shouldPublishEvent() throws JsonProcessingException, InterruptedException {
		for (int i = 1; i <= 10; i++) {
			var step = new RandomEnumGenerator<Step>(Step.class).randomEnum();
			var fase = new RandomEnumGenerator<Fase>(Fase.class).randomEnum();
			
			var status = new Status(step, fase);
			publisher.publishEvent(status);
		}
	}
	
	private class RandomEnumGenerator<T extends Enum<T>> {
	    private static final Random RANDOM = new Random();
	    private final T[] values;

	    public RandomEnumGenerator(Class<T> e) {
	        values = e.getEnumConstants();
	    }

	    public T randomEnum() {
	        return values[RANDOM.nextInt(values.length)];
	    }
	}
}
