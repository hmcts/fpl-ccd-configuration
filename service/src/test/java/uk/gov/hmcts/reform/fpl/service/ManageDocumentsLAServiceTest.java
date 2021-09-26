package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.COURT_BUNDLE_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class ManageDocumentsLAServiceTest {

    private final ManageDocumentLAService manageDocumentLAService = new ManageDocumentLAService(new ObjectMapper());

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final LocalDateTime futureDate = time.now().plusDays(1);

    @Test
    void shouldPopulateFieldsWhenAllExpectedFieldsArePresentOnCaseData() {
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6)))
        );

        Element<C2DocumentBundle> c2Bundle = element(buildC2DocumentBundle(futureDate.plusDays(2)));
        C2DocumentBundle c2Application = buildC2DocumentBundle(futureDate.plusDays(3));
        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(randomUUID())
            .applicationType(OtherApplicationType.C19_WARRANT_TO_ASSISTANCE)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(futureDate, DATE_TIME))
                .build();

        List<Element<Respondent>> respondents = List.of(
            element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Sam")
                    .lastName("Wilson")
                    .relationshipToChild("Father")
                    .build())
                .build()),
            element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Megan")
                    .lastName("Hannah")
                    .relationshipToChild("Mother")
                    .build())
                .build()));

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(List.of(c2Bundle))
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(c2Application)
                .otherApplicationsBundle(otherApplicationsBundle).build()))
            .hearingDetails(hearingBookings)
            .respondents1(respondents)
            .build();

        DynamicList expectedHearingDynamicList = asDynamicList(hearingBookings, HearingBooking::toLabel);

        DynamicList expectedC2DocumentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(c2Application.getId(), "C2, " + c2Application.getUploadedDateTime()),
            Pair.of(c2Bundle.getId(), "C2, " + c2Bundle.getValue().getUploadedDateTime()),
            Pair.of(otherApplicationsBundle.getId(), "C19, " + otherApplicationsBundle.getUploadedDateTime())
        );

        DynamicList expectedRespondentStatementList = ElementUtils
            .asDynamicList(respondents, null,
                respondent -> respondent.getParty().getFullName());

        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .build();

        Map<String, Object> listAndLabel = manageDocumentLAService.baseEventData(caseData);

        assertThat(listAndLabel)
            .extracting(COURT_BUNDLE_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_LA_KEY,
                RESPONDENTS_LIST_KEY)
            .containsExactly(expectedHearingDynamicList, expectedC2DocumentsDynamicList, expectedManageDocument,
                expectedRespondentStatementList);
    }

    @Test
    void shouldNotPopulateHearingListOrC2DocumentListWhenHearingAndApplicationBundlesAreNotPresentOnCaseData() {
        CaseData caseData = CaseData.builder().build();
        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .build();

        Map<String, Object> listAndLabel = manageDocumentLAService.baseEventData(caseData);

        assertThat(listAndLabel)
            .extracting(COURT_BUNDLE_HEARING_LIST_KEY, SUPPORTING_C2_LIST_KEY, MANAGE_DOCUMENT_LA_KEY)
            .containsExactly(null, null, expectedManageDocument);
    }

    @Test
    void shouldNotPopulateRespondentStatementListWhenRespondentsAreNotPresentOnCaseData() {
        CaseData caseData = CaseData.builder().build();
        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(NO.getValue())
            .hasC2s(NO.getValue())
            .build();

        Map<String, Object> listAndLabel = manageDocumentLAService.baseEventData(caseData);

        assertThat(listAndLabel)
            .extracting(RESPONDENTS_LIST_KEY, MANAGE_DOCUMENT_LA_KEY)
            .containsExactly(null, expectedManageDocument);
    }

    @Test
    void shouldReturnNewCourtBundleListWithCourtBundleWhenNoExistingCourtBundlesPresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> courtBundle = List.of(element(CourtBundle.builder().build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(courtBundle)
            .courtBundleHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .build();

        List<HearingCourtBundle> results = unwrapElements(manageDocumentLAService.buildCourtBundleList(caseData));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .isNotEmpty()
            .isEqualTo(courtBundle);
    }

    @Test
    void shouldReturnEditedCourtBundleListWithCourtBundleWhenExistingCourtBundlePresentForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> currentCourtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<CourtBundle>> editedCourtBundle = List.of(element(createCourtBundleWithFile("New filename")));

        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            selectedHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(currentCourtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(editedCourtBundle)
            .courtBundleList(courtBundleList)
            .courtBundleHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .build();

        List<HearingCourtBundle> results = unwrapElements(manageDocumentLAService.buildCourtBundleList(caseData));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .isNotEmpty()
            .isEqualTo(editedCourtBundle);
    }

    @Test
    void shouldReturnAdditionalCourtBundleForSelectedHearing() {
        UUID selectedHearingId = randomUUID();
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
        );
        List<Element<CourtBundle>> currentCourtBundle = List.of(element(createCourtBundleWithFile("Current filename")));
        List<Element<CourtBundle>> newCourtBundle = new ArrayList<>(currentCourtBundle);
        newCourtBundle.add(element(createCourtBundleWithFile("New filename 1")));
        newCourtBundle.add(element(createCourtBundleWithFile("New filename 2")));

        List<Element<HearingCourtBundle>> courtBundleList = List.of(element(
            selectedHearingId,
            HearingCourtBundle.builder()
                .hearing(hearingBookings.get(0).getValue().toLabel())
                .courtBundle(currentCourtBundle)
                .build()));

        CaseData caseData = CaseData.builder()
            .manageDocumentsCourtBundle(newCourtBundle)
            .courtBundleList(courtBundleList)
            .courtBundleHearingList(selectedHearingId.toString())
            .hearingDetails(hearingBookings)
            .build();

        List<HearingCourtBundle> results = unwrapElements(manageDocumentLAService.buildCourtBundleList(caseData));
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getCourtBundle())
            .hasSize(3)
            .isEqualTo(newCourtBundle);
    }

    private CourtBundle createCourtBundleWithFile(String filename) {
        return CourtBundle.builder()
            .document(
                DocumentReference.builder()
                    .filename(filename)
                    .build())
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME))
            .build();
    }
}
