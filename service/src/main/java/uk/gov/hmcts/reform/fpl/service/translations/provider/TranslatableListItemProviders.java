package uk.gov.hmcts.reform.fpl.service.translations.provider;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TranslatableListItemProviders {

    private final TranslatableGeneratedOrderListItemProvider translatableGeneratedOrderListItemProvider;
    private final TranslatableCaseManagementOrderProvider translatableCaseManagementOrderProvider;
    private final TranslatableStandardDirectionOrderProvider translatableStandardDirectionOrderProvider;
    private final TranslatableNoticeOfProceedingsProvider translatableNoticeOfProceedingsProvider;
    private final TranslatableUrgentHearingOrderProvider translatableUrgentHearingOrderProvider;
    private final TranslatableNoticeOfHearingProvider translatableNoticeOfHearingProvider;
    private final TranslatableC110AProvider translatableC110AProvider;
    private final TranslatableRespondentStatementsProvider translatableRespondentStatementsProvider;

    public List<TranslatableListItemProvider> getAll() {
        return List.of(
            translatableGeneratedOrderListItemProvider,
            translatableCaseManagementOrderProvider,
            translatableStandardDirectionOrderProvider,
            translatableNoticeOfProceedingsProvider,
            translatableUrgentHearingOrderProvider,
            translatableNoticeOfHearingProvider,
            translatableC110AProvider,
            translatableRespondentStatementsProvider
        );
    }

}
