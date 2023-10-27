package com.example.wealthwise_api.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expenses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idExpenses;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate createdDate;

    @Column(nullable = false)
    private String currency;

    @ManyToOne
    @JoinColumn(name = "idUser",nullable = false)
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "idCategory",nullable = false)
    private Categories category;

    public Expenses(double amount, LocalDate createdDate, String currency, UserEntity userEntity, Categories category) {
        this.amount = amount;
        this.createdDate = createdDate;
        this.currency = currency;
        this.userEntity = userEntity;
        this.category = category;
    }

    public Expenses() {
    }

    public long getIdExpenses() {
        return idExpenses;
    }

    public void setIdExpenses(long idExpenses) {
        this.idExpenses = idExpenses;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public Categories getCategory() {
        return category;
    }

    public void setCategory(Categories category) {
        this.category = category;
    }
}
