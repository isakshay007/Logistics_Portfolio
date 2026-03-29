package com.lafl.quote.repository;

import com.lafl.quote.domain.ContactRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRecordRepository extends JpaRepository<ContactRecord, String> {

    List<ContactRecord> findTop10ByOrderByCreatedAtDesc();
}
