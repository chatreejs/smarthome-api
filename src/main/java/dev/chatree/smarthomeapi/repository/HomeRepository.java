package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.HomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeRepository extends JpaRepository<HomeEntity, Long> {
    List<HomeEntity> findByAccountsId(Long accountId);

    boolean existsByIdAndAccountsId(Long id, Long accountId);

    boolean existsByIdAndAccountsSubject(Long id, String subject);
}
