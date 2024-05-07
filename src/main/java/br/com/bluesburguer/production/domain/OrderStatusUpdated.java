package br.com.bluesburguer.production.domain;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.com.bluesburguer.production.order.domain.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class OrderStatusUpdated implements Serializable {

	private static final long serialVersionUID = -2138667769166531317L;
	
	@NonNull
	private UUID id;

	@NonNull
	private Status newStatus;
}
