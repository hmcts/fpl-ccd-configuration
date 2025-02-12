package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(MessageJudgeController.class)
@OverrideAutoConfiguration(enabled = true)
class MessageJudgeControllerAboutToStartTest extends AbstractCallbackTest {
    @MockBean
    ManageDocumentService manageDocumentService;
    @MockBean
    private UserService userService;

    MessageJudgeControllerAboutToStartTest() {
        super("message-judge");
    }

    @Test
    void shouldInitialiseCaseFieldsForApplicationsAndDocTypes() {
        UUID c2DocumentBundleId = randomUUID();
        UUID otherApplicationsBundleId = randomUUID();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles = List.of(
            element(AdditionalApplicationsBundle.builder()
                .c2DocumentBundle(C2DocumentBundle.builder()
                    .id(c2DocumentBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .build())
                .otherApplicationsBundle(OtherApplicationsBundle.builder()
                    .id(otherApplicationsBundleId)
                    .uploadedDateTime("1 January 2021, 12:00pm")
                    .applicationType(C1_APPOINTMENT_OF_A_GUARDIAN)
                   .build())
                .build()
        ));

        CaseData caseData = CaseData.builder()
            .id(1111L)
            .additionalApplicationsBundle(additionalApplicationsBundles)
            .build();

        DynamicListElement documentTypeElement1 = DynamicListElement.builder()
            .code("SKELETON_ARGUMENTS")
            .label("Skeleton arguments")
            .build();

        DynamicListElement documentTypeElement2 = DynamicListElement.builder()
            .code("COURT_BUNDLE")
            .label("Court Bundles")
            .build();

        DynamicList docTypeDynamicList = DynamicList.builder()
            .listItems(List.of(documentTypeElement1, documentTypeElement2)).build();

        when(manageDocumentService.buildExistingDocumentTypeDynamicList(any())).thenReturn(docTypeDynamicList);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList builtDynamicList = mapper.convertValue(
            response.getData().get("documentTypesDynamicList"), DynamicList.class
        );

        assertThat(response.getData().get("hasAdditionalApplications")).isEqualTo(YES.getValue());
        assertThat(builtDynamicList).isEqualTo(docTypeDynamicList);
    }

    @Test
    void shouldPrePopulateRecipientIfCaseInitiatedByJudge() {
        CaseData caseData = CaseData.builder().build();

        when(userService.getUserEmail()).thenReturn("sender@mail.com");
        when(userService.hasUserRole(UserRole.JUDICIARY)).thenReturn(true);
        when(manageDocumentService.getUploaderType(any())).thenReturn(DocumentUploaderType.HMCTS);
        Map<String, Object> caseDetails = postAboutToStartEvent(caseData, UserRole.JUDICIARY.getRoleName()).getData();

        assertThat(caseDetails.get("judicialMessageMetaData")).extracting("sender", "recipient")
            .containsExactly("sender@mail.com", EMPTY);
    }
}
