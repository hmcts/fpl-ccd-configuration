package uk.gov.hmcts.reform.fpl.service.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCoverDocument;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class, DocmosisCoverDocumentsService.class })
class DocmosisCoverDocumentsServiceTest {
    private static final String NULL_FAMILY_MAN_NUMBER = null;
    private static final String FAMILY_MAN_NUMBER = "12345";
    private static final Long CCD_CASE_NUMBER = 1234123412341234L;
    private static final String FORMATTED_CASE_NUMBER = "1234-1234-1234-1234";

    private byte[] pdf = { 1, 2, 3, 4, 5 };
    private DocmosisDocument docmosisDocument = new DocmosisDocument("example.pdf", pdf);
    private Representative testRepresentative = buildRepresentative();

    @MockBean
    private DocmosisDocumentGeneratorService documentGeneratorService;

    @MockBean
    private CaseDetailsHelper caseDetailsHelper;

    @Autowired
    private DocmosisCoverDocumentsService underTest;

    @BeforeEach
    void setup() {
        given(documentGeneratorService.generateDocmosisDocument(any(DocmosisData.class), any()))
            .willReturn(docmosisDocument);

        given(caseDetailsHelper.formatCCDCaseNumber(CCD_CASE_NUMBER)).willReturn(FORMATTED_CASE_NUMBER);
    }

    @Test
    void shouldGenerateExpectedDocumentWhenAllDataProvided() {
        DocmosisDocument pdfDocument = underTest.createCoverDocuments(FAMILY_MAN_NUMBER,
            CCD_CASE_NUMBER, testRepresentative);

        assertThat(pdfDocument).isEqualTo(docmosisDocument);
    }

    @Test
    void shouldGenerateExpectedDataWhenAllDataProvided() {
        DocmosisCoverDocument coverDocumentData = underTest.buildCoverDocumentsData(FAMILY_MAN_NUMBER,
            CCD_CASE_NUMBER, testRepresentative);

        assertThat(coverDocumentData.getFamilyManCaseNumber()).isEqualTo(FAMILY_MAN_NUMBER);
        assertThat(coverDocumentData.getCcdCaseNumber()).isEqualTo(FORMATTED_CASE_NUMBER);
        assertThat(coverDocumentData.getRepresentativeName()).isEqualTo("Mark Jones");
        assertThat(coverDocumentData.getRepresentativeAddress()).isEqualTo("1 Petty France\nSt James's Park\nLondon");
        assertThat(coverDocumentData.getHmctsLogoLarge()).isEqualTo("[userImage:hmcts-logo-large.png]");
        assertThat(coverDocumentData.getHmctsLogoSmall()).isEqualTo("[userImage:hmcts-logo-small.png]");
    }

    @Test
    void shouldDefaultNullFamilyManCaseNumberToEmptyString() {
        DocmosisCoverDocument coverDocumentData = underTest.buildCoverDocumentsData(
            NULL_FAMILY_MAN_NUMBER, CCD_CASE_NUMBER,  buildRepresentative());

        assertThat(coverDocumentData.getFamilyManCaseNumber()).isEmpty();
    }

    private static Representative buildRepresentative() {
        return Representative.builder()
            .fullName("Mark Jones")
            .address(Address.builder()
                .addressLine1("1 Petty France")
                .addressLine2("St James's Park")
                .postTown("London")
                .build())
            .build();
    }
}
