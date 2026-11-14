package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.InventoryEntity;
import dev.chatree.smarthomeapi.model.inventory.InventoryRequest;
import dev.chatree.smarthomeapi.model.inventory.InventoryResponse;
import dev.chatree.smarthomeapi.model.inventory.InventoryStatus;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAllInventory(Long homeId) {
        var inventoryEntityList = inventoryRepository.findAllByHomeIdAndOrderByQuantityAsc(homeId);
        // The query sorts by quantity, and List.sort is stable, so each group stays in quantity order.
        inventoryEntityList.sort(Comparator.comparing((InventoryEntity inventory) -> !isLowStock(inventory.getQuantity(), inventory.getMaxQuantity())));

        var inventoryResponseList = new ArrayList<InventoryResponse>();

        for (var inventoryEntity : inventoryEntityList) {
            var updateBy = inventoryEntity.getUpdateBy();
            if (updateBy == null) {
                updateBy = inventoryEntity.getCreateBy();
            }
            var inventoryResponse = generateInventoryResponse(inventoryEntity, updateBy);
            inventoryResponseList.add(inventoryResponse);
        }
        return inventoryResponseList;
    }

    public InventoryResponse getInventoryById(Long id, Long homeId) {
        var inventoryEntity = inventoryRepository.findByIdAndHomeId(id, homeId);
        if (inventoryEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Inventory not found");
        }

        var updateBy = inventoryEntity.getUpdateBy();
        if (updateBy == null) {
            updateBy = inventoryEntity.getCreateBy();
        }

        return generateInventoryResponse(inventoryEntity, updateBy);
    }

    public void createInventory(InventoryRequest inventoryRequest, Long homeId, String subject) {
        AccountEntity account = accountRepository.findBySubject(subject);

        var homeEntity = homeRepository.findById(homeId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found"));
        var inventoryEntity = new InventoryEntity();
        inventoryEntity.setName(inventoryRequest.getName());
        inventoryEntity.setBrand(inventoryRequest.getBrand());
        inventoryEntity.setQuantity(inventoryRequest.getQuantity());
        inventoryEntity.setMaxQuantity(inventoryRequest.getMaxQuantity());
        inventoryEntity.setUnit(inventoryRequest.getUnit());
        inventoryEntity.setRestockDate(LocalDate.parse(inventoryRequest.getRestockDate(), DateTimeFormatter.ISO_DATE));
        inventoryEntity.setCreateBy(account);
        inventoryEntity.setHome(homeEntity);

        inventoryRepository.save(inventoryEntity);
    }

    public void updateInventory(Long id, InventoryRequest inventoryRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);

        var inventoryEntity = inventoryRepository.findByIdAndHomeId(id, homeId);
        if (inventoryEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Inventory not found");
        }

        inventoryEntity.setName(inventoryRequest.getName());
        inventoryEntity.setBrand(inventoryRequest.getBrand());
        inventoryEntity.setQuantity(inventoryRequest.getQuantity());
        inventoryEntity.setMaxQuantity(inventoryRequest.getMaxQuantity());
        inventoryEntity.setUnit(inventoryRequest.getUnit());
        inventoryEntity.setRestockDate(LocalDate.parse(inventoryRequest.getRestockDate(), DateTimeFormatter.ISO_DATE));
        inventoryEntity.setUpdateBy(account);

        inventoryRepository.save(inventoryEntity);
    }

    public void deleteInventory(Long id, Long homeId) {
        var inventoryEntity = inventoryRepository.findByIdAndHomeId(id, homeId);
        if (inventoryEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Inventory not found");
        }

        inventoryRepository.delete(inventoryEntity);
    }

    @Transactional
    public void deleteMultipleInventory(List<Long> ids, Long homeId) {
        var uniqueIds = new HashSet<>(ids);
        var inventoryEntityList = inventoryRepository.findAllByIdInAndHomeId(uniqueIds, homeId);
        if (inventoryEntityList.size() != uniqueIds.size()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Inventory not found");
        }
        inventoryRepository.deleteAll(inventoryEntityList);
    }

    private InventoryResponse generateInventoryResponse(InventoryEntity inventoryEntity, AccountEntity updateBy) {
        return InventoryResponse.builder()
                .id(inventoryEntity.getId())
                .name(inventoryEntity.getName())
                .brand(inventoryEntity.getBrand())
                .quantity(inventoryEntity.getQuantity())
                .maxQuantity(inventoryEntity.getMaxQuantity())
                .unit(inventoryEntity.getUnit())
                .restockDate(inventoryEntity.getRestockDate().toString())
                .status(checkInventoryStatus(inventoryEntity.getQuantity(), inventoryEntity.getMaxQuantity()))
                .updateBy(updateBy.getUsername())
                .updateDate(inventoryEntity.getUpdateDate().toString())
                .build();
    }

    /**
     * An item is low on stock at or below 30% of its max quantity. An item with no max quantity is never low.
     */
    private static boolean isLowStock(Double quantity, Double maxQuantity) {
        return quantity != null && maxQuantity != null && quantity <= maxQuantity * 0.3;
    }

    private InventoryStatus checkInventoryStatus(Double quantity, Double maxQuantity) {
        if (quantity == null || quantity == 0) {
            return InventoryStatus.OUT_OF_STOCK;
        } else if (isLowStock(quantity, maxQuantity)) {
            return InventoryStatus.LOW_STOCK;
        } else {
            return InventoryStatus.IN_STOCK;
        }
    }
}
