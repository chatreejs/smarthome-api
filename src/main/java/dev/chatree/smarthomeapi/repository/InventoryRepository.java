package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {

    InventoryEntity findByIdAndHomeId(Long id, Long homeId);

    List<InventoryEntity> findAllByIdInAndHomeId(Collection<Long> ids, Long homeId);

    @Query("SELECT i FROM InventoryEntity i WHERE i.home.id = :homeId ORDER BY i.quantity ASC")
    List<InventoryEntity> findAllByHomeIdAndOrderByQuantityAsc(Long homeId);
}
