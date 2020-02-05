package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.DocumentSentToParty;
import uk.gov.hmcts.reform.fpl.model.DocumentToBeSent;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocument;

@ExtendWith(SpringExtension.class)
public class DocumentSenderServiceTest {

    @Mock
    private Clock clock;

    @Mock
    private DateFormatterService dateFormatterService;

    @InjectMocks
    private DocumentSenderService documentSenderService;

    private static final LocalDateTime SENT_TIME = LocalDateTime.now();
    private static final String FORMATTED_SENT_TIME = SENT_TIME.toString();

    @BeforeEach
    void init() {
        when(clock.getZone()).thenReturn(UTC.normalized());
        when(clock.instant()).thenReturn(SENT_TIME.toInstant(UTC));
        when(dateFormatterService.formatLocalDateTimeBaseUsingFormat(SENT_TIME, "h:mma, d MMMM yyyy"))
            .thenReturn(FORMATTED_SENT_TIME);
    }

    @Test
    void shouldSendDocumentToAllParties() {

        DocumentReference documentToBeSent = testDocument();

//        DocumentToBeSent documentToBeSent = DocumentToBeSent.builder()
//            .document(testDocument())
//            .coversheet(testDocument())
//            .build();

        Representative representative1 = testRepresentative();
        Representative representative2 = testRepresentative();

        List<DocumentSentToParty> documentsSentToParties = documentSenderService.send(documentToBeSent,
            List.of(representative1, representative2));

        assertThat(documentsSentToParties).containsExactly(
            documentSentToRepresentative(documentToBeSent, representative1),
            documentSentToRepresentative(documentToBeSent, representative2)
        );
    }

    @Test
    void shouldNotSendDocumentIfNoPartiesToBePosted() {
        DocumentToBeSent documentToBeSent = DocumentToBeSent.builder()
            .document(testDocument())
            .coversheet(testDocument())
            .build();

        List<DocumentSentToParty> documentsSentToParties = documentSenderService.send(documentToBeSent, emptyList());

        assertThat(documentsSentToParties).isEmpty();
    }

    private static DocumentSentToParty documentSentToRepresentative(DocumentToBeSent documentToBeSent,
                                                                    Representative representative) {
        return DocumentSentToParty.builder()
            .partyName(representative.getFullName())
            .document(documentToBeSent.getDocument())
            .generalLetterAndCoversheet(documentToBeSent.getCoversheet())
            .sentAt(FORMATTED_SENT_TIME)
            .build();
    }

    private static Representative testRepresentative() {
        final String representativeName = String.format("John Smith %s", nextInt());
        return Representative.builder()
            .fullName(representativeName)
            .servingPreferences(POST)
            .role(RepresentativeRole.REPRESENTING_RESPONDENT_1)
            .email(String.format("%s@hmcts.net", deleteWhitespace(representativeName)))
            .build();
    }
}
