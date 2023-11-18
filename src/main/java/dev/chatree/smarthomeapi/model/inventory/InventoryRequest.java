package dev.chatree.smarthomeapi.model.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {
    private String name;
    private Double quantity;
    private Double maxQuantity;
    private String unit;
}