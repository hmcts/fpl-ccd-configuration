package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.CAFCASS_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.CafcassHelper.isNotifyingCafcass;

@ExtendWith(MockitoExtension.class)
public class CafcassHelperTest {

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Test
    void shouldReturTrueWhenCaseLocalAuthorityIsEngland() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(Optional.of(
                new CafcassLookupConfiguration.Cafcass(LOCAL_AUTHORITY_CODE, CAFCASS_EMAIL_ADDRESS)));

        assertThat(isNotifyingCafcass(CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE).build(), cafcassLookupConfiguration))
            .isEqualTo(true);
    }

    @Test
    void shouldReturnFalseWhenCaseLocalAuthorityWhenCafcassIsNotEngland() {
        when(cafcassLookupConfiguration.getCafcassEngland(any()))
            .thenReturn(Optional.empty());

        assertThat(isNotifyingCafcass(CaseData.builder()
            .caseLocalAuthority("XX").build(), cafcassLookupConfiguration))
            .isEqualTo(false);
    }

    @Test
    void shouldReturnFalseWhenCaseLocalAuthorityIsNull() {
        // existing old standalone case which does not have a null caseLocalAuthority
        assertThat(isNotifyingCafcass(CaseData.builder()
            .caseLocalAuthority(null).build(), cafcassLookupConfiguration))
            .isEqualTo(false);
    }
}
