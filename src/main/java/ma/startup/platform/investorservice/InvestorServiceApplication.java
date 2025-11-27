package ma.startup.platform.investorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients

public class InvestorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestorServiceApplication.class, args);
	}

}
