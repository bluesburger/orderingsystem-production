package br.com.bluesburguer.production.support;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import br.com.bluesburguer.production.core.domain.Fase;
import br.com.bluesburguer.production.core.domain.Step;

public final class ParametersSupport {

	public static Stream<Arguments> provideStepAndFaseParameters() {
		return Stream.of(Step.values()).flatMap(step -> {
			return Stream.of(Fase.values()).map(fase -> {
				return Arguments.of(step, fase);
			});
		});
	}
}
