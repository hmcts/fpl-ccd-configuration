package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
class ManageDocumentsLAServiceTest {

    private final ManageDocumentLAService manageDocumentLAService = new ManageDocumentLAService(new ObjectMapper());

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final LocalDateTime futureDate = time.now().plusDays(1);

    @Test
    void shouldPopulateFieldsWhenHearingAndC2DocumentBundleDetailsArePresentOnCaseData() {
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6)))
        );

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(futureDate.plusDays(2)))
        );

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .hearingDetails(hearingBookings)
            .build();

        DynamicList expectedHearingDynamicList = asDynamicList(hearingBookings, HearingBooking::toLabel);

        AtomicInteger i = new AtomicInteger(1);
        DynamicList expectedC2DocumentsDynamicList = asDynamicList(c2DocumentBundle, null,
            documentBundle -> documentBundle.toLabel(i.getAndIncrement()));

        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .build();

        Map<String, Object> listAndLabel = manageDocumentLAService.initialiseManageDocumentLAEvent(caseData);

        assertThat(listAndLabel)
            .extracting(COURT_BUNDLE_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_LA_KEY)
            .containsExactly(expectedHearingDynamicList, expectedC2DocumentsDynamicList, expectedManageDocument);
    }

    @Test
    void shouldNotPopulateHearingListOrC2DocumentListWhenHearingAndC2DocumentsAreNotPresentOnCaseData() {
        CaseData caseData = CaseData.builder().build();
        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .build();

        Map<String, Object> listAndLabel = manageDocumentLAService.initialiseManageDocumentLAEvent(caseData);

        assertThat(listAndLabel)
            .extracting(COURT_BUNDLE_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_LA_KEY)
            .containsExactly(null, null, expectedManageDocument);
    }

    @Test
    void shouldReturnNewCourtBundleListWithCourtBundleWhenNoExistingCourtBundlesPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(CourtBundle.builder().hearing("Test hearing").build())
            .courtBundleHearingList(selectedHearingId.toString())
            .build();

        assertThat(manageDocumentLAService.buildCourtBundleList(caseData))
            .isEqualTo(List.of(element(selectedHearingId, caseData.getManageDocumentsCourtBundle())));
    }

    @Test
    void shouldReturnEditedCourtBundleListWithCourtBundleWhenExistingCourtBundlePresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<CourtBundle>> courtBundleList = new ArrayList<>();
        courtBundleList.add(element(selectedHearingId, CourtBundle.builder().hearing("Test hearing").build()));

        CourtBundle editedBundle = CourtBundle.builder().hearing("Edited hearing").build();
        CaseData caseData = CaseData.builder()
            .courtBundleList(courtBundleList)
            .manageDocumentsCourtBundle(editedBundle)
            .courtBundleHearingList(selectedHearingId.toString())
            .build();

        assertThat(unwrapElements(manageDocumentLAService.buildCourtBundleList(caseData)))
            .containsExactly(editedBundle);
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }
}
