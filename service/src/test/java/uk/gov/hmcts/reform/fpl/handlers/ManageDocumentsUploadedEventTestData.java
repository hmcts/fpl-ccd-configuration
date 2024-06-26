package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.cfv.UploadBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.handlers.NotificationEventHandlerTestData.LOCAL_AUTHORITY_CODE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

public class ManageDocumentsUploadedEventTestData {

    public static final Long CASE_ID = 12345L;
    public static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);

    public static final Set<ConfidentialLevel> CTSC_ALLOWED = Set.of(ConfidentialLevel.CTSC);
    public static final Set<ConfidentialLevel> LA_ALLOWED = Set.of(ConfidentialLevel.LA, ConfidentialLevel.CTSC);
    public static final Set<ConfidentialLevel> NON_CONFIDENTIAL_ALLOWED = Set.of(ConfidentialLevel.NON_CONFIDENTIAL,
        ConfidentialLevel.LA, ConfidentialLevel.CTSC);


    private ManageDocumentsUploadedEventTestData() {
    }

    public static CaseData.CaseDataBuilder<?,?> commonCaseBuilder() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_ID.toString())
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .hearingDetails(wrapElementsWithUUIDs(HearingBooking.builder()
                .startDate(HEARING_DATE)
                .type(HearingType.CASE_MANAGEMENT)
                .build()));
    }

    public static CaseData.CaseDataBuilder<?,?> addManagedDocument(CaseData.CaseDataBuilder<?,?> builder,
                                                                   DocumentType docType,
                                                                   ConfidentialLevel confidentialLevel,
                                                                   List<Element<ManagedDocument>> documents)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String fieldName = docType.getBaseFieldNameResolver().apply(confidentialLevel);
        Method builderMethod = CaseData.CaseDataBuilder.class.getMethod(fieldName, List.class);

        return (CaseData.CaseDataBuilder<?,?>) builderMethod.invoke(builder, documents);
    }

    public static Stream<Arguments> allUploadableDocumentsTypeParameters() {
        List<Arguments> streamList = new ArrayList<>();

        for (DocumentType docType : DocumentType.values()) {
            if (isNotEmpty(docType.getBaseFieldNameResolver()) && docType.isUploadable()) {
                for (ConfidentialLevel level : ConfidentialLevel.values()) {
                    streamList.add(Arguments.of(docType, level));
                }
            }
        }

        return streamList.stream();
    }

    public static CaseData buildSubmittedCaseDataWithNewDocumentUploaded(List<DocumentType> docTypes,
                                                                         List<ConfidentialLevel> confidentialLevels)
        throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        CaseData.CaseDataBuilder<?,?> caseDataBuilder = commonCaseBuilder();

        HearingDocuments.HearingDocumentsBuilder hearingDocumentsBuilder = HearingDocuments.builder();
        boolean hasHearingDocument = false;

        for (DocumentType docType : docTypes) {
            if (docType.getBaseFieldNameResolver() != null) {

                if (docType.equals(DocumentType.COURT_BUNDLE) || docType.equals(DocumentType.CASE_SUMMARY)
                    || docType.equals(DocumentType.POSITION_STATEMENTS)
                    || docType.equals(DocumentType.SKELETON_ARGUMENTS)) {
                    for (ConfidentialLevel confidentialLevel : confidentialLevels) {
                        String fieldName = docType.getBaseFieldNameResolver().apply(confidentialLevel)
                            .split("\\.")[1];

                        Method builderMethod =
                            HearingDocuments.HearingDocumentsBuilder.class.getMethod(fieldName, List.class);

                        hearingDocumentsBuilder = (HearingDocuments.HearingDocumentsBuilder) builderMethod
                            .invoke(hearingDocumentsBuilder,
                                wrapElementsWithUUIDs(docType.getWithDocumentBuilder().apply(UploadBundle.builder()
                                    .document(getPDFDocument())
                                    .translationRequirement(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                                    .build())));

                        hasHearingDocument = true;
                    }
                } else {
                    for (ConfidentialLevel confidentialLevel : confidentialLevels) {
                        caseDataBuilder = addManagedDocument(caseDataBuilder, docType, confidentialLevel,
                            wrapElementsWithUUIDs(ManagedDocument.builder()
                                .document(getPDFDocument())
                                .translationRequirements(LanguageTranslationRequirement.ENGLISH_TO_WELSH)
                                .build()));
                    }
                }
            } else {
                throw new NoSuchMethodException(docType.toString() + ".getBaseFieldNameResolver");
            }
        }

        if (hasHearingDocument) {
            caseDataBuilder.hearingDocuments(hearingDocumentsBuilder.build());
        }

        return caseDataBuilder.build();
    }

    public static DocumentReference getPDFDocument() {
        return TestDataHelper.testDocumentReference(randomAlphanumeric(10).concat(".pdf"));
    }

    public static DocumentReference getNonPDFDocument() {
        return TestDataHelper.testDocumentReference(randomAlphanumeric(10));
    }

    public static boolean isHearingDocument(DocumentType docType) {
        return DocumentType.COURT_BUNDLE.equals(docType) || DocumentType.CASE_SUMMARY.equals(docType)
               || DocumentType.POSITION_STATEMENTS.equals(docType) || DocumentType.SKELETON_ARGUMENTS.equals(docType);
    }
}
