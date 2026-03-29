package com.lafl.quote.domain;

import java.util.List;
import java.util.Map;

public record OpsOverviewResponse(Map<String, Integer> counts,
                                  List<IssueSnapshot> activeIssues,
                                  List<RecentActivity> recentActivity) {
}
