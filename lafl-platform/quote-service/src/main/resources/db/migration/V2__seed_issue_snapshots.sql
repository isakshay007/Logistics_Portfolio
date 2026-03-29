INSERT INTO quote_service.issue_snapshots (reference, status, issue_count, summary)
VALUES
('LAFL-98241', 'Customs Review', 2,
 'Shipment arrived, but release is blocked pending customs documentation.'),
('LAFL-77802', 'Booked', 1,
 'Project cargo awaiting final crate approval before dispatch scheduling.')
ON CONFLICT (reference) DO UPDATE
SET status = EXCLUDED.status,
    issue_count = EXCLUDED.issue_count,
    summary = EXCLUDED.summary;
