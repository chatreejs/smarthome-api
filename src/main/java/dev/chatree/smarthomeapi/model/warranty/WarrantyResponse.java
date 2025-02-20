package dev.chatree.smarthomeapi.model.warranty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarrantyResponse {
    private long id;
    private String brand;
    private String productName;
    private String productNumber;
    private String model;
    private String serialNumber;
    private String purchaseDate;
    private String warrantyDate;
    private WarrantyStatus status;
}
