package app.domain.store.service;

import app.domain.owner.model.dto.request.StoreApproveRequest;
import app.domain.owner.model.dto.response.StoreApproveResponse;

public interface StoreService {
	StoreApproveResponse createStore(StoreApproveRequest request);
}
