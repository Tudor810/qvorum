package com.qvorum.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A staircase ("scara") of the building, e.g. "A". */
@Entity
@Table(name = "staircases")
public class Staircase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "association_id")
    private Association association;

    private String name;

    protected Staircase() {
    }

    public Staircase(Association association, String name) {
        this.association = association;
        this.name = name;
    }

    public Long getId() { return id; }
    public Association getAssociation() { return association; }
    public String getName() { return name; }
}
