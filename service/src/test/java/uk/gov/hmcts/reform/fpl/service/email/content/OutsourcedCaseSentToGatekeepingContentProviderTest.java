package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.submittedcase.OutsourcedCaseTemplate;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ContextConfiguration(classes = {OutsourcedCaseContentProvider.class})
class OutsourcedCaseSentToGatekeepingContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String THIRD_PARTY_ORG_NAME = "External org";
    private static final Long CASE_ID = 12345L;

    @Autowired
    OutsourcedCaseContentProvider outsourcedCaseContentProvider;

    @Test
    void shouldBuildNotifyLAOnOutsourcedCaseTemplateWithOutsourcedOrganisation() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .respondents1(List.of(ElementUtils.element(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Mark")
                    .lastName("Watson")
                    .build())
                .build())))
            .hearing(Hearing.builder()
                .timeFrame("Same day")
                .build())
            .orders(Orders.builder()
                .orderType(List.of(CARE_ORDER, EMERGENCY_PROTECTION_ORDER))
                .build())
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder()
                    .organisationName(THIRD_PARTY_ORG_NAME)
                    .build())
                .build())
            .build();

        OutsourcedCaseTemplate actualTemplate = outsourcedCaseContentProvider
            .buildNotifyLAOnOutsourcedCaseTemplate(caseData);

        assertThat(actualTemplate).isEqualTo(getExpectedTemplate());
    }

    private OutsourcedCaseTemplate getExpectedTemplate() {
        return OutsourcedCaseTemplate.builder()
            .caseUrl("http://fake-url/cases/case-details/12345")
            .firstRespondentName("Watson")
            .reference(String.valueOf(CASE_ID))
            .timeFrameValue("same day")
            .timeFramePresent(YES.getValue())
            .nonUrgentHearing(NO.getValue())
            .urgentHearing(YES.getValue())
            .ordersAndDirections(List.of(CARE_ORDER.getLabel(), EMERGENCY_PROTECTION_ORDER.getLabel()))
            .thirdParty(THIRD_PARTY_ORG_NAME)
            .build();
    }
}
