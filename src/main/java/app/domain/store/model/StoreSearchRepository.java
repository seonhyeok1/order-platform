package app.domain.store.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.domain.store.model.entity.Store;
import app.domain.store.model.enums.StoreAcceptStatus;

public interface StoreSearchRepository {
	Page<Store> searchStores(String keyword, StoreAcceptStatus status, Pageable pageable);
}