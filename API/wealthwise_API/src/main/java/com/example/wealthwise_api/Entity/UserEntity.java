package com.example.wealthwise_api.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;


@Entity
@Table(name = "users")
public class UserEntity  implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idUser;
    @Column(nullable = false,
            unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String surname;
    @Column(nullable = false)
    private String birthDay;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "userEntity",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<SavingsGoals> savingsGoalsSet;
    @OneToMany(mappedBy = "userEntity",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<Assets> assetsSet;

    @OneToMany(mappedBy = "userEntity",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<Incomes> incomesSet;

    @OneToMany(mappedBy = "userEntity",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private Set<Expenses> expensesSet;

    public UserEntity(long idUser, String email, String password, String username, String surname, String birthDay, Role role) {
        this.idUser = idUser;
        this.email = email;
        this.password = password;
        this.username = username;
        this.surname = surname;
        this.birthDay = birthDay;
        this.role = role;
    }

    public UserEntity(String email, String password, String username, String surname, String birthDay, Role role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.surname = surname;
        this.birthDay = birthDay;
        this.role = role;
    }

    public UserEntity() {
    }

    public long getIdUser() {
        return idUser;
    }

    public void setIdUser(long idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserEntity that)) return false;
        return getIdUser() == that.getIdUser() && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getSurname(), that.getSurname()) && Objects.equals(getBirthDay(), that.getBirthDay()) && getRole() == that.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdUser(), getEmail(), getPassword(), getUsername(), getSurname(), getBirthDay(), getRole());
    }
}