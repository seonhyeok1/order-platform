package app.domain.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import app.domain.store.model.entity.QStore;
import app.domain.store.model.entity.Store;
import app.domain.store.model.enums.StoreAcceptStatus;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class StoreSearchRepositoryImpl implements StoreSearchRepository {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Store> searchStores(String keyword, StoreAcceptStatus status, Pageable pageable) {
		QStore store = QStore.store;

		List<Store> content = queryFactory
			.selectFrom(store)
			.where(
				store.deletedAt.isNull(),
				statusEq(status),
				nameContains(keyword)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(store.createdAt.desc())
			.fetch();

		Long count = queryFactory
			.select(store.count())
			.from(store)
			.where(
				store.deletedAt.isNull(),
				statusEq(status),
				nameContains(keyword)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, count != null ? count : 0);
	}

	private BooleanExpression statusEq(StoreAcceptStatus status) {
		return status != null ? QStore.store.storeAcceptStatus.eq(status) : null;
	}

	private BooleanExpression nameContains(String keyword) {
		return keyword != null && !keyword.isBlank()
			? QStore.store.storeName.containsIgnoreCase(keyword)
			: null;
	}
}