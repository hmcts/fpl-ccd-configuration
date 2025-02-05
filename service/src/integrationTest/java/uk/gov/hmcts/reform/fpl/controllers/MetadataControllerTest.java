package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentLocalAuthority;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(MetadataController.class)
@OverrideAutoConfiguration(enabled = true)
public class MetadataControllerTest extends AbstractCallbackTest {

    protected MetadataControllerTest() {
        super("getCase");
    }

    @Test
    void shouldReturn3rdPartyFlagIfYes() {
        CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(RespondentLocalAuthority.builder()
                .name("Swansea County Council")
                .build())
            .build();
        CallbackRequest cb = toCallBackRequest(caseData, caseData);

        Map<String, Object> response = postMetadataCallback("/callback/getCase/metadata", cb);

        List<MetadataController.CaseViewField> metadataFields = mapper.convertValue(
            response.get("metadataFields"), new TypeReference<>() {});

        assertThat(metadataFields)
            .contains(MetadataController.CaseViewField.builder()
                .id("[INJECTED_DATA.HAS_3RD_PARTY]")
                .label("Has a respondent local authority been added to the case?")
                .value("Yes")
                .fieldType(MetadataController.FieldType.TEXT)
                .build());
    }

    @Test
    void shouldReturn3rdPartyFlagIfNo() {
        CaseData caseData = CaseData.builder()
            .respondentLocalAuthority(null)
            .build();
        CallbackRequest cb = toCallBackRequest(caseData, caseData);

        Map<String, Object> response = postMetadataCallback("/callback/getCase/metadata", cb);

        List<MetadataController.CaseViewField> metadataFields = mapper.convertValue(
            response.get("metadataFields"), new TypeReference<>() {});

        assertThat(metadataFields)
            .contains(MetadataController.CaseViewField.builder()
                .id("[INJECTED_DATA.HAS_3RD_PARTY]")
                .label("Has a respondent local authority been added to the case?")
                .value("No")
                .fieldType(MetadataController.FieldType.TEXT)
                .build());
    }

}
