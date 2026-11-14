package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.WaterworksEntity;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksRequest;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksResponse;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.WaterworksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class WaterworksService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final WaterworksRepository waterworksRepository;

    public List<WaterworksResponse> getAllWaterworks(Long homeId) {
        var waterworksEntityList = waterworksRepository.findAllByHomeId(homeId);
        var waterworksResponseList = new ArrayList<WaterworksResponse>();

        for (var waterworksEntity : waterworksEntityList) {
            var updateBy = waterworksEntity.getUpdateBy();
            if (updateBy == null) {
                updateBy = waterworksEntity.getCreateBy();
            }
            var waterworksResponse = generateWaterworksResponse(waterworksEntity, updateBy);
            waterworksResponseList.add(waterworksResponse);
        }
        return waterworksResponseList;
    }

    public WaterworksResponse getWaterworksById(Long id, Long homeId) {
        var waterworksEntity = findInHome(id, homeId);

        var updateBy = waterworksEntity.getUpdateBy();
        if (updateBy == null) {
            updateBy = waterworksEntity.getCreateBy();
        }
        return generateWaterworksResponse(waterworksEntity, updateBy);
    }

    public void createWaterworks(WaterworksRequest waterworksRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);

        var homeEntity = homeRepository.findById(homeId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found"));
        var waterworksEntity = new WaterworksEntity();
        applyRequest(waterworksEntity, waterworksRequest);
        waterworksEntity.setCreateBy(account);
        waterworksEntity.setHome(homeEntity);

        waterworksRepository.save(waterworksEntity);
    }

    public void updateWaterworks(Long id, WaterworksRequest waterworksRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);

        var waterworksEntity = findInHome(id, homeId);
        applyRequest(waterworksEntity, waterworksRequest);
        waterworksEntity.setUpdateBy(account);

        waterworksRepository.save(waterworksEntity);
    }

    public void deleteWaterworks(Long id, Long homeId) {
        var waterworksEntity = findInHome(id, homeId);
        waterworksRepository.delete(waterworksEntity);
    }

    @Transactional
    public void deleteMultipleWaterworks(List<Long> ids, Long homeId) {
        var uniqueIds = new HashSet<>(ids);
        var waterworksEntityList = waterworksRepository.findAllByIdInAndHomeId(uniqueIds, homeId);
        if (waterworksEntityList.size() != uniqueIds.size()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Waterworks not found");
        }
        waterworksRepository.deleteAll(waterworksEntityList);
    }

    private WaterworksEntity findInHome(Long id, Long homeId) {
        var waterworksEntity = waterworksRepository.findByIdAndHomeId(id, homeId);
        if (waterworksEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Waterworks not found");
        }
        return waterworksEntity;
    }

    private static void applyRequest(WaterworksEntity waterworksEntity, WaterworksRequest waterworksRequest) {
        waterworksEntity.setInvoiceNumber(waterworksRequest.getInvoiceNumber());
        waterworksEntity.setInvoiceDate(LocalDate.parse(waterworksRequest.getInvoiceDate(), DateTimeFormatter.ISO_DATE));
        waterworksEntity.setRawWaterUsage(waterworksRequest.getRawWaterUsage());
        waterworksEntity.setWaterUsage(waterworksRequest.getWaterUsage());
        waterworksEntity.setServiceCharge(waterworksRequest.getServiceCharge());
        waterworksEntity.setVat(waterworksRequest.getVat());
        waterworksEntity.setTotal(waterworksRequest.getTotal());
    }

    private WaterworksResponse generateWaterworksResponse(WaterworksEntity waterworksEntity, AccountEntity updateBy) {
        return WaterworksResponse.builder()
                .id(waterworksEntity.getId())
                .invoiceNumber(waterworksEntity.getInvoiceNumber())
                .invoiceDate(waterworksEntity.getInvoiceDate().toString())
                .rawWaterUsage(waterworksEntity.getRawWaterUsage())
                .waterUsage(waterworksEntity.getWaterUsage())
                .serviceCharge(waterworksEntity.getServiceCharge())
                .vat(waterworksEntity.getVat())
                .total(waterworksEntity.getTotal())
                .updateBy(updateBy.getUsername())
                .updateDate(waterworksEntity.getUpdateDate().toString())
                .build();
    }
}
