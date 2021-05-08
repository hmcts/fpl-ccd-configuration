package uk.gov.hmcts.reform.fpl.service.document.transformer;

import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

public class DocumentViewTestHelper {

    private DocumentViewTestHelper() {
    }

    public static final Element<SupportingEvidenceBundle> ADMIN_CONFIDENTIAL_DOCUMENT
        = buildFurtherEvidenceBundle("Admin uploaded evidence1", "HMCTS", true);

    public static final Element<SupportingEvidenceBundle> ADMIN_NON_CONFIDENTIAL_DOCUMENT
        = buildFurtherEvidenceBundle("Admin uploaded evidence2", "HMCTS", false);

    public static final Element<SupportingEvidenceBundle> LA_CONFIDENTIAL_DOCUMENT
        = buildFurtherEvidenceBundle("LA uploaded evidence1", "Kurt solicitor", true);

    public static final Element<SupportingEvidenceBundle> LA_NON_CONFIDENTIAL_DOCUMENT =
        buildFurtherEvidenceBundle("LA uploaded evidence2", "Kurt solicitor", false);

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

    private static Element<SupportingEvidenceBundle> buildFurtherEvidenceBundle(
        String name, String uploadedBy, boolean isConfidential) {
        return element(SupportingEvidenceBundle.builder()
            .name(name)
            .document(testDocumentReference())
            .dateTimeUploaded(LocalDateTime.now())
            .uploadedBy(uploadedBy)
            .confidential(isConfidential ? List.of("CONFIDENTIAL") : List.of())
            .build());
    }
}
