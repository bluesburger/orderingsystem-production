package br.com.bluesburguer.production.infra.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.bluesburguer.production.infra.messaging.OrderEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BillPerformedEvent extends OrderEvent {
	
	public static final String EVENT_NAME = "COBRANCA_REALIZADA";

	private static final long serialVersionUID = 7702500048926979660L;
	
	@JsonCreator
	public BillPerformedEvent(@JsonProperty("orderId") String orderId) {
		super(orderId);
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}
}
