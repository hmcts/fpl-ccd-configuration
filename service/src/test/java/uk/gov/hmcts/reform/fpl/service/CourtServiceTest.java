package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.fpl.config.CtscEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CourtServiceTest {

    @Mock
    private HmctsCourtLookupConfiguration courtLookup;

    @Mock
    private CtscEmailLookupConfiguration ctscLookup;

    @InjectMocks
    private CourtService underTest;

    private final Court court1 = Court.builder()
        .code("C1")
        .email("court1@test.com")
        .name("Court 1")
        .build();

    private final Court court2 = Court.builder()
        .code("C2")
        .email("court2@test.com")
        .name("Court 2")
        .build();

    @Nested
    class GetCourt {

        @Test
        void shouldReturnDesignatedCourt() {

            final CaseData caseData = CaseData.builder()
                .court(court1)
                .build();

            final Court actualCourt = underTest.getCourt(caseData);

            assertThat(actualCourt).isEqualTo(court1);
        }

        @Test
        void shouldReturnDefaultCourt() {

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final Court actualCourt = underTest.getCourt(caseData);

            assertThat(actualCourt).isEqualTo(court1);
        }

        @Test
        void shouldReturnNullWhenUserDidNotSelectCourt() {

            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final Court actualCourt = underTest.getCourt(caseData);

            assertThat(actualCourt).isNull();
        }

    }

    @Nested
    class GetCourtEmail {

        private final String ctscEmail = "ctsc@test.com";

        @BeforeEach
        void init() {
            when(ctscLookup.getEmail()).thenReturn(ctscEmail);
        }

        @Test
        void shouldReturnCtscEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            final String actualEmail = underTest.getCourtEmail(caseData);

            assertThat(actualEmail).isEqualTo(ctscEmail);
        }

        @Test
        void shouldReturnDesignatedCourtEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .court(court1)
                .build();

            final String actualEmail = underTest.getCourtEmail(caseData);

            assertThat(actualEmail).isEqualTo(court1.getEmail());
        }

        @Test
        void shouldReturnDefaultCourtEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final String actualEmail = underTest.getCourtEmail(caseData);

            assertThat(actualEmail).isEqualTo(court1.getEmail());
        }

        @Test
        void shouldReturnNullWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualEmail = underTest.getCourtEmail(caseData);

            assertThat(actualEmail).isNull();
        }
    }

    @Nested
    class GetCourtName {

        @Test
        void shouldReturnDesignatedCourtName() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .court(court1)
                .build();

            final String actualEmail = underTest.getCourtName(caseData);

            assertThat(actualEmail).isEqualTo(court1.getName());
        }

        @Test
        void shouldReturnDefaultCourtEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final String actualEmail = underTest.getCourtName(caseData);

            assertThat(actualEmail).isEqualTo(court1.getName());
        }

        @Test
        void shouldReturnNullWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualEmail = underTest.getCourtName(caseData);

            assertThat(actualEmail).isNull();
        }
    }

    @Nested
    class GetCourtCode {

        @Test
        void shouldReturnDesignatedCourtCode() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .court(court1)
                .build();

            final String actualEmail = underTest.getCourtCode(caseData);

            assertThat(actualEmail).isEqualTo(court1.getCode());
        }

        @Test
        void shouldReturnDefaultCourtCode() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final String actualEmail = underTest.getCourtCode(caseData);

            assertThat(actualEmail).isEqualTo(court1.getCode());
        }

        @Test
        void shouldReturnNullWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualEmail = underTest.getCourtCode(caseData);

            assertThat(actualEmail).isNull();
        }
    }

}
