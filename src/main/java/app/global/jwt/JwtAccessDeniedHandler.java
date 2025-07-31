package app.global.jwt;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.global.apiPayload.ApiResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 필요한 권한 없이 접근 시 처리
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);

		ApiResponse<Object> errorResponse = ApiResponse.onFailure(
			ErrorStatus._FORBIDDEN.getCode(),
			ErrorStatus._FORBIDDEN.getMessage(),
			null
		);

		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(response.getWriter(), errorResponse);
	}
}
