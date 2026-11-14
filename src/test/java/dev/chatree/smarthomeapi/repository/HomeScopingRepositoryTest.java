package dev.chatree.smarthomeapi.repository;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.FoodEntity;
import dev.chatree.smarthomeapi.entity.HomeEntity;
import dev.chatree.smarthomeapi.model.home.HomeRequest;
import dev.chatree.smarthomeapi.service.HomeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

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

    private static HomeRequest homeRequest(String name) {
        return new HomeRequest(name, "address", null, null, null);
    }
}
