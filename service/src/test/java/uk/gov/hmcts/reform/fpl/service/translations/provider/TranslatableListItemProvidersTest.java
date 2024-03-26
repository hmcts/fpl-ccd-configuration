package uk.gov.hmcts.reform.fpl.service.translations.provider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TranslatableListItemProvidersTest {

    private final TranslatableGeneratedOrderListItemProvider translatableGeneratedOrderListItemProvider = mock(
        TranslatableGeneratedOrderListItemProvider.class);
    private final TranslatableCaseManagementOrderProvider translatableCaseManagementOrderProvider = mock(
        TranslatableCaseManagementOrderProvider.class);
    private final TranslatableStandardDirectionOrderProvider translatableStandardDirectionOrderProvider = mock(
        TranslatableStandardDirectionOrderProvider.class);
    private final TranslatableNoticeOfProceedingsProvider translatableNoticeOfProceedingsProvider = mock(
        TranslatableNoticeOfProceedingsProvider.class);
    private final TranslatableUrgentHearingOrderProvider translatableUrgentHearingOrderProvider = mock(
        TranslatableUrgentHearingOrderProvider.class);
    private final TranslatableNoticeOfHearingProvider translatableNoticeOfHearingProvider =
        mock(TranslatableNoticeOfHearingProvider.class);
    private final TranslatableC110AProvider translatableC110AProvider =
        mock(TranslatableC110AProvider.class);
    private final TranslatableRespondentStatementsProvider translatableRespondentStatementsProvider =
        mock(TranslatableRespondentStatementsProvider.class);

    TranslatableListItemProviders underTest = new TranslatableListItemProviders(
        translatableGeneratedOrderListItemProvider,
        translatableCaseManagementOrderProvider,
        translatableStandardDirectionOrderProvider,
        translatableNoticeOfProceedingsProvider,
        translatableUrgentHearingOrderProvider,
        translatableNoticeOfHearingProvider,
        translatableC110AProvider,
        translatableRespondentStatementsProvider
    );

    @Test
    void testGetAll() {
        assertThat(underTest.getAll()).isEqualTo(List.of(
            translatableGeneratedOrderListItemProvider,
            translatableCaseManagementOrderProvider,
            translatableStandardDirectionOrderProvider,
            translatableNoticeOfProceedingsProvider,
            translatableUrgentHearingOrderProvider,
            translatableNoticeOfHearingProvider,
            translatableC110AProvider,
            translatableRespondentStatementsProvider
        ));
    }
}
