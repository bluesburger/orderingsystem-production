package br.com.bluesburguer.production.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.production.domain.entity.Fase;

class FaseUnitTests {

	@Test
	void shouldValidToString() {
		assertThat(Fase.REGISTERED).hasToString("REGISTERED");
		assertThat(Fase.CONFIRMED).hasToString("CONFIRMED");
		assertThat(Fase.FAILED).hasToString("FAILED");
		assertThat(Fase.CANCELED).hasToString("CANCELED");
	}
}
