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
import uk.gov.hmcts.reform.fpl.config.HighCourtAdminEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.Orders;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisImages.COURT_SEAL;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_CODE;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.service.CourtLookUpService.RCJ_HIGH_COURT_REGION;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class CourtServiceTest {

    @Mock
    private HmctsCourtLookupConfiguration courtLookup;

    @Mock
    private CtscEmailLookupConfiguration ctscLookup;

    @Mock
    private HighCourtAdminEmailLookupConfiguration highCourtAdminEmailLookupConfiguration;

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

    private final String highCourtEmail = "high.court@test.com";

    private final Court highCourt = Court.builder()
        .code(RCJ_HIGH_COURT_CODE)
        .name(RCJ_HIGH_COURT_NAME)
        .region(RCJ_HIGH_COURT_REGION)
        .build();

    private final Court transferredCourt1 = Court.builder()
        .code("TC1")
        .email("tcourt1@test.com")
        .name("Transferred Court 1")
        .dateTransferred(LocalDateTime.of(2022, Month.JANUARY, 5, 11, 0))
        .region("London")
        .build();

    private final Court transferredCourt2 = Court.builder()
        .code("TC2")
        .email("tcourt2@test.com")
        .name("Transferred Court 2")
        .dateTransferred(LocalDateTime.of(2022, Month.FEBRUARY, 5, 11, 0))
        .region("London")
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
        void shouldReturnCourtForAGivenCourtId() {
            when(courtLookup.getCourtByCode(court1.getCode()))
                    .thenReturn(Optional.of(court1));

            Optional<Court> actualCourt = underTest.getCourt(court1.getCode());
            assertThat(actualCourt).contains(court1);
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
        void shouldReturnCTSCEmailAsDefaultWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualEmail = underTest.getCourtEmail(caseData);

            assertThat(actualEmail).isEqualTo(ctscEmail);
        }
    }

    @Nested
    class GetCourtEmailNotCtsc {

        private final String ctscEmail = "ctsc@test.com";

        @BeforeEach
        void init() {
            when(ctscLookup.getEmail()).thenReturn(ctscEmail);
        }

        @Test
        void shouldReturnNullIfSendToCtscIsSet() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .sendToCtsc(YesNo.YES.getValue())
                .build();

            final String actualEmail = underTest.getCourtEmailNotCtsc(caseData);

            assertThat(actualEmail).isNull();
        }

        @Test
        void shouldReturnDesignatedCourtEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .court(court1)
                .build();

            final String actualEmail = underTest.getCourtEmailNotCtsc(caseData);

            assertThat(actualEmail).isEqualTo(court1.getEmail());
        }

        @Test
        void shouldReturnDefaultCourtEmail() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final String actualEmail = underTest.getCourtEmailNotCtsc(caseData);

            assertThat(actualEmail).isEqualTo(court1.getEmail());
        }

        @Test
        void shouldReturnCTSCEmailAsDefaultWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualEmail = underTest.getCourtEmailNotCtsc(caseData);

            assertThat(actualEmail).isEqualTo(ctscEmail);
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

            final String actualName = underTest.getCourtName(caseData);

            assertThat(actualName).isEqualTo(court1.getName());
        }

        @Test
        void shouldReturnDefaultCourtName() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .build();

            when(courtLookup.getCourts("LA1")).thenReturn(List.of(court1, court2));

            final String actualName = underTest.getCourtName(caseData);

            assertThat(actualName).isEqualTo(court1.getName());
        }

        @Test
        void shouldReturnNullWhenUserDidNotSelectCourt() {
            final CaseData caseData = CaseData.builder()
                .caseLocalAuthority("LA1")
                .multiCourts(YesNo.YES)
                .build();

            final String actualName = underTest.getCourtName(caseData);

            assertThat(actualName).isNull();
        }
    }

    @Nested
    class GetPreviousCourtName {

        @Test
        void shouldReturnPreviousCourtName() {
            final CaseData caseData = CaseData.builder()
                .court(court1)
                .pastCourtList(List.of(element(transferredCourt1)))
                .build();

            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt1.getName());
        }

        @Test
        void shouldReturnPreviousCourtNameWithMoreThanOnePastCourts() {
            final CaseData caseData = CaseData.builder()
                .court(court1)
                .pastCourtList(List.of(element(transferredCourt1), element(transferredCourt2)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt2.getName());
        }

        @Test
        void shouldReturnPreviousCourtNameWithReversedPastCourtList() {
            final CaseData caseData = CaseData.builder()
                .court(court1)
                .pastCourtList(List.of(element(transferredCourt2), element(transferredCourt1)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt2.getName());
        }

        @Test
        void shouldReturnPreviousCourtNameWithNullTransferredDateInPastCourtList() {
            final CaseData caseData = CaseData.builder()
                .court(court2)
                .pastCourtList(List.of(element(transferredCourt2), element(court1)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt2.getName());
        }

        @Test
        void shouldReturnPreviousCourtNameWithNullTransferredDateInReversedPastCourtList() {
            final CaseData caseData = CaseData.builder()
                .court(court2)
                .pastCourtList(List.of(element(court1), element(transferredCourt2)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt2.getName());
        }

        @Test
        void shouldReturnPreviousCourtNameWithSingleNullTransferredDateInPastCourtList() {
            final CaseData caseData = CaseData.builder()
                .court(transferredCourt2)
                .pastCourtList(List.of(element(court1)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(court1.getName());
        }

        @Test
        void shouldReturnNullWithoutPastCourts() {
            final CaseData caseData = CaseData.builder()
                .court(court1)
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isNull();
        }

        @Test
        void shouldReturnNullWithoutCourtAndPastCourts() {
            final CaseData caseData = CaseData.builder()
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isNull();
        }

        @Test
        void shouldReturnWhenUserDidNotSelectCourtWithPastCourtList() {
            final CaseData caseData = CaseData.builder()
                .multiCourts(YesNo.YES)
                .court(court1)
                .pastCourtList(List.of(element(transferredCourt1)))
                .build();
            final String actualName = underTest.getPreviousCourtName(caseData);
            assertThat(actualName).isEqualTo(transferredCourt1.getName());
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
        void shouldReturnCourtInOrdersIfCaseLocalAuthorityIsNull() {
            final CaseData caseData = CaseData.builder()
                .orders(Orders.builder().court("123").build())
                .build();

            when(courtLookup.getCourtByCode("123")).thenReturn(Optional.of(court1));

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

    @Nested
    class HighCourt {

        @Test
        void shouldReturnAsHighCourtCaseWhenCaseIsWithHighCourt() {
            CaseData caseData = CaseData.builder()
                    .court(Court.builder()
                            .code(CourtLookUpService.RCJ_HIGH_COURT_CODE)
                            .build())
                    .build();
            boolean highCourtCase = underTest.isHighCourtCase(caseData);

            assertThat(highCourtCase).isTrue();
        }

        @Test
        void shouldReturnNotAHighCourtCaseWhenCaseIsNotWithHighCourt() {
            CaseData caseData = CaseData.builder()
                    .court(Court.builder()
                            .code("10")
                            .build())
                    .build();
            boolean highCourtCase = underTest.isHighCourtCase(caseData);

            assertThat(highCourtCase).isFalse();
        }

        @Test
        void shouldReturnNotAHighCourtCaseWhenNoCourtAssigned() {
            CaseData caseData = CaseData.builder()
                    .build();
            boolean highCourtCase = underTest.isHighCourtCase(caseData);

            assertThat(highCourtCase).isFalse();
        }

        @Test
        void shouldReturnHighCourtSealWhenOrderIsSubmitted() {
            CaseData caseData = CaseData.builder()
                    .court(Court.builder()
                            .code(CourtLookUpService.RCJ_HIGH_COURT_CODE)
                            .build())
                    .build();
            String courtSeal = underTest.getCourtSeal(caseData, SEALED);

            assertThat(courtSeal).isNull();
        }

        @Test
        void shouldReturnCourtSealWhenOrderIsSubmitted() {
            CaseData caseData = CaseData.builder()
                    .court(Court.builder()
                            .code("0")
                            .build())
                    .build();
            String courtSeal = underTest.getCourtSeal(caseData, SEALED);

            assertThat(courtSeal).isEqualTo(COURT_SEAL.getValue(caseData.getImageLanguage()));
        }

        @Test
        void shouldNotReturnSealWhenDraftOrder() {
            CaseData caseData = CaseData.builder()
                    .court(Court.builder()
                            .code("0")
                            .build())
                    .build();
            String courtSeal = underTest.getCourtSeal(caseData, DRAFT);

            assertThat(courtSeal).isNull();
        }

        @Test
        void shouldGetTheHighCourtEmailWhenRequested() {
            when(highCourtAdminEmailLookupConfiguration.getEmail()).thenReturn(highCourtEmail);
            CaseData caseData = CaseData.builder()
                .court(highCourt)
                .sendToCtsc(NO.getValue())
                .build();

            String email = underTest.getCourtEmail(caseData);
            assertThat(email).isEqualTo(highCourtEmail);
        }

        @Test
        void shouldGetTheHighCourtEmailWhenRequestedWithGetCourtEmailNotCtsc() {
            when(highCourtAdminEmailLookupConfiguration.getEmail()).thenReturn(highCourtEmail);
            CaseData caseData = CaseData.builder()
                .court(highCourt)
                .sendToCtsc(NO.getValue())
                .build();

            String email = underTest.getCourtEmailNotCtsc(caseData);
            assertThat(email).isEqualTo(highCourtEmail);
        }
    }
}
