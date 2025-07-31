package app.global.jwt;

import org.springframework.stereotype.Component;

import app.domain.customer.model.entity.User;

// NOTE: This is a placeholder for a real JWT implementation.
// A real implementation would use a library like jjwt, a secret key, and handle claims, signing, and parsing.
@Component
public class JwtTokenProvider {

	private final long accessTokenValidityInMilliseconds = 3600000; // 1 hour
	private final long refreshTokenValidityInMilliseconds = 1209600000; // 14 days

	public String createAccessToken(User user) {
		return "dummy-access-token-for-" + user.getUsername();
	}

	public String createRefreshToken(User user) {
		return "dummy-refresh-token-for-" + user.getUsername();
	}

	public long getRefreshTokenValidityInMilliseconds() {
		return refreshTokenValidityInMilliseconds;
	}
}