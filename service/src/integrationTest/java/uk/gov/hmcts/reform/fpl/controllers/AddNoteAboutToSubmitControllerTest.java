package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(AddNoteController.class)
@OverrideAutoConfiguration(enabled = true)
class AddNoteAboutToSubmitControllerTest extends AbstractControllerTest {

    @MockBean
    IdamClient idamClient;

    AddNoteAboutToSubmitControllerTest() {
        super("add-note");
    }

    @BeforeEach
    void setup() {
        when(idamClient.getUserInfo(USER_AUTH_TOKEN)).thenReturn(UserInfo.builder().name("John Smith").build());
    }

    @Test
    void shouldAddCaseNoteToList() {
        CaseNote caseNote = caseNote(LocalDate.of(2019, 11, 12), "John Doe", "Existing note");

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "caseNote", "Example case note",
                "caseNotes", wrapElements(caseNote)))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getCaseNote()).isNull();
        assertThat(unwrapElements(caseData.getCaseNotes()))
            .containsExactly(caseNote, caseNote(dateNow(), "John Smith", "Example case note"));
    }

    private CaseNote caseNote(LocalDate date, String createdBy, String note) {
        return CaseNote.builder()
            .date(date)
            .createdBy(createdBy)
            .note(note)
            .build();
    }
}
