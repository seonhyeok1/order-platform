package app.domain.owner.repository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import app.domain.menu.model.entity.Category;
import app.domain.menu.model.entity.CategoryRepository;
import app.domain.owner.model.entity.Region;
import app.domain.owner.model.entity.RegionRepository;
import app.domain.owner.model.entity.Store;
import app.domain.owner.model.entity.StoreRepository;
import app.domain.user.model.UserRepository;
import app.domain.user.model.entity.User;
import app.domain.user.model.entity.enums.UserRole;

@DataJpaTest
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
class StoreRepositoryTest {
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	@DisplayName("Success: userId - store조회 ")
	void findByUserIdSuccess() {

		Region region = Region.builder()
			.regionCode("1114010200")
			.regionName("서울 광화문")
			.fullName("서울특별시 종로구 세종로")
			.sido("서울특별시")
			.sigungu("종로구")
			.eupmyendong("세종로")
			.build();
		region = regionRepository.save(region);

		User user = User.builder()
			.username("testuser")
			.email("test@example.com")
			.password("password")
			.nickname("testnick")
			.phoneNumber("01012345678")
			.role(UserRole.OWNER)
			.build();
		user = userRepository.save(user);

		Category category = Category.builder()
			.categoryName("한식")
			.build();
		category = categoryRepository.save(category);

		Store store = Store.builder()
			.user(user)
			.region(region)
			.category(category)
			.storeName("테스트 가게")
			.address("서울시 강남구")
			.build();
		storeRepository.save(store);

		Store foundStore = storeRepository.findByUser_UserId(user.getUserId()).orElse(null);

		assert foundStore != null;
		assert foundStore.getUser().getUserId().equals(user.getUserId());
	}

	@Test
	@DisplayName("Success: 선택적 필드 null")
	void saveStoreWithNullOptionalFieldsSuccess() {
		Region region = Region.builder()
			.regionCode("1114010200")
			.regionName("서울 광화문")
			.fullName("서울특별시 종로구 세종로")
			.sido("서울특별시")
			.sigungu("종로구")
			.eupmyendong("세종로")
			.build();
		region = regionRepository.save(region);

		User user = User.builder()
			.username("testuser_null")
			.email("test_null@example.com")
			.password("password_null")
			.nickname("testnick_null")
			.phoneNumber("01098765432")
			.role(UserRole.OWNER)
			.build();
		user = userRepository.save(user);

		Category category = Category.builder()
			.categoryName("양식")
			.build();
		category = categoryRepository.save(category);

		// description과 phoneNumber를 설정하지 않은 Store 생성
		Store store = Store.builder()
			.user(user)
			.region(region)
			.category(category)
			.storeName("선택적 필드 null 테스트 가게")
			.address("서울시 강남구 테헤란로")
			.build();
		storeRepository.save(store);

		Store foundStore = storeRepository.findByUser_UserId(user.getUserId()).orElse(null);

		assert foundStore != null;
		assert foundStore.getDescription() == null;
		assert foundStore.getPhoneNumber() == null;
	}

	@Test
	@DisplayName("Fail: UserId")
	void findByUserIdNotFound() {
		Long nonExistentUserId = 9999L; // 실제 DB에 존재하지 않을 것으로 예상되는 ID
		Optional<Store> foundStore = storeRepository.findByUser_UserId(nonExistentUserId);
		// 결과 Optional.empty() 검증
		assert foundStore.isEmpty();
	}

	@Test
	@DisplayName("Fail: StoreName And Region")
	void existsByStoreNameAndRegionNotFound() {
		// findByUser_UserIdSuccess - 다시 생성 (독립적인 테스트 위해)
		Region region = Region.builder()
			.regionCode("9999999999")
			.regionName("테스트 지역")
			.fullName("테스트 지역 전체 이름")
			.sido("테스트 시도")
			.sigungu("테스트 시군구")
			.eupmyendong("테스트 읍면동")
			.build();
		region = regionRepository.save(region);

		boolean exists = storeRepository.existsByStoreNameAndRegion("존재하지 않는 가게", region);
		//결과 false 검증
		assert !exists;
	}
}