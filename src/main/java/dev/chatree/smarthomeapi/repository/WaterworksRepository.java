package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.WaterworksEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterworksRepository extends JpaRepository<WaterworksEntity, Long> {

    @Query("SELECT w FROM WaterworksEntity w WHERE w.home.id = :homeId ORDER BY w.invoiceDate DESC")
    List<WaterworksEntity> findAllByHomeId(Long homeId);
}
