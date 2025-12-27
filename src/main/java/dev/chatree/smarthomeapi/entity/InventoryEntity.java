package dev.chatree.smarthomeapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "inventory")
public class InventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

    @Column(name = "brand")
    private String brand;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "max_quantity")
    private Double maxQuantity;

    @Column(name = "unit")
    private String unit;

    @Column(name = "restock_date")
    private LocalDate restockDate;

    @ManyToOne
    @JoinColumn(name = "create_by")
    private AccountEntity createBy;

    @ManyToOne
    @JoinColumn(name = "update_by")
    private AccountEntity updateBy;

    @CreationTimestamp
    @Column(name = "create_date")
    private LocalDateTime createDate;

    @UpdateTimestamp
    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @ManyToOne
    @JoinColumn(name = "home_id")
    private HomeEntity home;
}
