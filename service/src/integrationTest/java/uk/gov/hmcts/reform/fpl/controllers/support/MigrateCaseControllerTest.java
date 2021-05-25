package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PBAPayment;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {
    MigrateCaseControllerTest() {
        super("migrate-case");
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Fpla3080 {
        String familyManNumber = "SA21C50024";
        String migrationId = "FPLA-3080";

        private DocumentReference supportingDocument = testDocumentReference("Correct c2 document");
        private DocumentReference c2Document = testDocumentReference("Incorrect c2 document");

        @Test
        void shouldReplaceC2DocumentWithSupportingDocument() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(buildAdditionalApplicationsBundle(YES));

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            C2DocumentBundle expectedBundle = C2DocumentBundle.builder().document(supportingDocument)
                .type(WITH_NOTICE)
                .build();

            assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue().getPbaPayment())
                .isEqualTo(additionalApplications.get(0).getValue().getPbaPayment());

            System.out.println("Here's what it is" + additionalApplications.get(0).getValue().getC2DocumentBundle());
//            assertThat(extractedCaseData.getAdditionalApplicationsBundle().get(0).getValue().getC2DocumentBundle())
//                .isEqualTo(additionalApplications.get(0).getValue().getC2DocumentBundle()).isEqualTo(expectedBundle);
            //assert supporting evidence is gone
            //assert document is changed
            //assert that no other additional application bundles are changed
        }

        @Test
        void shouldNotMigrateCaseIfMigrationIdIsIncorrect() {
            String incorrectMigrationId = "FPLA-1111";
            List<Element<AdditionalApplicationsBundle>> additionalApplications = wrapElements(buildAdditionalApplicationsBundle(YES));

            CaseDetails caseDetails = caseDetails(additionalApplications, incorrectMigrationId);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getAdditionalApplicationsBundle()).isEqualTo(additionalApplications);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsNoAdditionalApplications() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = Collections.emptyList();

            CaseDetails caseDetails = caseDetails(additionalApplications, migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage(String.format("Migration failed on case %s: Case has %s additional applications", familyManNumber,
                    additionalApplications.size()));
        }

        private CaseDetails caseDetails(List<Element<AdditionalApplicationsBundle>> additionalApplications,
                                        String migrationId) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .state(State.CASE_MANAGEMENT)
                .additionalApplicationsBundle(additionalApplications)
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private AdditionalApplicationsBundle buildAdditionalApplicationsBundle(YesNo usePbaPayment) {
                    return AdditionalApplicationsBundle.builder()
                        .pbaPayment(PBAPayment.builder().usePbaPayment(usePbaPayment.getValue()).build())
                        .c2DocumentBundle(C2DocumentBundle.builder()
                            .type(WITH_NOTICE)
                            .document(c2Document)
                            .supplementsBundle(new ArrayList<>())
                            .usePbaPayment(usePbaPayment.getValue())
                            .supportingEvidenceBundle(wrapElements(SupportingEvidenceBundle.builder()
                            .document(supportingDocument)
                                .build()))
                            .build())
                        .build();
        }

    }
}
