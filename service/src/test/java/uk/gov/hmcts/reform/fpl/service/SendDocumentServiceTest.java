package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Recipient;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith({MockitoExtension.class, TestLogsExtension.class})
class SendDocumentServiceTest {

    @Mock
    private SendLetterService sendLetters;

    @Mock
    private CoreCaseDataService caseService;

    @Mock
    private SentDocumentHistoryService sentDocumentsService;

    @Mock
    private OthersService othersService;

    @TestLogs
    private TestLogger logs = new TestLogger(SendDocumentService.class);

    @InjectMocks
    private SendDocumentService underTest;

    @Nested
    class DocumentSender {

        private final CaseData caseData = CaseData.builder()
            .id(100L)
            .familyManCaseNumber("FMN123")
            .build();

        @Test
        void shouldDoNothingWhenNoRecipients() {
            final DocumentReference document = testDocumentReference();

            underTest.sendDocuments(caseData, List.of(document), null);

            verifyNoInteractions(sendLetters, caseService, sentDocumentsService);
        }

        @Test
        void shouldDoNothingWhenEmptyRecipients() {
            final DocumentReference document = testDocumentReference();

            underTest.sendDocuments(caseData, List.of(document), emptyList());

            verifyNoInteractions(sendLetters, caseService, sentDocumentsService);
        }

        @Test
        void shouldDoNothingWhenNoDocuments() {
            final Recipient recipient = recipient("Test 1", testAddress());

            underTest.sendDocuments(caseData, null, List.of(recipient));

            verifyNoInteractions(sendLetters, caseService, sentDocumentsService);
        }

        @Test
        void shouldDoNothingWhenEmptyDocuments() {
            final Recipient recipient = recipient("Test 1", testAddress());

            underTest.sendDocuments(caseData, emptyList(), List.of(recipient));

            verifyNoInteractions(sendLetters, caseService, sentDocumentsService);
        }

        @Test
        void shouldLogErrorWhenRecipientNameIsMissing() {
            final DocumentReference document = testDocumentReference();
            final Recipient recipient = recipient(null, testAddress());

            underTest.sendDocuments(caseData, List.of(document), List.of(recipient));

            verifyNoInteractions(sendLetters, caseService, sentDocumentsService);

            assertThat(logs.getErrors())
                .containsExactly("Case 100 has 1 recipients with incomplete postal information");
        }

        @Test
        void shouldExcludeRecipientsWithIncompleteAddress() {
            final DocumentReference document = testDocumentReference();

            final Recipient recipient1 = recipient(null, testAddress());
            final Recipient recipient2 = recipient("Test 1", null);
            final Recipient recipient3 = recipient("Test 2", testAddress());

            final SentDocument sentDocument = SentDocument.builder()
                .document(testDocumentReference())
                .coversheet(testDocumentReference())
                .build();

            final List<SentDocument> sentDocuments = List.of(sentDocument);

            final List<Element<SentDocuments>> sentDocumentsHistory = wrapElements(SentDocuments.builder()
                .documentsSentToParty(wrapElements(sentDocument))
                .build());

            when(sendLetters.send(any(), any(), any(), any()))
                .thenReturn(sentDocuments);

            when(sentDocumentsService.addToHistory(caseData.getDocumentsSentToParties(), sentDocuments))
                .thenReturn(sentDocumentsHistory);

            underTest.sendDocuments(caseData, List.of(document), List.of(recipient1, recipient2, recipient3));

            verify(sendLetters)
                .send(document, List.of(recipient3), caseData.getId(), caseData.getFamilyManCaseNumber());

            verify(caseService)
                .updateCase(caseData.getId(), Map.of("documentsSentToParties", sentDocumentsHistory));

            assertThat(logs.getErrors())
                .containsExactly("Case 100 has 2 recipients with incomplete postal information");
        }

        @Test
        void shouldSendMultipleDocumentsForMultipleRecipients() {
            final DocumentReference document1 = testDocumentReference();
            final DocumentReference document2 = testDocumentReference();

            final Recipient recipient1 = recipient("Test 1", testAddress());
            final Recipient recipient2 = recipient("Test 2", testAddress());

            final SentDocument sentDocument1ForRecipient1 = sentDocument();
            final SentDocument sentDocument1ForRecipient2 = sentDocument();
            final SentDocument sentDocument2ForRecipient1 = sentDocument();
            final SentDocument sentDocument2ForRecipient2 = sentDocument();

            final List<Element<SentDocuments>> sentDocumentsHistory = wrapElements(
                SentDocuments.builder()
                    .documentsSentToParty(wrapElements(sentDocument1ForRecipient1, sentDocument1ForRecipient2))
                    .build(),
                SentDocuments.builder()
                    .documentsSentToParty(wrapElements(sentDocument2ForRecipient1, sentDocument2ForRecipient2))
                    .build());

            when(sendLetters.send(eq(document1), any(), any(), any()))
                .thenReturn(List.of(sentDocument1ForRecipient1, sentDocument1ForRecipient2));

            when(sendLetters.send(eq(document2), any(), any(), any()))
                .thenReturn(List.of(sentDocument2ForRecipient1, sentDocument2ForRecipient2));

            when(sentDocumentsService.addToHistory(caseData.getDocumentsSentToParties(),
                List.of(
                    sentDocument1ForRecipient1,
                    sentDocument1ForRecipient2,
                    sentDocument2ForRecipient1,
                    sentDocument2ForRecipient2)))
                .thenReturn(sentDocumentsHistory);

            underTest.sendDocuments(caseData, List.of(document1, document2), List.of(recipient1, recipient2));

            verify(sendLetters)
                .send(document1, List.of(recipient1, recipient2), caseData.getId(), caseData.getFamilyManCaseNumber());

            verify(sendLetters)
                .send(document2, List.of(recipient1, recipient2), caseData.getId(), caseData.getFamilyManCaseNumber());

            verify(caseService)
                .updateCase(caseData.getId(), Map.of("documentsSentToParties", sentDocumentsHistory));

            assertThat(logs.getErrors()).isEmpty();
        }
    }

    @Nested
    class Recipients {

        @Test
        void shouldReturnEmptyListWhenNoRepresentativesServedByPostNorNotRepresentedRespondents() {
            final CaseData caseData = CaseData.builder().build();

            final List<Recipient> actualRecipients = underTest.getStandardRecipients(caseData);

            assertThat(actualRecipients).isEmpty();
        }

        @Test
        void shouldReturnRepresentativesServedByPostAndNotRepresentedRespondents() {

            final Element<Representative> representativeServedByPost = element(Representative.builder()
                .fullName("Representative 1")
                .servingPreferences(POST)
                .build());

            final Element<Representative> representativeServedByEmail = element(Representative.builder()
                .fullName("Representative 2")
                .servingPreferences(EMAIL)
                .build());

            final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
                .fullName("Representative 3")
                .servingPreferences(DIGITAL_SERVICE)
                .build());

            final Respondent representedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .representedBy(wrapElements(representativeServedByPost.getId()))
                .build();

            final Respondent notRepresentedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Not Represented")
                    .lastName("Respondent")
                    .build())
                .build();

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(
                    representativeServedByPost,
                    representativeServedByEmail,
                    representativeServedByDigitalService))
                .respondents1(wrapElements(
                    representedRespondent,
                    notRepresentedRespondent))
                .build();

            final List<Recipient> actualRecipients = underTest.getStandardRecipients(caseData);

            assertThat(actualRecipients)
                .containsExactlyInAnyOrder(representativeServedByPost.getValue(), notRepresentedRespondent.getParty());

        }

        @Test
        void shouldReturnNotRepresentedRespondentWhenLegalRepresentationIsNoAndNotRepresentedInMangeRep() {
            final Respondent notRepresentedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Not Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(NO.getValue())
                .build();

            final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
                .fullName("Representative 1")
                .servingPreferences(DIGITAL_SERVICE)
                .build());

            final Respondent representedRespondentOne = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(YES.getValue())
                .representedBy(wrapElements(representativeServedByDigitalService.getId()))
                .build();

            final Respondent representedRespondentTwo = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(NO.getValue())
                .representedBy(wrapElements(representativeServedByDigitalService.getId()))
                .build();

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(representativeServedByDigitalService))
                .respondents1(wrapElements(
                    representedRespondentOne,
                    representedRespondentTwo,
                    notRepresentedRespondent))
                .build();

            final List<Recipient> actualRecipients = underTest.getStandardRecipients(caseData);

            assertThat(actualRecipients).size().isEqualTo(1);
            assertThat(actualRecipients.get(0)).isEqualTo(notRepresentedRespondent.getParty());
        }

        @Test
        void shouldReturnNotRepresentedRespondentWhenLegalRepresentationIsNullAndNotRepresentedInMangeRep() {
            final Respondent notRepresentedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Not Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(null)
                .build();

            final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
                .fullName("Representative 1")
                .servingPreferences(DIGITAL_SERVICE)
                .build());

            final Respondent representedRespondentOne = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(null)
                .representedBy(wrapElements(representativeServedByDigitalService.getId()))
                .build();

            final Respondent representedRespondentTwo = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .legalRepresentation(YES.getValue())
                .build();

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(representativeServedByDigitalService))
                .respondents1(wrapElements(
                    representedRespondentOne,
                    representedRespondentTwo,
                    notRepresentedRespondent))
                .build();

            final List<Recipient> actualRecipients = underTest.getStandardRecipients(caseData);

            assertThat(actualRecipients).size().isEqualTo(1);
            assertThat(actualRecipients.get(0)).isEqualTo(notRepresentedRespondent.getParty());
        }


        @Test
        void shouldReturnEmptyListWhenNoOtherRecipients() {
            final CaseData caseData = CaseData.builder().others(Others.builder().build()).build();

            final List<Recipient> otherRecipients = underTest.getSelectedOtherRecipients(caseData, emptyList());

            assertThat(otherRecipients).isEmpty();
        }

        @Test
        void shouldReturnOtherRecipientsServedByPost() {
            UUID representativeID = UUID.randomUUID();
            final Element<Representative> representativeServedByPost = element(representativeID,
                Representative.builder()
                .fullName("Representative 1")
                .servingPreferences(POST)
                .build());

            final Element<Representative> representativeServedByEmail = element(Representative.builder()
                .fullName("Representative 3")
                .servingPreferences(EMAIL)
                .build());

            final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
                .fullName("Representative 4")
                .servingPreferences(DIGITAL_SERVICE)
                .build());

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(
                    representativeServedByPost,
                    representativeServedByEmail,
                    representativeServedByDigitalService))
                .build();

            Other firstOther = Other.builder()
                .name("Other 1")
                .build();

            firstOther.addRepresentative(representativeID);

            List<Element<Other>> othersSelected = List.of(element(firstOther));

            given(othersService.isRepresented(firstOther)).willReturn(true);

            final List<Recipient> otherRecipients = underTest.getSelectedOtherRecipients(caseData, othersSelected);

            assertThat(otherRecipients)
                .containsExactlyInAnyOrder(representativeServedByPost.getValue());
        }

        @Test
        void shouldReturnNotRepresentedOtherRecipients() {
            final CaseData caseData = CaseData.builder().build();

            Other firstOther = Other.builder()
                .name("Other 1")
                .address(testAddress())
                .build();

            List<Element<Other>> othersSelected = List.of(element(firstOther));

            given(othersService.hasAddressAdded(firstOther)).willReturn(true);

            final List<Recipient> otherRecipients = underTest.getSelectedOtherRecipients(caseData, othersSelected);

            assertThat(otherRecipients.get(0)).isEqualTo(Representative.builder()
                .fullName(firstOther.getName())
                .address(firstOther.getAddress())
                .build());
        }

        @Test
        void shouldReturnRecipientsServedByPostExcludingOthers() {
            UUID representativeID = UUID.randomUUID();
            final Element<Representative> representativeServedByPost = element(representativeID,
                Representative.builder()
                .fullName("Representative 1")
                .role(RepresentativeRole.REPRESENTING_PERSON_1)
                .servingPreferences(POST)
                .build());

            final Element<Representative> representativeServedByPost2 = element(representativeID,
                Representative.builder()
                .fullName("Representative 2")
                .role(RepresentativeRole.BARRISTER)
                .servingPreferences(POST)
                .build());

            final Element<Representative> representativeServedByEmail = element(Representative.builder()
                .fullName("Representative 3")
                .servingPreferences(EMAIL)
                .role(RepresentativeRole.BARRISTER)
                .build());

            final Element<Representative> representativeServedByDigitalService = element(Representative.builder()
                .fullName("Representative 4")
                .servingPreferences(DIGITAL_SERVICE)
                .role(RepresentativeRole.BARRISTER)
                .build());

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(
                    representativeServedByPost,
                    representativeServedByPost2,
                    representativeServedByEmail,
                    representativeServedByDigitalService))
                .build();

            final List<Recipient> recipients = underTest.getRecipientsExcludingOthers(caseData);

            assertThat(recipients)
                .containsExactlyInAnyOrder(representativeServedByPost2.getValue());

        }

        @Test
        void shouldReturnNotRepresentedRespondents() {
            final Element<Representative> representativeServedByPost = element(Representative.builder()
                .fullName("Representative 1")
                .role(RepresentativeRole.REPRESENTING_PERSON_1)
                .servingPreferences(POST)
                .build());

            final Respondent representedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Represented")
                    .lastName("Respondent")
                    .build())
                .representedBy(wrapElements(representativeServedByPost.getId()))
                .build();

            final Respondent notRepresentedRespondent = Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Not Represented")
                    .lastName("Respondent")
                    .build())
                .build();

            final CaseData caseData = CaseData.builder()
                .representatives(List.of(representativeServedByPost))
                .respondents1(wrapElements(
                    representedRespondent,
                    notRepresentedRespondent))
                .build();

            final List<Recipient> actualRecipients = underTest.getRecipientsExcludingOthers(caseData);

            assertThat(actualRecipients).size().isEqualTo(1);
            assertThat(actualRecipients.get(0)).isEqualTo(notRepresentedRespondent.getParty());
        }
    }

    private static SentDocument sentDocument() {
        return SentDocument.builder()
            .document(testDocumentReference())
            .coversheet(testDocumentReference())
            .build();
    }

    private static Recipient recipient(String name, Address address) {
        return Representative.builder()
            .fullName(name)
            .address(address)
            .build();
    }
}
