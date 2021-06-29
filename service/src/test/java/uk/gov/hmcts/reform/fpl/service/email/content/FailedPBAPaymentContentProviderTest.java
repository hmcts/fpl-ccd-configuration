package uk.gov.hmcts.reform.fpl.service.email.content;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.C2;

@ContextConfiguration(classes = {FailedPBAPaymentContentProvider.class})
class FailedPBAPaymentContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private FailedPBAPaymentContentProvider contentProvider;

    @ParameterizedTest
    @EnumSource(ApplicationType.class)
    void shouldReturnDataForCafcassNotification(ApplicationType applicationType) {
        final String applicant = "Swansea local authority, Applicant";
        final CaseData caseData = CaseData.builder()
            .id(RandomUtils.nextLong())
            .build();

        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(applicationType.getType())
            .caseUrl(applicationType == C110A_APPLICATION ? caseUrl((caseData.getId().toString()))
                : caseUrl(caseData.getId().toString(), C2))
            .applicant(applicant)
            .build();

        final FailedPBANotificationData actualParameters = contentProvider
            .getCtscNotifyData(caseData, applicationType, applicant);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnDataForLocalAuthorityNotification() {
        final ApplicationType applicationType = C110A_APPLICATION;
        final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
            .applicationType(applicationType.getType())
            .caseUrl(caseUrl("123"))
            .build();

        final FailedPBANotificationData actualParameters = contentProvider
            .getLocalAuthorityNotifyData(applicationType, 123L);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }
}
