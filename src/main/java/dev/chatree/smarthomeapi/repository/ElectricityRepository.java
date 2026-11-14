package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.ElectricityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ElectricityRepository extends JpaRepository<ElectricityEntity, Long> {

    ElectricityEntity findByIdAndHomeId(Long id, Long homeId);

    List<ElectricityEntity> findAllByIdInAndHomeId(Collection<Long> ids, Long homeId);

    @Query("SELECT e FROM ElectricityEntity e WHERE e.home.id = :homeId ORDER BY e.invoiceDate")
    List<ElectricityEntity> findAllByHomeId(Long homeId);
}
