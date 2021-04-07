package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractCallbackTest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseAccessService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LABARRISTER;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
class MigrateCaseControllerTest extends AbstractCallbackTest {

    @MockBean
    private CaseAccessService caseAccessService;

    MigrateCaseControllerTest() {
        super("migrate-case");
    }


    @Nested
    class Fpla2960 {
        String familyManNumber = "YO21C50001";
        String migrationId = "FPLA-2960";
        String userId = "d81bba10-dba7-4a2e-8bb8-b372407c1fba";
        UUID elementId = UUID.fromString("f2b1e4ee-287e-4257-9413-8f61d61af27f");
        Long caseId = 10L;

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseData caseData = CaseData.builder()
                .familyManCaseNumber("test")
                .build();

            CaseDetails caseDetails = caseDetails(migrationId, caseData);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Unexpected FMN test");

            verify(caseAccessService, never()).revokeCaseRoleFromUser(any(), any(), any());
        }

        @Test
        void shouldThrowExceptionWhenNoLegalRepresentatives() {
            CaseData caseData = CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .legalRepresentatives(null)
                .build();

            CaseDetails caseDetails = caseDetails(migrationId, caseData);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Empty list of legal representatives");

            verify(caseAccessService, never()).revokeCaseRoleFromUser(any(), any(), any());
        }

        @Test
        void shouldRemoveLegalRepresentative() {

            Element<LegalRepresentative> legalRepresentative1 = element(randomUUID(),
                LegalRepresentative.builder()
                    .email("test1@test.com")
                    .build());

            Element<LegalRepresentative> legalRepresentative2 = element(elementId,
                LegalRepresentative.builder()
                    .email("test2@test.com")
                    .build());

            Element<LegalRepresentative> legalRepresentative3 = element(randomUUID(),
                LegalRepresentative.builder()
                    .email("test3@test.com")
                    .build());

            CaseData caseData = CaseData.builder()
                .id(caseId)
                .familyManCaseNumber(familyManNumber)
                .legalRepresentatives(List.of(legalRepresentative1, legalRepresentative2, legalRepresentative3))
                .build();

            CaseDetails caseDetails = caseDetails(migrationId, caseData);

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(updatedCaseData.getLegalRepresentatives())
                .containsExactly(legalRepresentative1, legalRepresentative3);

            verify(caseAccessService).revokeCaseRoleFromUser(caseId, userId, LABARRISTER);
        }

        private CaseDetails caseDetails(String migrationId, CaseData caseData) {
            CaseDetails caseDetails = asCaseDetails(caseData);

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2946 {
        String familyManNumber = "WR21C50052";
        String migrationId = "FPLA-2946";

        private CaseDetails caseDetails(String migrationId,
                                        CaseData caseData) {
            CaseDetails caseDetails = asCaseDetails(caseData);

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseData caseData = CaseData.builder()
                .id(10L)
                .familyManCaseNumber("test")
                .caseLocalAuthority("SA")
                .caseLocalAuthorityName("Swansea County Council")
                .build();

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails(migrationId, caseData)))
                .getRootCause()
                .hasMessage("Unexpected FMN test");
        }


        @Test
        void shouldGrantCaseAccess() {
            CaseData caseData = CaseData.builder()
                .id(10L)
                .familyManCaseNumber(familyManNumber)
                .build();

            postAboutToSubmitEvent(caseDetails(migrationId, caseData));

            verify(caseAccessService).grantCaseRoleToUsers(
                caseData.getId(),
                Set.of(
                    "be3f6712-b01f-49d6-892a-7b00bdb24f5f",
                    "c596b0b5-d32c-46a8-b92b-e5a9ebef6bb6",
                    "9fe14653-897e-4643-85bc-96ab3aeb453c",
                    "47533fd8-293a-481a-ba61-b6d1f1a63cea",
                    "fc1b1c0f-2a2d-463d-9977-ff74ce336fb0",
                    "cdf6313c-7948-44c2-86d8-8444222e079f",
                    "0286746f-5e5e-4509-b25b-fd2c92d015c5",
                    "b6530997-ae3f-4170-8c98-70cb42acb475",
                    "2e28afcc-6d81-4695-9a5d-9cfb9a560d5f",
                    "d2b6f10e-1296-4b45-aa62-733d1e76ec33",
                    "bb3ae7ac-0781-4531-814d-652c52a06eb5",
                    "de58407e-7c60-4a22-92a2-1044a8e214c5",
                    "943d9dc2-8b50-4ee2-8afa-2e95bbf1a73b",
                    "44921108-0c72-4479-bb59-b82cdbd85806",
                    "affe04bc-7a13-4563-9995-4200c8548800",
                    "8167cf9e-8847-45a0-a2d2-a55dd8187ea6",
                    "5eeca97a-fad6-478b-bd73-8fe01a10532e",
                    "d981d8fc-a9ab-48ed-afe1-508ad955c7e5",
                    "3e40f009-75ba-4a7d-b9a4-d06f21f067b6",
                    "82f45661-6067-4a23-95e1-dd4818eeb4f5",
                    "133319ee-cd34-4f8b-953e-624b59263e3b",
                    "5564fd08-d823-4a55-86bf-d72a7e1664dc",
                    "67b61f34-2e2a-4801-a482-eaa10a843538",
                    "aeb944b6-9e66-4821-9126-d6b397a7cad2",
                    "365c2f8d-cc77-4f95-9f28-691252af630f",
                    "8a9b7a70-64c3-4931-9325-2a9cff917263",
                    "c3612b21-c8a5-41fa-8bb6-5c996e00ec1c",
                    "32115f54-0485-4c39-99aa-f51c68c08673"
                ),
                LASOLICITOR);
        }
    }

    @Nested
    class Fpla2774 {
        String familyManNumber = "NE21C50007";
        String migrationId = "FPLA-2774";
        UUID hearingIdOne = randomUUID();
        UUID hearingIdTwo = randomUUID();
        HearingBooking hearingBooking = HearingBooking.builder().build();

        @Test
        void shouldRemoveSecondHearing() {
            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(migrationId, familyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(List.of(hearingOne));
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(incorrectMigrationId, familyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedFamilyManCaseNumber() {
            String invalidFamilyManNumber = "PO20C50031";

            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            Element<HearingBooking> hearingTwo = element(hearingIdTwo, hearingBooking);

            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne, hearingTwo);
            CaseDetails caseDetails = caseDetails(migrationId, invalidFamilyManNumber, hearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getHearingDetails()).isEqualTo(hearingBookings);
        }

        @Test
        void shouldThrowAnExceptionIfCaseContainsFewerHearingsThanExpected() {
            Element<HearingBooking> hearingOne = element(hearingIdOne, hearingBooking);
            List<Element<HearingBooking>> hearingBookings = newArrayList(hearingOne);

            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, hearingBookings);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected 2 hearings in the case but found 1");
        }

        @Test
        void shouldThrowAnExceptionIfCaseDoesNotContainHearings() {
            CaseDetails caseDetails = caseDetails(
                migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No hearings in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<HearingBooking>> hearingBookings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .hearingDetails(hearingBookings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2905 {
        String familyManNumber = "CF20C50047";
        String migrationId = "FPLA-2905";

        @Test
        void shouldRemoveSecondC2DocumentBundle() {
            UUID elementIdOne = randomUUID();
            UUID elementIdTwo = randomUUID();

            Element<C2DocumentBundle> c2DocumentBundleElementOne = element(elementIdOne,
                C2DocumentBundle.builder().build());

            Element<C2DocumentBundle> c2DocumentBundleElementTwo = element(elementIdTwo,
                C2DocumentBundle.builder().build());

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, List.of(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo));

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updatedCaseData.getC2DocumentBundle()).containsExactly(c2DocumentBundleElementOne);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            UUID elementIdOne = randomUUID();
            UUID elementIdTwo = randomUUID();

            Element<C2DocumentBundle> c2DocumentBundleElementOne = element(elementIdOne,
                C2DocumentBundle.builder().build());

            Element<C2DocumentBundle> c2DocumentBundleElementTwo = element(elementIdTwo,
                C2DocumentBundle.builder().build());

            CaseDetails caseData = caseDetails(incorrectMigrationId, familyManNumber,
                List.of(c2DocumentBundleElementOne, c2DocumentBundleElementTwo));

            List<Element<C2DocumentBundle>> expectedBundle = List.of(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo);

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updatedCaseData.getC2DocumentBundle()).isEqualTo(expectedBundle);
        }

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseDetails caseData = caseDetails(migrationId, "test", emptyList());

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Unexpected FMN test");
        }

        @Test
        void shouldThrowExceptionWhenC2documentBundleIsMissing() {
            CaseDetails caseData = caseDetails(migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("No C2 document bundles in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<C2DocumentBundle>> bundles) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .c2DocumentBundle(bundles)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2872 {
        String familyManNumber = "NE20C50023";
        String migrationId = "FPLA-2872";
        UUID elementIdOne = randomUUID();
        UUID elementIdTwo = randomUUID();
        UUID elementIdThree = randomUUID();
        UUID elementIdFour = randomUUID();
        UUID elementIdFive = randomUUID();
        UUID elementIdSix = randomUUID();

        @Test
        void shouldRemoveExpectedC2DocumentBundlesFromCase() {
            Element<C2DocumentBundle> c2DocumentBundleElementOne = element(elementIdOne,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementTwo = element(elementIdTwo,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementThree = element(elementIdThree,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementFour = element(elementIdFour,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementFive = element(elementIdFive,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementSix = element(elementIdSix,
                createC2DocumentBundle());

            List<Element<C2DocumentBundle>> bundle = List.of(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo, c2DocumentBundleElementThree, c2DocumentBundleElementFour,
                c2DocumentBundleElementFive, c2DocumentBundleElementSix);

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, bundle);

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updatedCaseData.getC2DocumentBundle()).containsExactly(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo, c2DocumentBundleElementSix);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-1111";

            Element<C2DocumentBundle> c2DocumentBundleElementOne = element(elementIdOne,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementTwo = element(elementIdTwo,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementThree = element(elementIdThree,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementFour = element(elementIdFour,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementFive = element(elementIdFive,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementSix = element(elementIdSix,
                createC2DocumentBundle());

            List<Element<C2DocumentBundle>> bundle = List.of(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo, c2DocumentBundleElementThree, c2DocumentBundleElementFour,
                c2DocumentBundleElementFour, c2DocumentBundleElementFive, c2DocumentBundleElementSix);

            CaseDetails caseData = caseDetails(incorrectMigrationId, familyManNumber, bundle);
            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseData));

            assertThat(updatedCaseData.getC2DocumentBundle()).isEqualTo(bundle);
        }

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseDetails caseData = caseDetails(migrationId, "test", emptyList());

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Unexpected FMN test");
        }

        @Test
        void shouldThrowExceptionWhenC2documentBundleSizeIsSmallerThanExpected() {
            Element<C2DocumentBundle> c2DocumentBundleElementOne = element(elementIdOne,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementTwo = element(elementIdTwo,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementThree = element(elementIdThree,
                createC2DocumentBundle());

            Element<C2DocumentBundle> c2DocumentBundleElementFour = element(elementIdFour,
                createC2DocumentBundle());

            List<Element<C2DocumentBundle>> bundle = List.of(c2DocumentBundleElementOne,
                c2DocumentBundleElementTwo, c2DocumentBundleElementThree, c2DocumentBundleElementFour);

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, bundle);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Expected at least 5 C2 document bundles in the case but found 4");
        }

        @Test
        void shouldThrowExceptionWhenC2documentBundleIsMissing() {
            CaseDetails caseData = caseDetails(migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("No C2 document bundles in the case");
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<C2DocumentBundle>> bundles) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .c2DocumentBundle(bundles)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2913 {
        String familyManNumber = "SA20C50026";
        String migrationId = "FPLA-2913";

        @Test
        void shouldThrowExceptionWhenUnexpectedFamilyManNumber() {
            CaseDetails caseData = caseDetails(migrationId, "test", emptyList());

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Unexpected FMN test");
        }

        @Test
        void shouldThrowExceptionWhenNoSealedCmos() {
            CaseDetails caseData = caseDetails(migrationId, familyManNumber, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Expected at least 3 sealed cmos, but found 0");
        }

        @Test
        void shouldThrowExceptionWhenUnexpectedNumberOfSealedCmos() {
            Element<HearingOrder> hearingOrder1 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder2 = element(HearingOrder.builder().build());

            CaseDetails caseData = caseDetails(migrationId, familyManNumber, List.of(hearingOrder1, hearingOrder2));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Expected at least 3 sealed cmos, but found 2");
        }


        @Test
        void shouldThrowExceptionWhenUnexpectedNumberOfDocumentsToBeRemovedFound() {

            SupportingEvidenceBundle supportingDoc1 = SupportingEvidenceBundle.builder()
                .name("test 1")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDoc2 = SupportingEvidenceBundle.builder()
                .name("test 2")
                .document(testDocumentReference())
                .build();

            SupportingEvidenceBundle supportingDoc3 = SupportingEvidenceBundle.builder()
                .name("Final Placement Order")
                .document(testDocumentReference())
                .build();

            Element<HearingOrder> hearingOrder1 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder2 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder3 = element(HearingOrder.builder()
                .supportingDocs(wrapElements(supportingDoc1, supportingDoc2, supportingDoc3))
                .build());

            CaseDetails caseData = caseDetails(migrationId, familyManNumber,
                List.of(hearingOrder1, hearingOrder2, hearingOrder3));

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseData))
                .getRootCause()
                .hasMessage("Expected 2 documents to be removed, found 1");
        }


        @Test
        void shouldThrowsExceptionWhenunexpectedNumberOfHearingBundlesToBeRemoved() {
            Element<SupportingEvidenceBundle> supportingDoc1 = element(SupportingEvidenceBundle.builder()
                .name("Draft Placement Order")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc2 = element(SupportingEvidenceBundle.builder()
                .name("Final Placement Order")
                .document(testDocumentReference())
                .build());

            Element<HearingOrder> hearingOrder1 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder2 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder3 = element(HearingOrder.builder()
                .supportingDocs(List.of(supportingDoc1, supportingDoc2))
                .build());

            Element<HearingOrder> expectedHearingOrder3 = element(hearingOrder3.getId(),
                hearingOrder3.getValue().toBuilder()
                    .supportingDocs(List.of(supportingDoc1))
                    .build());

            Element<HearingFurtherEvidenceBundle> bundle = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Test 1")
                .supportingEvidenceBundle(List.of(supportingDoc1, supportingDoc2))
                .build());

            Element<HearingFurtherEvidenceBundle> bundle2 = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Test 1")
                .supportingEvidenceBundle(List.of(supportingDoc1))
                .build());

            CaseData caseData = CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .sealedCMOs(List.of(hearingOrder1, hearingOrder2, hearingOrder3))
                .hearingFurtherEvidenceDocuments(List.of(bundle, bundle2))
                .build();

            CaseDetails caseDetails = caseDetails(migrationId, caseData);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Expected 1 hearing bundle with documents to be removed, found 2");
        }

        @Test
        void shouldRemoveRequiredDocuments() {

            Element<SupportingEvidenceBundle> supportingDoc1 = element(SupportingEvidenceBundle.builder()
                .name("Placement application")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc2 = element(SupportingEvidenceBundle.builder()
                .name("Draft Placement Order")
                .document(testDocumentReference())
                .build());

            Element<SupportingEvidenceBundle> supportingDoc3 = element(SupportingEvidenceBundle.builder()
                .name("Final Placement Order")
                .document(testDocumentReference())
                .build());

            Element<HearingOrder> hearingOrder1 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder2 = element(HearingOrder.builder().build());
            Element<HearingOrder> hearingOrder3 = element(HearingOrder.builder()
                .supportingDocs(List.of(supportingDoc1, supportingDoc2, supportingDoc3))
                .build());

            Element<HearingOrder> expectedHearingOrder3 = element(hearingOrder3.getId(),
                hearingOrder3.getValue().toBuilder()
                    .supportingDocs(List.of(supportingDoc1))
                    .build());

            Element<HearingFurtherEvidenceBundle> bundle = element(HearingFurtherEvidenceBundle.builder()
                .hearingName("Test 1")
                .supportingEvidenceBundle(List.of(supportingDoc1, supportingDoc2, supportingDoc3))
                .build());

            Element<HearingFurtherEvidenceBundle> expectedBundle = element(bundle.getId(), bundle.getValue().toBuilder()
                .supportingEvidenceBundle(List.of(supportingDoc1))
                .build());

            CaseData caseData = CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .sealedCMOs(List.of(hearingOrder1, hearingOrder2, hearingOrder3))
                .hearingFurtherEvidenceDocuments(List.of(bundle))
                .build();

            CaseDetails caseDetails = caseDetails(migrationId, caseData);

            CaseData updatedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(updatedCaseData.getSealedCMOs().get(0)).isEqualTo(hearingOrder1);
            assertThat(updatedCaseData.getSealedCMOs().get(1)).isEqualTo(hearingOrder2);
            assertThat(updatedCaseData.getSealedCMOs().get(2)).isEqualTo(expectedHearingOrder3);

            assertThat(updatedCaseData.getHearingFurtherEvidenceDocuments())
                .containsExactly(expectedBundle);
        }

        private CaseDetails caseDetails(String migrationId,
                                        String familyManCaseNumber,
                                        List<Element<HearingOrder>> sealedCmos) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManCaseNumber)
                .sealedCMOs(sealedCmos)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }

        private CaseDetails caseDetails(String migrationId,
                                        CaseData caseData) {
            CaseDetails caseDetails = asCaseDetails(caseData);

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    @Nested
    class Fpla2871 {
        String familyManNumber = "WR20C50015";
        String migrationId = "FPLA-2871";

        @Test
        void shouldRemoveFirstC2() {

            Element<C2DocumentBundle> firstC2 = element(C2DocumentBundle.builder().description("test1").build());
            Element<C2DocumentBundle> secondC2 = element(C2DocumentBundle.builder().description("test2").build());
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .c2DocumentBundle(List.of(firstC2, secondC2))
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getC2DocumentBundle()).containsOnly(secondC2);
        }

        @Test
        void shouldDoNothingIfIncorrectFamilyNumber() {
            Element<C2DocumentBundle> firstC2 = element(C2DocumentBundle.builder().description("test1").build());
            Element<C2DocumentBundle> secondC2 = element(C2DocumentBundle.builder().description("test2").build());

            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber("1234")
                .c2DocumentBundle(List.of(firstC2, secondC2))
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getC2DocumentBundle()).containsExactly(firstC2, secondC2);
        }

        @Test
        void shouldThrowExceptionWhenNoC2s() {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .familyManCaseNumber(familyManNumber)
                .build());

            caseDetails.getData().put("migrationId", migrationId);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("No C2s on case");
        }
    }

    @Nested
    class Fpla2885 {
        String migrationId = "FPLA-2885";
        UUID idOne = randomUUID();
        UUID idTwo = randomUUID();
        UUID idThree = randomUUID();
        UUID idFour = randomUUID();

        @Test
        void shouldMigrateExpectedListElementCodes() {
            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT8"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT9"));

            Element<HearingBooking> hearingThree
                = element(idThree, hearingBookingWithCancellationReason("OT10"));

            Element<HearingBooking> hearingFour
                = element(idFour, hearingBookingWithCancellationReason("OT7"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(
                hearingOne, hearingTwo, hearingThree, hearingFour);

            CaseDetails caseDetails = caseDetails(migrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).containsExactly(
                element(idOne, hearingBookingWithCancellationReason("IN1")),
                element(idTwo, hearingBookingWithCancellationReason("OT8")),
                element(idThree, hearingBookingWithCancellationReason("OT9")),
                element(idFour, hearingBookingWithCancellationReason("OT7"))
            );
        }

        @Test
        void shouldNotUpdateListElementCodesWhenMigrationIsNotRequired() {
            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT1"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT3"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(hearingOne, hearingTwo);

            CaseDetails caseDetails = caseDetails(migrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearingBookings);
        }

        @Test
        void shouldNotChangeCaseIfNotExpectedMigrationId() {
            String incorrectMigrationId = "FPLA-2222";

            Element<HearingBooking> hearingOne
                = element(idOne, hearingBookingWithCancellationReason("OT8"));

            Element<HearingBooking> hearingTwo
                = element(idTwo, hearingBookingWithCancellationReason("OT9"));

            Element<HearingBooking> hearingThree
                = element(idThree, hearingBookingWithCancellationReason("OT10"));

            Element<HearingBooking> hearingFour
                = element(idFour, hearingBookingWithCancellationReason("OT7"));

            List<Element<HearingBooking>> cancelledHearingBookings = List.of(
                hearingOne, hearingTwo, hearingThree, hearingFour);

            CaseDetails caseDetails = caseDetails(incorrectMigrationId, cancelledHearingBookings);
            CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

            assertThat(extractedCaseData.getCancelledHearingDetails()).isEqualTo(cancelledHearingBookings);
        }

        @Test
        void shouldThrowAnErrorIfCaseDoesNotContainCancelledHearingBookings() {
            CaseDetails caseDetails = caseDetails(migrationId, null);

            assertThatThrownBy(() -> postAboutToSubmitEvent(caseDetails))
                .getRootCause()
                .hasMessage("Case does not contain cancelled hearing bookings");
        }

        private HearingBooking hearingBookingWithCancellationReason(String reasonCode) {
            return HearingBooking.builder().cancellationReason(reasonCode).build();
        }

        private CaseDetails caseDetails(String migrationId, List<Element<HearingBooking>> cancelledHearings) {
            CaseDetails caseDetails = asCaseDetails(CaseData.builder()
                .cancelledHearingDetails(cancelledHearings)
                .build());

            caseDetails.getData().put("migrationId", migrationId);
            return caseDetails;
        }
    }

    private C2DocumentBundle createC2DocumentBundle() {
        return C2DocumentBundle.builder().build();
    }
}
