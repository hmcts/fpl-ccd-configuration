package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ParentalResponsibilityType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.C2_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType.OTHER_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.APPOINTMENT_OF_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.fpl.enums.C2ApplicationType.WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C13A_SPECIAL_GUARDIANSHIP;
import static uk.gov.hmcts.reform.fpl.enums.SupplementType.C20_SECURE_ACCOMMODATION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ActiveProfiles("integration-test")
@WebMvcTest(UploadAdditionalApplicationsController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadAdditionalApplicationsMidEventControllerTest extends AbstractCallbackTest {

    @MockBean
    private FeeService feeService;

    UploadAdditionalApplicationsMidEventControllerTest() {
        super("upload-additional-applications");
    }

    @Test
    void shouldCalculateFeeForSelectedOrderBundlesAndAddAmountToPayField() {
        C2DocumentBundle temporaryC2Document = C2DocumentBundle.builder()
            .supplementsBundle(wrapElements(Supplement.builder().name(C13A_SPECIAL_GUARDIANSHIP).build()))
            .build();

        OtherApplicationsBundle temporaryOtherDocument = OtherApplicationsBundle.builder()
            .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
            .parentalResponsibilityType(ParentalResponsibilityType.PR_BY_FATHER)
            .document(DocumentReference.builder().build())
            .supplementsBundle(wrapElements(Supplement.builder()
                .name(C20_SECURE_ACCOMMODATION)
                .secureAccommodationType(SecureAccommodationType.WALES)
                .build()))
            .build();

        Element<Representative> representative = element(Representative.builder()
            .servingPreferences(RepresentativeServingPreferences.EMAIL)
            .email("test@test.com").build());
        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
            .temporaryOtherApplicationsBundle(temporaryOtherDocument)
            .temporaryC2Document(temporaryC2Document)
            .c2Type(WITH_NOTICE)
            .representatives(List.of(representative))
            .respondents1(wrapElements(Respondent.builder().representedBy(wrapElements(representative.getId()))
                .party(RespondentParty.builder().firstName("John").lastName("Smith").build())
                .build()))
            .othersV2(wrapElements(Other.builder().firstName("test1").build(),
                Other.builder().firstName("test2").build()))
            .build();

        List<FeeType> feeTypes = List.of(FeeType.C2_WITH_NOTICE, FeeType.SPECIAL_GUARDIANSHIP,
            FeeType.PARENTAL_RESPONSIBILITY_FATHER, FeeType.SECURE_ACCOMMODATION_WALES);

        given(feeService.getFeesDataForAdditionalApplications(feeTypes))
            .willReturn(FeesData.builder().totalAmount(BigDecimal.TEN).build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "populate-data");

        verify(feeService).getFeesDataForAdditionalApplications(feeTypes);
        assertThat(response.getData())
            .containsKeys("temporaryC2Document", "personSelector")
            .containsEntry("amountToPay", "1000")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldNotSetC2DocumentBundleWhenOnlyOtherApplicationIsSelected() {
        OtherApplicationsBundle temporaryOtherDocument = OtherApplicationsBundle.builder()
            .applicationType(OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN)
            .document(DocumentReference.builder().build())
            .build();

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(OTHER_ORDER))
            .temporaryOtherApplicationsBundle(temporaryOtherDocument)
            .build();

        List<FeeType> feeTypes = List.of(FeeType.APPOINTMENT_OF_GUARDIAN);

        given(feeService.getFeesDataForAdditionalApplications(feeTypes))
            .willReturn(FeesData.builder().totalAmount(BigDecimal.ONE).build());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseData, "get-fee");

        verify(feeService).getFeesDataForAdditionalApplications(feeTypes);
        assertThat(response.getData())
            .containsEntry("temporaryC2Document", null)
            .containsEntry("amountToPay", "100")
            .containsEntry("displayAmountToPay", YES.getValue());
    }

    @Test
    void shouldAddErrorOnFeeRegisterException() {
        given(feeService.getFeesDataForAdditionalApplications(any()))
            .willThrow((new FeeRegisterException(1, "", new Throwable())));

        CaseData caseData = CaseData.builder()
            .additionalApplicationType(List.of(C2_ORDER))
            .temporaryC2Document(C2DocumentBundle.builder().type(WITH_NOTICE).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "populate-data");

        assertThat(response.getData()).containsEntry("displayAmountToPay", NO.getValue());
    }

    @Test
    void shouldDisplayErrorForInvalidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryPbaPayment", Map.of("pbaNumber", "12345")))
            .build(), "validate");

        assertThat(response.getErrors()).contains("Payment by account (PBA) number must include 7 numbers");
        assertThat(response.getData().get("temporaryPbaPayment"))
            .extracting("pbaNumber").isEqualTo("PBA12345");
    }

    @Test
    void shouldNotDisplayErrorForValidPbaNumber() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(CaseDetails.builder()
            .data(Map.of("temporaryPbaPayment", Map.of("pbaNumber", "1234567")))
            .build(), "validate");

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().get("temporaryPbaPayment")).extracting("pbaNumber")
            .isEqualTo("PBA1234567");
    }

    @Test
    void shouldNotValidatePbaNumberWhenPBAPaymentIsNull() {
        AboutToStartOrSubmitCallbackResponse response = postMidEvent(
            CaseDetails.builder().data(Collections.emptyMap()).build(), "validate");

        assertThat(response.getErrors()).isEmpty();
    }

    @Nested
    class C2RequestAdjournment {

        @BeforeEach
        void beforeEach() {
            given(feeService.getFeesDataForAdditionalApplications(any()))
                .willReturn(FeesData.builder().totalAmount(BigDecimal.ONE).build());
        }

        @Test
        void shouldPopulateHearingLabelIfRequestingAdjournment() {
            List<Element<HearingBooking>> hearings = wrapElements(HearingBooking.builder()
                .startDate(now().plusDays(15))
                .type(HearingType.CASE_MANAGEMENT)
                .build());
            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .additionalApplicationType(List.of(C2_ORDER))
                .c2Type(WITH_NOTICE)
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT))
                    .hearingList(asDynamicList(hearings, hearings.get(0).getId(), HearingBooking::toLabel))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "populate-data");

            assertThat(response.getData().get("temporaryC2Document")).extracting("requestedHearingToAdjourn")
                .isEqualTo(hearings.get(0).getValue().toLabel());
            assertThat(response.getData().get("skipPaymentPage")).isEqualTo(YES.getValue());
        }

        @Test
        void shouldPopulateHearingLabelButNotSkipIfRequestingAdjournmentWithOtherOrder() {
            List<Element<HearingBooking>> hearings = wrapElements(HearingBooking.builder()
                .startDate(now().plusDays(15))
                .type(HearingType.CASE_MANAGEMENT)
                .build());
            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
                .c2Type(WITH_NOTICE)
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT))
                    .hearingList(asDynamicList(hearings, hearings.get(0).getId(), HearingBooking::toLabel))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "populate-data");

            assertThat(response.getData().get("temporaryC2Document")).extracting("requestedHearingToAdjourn")
                .isEqualTo(hearings.get(0).getValue().toLabel());
            assertThat(response.getData().get("skipPaymentPage")).isEqualTo(NO.getValue());
        }

        @Test
        void shouldPopulateHearingLabelButNotSkipIfRequestingAdjournmentWithAnotherC2Order() {
            List<Element<HearingBooking>> hearings = wrapElements(HearingBooking.builder()
                .startDate(now().plusDays(15))
                .type(HearingType.CASE_MANAGEMENT)
                .build());
            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .additionalApplicationType(List.of(C2_ORDER))
                .c2Type(WITH_NOTICE)
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(REQUESTING_ADJOURNMENT, APPOINTMENT_OF_GUARDIAN))
                    .hearingList(asDynamicList(hearings, hearings.get(0).getId(), HearingBooking::toLabel))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "populate-data");

            assertThat(response.getData().get("temporaryC2Document")).extracting("requestedHearingToAdjourn")
                .isEqualTo(hearings.get(0).getValue().toLabel());
            assertThat(response.getData().get("skipPaymentPage")).isEqualTo(NO.getValue());
        }


        @Test
        void shouldNotPopulateHearingLabelIfNotRequestingAdjournment() {
            List<Element<HearingBooking>> hearings = wrapElements(HearingBooking.builder()
                .startDate(now().plusDays(15))
                .type(HearingType.CASE_MANAGEMENT)
                .build());
            CaseData caseData = CaseData.builder()
                .hearingDetails(hearings)
                .additionalApplicationType(List.of(C2_ORDER))
                .c2Type(WITH_NOTICE)
                .temporaryC2Document(C2DocumentBundle.builder()
                    .c2AdditionalOrdersRequested(List.of(APPOINTMENT_OF_GUARDIAN))
                    .hearingList(asDynamicList(hearings, hearings.get(0).getId(), HearingBooking::toLabel))
                    .build())
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData),
                "populate-data");

            CaseData updatedCaseData = extractCaseData(CaseDetails.builder().data(response.getData()).build());

            assertThat(updatedCaseData.getTemporaryC2Document()).extracting("requestedHearingToAdjourn")
                .isNull();
            assertThat(response.getData().get("skipPaymentPage")).isEqualTo(NO.getValue());
        }

        @Test
        void shouldNotSkipPaymentIfOtherOrderSelected() {
            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData), "get-fee");
            assertThat(response.getData().get("skipPaymentPage")).isEqualTo(NO.getValue());
        }

    }

    @Nested
    class InitialChoice {

        @Test
        void shouldInitialiseC2DocumentBundleHearingListIfC2Chosen() {
            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData),"initial-choice");

            assertThat(response.getData().get("temporaryC2Document")).isNotNull();
            assertThat(response.getData().get("temporaryC2Document")).extracting("hearingList").isNotNull();
        }

        @Test
        void shouldInitialiseC2DocumentBundleHearingListIfC2AndOtherChosen() {
            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(C2_ORDER, OTHER_ORDER))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData),"initial-choice");

            assertThat(response.getData().get("temporaryC2Document")).isNotNull();
            assertThat(response.getData().get("temporaryC2Document")).extracting("hearingList").isNotNull();
        }

        @Test
        void shouldNotInitialiseC2DocumentBundleHearingListIfOnlyOtherOrderChosen() {
            CaseData caseData = CaseData.builder()
                .additionalApplicationType(List.of(OTHER_ORDER))
                .build();

            AboutToStartOrSubmitCallbackResponse response = postMidEvent(asCaseDetails(caseData),"initial-choice");

            assertThat(response.getData().get("temporaryC2Document")).isNull();
        }

    }

}
