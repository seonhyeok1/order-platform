package app.domain.store.model.entity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

	@Query("SELECT s FROM Store s WHERE s.user.userId = :userId")
	Optional<Store> findByUserId(UUID userId);

}
