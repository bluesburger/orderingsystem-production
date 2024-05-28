package br.com.bluesburguer.production.application.dto.pedido;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import br.com.bluesburguer.production.application.dto.OrderEventDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PedidoRegistradoDto extends OrderEventDto {
	
	public static final String EVENT_NAME = "PEDIDO_REGISTRADO";

	private static final long serialVersionUID = 7702500048926979660L;
	
	@JsonCreator
	public PedidoRegistradoDto(@JsonProperty("orderId") String orderId) {
		super(orderId);
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}
}