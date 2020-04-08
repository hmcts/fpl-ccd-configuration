package uk.gov.hmcts.reform.fpl.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JacksonAutoConfiguration.class, DocmosisCoverDocumentsService.class })
class DocmosisCoverDocumentsServiceTest {

    public static final String NULL_FAMILY_MAN_NUMBER = null;

    private DocmosisCoverDocumentsService documentsService;

    private DocmosisDocument docmosisDocument;

    private Representative testRepresentative;

    private static final String FAMILY_MAN_NUMBER = "12345";

    private static final Long CCD_CASE_NUMBER = 1234123412341234L;

    @MockBean
    DocmosisDocumentGeneratorService documentGeneratorService;

    @Autowired
    DocmosisCoverDocumentsServiceTest(DocmosisCoverDocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @BeforeEach
    void setup() {
        byte[] pdf = {1, 2, 3, 4, 5};
        testRepresentative = buildRepresentative();
        docmosisDocument = new DocmosisDocument("example.pdf", pdf);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
    }

    @Test
    void shouldGenerateDocmosisDocumentWhenAllDataProvided() {
        DocmosisDocument coverDocument = documentsService.createCoverDocuments(FAMILY_MAN_NUMBER,
            CCD_CASE_NUMBER, testRepresentative);

        assertThat(coverDocument).isEqualTo(docmosisDocument);
    }


    @Test
    void shouldBuildCoverDocumentsWhenAllDataProvided() {
        DocmosisCoverDocument coverDocumentData = documentsService.buildCoverDocumentsData(FAMILY_MAN_NUMBER,
            CCD_CASE_NUMBER, testRepresentative);

        assertThat(coverDocumentData.getFamilyManCaseNumber()).isEqualTo(FAMILY_MAN_NUMBER);
        assertThat(coverDocumentData.getCcdCaseNumber()).isEqualTo("1234-1234-1234-1234");
        assertThat(coverDocumentData.getRepresentativeName()).isEqualTo("Mark Jones");
        assertThat(coverDocumentData.getRepresentativeAddress()).isEqualTo("1 Petty France\nSt James's Park\nLondon");
    }

    @Test
    void shouldDefaultNullFamilyManCaseNumberToEmptyString() {
        DocmosisCoverDocument coverDocumentData = documentsService.buildCoverDocumentsData(
            NULL_FAMILY_MAN_NUMBER, CCD_CASE_NUMBER,  buildRepresentative());

        assertThat(coverDocumentData.getFamilyManCaseNumber()).isEqualTo("");
    }

    private Representative buildRepresentative() {
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
