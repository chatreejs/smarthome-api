package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.constant.ErrorMessage;
import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.WarrantyEntity;
import dev.chatree.smarthomeapi.model.warranty.WarrantyRequest;
import dev.chatree.smarthomeapi.model.warranty.WarrantyResponse;
import dev.chatree.smarthomeapi.model.warranty.WarrantyStatus;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.WarrantyRepository;
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
public class WarrantyService {

    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final WarrantyRepository warrantyRepository;

    public List<WarrantyResponse> getAllWarranty(Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var warrantyEntityList = warrantyRepository.findAllByHomeId(homeId);
        var warrantyResponseList = new ArrayList<WarrantyResponse>();

        for (WarrantyEntity warrantyEntity : warrantyEntityList) {
            var updateBy = warrantyEntity.getUpdateBy();
            if (updateBy == null) {
                updateBy = warrantyEntity.getCreateBy();
            }
            var warrantyResponse = generateWarrantyResponse(warrantyEntity, updateBy);
            warrantyResponseList.add(warrantyResponse);
        }

        return warrantyResponseList;
    }

    public WarrantyResponse getWarrantyById(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var warrantyEntity = warrantyRepository.findByIdAndHomeId(id, homeId);
        if (warrantyEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Warranty not found");
        }

        var updateBy = warrantyEntity.getUpdateBy();
        if (updateBy == null) {
            updateBy = warrantyEntity.getCreateBy();
        }

        return generateWarrantyResponse(warrantyEntity, updateBy);
    }

    public void createWarranty(WarrantyRequest warrantyRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var homeEntity = homeRepository.findById(homeId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found"));
        var warrantyEntity = new WarrantyEntity();
        warrantyEntity.setBrand(warrantyRequest.getBrand());
        warrantyEntity.setProductName(warrantyRequest.getProductName());
        warrantyEntity.setProductNumber(warrantyRequest.getProductNumber());
        warrantyEntity.setModel(warrantyRequest.getModel());
        warrantyEntity.setSerialNumber(warrantyRequest.getSerialNumber());
        warrantyEntity.setPurchaseDate(LocalDate.parse(warrantyRequest.getPurchaseDate(), DateTimeFormatter.ISO_DATE));
        warrantyEntity.setWarrantyDate(LocalDate.parse(warrantyRequest.getWarrantyDate(), DateTimeFormatter.ISO_DATE));
        warrantyEntity.setCreateBy(account);
        warrantyEntity.setHome(homeEntity);

        warrantyRepository.save(warrantyEntity);
    }

    public void updateWarranty(Long id, WarrantyRequest warrantyRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var warrantyEntity = warrantyRepository.findByIdAndHomeId(id, homeId);
        if (warrantyEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Warranty not found");
        }
        warrantyEntity.setBrand(warrantyRequest.getBrand());
        warrantyEntity.setProductName(warrantyRequest.getProductName());
        warrantyEntity.setProductNumber(warrantyRequest.getProductNumber());
        warrantyEntity.setModel(warrantyRequest.getModel());
        warrantyEntity.setSerialNumber(warrantyRequest.getSerialNumber());
        warrantyEntity.setPurchaseDate(LocalDate.parse(warrantyRequest.getPurchaseDate(), DateTimeFormatter.ISO_DATE));
        warrantyEntity.setWarrantyDate(LocalDate.parse(warrantyRequest.getWarrantyDate(), DateTimeFormatter.ISO_DATE));

        warrantyRepository.save(warrantyEntity);
    }

    public void deleteWarranty(Long id, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        var warrantyEntity = warrantyRepository.findByIdAndHomeId(id, homeId);
        if (warrantyEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Warranty not found");
        }
        warrantyRepository.delete(warrantyEntity);
    }

    public void deleteMultipleWarranty(List<Long> ids, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

        warrantyRepository.deleteAllById(ids);
    }

    private WarrantyResponse generateWarrantyResponse(WarrantyEntity warrantyEntity, AccountEntity updateBy) {
        return WarrantyResponse.builder()
                .id(warrantyEntity.getId())
                .brand(warrantyEntity.getBrand())
                .productName(warrantyEntity.getProductName())
                .productNumber(warrantyEntity.getProductNumber())
                .model(warrantyEntity.getModel())
                .serialNumber(warrantyEntity.getSerialNumber())
                .purchaseDate(warrantyEntity.getPurchaseDate().toString())
                .warrantyDate(warrantyEntity.getWarrantyDate().toString())
                .status(generateWarrantyStatus(warrantyEntity.getWarrantyDate()))
                .updateBy(updateBy.getUsername())
                .updateDate(warrantyEntity.getUpdateDate().toString())
                .build();
    }

    private WarrantyStatus generateWarrantyStatus(LocalDate warrantyDate) {
        if (warrantyDate.isBefore(LocalDate.now())) {
            return WarrantyStatus.OUT_OF_WARRANTY;
        } else {
            return WarrantyStatus.IN_WARRANTY;
        }
    }
}
