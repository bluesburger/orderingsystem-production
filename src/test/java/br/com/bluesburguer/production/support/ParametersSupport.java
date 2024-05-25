package br.com.bluesburguer.production.support;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import br.com.bluesburguer.production.domain.entity.Fase;
import br.com.bluesburguer.production.domain.entity.Step;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ParametersSupport {

	public static Stream<Arguments> provideStepAndFaseParameters() {
		return Stream.of(Step.values()).flatMap(step -> {
			return Stream.of(Fase.values()).map(fase -> {
				return Arguments.of(step, fase);
			});
		});
	}
}
