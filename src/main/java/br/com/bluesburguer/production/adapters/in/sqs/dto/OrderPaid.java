package br.com.bluesburguer.production.adapters.in.sqs.dto;

public class OrderPaid extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@Override
	public String toString() {
		return "OrderPaid(" + super.getOrderId() + ")";
	}
}
