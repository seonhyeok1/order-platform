// package app.domain.customer.service;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.*;
// import app.domain.customer.UserAddressService;
// import app.domain.customer.model.UserAddressRepository;
// import app.domain.customer.model.UserRepository;
// import app.domain.customer.model.dto.request.AddUserAddressRequest;
// import app.domain.customer.model.dto.response.AddUserAddressResponse;
// import app.domain.customer.model.entity.User;
// import app.domain.customer.model.entity.UserAddress;
// import app.domain.customer.model.entity.enums.UserRole;
// import app.global.apiPayload.code.status.ErrorStatus;
// import app.global.apiPayload.exception.GeneralException;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Nested;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.mockito.Mockito.*;
// import java.util.Optional;
// import java.util.UUID;
//
// @ExtendWith(MockitoExtension.class)
// class AddUserAddressServiceTest {
//
// 	@InjectMocks
// 	private UserAddressService userAddressService;
//
// 	@Mock
// 	private UserRepository userRepository;
//
// 	@Mock
// 	private UserAddressRepository userAddressRepository;
//
// 	private User testUser;
//
// 	@BeforeEach
// 	void setUp() {
// 		// 각 테스트 실행 전, 테스트에 사용할 공통 사용자 데이터를 Mocking합니다.
// 		testUser = User.builder()
// 			.id(1L) // Mocking을 위한 ID 설정
// 			.email("testuser@example.com")
// 			.password("password123")
// 			.name("테스트유저")
// 			.role(UserRole.CUSTOMER)
// 			.build();
//
// 		// userRepository.findById 호출 시 testUser 반환하도록 Mocking
// 		when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
//
// 		// userAddressRepository.save 호출 시 전달받은 UserAddress 객체 반환하도록 Mocking
// 		when(userAddressRepository.save(any(UserAddress.class)))
// 			.thenAnswer(invocation -> {
// 				UserAddress address = invocation.getArgument(0);
// 				// ID가 없으면 새로 생성 (JPA의 @GeneratedValue 역할)
// 				if (address.getAddressId() == null) {
// 					address = UserAddress.builder()
// 						.addressId(UUID.randomUUID())
// 						.user(address.getUser())
// 						.alias(address.getAlias())
// 						.address(address.getAddress())
// 						.addressDetail(address.getAddressDetail())
// 						.build();
// 				}
// 				return address;
// 			});
// 	}
//
// 	@Nested
// 	@DisplayName("주소 추가 성공 케이스")
// 	class SuccessCases {
//
// 		@Test
// 		@DisplayName("1. 모든 필드를 정상적으로 입력하여 주소를 등록합니다.")
// 		void addUserAddress_NormalCase() {
// 			// Given
// 			AddUserAddressRequest request = new AddUserAddressRequest(
// 				testUser.getUserId(),
// 				"우리집",
// 				"서울시 강남구 테헤란로 212",
// 				"1501호",
// 				true
// 			);
//
// 			// When
// 			AddUserAddressResponse response = userAddressService.addUserAddress(request);
//
// 			// Then
// 			assertNotNull(response);
// 			assertNotNull(response.address_id());
//
// 			// DB에 저장된 데이터를 직접 검증하여 신뢰도를 높입니다.
// 			UserAddress savedAddress = userAddressRepository.findById(response.address_id()).orElseThrow();
//
// 			assertThat(savedAddress.getUser().getUserId()).isEqualTo(testUser.getUserId());
// 			assertThat(savedAddress.getAlias()).isEqualTo("우리집");
// 			assertThat(savedAddress.getAddress()).isEqualTo("서울시 강남구 테헤란로 212");
// 			assertThat(savedAddress.getAddressDetail()).isEqualTo("1501호");
// 			assertThat(savedAddress.isDefault()).isTrue();
// 		}
// 		//
// 		// @Test
// 		// @DisplayName("2. 상세 주소(addressDetail) 없이 주소를 등록합니다.")
// 		// void addUserAddress_NoAddressDetail() {
// 		// 	// Given
// 		// 	AddUserAddressRequest request = new AddUserAddressRequest(
// 		// 		testUser.getUserId(),
// 		// 		"회사",
// 		// 		"서울시 구로구 디지털로 300",
// 		// 		null, // 상세 주소 null
// 		// 		false
// 		// 	);
// 		//
// 		// 	// When
// 		// 	AddUserAddressResponse response = userAddressService.addUserAddress(request);
// 		//
// 		// 	// Then
// 		// 	assertNotNull(response);
// 		// 	UserAddress savedAddress = userAddressRepository.findById(response.address_id()).orElseThrow();
// 		// 	assertThat(savedAddress.getAddressDetail()).isNull();
// 		// }
// 	}
//
// 	@Nested
// 	@DisplayName("주소 추가 실패 케이스")
// 	class FailureCases {
//
// 		@Test
// 		@DisplayName("1. 필수 값인 사용자 ID가 null일 경우 예외가 발생합니다.")
// 		void addUserAddress_Fail_NullUserID() {
// 			// Given
// 			AddUserAddressRequest request = new AddUserAddressRequest(
// 				null,
// 				"집",
// 				"서울시 강남구 테헤란로 212", // 필수 값 누락
// 				"101호",
// 				false
// 			);
//
// 			// When & Then
// 			assertThrows(
// 				org.springframework.dao.InvalidDataAccessApiUsageException.class,
// 				() -> userAddressService.addUserAddress(request)
// 			);
// 		}
//
// 		@Test
// 		@DisplayName("2. 필수 값인 별명(alias)가 null일 경우 예외가 발생합니다.")
// 		void addUserAddress_Fail_NullAlias() {
// 			// Given
// 			AddUserAddressRequest request = new AddUserAddressRequest(
// 				testUser.getUserId(),
// 				null,
// 				"서울시 강남구 테헤란로 212", // 필수 값 누락
// 				"101호",
// 				false
// 			);
//
// 			// When & Then
// 			assertThrows(
// 				DataIntegrityViolationException.class,
// 				() -> userAddressService.addUserAddress(request)
// 			);
// 		}
//
// 		@Test
// 		@DisplayName("3. 필수 값인 주소(address)가 null일 경우 예외가 발생합니다.")
// 		void addUserAddress_Fail_NullAddress() {
// 			// Given
// 			AddUserAddressRequest request = new AddUserAddressRequest(
// 				testUser.getUserId(),
// 				"집",
// 				null, // 필수 값 누락
// 				"101호",
// 				false
// 			);
//
// 			// When & Then
// 			assertThrows(
// 				DataIntegrityViolationException.class,
// 				() -> userAddressService.addUserAddress(request)
// 			);
// 		}
//
// 		@Test
// 		@DisplayName("4. 존재하지 않는 사용자 ID로 요청 시 예외가 발생합니다.")
// 		void addUserAddress_Fail_UserNotFound() {
// 			// Given
// 			long nonExistentUserId = 9999L;
// 			AddUserAddressRequest request = new AddUserAddressRequest(
// 				nonExistentUserId,
// 				"우리집",
// 				"서울시 강남구 테헤란로 212",
// 				"1501호",
// 				false
// 			);
//
// 			// When & Then
// 			GeneralException exception = assertThrows(
// 				GeneralException.class,
// 				() -> userAddressService.addUserAddress(request)
// 			);
//
// 			assertThat(exception.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
// 		}
//
// 		// @Test
// 		// @DisplayName("2. 데이터베이스 저장 실패 시 예외가 발생합니다.")
// 		// void addUserAddress_Fails_WhenDatabaseSaveFails() {
// 		// 	// Given
// 		// 	AddUserAddressRequest request = new AddUserAddressRequest(
// 		// 		testUser.getUserId(),
// 		// 		"우리집",
// 		// 		"서울시 강남구 테헤란로 212",
// 		// 		"1501호",
// 		// 		true
// 		// 	);
// 		//
// 		// 	// 2. userRepository.findById가 호출되면, 정상적인 User 객체를 반환하도록 설정합니다.
// 		// 	User testUser = User.builder().userId(1L).build();
// 		// 	when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
// 		//
// 		// 	// 3. userAddressRepository.save가 어떤 UserAddress 객체로든 호출될 때,
// 		// 	//    DB 연결 실패를 가정한 DataAccessResourceFailureException 예외를 던지도록 설정합니다.
// 		// 	doThrow(new DataAccessResourceFailureException("DB connection failed"))
// 		// 		.when(userAddressRepository).save(any(UserAddress.class));
// 		//
// 		// 	// When & Then: 실제 서비스 메서드를 호출하고, 기대하는 예외가 발생하는지 검증합니다.
// 		// 	assertThrows(
// 		// 		DataAccessResourceFailureException.class,
// 		// 		() -> userAddressService.addUserAddress(request)
// 		// 	);
// 		//
// 		// 	// Verify: 특정 메서드가 예상대로 호출되었는지 확인하여 테스트의 신뢰도를 높입니다.
// 		// 	verify(userRepository, times(1)).findById(1L);
// 		// 	verify(userAddressRepository, times(1)).save(any(UserAddress.class));
// 		// }
// 	}
// }