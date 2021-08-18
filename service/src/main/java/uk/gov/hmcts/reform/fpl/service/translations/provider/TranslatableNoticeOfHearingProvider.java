package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.interfaces.TranslatableItem;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableNoticeOfHearingProvider implements TranslatableListItemProvider {

    private static final String CASE_FIELD = "hearingDetails";

    private final Time time;

    @Override
    @SuppressWarnings("unchecked")
    public List<Element<? extends TranslatableItem>> provideListItems(CaseData caseData) {
        return (List<Element<? extends TranslatableItem>>) defaultIfNull(caseData.getHearingDetails(), List.of());
    }

    @Override
    @SuppressWarnings("unchecked")
    public DocumentReference provideSelectedItemDocument(CaseData caseData, UUID selectedOrderId) {
        return ((List<Element<HearingBooking>>) defaultIfNull(caseData.getHearingDetails(), List.of()))
            .stream()
            .filter(order -> order.getId().equals(selectedOrderId))
            .findFirst().map(it -> it.getValue().getNoticeOfHearing())
            .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean accept(CaseData caseData, UUID selectedOrderId) {
        return provideListItems(caseData).stream()
            .anyMatch(hearing -> Objects.equals(selectedOrderId, hearing.getId()));
    }

    @Override
    public Map<String, Object> applyTranslatedOrder(CaseData caseData,
                                                    DocumentReference document, UUID selectedOrderId) {
        List<Element<HearingBooking>> hearings = caseData.getHearingDetails();

        hearings.stream()
            .filter(order -> Objects.equals(order.getId(), selectedOrderId))
            .findFirst()
            .ifPresent(order -> {
                HearingBooking translated = order.getValue().toBuilder()
                    .translatedNoticeOfHearing(document)
                    .translationUploadDateTime(time.now())
                    .build();
                hearings.set(hearings.indexOf(order), element(order.getId(), translated));
            });

        return Map.of(CASE_FIELD, hearings);
    }
}
