package br.com.bluesburguer.production.application.sqs.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderStockFailedEvent extends OrderEvent {
	
	public static final String EVENT_NAME = "PEDIDO_CANCELADO";

	private static final long serialVersionUID = 7702500048926979660L;
	
	@JsonCreator
	public OrderStockFailedEvent(@JsonProperty("orderId") String orderId) {
		super(orderId);
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}
}
