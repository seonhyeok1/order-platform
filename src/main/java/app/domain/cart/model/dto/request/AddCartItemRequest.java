package app.domain.cart.model.dto.request;

import java.util.UUID;

import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;

public record AddCartItemRequest(
	UUID menuId,
	UUID storeId,
	int quantity
) {
}