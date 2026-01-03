package com.parking.common.domain;

import com.parking.common.entity.Client;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain wrapper around JPA Client entity.
 * This class delegates business-facing accessors to the underlying JPA entity.
 * Use it when you want a domain object but keep the single source of truth in the entity.
 */
public class ClientDomain {

    private final Client entity;

    public ClientDomain(Client client) {
        this.entity = Objects.requireNonNull(client, "client entity must not be null");
    }

    public Client getEntity() {
        return entity;
    }

    public Long getId() {
        return entity.getId();
    }

    public void setId(Long id) {
        entity.setId(id);
    }

    public String getFullName() {
        return entity.getFullName();
    }

    public void setFullName(String fullName) {
        entity.setFullName(fullName);
    }

    public String getPhoneNumber() {
        return entity.getPhoneNumber();
    }

    public void setPhoneNumber(String phoneNumber) {
        entity.setPhoneNumber(phoneNumber);
    }

    public String getEmail() {
        return entity.getEmail();
    }

    public void setEmail(String email) {
        entity.setEmail(email);
    }

    public LocalDateTime getRegisteredAt() {
        return entity.getRegisteredAt();
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        entity.setRegisteredAt(registeredAt);
    }

    @Override
    public String toString() {
        return "ClientDomain{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", email='" + getEmail() + '\'' +
                '}';
    }
}
