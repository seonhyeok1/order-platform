package app.domain.menu.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.menu.model.entity.Menu;
import app.domain.store.model.entity.Store;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {
	List<Menu> findByStoreAndDeletedAtIsNull(Store store);

	Optional<Menu> findByMenuIdAndDeletedAtIsNull(UUID menuId);

	boolean existsByStoreAndNameAndDeletedAtIsNull(Store store, String name);
}
