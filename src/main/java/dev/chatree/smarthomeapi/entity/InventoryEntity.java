package dev.chatree.smarthomeapi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "Inventory")
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Quantity")
    private Double quantity;

    @Column(name = "MaxQuantity")
    private Double maxQuantity;

    @Column(name = "Unit")
    private String unit;

    @Column(name = "CreateDate")
    private Date createDate;

    @Column(name = "UpdateDate")
    private Date updateDate;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        this.createDate = now;
        this.updateDate = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateDate = new Date();
    }
}
