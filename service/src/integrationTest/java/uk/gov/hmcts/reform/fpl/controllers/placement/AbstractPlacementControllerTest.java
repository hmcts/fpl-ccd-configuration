package uk.gov.hmcts.reform.fpl.controllers.placement;

import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.GUARDIANS_REPORT;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class AbstractPlacementControllerTest extends AbstractCallbackTest {

    final Element<Child> child1 = testChild("Alex", "Brown");
    final Element<Child> child2 = testChild("George", "White");

    final Element<Respondent> mother =
        element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Emma")
                .lastName("Green")
                .relationshipToChild("mother")
                .address(testAddress())
                .build())
            .build());

    final Element<Respondent> father = element(Respondent.builder()
        .party(RespondentParty.builder()
            .firstName("Adam")
            .lastName("Green")
            .relationshipToChild("father")
            .build())
        .solicitor(RespondentSolicitor.builder()
            .email("solicitor1@test.com")
            .build())
        .build());

    final PlacementConfidentialDocument defaultAnnexB = PlacementConfidentialDocument.builder()
        .type(ANNEX_B)
        .build();

    final PlacementSupportingDocument defaultBirthCertificate = PlacementSupportingDocument.builder()
        .type(BIRTH_ADOPTION_CERTIFICATE)
        .build();

    final PlacementSupportingDocument defaultStatementOfFacts = PlacementSupportingDocument.builder()
        .type(STATEMENT_OF_FACTS)
        .build();

    final PlacementConfidentialDocument annexB = PlacementConfidentialDocument.builder()
        .document(testDocumentReference())
        .type(ANNEX_B)
        .build();

    final PlacementSupportingDocument birthCertificate = PlacementSupportingDocument.builder()
        .document(testDocumentReference())
        .type(BIRTH_ADOPTION_CERTIFICATE)
        .build();

    final PlacementSupportingDocument statementOfFacts = PlacementSupportingDocument.builder()
        .document(testDocumentReference())
        .type(STATEMENT_OF_FACTS)
        .build();

    final PlacementConfidentialDocument guardiansReport = PlacementConfidentialDocument.builder()
        .document(testDocumentReference())
        .type(GUARDIANS_REPORT)
        .build();

    AbstractPlacementControllerTest() {
        super("placement");
    }

    String getPlacementTabUrl(Long caseId) {
        return caseUrl(caseId, "Placement");
    }
}
