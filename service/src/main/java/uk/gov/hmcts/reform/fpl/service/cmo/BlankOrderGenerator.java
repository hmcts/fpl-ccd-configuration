package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.TIME_DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.getSelectedJudge;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BlankOrderGenerator {

    private final Time time;

    public Element<GeneratedOrder> buildBlankOrder(CaseData caseData,
                                                   Element<HearingOrdersBundle> selectedOrdersBundle,
                                                   Element<HearingOrder> sealedOrder,
                                                   List<Element<Other>> selectedOthers,
                                                   String othersNotified) {

        Element<HearingBooking> hearingElement =
            defaultIfNull(caseData.getHearingDetails(), new ArrayList<Element<HearingBooking>>())
                .stream()
                .filter(hearing -> Objects.equals(hearing.getId(), selectedOrdersBundle.getValue().getHearingId()))
                .findFirst().orElse(null);

        HearingOrder order = sealedOrder.getValue();

        GeneratedOrder.GeneratedOrderBuilder builder = GeneratedOrder.builder()
            .type(BLANK_ORDER.getLabel())
            .title(order.getTitle())
            .dateOfIssue(order.getDateIssued() != null ? formatLocalDateToString(order.getDateIssued(), DATE) : null)
            .judgeAndLegalAdvisor(hearingElement != null
                ? getSelectedJudge(hearingElement.getValue().getJudgeAndLegalAdvisor(), caseData.getAllocatedJudge())
                : null)
            .date(formatLocalDateTimeBaseUsingFormat(time.now(), TIME_DATE))
            .children(caseData.getAllChildren())
            .others(selectedOthers)
            .othersNotified(othersNotified)
            .translationRequirements(order.getTranslationRequirements());

        if (order.isConfidentialOrder()) {
            builder.documentConfidential(order.getOrderConfidential());
        } else {
            builder.document(order.getOrder());
        }
        return element(sealedOrder.getId(), builder.build());
    }
}
