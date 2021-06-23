package uk.gov.hmcts.reform.fpl.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.rd.model.Organisation;

@Getter
@RequiredArgsConstructor
public class ManagingOrganisationRemoved {
    private final CaseData caseData;
    private final Organisation managingOrganisation;
}
