package uk.gov.hmcts.reform.fpl;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.User;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.model.User.user;


@SpringBootTest
@RunWith(SpringIntegrationSerenityRunner.class)
public class GeneratedOrder extends AbstractApiTest {

    private static final User SOLICITOR = user("kurt@swansea.gov.uk");
    private static final User COURT_ADMIN = user("hmcts-admin@example.com");

    @Test
    public void caseSubmission() {

        CaseData caseData = createCase("order-generation/case.json", SOLICITOR);
        callSubmitted(caseData);
    }

    public void callSubmitted(CaseData caseData) {
        caseService.submitCallback(caseData, COURT_ADMIN, "/callback/create-order/submitted");

        CaseData updatedCase = caseService.pollCase(caseData.getId(), COURT_ADMIN,
            aCase -> isNotEmpty(aCase.getDocumentsSentToParties()));

        List<String> letterIds = updatedCase.getDocumentsSentToParties().stream()
            .map(Element::getValue)
            .flatMap(x -> x.getDocumentsSentToParty().stream())
            .map(Element::getValue)
            .map(SentDocument::getLetterId)
            .collect(Collectors.toList());

        assertThat(updatedCase.getDocumentsSentToParties()).isNotEmpty();
        assertThat(letterIds).isNotEmpty().doesNotContainNull();
    }

}
