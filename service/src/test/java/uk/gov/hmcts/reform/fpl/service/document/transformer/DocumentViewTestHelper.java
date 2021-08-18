package uk.gov.hmcts.reform.fpl.service.document.transformer;

import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

public class DocumentViewTestHelper {

    private DocumentViewTestHelper() {
    }

    public static final Element<SupportingEvidenceBundle> ADMIN_CONFIDENTIAL_DOCUMENT = buildFurtherEvidenceBundle(
        "Admin uploaded evidence1", "HMCTS", true, EXPERT_REPORTS, now());

    public static final Element<SupportingEvidenceBundle> ADMIN_NON_CONFIDENTIAL_DOCUMENT = buildFurtherEvidenceBundle(
        "Admin uploaded evidence2", "HMCTS", false, EXPERT_REPORTS, now().minusMinutes(1));

    public static final Element<SupportingEvidenceBundle> LA_CONFIDENTIAL_DOCUMENT = buildFurtherEvidenceBundle(
        "LA uploaded evidence1", "Kurt LA", true, GUARDIAN_REPORTS, now().minusMinutes(2));

    public static final Element<SupportingEvidenceBundle> LA_NON_CONFIDENTIAL_DOCUMENT = buildFurtherEvidenceBundle(
        "LA uploaded evidence2", "Kurt LA", false, GUARDIAN_REPORTS, now().minusMinutes(3));

    public static final Element<SupportingEvidenceBundle> SOLICITOR_NON_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle(
        "Solicitor uploaded evidence1", "External solicitor", false, APPLICANT_STATEMENT, now().minusMinutes(5));

    public static final Element<SupportingEvidenceBundle> ADMIN_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Application statement document1", "HMCTS", false, APPLICANT_STATEMENT, now().minusMinutes(1));

    public static final Element<SupportingEvidenceBundle> LA_NON_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Application statement document2", "Kurt LA", false, APPLICANT_STATEMENT, now().minusHours(2));

    public static final Element<SupportingEvidenceBundle> ADMIN_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Application statement document3", "HMCTS", true, APPLICANT_STATEMENT, now().minusMinutes(3));

    public static final Element<SupportingEvidenceBundle> LA_CONFIDENTIAL_APPLICANT_STATEMENT_DOCUMENT =
        buildFurtherEvidenceBundle(
            "Application statement document4", "Kurt LA", true, APPLICANT_STATEMENT, now().minusHours(4));

    public static final Element<Respondent> RESPONDENT1 = buildRespondent("Dave", "Miller");
    public static final Element<Respondent> RESPONDENT2 = buildRespondent("Will", "Smith");

    public static final List<Element<SupportingEvidenceBundle>> SUPPORTING_EVIDENCE_DOCUMENTS = List.of(
        ADMIN_CONFIDENTIAL_DOCUMENT,
        ADMIN_NON_CONFIDENTIAL_DOCUMENT,
        LA_CONFIDENTIAL_DOCUMENT,
        LA_NON_CONFIDENTIAL_DOCUMENT);

    private static Element<Respondent> buildRespondent(String firstName, String lastName) {
        return element(Respondent.builder()
            .party(RespondentParty.builder().firstName(firstName).lastName(lastName).build())
            .build());
    }

    public static Element<SupportingEvidenceBundle> buildFurtherEvidenceBundle(
        String name, String uploadedBy, boolean isConfidential, FurtherEvidenceType type, LocalDateTime uploadedAt) {
        return element(SupportingEvidenceBundle.builder()
            .name(name)
            .document(testDocumentReference())
            .dateTimeUploaded(uploadedAt)
            .uploadedBy(uploadedBy)
            .type(type)
            .confidential(isConfidential ? List.of("CONFIDENTIAL") : List.of())
            .build());
    }
}
