package app.domain.store.service;

import app.domain.store.model.dto.request.StoreApproveRequest;
import app.domain.store.model.dto.response.StoreApproveResponse;

public interface StoreService {
	StoreApproveResponse createStore(StoreApproveRequest request);
}
