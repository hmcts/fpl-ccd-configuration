package uk.gov.hmcts.reform.fpl.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.events.order.AdditonalAppLicationDraftOrderUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.OrderCafcassData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.order.DraftOrder;
import uk.gov.hmcts.reform.fpl.service.cafcass.CafcassNotificationService;
import uk.gov.hmcts.reform.fpl.utils.CafcassHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor
public class AdditonalAppLicationDraftOrderUploadedEventHandler {
    private final CafcassNotificationService cafcassNotificationService;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;

    @EventListener
    @Async
    public void sendDocumentsToCafcass(final AdditonalAppLicationDraftOrderUploadedEvent event) {
        final CaseData caseData = event.getCaseData();

        if (CafcassHelper.isNotifyingCafcass(caseData, cafcassLookupConfiguration)) {
            AdditionalApplicationsBundle uploadedBundle = caseData.getAdditionalApplicationsBundle().get(0).getValue();

            final CaseData caseDataBefore = event.getCaseDataBefore();
            AdditionalApplicationsBundle oldBundle =
                    Optional.ofNullable(caseDataBefore.getAdditionalApplicationsBundle())
                            .filter(not(List::isEmpty))
                            .map(additionalApplicationsBundle -> additionalApplicationsBundle.get(0).getValue())
                            .orElse(null);

            if (!uploadedBundle.equals(oldBundle) && uploadedBundle.getC2DocumentBundle() != null) {
                Set<DocumentReference> documentReferences = unwrapElements(
                        uploadedBundle.getC2DocumentBundle().getDraftOrdersBundle()
                    )
                    .stream()
                    .map(DraftOrder::getDocument)
                    .collect(toSet());

                if (!documentReferences.isEmpty()) {
                    cafcassNotificationService.sendEmail(caseData,
                        documentReferences,
                        ORDER,
                        OrderCafcassData.builder()
                                .documentName("draft order")
                                .build()
                    );
                }
            }
        }
    }
}