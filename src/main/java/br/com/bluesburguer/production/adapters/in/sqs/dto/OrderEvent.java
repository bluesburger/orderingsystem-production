package br.com.bluesburguer.production.adapters.in.sqs.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@SuperBuilder
public abstract class OrderEvent implements Serializable {
	
	private static final long serialVersionUID = -3527498526085380774L;
	
	@NonNull
	@JsonProperty
	protected String orderId;
	
	@JsonCreator
	protected OrderEvent(@NonNull @JsonProperty("orderId") String orderId) {
		this.orderId = orderId;
	}
}
