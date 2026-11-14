package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.InventoryEntity;
import dev.chatree.smarthomeapi.model.inventory.InventoryResponse;
import dev.chatree.smarthomeapi.model.inventory.InventoryStatus;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final long HOME_ID = 1L;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HomeRepository homeRepository;
    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void listPutsLowStockFirstAndKeepsQuantityOrder() {
        // The old comparator returned -1 for any two low-stock items, which broke the quantity order.
        var random = new Random(42);
        var items = new ArrayList<InventoryEntity>();
        for (long id = 1; id <= 500; id++) {
            items.add(inventory(id, random.nextInt(11), 10.0));
        }
        items.add(inventory(501, 3, null));
        items.sort(Comparator.comparing(InventoryEntity::getQuantity));
        when(inventoryRepository.findAllByHomeIdAndOrderByQuantityAsc(HOME_ID)).thenReturn(items);

        var result = inventoryService.getAllInventory(HOME_ID);

        assertThat(result).hasSize(501);
        var lowStockCount = (int) result.stream().filter(InventoryServiceTest::isLowOrOut).count();
        assertThat(result.subList(0, lowStockCount)).allMatch(InventoryServiceTest::isLowOrOut);
        assertThat(result.subList(lowStockCount, result.size())).noneMatch(InventoryServiceTest::isLowOrOut);
        assertThat(result.subList(0, lowStockCount)).isSortedAccordingTo(Comparator.comparing(InventoryResponse::getQuantity));
        assertThat(result.subList(lowStockCount, result.size())).isSortedAccordingTo(Comparator.comparing(InventoryResponse::getQuantity));
    }

    @Test
    void itemWithoutMaxQuantityIsInStock() {
        when(inventoryRepository.findAllByHomeIdAndOrderByQuantityAsc(HOME_ID))
                .thenReturn(new ArrayList<>(List.of(inventory(1, 3, null))));

        var result = inventoryService.getAllInventory(HOME_ID);

        assertThat(result.get(0).getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
    }

    private static boolean isLowOrOut(InventoryResponse response) {
        return response.getStatus() != InventoryStatus.IN_STOCK;
    }

    private static InventoryEntity inventory(long id, double quantity, Double maxQuantity) {
        var account = new AccountEntity();
        account.setUsername("member");
        var inventory = new InventoryEntity();
        inventory.setId(id);
        inventory.setName("Item " + id);
        inventory.setQuantity(quantity);
        inventory.setMaxQuantity(maxQuantity);
        inventory.setRestockDate(LocalDate.of(2026, 11, 1));
        inventory.setCreateBy(account);
        inventory.setUpdateDate(LocalDateTime.of(2026, 11, 1, 0, 0));
        return inventory;
    }
}
