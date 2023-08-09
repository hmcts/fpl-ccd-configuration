package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOCIAL_WORKER;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class LocalAuthorityTest {

    @Nested
    class MainContact {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyValueWhenNoColleagues(List<Element<Colleague>> colleagues) {

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(colleagues)
                .build();

            assertThat(localAuthority.getMainContact()).isEmpty();
        }

        @Test
        void shouldReturnEmptyValueWhenNoMainContact() {

            final Colleague colleague1 = Colleague.builder()
                .build();

            final Colleague colleague2 = Colleague.builder()
                .mainContact("No")
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2))
                .build();

            assertThat(localAuthority.getMainContact()).isEmpty();
        }

        @Test
        void shouldReturnMainContact() {

            final Colleague colleague1 = Colleague.builder()
                .build();

            final Colleague colleague2 = Colleague.builder()
                .mainContact("Yes")
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2))
                .build();

            assertThat(localAuthority.getMainContact()).contains(colleague2);
        }
    }

    @Nested
    class FirstSolicitor {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyValueWhenNoColleagues(List<Element<Colleague>> colleagues) {

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(colleagues)
                .build();

            assertThat(localAuthority.getFirstSolicitor()).isEmpty();
        }

        @Test
        void shouldReturnEmptyValueWhenNoSolicitor() {

            final Colleague colleague1 = Colleague.builder()
                .build();

            final Colleague colleague2 = Colleague.builder()
                .role(SOCIAL_WORKER)
                .build();

            final Colleague colleague3 = Colleague.builder()
                .role(OTHER)
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2, colleague3))
                .build();

            assertThat(localAuthority.getFirstSolicitor()).isEmpty();
        }

        @Test
        void shouldReturnFirstSolicitor() {

            final Colleague colleague1 = Colleague.builder()
                .build();

            final Colleague colleague2 = Colleague.builder()
                .role(SOLICITOR)
                .build();

            final Colleague colleague3 = Colleague.builder()
                .role(SOCIAL_WORKER)
                .build();

            final Colleague colleague4 = Colleague.builder()
                .role(OTHER)
                .build();

            final Colleague colleague5 = Colleague.builder()
                .role(SOLICITOR)
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(colleague1, colleague2, colleague3, colleague4, colleague5))
                .build();

            assertThat(localAuthority.getFirstSolicitor()).contains(colleague2);
        }
    }

    @Nested
    class Contacts {

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyListOfContactEmails(List<Element<Colleague>> colleagues) {
            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(colleagues)
                .build();

            assertThat(localAuthority.getContactEmails()).isEmpty();
        }

        @Test
        void shouldReturnContactEmails() {
            final Colleague colleague1 = Colleague.builder()
                .build();

            final Colleague colleague2 = Colleague.builder()
                .email("colleague2@test.com")
                .build();

            final Colleague colleague3 = Colleague.builder()
                .email("colleague3@test.com")
                .notificationRecipient("No")
                .build();

            final Colleague colleague4 = Colleague.builder()
                .email("colleague4@test.com")
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague5 = Colleague.builder()
                .email("colleague5@test.com")
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague6 = Colleague.builder()
                .notificationRecipient("Yes")
                .build();

            final Colleague colleague7 = Colleague.builder()
                .email("")
                .notificationRecipient("Yes")
                .build();

            final LocalAuthority localAuthority = LocalAuthority.builder()
                .colleagues(wrapElements(
                    colleague1, colleague2, colleague3, colleague4, colleague5, colleague6, colleague7))
                .build();

            assertThat(localAuthority.getContactEmails()).containsExactly(colleague4.getEmail(), colleague5.getEmail());
        }
    }

}
