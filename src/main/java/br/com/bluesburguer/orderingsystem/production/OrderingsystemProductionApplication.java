package br.com.bluesburguer.orderingsystem.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/** 
 * 
*/
@SpringBootApplication
@EnableDiscoveryClient
public class OrderingsystemProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderingsystemProductionApplication.class, args);
	}

}
