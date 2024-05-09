package br.com.bluesburguer.production.core.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatusUnitTests {
	
	@Test
	void shouldNotInstanceWithAllParameteresInvalid() {
		assertThrows(NullPointerException.class, () -> new Status(null, null));
	}

	@ParameterizedTest
	@EnumSource(Step.class)
	void shouldNotInstanceWithoutFase(Step step) {
		assertThrows(NullPointerException.class, () -> new Status(step, null));
	}
	
	@ParameterizedTest
	@EnumSource(Fase.class)
	void shouldNotInstance_WithoutStep(Fase fase) {
		assertThrows(NullPointerException.class, () -> new Status(null, fase));
	}
	
	@ParameterizedTest
	@MethodSource("br.com.bluesburguer.production.support.ParametersSupport#provideStepAndFaseParameters")
	void shouldInstance_WithAllPossibleParameters(Step step, Fase fase) {
		assertThat(new Status(step, fase))
			.hasFieldOrPropertyWithValue("step", step)
			.hasFieldOrPropertyWithValue("fase", fase)
			.hasToString(String.format("Status(step=%s, fase=%s)", step, fase));
	}
}
