package app.domain.owner.model.entity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {
	Optional<Region> findByRegionCode(String regionCode);
}
