package dev.chatree.smarthomeapi.model.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private long id;
    private String name;
    private String brand;
    private String barcode;
    private String unit;
    private Double maxQuantity;
    private ProductType type;
}
