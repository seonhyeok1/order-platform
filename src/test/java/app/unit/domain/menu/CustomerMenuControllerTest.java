package app.unit.domain.menu;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import app.domain.menu.CustomerMenuController;
import app.domain.menu.CustomerMenuService;
import app.domain.menu.model.dto.response.GetMenuListResponse;
import app.global.apiPayload.PagedResponse;
import app.global.apiPayload.code.status.ErrorStatus;
import app.global.apiPayload.exception.GeneralException;
import app.global.config.MockSecurityConfig;
import app.global.jwt.JwtAccessDeniedHandler;
import app.global.jwt.JwtAuthenticationEntryPoint;
import app.global.jwt.JwtTokenProvider;

@WebMvcTest(CustomerMenuController.class)
@Import({MockSecurityConfig.class})
class CustomerMenuControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CustomerMenuService menuService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(springSecurity())
			.build();
	}

	@DisplayName("가게의 메뉴 목록을 조회한다")
	@WithMockUser(roles = "CUSTOMER")
	@Test
	void getMenusByStoreId() throws Exception {
		// given
		UUID storeId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 20);

		List<GetMenuListResponse> content = List.of(
			GetMenuListResponse.builder()
				.menuId(UUID.randomUUID())
				.name("치즈돈까스")
				.description("부드러운 치즈와 함께")
				.price(8900L)
				.build(),
			GetMenuListResponse.builder()
				.menuId(UUID.randomUUID())
				.name("김치찌개")
				.description("매콤한 돼지고기 김치찌개")
				.price(7500L)
				.build()
		);

		PageImpl<GetMenuListResponse> page = new PageImpl<>(content, pageable, content.size());
		PagedResponse<GetMenuListResponse> pagedResponse = PagedResponse.from(page);

		when(menuService.getMenusByStoreId(eq(storeId), any(Pageable.class))).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/api/customer/store/{storeId}/menus", storeId)
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content.length()").value(2))
			.andExpect(jsonPath("$.result.content[0].name").value("치즈돈까스"))
			.andExpect(jsonPath("$.result.content[1].price").value(7500L));
	}

	@DisplayName("존재하지 않는 가게 ID로 메뉴 조회 시 예외가 발생한다")
	@WithMockUser(roles = "CUSTOMER")
	@Test
	void getMenusByStoreId_fail_storeNotFound() throws Exception {
		// given
		UUID invalidStoreId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 20);

		// when
		when(menuService.getMenusByStoreId(eq(invalidStoreId), any(Pageable.class)))
			.thenThrow(new GeneralException(ErrorStatus.STORE_NOT_FOUND));

		// then
		mockMvc.perform(get("/customer/{storeId}/menus", invalidStoreId)
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(ErrorStatus.STORE_NOT_FOUND.getCode()))
			.andExpect(jsonPath("$.message").value(ErrorStatus.STORE_NOT_FOUND.getMessage()));
	}


	@DisplayName("가게 메뉴가 없는 경우 빈 목록을 반환한다")
	@WithMockUser(roles = "CUSTOMER")
	@Test
	void getMenusByStoreId_emptyList() throws Exception {
		// given
		UUID storeId = UUID.randomUUID();
		Pageable pageable = PageRequest.of(0, 20);
		PageImpl<GetMenuListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
		PagedResponse<GetMenuListResponse> pagedResponse = PagedResponse.from(emptyPage);

		when(menuService.getMenusByStoreId(eq(storeId), any(Pageable.class))).thenReturn(pagedResponse);

		// when & then
		mockMvc.perform(get("/customer/{storeId}/menus", storeId)
				.param("page", "0")
				.param("size", "20")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.result.content").isEmpty());
	}
}