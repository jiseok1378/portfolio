package dev.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// 키가 비어 있어도 서버는 떠야 한다(시스템 속성은 환경 변수와 yml 보다 우선)
		GeminiKeyDefaults.forEnv(System.getenv()).forEach(System::setProperty);
		SpringApplication.run(BackendApplication.class, args);
	}

}
