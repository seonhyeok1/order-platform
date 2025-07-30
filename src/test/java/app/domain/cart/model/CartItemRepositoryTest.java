package app.domain.cart.model;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import app.domain.cart.model.entity.Cart;
import app.domain.cart.model.entity.CartItem;
import app.domain.menu.model.entity.Menu;
import app.domain.store.model.entity.Store;
import app.domain.user.model.entity.User;

@DataJpaTest
class CartItemRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private CartItemRepository cartItemRepository;

	private User testUser;
	private Cart testCart;
	private Store testStore;
	private Menu testMenu1;
	private Menu testMenu2;
	private CartItem testCartItem1;
	private CartItem testCartItem2;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.userId(1L)
			.email("test@example.com")
			// .name("테스트유저")
			.build();
		entityManager.persistAndFlush(testUser);

		testCart = Cart.builder()
			.cartId(UUID.randomUUID())
			.user(testUser)
			.build();
		entityManager.persistAndFlush(testCart);

		testStore = Store.builder()
			.storeId(UUID.randomUUID())
			.storeName("테스트매장")
			.build();
		entityManager.persistAndFlush(testStore);

		testMenu1 = Menu.builder()
			.menuId(UUID.randomUUID())
			// .menuName("메뉴1")
			.price(10000)
			.store(testStore)
			.build();
		entityManager.persistAndFlush(testMenu1);

		testMenu2 = Menu.builder()
			.menuId(UUID.randomUUID())
			.name("메뉴2")
			.price(15000)
			.store(testStore)
			.build();
		entityManager.persistAndFlush(testMenu2);

		testCartItem1 = CartItem.builder()
			.cartItemId(UUID.randomUUID())
			.cart(testCart)
			.menu(testMenu1)
			.quantity(2)
			.build();
		entityManager.persistAndFlush(testCartItem1);

		testCartItem2 = CartItem.builder()
			.cartItemId(UUID.randomUUID())
			.cart(testCart)
			.menu(testMenu2)
			.quantity(1)
			.build();
		entityManager.persistAndFlush(testCartItem2);
	}

	@Test
	@DisplayName("장바구니 ID로 장바구니 아이템 조회 - 성공")
	void findByCart_CartId_Success() {
		List<CartItem> result = cartItemRepository.findByCart_CartId(testCart.getCartId());

		assertThat(result).hasSize(2);
		assertThat(result).extracting(CartItem::getCartItemId)
			.containsExactlyInAnyOrder(testCartItem1.getCartItemId(), testCartItem2.getCartItemId());
		assertThat(result).extracting(CartItem::getQuantity)
			.containsExactlyInAnyOrder(2, 1);
	}

	@Test
	@DisplayName("장바구니 ID로 장바구니 아이템 조회 - 빈 결과")
	void findByCart_CartId_Empty() {
		UUID nonExistentCartId = UUID.randomUUID();

		List<CartItem> result = cartItemRepository.findByCart_CartId(nonExistentCartId);

		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("장바구니 아이템 저장 및 조회")
	void saveAndFind() {
		Menu newMenu = Menu.builder()
			.menuId(UUID.randomUUID())
			.name("새메뉴")
			.price(20000)
			.store(testStore)
			.build();
		entityManager.persistAndFlush(newMenu);

		CartItem newCartItem = CartItem.builder()
			.cartItemId(UUID.randomUUID())
			.cart(testCart)
			.menu(newMenu)
			.quantity(3)
			.build();

		CartItem savedCartItem = cartItemRepository.save(newCartItem);
		entityManager.flush();
		entityManager.clear();

		var foundCartItem = cartItemRepository.findById(savedCartItem.getCartItemId());

		assertThat(foundCartItem).isPresent();
		assertThat(foundCartItem.get().getQuantity()).isEqualTo(3);
		assertThat(foundCartItem.get().getMenu().getMenuId()).isEqualTo(newMenu.getMenuId());
	}

	@Test
	@DisplayName("장바구니 ID로 장바구니 아이템 삭제")
	void deleteByCart_CartId() {
		UUID cartId = testCart.getCartId();

		cartItemRepository.deleteByCart_CartId(cartId);
		entityManager.flush();

		List<CartItem> result = cartItemRepository.findByCart_CartId(cartId);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("장바구니 ID로 삭제 - 존재하지 않는 장바구니")
	void deleteByCart_CartId_NonExistent() {
		UUID nonExistentCartId = UUID.randomUUID();
		int originalCount = cartItemRepository.findAll().size();

		cartItemRepository.deleteByCart_CartId(nonExistentCartId);
		entityManager.flush();

		int currentCount = cartItemRepository.findAll().size();
		assertThat(currentCount).isEqualTo(originalCount);
	}

	@Test
	@DisplayName("영속성 컨텍스트 - 동일한 엔티티 반환")
	void persistenceContext_SameEntity() {
		CartItem item1 = cartItemRepository.findById(testCartItem1.getCartItemId()).orElse(null);
		CartItem item2 = cartItemRepository.findById(testCartItem1.getCartItemId()).orElse(null);

		assertThat(item1).isSameAs(item2);
	}

	// @Test
	// @DisplayName("트랜잭션 - 변경 감지")
	// void transaction_DirtyChecking() {
	// 	CartItem cartItem = cartItemRepository.findById(testCartItem1.getCartItemId()).orElse(null);
	// 	assertThat(cartItem).isNotNull();
	//
	// 	int newQuantity = 5;
	// 	cartItem.updateQuantity(newQuantity);
	// 	entityManager.flush();
	// 	entityManager.clear();
	//
	// 	CartItem updatedCartItem = cartItemRepository.findById(testCartItem1.getCartItemId()).orElse(null);
	// 	assertThat(updatedCartItem).isNotNull();
	// 	assertThat(updatedCartItem.getQuantity()).isEqualTo(newQuantity);
	// }

	@Test
	@DisplayName("다른 장바구니의 아이템은 영향받지 않음")
	void deleteByCart_CartId_OnlyTargetCart() {
		User anotherUser = User.builder()
			.userId(2L)
			.email("another@example.com")
			// .name("다른유저")
			.build();
		entityManager.persistAndFlush(anotherUser);

		Cart anotherCart = Cart.builder()
			.cartId(UUID.randomUUID())
			.user(anotherUser)
			.build();
		entityManager.persistAndFlush(anotherCart);

		CartItem anotherCartItem = CartItem.builder()
			.cartItemId(UUID.randomUUID())
			.cart(anotherCart)
			.menu(testMenu1)
			.quantity(1)
			.build();
		entityManager.persistAndFlush(anotherCartItem);

		cartItemRepository.deleteByCart_CartId(testCart.getCartId());
		entityManager.flush();

		List<CartItem> testCartItems = cartItemRepository.findByCart_CartId(testCart.getCartId());
		List<CartItem> anotherCartItems = cartItemRepository.findByCart_CartId(anotherCart.getCartId());

		assertThat(testCartItems).isEmpty();
		assertThat(anotherCartItems).hasSize(1);
		assertThat(anotherCartItems.get(0).getCartItemId()).isEqualTo(anotherCartItem.getCartItemId());
	}

	@Test
	@DisplayName("장바구니 아이템 개별 삭제")
	void deleteIndividualCartItem() {
		UUID cartItemId = testCartItem1.getCartItemId();

		cartItemRepository.deleteById(cartItemId);
		entityManager.flush();

		var deletedItem = cartItemRepository.findById(cartItemId);
		List<CartItem> remainingItems = cartItemRepository.findByCart_CartId(testCart.getCartId());

		assertThat(deletedItem).isEmpty();
		assertThat(remainingItems).hasSize(1);
		assertThat(remainingItems.get(0).getCartItemId()).isEqualTo(testCartItem2.getCartItemId());
	}
}