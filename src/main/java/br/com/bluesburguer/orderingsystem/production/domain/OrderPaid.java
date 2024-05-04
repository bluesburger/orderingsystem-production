package br.com.bluesburguer.orderingsystem.production.domain;

public class OrderPaid extends OrderEvent {

	private static final long serialVersionUID = 7702500048926979660L;

	@Override
	public String toString() {
		return "OrderPaid(" + super.getOrderId() + ")";
	}
}
