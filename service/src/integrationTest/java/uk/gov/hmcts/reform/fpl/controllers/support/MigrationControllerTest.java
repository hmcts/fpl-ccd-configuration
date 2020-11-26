package uk.gov.hmcts.reform.fpl.controllers.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.AbstractControllerTest;
import uk.gov.hmcts.reform.fpl.controllers.MigrateCaseController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ActiveProfiles("integration-test")
@WebMvcTest(MigrateCaseController.class)
@OverrideAutoConfiguration(enabled = true)
public class MigrationControllerTest extends AbstractControllerTest {

    private final Element<C2DocumentBundle> firstC2 = element(buildC2("first author"));
    private final Element<C2DocumentBundle> secondC2 = element(buildC2("second author"));

    private String familyManCaseNumber;
    private String migrationId;

    protected MigrationControllerTest() {
        super("migrate-case");
    }

    @BeforeEach
    void setCaseIdentifiers() {
        familyManCaseNumber = "CF20C50014";
        migrationId = "FPLA-2450";
    }

    @Test
    void removeLastC2FromCorrectCase() {
        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, firstC2, secondC2);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getC2DocumentBundle()).hasSize(1)
            .containsOnly(firstC2);
    }

    @Test
    void shouldNotRemoveC2IfNotExpectedFamilyManNumber() {
        familyManCaseNumber = "something different";

        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, firstC2, secondC2);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getC2DocumentBundle()).hasSize(2)
            .containsOnly(firstC2, secondC2);
    }

    @Test
    void shouldNotRemoveC2IfNotExpectedMigrationId() {
        migrationId = "something different";

        CaseDetails caseDetails = caseDetails(familyManCaseNumber, migrationId, firstC2, secondC2);

        CaseData extractedCaseData = extractCaseData(postAboutToSubmitEvent(caseDetails));

        assertThat(extractedCaseData.getC2DocumentBundle()).hasSize(2)
            .containsOnly(firstC2, secondC2);
    }

    @SafeVarargs
    private CaseDetails caseDetails(String familyManCaseNumber, String migrationId, Element<C2DocumentBundle>... c2s) {
        CaseDetails caseDetails = asCaseDetails(CaseData.builder()
            .familyManCaseNumber(familyManCaseNumber)
            .c2DocumentBundle(List.of(c2s))
            .build());
        caseDetails.getData().put("migrationId", migrationId);
        return caseDetails;
    }

    private C2DocumentBundle buildC2(String author) {
        return C2DocumentBundle.builder()
            .author(author)
            .build();
    }
}
