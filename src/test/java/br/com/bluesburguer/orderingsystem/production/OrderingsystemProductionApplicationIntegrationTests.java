package br.com.bluesburguer.orderingsystem.production;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.orderingsystem.production.utils.BaseIntegrationTest;

class OrderingsystemProductionApplicationIntegrationTests extends BaseIntegrationTest {

	@Test
	void context() {
		assertThat(super.hashCode()).isNotZero();
	}
}
