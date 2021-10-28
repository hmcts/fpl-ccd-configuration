package uk.gov.hmcts.reform.fpl.service.cafcass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum CafcassRequestEmailContentProvider {
    ORDER("new order",
        "A new order for this case was uploaded to the Public Law Portal entitled %s",
        CafcassEmailConfiguration::getRecipientForOrder),

    COURT_BUNDLE("new court bundle",
        "A new court bundle for this case was uploaded to the Public Law Portal entitled %s",
        CafcassEmailConfiguration::getRecipientForCourtBundle);

    private final String type;
    private final String content;
    private final Function<CafcassEmailConfiguration, String> recipient;
}
