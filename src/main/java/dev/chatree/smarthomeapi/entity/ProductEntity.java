package dev.chatree.smarthomeapi.entity;

import dev.chatree.smarthomeapi.model.product.ProductType;
import jakarta.persistence.*;
import lombok.Data;

/**
 * A global catalog item (not scoped to a home) that users can search when adding food or inventory.
 * Rows are maintained directly in the database; there is no API to create or edit them.
 */
@Entity
@Data
@Table(name = "product", indexes = {
        @Index(name = "idx_product_type_barcode", columnList = "type, barcode")
})
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "brand")
    private String brand;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "unit")
    private String unit;

    /**
     * Default max quantity suggested for inventory items. Unused for food.
     */
    @Column(name = "max_quantity")
    private Double maxQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private ProductType type;
}
