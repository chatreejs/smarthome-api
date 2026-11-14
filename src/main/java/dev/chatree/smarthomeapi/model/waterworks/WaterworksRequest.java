package dev.chatree.smarthomeapi.model.waterworks;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterworksRequest {
    private String invoiceNumber;
    private String invoiceDate;
    private Integer rawWaterUsage;
    private Integer waterUsage;
    private BigDecimal serviceCharge;
    private BigDecimal vat;
    private BigDecimal total;
}
