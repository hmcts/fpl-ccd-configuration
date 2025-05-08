package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseNote;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { CaseNoteService.class, FixedTimeConfiguration.class})
class CaseNoteServiceTest {

    @Autowired
    private Time time;

    @MockBean
    private IdamClient idamClient;

    private CaseNoteService service;

    private static final String USER_AUTH_TOKEN = "Bearer";
    private static final UserInfo userDetails = UserInfo.builder().name("John Smith").build();

    @BeforeEach
    void setUp() {
        service = new CaseNoteService(idamClient, time);
        given(idamClient.getUserInfo(USER_AUTH_TOKEN)).willReturn(userDetails);
    }

    @Nested
    class BuildCaseNote {

        @ParameterizedTest
        @ValueSource(strings = {"new note"})
        @NullAndEmptySource
        void shouldBuildExpectedCaseNote(String note) {
            CaseNote caseNote = service.buildCaseNote(USER_AUTH_TOKEN, note);

            assertThat(caseNote).isEqualTo(caseNoteForToday(note));
        }
    }

    @Test
    void shouldAddNoteToListWhenNullList() {
        CaseNote caseNote = caseNoteForToday("new note");
        List<Element<CaseNote>> caseNotes = service.addNoteToList(caseNote, null);

        assertThat(unwrapElements(caseNotes)).contains(caseNote);
    }

    @Test
    void shouldAddNoteToListWhenEmptyList() {
        CaseNote caseNote = caseNoteForToday("new note");
        List<Element<CaseNote>> caseNotes = service.addNoteToList(caseNote, new ArrayList<>());

        assertThat(unwrapElements(caseNotes)).contains(caseNote);
    }

    @Test
    void shouldAddNoteToListWithNewestAtBottomWhenExistingNotes() {
        LocalDate today = time.now().toLocalDate();
        CaseNote newNote = caseNoteWithDate(today);
        CaseNote oldNote = caseNoteWithDate(today.minusDays(5));

        List<Element<CaseNote>> caseNotes = service.addNoteToList(newNote, wrapElements(oldNote));

        assertThat(unwrapElements(caseNotes)).isEqualTo(List.of(oldNote, newNote));
    }

    @Test
    void shouldRemoveCaseNote() {
        Element<CaseNote> caseNote = element(caseNoteForToday("Note to remove"));
        List<Element<CaseNote>> existingNotes = List.of(caseNote);

        List<Element<CaseNote>> result = service.removeCaseNote(caseNote.getId(), existingNotes);

        assertThat(result).doesNotContain(caseNote).isEmpty();
    }

    private CaseNote caseNoteForToday(String note) {
        return CaseNote.builder()
            .note(note)
            .createdBy(userDetails.getName())
            .date(time.now().toLocalDate())
            .build();
    }

    private CaseNote caseNoteWithDate(LocalDate date) {
        return CaseNote.builder()
            .note("note")
            .createdBy(userDetails.getName())
            .date(date)
            .build();
    }
}
