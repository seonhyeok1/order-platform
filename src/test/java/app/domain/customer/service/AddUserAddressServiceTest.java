// package app.domain.customer.service;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// import app.domain.customer.model.dto.request.AddUserAddressRequest;
// import app.domain.customer.UserAddressService;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import java.util.UUID;
//
// @SpringBootTest
// public class AddUserAddressServiceTest {
//
// 	@Autowired
// 	private UserAddressService useraddressService;
//
// 	@Test
// 	@DisplayName("성공")
// 	public void AddUserAddressTest() {
// 		// Given
// 		UUID userId = UUID.randomUUID(); // 테스트용 가짜 사용자 ID
// 		AddUserAddressRequest request = new AddUserAddressRequest(
// 			userId,
// 			"강의실",
// 			"서울 종로구 세종대로 175",
// 			"301호"
// 		);
//
// 		// When
// 		AddUserAddressRequest response = useraddressService.AddUserAddress(request);
//
// 		// Then
// 		assertNotNull(response);
// 		assertNotNull(response.getAddressId(), "주소 ID는 null이 아니어야 합니다");
// 		assertInstanceOf(UUID.class, response.getAddressId());
// 	}
//
// 	@Test
// 	@DisplayName("실패 - DB에러")
//
// }