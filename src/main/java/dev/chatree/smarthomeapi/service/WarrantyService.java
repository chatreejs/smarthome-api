package dev.chatree.smarthomeapi.service;

import dev.chatree.smarthomeapi.entity.WarrantyEntity;
import dev.chatree.smarthomeapi.model.warranty.WarrantyRequest;
import dev.chatree.smarthomeapi.model.warranty.WarrantyResponse;
import dev.chatree.smarthomeapi.model.warranty.WarrantyStatus;
import dev.chatree.smarthomeapi.repository.WarrantyRepository;
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
public class WarrantyService {

    private final WarrantyRepository warrantyRepository;

    public WarrantyService(WarrantyRepository warrantyRepository) {
        this.warrantyRepository = warrantyRepository;
    }

    public List<WarrantyResponse> getAllWarranty() {
        var warrantyEntityList = warrantyRepository.findAllByOrderByWarrantyDateAsc();
        var warrantyResponseList = new ArrayList<WarrantyResponse>();

        for (WarrantyEntity warrantyEntity : warrantyEntityList) {
            var warrantyResponse = generateWarrantyResponse(warrantyEntity);
            warrantyResponseList.add(warrantyResponse);
        }

        return warrantyResponseList;
    }

    public WarrantyResponse getWarrantyById(Long id) {
        var warrantyEntity = warrantyRepository.findById(id).orElse(null);
        if (warrantyEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Warranty not found");
        }

        return generateWarrantyResponse(warrantyEntity);
    }

    public void createWarranty(WarrantyRequest warrantyRequest) {
        var warrantyEntity = new WarrantyEntity();
        warrantyEntity.setBrand(warrantyRequest.getBrand());
        warrantyEntity.setProductName(warrantyRequest.getProductName());
        warrantyEntity.setProductNumber(warrantyRequest.getProductNumber());
        warrantyEntity.setModel(warrantyRequest.getModel());
        warrantyEntity.setSerialNumber(warrantyRequest.getSerialNumber());
        warrantyEntity.setPurchaseDate(LocalDate.parse(warrantyRequest.getPurchaseDate(), DateTimeFormatter.ISO_DATE));
        warrantyEntity.setWarrantyDate(LocalDate.parse(warrantyRequest.getWarrantyDate(), DateTimeFormatter.ISO_DATE));

        warrantyRepository.save(warrantyEntity);
    }

    public void updateWarranty(Long id, WarrantyRequest warrantyRequest) {
        var warrantyEntity = warrantyRepository.findById(id).orElse(null);
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

    public void deleteWarranty(Long id) {
        var warrantyEntity = warrantyRepository.findById(id).orElse(null);
        if (warrantyEntity == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Warranty not found");
        }
        warrantyRepository.delete(warrantyEntity);
    }

    public void deleteMultipleWarranty(List<Long> ids) {
        warrantyRepository.deleteAllById(ids);
    }

    private WarrantyResponse generateWarrantyResponse(WarrantyEntity warrantyEntity) {
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
