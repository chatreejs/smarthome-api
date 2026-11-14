package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<FoodEntity, Long> {
    FoodEntity findByIdAndHomeId(Long id, Long homeId);

    List<FoodEntity> findAllByIdInAndHomeId(Collection<Long> ids, Long homeId);

    @Query("SELECT f FROM FoodEntity f WHERE f.home.id = :homeId ORDER BY f.expiryDate ASC")
    List<FoodEntity> findAllByHomeId(Long homeId);
}
