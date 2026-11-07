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
import java.time.LocalDateTime;

/** A homeowners association (one building). Its administrator is a User with ADMINISTRATOR. */
@Entity
@Table(name = "associations")
public class Association {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** LAZY: the administrator row is loaded only when getAdministrator() is used. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "administrator_id")
    private User administrator;

    private String name;
    private String address;
    private String fiscalCode;
    private String iban;

    /** Day of the month by which the maintenance bill must be paid. */
    private int dueDay;

    /** Residents can submit meter readings from readingStartDay to readingEndDay of each month. */
    private int readingStartDay;
    private int readingEndDay;

    /** Penalty per day of delay, in percent (0.200 = 0.2% per day). */
    private BigDecimal penaltyRate;

    /** Days after the due date before penalties start. */
    private int graceDays;

    /** Invoices above this amount (in bani) need the president's approval. */
    private long approvalThreshold;

    private LocalDateTime createdAt;

    /** Required by JPA, which creates the object first and then fills in the fields. */
    protected Association() {
    }

    public Association(User administrator, String name, String address, String fiscalCode, String iban,
            int dueDay, int readingStartDay, int readingEndDay, BigDecimal penaltyRate, int graceDays,
            long approvalThreshold) {
        this.administrator = administrator;
        this.name = name;
        this.address = address;
        this.fiscalCode = fiscalCode;
        this.iban = iban;
        this.dueDay = dueDay;
        this.readingStartDay = readingStartDay;
        this.readingEndDay = readingEndDay;
        this.penaltyRate = penaltyRate;
        this.graceDays = graceDays;
        this.approvalThreshold = approvalThreshold;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getAdministrator() { return administrator; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getFiscalCode() { return fiscalCode; }
    public String getIban() { return iban; }
    public int getDueDay() { return dueDay; }
    public int getReadingStartDay() { return readingStartDay; }
    public int getReadingEndDay() { return readingEndDay; }
    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public int getGraceDays() { return graceDays; }
    public long getApprovalThreshold() { return approvalThreshold; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
