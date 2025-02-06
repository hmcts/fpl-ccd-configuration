package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsControllerAboutToStartTest extends AbstractCallbackTest {

    UploadAdditionalApplicationsControllerAboutToStartTest() {
        super("upload-additional-applications");
    }

    @Test
    void shouldPopulateApplicantsDynamicList() {
        RespondentParty respondent1Party = RespondentParty.builder().firstName("Joe").lastName("Blogs").build();
        RespondentParty respondent2Party = RespondentParty.builder().firstName("John").lastName("Smith").build();

        List<Element<Respondent>> respondents = List.of(
            element(Respondent.builder().party(respondent1Party).build()),
            element(Respondent.builder().party(respondent2Party).build()));

        List<Element<Other>> others = List.of(
            element(Other.builder().name("Bob").build()),
            element(Other.builder().name("Tom").build()));

        CaseData caseData = CaseData.builder()
            .caseLocalAuthorityName("Swansea local authority")
            .respondents1(respondents)
            .others(Others.builder()
                .additionalOthers(others)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList actualDynamicList = mapper.convertValue(
            response.getData().get("applicantsList"), DynamicList.class
        );

        DynamicList expectedDynamicList = DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder().code("applicant").label("Swansea local authority, Applicant").build(),
                DynamicListElement.builder().code(respondents.get(0).getId().toString())
                    .label(respondent1Party.getFullName() + ", Respondent 1").build(),
                DynamicListElement.builder().code(respondents.get(1).getId().toString())
                    .label(respondent2Party.getFullName() + ", Respondent 2").build(),
                DynamicListElement.builder().code(caseData.getOthersV2().get(0).getId().toString())
                    .label("Bob, Other to be given notice 1").build(),
                DynamicListElement.builder().code(caseData.getOthersV2().get(1).getId().toString())
                    .label("Tom, Other to be given notice 2").build(),
                DynamicListElement.builder().code("SOMEONE_ELSE").label("Someone else").build()))
            .value(DynamicListElement.EMPTY).build();

        assertThat(actualDynamicList).isEqualTo(expectedDynamicList);
    }

}
