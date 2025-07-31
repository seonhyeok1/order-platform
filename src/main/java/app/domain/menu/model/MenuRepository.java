package app.domain.menu.model;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.domain.menu.model.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {
}
