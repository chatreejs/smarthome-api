package dev.chatree.smarthomeapi.entity;

import dev.chatree.smarthomeapi.model.product.ProductType;
import jakarta.persistence.*;
import lombok.Data;

/**
 * A catalog item that users search when they add food or inventory. It belongs to no home.
 * Admins add and edit rows directly in the database. No endpoint changes them.
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
     * The max quantity that the add-inventory form fills in. Food ignores it.
     */
    @Column(name = "max_quantity")
    private Double maxQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private ProductType type;
}
