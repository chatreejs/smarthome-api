package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    List<InventoryEntity> findAllByOrderByQuantityAsc();
}
