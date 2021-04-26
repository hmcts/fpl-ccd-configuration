package uk.gov.hmcts.reform.fpl.service.representative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentation;
import uk.gov.hmcts.reform.fpl.model.noc.ChangeOfRepresentationMethod;
import uk.gov.hmcts.reform.fpl.model.noc.ChangedRepresentative;
import uk.gov.hmcts.reform.fpl.service.IdentityService;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;


class ChangeOfRepresentationServiceTest {

    private static final ChangeOfRepresentationMethod METHOD = ChangeOfRepresentationMethod.NOC;
    private static final String ACTIONED_BY = "actionedByPerson";
    private static final UUID UUID = java.util.UUID.randomUUID();
    private static final UUID ANOTHER_UUID = java.util.UUID.randomUUID();
    private static final String RESPONDENT_FIRST_NAME = "gio";
    private static final String RESPONDENT_LAST_NAME = "gia";
    private static final Respondent RESPONDENT = Respondent.builder()
        .party(RespondentParty.builder()
            .firstName(RESPONDENT_FIRST_NAME)
            .lastName(RESPONDENT_LAST_NAME).build())
        .build();
    private static final String RESPONDENT_FULL_NAME = String.format("%s %s",
        RESPONDENT_FIRST_NAME,
        RESPONDENT_LAST_NAME);
    private static final LocalDate TODAY = LocalDate.of(2012, 12, 12);
    private static final String EMAIL_1 = "email1";
    private static final String EMAIL_2 = "email2";
    private static final Organisation ORGANISATION_1 = mock(Organisation.class);
    private static final Organisation ORGANISATION_2 = mock(Organisation.class);
    private static final String FIRST_NAME_1 = "firstName1";
    private static final String FIRST_NAME_2 = "firstName2";
    private static final String LAST_NAME_1 = "lastName1";
    private static final String LAST_NAME_2 = "lastName2";
    private static final ChangedRepresentative CHANGED_REPRESENTATIVE_1 = ChangedRepresentative.builder()
        .organisation(ORGANISATION_1)
        .firstName(FIRST_NAME_1)
        .lastName(LAST_NAME_1)
        .email(EMAIL_1)
        .build();
    private static final ChangedRepresentative CHANGED_REPRESENTATIVE_2 = ChangedRepresentative.builder()
        .organisation(ORGANISATION_2)
        .firstName(FIRST_NAME_2)
        .lastName(LAST_NAME_2)
        .email(EMAIL_2)
        .build();
    private static final RespondentSolicitor REPRESENTATIVE_1 = RespondentSolicitor.builder()
        .email(EMAIL_1)
        .firstName(FIRST_NAME_1)
        .lastName(LAST_NAME_1)
        .organisation(ORGANISATION_1)
        .build();
    private static final RespondentSolicitor REPRESENTATIVE_2 = RespondentSolicitor.builder()
        .email(EMAIL_2)
        .firstName(FIRST_NAME_2)
        .lastName(LAST_NAME_2)
        .organisation(ORGANISATION_2)
        .build();

    private final IdentityService identityService = mock(IdentityService.class);
    private final Time time = mock(Time.class);

    ChangeOfRepresentationService underTest = new ChangeOfRepresentationService(identityService, time);

    @BeforeEach
    void setUp() {
        when(identityService.generateId()).thenReturn(UUID);
        when(time.now()).thenReturn(TODAY.atStartOfDay());
    }

    @Test
    void testAddingRepresentativeWhenNotRepresented() {
        List<Element<ChangeOfRepresentation>> actual = underTest.changeRepresentative(
            new ChangeOfRepresentationRequest(List.of(), RESPONDENT, REPRESENTATIVE_1, null, METHOD, ACTIONED_BY));

        assertThat(actual).isEqualTo(List.of(
            element(UUID, changeOfRepresentation()
                .added(CHANGED_REPRESENTATIVE_1)
                .build())
        ));

    }

    @Test
    void testAddingRepresentativeChangedRepresented() {
        List<Element<ChangeOfRepresentation>> actual = underTest.changeRepresentative(
            new ChangeOfRepresentationRequest(List.of(),
                RESPONDENT,
                REPRESENTATIVE_1,
                REPRESENTATIVE_2,
                METHOD,
                ACTIONED_BY));

        assertThat(actual).isEqualTo(List.of(
            element(UUID, changeOfRepresentation()
                .added(CHANGED_REPRESENTATIVE_1)
                .removed(CHANGED_REPRESENTATIVE_2)
                .build())
        ));

    }

    @Test
    void testAddingRepresentativeRemovedRepresented() {
        List<Element<ChangeOfRepresentation>> actual = underTest.changeRepresentative(
            new ChangeOfRepresentationRequest(List.of(), RESPONDENT, null, REPRESENTATIVE_1, METHOD, ACTIONED_BY));

        assertThat(actual).isEqualTo(List.of(
            element(UUID, changeOfRepresentation()
                .removed(CHANGED_REPRESENTATIVE_1)
                .build())
        ));

    }

    @Test
    void testAddingRepresentativeOrderByDateAsc() {
        Element<ChangeOfRepresentation> yesterdayChange = element(ANOTHER_UUID, changeOfRepresentation()
            .date(TODAY.minusDays(1))
            .build());
        Element<ChangeOfRepresentation> tomorrowChange = element(ANOTHER_UUID, changeOfRepresentation()
            .date(TODAY.plusDays(1))
            .build());
        List<Element<ChangeOfRepresentation>> actual = underTest.changeRepresentative(
            new ChangeOfRepresentationRequest(List.of(yesterdayChange, tomorrowChange),
                RESPONDENT,
                null,
                REPRESENTATIVE_1,
                METHOD,
                ACTIONED_BY));

        assertThat(actual).isEqualTo(List.of(
            yesterdayChange,
            element(UUID, changeOfRepresentation()
                .removed(CHANGED_REPRESENTATIVE_1)
                .build()),
            tomorrowChange
        ));

    }

    private ChangeOfRepresentation.ChangeOfRepresentationBuilder changeOfRepresentation() {
        return ChangeOfRepresentation.builder()
            .respondent(RESPONDENT_FULL_NAME)
            .date(TODAY)
            .by(ACTIONED_BY)
            .via(METHOD.getLabel());
    }
}
