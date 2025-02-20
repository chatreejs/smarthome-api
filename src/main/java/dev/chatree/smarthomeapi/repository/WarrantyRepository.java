package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.WarrantyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<WarrantyEntity, Long> {
    WarrantyEntity findByIdAndHomeId(Long id, Long homeId);

    @Query("SELECT w FROM WarrantyEntity w WHERE w.home.id = :homeId ORDER BY w.warrantyDate ASC")
    List<WarrantyEntity> findAllByHomeId(Long homeId);
}
