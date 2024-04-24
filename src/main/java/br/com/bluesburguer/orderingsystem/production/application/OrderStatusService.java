package br.com.bluesburguer.orderingsystem.production.application;

import org.springframework.stereotype.Service;

import br.com.bluesburguer.orderingsystem.production.domain.OrderStatusUpdated;

@Service
public class OrderStatusService {

	public boolean update(OrderStatusUpdated orderStatus) {
		// TODO: consumir feignClient do product
		return true;
	}
}
