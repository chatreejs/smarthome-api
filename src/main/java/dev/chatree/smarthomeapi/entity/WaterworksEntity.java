package dev.chatree.smarthomeapi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "waterworks")
public class WaterworksEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name ="raw_water_usage")
    private Integer rawWaterUsage;

    @Column(name = "water_usage")
    private Integer waterUsage;

    @Column(name = "service_charge")
    private Double serviceCharge;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "total")
    private Double total;

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
