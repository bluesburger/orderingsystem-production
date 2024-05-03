package br.com.bluesburguer.orderingsystem.production.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public abstract class OrderEvent implements Serializable {
	
	private static final long serialVersionUID = -3527498526085380774L;
	
	@NonNull
	protected Long orderId;
}
