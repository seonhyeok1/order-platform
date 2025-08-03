package app.domain.review.model;

import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import app.domain.review.model.entity.Review;
import org.springframework.data.repository.query.Param;
import app.domain.order.model.entity.Orders;
import app.domain.review.model.entity.Review;
import app.domain.user.model.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.storeId = :storeId")
	Double getAverageRatingByStore(@Param("storeId") UUID storeId);

  List<Review> findByUser(User user);

	boolean existsByOrders(Orders orders);
  
  
}
