package com.lafl.quote.repository;

import com.lafl.quote.domain.IssueSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IssueSnapshotRepository extends JpaRepository<IssueSnapshot, String> {

    List<IssueSnapshot> findByIssueCountGreaterThanOrderByReferenceAsc(int issueCount);
}
