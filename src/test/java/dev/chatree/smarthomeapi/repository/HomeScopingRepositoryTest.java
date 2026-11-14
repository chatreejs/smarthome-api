package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.ElectricityEntity;
import dev.chatree.smarthomeapi.entity.FoodEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.entity.WaterworksEntity;
import dev.chatree.smarthomeapi.model.home.HomeRequest;
import dev.chatree.smarthomeapi.service.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the home-scoping queries against an in-memory H2 database.
 */
@DataJpaTest
@Import(HomeService.class)
class HomeScopingRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private HomeService homeService;
    @Autowired
    private HomeRepository homeRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private WaterworksRepository waterworksRepository;
    @Autowired
    private ElectricityRepository electricityRepository;

    @Test
    void creatingASecondHomeKeepsTheFirst() {
        var account = account("user-1");

        var first = homeService.createHome(homeRequest("Condo"), account);
        var second = homeService.createHome(homeRequest("House"), account);
        em.flush();
        em.clear();

        assertThat(homeRepository.findByAccountsId(account.getId()))
                .extracting(HomeEntity::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(homeRepository.existsByIdAndAccountsSubject(first.getId(), "user-1")).isTrue();
        assertThat(homeRepository.existsByIdAndAccountsSubject(second.getId(), "user-1")).isTrue();
    }

    @Test
    void membershipCheckIsPerHome() {
        var alice = account("alice");
        var bob = account("bob");
        var aliceHome = homeService.createHome(homeRequest("Alice home"), alice);
        homeService.createHome(homeRequest("Bob home"), bob);
        em.flush();

        assertThat(homeRepository.existsByIdAndAccountsSubject(aliceHome.getId(), "alice")).isTrue();
        assertThat(homeRepository.existsByIdAndAccountsSubject(aliceHome.getId(), "bob")).isFalse();
    }

    @Test
    void scopedFoodQueriesIgnoreOtherHomes() {
        var account = account("user-2");
        var homeA = em.find(HomeEntity.class, homeService.createHome(homeRequest("A"), account).getId());
        var homeB = em.find(HomeEntity.class, homeService.createHome(homeRequest("B"), account).getId());
        var foodA = food(homeA);
        var foodB = food(homeB);
        em.flush();

        assertThat(foodRepository.findByIdAndHomeId(foodA.getId(), homeA.getId())).isNotNull();
        assertThat(foodRepository.findByIdAndHomeId(foodB.getId(), homeA.getId())).isNull();
        assertThat(foodRepository.findAllByIdInAndHomeId(List.of(foodA.getId(), foodB.getId()), homeA.getId()))
                .extracting(FoodEntity::getId)
                .containsExactly(foodA.getId());
    }

    @Test
    void scopedWaterworksQueriesIgnoreOtherHomes() {
        var account = account("user-3");
        var homeA = em.find(HomeEntity.class, homeService.createHome(homeRequest("A"), account).getId());
        var homeB = em.find(HomeEntity.class, homeService.createHome(homeRequest("B"), account).getId());
        var billA = waterworks(homeA);
        var billB = waterworks(homeB);
        em.flush();
        em.clear();

        var found = waterworksRepository.findByIdAndHomeId(billA.getId(), homeA.getId());
        assertThat(found).isNotNull();
        assertThat(found.getTotal()).isEqualByComparingTo("123.45");
        assertThat(waterworksRepository.findByIdAndHomeId(billB.getId(), homeA.getId())).isNull();
        assertThat(waterworksRepository.findAllByIdInAndHomeId(List.of(billA.getId(), billB.getId()), homeA.getId()))
                .extracting(WaterworksEntity::getId)
                .containsExactly(billA.getId());
    }

    @Test
    void scopedElectricityQueriesIgnoreOtherHomes() {
        var account = account("user-4");
        var homeA = em.find(HomeEntity.class, homeService.createHome(homeRequest("A"), account).getId());
        var homeB = em.find(HomeEntity.class, homeService.createHome(homeRequest("B"), account).getId());
        var billA = electricity(homeA);
        var billB = electricity(homeB);
        em.flush();
        em.clear();

        var found = electricityRepository.findByIdAndHomeId(billA.getId(), homeA.getId());
        assertThat(found).isNotNull();
        // ft_rate keeps four decimal places, unlike the money columns.
        assertThat(found.getFtRate()).isEqualByComparingTo("0.3972");
        assertThat(electricityRepository.findByIdAndHomeId(billB.getId(), homeA.getId())).isNull();
        assertThat(electricityRepository.findAllByIdInAndHomeId(List.of(billA.getId(), billB.getId()), homeA.getId()))
                .extracting(ElectricityEntity::getId)
                .containsExactly(billA.getId());
    }

    private AccountEntity account(String subject) {
        var account = new AccountEntity();
        account.setSubject(subject);
        account.setUsername(subject);
        return em.persistAndFlush(account);
    }

    private FoodEntity food(HomeEntity home) {
        var food = new FoodEntity();
        food.setName("Milk");
        food.setHome(home);
        return em.persist(food);
    }

    private WaterworksEntity waterworks(HomeEntity home) {
        var bill = new WaterworksEntity();
        bill.setInvoiceNumber("INV-1");
        bill.setInvoiceDate(LocalDate.of(2026, 10, 31));
        bill.setTotal(new BigDecimal("123.45"));
        bill.setHome(home);
        return em.persist(bill);
    }

    private ElectricityEntity electricity(HomeEntity home) {
        var bill = new ElectricityEntity();
        bill.setInvoiceNumber("INV-1");
        bill.setInvoiceDate(LocalDate.of(2026, 10, 31));
        bill.setUsage(250);
        bill.setFtRate(new BigDecimal("0.3972"));
        bill.setTotal(new BigDecimal("123.45"));
        bill.setHome(home);
        return em.persist(bill);
    }

    private static HomeRequest homeRequest(String name) {
        return new HomeRequest(name, "address", null, null, null);
    }
}
