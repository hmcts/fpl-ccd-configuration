package uk.gov.hmcts.reform.fpl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.C6A;

@Getter
@RequiredArgsConstructor
public enum ProceedingType {
    NOTICE_OF_PROCEEDINGS_FOR_PARTIES(C6),
    NOTICE_OF_PROCEEDINGS_FOR_NON_PARTIES(C6A);

    private final DocmosisTemplates template;
}
