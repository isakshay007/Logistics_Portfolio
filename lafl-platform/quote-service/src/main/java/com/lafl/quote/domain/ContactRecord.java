package com.lafl.quote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contacts", schema = "quote_service")
public class ContactRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false, length = 1024)
    private String message;

    @Column(nullable = false)
    private String createdAt;

    protected ContactRecord() {
    }

    public ContactRecord(String id, String name, String email, String company, String message, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.company = company;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCompany() {
        return company;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
