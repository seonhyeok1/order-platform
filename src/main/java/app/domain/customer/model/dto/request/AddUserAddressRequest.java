package app.domain.customer.model.dto.request;

public record AddUserAddressRequest(
        Long userId,
        String alias,
        String address,
        String addressDetail,
        boolean isDefault
) {
}