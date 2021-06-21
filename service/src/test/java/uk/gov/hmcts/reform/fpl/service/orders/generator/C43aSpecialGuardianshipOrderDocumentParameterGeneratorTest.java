package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C43aSpecialGuardianshipOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;

import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.SGO;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C43A_SPECIAL_GUARDIANSHIP_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C43aSpecialGuardianshipOrderDocumentParameterGeneratorTest {

    private static final String LA_CODE = "LA_CODE";
    private static final String LA_NAME = "Local Authority Name";
    private static final Child CHILD = mock(Child.class);
    private static final Applicant APPLICANT = mock(Applicant.class);
    private static final String[] APPLICANT_NAMES = {"Person1","Person2"};
    private static final String FURTHER_DIRECTIONS = "further directions";
    private static final String ORDER_HEADER = "Warning";
    private static final String ORDER_MESSAGE = ORDER_HEADER + " \n\n"
        + "Where a Special Guardianship Order is in force no person may "
        + "cause the child to be known by a new surname or remove the "
        + "child from the United Kingdom without either the written consent"
        + " of every person who has parental responsibility for the child or "
        + "the leave of the court. "
        + "\n \n"
        + "However, this does not prevent the removal "
        + "of a child for a period of less than 3 months, "
        + "by its special guardian(s) (Section 14C (3) and (4) Children Act 1989)."
        + "\n \n"
        + "It may be a criminal offence under the Child Abduction Act 1984 "
        + "to remove the child from the United Kingdom without leave of the court.";
    private static final String NOTICE_HEADER = "Notice";
    private static final String NOTICE_MESSAGE = NOTICE_HEADER + "\n\n"
        + "Any person with parental responsibility for a child may "
        + "obtain advice on what can be done to prevent the issue of a passport to the child. They should write "
        + "to The United Kingdom Passport Agency, Globe House, 89 Eccleston Square, LONDON, SW1V 1PN.";

    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LA_CODE)
        .manageOrdersEventData(ManageOrdersEventData.builder()
            .manageOrdersFurtherDirections(FURTHER_DIRECTIONS)
            .manageOrdersIsByConsent("Yes")
            .build())
        .build();


    @Mock
    private ChildrenService childrenService;

//    @Mock
//    private ApplicantService applicantService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C43aSpecialGuardianshipOrderDocumentParameterGenerator underTest;

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C43A_SPECIAL_GUARDIANSHIP_ORDER);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(SGO);
    }

    @Test
    void generateDocumentForChildAndSingleApplicant() {
        List<Element<Child>> selectedChildren = wrapElements(CHILD);
//        List<Element<Applicant>> applicants = wrapElements(APPLICANT);

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
//        when(applicantService.addHiddenValues(CASE_DATA)).thenReturn(applicants);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildAndSingleApplicant())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildrenAndSingleApplicant() {
        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);
//        List<Element<Applicant>> applicants = wrapElements(APPLICANT);

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
//        when(applicantService.addHiddenValues(CASE_DATA)).thenReturn(applicants);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildrenAndSingleApplicant())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForChildAndMultipleApplicants() {
        List<Element<Child>> selectedChildren = wrapElements(CHILD);
//        List<Element<Applicant>> applicants = wrapElements(APPLICANT);

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
//        when(applicantService.addHiddenValues(CASE_DATA)).thenReturn(applicants);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildAndMutipleApplicants())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForForChildrenAndMultipleApplicants() {
        List<Element<Child>> selectedChildren = wrapElements(CHILD, CHILD);
//        List<Element<Applicant>> applicants = wrapElements(APPLICANT);

        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(selectedChildren);
//        when(applicantService.addHiddenValues(CASE_DATA)).thenReturn(applicants);

        DocmosisParameters generatedParameters = underTest.generate(CASE_DATA);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderAppointmentMessageForChildrenAndMultipleApplicants())
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getOrderAppointmentMessageForChildAndSingleApplicant() {
        return format("The Court orders [Applicant1] is appointed as Special Guardian for the child.");
    }

    private String getOrderAppointmentMessageForChildrenAndSingleApplicant() {
        return format("The Court orders [Applicant1] is appointed as Special Guardian for the children.");
    }

    private String getOrderAppointmentMessageForChildAndMutipleApplicants() {
        return format("The Court orders [Applicant1],[Applicant1] is appointed as Special Guardian for the child.");
    }

    private String getOrderAppointmentMessageForChildrenAndMultipleApplicants() {
        return format("The Court orders [Applicant1],[Applicant1] are appointed as Special Guardian for the children.");
    }

    private C43aSpecialGuardianshipOrderDocmosisParameters.C43aSpecialGuardianshipOrderDocmosisParametersBuilder<?,?>
    expectedCommonParameters(Boolean isOrderByConsent) {
        String orderByConsentContent = "By consent";
        if (!isOrderByConsent){
            orderByConsentContent = null;
        }
        return C43aSpecialGuardianshipOrderDocmosisParameters.builder()
            .orderTitle(Order.C43A_SPECIAL_GUARDIANSHIP_ORDER.getTitle())
            .orderType(GeneratedOrderType.SPECIAL_GUARDIANSHIP_ORDER)
            .furtherDirections(FURTHER_DIRECTIONS)
            .orderByConsent(orderByConsentContent)
            .warningMessage(ORDER_MESSAGE)
            .orderMessage(NOTICE_MESSAGE);
    }
}
