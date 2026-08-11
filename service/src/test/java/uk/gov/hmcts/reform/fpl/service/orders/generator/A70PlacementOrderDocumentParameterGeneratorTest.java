package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.service.CourtService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.A70PlacementOrderDocmosisParameters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.MARCH;
import static java.time.Month.NOVEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ExtendWith(MockitoExtension.class)
class A70PlacementOrderDocumentParameterGeneratorTest {

    private static final String UNKNOWN = "unknown";
    private static final Court COURT = Court.builder().build();

    @Mock
    private PlacementService placementService;
    @Mock
    private CourtService courtService;

    @InjectMocks
    private A70PlacementOrderDocumentParameterGenerator parameterGenerator;

    @Test
    void generate() {
        Element<Child> placementChild = element(
            Child.builder()
                .party(ChildParty.builder()
                    .firstName("Alex")
                    .lastName("White")
                    .fathersName("James White")
                    .mothersName("Laura White")
                    .dateOfBirth(LocalDate.of(2018, NOVEMBER, 20))
                    .gender(ChildGender.GIRL)
                    .build())
                .build()
        );
        Element<Placement> placementElement = element(Placement.builder()
            .childId(placementChild.getId())
            .placementUploadDateTime(LocalDateTime.of(2021, MARCH, 15, 13, 30))
            .build());
        CaseData caseData = CaseData.builder()
            .court(COURT)
            .children1(List.of(
                placementChild,
                testChild(),
                testChild()
            ))
            .localAuthorities(List.of(element(
                LocalAuthority.builder()
                    .name("LA name")
                    .address(Address.builder().addressLine1("First line").postcode("A1 BC2").build())
                    .designated(YesNo.YES.getValue())
                    .build()
            )))
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersChildPlacementApplication(
                        buildDynamicList(0, Pair.of(placementElement.getId(), "My placement")))
                    .manageOrdersBirthCertificateNumber("testBcNumber")
                    .manageOrdersBirthCertificateDate("testBcDateAsFreeText")
                    .manageOrdersBirthCertificateRegistrationDistrict("testBcRegDistrict")
                    .manageOrdersBirthCertificateRegistrationSubDistrict("testBcRegSubDistrict")
                    .manageOrdersBirthCertificateRegistrationCounty("testBcRegCounty")
                    .manageOrdersPlacementOrderOtherDetails("testPlacementOrderOtherDetails")
                    .build()
            )
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder().firstName("Test").lastName("Respondent").build())
                .build())))
            .build();
        when(placementService.getPlacementById(caseData, placementElement.getId()))
            .thenReturn(placementElement.getValue());
        when(placementService.getChildByPlacementId(caseData, placementElement.getId())).thenReturn(placementChild);
        when(courtService.isHighCourtCase(caseData)).thenReturn(false);

        A70PlacementOrderDocmosisParameters docmosisParameters = parameterGenerator.generate(caseData);

        assertThat(docmosisParameters.getOrderTitle()).isEqualTo("Placement Order");
        assertThat(docmosisParameters.getChildrenAct()).isEqualTo("Section 21 of the Adoption and Children Act 2002");
        assertThat(docmosisParameters.getLocalAuthorityName()).isEqualTo("LA name");
        assertThat(docmosisParameters.getLocalAuthorityAddress()).isEqualTo("First line\nA1 BC2");
        DocmosisChild docmosisChild = docmosisParameters.getChild();
        assertThat(docmosisChild.getName()).isEqualTo("Alex White");
        assertThat(docmosisChild.getFathersName()).isEqualTo("James White");
        assertThat(docmosisChild.getMothersName()).isEqualTo("Laura White");
        assertThat(docmosisChild.getDateOfBirth()).isEqualTo("20/11/2018");
        assertThat(docmosisChild.getGender()).isEqualTo("Female");
        assertThat(docmosisChild.getBirthCertificate().getNumber()).isEqualTo("testBcNumber");
        assertThat(docmosisChild.getBirthCertificate().getDate()).isEqualTo("testBcDateAsFreeText");
        assertThat(docmosisChild.getBirthCertificate().getRegistrationDistrict()).isEqualTo("testBcRegDistrict");
        assertThat(docmosisChild.getBirthCertificate().getRegistrationSubDistrict()).isEqualTo("testBcRegSubDistrict");
        assertThat(docmosisChild.getBirthCertificate().getRegistrationCounty()).isEqualTo("testBcRegCounty");
        assertThat(docmosisParameters.getApplicationDate()).isEqualTo("15/03/2021");
    }

    @Test
    void shouldGenerateDocmosisParametersWithDefaultValues() {
        Element<Child> placementChild = element(Child.builder()
            .party(ChildParty.builder().build())
            .build());
        Element<Placement> placementElement = element(Placement.builder()
            .childId(placementChild.getId())
            .placementUploadDateTime(LocalDateTime.of(2021, MARCH, 15, 13, 30))
            .build());
        CaseData caseData = CaseData.builder()
            .court(COURT)
            .children1(List.of(placementChild))
            .localAuthorities(List.of(element(
                LocalAuthority.builder()
                    .name("LA name")
                    .address(Address.builder().addressLine1("First line").postcode("A1 BC2").build())
                    .designated(YesNo.YES.getValue())
                    .build()
            )))
            .manageOrdersEventData(
                ManageOrdersEventData.builder()
                    .manageOrdersChildPlacementApplication(
                        buildDynamicList(0, Pair.of(placementElement.getId(), "My placement")))
                    .build()
            )
            .respondents1(List.of(element(Respondent.builder()
                .party(RespondentParty.builder().firstName("Test").lastName("Respondent").build())
                .build())))
            .build();
        when(placementService.getPlacementById(caseData, placementElement.getId()))
            .thenReturn(placementElement.getValue());
        when(placementService.getChildByPlacementId(caseData, placementElement.getId())).thenReturn(placementChild);
        when(courtService.isHighCourtCase(caseData)).thenReturn(false);

        A70PlacementOrderDocmosisParameters docmosisParameters = parameterGenerator.generate(caseData);

        assertThat(docmosisParameters.getChild().getName()).isEqualTo(UNKNOWN);
        assertThat(docmosisParameters.getChild().getFathersName()).isEqualTo(UNKNOWN);
        assertThat(docmosisParameters.getChild().getMothersName()).isEqualTo(UNKNOWN);
        assertThat(docmosisParameters.getChild().getDateOfBirth()).isEqualTo(UNKNOWN);
        assertThat(docmosisParameters.getChild().getGender()).isEqualTo(UNKNOWN);
    }

    @Test
    void template() {
        assertThat(parameterGenerator.template()).isEqualTo(DocmosisTemplates.A70);
    }

    @Test
    void shouldFormatRespondentNamesSingleRespondent() {
        RespondentParty respondentParty = RespondentParty.builder()
            .firstName("John")
            .lastName("Ross")
            .build();
        Respondent respondent = Respondent.builder().party(respondentParty).build();
        Element<Child> placementChild = element(Child.builder().party(ChildParty.builder().build()).build());
        Element<Placement> placementElement = element(Placement.builder()
            .childId(placementChild.getId())
            .placementUploadDateTime(LocalDate.now().atStartOfDay())
            .build());
        UUID placementId = placementElement.getId();
        DynamicList placementList = buildDynamicList(0, Pair.of(placementId, "My placement"));
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent)))
            .children1(List.of(placementChild))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersChildPlacementApplication(placementList)
                .build())
            .build();
        when(placementService.getPlacementById(caseData, placementId)).thenReturn(placementElement.getValue());
        when(placementService.getChildByPlacementId(caseData, placementId)).thenReturn(placementChild);
        when(courtService.isHighCourtCase(caseData)).thenReturn(false);
        A70PlacementOrderDocmosisParameters params = parameterGenerator.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross is");
    }

    @Test
    void shouldFormatRespondentNamesTwoRespondents() {
        RespondentParty respondentParty1 = RespondentParty.builder()
            .firstName("John").lastName("Ross").build();
        RespondentParty respondentParty2 = RespondentParty.builder()
            .firstName("Julie").lastName("Ross").build();
        Respondent respondent1 = Respondent.builder().party(respondentParty1).build();
        Respondent respondent2 = Respondent.builder().party(respondentParty2).build();
        Element<Child> placementChild = element(Child.builder().party(ChildParty.builder().build()).build());
        Element<Placement> placementElement = element(Placement.builder()
            .childId(placementChild.getId())
            .placementUploadDateTime(LocalDate.now().atStartOfDay())
            .build());
        UUID placementId = placementElement.getId();
        DynamicList placementList = buildDynamicList(0, Pair.of(placementId, "My placement"));
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent1), element(respondent2)))
            .children1(List.of(placementChild))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersChildPlacementApplication(placementList)
                .build())
            .build();
        when(placementService.getPlacementById(caseData, placementId)).thenReturn(placementElement.getValue());
        when(placementService.getChildByPlacementId(caseData, placementId)).thenReturn(placementChild);
        when(courtService.isHighCourtCase(caseData)).thenReturn(false);
        A70PlacementOrderDocmosisParameters params = parameterGenerator.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross and Julie Ross are");
    }

    @Test
    void shouldFormatRespondentNamesThreeRespondents() {
        RespondentParty respondentParty1 = RespondentParty.builder().firstName("John").lastName("Ross").build();
        RespondentParty respondentParty2 = RespondentParty.builder().firstName("Julie").lastName("Ross").build();
        RespondentParty respondentParty3 = RespondentParty.builder().firstName("Karen").lastName("Donalds").build();
        Respondent respondent1 = Respondent.builder().party(respondentParty1).build();
        Respondent respondent2 = Respondent.builder().party(respondentParty2).build();
        Respondent respondent3 = Respondent.builder().party(respondentParty3).build();
        Element<Child> placementChild = element(Child.builder().party(ChildParty.builder().build()).build());
        Element<Placement> placementElement = element(Placement.builder()
            .childId(placementChild.getId())
            .placementUploadDateTime(LocalDate.now().atStartOfDay())
            .build());
        UUID placementId = placementElement.getId();
        DynamicList placementList = buildDynamicList(0, Pair.of(placementId, "My placement"));
        CaseData caseData = CaseData.builder()
            .respondents1(List.of(element(respondent1), element(respondent2), element(respondent3)))
            .children1(List.of(placementChild))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersChildPlacementApplication(placementList)
                .build())
            .build();
        when(placementService.getPlacementById(caseData, placementId)).thenReturn(placementElement.getValue());
        when(placementService.getChildByPlacementId(caseData, placementId)).thenReturn(placementChild);
        when(courtService.isHighCourtCase(caseData)).thenReturn(false);
        A70PlacementOrderDocmosisParameters params = parameterGenerator.generate(caseData);
        assertThat(params.getRespondentNames()).isEqualTo("John Ross, Julie Ross and Karen Donalds are");
    }

}
