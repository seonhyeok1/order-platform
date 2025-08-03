package app.domain.store.model.entity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import app.domain.store.model.enums.StoreAcceptStatus;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

	Optional<Store> findByUser_UserId(Long userId);

	boolean existsByStoreNameAndRegion(String storeName, Region region);

	Optional<Store> findByStoreIdAndDeletedAtIsNull(UUID storeId);

	Optional<Store> findByStoreIdAndStoreAcceptStatusAndDeletedAtIsNull(UUID storeId, StoreAcceptStatus status);

	boolean existsByStoreIdAndDeletedAtIsNull(UUID storeId);
}
