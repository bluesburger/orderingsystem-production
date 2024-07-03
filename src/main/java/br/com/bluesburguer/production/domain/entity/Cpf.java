package br.com.bluesburguer.production.domain.entity;

import java.io.Serializable;
import java.util.Objects;

import br.com.bluesburguer.production.domain.exception.CpfInvalidException;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
import io.micrometer.common.lang.NonNull;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
@Embeddable
public class Cpf implements Serializable {

	private static final long serialVersionUID = 6911888938392251986L;

	@NotNull
	@NonNull
	private String value;

	public Cpf(String value) {
		this.value = validateCpf(value);
	}

	@Override
	public String toString() {
		return this.value;
	}

	private String validateCpf(String value) {
		if (Objects.isNull(value)) {
			throw new CpfInvalidException();
		}

		try {
			var isFormatted = CPFValidator.FORMATED.matcher(value).find();
			var validator = new CPFValidator(isFormatted);
			validator.assertValid(value);
			return value;
		} catch (InvalidStateException ise) {
			throw new CpfInvalidException(ise);
		}
	}
}
