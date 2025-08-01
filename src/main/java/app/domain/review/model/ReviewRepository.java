package app.domain.review.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import app.domain.review.model.entity.Review;
import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.storeId = :storeId")
	Double getAverageRatingByStore(@Param("storeId") UUID storeId);

}
