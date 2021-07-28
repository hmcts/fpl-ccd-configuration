package uk.gov.hmcts.reform.fpl.controllers.courtselection;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.controllers.AddNoteController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;

import static org.apache.commons.lang3.tuple.Pair.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_COURT_A_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_COURT_A_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_COURT_B_ID;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_3_COURT_B_NAME;

@WebMvcTest(AddNoteController.class)
@OverrideAutoConfiguration(enabled = true)
class CourtSelectionControllerAboutToStartTest extends AbstractCallbackTest {

    CourtSelectionControllerAboutToStartTest() {
        super("select-court");
    }

    @Test
    void shouldAddListOfCourts() {

        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_3_CODE)
            .build();

        final DynamicList expectedCourtsList = dynamicLists.from(
            of(LOCAL_AUTHORITY_3_COURT_A_NAME, LOCAL_AUTHORITY_3_COURT_A_ID),
            of(LOCAL_AUTHORITY_3_COURT_B_NAME, LOCAL_AUTHORITY_3_COURT_B_ID));

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(updatedCaseData.getCourtsList()).isEqualTo(expectedCourtsList);
    }

    @Test
    void shouldAddListOfCourtsWithPreselectedCourt() {

        final CaseData caseData = CaseData.builder()
            .caseLocalAuthority(LOCAL_AUTHORITY_3_CODE)
            .court(Court.builder()
                .code(LOCAL_AUTHORITY_3_COURT_A_ID)
                .name(LOCAL_AUTHORITY_3_COURT_A_NAME)
                .build())
            .build();

        final DynamicList expectedCourtsList = dynamicLists.from(0,
            of(LOCAL_AUTHORITY_3_COURT_A_NAME, LOCAL_AUTHORITY_3_COURT_A_ID),
            of(LOCAL_AUTHORITY_3_COURT_B_NAME, LOCAL_AUTHORITY_3_COURT_B_ID));

        final CaseData updatedCaseData = extractCaseData(postAboutToStartEvent(caseData));

        assertThat(updatedCaseData.getCourtsList()).isEqualTo(expectedCourtsList);
    }
}
