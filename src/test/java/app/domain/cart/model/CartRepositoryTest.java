package app.domain.cart.model;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import app.domain.cart.model.entity.Cart;
import app.domain.customer.model.entity.User;
import app.domain.customer.model.entity.enums.UserRole;
import app.domain.owner.model.entity.Store;

@DataJpaTest
class CartRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CartRepository cartRepository;

	private User testUser;
	private Store testStore;
	private Cart testCart;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.username("testuser")
			.email("test@example.com")
			.password("password123")
			.nickname("테스트유저")
			.phoneNumber("010-1234-5678")
			.userRole(UserRole.CUSTOMER)
			.build();
		entityManager.persistAndFlush(testUser);

		testStore = Store.builder()
			.user(testUser)
			.storeName("테스트매장")
			.address("서울시 강남구")
			.build();
		entityManager.persistAndFlush(testStore);

		testCart = Cart.builder()
			.user(testUser)
			.store(testStore)
			.build();
		entityManager.persistAndFlush(testCart);
	}

	@Test
	@DisplayName("사용자 ID로 장바구니 조회 - 성공")
	void findByUser_UserId_Success() {
		Optional<Cart> result = cartRepository.findByUser_UserId(testUser.getUserId());

		assertThat(result).isPresent();
		assertThat(result.get().getCartId()).isEqualTo(testCart.getCartId());
		assertThat(result.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
	}

	@Test
	@DisplayName("사용자 ID로 장바구니 조회 - 존재하지 않는 사용자")
	void findByUser_UserId_NotFound() {
		Long nonExistentUserId = 999L;

		Optional<Cart> result = cartRepository.findByUser_UserId(nonExistentUserId);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("장바구니 저장 및 조회")
	void saveAndFind() {
		User newUser = User.builder()
			.username("newuser")
			.email("new@example.com")
			.password("password123")
			.nickname("새유저")
			.phoneNumber("010-9876-5432")
			.userRole(UserRole.CUSTOMER)
			.build();
		entityManager.persistAndFlush(newUser);

		Store newStore = Store.builder()
			.user(newUser)
			.storeName("새매장")
			.address("서울시 서초구")
			.build();
		entityManager.persistAndFlush(newStore);

		Cart newCart = Cart.builder()
			.user(newUser)
			.store(newStore)
			.build();

		Cart savedCart = cartRepository.save(newCart);
		entityManager.flush();
		entityManager.clear();

		Optional<Cart> foundCart = cartRepository.findById(savedCart.getCartId());

		assertThat(foundCart).isPresent();
		assertThat(foundCart.get().getCartId()).isEqualTo(savedCart.getCartId());
		assertThat(foundCart.get().getUser().getUserId()).isEqualTo(newUser.getUserId());
	}

	@Test
	@DisplayName("장바구니 삭제")
	void deleteCart() {
		UUID cartId = testCart.getCartId();

		cartRepository.delete(testCart);
		entityManager.flush();

		Optional<Cart> result = cartRepository.findById(cartId);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("영속성 컨텍스트 - 동일한 엔티티 반환")
	void persistenceContext_SameEntity() {
		Cart cart1 = cartRepository.findByUser_UserId(testUser.getUserId()).orElse(null);
		Cart cart2 = cartRepository.findByUser_UserId(testUser.getUserId()).orElse(null);

		assertThat(cart1).isSameAs(cart2);
	}

	@Test
	@DisplayName("존재하는 모든 장바구니 조회")
	void findAll() {
		User anotherUser = User.builder()
			.username("anotheruser")
			.email("another@example.com")
			.password("password123")
			.nickname("다른유저")
			.phoneNumber("010-1111-2222")
			.userRole(UserRole.CUSTOMER)
			.build();
		entityManager.persistAndFlush(anotherUser);

		Store anotherStore = Store.builder()
			.user(anotherUser)
			.storeName("다른매장")
			.address("서울시 마포구")
			.build();
		entityManager.persistAndFlush(anotherStore);

		Cart anotherCart = Cart.builder()
			.user(anotherUser)
			.store(anotherStore)
			.build();
		entityManager.persistAndFlush(anotherCart);

		var allCarts = cartRepository.findAll();

		assertThat(allCarts).hasSize(2);
		assertThat(allCarts).extracting(Cart::getCartId)
			.containsExactlyInAnyOrder(testCart.getCartId(), anotherCart.getCartId());
	}
}