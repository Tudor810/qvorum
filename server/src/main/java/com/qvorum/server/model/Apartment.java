package com.qvorum.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** An apartment. Its meters point to it through Meter.apartment. */
@Entity
@Table(name = "apartments")
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "association_id")
    private Association association;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staircase_id")
    private Staircase staircase;

    /** Text, because numbers like "12A" exist. Unique within the association. */
    private String number;

    private int floor;

    /** Usable area in square meters. */
    private BigDecimal area;

    /** Share of the common property ("cota-parte"), used to split some invoices. */
    private BigDecimal share;

    protected Apartment() {
    }

    public Apartment(Association association, Staircase staircase, String number, int floor,
            BigDecimal area, BigDecimal share) {
        this.association = association;
        this.staircase = staircase;
        this.number = number;
        this.floor = floor;
        this.area = area;
        this.share = share;
    }

    public Long getId() { return id; }
    public Association getAssociation() { return association; }
    public Staircase getStaircase() { return staircase; }
    public String getNumber() { return number; }
    public int getFloor() { return floor; }
    public BigDecimal getArea() { return area; }
    public BigDecimal getShare() { return share; }
}
