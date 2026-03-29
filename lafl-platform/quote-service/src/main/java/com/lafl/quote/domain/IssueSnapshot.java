package com.lafl.quote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issue_snapshots", schema = "quote_service")
public class IssueSnapshot {

    @Id
    @Column(length = 64)
    private String reference;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int issueCount;

    @Column(nullable = false, length = 1024)
    private String summary;

    protected IssueSnapshot() {
    }

    public IssueSnapshot(String reference, String status, int issueCount, String summary) {
        this.reference = reference;
        this.status = status;
        this.issueCount = issueCount;
        this.summary = summary;
    }

    public String getReference() {
        return reference;
    }

    public String getStatus() {
        return status;
    }

    public int getIssueCount() {
        return issueCount;
    }

    public String getSummary() {
        return summary;
    }
}
