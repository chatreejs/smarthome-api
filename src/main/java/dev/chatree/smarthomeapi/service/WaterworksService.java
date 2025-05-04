package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.constant.ErrorMessage;
import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.WaterworksEntity;
import dev.chatree.smarthomeapi.model.waterworks.WaterworksResponse;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.WaterworksRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class WaterworksService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final WaterworksRepository waterworksRepository;

    public List<WaterworksResponse> getAllWaterworks(Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);
        var isHomeOwner = homeRepository.existsByIdAndAccountsId(homeId, account.getId());
        if (!isHomeOwner) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, ErrorMessage.ACCOUNT_NOT_ALLOW_TO_ACCESS_HOME);
        }

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