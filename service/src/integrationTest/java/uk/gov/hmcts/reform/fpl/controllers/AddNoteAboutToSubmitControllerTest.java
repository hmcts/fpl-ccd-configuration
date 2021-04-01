package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@WebMvcTest(AddNoteController.class)
@OverrideAutoConfiguration(enabled = true)
class AddNoteAboutToSubmitControllerTest extends AbstractCallbackTest {

    AddNoteAboutToSubmitControllerTest() {
        super("add-note");
    }

    @BeforeEach
    void setup() {
        givenCurrentUserWithName("John Smith");
    }

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
