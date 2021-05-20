package uk.gov.hmcts.reform.fpl.controllers;

import org.assertj.core.api.Assertions;
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
                .firstOther(Other.builder().name("Ross").build())
                .additionalOthers(others)
                .build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseData);

        DynamicList actualDynamicList = mapper.convertValue(
            response.getData().get("temporaryApplicantsList"), DynamicList.class
        );

        Assertions.assertThat(actualDynamicList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Swansea local authority, Applicant",
                respondent1Party.getFullName() + ", Respondent 1",
                respondent2Party.getFullName() + ", Respondent 2",
                "Ross, Other to be given notice 1",
                "Bob, Other to be given notice 2",
                "Tom, Other to be given notice 3",
                "Someone else");
    }

}
