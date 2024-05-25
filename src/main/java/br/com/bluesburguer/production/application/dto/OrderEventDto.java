package br.com.bluesburguer.production.application.dto;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class OrderEventDto implements Serializable {
	
	private static final long serialVersionUID = -3527498526085380774L;
	
	@NonNull
	@JsonProperty
	protected String orderId;
	
	@JsonCreator
	protected OrderEventDto(@JsonProperty("orderId") String orderId) {
		Objects.requireNonNull(orderId);
		this.orderId = orderId;
	}
}
