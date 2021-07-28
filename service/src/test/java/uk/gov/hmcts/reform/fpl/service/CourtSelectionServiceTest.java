package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtSelectionServiceTest {

    private final Court court1 = Court.builder()
        .code("C1")
        .name("Court 1")
        .email("court@test.com")
        .build();

    private final Court court2 = Court.builder()
        .code("C1")
        .name("Court 1")
        .email("court@test.com")
        .build();

    private final List<Court> courts = List.of(court1, court2);

    @Mock
    private HmctsCourtLookupConfiguration courtLookup;

    @Mock
    private DynamicListService dynamicListService;

    @InjectMocks
    private CourtSelectionService underTest;

    @Nested
    class CourtList {

        @Test
        void shouldReturnListOfCourts() {

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            final DynamicList dynamicList = DynamicList.builder()
                .listItems(List.of(DynamicListElement.defaultListItem("Court 1")))
                .build();

            when(courtLookup.getCourt(caseData.getCaseLocalAuthority())).thenReturn(courts);
            when(dynamicListService.asDynamicList(eq(courts), eq(null), any(), any()))
                .thenReturn(dynamicList);

            assertThat(underTest.getCourtsList(caseData)).isEqualTo(dynamicList);
        }

        @Test
        void shouldReturnListOfCourtsWithPreselectedCourt() {

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .court(court2)
                .build();

            final DynamicList dynamicList = DynamicList.builder()
                .listItems(List.of(DynamicListElement.defaultListItem("Court 1")))
                .build();

            when(courtLookup.getCourt(caseData.getCaseLocalAuthority())).thenReturn(courts);
            when(dynamicListService.asDynamicList(eq(courts), eq(court2.getCode()), any(), any()))
                .thenReturn(dynamicList);

            assertThat(underTest.getCourtsList(caseData)).isEqualTo(dynamicList);
        }
    }


    @Nested
    class SelectedCourt {

        @Test
        void shouldReturnSelectedCourt() {

            final DynamicList dynamicList = DynamicList.builder()
                .value(listElement(court2))
                .listItems(List.of(listElement(court1), listElement(court2)))
                .build();

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .courtsList(dynamicList)
                .build();

            when(courtLookup.getCourt(caseData.getCaseLocalAuthority())).thenReturn(courts);

            assertThat(underTest.getSelectedCourt(caseData)).isEqualTo(court2);
        }

        @Test
        void shouldReturnNullWhenNoCourtSelected() {

            final DynamicList dynamicList = DynamicList.builder()
                .value(null)
                .listItems(List.of(listElement(court1), listElement(court2)))
                .build();

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .courtsList(dynamicList)
                .build();

            when(courtLookup.getCourt(caseData.getCaseLocalAuthority())).thenReturn(courts);

            assertThat(underTest.getSelectedCourt(caseData)).isNull();
        }

    }

    private static DynamicListElement listElement(Court court) {
        return DynamicListElement.builder()
            .code(court.getCode())
            .label(court.getName())
            .build();
    }
}
