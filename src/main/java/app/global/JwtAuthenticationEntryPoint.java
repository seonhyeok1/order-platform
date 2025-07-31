package app.global;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 인증되지 않은 사용자의 접근 처리
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

		ApiResponse<Object> errorResponse = ApiResponse.onFailure(
			ErrorStatus._UNAUTHORIZED.getCode(),
			ErrorStatus._UNAUTHORIZED.getMessage(),
			null
		);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getWriter(), errorResponse);
	}
}

