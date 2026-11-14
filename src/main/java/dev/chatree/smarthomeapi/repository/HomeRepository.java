package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.HomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeRepository extends JpaRepository<HomeEntity, Long> {
    List<HomeEntity> findByAccountsId(Long accountId);

    boolean existsByIdAndAccountsId(Long id, Long accountId);

    boolean existsByIdAndAccountsSubject(Long id, String subject);

    /**
     * Inserts the {@code home_account} row directly, so neither entity's collection gets loaded.
     */
    @Modifying
    @Query(value = "INSERT INTO home_account (account_id, home_id) VALUES (:accountId, :homeId)", nativeQuery = true)
    void addMember(@Param("homeId") Long homeId, @Param("accountId") Long accountId);
}
