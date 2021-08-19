package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.legalcounsel.LegalCounsellorRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorAddedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.legalcounsel.LegalCounsellorRemovedNotifyTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_FORMATTED_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.GIRL;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ExtendWith(MockitoExtension.class)
class LegalCounsellorEmailContentProviderTest {

    private static final LegalCounsellor TEST_LEGAL_COUNCILLOR = LegalCounsellor.builder()
        .email("ted.baker@example.com")
        .firstName("Ted")
        .lastName("Baker")
        .build();

    @Mock
    private CaseUrlService caseUrlService;

    @Mock
    private EmailNotificationHelper helper;

    @InjectMocks
    private LegalCounsellorEmailContentProvider underTest;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .children1(List.of(testChild("Beatrice", "Langley", GIRL, LocalDate.now())))
            .caseName("testCaseName")
            .build();

        when(helper.getEldestChildLastName(anyList())).thenCallRealMethod();
    }

    @Test
    void buildLegalCounsellorAddedNotificationTemplate() {
        when(caseUrlService.getCaseUrl(TEST_CASE_ID_AS_LONG)).thenReturn("myUrl");

        LegalCounsellorAddedNotifyTemplate returnedTemplate =
            underTest.buildLegalCounsellorAddedNotificationTemplate(caseData);

        assertThat(returnedTemplate).isEqualTo(LegalCounsellorAddedNotifyTemplate.builder()
            .childLastName("Langley")
            .caseId(TEST_FORMATTED_CASE_ID)
            .caseUrl("myUrl")
            .build());
    }

    @Test
    void buildLegalCounsellorRemovedNotificationTemplate() {
        LegalCounsellorRemoved event = new LegalCounsellorRemoved(
            caseData, "Peter Taylor Solicitors Ltd", TEST_LEGAL_COUNCILLOR
        );
        LegalCounsellorRemovedNotifyTemplate returnedTemplate =
            underTest.buildLegalCounsellorRemovedNotificationTemplate(caseData, event);

        assertThat(returnedTemplate).isEqualTo(LegalCounsellorRemovedNotifyTemplate.builder()
            .caseName("testCaseName")
            .childLastName("Langley")
            .salutation("Dear Ted Baker")
            .clientFullName("Peter Taylor Solicitors Ltd")//Solicitor firm
            .ccdNumber(TEST_FORMATTED_CASE_ID)
            .build());
    }

}
