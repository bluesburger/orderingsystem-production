package br.com.bluesburguer.production.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.production.domain.entity.Step;

class StepUnitTests {

	@Test
	void shouldValidToString() {
		assertThat(Step.ORDER).hasToString("ORDER");
		assertThat(Step.KITCHEN).hasToString("KITCHEN");
		assertThat(Step.DELIVERY).hasToString("DELIVERY");
	}
}
