package uk.gov.hmcts.reform.fpl.service.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.ManageDocumentRemovalReason;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.cfv.ConfidentialLevel;
import uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType;
import uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType;
import uk.gov.hmcts.reform.fpl.events.ManageDocumentsUploadedEvent;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingDocuments;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.cfv.UploadBundle;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.ApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.ObjectHelper;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.designatedLASolicitors;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.AA_PARENT_APPLICATIONS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C1_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.C2_APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.COURT_BUNDLE;
import static uk.gov.hmcts.reform.fpl.enums.cfv.DocumentType.PLACEMENT_RESPONSES;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.BARRISTER;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.DESIGNATED_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.HMCTS;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SECONDARY_LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.notification.DocumentUploaderType.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManageDocumentService {
    private final PlacementService placementService;
    private final DynamicListService dynamicListService;
    private final UserService userService;

    public static final String DOCUMENT_ACKNOWLEDGEMENT_KEY = "ACK_RELATED_TO_CASE";

    public static final String DOCUMENT_TO_BE_REMOVED_SEPARATOR = "###";

    private final CaseConverter caseConverter;

    private String getDocumentListHolderFieldName(String fieldName) {
        String[] splitFieldName = fieldName.split("\\.");
        if (splitFieldName.length == 2) {
            return splitFieldName[1];
        } else {
            return fieldName;
        }
    }

    private Class getDocumentListHolderClass(String fieldName) {
        String[] splitFieldName = fieldName.split("\\.");
        if (splitFieldName.length == 2) {
            String parentFieldName = splitFieldName[0];
            if ("hearingDocuments".equals(parentFieldName)) {
                return HearingDocuments.class;
            } else {
                throw new IllegalStateException("unresolved target class: " + parentFieldName);
            }
        }
        return CaseData.class;
    }

    private Object getDocumentListHolder(String fieldName, CaseData caseData) throws Exception {
        String[] splitFieldName = fieldName.split("\\.");
        if (splitFieldName.length == 2) {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(CaseData.class, splitFieldName[0]);
            if (pd != null) {
                return pd.getReadMethod().invoke(caseData);
            } else {
                throw new IllegalArgumentException("unable to get property descriptor from CaseData.class: "
                    + splitFieldName[0]);
            }
        } else {
            return caseData;
        }
    }

    public List<CaseRole> getUploaderCaseRoles(CaseData caseData) {
        return new ArrayList<>(userService.getCaseRoles(caseData.getId()));
    }

    public DocumentUploaderType getUploaderType(CaseData caseData) {
        final List<CaseRole> caseRoles = getUploaderCaseRoles(caseData);
        if (caseRoles.stream().anyMatch(representativeSolicitors()::contains)) {
            return SOLICITOR;
        }
        if (caseRoles.contains(CaseRole.BARRISTER)) {
            return BARRISTER;
        }
        if (caseRoles.contains(CaseRole.CAFCASSSOLICITOR)) {
            return CAFCASS;
        }
        if (userService.isHmctsUser()) {
            return HMCTS;
        }
        if (caseRoles.stream().anyMatch(designatedLASolicitors()::contains)) {
            return DESIGNATED_LOCAL_AUTHORITY;
        }
        if (caseRoles.contains(CaseRole.LASHARED)) {
            return SECONDARY_LOCAL_AUTHORITY;
        }
        if (Optional.ofNullable(userService.getIdamRoles()).orElse(Set.of()).contains(UserRole.CAFCASS.getRoleName())) {
            return CAFCASS;
        }

        throw new IllegalStateException("Unable to determine document uploader type");
    }

    private boolean isLocalAuthority(DocumentUploaderType type) {
        return List.of(DESIGNATED_LOCAL_AUTHORITY, SECONDARY_LOCAL_AUTHORITY).contains(type);
    }

    public boolean allowMarkDocumentConfidential(CaseData caseData) {
        return !List.of(SOLICITOR, BARRISTER, CAFCASS).contains(getUploaderType(caseData));
    }

    public boolean allowSelectDocumentTypeToRemoveDocument(CaseData caseData) {
        return List.of(HMCTS).contains(getUploaderType(caseData));
    }

    private Element<? extends WithDocument> handlePlacementResponseRemoval(CaseData caseData,
                                                                           UUID documentElementId,
                                                                           Map<String, Object> output) {
        Element<Placement> placement = caseData.getPlacementEventData().getPlacements().stream()
            .filter(placementElement -> placementElement.getValue().getNoticeDocuments().stream()
                .anyMatch(nd -> documentElementId.equals(nd.getId())))
            .findAny().orElseThrow(() -> new IllegalArgumentException("Fail to locate placement"));

        Element<PlacementNoticeDocument> target = placement.getValue().getNoticeDocuments().stream()
            .filter(nd -> documentElementId.equals(nd.getId()))
            .findAny().orElseThrow(() -> new IllegalArgumentException("Fail to locate notice documents"));

        placement.getValue().getNoticeDocuments().remove(target);

        List<Element<PlacementNoticeDocument>> noticeDocumentsRemoved = placement.getValue()
            .getNoticeDocumentsRemoved() == null ? new ArrayList<>() : placement.getValue()
            .getNoticeDocumentsRemoved();
        noticeDocumentsRemoved.add(target);
        placement.getValue().setNoticeDocumentsRemoved(noticeDocumentsRemoved);

        output.put("placements", caseData.getPlacementEventData().getPlacements());
        output.put("placementsNonConfidential", caseData.getPlacementEventData()
            .getPlacementsNonConfidential(true));
        output.put("placementsNonConfidentialNotices", caseData.getPlacementEventData()
            .getPlacementsNonConfidential(true));

        return target;
    }

    @SuppressWarnings("unchecked")
    private List<Element<? extends WithDocument>> handleCourtBundleRemoval(String fieldName,
                                                                           CaseData caseData,
                                                                           UUID documentElementId,
                                                                           Map<String, Object> output) {
        List<Element> listOfElement = null;
        HearingDocuments hearingDocuments = caseData.getHearingDocuments();
        switch (fieldName) {
            case "hearingDocuments.courtBundleListV2":
                listOfElement = new ArrayList<>(hearingDocuments.getCourtBundleListV2());
                break;
            case "hearingDocuments.courtBundleListLA":
                listOfElement = new ArrayList<>(hearingDocuments.getCourtBundleListLA());
                break;
            case "hearingDocuments.courtBundleListCTSC":
                listOfElement = new ArrayList<>(hearingDocuments.getCourtBundleListCTSC());
                break;
            default:
                throw new IllegalStateException("unrecognised field name: " + fieldName);
        }

        Element<HearingCourtBundle> hcbElement =
            Stream.concat(hearingDocuments.getCourtBundleListCTSC().stream(),
                    Stream.concat(hearingDocuments.getCourtBundleListV2().stream(),
                        hearingDocuments.getCourtBundleListLA().stream()))
                .filter(loe -> loe.getValue().getCourtBundle().stream().anyMatch(
                    cb -> documentElementId.equals(cb.getId())
                )).findFirst()
                .orElseThrow(() -> new IllegalStateException("Fail to find the target hearing court bundle"));

        Element<CourtBundle> targetElement = hcbElement.getValue().getCourtBundle().stream()
            .filter(i -> documentElementId.equals(i.getId())).findFirst().orElseThrow(() -> {
                throw new IllegalStateException("Fail to locate the target document");
            });
        List<Element<? extends WithDocument>> targetElements = new ArrayList<>();

        if (hcbElement.getValue().getCourtBundle().size() == 1) {
            // multiple court bundles(nc) keep hcbElement, otherwise remove it
            listOfElement.remove(hcbElement);
        }
        hcbElement.getValue().getCourtBundle().remove(targetElement);
        targetElements.add(targetElement);

        List<Element<?>> listOfRemovedElement = getListOfRemovedElement(caseData, COURT_BUNDLE);

        final boolean isNewHearingCourtBundleInRemovedList = !listOfRemovedElement.stream()
            .anyMatch(e -> e.getId().equals(hcbElement.getId()));

        @SuppressWarnings("unchecked")
        Element<HearingCourtBundle> hcbFromRemovedList = ((List<Element<HearingCourtBundle>>) (List<?>)
            listOfRemovedElement).stream()
            .filter(e -> e.getId().equals(hcbElement.getId())).findFirst()
            .orElse(element(hcbElement.getId(), hcbElement.getValue().toBuilder()
                .courtBundle(new ArrayList<>())
                .build()));
        hcbFromRemovedList.getValue().getCourtBundle().add(targetElement);
        if (isNewHearingCourtBundleInRemovedList) {
            listOfRemovedElement.add(hcbFromRemovedList);
        }

        output.put(DocumentType.toJsonFieldName(fieldName), listOfElement);
        output.put(DocumentType.toJsonFieldName(COURT_BUNDLE.getFieldNameOfRemovedList()), listOfRemovedElement);
        return targetElements;
    }

    private boolean checkEvidenceBundle(ApplicationsBundle bundle, UUID documentElementId) {
        return bundle != null && bundle.getSupportingEvidenceBundle().stream()
            .anyMatch(e -> documentElementId.equals(e.getId()));
    }

    private Element<AdditionalApplicationsBundle> locateAdditionalApplicationBundleToBeModified(CaseData caseData,
        UUID documentElementId) {
        return Optional.ofNullable(caseData.getAdditionalApplicationsBundle())
            .orElse(List.of()).stream()
            .filter(adb -> checkEvidenceBundle(adb.getValue().getC2DocumentBundle(), documentElementId)
                || checkEvidenceBundle(adb.getValue().getC2DocumentBundleConfidential(), documentElementId)
                || checkEvidenceBundle(adb.getValue().getOtherApplicationsBundle(), documentElementId))
            .findFirst()
            .orElse(null);
    }

    private static C2DocumentBundle getC2DocumentBundle(AdditionalApplicationsBundle aab,
                                                        String propertyName) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(AdditionalApplicationsBundle.class, propertyName);
        if (pd == null) {
            throw new AssertionError(format("Fail to get property %s from %s", propertyName,
                AdditionalApplicationsBundle.class));
        }
        Method getter = pd.getReadMethod();
        try {
            return (C2DocumentBundle) getter.invoke(aab);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(format("Fail to get property %s from %s", propertyName,
                AdditionalApplicationsBundle.class));
        }
    }

    private static AdditionalApplicationsBundle applyNewList(String propertyName,
                                                             AdditionalApplicationsBundle aab,
                                                             C2DocumentBundle c2DocumentBundle,
                                                             List<Element<SupportingEvidenceBundle>> newList) {
        try {
            Method builderPropertySetter = AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder.class
                .getMethod(propertyName, C2DocumentBundle.class);
            C2DocumentBundle newC2DocumentBundle = c2DocumentBundle.toBuilder()
                .supportingEvidenceBundle(newList)
                .build();
            return ((AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder)
                builderPropertySetter.invoke(aab.toBuilder(), newC2DocumentBundle)).build();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new AssertionError(format("Fail to get property %s from %s", propertyName,
                AdditionalApplicationsBundle.AdditionalApplicationsBundleBuilder.class));
        }
    }

    private void appendToRemovedList(DocumentType documentType,
                                     CaseData caseData,
                                     Element removed,
                                     Map<String, Object> output) {
        List<Element<?>> removedList = getListOfRemovedElement(caseData, documentType);
        removedList.add(removed);
        output.put(documentType.getJsonFieldNameOfRemovedList(), removedList);
    }

    private static Element<ManagedDocument> toManagedDocumentElement(Element<SupportingEvidenceBundle> removed) {
        SupportingEvidenceBundle seb = removed.getValue();
        return element(removed.getId(), ManagedDocument.builder()
            .uploaderType(seb.getUploaderType())
            .uploaderCaseRoles(seb.getUploaderCaseRoles())
            .translationRequirements(seb.getTranslationRequirements())
            .markAsConfidential(seb.getMarkAsConfidential())
            .document(seb.getDocument())
            .build());
    }

    private List<String> getC2DocumentBundleProperties() {
        List<String> allProperties = new ArrayList<>(List.of("c2DocumentBundle", "c2DocumentBundleConfidential",
            "c2DocumentBundleLA"));
        for (int i = 0; i <= 14; i++) {
            allProperties.add("c2DocumentBundleChild" + i);
        }
        for (int i = 0; i <= 9; i++) {
            allProperties.add("c2DocumentBundleResp" + i);
        }
        return allProperties;
    }

    @SuppressWarnings("unchecked")
    private Element<? extends WithDocument> handleSupportingDocumentsInC1WithSupplementRemoval(
        CaseData caseData, UUID documentElementId, Map<String, Object> output) {
        SubmittedC1WithSupplementBundle targetBundle = Optional.ofNullable(caseData.getSubmittedC1WithSupplement())
            .orElse(null);

        Element<SupportingEvidenceBundle> removed = null;
        if (targetBundle != null) {
            removed = ElementUtils.findElement(documentElementId, targetBundle.getSupportingEvidenceBundle())
                .orElseThrow(() -> new AssertionError(format("target element not found (%s)", documentElementId)));
            List<Element<SupportingEvidenceBundle>> newList = targetBundle
                .getSupportingEvidenceBundle().stream()
                .filter(el -> !documentElementId.equals(el.getId()))
                .toList();

            targetBundle = targetBundle.toBuilder().supportingEvidenceBundle(newList).build();
            assert removed != null;
            appendToRemovedList(C1_APPLICATION_DOCUMENTS, caseData, toManagedDocumentElement(removed), output);
            output.put("submittedC1WithSupplement", targetBundle);
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    private Element<? extends WithDocument> handleC1SupportingDocumentsInAdditionalApplicationsRemoval(
        CaseData caseData, UUID documentElementId, Map<String, Object> output) {
        Element<AdditionalApplicationsBundle> targetBundle = locateAdditionalApplicationBundleToBeModified(caseData,
            documentElementId);
        if (targetBundle == null) {
            return handleSupportingDocumentsInC1WithSupplementRemoval(caseData, documentElementId, output);
        }

        Element<SupportingEvidenceBundle> removed = null;
        if (targetBundle != null) {
            AdditionalApplicationsBundle aab = targetBundle.getValue();
            removed = ElementUtils.findElement(documentElementId, aab.getOtherApplicationsBundle()
                    .getSupportingEvidenceBundle())
                .orElseThrow(() -> new AssertionError(format("target element not found (%s)", documentElementId)));
            List<Element<SupportingEvidenceBundle>> newList = aab.getOtherApplicationsBundle()
                .getSupportingEvidenceBundle().stream()
                .filter(el -> !Arrays.asList(documentElementId).contains(el.getId()))
                .toList();
            targetBundle.setValue(aab.toBuilder()
                .otherApplicationsBundle(aab.getOtherApplicationsBundle().toBuilder()
                    .supportingEvidenceBundle(newList)
                    .build())
                .build());
            assert removed != null;
            appendToRemovedList(C1_APPLICATION_DOCUMENTS, caseData, toManagedDocumentElement(removed), output);

            output.put("additionalApplicationsBundle", caseData.getAdditionalApplicationsBundle());
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    private Element<? extends WithDocument> handleC2SupportingDocumentsInAdditionalApplicationsRemoval(
        CaseData caseData, UUID documentElementId, Map<String, Object> output) {
        Element<AdditionalApplicationsBundle> targetBundle = locateAdditionalApplicationBundleToBeModified(caseData,
            documentElementId);

        Element<SupportingEvidenceBundle> removed = null;
        if (targetBundle != null) {
            AdditionalApplicationsBundle aab = targetBundle.getValue();
            for (String propertyName : getC2DocumentBundleProperties()) {
                C2DocumentBundle c2DocumentBundle = getC2DocumentBundle(aab, propertyName);
                if (isEmpty(c2DocumentBundle)) {
                    continue;
                }
                removed = ElementUtils.findElement(documentElementId, c2DocumentBundle
                        .getSupportingEvidenceBundle())
                    .orElseThrow(
                        () -> new AssertionError(format("target element not found (%s)", documentElementId)));
                List<Element<SupportingEvidenceBundle>> newList = c2DocumentBundle.getSupportingEvidenceBundle()
                    .stream()
                    .filter(el -> !Arrays.asList(documentElementId).contains(el.getId()))
                    .toList();
                targetBundle.setValue(applyNewList(propertyName, aab, c2DocumentBundle, newList));
                aab = targetBundle.getValue();
            }
            if (removed != null) {
                appendToRemovedList(C2_APPLICATION_DOCUMENTS, caseData, toManagedDocumentElement(removed), output);
            }
            output.put("additionalApplicationsBundle", caseData.getAdditionalApplicationsBundle());
        }
        return removed;
    }

    @SuppressWarnings("unchecked")
    private Element<? extends WithDocument> handleGeneralDocumentTypeRemoval(DocumentType documentType,
                                                                             String fieldName,
                                                                             CaseData caseData, UUID documentElementId,
                                                                             Map<String, Object> output) {
        if (C1_APPLICATION_DOCUMENTS.name().equals(fieldName)) {
            return handleC1SupportingDocumentsInAdditionalApplicationsRemoval(caseData, documentElementId, output);
        } else if (C2_APPLICATION_DOCUMENTS.name().equals(fieldName)) {
            return handleC2SupportingDocumentsInAdditionalApplicationsRemoval(caseData, documentElementId, output);
        } else {
            List<Element<?>> listOfElement = readFromFieldName(caseData, fieldName);
            Element removed = listOfElement.stream().filter(i -> documentElementId.equals(i.getId())).findFirst()
                .orElseThrow(() -> {
                    throw new IllegalStateException("Fail to locate the target document");
                });
            listOfElement.remove(removed);

            output.put(DocumentType.toJsonFieldName(fieldName), listOfElement);
            appendToRemovedList(documentType, caseData, removed, output);
            return removed;
        }
    }

    private List<Element<?>> getListOfRemovedElement(CaseData caseData, DocumentType documentType) {
        return Optional.ofNullable(readFromFieldName(caseData,
                documentType.getFieldNameOfRemovedList())).orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> removeDocuments(CaseData caseData) {
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();

        String removalReason;
        if (ManageDocumentRemovalReason.OTHER == eventData.getManageDocumentRemoveDocReason()) {
            removalReason = eventData.getManageDocumentRemoveDocAnotherReason();
        } else {
            removalReason = eventData.getManageDocumentRemoveDocReason().getDescription();
        }

        final Map<String, Object> output = new HashMap<>();
        DynamicListElement selected = eventData.getDocumentsToBeRemoved().getValue();
        List<Element<? extends WithDocument>> targetElements = getSelectedDocuments(
            caseData, selected, Optional.of(output));
        targetElements.forEach(t -> t.getValue().setRemovalReason(removalReason));

        return output;
    }

    public List<Element<? extends WithDocument>> getSelectedDocuments(CaseData caseData, DynamicListElement selected,
                                                                      Optional<Map<String, Object>> fieldsToUpdate) {
        String[] split = selected.getCode().split(DOCUMENT_TO_BE_REMOVED_SEPARATOR);
        String fieldName = split[0];
        DocumentType documentType = DocumentType.fromFieldName(fieldName);
        UUID documentElementId = UUID.fromString(split[1]);
        List<Element<? extends WithDocument>> targetElements = new ArrayList<>();

        final Map<String, Object> output = fieldsToUpdate.orElse(new HashMap<>());
        if (documentType == PLACEMENT_RESPONSES) {
            targetElements.add(handlePlacementResponseRemoval(caseData, documentElementId, output));
        } else {
            if (documentType == COURT_BUNDLE) {
                targetElements.addAll(handleCourtBundleRemoval(fieldName, caseData, documentElementId, output));
            } else {
                targetElements.add(handleGeneralDocumentTypeRemoval(documentType, fieldName, caseData,
                    documentElementId, output));
            }
        }

        return targetElements;
    }

    @SuppressWarnings("unchecked")
    private void uploadPlacementResponse(Map<String, Object> changes, DocumentUploaderType uploaderType,
                                         Element<UploadableDocumentBundle> e, CaseData caseData) {
        boolean isLocalAuthority = isLocalAuthority(uploaderType);
        boolean isSolicitor = uploaderType == SOLICITOR || uploaderType == CAFCASS;
        boolean isAdmin = !(isSolicitor || isLocalAuthority);

        Map<String, Object> initialisedPlacement = null;
        PlacementNoticeDocument.RecipientType recipientType = null;
        if (!isAdmin) {
            initialisedPlacement = initialisePlacementHearingResponseFields(caseData,
                isSolicitor ? PlacementNoticeDocument.RecipientType.RESPONDENT :
                    PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY);
            recipientType = isSolicitor ? PlacementNoticeDocument.RecipientType.RESPONDENT :
                PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY;
        } else {
            initialisedPlacement = initialisePlacementHearingResponseFields(caseData);
        }
        caseData.getPlacementEventData().setPlacement((Placement) initialisedPlacement.get("placement"));
        List<Element<PlacementNoticeDocument>> placementNoticeResponses = (List<Element<PlacementNoticeDocument>>)
            Optional.ofNullable(initialisedPlacement.get("placementNoticeResponses")).orElse(new ArrayList<>());
        placementNoticeResponses.add(element(PlacementNoticeDocument.builder()
            .type(recipientType)
            .response(e.getValue().getDocument())
            .uploaderType(uploaderType)
            .uploaderCaseRoles(getUploaderCaseRoles(caseData))
            .translationRequirements(e.getValue().getTranslationRequirements())
            .build()));
        caseData.setPlacementNoticeResponses(placementNoticeResponses);
        if (changes.containsKey("placements")) {
            caseData.getPlacementEventData().setPlacements((List<Element<Placement>>) changes.get("placements"));
        }

        PlacementEventData eventData = isAdmin
            ? updatePlacementNoticesAdmin(caseData)
            : ((isLocalAuthority ? updatePlacementNoticesLA(caseData) : updatePlacementNoticesSolicitor(caseData)));

        changes.put("placements", eventData.getPlacements());
        changes.put("placementsNonConfidential", eventData
            .getPlacementsNonConfidential(true));
        changes.put("placementsNonConfidentialNotices", eventData
            .getPlacementsNonConfidential(true));
    }

    @SuppressWarnings("unchecked")
    public void uploadGenericDocuments(Map<String, Object> changes, DocumentType dt, DocumentUploaderType uploaderType,
                                       Element<UploadableDocumentBundle> e, CaseData caseData) {
        boolean confidential = YES.equals(YesNo.fromString(e.getValue().getConfidential()));
        String fieldName = dt.getFieldName(uploaderType, confidential);

        Class documentListHolderClass = getDocumentListHolderClass(fieldName);
        String documentListHolderFieldName = getDocumentListHolderFieldName(fieldName);
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(documentListHolderClass,
            documentListHolderFieldName);
        if (pd == null) {
            throw new IllegalArgumentException("unable to get property descriptor from "
                + documentListHolderClass.getName() + " (" + documentListHolderFieldName + ")");
        }
        List<Element<?>> docs = null;
        if (changes.containsKey(documentListHolderFieldName)) {
            docs = (List<Element<?>>) changes.get(documentListHolderFieldName);
        }
        if (docs == null) {
            try {
                Object bean = getDocumentListHolder(fieldName, caseData);
                docs = (List<Element<?>>) pd.getReadMethod().invoke(bean);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to read bean property: " + fieldName);
            }
            if (docs == null) {
                docs = new ArrayList<>();
            }
        }
        UploadBundle bundle = UploadBundle.builder().document(e.getValue().getDocument())
            .uploaderType(uploaderType)
            .uploaderCaseRoles(getUploaderCaseRoles(caseData))
            .translationRequirement(e.getValue().getTranslationRequirements())
            .confidential(confidential)
            .build();
        docs.add(element(e.getId(), dt.getWithDocumentBuilder().apply(bundle)));
        changes.put(documentListHolderFieldName, docs);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadDocuments(CaseData caseData) {
        DocumentUploaderType uploaderType = getUploaderType(caseData);
        List<Element<UploadableDocumentBundle>> elements  = caseData.getManageDocumentEventData()
            .getUploadableDocumentBundle();
        Map<String, Object> changes = new HashMap<>();
        elements.forEach(e -> {
            DocumentType dt = e.getValue().getDocumentTypeSelected();
            if (dt == PLACEMENT_RESPONSES) {
                uploadPlacementResponse(changes, uploaderType, e,  caseData);
            } else {
                uploadGenericDocuments(changes, dt, uploaderType, e, caseData);
            }
        });
        return changes;
    }

    private boolean isHiddenFromUpload(DocumentType documentType, DocumentUploaderType uploaderType) {
        switch (uploaderType) {
            case SOLICITOR:
            case CAFCASS:
            case BARRISTER:
                return documentType.isHiddenFromSolicitorUpload();
            case DESIGNATED_LOCAL_AUTHORITY:
            case SECONDARY_LOCAL_AUTHORITY:
                return documentType.isHiddenFromLAUpload();
            case HMCTS:
                return documentType.isHiddenFromCTSCUpload();
            default:
                throw new IllegalStateException("unsupported uploaderType: " + uploaderType);
        }
    }

    // Also used in SendNewJudgeMessageService for document attachment type dropdown
    public DynamicList buildDocumentTypeDynamicList(CaseData caseData) {
        boolean hasPlacementNotices = caseData.getPlacementEventData().getPlacements().stream()
            .anyMatch(el -> el.getValue().getPlacementNotice() != null);
        final List<Pair<String, String>> documentTypes = Arrays.stream(DocumentType.values())
            .filter(documentType -> !isHiddenFromUpload(documentType, getUploaderType(caseData)))
            .filter(documentType -> PLACEMENT_RESPONSES == documentType ? hasPlacementNotices : true)
            .sorted(comparing(DocumentType::getDisplayOrder))
            .map(dt -> Pair.of(dt.name(), dt.getDescription()))
            .toList();
        return dynamicListService.asDynamicList(documentTypes);
    }

    @SuppressWarnings("unchecked")
    private List<Element<?>> readFromFieldName(CaseData caseData, String fieldName) {
        String[] splitFieldName = fieldName.split("\\.");
        if (splitFieldName.length == 1) {
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(CaseData.class, fieldName);
                if (pd == null) {
                    throw new IllegalStateException("Fail to find the property descriptor of " + fieldName);
                }
                return (List<Element<?>>) pd.getReadMethod().invoke(caseData);
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException(format("Fail to grep the documents' filename - %s", fieldName), ex);
            }
        } else if (splitFieldName.length == 2 && splitFieldName[0].equals("hearingDocuments")) {
            String actualFieldName = splitFieldName[1];
            List<Element<?>> listOfElement = null;
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(HearingDocuments.class, actualFieldName);
                if (pd == null) {
                    throw new IllegalStateException("Fail to find the property descriptor of " + actualFieldName);
                }
                listOfElement = (List<Element<?>>) pd.getReadMethod().invoke(caseData.getHearingDocuments());
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Fail to grep the documents' from hearingDocuments", ex);
            }
            if (listOfElement != null) {
                if (DocumentType.fromJsonFieldName(actualFieldName) == DocumentType.COURT_BUNDLE) {
                    return new ArrayList<>(listOfElement.stream()
                        .flatMap(loe -> ((Element<HearingCourtBundle>) loe).getValue().getCourtBundle().stream())
                        .toList());
                } else {
                    return listOfElement;
                }
            }
        }
        return new ArrayList<>();
    }

    private C2DocumentBundle getC2DocumentBundleResp(AdditionalApplicationsBundle aab, int index) {
        try {
            return (C2DocumentBundle) AdditionalApplicationsBundle.class
                .getMethod("getC2DocumentBundleResp" + index).invoke(aab);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private C2DocumentBundle getC2DocumentBundleChild(AdditionalApplicationsBundle aab, int index) {
        try {
            return (C2DocumentBundle) AdditionalApplicationsBundle.class
                .getMethod("getC2DocumentBundleChild" + index).invoke(aab);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Element<AdditionalApplicationsBundle> mimicAdditionalApplicationsBundle(
        SubmittedC1WithSupplementBundle submittedC1WithSupplementBundle) {
        return element(AdditionalApplicationsBundle.builder().otherApplicationsBundle(
            OtherApplicationsBundle.builder()
                .supportingEvidenceBundle(submittedC1WithSupplementBundle == null ? List.of()
                    : submittedC1WithSupplementBundle.getSupportingEvidenceBundle())
                .build())
            .build());
    }

    private Map<String, List<Element<?>>> toFieldNameToListOfElementMap(CaseData caseData, DocumentType documentType,
                                                                        ConfidentialLevel level) {
        Map<String, List<Element<?>>> ret = new LinkedHashMap<>();
        if (documentType.getBaseFieldNameResolver() != null) {
            String fieldName = documentType.getBaseFieldNameResolver().apply(level);
            List<Element<?>> listOfElement = readFromFieldName(caseData, fieldName);
            if (listOfElement != null) {
                ret.put(fieldName, listOfElement);
            }
        }
        if (List.of(C1_APPLICATION_DOCUMENTS, C2_APPLICATION_DOCUMENTS).contains(documentType)) {
            boolean isC1DocumentType = C1_APPLICATION_DOCUMENTS.equals(documentType);
            Optional.ofNullable(caseData.getAdditionalApplicationsBundle())
                .orElse(List.of(mimicAdditionalApplicationsBundle(caseData.getSubmittedC1WithSupplement()))).forEach(
                    app -> ret.computeIfAbsent(documentType.name(), key -> new ArrayList<>()).addAll(
                        Optional.ofNullable(
                            isC1DocumentType ? app.getValue().getOtherApplicationsBundle()
                                : app.getValue().getC2DocumentBundle()).orElse(
                            isC1DocumentType ? OtherApplicationsBundle.builder().supportingEvidenceBundle(List.of())
                                .build()
                                : C2DocumentBundle.builder().supportingEvidenceBundle(List.of()).build())
                        .getSupportingEvidenceBundle()));
            if (!isC1DocumentType) {
                if (level == ConfidentialLevel.CTSC) {
                    Optional.ofNullable(caseData.getAdditionalApplicationsBundle()).orElse(List.of()).stream()
                        .filter(a -> a.getValue().getC2DocumentBundleConfidential() != null)
                        .forEach(app -> ret.computeIfAbsent(documentType.name(), key -> new ArrayList<>()).addAll(
                            app.getValue().getC2DocumentBundleConfidential().getSupportingEvidenceBundle()));
                }
                if (level == ConfidentialLevel.LA) {
                    Optional.ofNullable(caseData.getAdditionalApplicationsBundle()).orElse(List.of()).stream()
                        .filter(a -> a.getValue().getC2DocumentBundleLA() != null)
                        .forEach(app -> ret.computeIfAbsent(documentType.name(), key -> new ArrayList<>()).addAll(
                            app.getValue().getC2DocumentBundleLA().getSupportingEvidenceBundle()));
                }
                if (level == ConfidentialLevel.NON_CONFIDENTIAL) {
                    for (int i = 0; i <= 9; i++) {
                        final int index = i;
                        Optional.ofNullable(caseData.getAdditionalApplicationsBundle()).orElse(List.of()).stream()
                            .filter(a -> getC2DocumentBundleResp(a.getValue(), index) != null)
                            .forEach(app -> ret.computeIfAbsent(documentType.name(), key -> new ArrayList<>()).addAll(
                                getC2DocumentBundleResp(app.getValue(), index).getSupportingEvidenceBundle()));
                    }
                    for (int i = 0; i <= 14; i++) {
                        final int index = i;
                        Optional.ofNullable(caseData.getAdditionalApplicationsBundle()).orElse(List.of()).stream()
                            .filter(a -> getC2DocumentBundleChild(a.getValue(), index) != null)
                            .forEach(app -> ret.computeIfAbsent(documentType.name(), key -> new ArrayList<>()).addAll(
                                getC2DocumentBundleChild(app.getValue(), index).getSupportingEvidenceBundle()));
                    }
                }
            }
        }
        return ret;
    }

    private List<Pair<String, String>> toListOfPair(CaseData caseData,
                                                    Map<String, List<Element<?>>> fieldNameToListOfElement) {
        final DocumentUploaderType currentUploaderType = getUploaderType(caseData);
        final List<Pair<String, String>> ret = new ArrayList<>();

        Set<CaseRole> currentUploaderCaseRoles;
        if (currentUploaderType != HMCTS && currentUploaderType != CAFCASS) {
            currentUploaderCaseRoles = Optional
                .ofNullable(userService.getCaseRoles(caseData.getId()))
                .orElse(Set.of());
        } else {
            currentUploaderCaseRoles = Set.of();
        }

        for (Map.Entry<String, List<Element<?>>> entrySet : fieldNameToListOfElement.entrySet()) {
            String fieldName = entrySet.getKey();
            for (Element e : entrySet.getValue()) {
                WithDocument wd = ((WithDocument) e.getValue());
                DocumentReference document = wd.getDocument();

                // checks CaseRoles
                if (currentUploaderType != HMCTS && currentUploaderType != CAFCASS) {
                    List<CaseRole> docCaseRoles = wd.getUploaderCaseRoles() == null
                        ? new ArrayList<>() : wd.getUploaderCaseRoles();

                    if (!docCaseRoles.stream().filter(cr -> currentUploaderCaseRoles.contains(cr)).findAny()
                        .isPresent()) {
                        continue;
                    }
                }

                // Check currentUploaderType if it is CAFCASS
                if (currentUploaderType == CAFCASS && !CAFCASS.equals(wd.getUploaderType())) {
                    continue;
                }
                ret.add(Pair.of(fieldName + DOCUMENT_TO_BE_REMOVED_SEPARATOR + e.getId(), document.getFilename()));
            }
        }
        return ret;
    }

    public DynamicList buildAvailableDocumentsDynamicList(CaseData caseData) {
        return buildAvailableDocumentsDynamicList(caseData, null);
    }

    // Return all documents when documentType is null
    public DynamicList buildAvailableDocumentsDynamicList(CaseData caseData, DocumentType documentType) {
        DocumentUploaderType currentUserType = getUploaderType(caseData);

        Map<String, List<Element<?>>> fieldNameToListOfElementMap = new LinkedHashMap<>();
        for (DocumentType dt : documentType != null ? List.of(documentType) : Arrays.stream(DocumentType.values())
            .filter(DocumentType::isUploadable)
            .sorted(Comparator.comparing(DocumentType::getDisplayOrder))
            .toList()) {
            if (dt == PLACEMENT_RESPONSES) {
                List<Element<Placement>> placements = caseData.getPlacementEventData().getPlacements();

                fieldNameToListOfElementMap.put(PLACEMENT_RESPONSES.name(), placements.stream()
                    .flatMap(pe -> pe.getValue().getNoticeDocuments().stream())
                    .collect(toList()));
            } else {
                for (ConfidentialLevel level : Arrays.stream(ConfidentialLevel.values()).filter(level -> {
                    switch (level) {
                        case LA:
                            return currentUserType != DocumentUploaderType.SOLICITOR;
                        case CTSC:
                            return currentUserType == DocumentUploaderType.HMCTS;
                        case NON_CONFIDENTIAL:
                            return true;
                        default:
                            return false;
                    }
                }).toList()) {
                    fieldNameToListOfElementMap.putAll(toFieldNameToListOfElementMap(caseData, dt, level));
                }
            }
        }
        return dynamicListService.asDynamicList(toListOfPair(caseData, fieldNameToListOfElementMap));
    }

    private List<Element<AdditionalApplicationsBundle>> getC1Applications(CaseData caseData) {
        return Optional.ofNullable(caseData.getAdditionalApplicationsBundle())
            .orElse(List.of(mimicAdditionalApplicationsBundle(caseData.getSubmittedC1WithSupplement())));
    }

    private List<Element<AdditionalApplicationsBundle>> getC2Applications(CaseData caseData) {
        return Optional.ofNullable(caseData.getAdditionalApplicationsBundle()).orElse(List.of());
    }

    // For HMCTS admin's journey
    public DynamicList buildExistingDocumentTypeDynamicList(CaseData caseData) {
        Map<String, Object> map = caseConverter.toMap(caseData);

        Set<DocumentType> availableDocumentTypes = Arrays.stream(DocumentType.values())
            .map(DocumentType::getJsonFieldNames)
            .flatMap(List::stream)
            .filter(name -> map.containsKey(name))
            .filter(name -> !(Optional.ofNullable((List) map.get(name))).orElse(List.of()).isEmpty())
            .map(name -> DocumentType.fromJsonFieldName(name))
            .collect(toSet());

        Set<DocumentType> finalDocumentTypes = new HashSet<>(availableDocumentTypes);
        // add parent folders
        finalDocumentTypes.addAll(availableDocumentTypes.stream().map(d -> d.getParentFolder())
            .filter(Objects::nonNull)
            .toList());

        // placement response
        List<Element<Placement>> placements = caseData.getPlacementEventData().getPlacements();
        if (placements.stream().flatMap(pe -> pe.getValue().getNoticeDocuments().stream()).findAny().isPresent()) {
            finalDocumentTypes.add(PLACEMENT_RESPONSES);
        }

        // C1 application documents
        if (!getC1Applications(caseData).isEmpty()) {
            if (!getC1Applications(caseData).stream()
                .flatMap(a -> Optional.ofNullable(a.getValue().getOtherApplicationsBundle())
                    .orElse(OtherApplicationsBundle.builder().supportingEvidenceBundle(List.of()).build())
                    .getSupportingEvidenceBundle().stream())
                .toList().isEmpty()) {
                finalDocumentTypes.add(AA_PARENT_APPLICATIONS);
                finalDocumentTypes.add(C1_APPLICATION_DOCUMENTS);
            }
        }
        // C2 application documents
        if (!getC2Applications(caseData).isEmpty()) {
            if (!getC2Applications(caseData).stream()
                .flatMap(a -> Optional.ofNullable(a.getValue().getC2DocumentBundle())
                    .orElse(Optional.ofNullable(a.getValue().getC2DocumentBundleConfidential())
                        .orElse(C2DocumentBundle.builder().supportingEvidenceBundle(List.of()).build()))
                    .getSupportingEvidenceBundle().stream())
                .toList().isEmpty()) {
                finalDocumentTypes.add(AA_PARENT_APPLICATIONS);
                finalDocumentTypes.add(C2_APPLICATION_DOCUMENTS);
            }
        }

        final List<Pair<String, String>> documentTypes = finalDocumentTypes.stream()
            .sorted(comparing(DocumentType::getDisplayOrder))
            .map(dt -> Pair.of(dt.name(), dt.getDescription()))
            .toList();
        return dynamicListService.asDynamicList(documentTypes);
    }

    public Map<String, Object> initialisePlacementHearingResponseFields(CaseData caseData,
                                                                        PlacementNoticeDocument.RecipientType type) {
        Map<String, Object> map = new HashMap<>();
        PlacementEventData data = placementService.preparePlacementFromExisting(caseData);
        map.put("placement", data.getPlacement());
        map.put("placementNoticeResponses", data.getPlacement().getNoticeDocuments().stream().filter(
            doc -> type == null ? true : (doc.getValue().getType() == type)
        ).collect(toList()));
        return map;
    }

    public Map<String, Object> initialisePlacementHearingResponseFields(CaseData caseData) {
        return initialisePlacementHearingResponseFields(caseData, null);
    }

    public PlacementEventData updatePlacementNoticesLA(CaseData caseData) {
        return placementService.savePlacementNoticeResponses(
            caseData, PlacementNoticeDocument.RecipientType.LOCAL_AUTHORITY);
    }

    public PlacementEventData updatePlacementNoticesSolicitor(CaseData caseData) {
        return placementService.savePlacementNoticeResponses(
            caseData, PlacementNoticeDocument.RecipientType.RESPONDENT);
    }

    public PlacementEventData updatePlacementNoticesAdmin(CaseData caseData) {
        return placementService.savePlacementNoticeResponsesAdmin(caseData);
    }

    @SuppressWarnings("unchecked")
    public ManageDocumentsUploadedEvent buildManageDocumentsUploadedEvent(CaseData caseData, CaseData caseDataBefore)
        throws Exception {
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocuments = new HashMap<>();
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsLA = new HashMap<>();
        Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocumentsCTSC = new HashMap<>();

        Map<ConfidentialLevel, Map<DocumentType, List<Element<NotifyDocumentUploaded>>>> resultMapByConfidentialLevel =
            Map.of(ConfidentialLevel.NON_CONFIDENTIAL, newDocuments,
                ConfidentialLevel.LA, newDocumentsLA,
                ConfidentialLevel.CTSC, newDocumentsCTSC);

        for (DocumentType documentType : DocumentType.values()) {
            for (ConfidentialLevel confidentialLevel : resultMapByConfidentialLevel.keySet()) {
                if (documentType.getBaseFieldNameResolver() != null) {
                    String fieldName = documentType.getBaseFieldNameResolver().apply(confidentialLevel);

                    Map<DocumentType, List<Element<NotifyDocumentUploaded>>> newDocMap =
                            resultMapByConfidentialLevel.get(confidentialLevel);

                    List documentList = Optional.ofNullable(ObjectHelper
                        .getFieldValue(caseData, fieldName, List.class)).orElse(List.of());
                    List documentListBefore = Optional.ofNullable(ObjectHelper
                        .getFieldValue(caseDataBefore, fieldName, List.class)).orElse(List.of());

                    if (DocumentType.COURT_BUNDLE.equals(documentType)) {
                        documentList = ((List<Element<HearingCourtBundle>>) documentList).stream()
                            .map(Element::getValue)
                            .map(HearingCourtBundle::getCourtBundle)
                            .flatMap(List::stream)
                            .toList();

                        documentListBefore = ((List<Element<HearingCourtBundle>>) documentListBefore).stream()
                            .map(Element::getValue)
                            .map(HearingCourtBundle::getCourtBundle)
                            .flatMap(List::stream)
                            .toList();
                    }

                    for (Object document : documentList) {
                        if (!documentListBefore.contains(document)) {
                            List<Element<NotifyDocumentUploaded>> docList =
                                Optional.ofNullable(newDocMap.get(documentType))
                                    .orElse(new ArrayList<>());
                            newDocMap.putIfAbsent(documentType, docList);

                            docList.add((Element<NotifyDocumentUploaded>) document);
                        }
                    }
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info("New non-confidential document found: {}", newDocuments.entrySet().stream()
                .map(entry -> entry.getKey().toString() + " " + entry.getValue().size())
                .collect(Collectors.joining(", ")));
            log.info("New confidential document found: {}", newDocumentsLA.entrySet().stream()
                .map(entry -> entry.getKey().toString() + " " + entry.getValue().size())
                .collect(Collectors.joining(", ")));
            log.info("New confidential (CTSC) document found: {}", newDocumentsCTSC.entrySet().stream()
                .map(entry -> entry.getKey().toString() + " " + entry.getValue().size())
                .collect(Collectors.joining(", ")));
        }

        return ManageDocumentsUploadedEvent.builder()
            .caseData(caseData)
            .initiatedBy(userService.getUserDetails())
            .uploadedUserType(getUploaderType(caseData))
            .newDocuments(newDocuments)
            .newDocumentsLA(newDocumentsLA)
            .newDocumentsCTSC(newDocumentsCTSC)
            .build();
    }
}
