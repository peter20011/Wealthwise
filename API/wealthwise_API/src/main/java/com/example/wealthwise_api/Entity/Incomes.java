package com.example.wealthwise_api.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "incomes")
public class Incomes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idIncomes;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false)
    private LocalDate createdDate;

    @ManyToOne
    @JoinColumn(name = "idUser",nullable = false)
    private UserEntity userEntity;

    public Incomes(double value, LocalDate createdDate, UserEntity userEntity) {
        this.value = value;
        this.createdDate = createdDate;
        this.userEntity = userEntity;
    }

    public Incomes() {
    }

    public long getIdIncomes() {
        return idIncomes;
    }

    public void setIdIncomes(long idIncomes) {
        this.idIncomes = idIncomes;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
}
