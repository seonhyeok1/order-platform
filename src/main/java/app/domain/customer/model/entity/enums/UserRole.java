package app.domain.customer.model.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	CUSTOMER,
	OWNER,
	MANAGER,
	MASTER;
}