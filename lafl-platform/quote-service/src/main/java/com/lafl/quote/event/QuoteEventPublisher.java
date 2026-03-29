package com.lafl.quote.event;

import com.lafl.quote.domain.ContactRecord;
import com.lafl.quote.domain.QuoteRecord;

public interface QuoteEventPublisher {

    void publishQuoteSubmitted(QuoteRecord quoteRecord);

    void publishContactSubmitted(ContactRecord contactRecord);
}
