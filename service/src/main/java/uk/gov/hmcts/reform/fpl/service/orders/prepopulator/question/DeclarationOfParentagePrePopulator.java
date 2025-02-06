package uk.gov.hmcts.reform.fpl.service.orders.prepopulator.question;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.order.OrderQuestionBlock;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeclarationOfParentagePrePopulator implements QuestionBlockOrderPrePopulator {
    private static final String FORMATTED_RESPONDENT = ", Respondent ";
    private final DynamicListService dynamicListService;

    @Override
    public OrderQuestionBlock accept() {
        return OrderQuestionBlock.DECLARATION_OF_PARENTAGE;
    }

    @Override
    public Map<String, Object> prePopulate(CaseData caseData) {
        DynamicList parentageAction = dynamicListService.asDynamicList(Map.of());
        parentageAction.getListItems().add(DynamicListElement.builder()
            .code("IS").label("is").build());
        parentageAction.getListItems().add(DynamicListElement.builder()
            .code("ISNOT").label("is not").build());
        parentageAction.getListItems().add(DynamicListElement.builder()
            .code("WAS").label("was").build());
        parentageAction.getListItems().add(DynamicListElement.builder()
            .code("WASNOT").label("was not").build());

        DynamicList applicants = dynamicListService.asDynamicList(Map.of());
        DynamicList hearingParties = dynamicListService.asDynamicList(Map.of());
        DynamicList personWhoseParenthoodIs = dynamicListService.asDynamicList(Map.of());

        caseData.getLocalAuthorities().forEach(la -> {
                applicants.getListItems().add(DynamicListElement.builder()
                    .code(la.getId())
                    .label(la.getValue().getName())
                    .build());
                hearingParties.getListItems().add(DynamicListElement.builder()
                    .code(la.getValue().getName())
                    .label(la.getValue().getName() + ", Applicant")
                    .build());
            }
        );
        IncrementalInteger childIncrementer = new IncrementalInteger(1);
        caseData.getAllChildren().forEach(c -> {
            RespondentSolicitor solicitor = c.getValue().getSolicitor();
            if (solicitor != null) {
                hearingParties.getListItems().add(DynamicListElement.builder()
                    .code(solicitor.getFullName())
                    .label(solicitor.getFullName() + ", Child " + childIncrementer.getValue() + "'s solicitor")
                    .build());
            }
            childIncrementer.getAndIncrement();
        });
        IncrementalInteger respondentIncrementer = new IncrementalInteger(1);
        caseData.getAllRespondents().forEach(r -> {
            RespondentSolicitor solicitor = r.getValue().getSolicitor();
            applicants.getListItems().add(DynamicListElement.builder()
                .code(r.getId())
                .label(r.getValue().getParty().getFullName())
                .build());
            hearingParties.getListItems().add(DynamicListElement.builder()
                .code(r.getValue().getParty().getFullName())
                .label(r.getValue().getParty().getFullName() + FORMATTED_RESPONDENT + respondentIncrementer.getValue())
                .build());
            if (solicitor != null) {
                hearingParties.getListItems().add(DynamicListElement.builder()
                    .code(solicitor.getFullName())
                    .label(solicitor.getFullName() + FORMATTED_RESPONDENT + respondentIncrementer.getValue()
                        + "'s solicitor")
                    .build());
            }
            personWhoseParenthoodIs.getListItems().add(DynamicListElement.builder()
                .code(r.getValue().getParty().getFullName())
                .label(r.getValue().getParty().getFullName() + FORMATTED_RESPONDENT + respondentIncrementer.getValue())
                .build());
            respondentIncrementer.getAndIncrement();
        });
        IncrementalInteger othersIncrementer = new IncrementalInteger(1);
        caseData.getOthersV2().forEach(o -> {
            personWhoseParenthoodIs.getListItems().add(DynamicListElement.builder()
                .code(o.getValue().getName())
                .label(o.getValue().getName() + ", Other to be given notice " + othersIncrementer.getAndIncrement())
                .build());
        });

        return Map.of(
            "manageOrdersParentageApplicant", applicants,
            "manageOrdersHearingParty1", hearingParties,
            "manageOrdersHearingParty2", hearingParties,
            "manageOrdersPersonWhoseParenthoodIs", personWhoseParenthoodIs,
            "manageOrdersParentageAction", parentageAction
        );
    }
}

