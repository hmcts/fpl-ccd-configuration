package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.NonMolestationOrderDocumentParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.Order.FL404A_NON_MOLESTATION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

@ExtendWith({MockitoExtension.class})
public class NonMolestationOrderDocumentParameterGeneratorTest {
    @Mock
    private OrderMessageGenerator orderMessageGenerator;
    @InjectMocks
    private NonMolestationOrderDocumentParameterGenerator underTest;

    @Test
    void accept() {
        assertThat(underTest.accept()).isEqualTo(FL404A_NON_MOLESTATION_ORDER);
    }

    @Test
    void template() {
        assertThat(underTest.template()).isEqualTo(DocmosisTemplates.NON_MOLESTATION_ORDER);
    }

    @Test
    void shouldBuildNonMolestationOrderDocumentParameter() {
        LocalDate dateOfBirth = LocalDate.of(2024, 06, 17);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("By consent");

        List<Element<Respondent>> respondents = wrapElementsWithUUIDs(Respondent.builder()
            .party(RespondentParty.builder()
                .contactDetailsHidden(NO.getValue())
                .firstName("First")
                .lastName("Last")
                .dateOfBirth(dateOfBirth)
                .address(Address.builder().addressLine1("First address line").country("UK").build())
                .build())
            .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                    .value(DynamicListElement.builder().code("1").label("Applicant, LastName, Second LA").build())
                    .build())
                .manageOrdersNonMolestationOrderRespondent(DynamicList.builder()
                    .value(DynamicListElement.builder().code(respondents.get(0).getId())
                        .label(respondents.get(0).getValue().getParty().getFullName()).build())
                    .build())
                .manageOrdersRecitalsAndPreamblesOptional("This is RecitalsAndPreambles")
                .manageOrdersNonMolestationOrder("This is details of NonMolestationOrder")
                .build())
            .build();

        DocmosisParameters actual = underTest.generate(caseData);

        NonMolestationOrderDocumentParameters expected = NonMolestationOrderDocumentParameters.builder()
            .orderTitle("Non-molestation order (FL404A)")
            .applicantName("Applicant, LastName")
            .respondents(List.of(DocmosisRespondent.builder()
                    .name("First Last")
                    .dateOfBirth("17 June 2024")
                    .address("First address line, UK")
                .build()))
            .recitalsOrPreamble("This is RecitalsAndPreambles")
            .orderByConsent("By consent")
            .courtOrder("This is details of NonMolestationOrder")
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldBuildNonMolestationOrderDocumentParameterWithConfidential() {
        LocalDate dateOfBirth = LocalDate.of(2024, 06, 17);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn("By consent");

        List<Element<Respondent>> respondents = wrapElementsWithUUIDs(Respondent.builder()
            .party(RespondentParty.builder()
                .contactDetailsHidden(YES.getValue())
                .firstName("First")
                .lastName("Last")
                .dateOfBirth(dateOfBirth)
                .address(Address.builder().addressLine1("First address line").country("UK").build())
                .build())
            .build());

        CaseData caseData = CaseData.builder()
            .respondents1(respondents)
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersNonMolestationOrderApplicant(DynamicList.builder()
                    .value(DynamicListElement.builder().code("1").label("Applicant, LastName, Second LA").build())
                    .build())
                .manageOrdersNonMolestationOrderRespondent(DynamicList.builder()
                    .value(DynamicListElement.builder().code(respondents.get(0).getId())
                        .label(respondents.get(0).getValue().getParty().getFullName()).build())
                    .build())
                .manageOrdersRecitalsAndPreamblesOptional("This is RecitalsAndPreambles")
                .manageOrdersNonMolestationOrder("This is details of NonMolestationOrder")
                .build())
            .build();

        DocmosisParameters actual = underTest.generate(caseData);

        NonMolestationOrderDocumentParameters expected = NonMolestationOrderDocumentParameters.builder()
            .orderTitle("Non-molestation order (FL404A)")
            .applicantName("Applicant, LastName")
            .respondents(List.of(DocmosisRespondent.builder()
                .name("First Last")
                .dateOfBirth("17 June 2024")
                .address("")
                .build()))
            .recitalsOrPreamble("This is RecitalsAndPreambles")
            .orderByConsent("By consent")
            .courtOrder("This is details of NonMolestationOrder")
            .build();

        assertThat(actual).isEqualTo(expected);
    }
}
