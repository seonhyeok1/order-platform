package app.domain.cart.model.entity;

import java.util.UUID;

import app.domain.menu.model.entity.Menu;
import app.domain.store.model.entity.Store;
import app.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class CartItem extends BaseEntity {

	@Id
	@GeneratedValue
	@Column(name = "cart_item_id", updatable = false, nullable = false)
	private UUID cartItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "menu_id", nullable = false)
	private Menu menu;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false)
	private int quantity;

	public CartItem(UUID cartItemId, Cart cart, Menu menu, Store store, int quantity) {
		this.cartItemId = cartItemId;
		this.cart = cart;
		this.menu = menu;
		this.store = store;
		this.quantity = quantity;
	}
}