package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.ApplicationType;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.document.SealType;
import uk.gov.hmcts.reform.fpl.service.DocumentSealingService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocumentConversionService;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;
import uk.gov.hmcts.reform.fpl.utils.PolicyHelper;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationType.C2_APPLICATION;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.REQUESTING_ADJOURNMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE_TIME;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadAdditionalApplicationsService {

    private static final String APPLICANT_SOMEONE_ELSE = "SOMEONE_ELSE";

    private final Time time;
    private final UserService userService;
    private final ManageDocumentService manageDocumentService;
    private final DocumentUploadHelper documentUploadHelper;
    private final DocumentSealingService documentSealingService;
    private final DocumentConversionService documentConversionService;

    public List<ApplicationType> getApplicationTypes(AdditionalApplicationsBundle bundle) {
        List<ApplicationType> applicationTypes = new ArrayList<>();
        if (!isNull(bundle.getC2DocumentBundle()) || !isNull(bundle.getC2DocumentBundleConfidential())) {
            applicationTypes.add(C2_APPLICATION);
        }

        if (!isNull(bundle.getOtherApplicationsBundle())) {
            applicationTypes.add(ApplicationType.valueOf(
                bundle.getOtherApplicationsBundle().getApplicationType().name()));
        }
        return applicationTypes;
    }

    public AdditionalApplicationsBundle buildAdditionalApplicationsBundle(CaseData caseData) {
        String applicantName = getSelectedApplicantName(caseData.getApplicantsList(), caseData.getOtherApplicant())
            .filter(not(String::isBlank))
            .orElseThrow(() -> new IllegalArgumentException("Applicant should not be empty"));

        final String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();
        final LocalDateTime now = time.now();

        List<Element<Respondent>> respondentsInCase = caseData.getAllRespondents();

        AdditionalApplicationsBundleBuilder additionalApplicationsBundleBuilder = AdditionalApplicationsBundle.builder()
            .pbaPayment(caseData.getTemporaryPbaPayment())
            .amountToPay(caseData.getAmountToPay())
            .author(uploadedBy)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(now, DATE_TIME))
            .applicationReviewed(YesNo.NO);

        List<AdditionalApplicationType> additionalApplicationTypeList = caseData.getAdditionalApplicationType();
        if (additionalApplicationTypeList.contains(AdditionalApplicationType.C2_ORDER)) {
            C2DocumentBundle c2DocumentBundle = buildC2DocumentBundle(
                caseData, applicantName, respondentsInCase, uploadedBy, now);
            if (YesNo.YES.equals(caseData.getIsC2Confidential())) {
                additionalApplicationsBundleBuilder.c2DocumentBundleConfidential(c2DocumentBundle);
                buildC2BundleByPolicy(caseData, c2DocumentBundle, additionalApplicationsBundleBuilder);
            } else {
                additionalApplicationsBundleBuilder.c2DocumentBundle(c2DocumentBundle);
            }
        }

        if (additionalApplicationTypeList.contains(AdditionalApplicationType.OTHER_ORDER)) {
            additionalApplicationsBundleBuilder.otherApplicationsBundle(buildOtherApplicationsBundle(
                caseData, applicantName, respondentsInCase, uploadedBy, now
            ));
        }

        return additionalApplicationsBundleBuilder.build();
    }

    public AdditionalApplicationsBundle sealAllDocuments(AdditionalApplicationsBundle bundle, CaseData caseData) {
        AdditionalApplicationsBundleBuilder bundleBuilder = bundle.toBuilder();

        if (!isEmpty(bundle.getC2DocumentBundle())) {
            bundleBuilder.c2DocumentBundle(convertC2Bundle(bundle.getC2DocumentBundle(), caseData));
        }
        if (!isEmpty(bundle.getC2DocumentBundleConfidential())) {
            convertConfidentialC2Bundle(caseData, bundle.getC2DocumentBundleConfidential(), bundleBuilder);
        }

        // If we have an other application, do conversion if needed
        if (!isEmpty(bundle.getOtherApplicationsBundle())) {
            bundleBuilder.otherApplicationsBundle(convertOtherBundle(bundle.getOtherApplicationsBundle(), caseData));
        }
        return bundleBuilder.build();
    }

    private void buildC2BundleByPolicy(CaseData caseData, C2DocumentBundle c2Bundle,
                                      AdditionalApplicationsBundleBuilder builder) {
        final Set<CaseRole> caseRoles = userService.getCaseRoles(caseData.getId());

        final Consumer<String> setConfidentialC2 = fieldName -> {
            try {
                AdditionalApplicationsBundleBuilder.class.getMethod(fieldName, C2DocumentBundle.class)
                    .invoke(builder, c2Bundle);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        };

        PolicyHelper.processFieldByPolicyDatas(caseData.getRespondentPolicyData(), "c2DocumentBundleResp",
            caseRoles, setConfidentialC2);
        PolicyHelper.processFieldByPolicyDatas(caseData.getChildPolicyData(), "c2DocumentBundleChild",
            caseRoles, setConfidentialC2);
        if (PolicyHelper.isPolicyMatchingCaseRoles(caseData.getLocalAuthorityPolicy(), caseRoles)) {
            setConfidentialC2.accept("c2DocumentBundleLA");
        }
    }

    public List<Element<C2DocumentBundle>> sortOldC2DocumentCollection(List<Element<C2DocumentBundle>> bundles) {
        bundles.sort(comparing(bundle -> bundle.getValue().getUploadedDateTime(), reverseOrder()));
        return bundles;
    }

    private Optional<String> getSelectedApplicantName(DynamicList applicantsList, String otherApplicant) {
        if (Objects.nonNull(applicantsList)) {
            DynamicListElement selectedElement = applicantsList.getValue();

            if (isNotEmpty(selectedElement)) {
                if (APPLICANT_SOMEONE_ELSE.equals(selectedElement.getCode())) {
                    return isBlank(otherApplicant) ? Optional.empty() : Optional.of(otherApplicant);
                } else {
                    return Optional.of(selectedElement.getLabel());
                }
            }
        }
        return Optional.empty();
    }

    private C2DocumentBundle buildC2DocumentBundle(CaseData caseData,
                                                   String applicantName,
                                                   List<Element<Respondent>> respondentsInCase,
                                                   String uploadedBy,
                                                   LocalDateTime uploadedTime) {
        C2DocumentBundle temporaryC2Document = caseData.getTemporaryC2Document();

        List<Element<SupportingEvidenceBundle>> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(caseData,
            temporaryC2Document.getSupportingEvidenceBundle(), uploadedBy, uploadedTime
        );

        List<Element<Supplement>> updatedSupplementsBundle =
            getSupplementsBundle(temporaryC2Document.getSupplementsBundle(),
                uploadedBy, uploadedTime);


        return temporaryC2Document.toBuilder()
            .id(UUID.randomUUID())
            .applicantName(applicantName)
            .author(uploadedBy)
            .document(temporaryC2Document.getDocument())
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(uploadedTime, DATE_TIME))
            .supplementsBundle(updatedSupplementsBundle)
            .supportingEvidenceBundle(updatedSupportingEvidenceBundle)
            .type(caseData.getC2Type())
            .respondents(respondentsInCase)
            .build();
    }

    private OtherApplicationsBundle buildOtherApplicationsBundle(CaseData caseData,
                                                                 String applicantName,
                                                                 List<Element<Respondent>> respondentsInCase,
                                                                 String uploadedBy,
                                                                 LocalDateTime uploadedTime) {
        OtherApplicationsBundle temporaryOtherApplicationsBundle = caseData.getTemporaryOtherApplicationsBundle();

        List<Element<SupportingEvidenceBundle>> updatedSupportingEvidenceBundle = getSupportingEvidenceBundle(caseData,
            temporaryOtherApplicationsBundle.getSupportingEvidenceBundle(), uploadedBy, uploadedTime
        );

        List<Element<Supplement>> updatedSupplementsBundle = getSupplementsBundle(
            temporaryOtherApplicationsBundle.getSupplementsBundle(), uploadedBy, uploadedTime);


        return temporaryOtherApplicationsBundle.toBuilder()
            .author(uploadedBy)
            .id(UUID.randomUUID())
            .applicantName(applicantName)
            .uploadedDateTime(formatLocalDateTimeBaseUsingFormat(uploadedTime, DATE_TIME))
            .applicationType(temporaryOtherApplicationsBundle.getApplicationType())
            .document(temporaryOtherApplicationsBundle.getDocument())
            .supportingEvidenceBundle(updatedSupportingEvidenceBundle)
            .supplementsBundle(updatedSupplementsBundle)
            .respondents(respondentsInCase)
            .build();
    }

    public OtherApplicationsBundle convertOtherBundle(OtherApplicationsBundle bundle, CaseData caseData) {
        return bundle.toBuilder()
            .document(sealDocument(bundle.getDocument(), caseData))
            .supplementsBundle(!isEmpty(bundle.getSupplementsBundle())
                ? getSupplementsBundleConverted(bundle.getSupplementsBundle(), caseData)
                 : List.of())
            .build();
    }

    public C2DocumentBundle convertC2Bundle(C2DocumentBundle bundle, CaseData caseData) {
        return bundle.toBuilder()
            .document(sealDocument(bundle.getDocument(), caseData))
            .supplementsBundle(!isEmpty(bundle.getSupplementsBundle())
                ? getSupplementsBundleConverted(bundle.getSupplementsBundle(), caseData)
                : List.of())
            .build();
    }

    public void convertConfidentialC2Bundle(CaseData caseData, C2DocumentBundle bundle,
                                            AdditionalApplicationsBundleBuilder builder) {
        C2DocumentBundle convertedC2 = convertC2Bundle(bundle, caseData);
        builder.c2DocumentBundleConfidential(convertedC2);
        buildC2BundleByPolicy(caseData, convertedC2, builder);
    }

    private List<Element<SupportingEvidenceBundle>> getSupportingEvidenceBundle(CaseData caseData,
        List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle,
        String uploadedBy, LocalDateTime uploadedDateTime) {

        supportingEvidenceBundle.forEach(supportingEvidence -> {
            supportingEvidence.getValue().setDateTimeUploaded(uploadedDateTime);
            supportingEvidence.getValue().setUploadedBy(uploadedBy);
            supportingEvidence.getValue().setUploaderType(manageDocumentService.getUploaderType(caseData));
            supportingEvidence.getValue().setUploaderCaseRoles(new ArrayList<>(userService
                .getCaseRoles(caseData.getId())));
        });

        return supportingEvidenceBundle;
    }

    private List<Element<Supplement>> getSupplementsBundleConverted(List<Element<Supplement>> supplementsBundle,
                                                                    CaseData caseData) {
        return supplementsBundle.stream().map(supplementElement -> {
            Supplement incomingSupplement = supplementElement.getValue();

            DocumentReference sealedDocument = documentSealingService.sealDocument(incomingSupplement.getDocument(),
                caseData.getCourt(), SealType.ENGLISH);

            Supplement modifiedSupplement = incomingSupplement.toBuilder()
                .document(sealedDocument)
                .build();

            return supplementElement.toBuilder().value(modifiedSupplement).build();
        }).collect(Collectors.toList());
    }

    private List<Element<Supplement>> getSupplementsBundle(
        List<Element<Supplement>> supplementsBundle, String uploadedBy, LocalDateTime dateTime) {

        return supplementsBundle.stream().map(supplementElement -> {
            Supplement incomingSupplement = supplementElement.getValue();

            Supplement modifiedSupplement = incomingSupplement.toBuilder()
                .dateTimeUploaded(dateTime)
                .uploadedBy(uploadedBy)
                .build();

            return supplementElement.toBuilder().value(modifiedSupplement).build();
        }).collect(Collectors.toList());

    }

    private DocumentReference sealDocument(DocumentReference originalDoc, CaseData caseData) {
        return documentSealingService.sealDocument(originalDoc, caseData.getCourt(), SealType.ENGLISH);
    }

    private boolean onlyApplyingForC2(CaseData caseData) {
        return caseData.getAdditionalApplicationType().contains(AdditionalApplicationType.C2_ORDER)
            && caseData.getAdditionalApplicationType().size() == 1;
    }

    public boolean onlyApplyingForAnAdjournment(CaseData caseData, C2DocumentBundle temporaryC2Bundle) {
        return onlyApplyingForC2(caseData)
            && temporaryC2Bundle.getC2AdditionalOrdersRequested().size() == 1
            && temporaryC2Bundle.getC2AdditionalOrdersRequested().contains(REQUESTING_ADJOURNMENT);
    }

    /** Only skip the payment if the hearing we are asking to adjourn is >= 14 days away,
     * AND we're not applying for an 'other order (C1/C100/supplement)' at the same time,
     * AND we aren't applying for other C2 things (surname, guardian appt, etc).
     * @param caseData - CaseData at current callback
     * @param hearing - the selected hearing that the user is applying to adjourn
     * @param temporaryC2Bundle - the current C2 bundle as amended during the callback
     * @return - boolean for whether we should skip the payments or not
     */
    public boolean shouldSkipPayments(CaseData caseData, HearingBooking hearing, C2DocumentBundle temporaryC2Bundle) {
        return (Duration.between(LocalDateTime.now(), hearing.getStartDate()).toDays() >= 14L)
            && onlyApplyingForAnAdjournment(caseData, temporaryC2Bundle);
    }
}
