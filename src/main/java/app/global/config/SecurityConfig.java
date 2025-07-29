package app.global.config;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	// userService/CreateUser 비밀번호 암호화
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// swagger 및 기타 public 경로 자격증명 생략
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// 1. CORS 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// 2. CSRF 비활성화 (Stateless 서버에서는 불필요)
			.csrf(AbstractHttpConfigurer::disable)

			// 3. 세션 관리 정책을 STATELESS로 설정 (토큰 기반 인증)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// 4. HTTP 요청에 대한 인가 규칙 설정
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					// Swagger 허용 URL
					"/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**", "/swagger-resources",
					"/swagger-resources/**", "/configuration/ui", "/configuration/security", "/swagger-ui/**",
					"/webjars/**", "/swagger-ui.html",
					"/api/auth/signup", "/api/auth/login"
				).permitAll() // 위에 명시된 경로는 인증 없이 접근 허용
				.anyRequest().authenticated() // 나머지 모든 경로는 인증 필요
			);

		return http.build();
	}

	// CORS 설정을 위한 Bean TODO: 실제 운영 환경에서는 구체적인 도메인 명시 필요
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// 허용할 오리진(출처) 설정
		configuration.setAllowedOrigins(Collections.singletonList("*")); // 모든 출처 허용 (개발용)
		// 허용할 HTTP 메서드 설정
		configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
		// 허용할 HTTP 헤더 설정
		configuration.addAllowedHeader("*"); // 모든 헤더 허용
		// 자격 증명(쿠키 등) 허용 여부
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
		return source;
	}
}