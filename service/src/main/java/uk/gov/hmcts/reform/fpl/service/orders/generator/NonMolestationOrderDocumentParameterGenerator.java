package uk.gov.hmcts.reform.fpl.service.orders.generator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;
import uk.gov.hmcts.reform.fpl.model.event.ManageOrdersEventData;
import uk.gov.hmcts.reform.fpl.model.order.Order;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.DocmosisParameters;
import uk.gov.hmcts.reform.fpl.service.orders.docmosis.NonMolestationOrderDocumentParameters;
import uk.gov.hmcts.reform.fpl.service.orders.generator.common.OrderMessageGenerator;

import static java.time.format.FormatStyle.LONG;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NON_MOLESTATION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.order.Order.FL404A_NON_MOLESTATION_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NonMolestationOrderDocumentParameterGenerator implements DocmosisParameterGenerator {
    private final OrderMessageGenerator orderMessageGenerator;

    @Override
    public Order accept() {
        return FL404A_NON_MOLESTATION_ORDER;
    }

    @Override
    public DocmosisParameters generate(CaseData caseData) {
        ManageOrdersEventData eventData = caseData.getManageOrdersEventData();
        return NonMolestationOrderDocumentParameters.builder()
            .orderTitle(FL404A_NON_MOLESTATION_ORDER.getTitle())
            .applicantName(getSelectedApplicantName(eventData.getManageOrdersNonMolestationOrderApplicant()))
            .respondents(caseData.getRespondents1().stream()
                .filter(respondentElement -> respondentElement.getId()
                    .equals(eventData.getManageOrdersNonMolestationOrderRespondent().getValueCodeAsUUID()))
                .map(element -> element.getValue().getParty())
                .map(respondent -> {
                    final boolean isConfidential =
                        equalsIgnoreCase(respondent.getContactDetailsHidden(), YES.getValue());
                    return DocmosisRespondent.builder()
                        .name(respondent.getFullName())
                        .dateOfBirth(respondent.getDateOfBirth() != null
                            ? formatLocalDateToString(respondent.getDateOfBirth(), LONG) : "")
                        .address(!isConfidential && respondent.getAddress() != null
                            ? defaultIfEmpty(respondent.getAddress().getAddressAsString(", "), "")
                            : "")
                        .build();
                })
                .collect(toList()))
            .recitalsOrPreamble(eventData.getManageOrdersRecitalsAndPreamblesOptional())
            .orderByConsent(orderMessageGenerator.getOrderByConsentMessage(eventData))
            .courtOrder(eventData.getManageOrdersNonMolestationOrder())
            .build();
    }

    @Override
    public DocmosisTemplates template() {
        return NON_MOLESTATION_ORDER;
    }

    private String getSelectedApplicantName(DynamicList applicantsList) {
        String labelSelected = applicantsList.getValueLabel();
        return labelSelected.substring(0, labelSelected.lastIndexOf(","));
    }
}
