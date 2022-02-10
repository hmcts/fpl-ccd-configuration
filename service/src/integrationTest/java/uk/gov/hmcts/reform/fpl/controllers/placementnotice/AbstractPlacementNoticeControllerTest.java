package uk.gov.hmcts.reform.fpl.controllers.placementnotice;

import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

class AbstractPlacementNoticeControllerTest extends AbstractCallbackTest {

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

    AbstractPlacementNoticeControllerTest() {
        super("placementNotice");
    }

    String getPlacementTabUrl(Long caseId) {
        return caseUrl(caseId, "placementNotice");
    }
}
