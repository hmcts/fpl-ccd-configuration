package uk.gov.hmcts.reform.fpl.controllers.documents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(ManageDocumentsControllerV2.class)
@OverrideAutoConfiguration(enabled = true)
class ManageDocumentsControllerV2MidEventTest extends AbstractCallbackTest {

    @MockBean
    private UserService userService;

    ManageDocumentsControllerV2MidEventTest() {
        super("manage-documentsv2");
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "SOLICITOR",

        "SOLICITORA", "SOLICITORB", "SOLICITORC", "SOLICITORD", "SOLICITORE", "SOLICITORF", "SOLICITORG", "SOLICITORH",
        "SOLICITORI", "SOLICITORJ",
        "CAFCASSSOLICITOR",
        "CHILDSOLICITORA", "CHILDSOLICITORB", "CHILDSOLICITORC", "CHILDSOLICITORD", "CHILDSOLICITORE",
        "CHILDSOLICITORF", "CHILDSOLICITORG", "CHILDSOLICITORH", "CHILDSOLICITORI", "CHILDSOLICITORJ",
        "CHILDSOLICITORK", "CHILDSOLICITORL", "CHILDSOLICITORM", "CHILDSOLICITORN", "CHILDSOLICITORO",

        "BARRISTER"
    })
    void shouldPopulateAllowMarkDocumentConfidentialWithNo(CaseRole caseRole) {
        CaseData caseData = CaseData.builder().build();

        when(userService.getCaseRoles(any())).thenReturn(Set.of(caseRole));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection", caseRole.name());

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowMarkDocumentConfidential())
            .isEqualTo("NO");
    }

    @ParameterizedTest
    @EnumSource(value = CaseRole.class, names = {
        "LASHARED",
        "LASOLICITOR", "EPSMANAGING", "LAMANAGING", "LABARRISTER"
    })
    void shouldPopulateAllowMarkDocumentConfidentialWithYes(CaseRole caseRole) {
        CaseData caseData = CaseData.builder().build();

        when(userService.getCaseRoles(any())).thenReturn(Set.of(caseRole));

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection", caseRole.name());

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowMarkDocumentConfidential())
            .isEqualTo("YES");
    }

    @Test
    void shouldPopulateAllowMarkDocumentConfidentialWithYesForHMCTSUser() {
        CaseData caseData = CaseData.builder().build();

        when(userService.isHmctsUser()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData,
            "manage-document-action-selection");

        CaseData responseCaseData = extractCaseData(callbackResponse);
        assertThat(responseCaseData.getManageDocumentEventData().getAllowMarkDocumentConfidential())
            .isEqualTo("YES");
    }
}
