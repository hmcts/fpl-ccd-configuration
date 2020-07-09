package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.hearing.NewNoticeOfHearingTemplate;
import uk.gov.hmcts.reform.fpl.service.CaseDataExtractionService;
import uk.gov.hmcts.reform.fpl.service.HearingVenueLookUpService;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(
    classes = {NewNoticeOfHearingEmailContentProvider.class,
        CaseDataExtractionService.class,
        HearingVenueLookUpService.class,
        LookupTestConfig.class,
        FixedTimeConfiguration.class
    })
class NewNoticeOfHearingEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private NewNoticeOfHearingEmailContentProvider newNoticeOfHearingEmailContentProvider;

    @Autowired
    private Time time;

    private LocalDateTime futureDate;
    private static final byte[] APPLICATION_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static DocumentReference applicationDocument;

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
        applicationDocument = testDocumentReference();
        when(documentDownloadService.downloadDocument(applicationDocument.getBinaryUrl()))
            .thenReturn(APPLICATION_BINARY);
    }

    @Test
    void shouldReturnExpectedTemplateWithValidHearingContent() {

        LocalDateTime hearingDate = LocalDateTime.of(2020, 1, 1, 0, 0, 0);

        CaseDetails caseDetails = populatedCaseDetails(
            Map.of("applicationBinaryUrl", applicationDocument.getBinaryUrl()));

        assertThat(expectedMap()).isEqualTo(newNoticeOfHearingEmailContentProvider
            .buildNewNoticeOfHearingNotification(
                caseDetails, createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3)), DIGITAL_SERVICE));
    }

    private NewNoticeOfHearingTemplate expectedMap() {
        return NewNoticeOfHearingTemplate.builder()
            .hearingVenue("testVenue")
            .hearingType(HearingType.FINAL.getLabel())
            .build();
    }
}
