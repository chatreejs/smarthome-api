package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.AccountEntity;
import dev.chatree.smarthomeapi.entity.ElectricityEntity;
import dev.chatree.smarthomeapi.model.electricity.ElectricityRequest;
import dev.chatree.smarthomeapi.model.electricity.ElectricityResponse;
import dev.chatree.smarthomeapi.repository.AccountRepository;
import dev.chatree.smarthomeapi.repository.HomeRepository;
import dev.chatree.smarthomeapi.repository.ElectricityRepository;
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
public class ElectricityService {
    private final AccountRepository accountRepository;
    private final HomeRepository homeRepository;
    private final ElectricityRepository electricityRepository;

    public List<ElectricityResponse> getAllElectricity(Long homeId) {
        var electricityEntityList = electricityRepository.findAllByHomeId(homeId);
        var electricityResponseList = new ArrayList<ElectricityResponse>();

        for (var electricityEntity : electricityEntityList) {
            var updateBy = electricityEntity.getUpdateBy();
            if (updateBy == null) {
                updateBy = electricityEntity.getCreateBy();
            }
            var electricityResponse = generateElectricityResponse(electricityEntity, updateBy);
            electricityResponseList.add(electricityResponse);
        }
        return electricityResponseList;
    }

    public ElectricityResponse getElectricityById(Long id, Long homeId) {
        var electricityEntity = findInHome(id, homeId);

        var updateBy = electricityEntity.getUpdateBy();
        if (updateBy == null) {
            updateBy = electricityEntity.getCreateBy();
        }
        return generateElectricityResponse(electricityEntity, updateBy);
    }

    public void createElectricity(ElectricityRequest electricityRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);

        var homeEntity = homeRepository.findById(homeId).orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Home not found"));
        var electricityEntity = new ElectricityEntity();
        applyRequest(electricityEntity, electricityRequest);
        electricityEntity.setCreateBy(account);
        electricityEntity.setHome(homeEntity);

        electricityRepository.save(electricityEntity);
    }

    public void updateElectricity(Long id, ElectricityRequest electricityRequest, Long homeId, String subject) {
        var account = accountRepository.findBySubject(subject);

        var electricityEntity = findInHome(id, homeId);
        applyRequest(electricityEntity, electricityRequest);
        electricityEntity.setUpdateBy(account);

        electricityRepository.save(electricityEntity);
    }

    public void deleteElectricity(Long id, Long homeId) {
        var electricityEntity = findInHome(id, homeId);
        electricityRepository.delete(electricityEntity);
    }

    @Transactional
    public void deleteMultipleElectricity(List<Long> ids, Long homeId) {
        var uniqueIds = new HashSet<>(ids);
        var electricityEntityList = electricityRepository.findAllByIdInAndHomeId(uniqueIds, homeId);
        if (electricityEntityList.size() != uniqueIds.size()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Electricity not found");
        }
        electricityRepository.deleteAll(electricityEntityList);
    }

    private ElectricityEntity findInHome(Long id, Long homeId) {
        var electricityEntity = electricityRepository.findByIdAndHomeId(id, homeId);
        if (electricityEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Electricity not found");
        }
        return electricityEntity;
    }

    private static void applyRequest(ElectricityEntity electricityEntity, ElectricityRequest electricityRequest) {
        electricityEntity.setInvoiceNumber(electricityRequest.getInvoiceNumber());
        electricityEntity.setInvoiceDate(LocalDate.parse(electricityRequest.getInvoiceDate(), DateTimeFormatter.ISO_DATE));
        electricityEntity.setUsage(electricityRequest.getUsage());
        electricityEntity.setFtRate(electricityRequest.getFtRate());
        electricityEntity.setElectricityPrice(electricityRequest.getElectricityPrice());
        electricityEntity.setServiceCharge(electricityRequest.getServiceCharge());
        electricityEntity.setFt(electricityRequest.getFt());
        electricityEntity.setVat(electricityRequest.getVat());
        electricityEntity.setTotal(electricityRequest.getTotal());
    }

    private ElectricityResponse generateElectricityResponse(ElectricityEntity electricityEntity, AccountEntity updateBy) {
        return ElectricityResponse.builder()
                .id(electricityEntity.getId())
                .invoiceNumber(electricityEntity.getInvoiceNumber())
                .invoiceDate(electricityEntity.getInvoiceDate().toString())
                .usage(electricityEntity.getUsage())
                .ftRate(electricityEntity.getFtRate())
                .electricityPrice(electricityEntity.getElectricityPrice())
                .serviceCharge(electricityEntity.getServiceCharge())
                .ft(electricityEntity.getFt())
                .vat(electricityEntity.getVat())
                .total(electricityEntity.getTotal())
                .updateBy(updateBy.getUsername())
                .updateDate(electricityEntity.getUpdateDate().toString())
                .build();
    }
}
