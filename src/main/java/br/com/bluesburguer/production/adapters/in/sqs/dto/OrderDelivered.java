package br.com.bluesburguer.production.adapters.in.sqs.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderDelivered extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@JsonCreator
	public OrderDelivered(@JsonProperty("orderId") String orderId) {
		super(orderId);
	}
}
