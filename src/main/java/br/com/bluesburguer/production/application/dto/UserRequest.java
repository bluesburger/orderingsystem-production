package br.com.bluesburguer.production.application.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.bluesburguer.production.domain.entity.Cpf;
import br.com.bluesburguer.production.domain.entity.Email;
import jakarta.persistence.Embedded;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRequest implements Serializable {
	
	private static final long serialVersionUID = 3695870866440519918L;

	@JsonProperty(value = "id")
	private Long id;
	
	@JsonProperty(value = "cpf")
	@Embedded
	private Cpf cpf;
	
	@JsonProperty(value = "email")
	@Embedded
	private Email email;
}
