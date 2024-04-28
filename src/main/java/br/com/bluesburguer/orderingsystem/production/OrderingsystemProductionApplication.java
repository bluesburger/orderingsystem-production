package br.com.bluesburguer.orderingsystem.production;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.stereotype.Controller;

/** 
 * 
*/
@SpringBootApplication
@Controller
@EnableDiscoveryClient
public class OrderingsystemProductionApplication {

	public static void main(String[] args) {
		// SpringApplication.run(OrderingsystemProductionApplication.class, args);
		new SpringApplicationBuilder(OrderingsystemProductionApplication.class)
			// .web(WebApplicationType.SERVLET)
			.run(args);
	}

}
