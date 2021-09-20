package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.TranslatedItemNotifyData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;


@ContextConfiguration(classes = {TranslatedItemEmailContentProvider.class})
class TranslatedItemEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String CHILD_LAST_NAME = "ChildLastName";
    private static final String DOC_TYPE = "docType";

    @Autowired
    private TranslatedItemEmailContentProvider underTest;

    @MockBean
    private CourtService courtService;

    @MockBean
    private EmailNotificationHelper helper;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(anyList())).thenReturn(CHILD_LAST_NAME);
        when(courtService.getCourtName(any())).thenReturn("Family Court");
    }

    @Test
    void getNotifyData() {
        TranslatedItemNotifyData actual = underTest.getNotifyData(CaseData.builder()
            .id(12344556565L)
            .familyManCaseNumber("familyMan123")
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder().lastName("Love").build())
                .build())))
            .hearingDetails(List.of(element(HearingBooking.builder()
                .startDate(LocalDateTime.of(2019, 12, 15, 12, 4))
                .build())))
            .caseLocalAuthority("test1")
            .build(), null, DOC_TYPE);

        assertThat(actual).isEqualTo(TranslatedItemNotifyData.builder()
            .childLastName(CHILD_LAST_NAME)
            .docType(DOC_TYPE)
            .caseUrl("http://fake-url/cases/case-details/12344556565")
            .courtName("Family Court")
            .callout("^Love, familyMan123, hearing 15 Dec 2019")
            .build());
    }
}
