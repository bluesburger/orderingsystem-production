package br.com.bluesburguer.production;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.bluesburguer.production.support.SqsBaseIntegrationSupport;

class OrderingsystemProductionApplicationIntegrationTests extends SqsBaseIntegrationSupport {

	@Test
	void context() {
		assertThat(super.hashCode()).isNotZero();
	}
}
