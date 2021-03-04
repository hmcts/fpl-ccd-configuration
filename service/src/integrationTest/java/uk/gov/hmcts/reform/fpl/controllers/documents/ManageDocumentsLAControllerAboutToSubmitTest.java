package uk.gov.hmcts.reform.fpl.controllers.documents;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.C2;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeListLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerAboutToSubmitTest extends AbstractCallbackTest {

    private static final String USER = "LA";
    private static final String USER_ROLES = "caseworker-publiclaw-solicitor";
    private static final SupportingEvidenceBundle NON_CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .dateTimeUploaded(LocalDateTime.now())
        .uploadedBy(USER)
        .name("test")
        .build();
    private static final SupportingEvidenceBundle CONFIDENTIAL_BUNDLE = SupportingEvidenceBundle.builder()
        .dateTimeUploaded(LocalDateTime.now())
        .uploadedBy(USER)
        .name("confidential test")
        .confidential(List.of("CONFIDENTIAL"))
        .build();

    ManageDocumentsLAControllerAboutToSubmitTest() {
        super("manage-documents-la");
    }

    @BeforeEach
    void init() {
        givenCurrentUser(buildUserDetailsWithLARole());
    }

    @Test
    void shouldPopulateHearingFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .manageDocumentsHearingList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build())
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentsRelatedToHearing(YES.getValue())
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(responseData.getHearingFurtherEvidenceDocuments()).first()
            .extracting(evidence -> evidence.getValue().getSupportingEvidenceBundle())
            .isEqualTo(furtherEvidenceBundle);

        assertExpectedFieldsAreRemoved(responseData);
    }

    @Test
    void shouldPopulateFurtherEvidenceDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(responseData.getFurtherEvidenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(responseData);
    }

    @Test
    void shouldPopulateCourtBundleCollection() {
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();
        CourtBundle courtBundle = buildCourtBundle();

        CaseData caseData = CaseData.builder()
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .courtBundleHearingList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build())
            .manageDocumentsCourtBundle(courtBundle)
            .manageDocumentLA(buildManagementDocument(COURT_BUNDLE))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(responseData.getCourtBundleList()).first()
            .isEqualTo(element(hearingId, courtBundle));
        assertExpectedFieldsAreRemoved(responseData);
    }

    @Test
    void shouldPopulateCorrespondenceEvidenceCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentLA(buildManagementDocument(CORRESPONDENCE))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(responseData.getCorrespondenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(responseData);
    }

    @Test
    void shouldDuplicateNonConfidentialCorrespondenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentLA(buildManagementDocument(CORRESPONDENCE))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> correspondenceDocumentsLANC =
            mapper.convertValue(response.getData().get("correspondenceDocumentsLANC"), new TypeReference<>() {
            });

        assertThat(extractedCaseData.getCorrespondenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertThat(correspondenceDocumentsLANC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    @Test
    void shouldDuplicateNonConfidentialFurtherEvidenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsLANC =
            mapper.convertValue(response.getData().get("furtherEvidenceDocumentsLANC"), new TypeReference<>() {
            });

        assertThat(extractedCaseData.getFurtherEvidenceDocumentsLA()).isEqualTo(furtherEvidenceBundle);
        assertThat(furtherEvidenceDocumentsLANC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    @Test
    void shouldPopulateC2DocumentBundleCollection() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(now().plusDays(2));
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle
            = buildSupportingEvidenceBundle(now().plusDays(3));

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(now().plusDays(2))),
            element(selectedC2DocumentId, selectedC2DocumentBundle),
            element(buildC2DocumentBundle(now().plusDays(2))));

        IncrementalInteger i = new IncrementalInteger(1);
        DynamicList c2DocumentsDynamicList = asDynamicList(c2DocumentBundleList, selectedC2DocumentId,
            documentBundle -> documentBundle.toLabel(i.getAndIncrement()));

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .manageDocumentsSupportingC2List(c2DocumentsDynamicList)
            .c2SupportingDocuments(supportingEvidenceBundle)
            .manageDocumentLA(buildManagementDocument(C2))
            .build();

        CaseData responseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(responseData.getC2DocumentBundle()).first()
            .isEqualTo(c2DocumentBundleList.get(0));
        assertExpectedFieldsAreRemoved(responseData);
    }

    private void assertExpectedFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEmpty();
        assertThat(caseData.getManageDocumentLA()).isNull();
        assertThat(caseData.getManageDocumentsCourtBundle()).isNull();
        assertThat(caseData.getC2SupportingDocuments()).isNull();
        assertThat(caseData.getManageDocumentsHearingList()).isNull();
        assertThat(caseData.getManageDocumentsSupportingC2List()).isNull();
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(LocalDateTime.now())
            .build();
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeListLA type) {
        return ManageDocumentLA.builder().type(type).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .dateTimeUploaded(LocalDateTime.now())
            .uploadedBy(USER)
            .name("test")
            .build());
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle(LocalDateTime localDateTime) {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .dateTimeUploaded(localDateTime)
            .uploadedBy(USER)
            .build());
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private CourtBundle buildCourtBundle() {
        return CourtBundle.builder()
            .hearing("string")
            .document(testDocumentReference())
            .uploadedBy("Kurt")
            .build();
    }

    private UserDetails buildUserDetailsWithLARole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Swansea")
            .forename("Kurt")
            .email("kurt@swansea.gov.uk")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build();
    }
}
