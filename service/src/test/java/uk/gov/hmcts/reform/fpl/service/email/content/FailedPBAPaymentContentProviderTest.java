package uk.gov.hmcts.reform.fpl.service.email.content;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.notify.payment.FailedPBANotificationData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.A50_PLACEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C110A_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.OTHER_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;

@ContextConfiguration(classes = {FailedPBAPaymentContentProvider.class})
class FailedPBAPaymentContentProviderTest extends AbstractEmailContentProviderTest {

    private final String applicant = "Swansea local authority, Applicant";

    final CaseData caseData = CaseData.builder()
        .id(RandomUtils.nextLong())
        .build();

    @Autowired
    private FailedPBAPaymentContentProvider contentProvider;

    @Nested
    class CourtNotifications {

        @ParameterizedTest
        @EnumSource(value = ApplicationType.class, names = {"C110A_APPLICATION", "A50_PLACEMENT"}, mode = EXCLUDE)
        void shouldReturnDataAboutOtherApplications(ApplicationType applicationType) {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(applicationType.getType())
                .caseUrl(caseUrl(caseData.getId().toString(), OTHER_APPLICATIONS))
                .applicant(applicant)
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getCtscNotifyData(caseData, List.of(applicationType), applicant);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }

        @Test
        void shouldReturnDataAboutMainApplication() {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(C110A_APPLICATION.getType())
                .caseUrl(caseUrl(caseData.getId().toString()))
                .applicant(applicant)
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getCtscNotifyData(caseData, List.of(C110A_APPLICATION), applicant);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }

        @Test
        void shouldReturnDataAboutPlacementApplication() {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(A50_PLACEMENT.getType())
                .caseUrl(caseUrl(caseData.getId().toString(), PLACEMENT))
                .applicant(applicant)
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getCtscNotifyData(caseData, List.of(A50_PLACEMENT), applicant);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }
    }

    @Nested
    class ApplicantNotifications {

        @ParameterizedTest
        @EnumSource(value = ApplicationType.class, names = {"C110A_APPLICATION", "A50_PLACEMENT"}, mode = EXCLUDE)
        void shouldReturnDataAboutOtherApplications(ApplicationType applicationType) {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(applicationType.getType())
                .caseUrl(caseUrl(caseData.getId().toString(), OTHER_APPLICATIONS))
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getApplicantNotifyData(List.of(applicationType), caseData);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }

        @Test
        void shouldReturnDataAboutMainApplication() {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(C110A_APPLICATION.getType())
                .caseUrl(caseUrl(caseData.getId().toString()))
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getApplicantNotifyData(List.of(C110A_APPLICATION), caseData);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }

        @Test
        void shouldReturnDataAboutPlacementApplication() {

            final FailedPBANotificationData expectedParameters = FailedPBANotificationData.builder()
                .applicationType(A50_PLACEMENT.getType())
                .caseUrl(caseUrl(caseData.getId().toString(), PLACEMENT))
                .build();

            final FailedPBANotificationData actualParameters = contentProvider
                .getApplicantNotifyData(List.of(A50_PLACEMENT), caseData);

            assertThat(actualParameters).isEqualTo(expectedParameters);
        }
    }

}
