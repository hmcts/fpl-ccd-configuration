package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisDocument;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DocmosisCoverDocumentsService.class})
class DocmosisCoverDocumentsServiceTest {

    private final DocmosisCoverDocumentsService documentsService;

    @MockBean
    DocmosisDocumentGeneratorService documentGeneratorService;

    @Autowired
    DocmosisCoverDocumentsServiceTest(DocmosisCoverDocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    @BeforeEach
    void setup() {
        byte[] pdf = {1, 2, 3, 4, 5};
        DocmosisDocument docmosisDocument = new DocmosisDocument("example.pdf", pdf);

        given(documentGeneratorService.generateDocmosisDocument(any(), any())).willReturn(docmosisDocument);
    }

    @Test
    void shouldBuildGeneralLetterWhenFamilyManCaseNumberAndRepresentativeDataProvided() {
        String familyManCaseNumber = "12345";
        Representative testRepresentative = buildRepresentative();
        Map<String, Object> generalLetterData = documentsService.buildGeneralLetterData(familyManCaseNumber,
            testRepresentative);

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("familyManCaseNumber", "12345")
            .put("representativeName", "Mark Jones")
            .put("representativeAddress", "1 Petty France\nSt James's Park\nLondon")
            .build();

        assertThat(generalLetterData).isEqualTo(expectedMap);
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
