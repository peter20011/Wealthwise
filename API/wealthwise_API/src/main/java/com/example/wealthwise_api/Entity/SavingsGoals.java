package com.example.wealthwise_api.Entity;


import jakarta.persistence.*;

@Entity
@Table(name = "savings_goals")
public class SavingsGoals {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idSavingsGoals;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private double targetAmount;
    @Column(nullable = false)
    private double currentAmount;
    @Column(nullable = false)
    private boolean status;
    @ManyToOne
    @JoinColumn(name = "idUser",nullable = false)
    private UserEntity userEntity;

    public SavingsGoals(String name, double targetAmount, double currentAmount, Boolean status, UserEntity userEntity) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.status = status;
        this.userEntity = userEntity;
    }

    public SavingsGoals() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
}
