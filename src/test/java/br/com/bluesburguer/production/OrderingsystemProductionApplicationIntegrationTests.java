package br.com.bluesburguer.production;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.production.utils.BaseIntegrationTest;

class OrderingsystemProductionApplicationIntegrationTests extends BaseIntegrationTest {

	//@Test
	void context() {
		assertThat(super.hashCode()).isNotZero();
	}
}
