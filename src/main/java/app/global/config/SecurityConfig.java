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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import app.domain.user.model.entity.enums.UserRole;
import app.global.jwt.JwtAccessDeniedHandler;
import app.global.jwt.JwtAuthenticationEntryPoint;
import app.global.jwt.JwtAuthenticationFilter;
import app.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	// userService/CreateUser 비밀번호 암호화
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// swagger 및 기타 public 경로 자격증명 생략
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CORS 설정
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// CSRF 비활성화
			.csrf(AbstractHttpConfigurer::disable)

			// 예외 처리 핸들러 등록
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)

			// 세션 관리 정책을 STATELESS로 설정 (토큰 기반 인증)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// HTTP 요청에 대한 인가 규칙
			.authorizeHttpRequests(auth -> auth
				// 1. 인증 없이 접근 허용
				.requestMatchers(
					// Swagger 허용 URL
					"/v2/api-docs", "/v3/api-docs", "/v3/api-docs/**", "/swagger-resources",
					"/swagger-resources/**", "/configuration/ui", "/configuration/security", "/swagger-ui/**",
					"/webjars/**", "/swagger-ui.html",
					"/user/signup", "/user/login"
				)
				.permitAll() // 위에 명시된 경로는 인증 없이 접근 허용

				// 2. 권한에 따른 접근 제한
				.requestMatchers("/api/store/**")
				.hasRole(UserRole.OWNER.name())
				.requestMatchers("/api/customer/**")
				.hasRole(UserRole.CUSTOMER.name())
				.requestMatchers("/api/cart/**")
				.hasRole(UserRole.CUSTOMER.name())
				.requestMatchers("/api/admin/**")
				.hasRole(UserRole.MANAGER.name())
				.requestMatchers("/api/master/**")
				.hasRole(UserRole.MASTER.name())

				// 3. 나머지 모든 요청은 인증된 사용자만 접근 가능
				.anyRequest()
				.authenticated()
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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