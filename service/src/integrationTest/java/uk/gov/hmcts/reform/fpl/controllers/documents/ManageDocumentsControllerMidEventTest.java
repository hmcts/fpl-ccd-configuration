package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.ADDITIONAL_APPLICATIONS_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C12_WARRANT_TO_ASSIST_PERSON;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerMidEventTest extends AbstractCallbackTest {

    private static final String USER_ROLES = "caseworker-publiclaw-courtadmin";

    ManageDocumentsControllerMidEventTest() {
        super("manage-documents");
    }

    @Test
    void shouldInitialiseFurtherEvidenceCollection() {
        UUID selectHearingId = randomUUID();
        LocalDateTime today = now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectHearingId, selectedHearingBooking)
        );

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(selectHearingId)
            .hearingFurtherEvidenceDocuments(List.of(
                element(selectHearingId, HearingFurtherEvidenceBundle.builder()
                    .supportingEvidenceBundle(furtherEvidenceBundle)
                    .build())
            ))
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            caseData, "initialise-manage-document-collections", USER_ROLES
        );

        CaseData extractedCaseData = extractCaseData(response);

        DynamicList expectedDynamicList = asDynamicList(hearingBookings, selectHearingId, HearingBooking::toLabel);

        DynamicList hearingList = mapper.convertValue(
            response.getData().get(MANAGE_DOCUMENTS_HEARING_LIST_KEY), DynamicList.class
        );

        assertThat(hearingList).isEqualTo(expectedDynamicList);

        assertThat(response.getData().get(MANAGE_DOCUMENTS_HEARING_LABEL_KEY))
            .isEqualTo(selectedHearingBooking.toLabel());

        assertThat(extractedCaseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(furtherEvidenceBundle);

        assertThat(extractedCaseData.getManageDocument()).isEqualTo(ManageDocument.builder()
            .type(FURTHER_EVIDENCE_DOCUMENTS)
            .relatedToHearing("Yes")
            .hasHearings("Yes")
            .hasC2s("No")
            .build());
    }

    @Test
    void shouldInitialiseCorrespondenceCollection() {
        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .correspondenceDocuments(correspondenceDocuments)
            .manageDocument(buildManagementDocument(CORRESPONDENCE))
            .build();

        CaseData extractedCaseData = extractCaseData(postMidEvent(caseData, "initialise-manage-document-collections"));

        assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(correspondenceDocuments);

        assertThat(extractedCaseData.getManageDocument()).isEqualTo(ManageDocument.builder()
            .type(CORRESPONDENCE)
            .hasHearings("No")
            .hasC2s("No")
            .build());
    }

    @Test
    void shouldInitialiseC2SupportingDocumentsWhenTheSelectedC2IsInC2DocumentsBundle() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(c2EvidenceDocuments)),
            element(buildC2DocumentBundle(today.plusDays(2))));

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedC2DocumentId)
            .build();

        CaseData extractedCaseData = extractCaseData(postMidEvent(
            caseData, "initialise-manage-document-collections", USER_ROLES
        ));

        assertThat(extractedCaseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);

        assertThat(extractedCaseData.getManageDocument()).isEqualTo(ManageDocument.builder()
            .type(ADDITIONAL_APPLICATIONS_DOCUMENTS)
            .hasHearings("No")
            .hasC2s("Yes")
            .build());
    }

    @Test
    void shouldInitialiseC2SupportingDocumentsWhenSelectedC2ExistsInAdditionalApplications() {
        UUID selectedBundleId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(buildC2DocumentBundle(today.plusDays(2))));

        C2DocumentBundle selectedC2DocumentBundle = C2DocumentBundle.builder()
            .id(selectedBundleId).supportingEvidenceBundle(c2EvidenceDocuments)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(today.plusDays(1), DATE_TIME))
            .build();

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(randomUUID()).applicationType(C12_WARRANT_TO_ASSIST_PERSON)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(today.plusDays(1), DATE_TIME))
            .build();

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2DocumentBundle)
                .otherApplicationsBundle(otherApplicationsBundle).build()))
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedBundleId)
            .build();

        CaseData extractedCaseData = extractCaseData(postMidEvent(
            caseData, "initialise-manage-document-collections", USER_ROLES
        ));

        assertThat(extractedCaseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);
    }

    @Test
    void shouldInitialiseOtherApplicationSupportingDocumentsWhenSelectedApplicationExistsInAdditionalApplications() {
        UUID selectedBundleId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(buildC2DocumentBundle(today.plusDays(2))));

        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(today.plusDays(1));

        OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
            .id(selectedBundleId).applicationType(C12_WARRANT_TO_ASSIST_PERSON)
            .supportingEvidenceBundle(c2EvidenceDocuments)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(today.plusDays(1), DATE_TIME))
            .build();

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(selectedC2DocumentBundle)
                .otherApplicationsBundle(otherApplicationsBundle).build()))
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .manageDocumentsSupportingC2List(selectedBundleId)
            .build();

        CaseData extractedCaseData = extractCaseData(postMidEvent(
            caseData, "initialise-manage-document-collections", USER_ROLES
        ));

        assertThat(extractedCaseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);
    }

    @Test
    void shouldReturnErrorWhenNoC2sOnCaseAndUserSelectsC2SupportingDocs() {
        CaseData caseData = CaseData.builder()
            .manageDocument(buildManagementDocument(ADDITIONAL_APPLICATIONS_DOCUMENTS))
            .build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections");

        assertThat(callbackResponse.getErrors()).containsExactly(
            "There are no additional applications to associate supporting documents with");
    }

    @Test
    void shouldReturnValidationErrorsIfSupportingEvidenceDateTimeReceivedOnFurtherEvidenceIsInTheFuture() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(List.of(
                element(SupportingEvidenceBundle.builder().dateTimeReceived(futureDate).build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "validate-supporting-evidence");

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).containsExactly("Date received cannot be in the future");
    }

    @Test
    void shouldReturnNoValidationErrorsIfSupportingEvidenceDateTimeReceivedOnFurtherEvidenceIsInThePast() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(2);
        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(List.of(
                element(SupportingEvidenceBundle.builder().dateTimeReceived(pastDate).build())
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "validate-supporting-evidence");

        assertThat(response.getErrors()).isEmpty();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type) {
        return ManageDocument.builder().type(type).build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .uploadedBy("HMCTS")
            .type(GUARDIAN_REPORTS)
            .build());
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder()
            .id(UUID.randomUUID())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(dateTime, DATE_TIME))
            .build();
    }

    private C2DocumentBundle buildC2DocumentBundle(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {
        return buildC2DocumentBundle(now()).toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build();
    }
}
