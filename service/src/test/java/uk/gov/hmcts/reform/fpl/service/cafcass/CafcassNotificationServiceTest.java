package uk.gov.hmcts.reform.fpl.service.cafcass;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.cafcass.CafcassEmailConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.email.EmailData;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.email.EmailService;

import static java.util.Set.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.model.email.EmailAttachment.document;
import static uk.gov.hmcts.reform.fpl.service.cafcass.CafcassRequestEmailContentProvider.ORDER;

@ExtendWith(MockitoExtension.class)
class CafcassNotificationServiceTest {
    private static final String SENDER_EMAIL = "senderEmail";
    private static final String RECIPIENT_EMAIL = "recipientEmail";
    private static final String DOCUMENT_BINARY_URL = "originalDocumentBinaryUrl";
    private static final byte[] DOCUMENT_CONTENT = "OriginalDocumentContent".getBytes();
    private static final String DOCUMENT_FILENAME = "fileToSend.pdf";
    private static final String FAMILY_MAN = "FM1234";
    private static final String ORDER_TITLE = "dummy";

    @Mock
    private EmailService emailService;
    @Mock
    private DocumentDownloadService documentDownloadService;
    @Mock
    private CafcassEmailConfiguration configuration;

    @InjectMocks
    private CafcassNotificationService underTest;

    @Captor
    private ArgumentCaptor<EmailData> emailData;

    @Test
    void testSendRequest() {
        when(configuration.getRecipientForOrder()).thenReturn(RECIPIENT_EMAIL);
        when(configuration.getSender()).thenReturn(SENDER_EMAIL);
        when(documentDownloadService.downloadDocument(DOCUMENT_BINARY_URL)).thenReturn(
            DOCUMENT_CONTENT);

        CaseData caseData = CaseData.builder()
            .familyManCaseNumber(FAMILY_MAN)
            .build();

        underTest.sendEmail(caseData,
            of(DocumentReference.builder().binaryUrl(DOCUMENT_BINARY_URL)
                .filename(DOCUMENT_FILENAME)
                .build()),
            ORDER,
            ORDER_TITLE
        );

        verify(documentDownloadService).downloadDocument(DOCUMENT_BINARY_URL);

        verify(emailService).sendEmail(eq(SENDER_EMAIL), emailData.capture());
        EmailData data = emailData.getValue();
        assertThat(data.getRecipient()).isEqualTo(RECIPIENT_EMAIL);
        assertThat(data.getSubject()).isEqualTo(String.join("",
            "Court Ref. ",
            caseData.getFamilyManCaseNumber(),
            ".- ",
            ORDER.getType()));
        assertThat(data.getAttachments()).containsExactly(
            document("application/pdf",  DOCUMENT_CONTENT, DOCUMENT_FILENAME)
        );
        assertThat(data.getMessage()).isEqualTo(
            String.join(" ",
                "A new order for this case was uploaded to the Public Law Portal entitled",
                ORDER_TITLE)
        );
    }
}
