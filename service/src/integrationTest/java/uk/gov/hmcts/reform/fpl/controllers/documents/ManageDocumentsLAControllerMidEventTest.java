package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.C2;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.CORRESPONDENCE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.ManageDocumentTypeLA.FURTHER_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.ManageDocumentService.MANAGE_DOCUMENTS_HEARING_LABEL_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(ManageDocumentsController.class)
@OverrideAutoConfiguration(enabled = true)
public class ManageDocumentsLAControllerMidEventTest extends AbstractControllerTest {
    ManageDocumentsLAControllerMidEventTest() {
        super("manage-documents-la");
    }

    @MockBean
    private IdamClient idamClient;

    @MockBean
    private RequestData requestData;

    @BeforeEach
    void before() {
        given(idamClient.getUserDetails(eq(USER_AUTH_TOKEN))).willReturn(createUserDetailsWithLARole());
        given(requestData.authorisation()).willReturn(USER_AUTH_TOKEN);
    }

    @Test
    void shouldInitialiseFurtherEvidenceCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));
        List<Element<SupportingEvidenceBundle>> furtherEvidenceBundle = buildSupportingEvidenceBundle();

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .manageDocumentsHearingList(selectedHearingId)
            .hearingFurtherEvidenceDocuments(List.of(element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                .supportingEvidenceBundle(furtherEvidenceBundle)
                .build())))
            .manageDocumentLA(buildManagementDocument(FURTHER_EVIDENCE_DOCUMENTS, YES.getValue()))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections");

        CaseData responseData = extractCaseData(callbackResponse);

        DynamicList expectedDynamicList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        DynamicList hearingList = mapper.convertValue(responseData.getManageDocumentsHearingList(), DynamicList.class);

        assertThat(hearingList).isEqualTo(expectedDynamicList);

        assertThat(callbackResponse.getData().get(MANAGE_DOCUMENTS_HEARING_LABEL_KEY))
            .isEqualTo(selectedHearingBooking.toLabel());

        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(furtherEvidenceBundle);
    }

    @Test
    void shouldInitialiseCorrespondenceCollection() {
        List<Element<SupportingEvidenceBundle>> correspondenceDocuments = buildSupportingEvidenceBundle();

        CaseData caseData = CaseData.builder()
            .correspondenceDocumentsLA(correspondenceDocuments)
            .manageDocumentLA(buildManagementDocument(CORRESPONDENCE)).build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections");

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getCorrespondenceDocumentsLA()).isEqualTo(correspondenceDocuments);
    }

    @Test
    void shouldInitialiseCourtBundleCollection() {
        UUID selectedHearingId = randomUUID();
        LocalDateTime today = LocalDateTime.now();
        HearingBooking selectedHearingBooking = createHearingBooking(today, today.plusDays(3));

        List<Element<CourtBundle>> courtBundleList = buildCourtBundleList(selectedHearingId);

        List<Element<HearingBooking>> hearingBookings = List.of(
            element(createHearingBooking(today.plusDays(5), today.plusDays(6))),
            element(createHearingBooking(today.plusDays(2), today.plusDays(3))),
            element(createHearingBooking(today, today.plusDays(1))),
            element(selectedHearingId, selectedHearingBooking));

        DynamicList hearingList = ElementUtils
            .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBookings)
            .courtBundleList(courtBundleList)
            .courtBundleHearingList(hearingList)
            .manageDocumentLA(buildManagementDocument(COURT_BUNDLE))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections");

        CaseData responseData = mapper.convertValue(callbackResponse.getData(), CaseData.class);
        assertThat(responseData.getCourtBundleList()).isEqualTo(courtBundleList);
    }

    @Test
    void shouldInitialiseC2SupportingDocuments() {
        UUID selectedC2DocumentId = UUID.randomUUID();
        LocalDateTime today = LocalDateTime.now();

        List<Element<SupportingEvidenceBundle>> c2EvidenceDocuments = buildSupportingEvidenceBundle();

        List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
            element(buildC2DocumentBundle(today.plusDays(2))),
            element(selectedC2DocumentId, buildC2DocumentBundle(c2EvidenceDocuments)),
            element(buildC2DocumentBundle(today.plusDays(2))));

        CaseData caseData = CaseData.builder()
            .c2DocumentBundle(c2DocumentBundle)
            .manageDocumentLA(buildManagementDocument(C2))
            .manageDocumentsSupportingC2List(selectedC2DocumentId)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "initialise-manage-document-collections");

        CaseData responseData = extractCaseData(callbackResponse);
        assertThat(responseData.getSupportingEvidenceDocumentsTemp()).isEqualTo(c2EvidenceDocuments);
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type) {
        return ManageDocumentLA.builder().type(type).build();
    }

    private ManageDocumentLA buildManagementDocument(ManageDocumentTypeLA type, String isRelatedToHearing) {
        return buildManagementDocument(type).toBuilder().relatedToHearing(isRelatedToHearing).build();
    }

    private List<Element<SupportingEvidenceBundle>> buildSupportingEvidenceBundle() {
        return wrapElements(SupportingEvidenceBundle.builder()
            .name("test")
            .uploadedBy("kurt.swansea@gov.uk")
            .build());
    }

    private List<Element<CourtBundle>> buildCourtBundleList(UUID hearingId) {
        return List.of(element(hearingId, CourtBundle.builder().hearing("test hearing").build()));
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle) {
        return buildC2DocumentBundle(now()).toBuilder()
            .supportingEvidenceBundle(supportingEvidenceBundle)
            .build();
    }

    private UserDetails createUserDetailsWithLARole() {
        return UserDetails.builder()
            .id(USER_ID)
            .surname("Swansea")
            .forename("Kurt")
            .email("kurt.swansea@gov.uk")
            .roles(List.of("caseworker-publiclaw-solicitor"))
            .build();
    }
}
