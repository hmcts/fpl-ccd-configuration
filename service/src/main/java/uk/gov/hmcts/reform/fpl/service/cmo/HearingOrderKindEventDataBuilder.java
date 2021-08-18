package uk.gov.hmcts.reform.fpl.service.cmo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.components.OptionCountBuilder;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.IdentityService;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingOrderKindEventDataBuilder {

    private final IdentityService identityService;
    private final OptionCountBuilder optionCountBuilder;


    public void build(
        UUID selectedHearingId, CaseData caseData, UploadDraftOrdersData eventData,
        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder
    ) {

        List<Element<HearingOrder>> c21Drafts = isEmpty(eventData.getCurrentHearingOrderDrafts()) ? getC21Drafts(
            caseData,
            selectedHearingId) : eventData.getCurrentHearingOrderDrafts();
        newEventDataBuilder.currentHearingOrderDrafts(c21Drafts);
        setIndividualOrderDrafts(newEventDataBuilder, c21Drafts);
        newEventDataBuilder.orderToSendOptionCount(optionCountBuilder.generateCode(c21Drafts));

    }

    @SuppressWarnings("EmptyCatchBlock")
    private void setIndividualOrderDrafts(UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder,
                                          List<Element<HearingOrder>> c21Drafts) {
        range(0, 10).forEach(
            i -> {
                try {
                    UploadDraftOrdersData.UploadDraftOrdersDataBuilder.class.getMethod(
                        String.format("orderToSend%d", i), DocumentReference.class
                    ).invoke(newEventDataBuilder,
                        i < c21Drafts.size() ? defaultIfNull(c21Drafts.get(i).getValue().getOrder(), null) : null
                    );
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
                }
            }
        );
    }

    private List<Element<HearingOrder>> getC21Drafts(CaseData caseData, UUID selectedHearingId) {

        final List<Element<HearingOrder>> draftOrders = unwrapElements(caseData.getHearingOrdersBundlesDrafts())
            .stream()
            .filter(bundle -> Objects.equals(bundle.getHearingId(), selectedHearingId))
            .findFirst()
            .map(HearingOrdersBundle::getOrders)
            .orElse(new ArrayList<>());

        final List<Element<HearingOrder>> nonCMODraftOrders = draftOrders.stream()
            .filter(draftOrder -> C21.equals(draftOrder.getValue().getType()))
            .collect(toList());

        if (isEmpty(nonCMODraftOrders)) {
            nonCMODraftOrders.add(element(identityService.generateId(), HearingOrder.builder().build()));
        }

        return nonCMODraftOrders;
    }
}
