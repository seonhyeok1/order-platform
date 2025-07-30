package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// Test 관련 에러로 JpaAuditingConfig로 분리
public class OrderPlatformBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderPlatformBackendApplication.class, args);
	}

}
