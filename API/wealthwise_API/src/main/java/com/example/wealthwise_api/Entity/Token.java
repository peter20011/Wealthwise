package com.example.wealthwise_api.Entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "tokenLists")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idToken;
    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Date exprationDate;

    public Token() {
    }

    public Token(String token, Date exprationDate) {
        this.token = token;
        this.exprationDate = exprationDate;
    }

    public Token(long idToken, String token, Date exprationDate) {
        this.idToken = idToken;
        this.token = token;
        this.exprationDate = exprationDate;
    }

    public long getIdToken() {
        return idToken;
    }

    public void setIdToken(long idToken) {
        this.idToken = idToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExprationDate() {
        return exprationDate;
    }

    public void setExprationDate(Date exprationDate) {
        this.exprationDate = exprationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token1 = (Token) o;
        return idToken == token1.idToken && Objects.equals(token, token1.token) && Objects.equals(exprationDate, token1.exprationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idToken, token, exprationDate);
    }
}