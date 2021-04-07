package uk.gov.hmcts.reform.fpl.model.noticeofchange;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;

import java.util.UUID;

@Data
@Builder
@Jacksonized
public class NoticeOfChangeRespondent {
    private UUID respondentId;
    private NoticeOfChangeAnswers noticeOfChangeAnswers;
    private OrganisationPolicy organisationPolicy;
}
