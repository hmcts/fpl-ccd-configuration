package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseNoteService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(AddNoteController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({CaseNoteService.class})
class AddNoteAboutToSubmitControllerTest extends AbstractCallbackTest {

    @MockBean
    private RequestData requestData;

    AddNoteAboutToSubmitControllerTest() {
        super("add-note");
    }

    @BeforeEach
    void setup() {
        super.setUp();
        givenCurrentUserWithName("John Smith");
        given(idamClient.getUserInfo(any())).willReturn(UserInfo.builder().name("John Smith").build());
    }

    @WithMockUser
    @Test
    void shouldAddCaseNoteToList() {
        CaseNote caseNote = caseNote(LocalDate.of(2019, 11, 12), "John Doe", "Existing note");

        CaseData caseData = CaseData.builder()
            .caseNote("Example case note")
            .caseNotes(wrapElements(caseNote))
            .build();

        CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

        assertThat(updatedCaseData.getCaseNote()).isNull();
        assertThat(updatedCaseData.getCaseNotes()).extracting(Element::getValue)
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
