package br.com.bluesburguer.production.core.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FaseUnitTests {

	@Test
	void shouldValidToString() {
		assertThat(Fase.PENDING).hasToString("PENDING");
		assertThat(Fase.IN_PROGRESS).hasToString("IN_PROGRESS");
		assertThat(Fase.DONE).hasToString("DONE");
		assertThat(Fase.CANCELED).hasToString("CANCELED");
	}
}
