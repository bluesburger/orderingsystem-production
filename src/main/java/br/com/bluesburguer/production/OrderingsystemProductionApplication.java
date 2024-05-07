package br.com.bluesburguer.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** 
 * 
*/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableSqs
public class OrderingsystemProductionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderingsystemProductionApplication.class, args);
	}
}
