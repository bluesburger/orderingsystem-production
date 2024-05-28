package br.com.bluesburguer.production.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import br.com.bluesburguer.production.domain.exception.EmailInvalidException;
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
public class Email implements Serializable {

	private static final long serialVersionUID = -2141519187977764558L;
	
	@NotNull
	@NonNull
	private String value;

	public Email(String value) {
		validate(value);
		this.value = value;
	}

	@Override
	public String toString() {
		return this.value;
	}

	private void validate(String value) {
		if (Objects.isNull(value)) {
			throw new EmailInvalidException();
		}
		
		if (!isValidEmail(value)) {
			throw new EmailInvalidException();
		}
	}
	
	private boolean isValidEmail(String emailAddress) {
		String regexPattern = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		return Pattern.compile(regexPattern)
			      .matcher(emailAddress)
			      .matches();
	}
}
