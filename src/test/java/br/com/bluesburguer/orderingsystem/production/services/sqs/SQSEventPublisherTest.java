package br.com.bluesburguer.orderingsystem.production.services.sqs;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.bluesburguer.orderingsystem.order.Fase;
import br.com.bluesburguer.orderingsystem.order.Status;
import br.com.bluesburguer.orderingsystem.order.Step;
import br.com.bluesburguer.orderingsystem.production.BaseIntegrationTest;

class SQSEventPublisherTest extends BaseIntegrationTest {
	
	@Autowired
	private SQSEventPublisher publisher;

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
	    private static final Random PRNG = new Random();
	    private final T[] values;

	    public RandomEnumGenerator(Class<T> e) {
	        values = e.getEnumConstants();
	    }

	    public T randomEnum() {
	        return values[PRNG.nextInt(values.length)];
	    }
	}
}
