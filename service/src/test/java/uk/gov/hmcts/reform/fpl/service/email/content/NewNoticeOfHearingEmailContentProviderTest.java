package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {NewNoticeOfHearingEmailContentProvider.class, LookupTestConfig.class, HearingBookingService.class, FixedTimeConfiguration.class})
class NewNoticeOfHearingEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidHearingContent() {
        System.out.println("TEST--->?" + newNoticeOfHearingEmailContentProvider.buildNewNoticeOfHearingNotification(populatedCaseDetails(), caseDetails()).getHearingDetails());
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder()
            .id(1L)
            .data(Map.of("respondents1", wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Moley").build())
                .build())))
            .build();
    }
}
