package com.rishab.seat_reservation_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "shows")
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_paise", nullable = false)
    private long pricePaise;

    @Column(name = "per_user_limit", nullable = false)
    private int perUserLimit = 4;

    protected Show() {
    }

    public Show(String name, long pricePaise, int perUserLimit) {
        this.name = name;
        this.pricePaise = pricePaise;
        this.perUserLimit = perUserLimit;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPricePaise() {
        return pricePaise;
    }

    public int getPerUserLimit() {
        return perUserLimit;
    }
}