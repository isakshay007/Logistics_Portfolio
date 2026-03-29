INSERT INTO shipment_service.shipments
(reference, client, mode, status, progress, eta, current_location, destination, last_updated, summary, next_action, support_owner)
VALUES
('LAFL-24017', 'Northline Retail', 'Ocean + Road', 'On Schedule', 72, '2026-03-29', 'Rotterdam Distribution Hub', 'Hamburg, Germany', '2026-03-26T09:20:00.000Z',
 'Cargo is moving on plan with no open operational exceptions.',
 'Final truck dispatch to Hamburg on arrival slot confirmation.',
 'Ava Patel'),
('LAFL-98241', 'AeroPharm Labs', 'Air Freight', 'Customs Review', 54, '2026-03-30', 'JFK International Cargo Center', 'Toronto, Canada', '2026-03-26T06:40:00.000Z',
 'Shipment arrived, but release is blocked until customs documentation and cold-chain validation are cleared.',
 'Use this sample ID to review issue handling in the tracker UI and ops endpoint.',
 'Jordan Lee'),
('LAFL-77802', 'Everstone Interiors', 'Project Cargo', 'Booked', 18, '2026-04-04', 'Chennai Consolidation Center', 'Dubai, UAE', '2026-03-25T17:10:00.000Z',
 'Project cargo booking is confirmed and waiting on final crate approval before dispatch scheduling.',
 'Collect final packaging approval from the origin team.',
 'Mina Joseph')
ON CONFLICT (reference) DO NOTHING;

INSERT INTO shipment_service.tracking_events (shipment_id, label, location, timestamp)
SELECT s.id, v.label, v.location, v.timestamp
FROM shipment_service.shipments s
JOIN (
  VALUES
    ('LAFL-24017', 'Container cleared customs', 'Rotterdam Port', '2026-03-25T13:15:00.000Z'),
    ('LAFL-24017', 'Truck transfer assigned', 'Rotterdam Distribution Hub', '2026-03-26T09:20:00.000Z'),
    ('LAFL-98241', 'Flight landed', 'JFK International Cargo Center', '2026-03-26T02:05:00.000Z'),
    ('LAFL-98241', 'Documentation submitted', 'JFK International Cargo Center', '2026-03-26T06:40:00.000Z'),
    ('LAFL-77802', 'Cargo inspection complete', 'Chennai Consolidation Center', '2026-03-25T11:30:00.000Z'),
    ('LAFL-77802', 'Export booking confirmed', 'Chennai Consolidation Center', '2026-03-25T17:10:00.000Z')
) AS v(reference, label, location, timestamp)
  ON s.reference = v.reference
WHERE NOT EXISTS (
  SELECT 1
  FROM shipment_service.tracking_events e
  WHERE e.shipment_id = s.id
    AND e.label = v.label
    AND e.timestamp = v.timestamp
);

INSERT INTO shipment_service.tracking_issues (shipment_id, severity, title, detail, owner, action)
SELECT s.id, v.severity, v.title, v.detail, v.owner, v.action
FROM shipment_service.shipments s
JOIN (
  VALUES
    ('LAFL-98241', 'High', 'Customs hold on active ingredient certificate',
      'Border review flagged a mismatch between the uploaded certificate and the carton batch number.',
      'JFK Customs Desk', 'Upload corrected certificate before 3:00 PM ET to avoid a 24-hour delay.'),
    ('LAFL-98241', 'Medium', 'Temperature logger check required',
      'Cold-chain audit requested one manual validation before final release from the cargo center.',
      'Station Handling Team', 'Confirm logger reading and re-seal container after inspection.'),
    ('LAFL-77802', 'Low', 'Crating dimensions pending final approval',
      'Final crate measurements are awaiting shipper sign-off before pickup scheduling can be locked.',
      'Project Cargo Planning', 'Approve crate plan to release pickup slot confirmation.')
) AS v(reference, severity, title, detail, owner, action)
  ON s.reference = v.reference
WHERE NOT EXISTS (
  SELECT 1
  FROM shipment_service.tracking_issues i
  WHERE i.shipment_id = s.id
    AND i.title = v.title
);
