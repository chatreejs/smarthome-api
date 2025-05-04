package dev.chatree.smarthomeapi.model.waterworks;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Double serviceCharge;
    private Double vat;
    private Double total;
    private String updateBy;
    private String updateDate;
}
