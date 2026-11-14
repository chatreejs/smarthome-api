package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.FoodEntity;
import dev.chatree.smarthomeapi.model.food.FoodRequest;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.FoodRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    private static final long HOME_ID = 1L;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HomeRepository homeRepository;
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    void updateFoodFromAnotherHomeIsNotFound() {
        when(foodRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);
        var request = new FoodRequest("Milk", "Brand", 1.0, "L", "2026-11-01", "2026-11-30");

        assertNotFound(() -> foodService.updateFood(99L, request, HOME_ID, "member"));
        verify(foodRepository, never()).save(any());
    }

    @Test
    void deleteFoodFromAnotherHomeIsNotFound() {
        when(foodRepository.findByIdAndHomeId(99L, HOME_ID)).thenReturn(null);

        assertNotFound(() -> foodService.deleteFood(99L, HOME_ID));
        verify(foodRepository, never()).delete(any());
    }

    @Test
    void deleteFoodInSameHomeDeletesIt() {
        var food = new FoodEntity();
        when(foodRepository.findByIdAndHomeId(5L, HOME_ID)).thenReturn(food);

        foodService.deleteFood(5L, HOME_ID);

        verify(foodRepository).delete(food);
    }

    @Test
    void bulkDeleteWithAnyForeignIdDeletesNothing() {
        // Id 99 belongs to another home, so the query finds only id 5.
        when(foodRepository.findAllByIdInAndHomeId(Set.of(5L, 99L), HOME_ID)).thenReturn(List.of(new FoodEntity()));

        assertNotFound(() -> foodService.deleteMultipleFood(List.of(5L, 99L), HOME_ID));
        verify(foodRepository, never()).deleteAll(anyIterable());
        verify(foodRepository, never()).deleteAllById(anyIterable());
    }

    @Test
    void bulkDeleteWithAllIdsInHomeDeletesThem() {
        var foods = List.of(new FoodEntity(), new FoodEntity());
        // The request repeats id 6, and the service ignores the duplicate.
        when(foodRepository.findAllByIdInAndHomeId(Set.of(5L, 6L), HOME_ID)).thenReturn(foods);

        foodService.deleteMultipleFood(List.of(5L, 6L, 6L), HOME_ID);

        verify(foodRepository).deleteAll(foods);
    }

    private static void assertNotFound(Runnable call) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(HttpClientErrorException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
