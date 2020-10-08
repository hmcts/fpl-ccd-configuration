package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingVenue;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
public class ManageHearingsServiceTest {

    @Mock
    private HearingVenueLookUpService hearingVenueLookUpService;

    @Mock
    private NoticeOfHearingGenerationService noticeOfHearingGenerationService;

    @Mock
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @Mock
    private UploadDocumentService uploadDocumentService;

    @InjectMocks
    private ManageHearingsService service;

    @BeforeEach
    void setUp() {
        given(uploadDocumentService.uploadPDF(any(), any())).willReturn(testDocument());
    }

    @Test
    void name() {
        CaseData caseData = CaseData.builder().build();

        HearingVenue expectedPreviousVenue = HearingVenue.builder().build();
        assertThat(service.getPreviousHearingVenue(caseData)).isEqualTo(expectedPreviousVenue);
    }

    //getPreviousHearingVenue tests

    //findHearingBooking tests

    //populateHearingCaseFields tests (low prio)

    //buildHearingBooking tests

    //addNoticeOfHearing tests

    //updateEditedHearingEntry tests

    //appendHearingBooking tests
}
