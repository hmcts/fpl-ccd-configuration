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

    TranslatableListItemProviders underTest = new TranslatableListItemProviders(
        translatableGeneratedOrderListItemProvider,
        translatableCaseManagementOrderProvider,
        translatableStandardDirectionOrderProvider,
        translatableNoticeOfProceedingsProvider,
        translatableUrgentHearingOrderProvider,
        translatableNoticeOfHearingProvider
    );

    @Test
    void testGetAll() {
        assertThat(underTest.getAll()).isEqualTo(List.of(
            translatableGeneratedOrderListItemProvider,
            translatableCaseManagementOrderProvider,
            translatableStandardDirectionOrderProvider,
            translatableNoticeOfProceedingsProvider,
            translatableUrgentHearingOrderProvider,
            translatableNoticeOfHearingProvider
        ));
    }
}
