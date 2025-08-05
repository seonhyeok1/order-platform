package app.domain.menu.model.dto.response;

import java.util.UUID;

import app.domain.menu.model.entity.Menu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMenuListResponse {
    private UUID menuId;
    private String name;
    private String description;
    private Long price;

    public static GetMenuListResponse from(Menu menu) {
        return GetMenuListResponse.builder()
            .menuId(menu.getMenuId())
            .name(menu.getName())
            .description(menu.getDescription())
            .price(menu.getPrice())
            .build();
    }
}