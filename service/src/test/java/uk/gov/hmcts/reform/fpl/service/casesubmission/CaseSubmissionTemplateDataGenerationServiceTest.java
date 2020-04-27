package uk.gov.hmcts.reform.fpl.service.casesubmission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrderDirectionsType;
import uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.GroundsForEPO;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Document;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.DocumentSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.service.UserDetailsService;

import java.io.IOException;
import java.time.LocalDate;

import static java.time.LocalDate.now;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.config.utils.EmergencyProtectionOrdersType.CHILD_WHEREABOUTS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.HOSPITAL_SOON_TO_BE_DISCHARGED;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.REMOVED_BY_POLICE_POWER_ENDS;
import static uk.gov.hmcts.reform.fpl.enums.ChildLivingSituation.VOLUNTARILY_SECTION_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.DONT_KNOW;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.service.casesubmission.SampleCaseSubmissionTestDataHelper.expectedDocmosisCaseSubmission;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseSubmissionTemplateDataGenerationService.class, JacksonAutoConfiguration.class})
public class CaseSubmissionTemplateDataGenerationServiceTest {
    private static final LocalDate NOW = now();

    private static final String FORMATTED_DATE = formatLocalDateToString(NOW, DATE);

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseSubmissionTemplateDataGenerationService templateDataGenerationService;

    private CaseData givenCaseData;

    @BeforeEach
    void init() {
        givenCaseData = prepareCaseData();
        given(userDetailsService.getUserName()).willReturn("Professor");
    }

    @Test
    void shouldReturnExpectedTemplateDataWithCourtSealWhenAllDataPresent() throws IOException {
        DocmosisCaseSubmission returnedCaseSubmission = templateDataGenerationService.getTemplateData(givenCaseData);
        assertThat(returnedCaseSubmission).isEqualToComparingFieldByField(expectedDocmosisCaseSubmission());
    }

    @Nested
    class DocmosisCaseSubmissionOrdersNeededTest {
        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderGiven() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .otherOrder("expected other order")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\nexpected other order";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededWithOtherOrderAppendedWhenOtherOrderAndWithOrderTypesGiven() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .otherOrder("expected other order")
                    .orderType(of(OrderType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Care order\n"
                + "Interim care order\n"
                + "Supervision order\n"
                + "Interim supervision order\n"
                + "Education supervision order\n"
                + "Emergency protection order\n"
                + "Variation or discharge of care or supervision order\n"
                + "expected other order";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededWithAppendedEmergencyProtectionOrdersTypesWhenEmergencyProtectionOrdersTypesGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrders(of(EmergencyProtectionOrdersType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Information on the whereabouts of the child\n"
                + "Authorisation for entry of premises\n"
                + "Authorisation to search for another child on the premises\n"
                + "Other order under section 48 of the Children Act 1989";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }

        @Test
        void shouldReturnOrdersNeededAppendedEmergencyProtectionOrderDetailsWhenEmergencyProtectionOrderDetailsGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrders(of(CHILD_WHEREABOUTS))
                    .emergencyProtectionOrderDetails("emergency protection order details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedOrdersNeeded = "Emergency protection order\n"
                + "Information on the whereabouts of the child\n"
                + "emergency protection order details";
            assertThat(caseSubmission.getOrdersNeeded()).isEqualTo(expectedOrdersNeeded);
        }
    }

    @Nested
    class DocmosisCaseSubmissionGroundsForEPOReasonTest {

        @Test
        void shouldReturnEmptyWhenOrderTypesAreEmpty() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("");
        }

        @Test
        void shouldReturnEmptyWhenEPOIsNotInOrderType() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EDUCATION_SUPERVISION_ORDER))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("");
        }

        @Test
        void shouldReturnEmptyWhenOrderTypeEPOAndGroundsForEPOIsEmpty() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("-");
        }

        @Test
        void shouldReturnEmptyWhenOrderTypeEPOAndGroundsForEPOReasonIsEmpty() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(GroundsForEPO.builder()
                    .reason(of())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason()).isEqualTo("-");
        }

        @Test
        void shouldReturnGroundsForEPOReasonWhenOrderTypeEPOAndGroundsForEPOReasonIsNotEmpty() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .orderType(of(OrderType.CARE_ORDER,
                        OrderType.EMERGENCY_PROTECTION_ORDER))
                    .build())
                .groundsForEPO(GroundsForEPO.builder()
                    .reason(of("HARM_IF_KEPT_IN_CURRENT_ACCOMMODATION",
                        "URGENT_ACCESS_TO_CHILD_IS_OBSTRUCTED"))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getGroundsForEPOReason())
                .isEqualTo("There’s reasonable cause to believe the child is likely to suffer significant "
                    + "harm if they don’t stay in their current accommodation\nYou’re making enquiries and "
                    + "need urgent access to the child to find out about their welfare, and access is being "
                    + "unreasonably refused");
        }
    }

    @Nested
    class DocmosisCaseSubmissionDirectionsNeededTest {
        @Test
        void shouldReturnDirectionsNeededWithAppendedEmergencyProtectionOrderDirectionDetails() throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .emergencyProtectionOrderDirectionDetails("direction details")
                    .emergencyProtectionOrderDirections(of(EmergencyProtectionOrderDirectionsType.values()))
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "Contact with any named person\n"
                + "A medical or psychiatric examination, or another assessment of the child\n"
                + "To be accompanied by a registered medical practitioner, nurse or midwife\n"
                + "An exclusion requirement\n"
                + "Other direction relating to an emergency protection order\n"
                + "direction details\n"
                + "Yes";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }

        @Test
        void shouldReturnDirectionsNeededWithAppendedDirectionsAndDirectionDetailsWhenGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .orders(givenCaseData.getOrders().toBuilder()
                    .directions("directions")
                    .directionDetails("direction  details")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedDirectionsNeeded = "Contact with any named person\n"
                + "directions\n"
                + "direction  details";
            assertThat(caseSubmission.getDirectionsNeeded()).isEqualTo(expectedDirectionsNeeded);
        }
    }

    @Nested
    class DocmosisCaseSubmissionLivingSituationTest {
        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyForLivingSituationWhenChildLivingSituationIsEmptyOrNull(final String livingSituation)
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(livingSituation)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getChildren()).hasSize(1);
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo("-");
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsInHospitalSoonToBeDischarged()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(HOSPITAL_SOON_TO_BE_DISCHARGED.getValue())
                        .dischargeDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "In hospital and soon to be discharged\nDischarge date: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsRemovedByPolicePowerEnds()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(REMOVED_BY_POLICE_POWER_ENDS.getValue())
                        .datePowersEnd(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Removed by Police, powers ending soon\nDate powers end: "
                + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsVoluntarySectionCareOrder()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation(VOLUNTARILY_SECTION_CARE_ORDER.getValue())
                        .careStartDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Voluntarily in section 20 care order\nDate this began: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }

        @Test
        void shouldReturnCorrectlyFormattedLivingSituationWhenSituationIsOther()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .children1(wrapElements(Child.builder()
                    .party(ChildParty.builder()
                        .livingSituation("Other")
                        .addressChangeDate(NOW)
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            String expectedLivingSituation = "Other\nDate this began: " + FORMATTED_DATE;
            assertThat(caseSubmission.getChildren().get(0).getLivingSituation()).isEqualTo(expectedLivingSituation);
        }
    }

    @Nested
    class DocmosisCaseSubmissionBuildRespondentTest {
        @Test
        void shouldNotReturnRespondentConfidentialDetailsWhenContactDetailsHiddenIsSetToYes()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .address(Address.builder()
                            .postcode("SL11GF")
                            .build())
                        .contactDetailsHidden("Yes")
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("080-90909090")
                            .build())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRespondents()).hasSize(1);
            assertThat(caseSubmission.getRespondents().get(0).getAddress()).isEqualTo("Confidential");
            assertThat(caseSubmission.getRespondents().get(0).getTelephoneNumber()).isEqualTo("Confidential");
        }

        @Test
        void shouldReturnRespondentAddressAndTelephoneDetailsWhenContactDetailsHiddenIsSetToNo()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .respondents1(wrapElements(Respondent.builder()
                    .party(RespondentParty.builder()
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .contactDetailsHidden("No")
                        .telephoneNumber(Telephone.builder()
                            .telephoneNumber("080-90909090")
                            .build())
                        .build())
                    .build()))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRespondents()).hasSize(1);
            assertThat(caseSubmission.getRespondents().get(0).getAddress()).isEqualTo("Flat 13\nSL11GF");
            assertThat(caseSubmission.getRespondents().get(0).getTelephoneNumber()).isEqualTo("080-90909090");
        }
    }

    @Nested
    class DocmosisCaseSubmissionBuildOtherPartyTest {
        @Test
        void shouldNotReturnOtherPartyConfidentialDetailsWhenDetailsHiddenIsSetToYes()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .detailsHidden("yes")
                        .telephone("090-0999000")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getAddress()).isEqualTo("Confidential");
            assertThat(caseSubmission.getOthers().get(0).getTelephoneNumber()).isEqualTo("Confidential");
        }

        @Test
        void shouldReturnOtherPartyAddressAndTelephoneDetailsWhenDetailsHiddenIsSetToNo()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .address(Address.builder()
                            .addressLine1("Flat 13")
                            .postcode("SL11GF")
                            .build())
                        .detailsHidden("no")
                        .telephone("090-0999000")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getAddress()).isEqualTo("Flat 13\nSL11GF");
            assertThat(caseSubmission.getOthers().get(0).getTelephoneNumber()).isEqualTo("090-0999000");
        }

        @Test
        void shouldReturnOtherPartyDOBAsDefaultStringWhenDOBIsNull()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getDateOfBirth()).isEqualTo("-");
        }

        @Test
        void shouldReturnOtherPartyFormattedDOBAsWhenDOBIsGiven()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .others(Others.builder()
                    .firstOther(Other.builder()
                        .DOB("1999-02-02")
                        .build())
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getOthers()).hasSize(1);
            assertThat(caseSubmission.getOthers().get(0).getDateOfBirth()).isEqualTo("2 February 1999");
        }
    }

    @Nested
    class DocmosisCaseSubmissionGetValidAnswerOrDefaultValueTest {

        @Test
        void shouldReturnRelevantProceedingAsEmptyWhenGivenProceedingsAreEmpty()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo("-");
        }

        @Test
        void shouldReturnRelevantProceedingAsYesWhenGivenOnGoingProceedingIsYes()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("yes")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(YES.getValue());
        }

        @Test
        void shouldReturnRelevantProceedingAsNoWhenGivenOnGoingProceedingIsYes()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("no")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(NO.getValue());
        }

        @Test
        void shouldReturnRelevantProceedingAsDontKnowWhenGivenOnGoingProceedingIsDontKnow()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .proceeding(Proceeding.builder()
                    .onGoingProceeding("Don't know")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getRelevantProceedings()).isEqualTo(DONT_KNOW.getValue());
        }
    }

    @Nested
    class DocmosisCaseSubmissionFormatAnnexDocumentDisplayTest {

        @Test
        void shouldReturnEmptyWhenDocumentIsNotAvailable()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(null)
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("-");
        }

        @Test
        void shouldReturnEmptyWhenDocumentStatusIsEmpty()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("-");
        }

        @Test
        void shouldReturnStatusWhenDocumentStatusIsAvailable()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("Attached")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology()).isEqualTo("Attached");
        }

        @Test
        void shouldReturnStatusAndReasonWhenDocumentStatusIsOtherThanAttached()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .socialWorkChronologyDocument(Document.builder()
                    .documentStatus("To follow")
                    .statusReason("Documents not uploaded")
                    .build())
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getSocialWorkChronology())
                .isEqualTo("To follow\nDocuments not uploaded");
        }

        @Test
        void shouldReturnDocumentTitleOrDefaultValueForAdditionalAnnexDocuments()
            throws IOException {
            CaseData updatedCaseData = givenCaseData.toBuilder()
                .otherSocialWorkDocuments(wrapElements(DocumentSocialWorkOther.builder()
                        .documentTitle("Additional Doc 1")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build(),
                    DocumentSocialWorkOther.builder()
                        .documentTitle("Additional Doc 2")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build(),
                    DocumentSocialWorkOther.builder()
                        .documentTitle("")
                        .typeOfDocument(DocumentReference.builder()
                            .url("/test.doc")
                            .build())
                        .build()
                ))
                .build();

            DocmosisCaseSubmission caseSubmission = templateDataGenerationService.getTemplateData(updatedCaseData);

            assertThat(caseSubmission.getAnnexDocuments().getOthers()).hasSize(3);
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(0).getDocumentTitle())
                .isEqualTo("Additional Doc 1");
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(1).getDocumentTitle())
                .isEqualTo("Additional Doc 2");
            assertThat(caseSubmission.getAnnexDocuments().getOthers().get(2).getDocumentTitle()).isEqualTo("-");
        }
    }

    private CaseData prepareCaseData() {
        CaseData caseData = objectMapper.convertValue(populatedCaseDetails().getData(), CaseData.class);
        caseData.setDateSubmitted(now());

        return caseData;
    }
}
