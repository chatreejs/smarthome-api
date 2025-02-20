package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.constant.ErrorMessage;
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
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAllInventory(Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var inventoryEntityList = inventoryRepository.findAllByHomeIdAndOrderByQuantityAsc(homeId);
        inventoryEntityList.sort((o1, o2) -> {
            if (o1.getQuantity() <= (o1.getMaxQuantity() * 0.3)) {
                return -1;
            } else if (o2.getQuantity() <= (o2.getMaxQuantity() * 0.3)) {
                return 1;
            } else {
                return 0;
            }
        });

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

    public InventoryResponse getInventoryById(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

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
        boolean isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

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
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var inventoryEntity = inventoryRepository.findById(id).orElse(null);
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

    public void deleteInventory(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var inventoryEntity = inventoryRepository.findById(id).orElse(null);
        if (inventoryEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Inventory not found");
        }

        inventoryRepository.deleteById(id);
    }

    public void deleteMultipleInventory(List<Long> ids, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        inventoryRepository.deleteAllById(ids);
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

    private InventoryStatus checkInventoryStatus(Double quantity, Double maxQuantity) {
        if (quantity == 0) {
            return InventoryStatus.OUT_OF_STOCK;
        } else if (quantity <= (maxQuantity * 0.3)) {
            return InventoryStatus.LOW_STOCK;
        } else {
            return InventoryStatus.IN_STOCK;
        }
    }
}
