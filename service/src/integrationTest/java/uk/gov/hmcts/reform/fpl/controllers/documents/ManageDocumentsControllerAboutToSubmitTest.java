package uk.gov.hmcts.reform.fpl.controllers.documents;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.C2;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentType.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerAboutToSubmitTest extends AbstractControllerTest {

    private static final String USER = "HMCTS";
    private static final String[] USER_ROLES = {"caseworker-publiclaw-courtadmin", "caseworker-publiclaw-judiciary"};
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

    ManageDocumentsControllerAboutToSubmitTest() {
        super("manage-documents");
    }

    @BeforeEach
    void init() {
        givenCurrentUser(createUserDetailsWithHmctsRole());
    }

    @Test
    void shouldPopulateHearingFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();
        UUID hearingId = UUID.randomUUID();
        HearingBooking hearingBooking = buildFinalHearingBooking();

        CaseData caseData = CaseData.builder()
            .manageDocumentsHearingList(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .code(hearingId)
                    .build())
                .build())
            .hearingDetails(List.of(element(hearingId, hearingBooking)))
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(extractedCaseData.getHearingFurtherEvidenceDocuments()).first()
            .extracting(evidence -> evidence.getValue().getSupportingEvidenceBundle())
            .isEqualTo(furtherEvidenceBundle);

        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateFurtherDocumentsCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, NO.getValue()))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(extractedCaseData.getFurtherEvidenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateCorrespondenceEvidenceCollection() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(CORRESPONDENCE))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldPopulateC2DocumentBundleCollection() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        C2DocumentBundle selectedC2DocumentBundle = buildC2DocumentBundle(now().plusDays(2));
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle = buildSupportingEvidenceBundle(
            now().plusDays(3)
        );

        List<Element<C2DocumentBundle>> c2DocumentBundleList = List.of(
            element(buildC2DocumentBundle(now().plusDays(2))),
            element(selectedC2DocumentId, selectedC2DocumentBundle),
            element(buildC2DocumentBundle(now().plusDays(2)))
        );

        IncrementalInteger i = new IncrementalInteger(1);
        DynamicList expectedC2DocumentsDynamicList = asDynamicList(
            c2DocumentBundleList, selectedC2DocumentId, documentBundle -> documentBundle.toLabel(i.getAndIncrement())
        );

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundleList)
            .manageDocumentsSupportingC2List(expectedC2DocumentsDynamicList)
            .c2SupportingDocuments(supportingEvidenceBundle)
            .manageDocument(buildManagementDocument(C2))
            .build();

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseData, USER_ROLES));

        assertThat(extractedCaseData.getC2DocumentBundle()).first()
            .isEqualTo(c2DocumentBundleList.get(0));
        assertExpectedFieldsAreRemoved(extractedCaseData);
    }

    @Test
    void shouldDuplicateNonConfidentialCorrespondenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(CORRESPONDENCE))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> correspondenceDocumentsNC =
            mapper.convertValue(response.getData().get("correspondenceDocumentsNC"), new TypeReference<>() {});

        assertThat(extractedCaseData.getCorrespondenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(correspondenceDocumentsNC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    @Test
    void shouldDuplicateNonConfidentialFurtherEvidenceDocsInAnotherList() {
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = wrapElements(
            CONFIDENTIAL_BUNDLE, NON_CONFIDENTIAL_BUNDLE
        );

        CaseData caseData = CaseData.builder()
            .supportingEvidenceDocumentsTemp(furtherEvidenceBundle)
            .manageDocument(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseData, USER_ROLES);
        CaseData extractedCaseData = extractCaseData(response);

        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsNC =
            mapper.convertValue(response.getData().get("furtherEvidenceDocumentsNC"), new TypeReference<>() {});

        assertThat(extractedCaseData.getFurtherEvidenceDocuments()).isEqualTo(furtherEvidenceBundle);
        assertThat(furtherEvidenceDocumentsNC).isEqualTo(wrapElements(NON_CONFIDENTIAL_BUNDLE));
    }

    private HearingBooking buildFinalHearingBooking() {
        return HearingBooking.builder()
            .type(HearingType.FINAL)
            .startDate(LocalDateTime.now())
            .build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type) {
        return ManageDocument.builder().type(type).build();
    }

    private ManageDocument buildManagementDocument(ManageDocumentType type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
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

    private void assertExpectedFieldsAreRemoved(CaseData caseData) {
        assertThat(caseData.getSupportingEvidenceDocumentsTemp()).isEmpty();
        assertThat(caseData.getManageDocument()).isNull();
        assertThat(caseData.getC2SupportingDocuments()).isNull();
        assertThat(caseData.getManageDocumentsHearingList()).isNull();
        assertThat(caseData.getManageDocumentsSupportingC2List()).isNull();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private UserDetails createUserDetailsWithHmctsRole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Hudson")
            .forename("Steve")
            .email("steve.hudson@gov.uk")
            .roles(Arrays.asList(USER_ROLES))
            .build();
    }
}
