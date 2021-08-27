package uk.gov.hmcts.reform.fpl.controllers.documents;

import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_2_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

abstract class ManageDocumentsControllerSubmittedBaseTest extends AbstractCallbackTest {
    protected static final String CONFIDENTIAL_MARKER = "CONFIDENTIAL";
    protected static final String REP_1_EMAIL = "rep1@example.com";

    protected ManageDocumentsControllerSubmittedBaseTest(String eventName) {
        super(eventName);
    }

    protected CallbackRequest buildCallbackRequest(final String bundleName,
                                                   final boolean confidential) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(buildData(bundleName, buildEvidenceBundle(confidential)))
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .data(Map.of("dummy", "some dummy data"))
            .id(TEST_CASE_ID)
            .build();

        return CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();
    }

    protected static SupportingEvidenceBundle buildEvidenceBundle(
        boolean confidential) {
        SupportingEvidenceBundle.SupportingEvidenceBundleBuilder document
            = SupportingEvidenceBundle.builder()
            .name("dummy document")
            .uploadedBy("user who uploaded")
            .dateTimeUploaded(LocalDateTime.now())
            .document(TestDataHelper.testDocumentReference());

        if (confidential) {
            document.confidential(List.of(CONFIDENTIAL_MARKER));
        }

        return document.build();
    }

    protected Map<String, Object> buildData(String bundleName, SupportingEvidenceBundle supportingEvidenceBundle) {
        return Map.of(
            "localAuthorities", wrapElements(
                LocalAuthority.builder()
                    .designated(YES.getValue())
                    .email(LOCAL_AUTHORITY_1_INBOX)
                    .build(),
                LocalAuthority.builder()
                    .designated(NO.getValue())
                    .email(LOCAL_AUTHORITY_2_INBOX)
                    .build()),
            "caseLocalAuthority", LOCAL_AUTHORITY_1_CODE,
            "representatives", wrapElements(Representative.builder()
                .email(REP_1_EMAIL)
                .fullName("Representative Snow")
                .role(REPRESENTING_RESPONDENT_1)
                .servingPreferences(DIGITAL_SERVICE)
                .build()),
            "respondents1", createRespondents(),
            bundleName, wrapElements(supportingEvidenceBundle)
        );
    }
}
