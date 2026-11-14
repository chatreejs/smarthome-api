package dev.chatree.smarthomeapi.model.waterworks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WaterworksResponse {
    private long id;
    private String invoiceNumber;
    private String invoiceDate;
    private Integer rawWaterUsage;
    private Integer waterUsage;
    private BigDecimal serviceCharge;
    private BigDecimal vat;
    private BigDecimal total;
    private String updateBy;
    private String updateDate;
}
