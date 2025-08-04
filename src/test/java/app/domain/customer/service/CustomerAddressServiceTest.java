package app.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import app.domain.customer.CustomerAddressService;
import app.domain.customer.dto.response.GetCustomerAddressListResponse;
import app.domain.user.model.UserAddressRepository;
import app.domain.user.model.UserRepository;
import app.domain.customer.dto.request.AddCustomerAddressRequest;
import app.domain.customer.dto.response.AddCustomerAddressResponse;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.UserAddress;
import app.domain.user.model.entity.enums.UserRole;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CustomerAddressServiceTest {

	@InjectMocks
	private CustomerAddressService customerAddressService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserAddressRepository userAddressRepository;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.userId(1L)
			.username("tester")
			.email("testuser@example.com")
			.password("password123")
			.nickname("test_nickname")
			.realName("tester_name")
			.phoneNumber("01012345678")
			.userRole(UserRole.CUSTOMER)
			.build();
	}

	private List<UserAddress> createMockAddressList(User user) {
		UserAddress address1 = UserAddress.builder()
			.addressId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
			.user(user)
			.alias("우리집")
			.address("서울시 강남구")
			.addressDetail("101호")
			.isDefault(true)
			.build();
		UserAddress address2 = UserAddress.builder()
			.addressId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
			.user(user)
			.alias("회사")
			.address("서울시 서초구")
			.addressDetail("202호")
			.isDefault(false)
			.build();
		return List.of(address1, address2);
	}


	@Nested
	@DisplayName("주소 추가 성공 케이스")
	class addAddressSuccessCases {

		@Test
		@DisplayName("1. isDefault = true & 기존 존재 없음")
		void addUserAddress_isDefault_true_NormalCase() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				
				"우리집",
				"서울시 강남구 테헤란로 212",
				"1501호",
				true
			);
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.save(any(UserAddress.class)))
				.thenAnswer(invocation -> {
					UserAddress useraddress = invocation.getArgument(0);
					if (useraddress.getAddressId() == null) {
						return UserAddress.builder()
							.addressId(UUID.randomUUID())
							.user(useraddress.getUser())
							.alias(useraddress.getAlias())
							.address(useraddress.getAddress())
							.addressDetail(useraddress.getAddressDetail())
							.isDefault(useraddress.isDefault())
							.build();
					}
					return useraddress;
				});

			// When
			AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(testUser.getUserId(), request);

			// Then
			assertNotNull(response);
			assertNotNull(response.address_id());

			// DB에 저장된 데이터 검증
			ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).save(captor.capture());
			UserAddress savedArg = captor.getValue();

			assertThat(savedArg.getUser().getUserId()).isEqualTo(testUser.getUserId());
			assertThat(savedArg.getAlias()).isEqualTo("우리집");
			assertThat(savedArg.getAddress()).isEqualTo("서울시 강남구 테헤란로 212");
			assertThat(savedArg.getAddressDetail()).isEqualTo("1501호");
			assertThat(savedArg.isDefault()).isTrue();
		}

		@Test
		@DisplayName("2. isDefault = false & 기존 존재 없음")
		void addUserAddress_isDefault_false_NormalCase() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				
				"우리집",
				"서울시 강남구 테헤란로 212",
				"1501호",
				false
			);
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.save(any(UserAddress.class)))
				.thenAnswer(invocation -> {
					UserAddress useraddress = invocation.getArgument(0);
					if (useraddress.getAddressId() == null) {
						return UserAddress.builder()
							.addressId(UUID.randomUUID())
							.user(useraddress.getUser())
							.alias(useraddress.getAlias())
							.address(useraddress.getAddress())
							.addressDetail(useraddress.getAddressDetail())
							.isDefault(useraddress.isDefault())
							.build();
					}
					return useraddress;
				});

			// When
			AddCustomerAddressResponse response = customerAddressService.addCustomerAddress(testUser.getUserId(), request);

			// Then
			assertNotNull(response);
			assertNotNull(response.address_id());

			// DB에 저장된 데이터 검증
			ArgumentCaptor<UserAddress> captor = ArgumentCaptor.forClass(UserAddress.class);
			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).save(captor.capture());
			UserAddress savedArg = captor.getValue();

			assertThat(savedArg.getUser().getUserId()).isEqualTo(testUser.getUserId());
			assertThat(savedArg.getAlias()).isEqualTo("우리집");
			assertThat(savedArg.getAddress()).isEqualTo("서울시 강남구 테헤란로 212");
			assertThat(savedArg.getAddressDetail()).isEqualTo("1501호");
			assertThat(savedArg.isDefault()).isTrue();
		}

		@Test
		@DisplayName("3. isDefault=true & 기존 기본 존재 → 기존 기본 해제 후 신규 기본 설정")
		void addUserAddress_SetNewDefault_DemotesPreviousDefault() {
			// Given: 새로운 주소를 기본으로 설정하려고 하고, 기존 기본 주소가 이미 존재함
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				"새로운 우리집",
				"서울시 서초구",
				"202호",
				true
			);

			UserAddress previousDefaultAddress = UserAddress.builder()
				.addressId(UUID.randomUUID())
				.user(testUser)
				.alias("옛날 집")
				.address("서울시 강남구")
				.addressDetail("101동 101호")
				.isDefault(true)
				.build();

			// 기존 기본 주소 조회 Stub
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.findByUser_UserIdAndIsDefaultTrue(testUser.getUserId()))
				.thenReturn(Optional.of(previousDefaultAddress));

			// save 메서드는 전달된 객체를 그대로 반환하도록 설정 (ID 생성 흉내)
			when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> {
				UserAddress savedAddress = invocation.getArgument(0);
				if (savedAddress.getAddressId() == null) {
					return UserAddress.builder()
						.addressId(UUID.randomUUID())
						.user(savedAddress.getUser())
						.alias(savedAddress.getAlias())
						.address(savedAddress.getAddress())
						.addressDetail(savedAddress.getAddressDetail())
						.isDefault(savedAddress.isDefault())
						.build();
				}
				return savedAddress; // 기존 주소 업데이트 시에는 그대로 반환
			});


			// When
			customerAddressService.addCustomerAddress(testUser.getUserId(), request);


			// Then: save가 2번 호출되었는지, 그리고 각 호출의 내용이 올바른지 검증
			ArgumentCaptor<UserAddress> addressCaptor = ArgumentCaptor.forClass(UserAddress.class);
			verify(userAddressRepository, times(2)).save(addressCaptor.capture());
			List<UserAddress> savedAddresses = addressCaptor.getAllValues();

			// 첫 번째 save 호출: 기존 기본 주소의 isDefault가 false로 변경되었는지 확인
			UserAddress demotedAddress = savedAddresses.get(0);
			assertThat(demotedAddress.getAddressId()).isEqualTo(previousDefaultAddress.getAddressId());
			assertThat(demotedAddress.isDefault()).isFalse();

			// 두 번째 save 호출: 새로 추가된 주소의 정보가 올바르고 isDefault가 true 로 설정되었는지 확인
			UserAddress newDefaultAddress = savedAddresses.get(1);
			assertThat(newDefaultAddress.getUser().getUserId()).isEqualTo(testUser.getUserId()); // This is the corrected line
			assertThat(newDefaultAddress.getAlias()).isEqualTo("새로운 우리집");
			assertThat(newDefaultAddress.getAddress()).isEqualTo("서울시 서초구");
			assertThat(newDefaultAddress.getAddressDetail()).isEqualTo("202호");
			assertThat(newDefaultAddress.isDefault()).isTrue();
		}

		@Test
		@DisplayName("4. isDefault=false & 기존 기본 존재 → 기존 기본 유지, 신규 false 저장")
		void addUserAddress_AddNonDefault_KeepsPreviousDefault() {
			// Given: 새로운 주소를 기본으로 설정하려고 하고, 기존 기본 주소가 이미 존재함
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				"회사",
				"서울시 서초구",
				"202호",
				false
			);

			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.findAllByUserUserId(testUser.getUserId()))
				.thenReturn(List.of(UserAddress.builder().build()));

			// save 메서드는 전달된 객체를 그대로 반환하도록 설정 (ID 생성 흉내)
			when(userAddressRepository.save(any(UserAddress.class))).thenAnswer(invocation -> {
				UserAddress savedAddress = invocation.getArgument(0);
				return UserAddress.builder()
					.addressId(UUID.randomUUID())
					.user(savedAddress.getUser())
					.alias(savedAddress.getAlias())
					.address(savedAddress.getAddress())
					.addressDetail(savedAddress.getAddressDetail())
					.isDefault(savedAddress.isDefault())
					.build();
			});

			// When
			customerAddressService.addCustomerAddress(testUser.getUserId(), request);

			// Then: save가 정확히 1번만 호출되었는지 검증
			ArgumentCaptor<UserAddress> addressCaptor = ArgumentCaptor.forClass(UserAddress.class);
			verify(userAddressRepository, times(1)).save(addressCaptor.capture());

			UserAddress savedAddress = addressCaptor.getValue();
			assertThat(savedAddress.getUser().getUserId()).isEqualTo(testUser.getUserId()); // This is the corrected line
			assertThat(savedAddress.getAlias()).isEqualTo("회사");
			assertThat(savedAddress.getAddress()).isEqualTo("서울시 서초구");
			assertThat(savedAddress.getAddressDetail()).isEqualTo("202호");
			assertThat(savedAddress.isDefault()).isFalse();

			verify(userAddressRepository, never()).findByUser_UserIdAndIsDefaultTrue(anyLong());
		}
	}

	@Nested
	@DisplayName("주소 추가 실패 케이스")
	class addAddressFailureCases {

		@Test
		@DisplayName("1. 존재하지 않는 사용자 ID로 요청 시")
		void addUserAddress_Fail_UserNotFound() {
			// Given
			long nonExistentUserId = 9999L;
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				"우리집",
				"서울시 강남구 테헤란로 212",
				"1501호",
				false
			);
			when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addCustomerAddress(nonExistentUserId, request)
			);
			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);

			verify(userRepository, times(1)).findById(nonExistentUserId);
			verify(userAddressRepository, never()).save(any(UserAddress.class));
		}

		//			@Test
		//			@DisplayName("10. 동일한 주소(주소+상세주소)를 중복 등록 시 예외 발생")
		//			void addUserAddress_Fail_WhenAddressIsDuplicate() {
		//				// Given: 중복 등록을 시도하는 주소 정보
		//				AddCustomerAddressRequest request = new AddCustomerAddressRequest(
		//						
		//						"우리집",
		//						"서울시 강남구 테헤란로 212",
		//						"1501호",
		//						false
		//				);
		//
		//				// 사용자는 존재함
		//				when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
		//
		//				// 핵심: 레포지토리에게 "해당 주소는 이미 존재한다(true)"고 응답하도록 설정
		//				when(userAddressRepository.existsByUserAndAddressAndAddressDetail(
		//						testUser,
		//						request.address(),
		//						request.addressDetail()
		//				)).thenReturn(true);
		//
		//				// When & Then: ADDRESS_ALREADY_EXISTS 예외가 발생하는지 확인
		//				GeneralException ex = assertThrows(GeneralException.class,
		//						() -> customerAddressService.addCustomerAddress(request)
		//				);
		//
		//				assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ADDRESS_ALREADY_EXISTS);
		//
		//				// 중복이므로, 주소 개수 확인이나 save 로직은 절대 호출되지 않았어야 함
		//				verify(userAddressRepository, never()).countByUser(any());
		//				verify(userAddressRepository, never()).save(any(UserAddress.class));
		//			}
		//		}
	}

	@Nested
	@DisplayName("주소 추가 예외 케이스")
	class addAddressExceptionCases {

		@Test
		@DisplayName("1. DB 저장 실패 시 _INTERNAL_SERVER_ERROR 예외 발생")
		void addUserAddress_Fail_DbSaveError() {
			// Given
			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				"우리집",
				"서울시",
				"101동",
				true
			);

			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

			when(userAddressRepository.save(any(UserAddress.class)))
				.thenThrow(new DataAccessResourceFailureException("Simulated DB error"));

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addCustomerAddress(testUser.getUserId(), request)
			);

			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ADDRESS_ADD_FAILED);

			// Verify interactions
			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).save(any(UserAddress.class));
		}

		@Test
		@DisplayName("2. UUID 생성 실패 시 _INTERNAL_SERVER_ERROR 예외 발생")
		void addUserAddress_Fail_UuidNotAssigned() {
			// Given
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.save(any(UserAddress.class)))
				.thenAnswer(inv -> {
					UserAddress ua = inv.getArgument(0);
					// addressId 를 의도적으로 null 로 반환
					return UserAddress.builder()
						.addressId(null)
						.user(ua.getUser()).alias(ua.getAlias())
						.address(ua.getAddress()).addressDetail(ua.getAddressDetail())
						.isDefault(ua.isDefault())
						.build();
				});

			AddCustomerAddressRequest request = new AddCustomerAddressRequest(
				
				"우리집",
				"서울시",
				"101동",
				true
			);

			// When & Then
			GeneralException ex = assertThrows(
				GeneralException.class,
				() -> customerAddressService.addCustomerAddress(testUser.getUserId(), request)
			);
			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ADDRESS_ADD_FAILED);
			verify(userAddressRepository, times(1)).save(any(UserAddress.class));
		}
	}



	// --- getCustomerAddresses 테스트 시작 ---

	@Nested
	@DisplayName("주소 목록 조회 성공 케이스")
	class getAddressSuccessCases {

		@Test
		@DisplayName("1. 사용자의 주소 목록 1건 정상적으로 조회")
		void getCustomerAddresses_Success_ReturnsSingleDto() {
			// Given: 사용자가 1개의 주소를 가지고 있는 상황
			UserAddress singleAddress = UserAddress.builder()
				.addressId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
				.user(testUser)
				.alias("우리집")
				.address("서울시 강남구")
				.addressDetail("101호")
				.isDefault(true)
				.build();
			List<UserAddress> mockAddressList = List.of(singleAddress);

			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId())).thenReturn(mockAddressList);

			// When
			List<GetCustomerAddressListResponse> result = customerAddressService.getCustomerAddresses(testUser.getUserId());

			// Then
			assertThat(result).isNotNull();
			assertThat(result.size()).isEqualTo(1);

			GetCustomerAddressListResponse result1 = result.get(0);
			assertThat(result1.alias()).isEqualTo(singleAddress.getAlias());
			assertThat(result1.address()).isEqualTo(singleAddress.getAddress());
			assertThat(result1.addressDetail()).isEqualTo(singleAddress.getAddressDetail());
			assertThat(result1.isDefault()).isEqualTo(singleAddress.isDefault());

			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).findAllByUserUserId(testUser.getUserId());
		}

		@Test
		@DisplayName("2. 사용자의 주소 목록 여러개 정상적으로 조회")
		void getCustomerAddresses_Success_ReturnsDtoList() {
			List<UserAddress> mockAddressList = createMockAddressList(testUser);
			UserAddress address1 = mockAddressList.get(0);
			UserAddress address2 = mockAddressList.get(1);

			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId())).thenReturn(mockAddressList);

			// When
			List<GetCustomerAddressListResponse> result = customerAddressService.getCustomerAddresses(testUser.getUserId());

			// Then
			assertThat(result).isNotNull();
			assertThat(result.size()).isEqualTo(2);

			GetCustomerAddressListResponse result1 = result.get(0);
			assertThat(result1.alias()).isEqualTo(address1.getAlias());
			assertThat(result1.address()).isEqualTo(address1.getAddress());
			assertThat(result1.addressDetail()).isEqualTo(address1.getAddressDetail());
			assertThat(result1.isDefault()).isEqualTo(address1.isDefault());

			GetCustomerAddressListResponse result2 = result.get(1);
			assertThat(result2.alias()).isEqualTo(address2.getAlias());
			assertThat(result2.address()).isEqualTo(address2.getAddress());
			assertThat(result2.addressDetail()).isEqualTo(address2.getAddressDetail());
			assertThat(result2.isDefault()).isEqualTo(address2.isDefault());

			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).findAllByUserUserId(testUser.getUserId());
		}

		@Test
		@DisplayName("3. 주소가 없는 사용자의 경우 빈 리스트를 반환함")
		void getCustomerAddresses_Success_WhenNoAddressesExist_ReturnsEmptyList() {
			// Given
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId())).thenReturn(List.of()); // 빈 리스트 반환

			// When
			List<GetCustomerAddressListResponse> result = customerAddressService.getCustomerAddresses(testUser.getUserId());

			// Then
			assertThat(result).isNotNull();
			assertThat(result).isEmpty();

			verify(userRepository, times(1)).findById(testUser.getUserId());
			verify(userAddressRepository, times(1)).findAllByUserUserId(testUser.getUserId());
		}
	}

	@Nested
	@DisplayName("주소 목록 조회 실패 케이스")
	class getAddressFailureCases {

		@Test
		@DisplayName("1. 존재하지 않는 사용자로 조회 시 USER_NOT_FOUND 예외 발생")
		void getCustomerAddresses_Fail_UserNotFound() {
			// Given
			long nonExistentUserId = 9999L;
			when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

			// When & Then
			GeneralException ex = assertThrows(GeneralException.class,
				() -> customerAddressService.getCustomerAddresses(nonExistentUserId));

			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
			verify(userAddressRepository, never()).findAllByUserUserId(anyLong());
		}
	}

	@Nested
	@DisplayName("주소 목록 조회 예외 케이스")
	class getAddressExceptionCases {

		@Test
		@DisplayName("1. DB 조회 중 오류 발생 시 _INTERNAL_SERVER_ERROR 예외 발생")
		void getCustomerAddresses_Exception_DbError() {
			// Given
			when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
			when(userAddressRepository.findAllByUserUserId(testUser.getUserId()))
				.thenThrow(new DataAccessResourceFailureException("Simulated DB Connection Failure"));

			// When & Then
			GeneralException ex = assertThrows(GeneralException.class,
				() -> customerAddressService.getCustomerAddresses(testUser.getUserId()));

			assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ADDRESS_READ_FAILED);
		}
	}
}