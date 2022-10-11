package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.ManageDocumentLA;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.MANAGE_DOCUMENT_LA_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentLAService.RESPONDENTS_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.HEARING_DOCUMENT_HEARING_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.SUPPORTING_C2_LIST_KEY;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(ManageDocumentsLAController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsLAControllerAboutToStartTest extends AbstractCallbackTest {

    private static final UUID UUID_1 = UUID.randomUUID();
    private static final String FILE_URL = "https://docuURL";
    private static final String ANOTHER_FILE_URL = "https://AnotherdocuURL";
    private static final String FILE_NAME = "mockChecklist.pdf";
    private static final String FILE_BINARY_URL = "http://dm-store:8080/documents/fakeUrl/binary";
    private static final String ANOTHER_USER = "siva@swansea.gov.uk";

    ManageDocumentsLAControllerAboutToStartTest() {
        super("manage-documents-la");
    }

    @Test
    void shouldBuildHearingDocumentsHearingListAndSupportingC2DocumentsList() {
        List<Element<HearingBooking>> hearingBookings = List.of(
            element(buildHearing(LocalDateTime.of(2020, 3, 15, 20, 20))),
            element(buildHearing(LocalDateTime.of(2020, 3, 16, 10, 10))));

        LocalDateTime now = LocalDateTime.now();
        Element<C2DocumentBundle> c2BundleElement = element(buildC2DocumentBundle(now.plusDays(2)));

        C2DocumentBundle c2Bundle = buildC2DocumentBundle(now.plusDays(1));

        OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder()
            .id(UUID.randomUUID()).applicationType(OtherApplicationType.C1_WITH_SUPPLEMENT)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(now.plusDays(1), DATE_TIME)).build();

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

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "furtherEvidenceDocumentsTEMP", List.of(),
                "additionalApplicationsBundle", wrapElements(AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2Bundle).otherApplicationsBundle(otherBundle).build()),
                "c2DocumentBundle", List.of(c2BundleElement),
                "hearingDetails", hearingBookings,
                "respondents1", respondents
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        DynamicList expectedHearingDynamicList = ElementUtils
            .asDynamicList(hearingBookings, null, HearingBooking::toLabel);

        DynamicList expectedC2DocumentsDynamicList = TestDataHelper.buildDynamicList(
            Pair.of(otherBundle.getId(), "C1, " + otherBundle.getUploadedDateTime()),
            Pair.of(c2BundleElement.getId(), "C2, " + c2BundleElement.getValue().getUploadedDateTime()),
            Pair.of(c2Bundle.getId(), "C2, " + c2Bundle.getUploadedDateTime())
        );

        DynamicList expectedRespondentStatementList = ElementUtils
            .asDynamicList(respondents, null,
                respondent -> respondent.getParty().getFullName());

        DynamicList hearingDocumentsHearingList =
            mapper.convertValue(response.getData().get(HEARING_DOCUMENT_HEARING_LIST_KEY), DynamicList.class);

        DynamicList c2DocumentDynamicList =
            mapper.convertValue(response.getData().get(SUPPORTING_C2_LIST_KEY), DynamicList.class);

        DynamicList respondentStatementList =
            mapper.convertValue(response.getData().get(RESPONDENTS_LIST_KEY), DynamicList.class);

        ManageDocumentLA actualManageDocument =
            mapper.convertValue(response.getData().get(MANAGE_DOCUMENT_LA_KEY), ManageDocumentLA.class);

        ManageDocumentLA expectedManageDocument = ManageDocumentLA.builder()
            .hasHearings(YES.getValue())
            .hasC2s(YES.getValue())
            .hasConfidentialAddress(NO.getValue())
            .hasPlacementNotices(NO.getValue())
            .build();

        assertThat(hearingDocumentsHearingList).isEqualTo(expectedHearingDynamicList);
        assertThat(c2DocumentDynamicList).isEqualTo(expectedC2DocumentsDynamicList);
        assertThat(actualManageDocument).isEqualTo(expectedManageDocument);
        assertThat(respondentStatementList).isEqualTo(expectedRespondentStatementList);
    }

    @Test
    void shouldInitialiseApplicationDocumentCollectionWithExistingDocument() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "applicationDocuments", List.of(element(UUID_1, ApplicationDocument.builder()
                    .documentType(SOCIAL_WORK_STATEMENT)
                    .document(DocumentReference.builder()
                        .binaryUrl(FILE_BINARY_URL)
                        .filename(FILE_NAME)
                        .url(ANOTHER_FILE_URL)
                        .build())
                    .uploadedBy(ANOTHER_USER)
                    .dateTimeUploaded(now())
                    .build()))
            ))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        List applicationDocuments =
            mapper.convertValue(response.getData().get("applicationDocuments"), List.class);
        Element e = mapper.convertValue(applicationDocuments.get(0), Element.class);
        ApplicationDocument ad = mapper.convertValue(e.getValue(), ApplicationDocument.class);
        assertThat(ad.getDocumentAcknowledge()).isEqualTo(List.of("ACK_RELATED_TO_CASE"));
    }

    private HearingBooking buildHearing(LocalDateTime startDate) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
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
