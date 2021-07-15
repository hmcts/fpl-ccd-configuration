package uk.gov.hmcts.reform.fpl.docmosis.generator;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.enums.C43OrderType;
import uk.gov.hmcts.reform.fpl.enums.EPOType;
import uk.gov.hmcts.reform.fpl.enums.RelationshipWithChild;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.EnglandOffices.BRIGHTON;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.MAGISTRATES;
import static uk.gov.hmcts.reform.fpl.enums.Jurisdiction.ENGLAND;
import static uk.gov.hmcts.reform.fpl.enums.ReasonForSecureAccommodation.ABSCOND;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.END_OF_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.orders.ManageOrdersEndDateType.NUMBER_OF_MONTHS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;

public class DocmosisOrderCaseDataGenerator {

    private static final String LA_CODE = "LA_CODE";

    private static final Element<Child> FIRST_CHILD = element(UUID.randomUUID(), Child.builder()
        .party(ChildParty.builder()
            .firstName("Kenny")
            .lastName("Kruger")
            .dateOfBirth(LocalDate.of(2010, 1, 1))
            .gender("Boy")
            .build()
        ).build()
    );

    public CaseData generateForOrder(final Order order) {

        return order.getQuestionsBlocks().stream().reduce(
            commonCaseData(order),
            this::addDataForQuestion,
            (v, v2) -> v2
        ).build();
    }

    private CaseData.CaseDataBuilder commonCaseData(Order order) {
        return CaseData.builder()
            .children1(List.of(FIRST_CHILD))
            .manageOrdersEventData(ManageOrdersEventData.builder()
                .manageOrdersType(order)
                .build())
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("Remy").lastName("Respondy").build()).build()))
            .others(Others.builder().additionalOthers(wrapElements(
                Other.builder().name("Otto Otherman").build())).build())
            .familyManCaseNumber("FamilyManCaseNumber113")
            .caseLocalAuthority(LA_CODE)
            .id(1234567890123456L);
    }

    private CaseData.CaseDataBuilder addDataForQuestion(CaseData.CaseDataBuilder builder,
                                                        OrderQuestionBlock questionBlock) {

        switch (questionBlock) {
            case LINKED_TO_HEARING:
            case LINK_APPLICATION:
            case REVIEW_DRAFT_ORDER:
            case CLOSE_CASE:
            case WHICH_OTHERS:
                // Do Nothing - they won't modify the document
                break;
            case IS_CHILD_REPRESENTED:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersIsChildRepresented("No")
                        .build()
                );
            case REASON_FOR_SECURE_ACCOMMODATION:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersReasonForSecureAccommodation(ABSCOND)
                        .build()
                );
            case SECURE_ACCOMMODATION_ORDER_JURISDICTION:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersOrderJurisdiction(ENGLAND)
                        .build()
                );
            case APPROVER:
                return builder.judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                    .judgeTitle(MAGISTRATES)
                    .judgeLastName("Stevenson")
                    .legalAdvisorName("John Papa")
                    .build());
            case APPROVAL_DATE:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersApprovalDate(LocalDate.of(2013, 10, 5))
                        .build());
            case SELECT_SINGLE_CHILD:
                return builder
                    .manageOrdersEventData(
                        getManageOrdersEvent(builder)
                            .whichChildIsTheOrderFor(buildDynamicList(0,
                                Pair.of(FIRST_CHILD.getId(), FIRST_CHILD.getValue().getParty().getFullName())
                            ))
                            .build()
                    );
            case WHICH_CHILDREN:
                return builder.children1(List.of(FIRST_CHILD))
                    .childSelector(Selector.builder().selected(List.of(1)).build());
            case TITLE:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersTitle("Blank Order Title")
                        .build()
                );
            case DISCHARGE_DETAILS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersCareOrderIssuedDate(LocalDate.of(2013, 10, 4))
                        .manageOrdersCareOrderIssuedCourt("98")
                        .build()
                );
            case DETAILS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersDirections("Some order directions")
                        .build()
                );
            case APPROVAL_DATE_TIME:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersApprovalDateTime(LocalDateTime.of(2012, 8, 7, 15, 23, 11))
                        .build()
                );
            case EPO_TYPE_AND_PREVENT_REMOVAL:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersEpoRemovalAddress(Address.builder()
                            .addressLine1("Soccer Street 212")
                            .addressLine2("World's end")
                            .addressLine3("Miami Beach")
                            .country("Slovakia")
                            .postcode("XXX 123")
                            .postTown("Humberland Cumberland")
                            .build())
                        .manageOrdersEpoType(EPOType.REMOVE_TO_ACCOMMODATION)
                        .build()
                );
            case EPO_INCLUDE_PHRASE:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersIncludePhrase("included extra EPO phrase")
                        .build()
                );
            case EPO_CHILDREN_DESCRIPTION:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersChildrenDescription("the children are really lovely.")
                        .build()
                );
            case EPO_EXPIRY_DATE:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersEndDateTime(LocalDateTime.of(2018, 9, 1, 13, 20, 4))
                        .build()
                );
            case CHILD_ARRANGEMENT_SPECIFIC_ISSUE_PROHIBITED_STEPS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                    .manageOrdersMultiSelectListForC43(Collections.singletonList(C43OrderType.CHILD_ARRANGEMENT_ORDER))
                    .manageOrdersRecitalsAndPreambles("Recitals and Preambles")
                    .build()
                );
            case FURTHER_DIRECTIONS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersFurtherDirections("Some further directions.")
                        .build()
                );
            case PARENTAL_RESPONSIBILITY:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersParentResponsible("Remmy Responsible")
                        .manageOrdersRelationshipWithChild(RelationshipWithChild.FATHER)
                        .build()
                );
            case ICO_EXCLUSION:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersExclusionDetails("Some exclusion details")
                        .build()
                );
            case MANAGE_ORDER_END_DATE_WITH_END_OF_PROCEEDINGS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersEndDateTypeWithEndOfProceedings(END_OF_PROCEEDINGS)
                        .build()
                );
            case MANAGE_ORDER_END_DATE_WITH_MONTH:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersEndDateTypeWithMonth(NUMBER_OF_MONTHS)
                        .manageOrdersSetMonthsEndDate(4)
                        .build()
                );
            case CAFCASS_JURISDICTIONS:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersCafcassOfficesEngland(BRIGHTON)
                        .manageOrdersCafcassRegion("ENGLAND")
                        .build()
                );
            case APPOINTED_GUARDIAN:
                return builder.appointedGuardianSelector(Selector.builder().selected(List.of(0, 1)).build());
            case ORDER_BY_CONSENT:
                return builder.manageOrdersEventData(
                    getManageOrdersEvent(builder)
                        .manageOrdersIsByConsent("Yes")
                        .build()
                );
            default:
                throw new RuntimeException("Question block for " + questionBlock + " not implemented");
        }

        return builder;
    }

    private ManageOrdersEventData.ManageOrdersEventDataBuilder getManageOrdersEvent(CaseData.CaseDataBuilder builder) {
        return getManageOrdersEventData(builder).toBuilder();
    }

    private ManageOrdersEventData getManageOrdersEventData(CaseData.CaseDataBuilder builder) {
        return defaultIfNull(builder.build().getManageOrdersEventData(), ManageOrdersEventData.builder().build());
    }

}
