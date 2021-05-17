package uk.gov.hmcts.reform.fpl.model.documentview;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentViewType {
    HMCTS("HMCTS", true, true),
    LA("LA", false, true),
    NONCONFIDENTIAL("NC", false, false);


    private final String type;
    private final boolean includeConfidentialHMCTS;
    private final boolean includeConfidentialLA;
}
