package dev.chatree.smarthomeapi.model.electricity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElectricityRequest {
    private String invoiceNumber;
    private String invoiceDate;
    private Integer usage;
    private BigDecimal ftRate;
    private BigDecimal electricityPrice;
    private BigDecimal serviceCharge;
    private BigDecimal ft;
    private BigDecimal vat;
    private BigDecimal total;
}
