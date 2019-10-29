package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C21OrderBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class CreateC21OrderServiceTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String COURT_NAME = "Example Court";
    private static final String COURT_EMAIL = "example@court.com";
    private static final String CONFIG = String.format("%s=>%s:%s", LOCAL_AUTHORITY_CODE, COURT_NAME, COURT_EMAIL);

    private DateFormatterService dateFormatterService = new DateFormatterService();
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration = new HmctsCourtLookupConfiguration(CONFIG);
    private CreateC21OrderService createC21OrderService = new CreateC21OrderService(dateFormatterService,
        hmctsCourtLookupConfiguration);

    @Test
    void shouldAppendNewC21OrderBundleWhenC21OrderBundlePreExists() {
        CaseData caseData = CaseData.builder()
            .c21OrderBundle(ImmutableList.of(
                Element.<C21OrderBundle>builder()
                    .id(UUID.randomUUID())
                    .value(C21OrderBundle.builder()
                        .c21OrderDocument(DocumentReference.builder()
                            .filename("C21_1.pdf")
                            .build())
                        .build())
                    .build()
            ))
            .temporaryC2Document(C2DocumentBundle.builder()
                .document(DocumentReference.builder()
                    .filename("C21_2.pdf")
                    .build())
                .build())
            .build();

        List<Element<C21OrderBundle>> updatedC2OrderBundle = createC21OrderService.appendToC21OrderBundle(caseData);
        assertThat(updatedC2OrderBundle).size().isEqualTo(2);
    }

    @Test
    void shouldFormatC21TemplateDataToEmptyStringWhenCaseDataIsEmpty() {

    }

    @Test
    void shouldFormatC21TemplateDataCorrectlyWhenCaseDataIsPopulated() {

    }
}
