package app.domain.ai.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.ai.model.entity.AiRequest;

@Repository
public interface AiRequestRepository extends JpaRepository<AiRequest, UUID> {
}
