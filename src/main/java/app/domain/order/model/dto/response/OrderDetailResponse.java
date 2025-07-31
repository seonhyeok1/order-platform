package app.domain.order.model.dto.response;

import java.util.List;

import app.domain.order.model.entity.OrderItem;
import app.domain.order.model.entity.Orders;
import app.domain.order.model.entity.enums.OrderChannel;
import app.domain.order.model.entity.enums.OrderStatus;
import app.domain.order.model.entity.enums.PaymentMethod;
import app.domain.order.model.entity.enums.ReceiptMethod;

public record OrderDetailResponse(
	String storeName,
	List<Menu> menuList,
	Long totalPrice,
	String deliveryAddress,
	PaymentMethod paymentMethod,
	OrderChannel orderChannel,
	ReceiptMethod receiptMethod,
	OrderStatus orderStatus,
	String requestMessage
) {
	public record Menu(
		String menuName,
		int quantity,
		int price
	) {
		public static Menu from(OrderItem orderItem) {
			return new Menu(
				orderItem.getMenuName(),
				orderItem.getQuantity(),
				orderItem.getPrice()
			);
		}
	}

	public static OrderDetailResponse from(Orders orders, List<OrderItem> orderItems) {
		return new OrderDetailResponse(
			orders.getStore().getStoreName(),
			orderItems.stream().map(Menu::from).toList(),
			orders.getTotalPrice(),
			orders.getDeliveryAddress(),
			orders.getPaymentMethod(),
			orders.getOrderChannel(),
			orders.getReceiptMethod(),
			orders.getOrderStatus(),
			orders.getRequestMessage()
		);
	}
}
