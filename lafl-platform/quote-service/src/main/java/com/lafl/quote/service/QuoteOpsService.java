package com.lafl.quote.service;

import com.lafl.quote.api.ContactCreateRequest;
import com.lafl.quote.api.QuoteCreateRequest;
import com.lafl.quote.domain.ContactRecord;
import com.lafl.quote.domain.IssueSnapshot;
import com.lafl.quote.domain.OpsOverviewResponse;
import com.lafl.quote.domain.QuoteRecord;
import com.lafl.quote.domain.RecentActivity;
import com.lafl.quote.event.QuoteEventPublisher;
import com.lafl.quote.repository.ContactRecordRepository;
import com.lafl.quote.repository.IssueSnapshotRepository;
import com.lafl.quote.repository.QuoteRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class QuoteOpsService {

    private final QuoteEventPublisher eventPublisher;
    private final QuoteRecordRepository quoteRecordRepository;
    private final ContactRecordRepository contactRecordRepository;
    private final IssueSnapshotRepository issueSnapshotRepository;

    public QuoteOpsService(QuoteEventPublisher eventPublisher,
                           QuoteRecordRepository quoteRecordRepository,
                           ContactRecordRepository contactRecordRepository,
                           IssueSnapshotRepository issueSnapshotRepository) {
        this.eventPublisher = eventPublisher;
        this.quoteRecordRepository = quoteRecordRepository;
        this.contactRecordRepository = contactRecordRepository;
        this.issueSnapshotRepository = issueSnapshotRepository;
    }

    @Transactional
    public QuoteRecord createQuote(QuoteCreateRequest request) {
        QuoteRecord record = new QuoteRecord(
            "quote-" + UUID.randomUUID().toString().substring(0, 8),
            request.company().trim(),
            request.contactName().trim(),
            request.email().trim().toLowerCase(),
            request.serviceType().trim(),
            request.origin().trim(),
            request.destination().trim(),
            request.shipmentType() == null ? "" : request.shipmentType().trim(),
            request.cargoDetails() == null ? "" : request.cargoDetails().trim(),
            "Pending Review",
            Instant.now().toString());

        QuoteRecord saved = quoteRecordRepository.save(record);
        eventPublisher.publishQuoteSubmitted(saved);
        return saved;
    }

    @Transactional
    public ContactRecord createContact(ContactCreateRequest request) {
        ContactRecord record = new ContactRecord(
            "msg-" + UUID.randomUUID().toString().substring(0, 8),
            request.name().trim(),
            request.email().trim().toLowerCase(),
            request.company() == null ? "" : request.company().trim(),
            request.message().trim(),
            Instant.now().toString());

        ContactRecord saved = contactRecordRepository.save(record);
        eventPublisher.publishContactSubmitted(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public OpsOverviewResponse getOverview() {
        List<RecentActivity> activity = new ArrayList<>();

        quoteRecordRepository.findTop10ByOrderByCreatedAtDesc().forEach(quote -> activity.add(new RecentActivity(
            "quote", quote.getId(), quote.getCompany() + " requested a quote", quote.getCreatedAt())));

        contactRecordRepository.findTop10ByOrderByCreatedAtDesc().forEach(contact -> activity.add(new RecentActivity(
            "contact", contact.getId(), contact.getName() + " sent a contact request", contact.getCreatedAt())));

        activity.sort(Comparator.comparing(RecentActivity::createdAt).reversed());

        return new OpsOverviewResponse(
            Map.of(
                "contacts", Math.toIntExact(contactRecordRepository.count()),
                "quotes", Math.toIntExact(quoteRecordRepository.count()),
                "users", 0,
                "trackedShipments", 3
            ),
            issueSnapshotRepository.findByIssueCountGreaterThanOrderByReferenceAsc(0),
            activity.stream().limit(10).toList());
    }

    @Transactional(readOnly = true)
    public List<IssueSnapshot> getActiveIssues() {
        return issueSnapshotRepository.findByIssueCountGreaterThanOrderByReferenceAsc(0);
    }
}
