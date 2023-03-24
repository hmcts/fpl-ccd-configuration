package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.C34aContactWithAChildInCareOrderDocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.ORDER_V2;
import static uk.gov.hmcts.reform.fpl.model.order.Order.C34A_CONTACT_WITH_A_CHILD_IN_CARE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith({MockitoExtension.class})
class C34aContactWithAChildCareOrderParameterGeneratorTest {

    public static final LocalDateTime APPROVAL_DATE_TIME = LocalDateTime.of(2021, 4, 20, 10, 0, 0);
    public static final String CONSENT = "By consent";
    private static final String LOCAL_AUTHORITY_NAME = "Swansea Local Authority";
    private static final String CONDITION_MESSAGE = "This is the condition message.";
    private static final String CONTACT_NAME_1 = "Peter Smith";
    private static final String CONTACT_NAME_2 = "Holmes Watson";
    private static final String CONTACT_NAME_3 = "Baker Smith";
    @Mock
    private static Child CHILD;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;
    @Mock
    private ChildrenSmartSelector childrenSmartSelector;
    @InjectMocks
    private C34aContactWithAChildInCareOrderDocumentParameterGenerator underTest;
    @Mock
    private OrderMessageGenerator orderMessageGenerator;

    @Test
    void shouldReturnCorrectOrder() {
        assertThat(underTest.accept()).isEqualTo(C34A_CONTACT_WITH_A_CHILD_IN_CARE);
    }

    @Test
    void shouldReturnCorrectTemplate() {
        assertThat(underTest.template()).isEqualTo(ORDER_V2);
    }

    @Test
    void generateDocumentForSingleChildWithOrderByConsent() {
        CaseData caseData = getCaseData(true, CONDITION_MESSAGE, CONTACT_NAME_1, null, null);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
                .orderDetails(getOrderDetailForOneAllowedContactName())
                .orderMessage(getOrderMessageForLocalAuthority(LOCAL_AUTHORITY_NAME))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForTwoChildrenWithOrderByConsent() {
        CaseData caseData = getCaseData(true, CONDITION_MESSAGE, CONTACT_NAME_1, CONTACT_NAME_2, null);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderDetailForTwoAllowedContactName())
            .orderMessage(getOrderMessageForLocalAuthority(LOCAL_AUTHORITY_NAME))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForThreeChildrenWithOrderByConsent() {
        CaseData caseData = getCaseData(true, CONDITION_MESSAGE, CONTACT_NAME_1, CONTACT_NAME_2,
            CONTACT_NAME_3);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(CONSENT);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(true)
            .orderDetails(getOrderDetailForThreeAllowedContactName())
            .orderMessage(getOrderMessageForLocalAuthority(LOCAL_AUTHORITY_NAME))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    @Test
    void generateDocumentForThreeChildren() {
        CaseData caseData = getCaseData(false, CONDITION_MESSAGE, CONTACT_NAME_1, CONTACT_NAME_2,
            CONTACT_NAME_3);

        List<Element<Child>> selectedChildren = wrapElements(CHILD);
        when(laNameLookup.getLocalAuthorityName(any())).thenReturn(LOCAL_AUTHORITY_NAME);
        when(childrenSmartSelector.getSelectedChildren(caseData)).thenReturn(selectedChildren);
        when(orderMessageGenerator.getOrderByConsentMessage(any())).thenReturn(null);

        DocmosisParameters generatedParameters = underTest.generate(caseData);
        DocmosisParameters expectedParameters = expectedCommonParameters(false)
            .orderDetails(getOrderDetailForThreeAllowedContactName())
            .orderMessage(getOrderMessageForLocalAuthority(LOCAL_AUTHORITY_NAME))
            .build();

        assertThat(generatedParameters).isEqualTo(expectedParameters);
    }

    private String getOrderDetailForOneAllowedContactName() {
        return format("The Court orders that there may be contact between the child and \n\nPeter Smith\n\n"
            + "The contact is subject to the following conditions\n\n%s", CONDITION_MESSAGE);
    }

    private String getOrderDetailForTwoAllowedContactName() {
        return format("The Court orders that there may be contact between the child and \n\nPeter Smith\n"
            + "Holmes Watson\n\nThe contact is subject to the following conditions\n\n%s", CONDITION_MESSAGE);
    }

    private String getOrderDetailForThreeAllowedContactName() {
        return format("The Court orders that there may be contact between the child and \n\nPeter Smith\n"
            + "Holmes Watson\n"
            + "Baker Smith\n\nThe contact is subject to the following conditions\n\n%s", CONDITION_MESSAGE);
    }

    private String getOrderMessageForLocalAuthority(String localAuthority) {
        return format("The local authority is %s.", localAuthority);
    }

    private C34aContactWithAChildInCareOrderDocmosisParameters
        .C34aContactWithAChildInCareOrderDocmosisParametersBuilder<?, ?>
        expectedCommonParameters(Boolean isOrderByConsent) {
        String orderByConsentContent = getOrderByConsentContent(isOrderByConsent);

        return C34aContactWithAChildInCareOrderDocmosisParameters.builder()
            .orderTitle(C34A_CONTACT_WITH_A_CHILD_IN_CARE.getTitle())
            .childrenAct(C34A_CONTACT_WITH_A_CHILD_IN_CARE.getChildrenAct())
            .noticeMessage("An authority may refuse to allow the contact that would otherwise be required by "
                + "virtue of Section 34(1) Children Act 1989 or an order under this section "
                + "if (a) they are satisfied that it is necessary to do so in order to safeguard or "
                + "promote the welfare of the [4]{child(ren)}; and (b) the refusal (i) is decided "
                + "upon as a matter of urgency; and (ii) does not last "
                + "for more than 7 days (Section 34(6) Children Act 1989).")
            .noticeHeader("Notice")
            .orderByConsent(orderByConsentContent);
    }

    private String getOrderByConsentContent(Boolean isOrderByConsent) {
        String orderByConsentContent = CONSENT;
        if (!isOrderByConsent) {
            orderByConsentContent = null;
        }
        return orderByConsentContent;
    }

    private CaseData getCaseData(boolean isOrderByConsent, String conditionsOfContact,
                                 String allowedContact1Name, String allowedContact2Name, String allowedContact3Name) {
        return CaseData.builder()
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersApprovalDateTime(APPROVAL_DATE_TIME)
                .manageOrdersIsByConsent(isOrderByConsent ? "Yes" : "No")
                .manageOrdersAllowedContact1(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(UUID.randomUUID().toString())
                        .label(allowedContact1Name)
                        .build()).build())
                .manageOrdersAllowedContact2(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(StringUtils.isEmpty(allowedContact2Name) ? "" : UUID.randomUUID().toString())
                        .label(allowedContact2Name)
                        .build()).build())
                .manageOrdersAllowedContact3(DynamicList.builder()
                    .value(DynamicListElement.builder()
                        .code(StringUtils.isEmpty(allowedContact3Name) ? "" : UUID.randomUUID().toString())
                        .label(allowedContact3Name)
                        .build()).build())
                .manageOrdersConditionsOfContact(conditionsOfContact)
                .build())
            .build();
    }
}
