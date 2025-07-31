package app.domain.review.model;

import app.domain.order.model.entity.Orders;
import app.domain.review.model.entity.Review;
import app.domain.store.model.entity.Store;
import app.domain.user.model.entity.User;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByUser(User user);

    List<Review> findByStore(Store store);

    boolean existsByOrders(Orders orders);
}
