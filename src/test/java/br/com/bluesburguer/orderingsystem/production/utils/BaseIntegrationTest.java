package br.com.bluesburguer.orderingsystem.production.utils;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles({ "test" })
public class BaseIntegrationTest {

}
