package br.com.bluesburguer.production.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class StatusUnitTests {
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldInstance_WithAllPossibleParameters(Step step, Fase fase) {
		assertThat(new Status(step, fase))
			.hasFieldOrPropertyWithValue("step", step)
			.hasFieldOrPropertyWithValue("fase", fase)
			.hasToString(String.format("Status(step=%s, fase=%s)", step, fase));
	}
}
