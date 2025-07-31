package app.domain.store.model.entity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

	Optional<Store> findByUser_UserId(Long userId);

	boolean existsByStoreNameAndRegion(String storeName, Region region);

}
