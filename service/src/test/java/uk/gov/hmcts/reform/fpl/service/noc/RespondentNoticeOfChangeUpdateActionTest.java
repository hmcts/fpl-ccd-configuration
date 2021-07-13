package uk.gov.hmcts.reform.fpl.service.noc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class RespondentNoticeOfChangeUpdateActionTest {

    private final RespondentNoticeOfChangeUpdateAction underTest = new RespondentNoticeOfChangeUpdateAction();

    @Test
    void acceptsValid() {
        assertThat(underTest.accepts(RESPONDENT)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = Representing.class, mode = EnumSource.Mode.EXCLUDE, names = "RESPONDENT")
    void acceptsInvalid(Representing representing) {
        assertThat(underTest.accepts(representing)).isFalse();
    }

    @Test
    void applyUpdates() {
        UUID respondentId = UUID.randomUUID();

        Respondent respondentToUpdate = Respondent.builder().build();
        RespondentSolicitor solicitor = mock(RespondentSolicitor.class);
        CaseData caseData = mock(CaseData.class);

        when(caseData.getAllRespondents()).thenReturn(List.of(element(respondentId, respondentToUpdate)));

        Map<String, Object> data = underTest.applyUpdates(respondentToUpdate, caseData, solicitor);

        Respondent updatedRespondent = Respondent.builder().solicitor(solicitor).legalRepresentation("Yes").build();

        assertThat(data).isEqualTo(Map.of("respondents1", List.of(element(respondentId, updatedRespondent))));
    }
}
