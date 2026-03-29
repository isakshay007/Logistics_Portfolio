package com.lafl.quote.repository;

import com.lafl.quote.domain.QuoteRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRecordRepository extends JpaRepository<QuoteRecord, String> {

    List<QuoteRecord> findTop10ByOrderByCreatedAtDesc();
}
