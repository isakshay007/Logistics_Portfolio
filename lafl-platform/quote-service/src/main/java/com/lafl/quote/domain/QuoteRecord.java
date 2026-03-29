package com.lafl.quote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quotes", schema = "quote_service")
public class QuoteRecord {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String serviceType;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private String shipmentType;

    @Column(nullable = false, length = 1024)
    private String cargoDetails;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String createdAt;

    protected QuoteRecord() {
    }

    public QuoteRecord(String id, String company, String contactName, String email,
                       String serviceType, String origin, String destination,
                       String shipmentType, String cargoDetails, String status, String createdAt) {
        this.id = id;
        this.company = company;
        this.contactName = contactName;
        this.email = email;
        this.serviceType = serviceType;
        this.origin = origin;
        this.destination = destination;
        this.shipmentType = shipmentType;
        this.cargoDetails = cargoDetails;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getCompany() {
        return company;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getShipmentType() {
        return shipmentType;
    }

    public String getCargoDetails() {
        return cargoDetails;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
