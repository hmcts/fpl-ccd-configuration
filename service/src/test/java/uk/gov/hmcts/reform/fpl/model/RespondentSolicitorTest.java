package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Test;

public class RespondentSolicitorTest {
    private static final String SOLICITOR_FIRST_NAME = "John";
    private static final String SOLICITOR_LAST_NAME = "Smith";

    @Test
    void shouldReturnFullNameWhenFullNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        String expectedName = String.format("%s %s", SOLICITOR_FIRST_NAME, SOLICITOR_LAST_NAME);

        assert (respondentSolicitor.getFullName()).equals(expectedName);
    }

    @Test
    void shouldReturnFullNameWhenOnlyFirstNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .firstName(SOLICITOR_FIRST_NAME)
            .build();

        assert (respondentSolicitor.getFullName()).equals(SOLICITOR_FIRST_NAME);
    }

    @Test
    void shouldReturnFullNameWhenOnlyLastNameProvided() {
        RespondentSolicitor respondentSolicitor = RespondentSolicitor.builder()
            .lastName(SOLICITOR_LAST_NAME)
            .build();

        assert (respondentSolicitor.getFullName()).equals(SOLICITOR_LAST_NAME);
    }
}
