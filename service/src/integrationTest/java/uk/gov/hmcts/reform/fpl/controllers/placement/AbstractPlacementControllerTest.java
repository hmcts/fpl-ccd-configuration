package uk.gov.hmcts.reform.fpl.controllers.placement;

import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument;
import uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.ANNEX_B;
import static uk.gov.hmcts.reform.fpl.model.PlacementConfidentialDocument.Type.GUARDIANS_REPORT;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.BIRTH_ADOPTION_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.model.PlacementSupportingDocument.Type.STATEMENT_OF_FACTS;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRespondent;

class AbstractPlacementControllerTest extends AbstractCallbackTest {

    final Element<Child> child1 = testChild("Alex", "Brown");
    final Element<Child> child2 = testChild("George", "White");

    final Element<Respondent> respondent1 = testRespondent("Emma", "Green", "mother");
    final Element<Respondent> respondent2 = testRespondent("Adam", "Green", "father");

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
}
