package com.qvorum.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A water or gas meter installed in an apartment. */
@Entity
@Table(name = "meters")
public class Meter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    /** STRING stores the enum name ("COLD_WATER"), not its position, so reordering is safe. */
    @Enumerated(EnumType.STRING)
    private MeterType type;

    private String serialNumber;

    protected Meter() {
    }

    public Meter(Apartment apartment, MeterType type, String serialNumber) {
        this.apartment = apartment;
        this.type = type;
        this.serialNumber = serialNumber;
    }

    public Long getId() { return id; }
    public Apartment getApartment() { return apartment; }
    public MeterType getType() { return type; }
    public String getSerialNumber() { return serialNumber; }
}
