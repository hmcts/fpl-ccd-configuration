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
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.model.cfv.UploadBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.ManageDocumentEventData;
import uk.gov.hmcts.reform.fpl.model.event.PlacementEventData;
import uk.gov.hmcts.reform.fpl.model.event.UploadableDocumentBundle;
import uk.gov.hmcts.reform.fpl.model.interfaces.NotifyDocumentUploaded;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithDocument;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;
import uk.gov.hmcts.reform.fpl.service.PlacementService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.utils.ObjectHelper;

import java.beans.PropertyDescriptor;
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
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.designatedLASolicitors;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.representativeSolicitors;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
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

    private static final String DOCUMENT_TO_BE_REMOVED_SEPARATOR = "###";

    @Autowired
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> removeDocuments(CaseData caseData) {
        ManageDocumentEventData eventData = caseData.getManageDocumentEventData();

        String removalReason;
        if (ManageDocumentRemovalReason.OTHER == eventData.getManageDocumentRemoveDocReason()) {
            removalReason = eventData.getManageDocumentRemoveDocAnotherReason();
        } else {
            removalReason = eventData.getManageDocumentRemoveDocReason().getDescription();
        }

        DynamicListElement selected = eventData.getDocumentsToBeRemoved().getValue();

        String[] split = selected.getCode().split(DOCUMENT_TO_BE_REMOVED_SEPARATOR);
        String fieldName = split[0];
        DocumentType documentType = DocumentType.fromFieldName(fieldName);
        UUID documentElementId = UUID.fromString(split[1]);

        final Map<String, Object> ret = new HashMap<>();
        if (documentType == PLACEMENT_RESPONSES) {
            Element<Placement> placement = caseData.getPlacementEventData().getPlacements().stream()
                .filter(placementElement -> placementElement.getValue().getNoticeDocuments().stream()
                    .anyMatch(nd -> documentElementId.equals(nd.getId())))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Fail to locate placement"));

            Element<PlacementNoticeDocument> target = placement.getValue().getNoticeDocuments().stream()
                .filter(nd -> documentElementId.equals(nd.getId()))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Fail to locate notice documents"));
            target.getValue().setRemovalReason(removalReason);

            placement.getValue().getNoticeDocuments().remove(target);

            List<Element<PlacementNoticeDocument>> noticeDocumentsRemoved = placement.getValue()
                .getNoticeDocumentsRemoved() == null ? new ArrayList<>() : placement.getValue()
                .getNoticeDocumentsRemoved();
            noticeDocumentsRemoved.add(target);
            placement.getValue().setNoticeDocumentsRemoved(noticeDocumentsRemoved);

            ret.put("placements", caseData.getPlacementEventData().getPlacements());
            ret.put("placementsNonConfidential", caseData.getPlacementEventData()
                .getPlacementsNonConfidential(true));
            ret.put("placementsNonConfidentialNotices", caseData.getPlacementEventData()
                .getPlacementsNonConfidential(true));
        } else {
            // getting list of removed element
            List<Element> listOfRemovedElement =
                this.readFromFieldName(caseData, documentType.getFieldNameOfRemovedList());
            if (listOfRemovedElement == null) {
                listOfRemovedElement = new ArrayList<>();
            }

            List<Element> listOfElement = this.readFromFieldName(caseData, fieldName);

            if (documentType == COURT_BUNDLE) {
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

                Element<CourtBundle> target = hcbElement.getValue().getCourtBundle().stream()
                    .filter(i -> documentElementId.equals(i.getId())).findFirst().orElseThrow(() -> {
                        throw new IllegalStateException("Fail to locate the target document");
                    });

                if (hcbElement.getValue().getCourtBundle().size() == 1) {
                    listOfElement.remove(hcbElement);
                }
                hcbElement.getValue().getCourtBundle().remove(target);

                final boolean isNewHearingCourtBundleInRemovedList = !listOfRemovedElement.stream()
                    .anyMatch(e -> e.getId().equals(hcbElement.getId()));
                Element<HearingCourtBundle> hcbFromRemovedList = listOfRemovedElement.stream()
                    .filter(e -> e.getId().equals(hcbElement.getId())).findFirst()
                    .orElse(element(hcbElement.getId(), hcbElement.getValue().toBuilder()
                        .courtBundle(new ArrayList<>())
                        .build()));
                target.getValue().setRemovalReason(removalReason); // Setting the removal reason
                hcbFromRemovedList.getValue().getCourtBundle().add(target);
                if (isNewHearingCourtBundleInRemovedList) {
                    listOfRemovedElement.add(hcbFromRemovedList);
                }
            } else {
                Element target = listOfElement.stream().filter(i -> documentElementId.equals(i.getId())).findFirst()
                    .orElseThrow(() -> {
                        throw new IllegalStateException("Fail to locate the target document");
                    });

                listOfElement.remove(target);
                ((WithDocument) target.getValue()).setRemovalReason(removalReason); // Setting the removal reason
                listOfRemovedElement.add(target); // Putting it to removed list for backing up
            }

            ret.put(DocumentType.toJsonFieldName(fieldName), listOfElement);
            ret.put(DocumentType.toJsonFieldName(documentType.getFieldNameOfRemovedList()), listOfRemovedElement);
        }
        return ret;
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
    private List<Element> readFromFieldName(CaseData caseData, String fieldName) {
        String[] splitFieldName = fieldName.split("\\.");
        if (splitFieldName.length == 1) {
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(CaseData.class, fieldName);
                if (pd == null) {
                    throw new IllegalStateException("Fail to find the property descriptor of " + fieldName);
                }
                return (List<Element>) pd.getReadMethod().invoke(caseData);
            } catch (IllegalStateException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException(format("Fail to grep the documents' filename - %s", fieldName), ex);
            }
        } else if (splitFieldName.length == 2 && splitFieldName[0].equals("hearingDocuments")) {
            String actualFieldName = splitFieldName[1];
            List<Element> listOfElement = null;
            try {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(HearingDocuments.class, actualFieldName);
                if (pd == null) {
                    throw new IllegalStateException("Fail to find the property descriptor of " + actualFieldName);
                }
                listOfElement = (List<Element>) pd.getReadMethod().invoke(caseData.getHearingDocuments());
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

    private Map<String, List<Element>> toFieldNameToListOfElementMap(CaseData caseData, DocumentType documentType,
                                                                     ConfidentialLevel level) {
        Map<String, List<Element>> ret = new LinkedHashMap<String, List<Element>>();
        if (documentType.getBaseFieldNameResolver() != null) {
            String fieldName = documentType.getBaseFieldNameResolver().apply(level);
            List<Element> listOfElement = readFromFieldName(caseData, fieldName);
            if (listOfElement != null) {
                ret.put(fieldName, listOfElement);
            }
        }
        return ret;
    }

    private List<Pair<String, String>> toListOfPair(CaseData caseData,
                                                    Map<String, List<Element>> fieldNameToListOfElement) {
        final DocumentUploaderType uploaderType = getUploaderType(caseData);
        final List<Pair<String, String>> ret = new ArrayList<>();
        for (Map.Entry<String, List<Element>> entrySet : fieldNameToListOfElement.entrySet()) {
            String fieldName = entrySet.getKey();
            for (Element e : entrySet.getValue()) {
                WithDocument wd = ((WithDocument) e.getValue());
                DocumentReference document = wd.getDocument();

                if (uploaderType != HMCTS && uploaderType != CAFCASS) {
                    List<CaseRole> docCaseRoles = wd.getUploaderCaseRoles() == null
                        ? new ArrayList<>() : wd.getUploaderCaseRoles();
                    final Set<CaseRole> currentUploaderCaseRoles = Optional
                        .ofNullable(userService.getCaseRoles(caseData.getId()))
                        .orElse(Set.of());

                    if (!docCaseRoles.stream().filter(cr -> currentUploaderCaseRoles.contains(cr)).findAny()
                        .isPresent()) {
                        continue;
                    }
                }
                if (uploaderType == CAFCASS) {
                    if (!uploaderType.equals(wd.getUploaderType())) {
                        continue;
                    }
                }
                ret.add(Pair.of(fieldName + DOCUMENT_TO_BE_REMOVED_SEPARATOR + e.getId(), document.getFilename()));
            }
        }
        return ret;
    }

    public DynamicList buildAvailableDocumentsToBeRemoved(CaseData caseData) {
        return buildAvailableDocumentsToBeRemoved(caseData, null);
    }

    public DynamicList buildAvailableDocumentsToBeRemoved(CaseData caseData, DocumentType documentType) {
        DocumentUploaderType currentUserType = getUploaderType(caseData);

        Map<String, List<Element>> fieldNameToListOfElementMap = new LinkedHashMap<>();
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

    public DynamicList buildDocumentTypeDynamicListForRemoval(CaseData caseData) {
        Map<String, Object> map = caseConverter.toMap(caseData);

        Set<DocumentType> availableDocumentTypes = Arrays.stream(DocumentType.values())
            .map(DocumentType::getJsonFieldNames)
            .flatMap(List::stream)
            .filter(name -> map.containsKey(name))
            .filter(name -> !(Optional.ofNullable((List) map.get(name))).orElse(List.of()).isEmpty())
            .map(name -> DocumentType.fromJsonFieldName(name))
            .collect(toSet());

        Set<DocumentType> finalDocumentTypes = new HashSet<>(availableDocumentTypes);
        finalDocumentTypes.addAll(availableDocumentTypes.stream().map(d -> d.getParentFolder())
            .filter(Objects::nonNull)
            .toList());

        List<Element<Placement>> placements = caseData.getPlacementEventData().getPlacements();
        if (placements.stream().flatMap(pe -> pe.getValue().getNoticeDocuments().stream()).findAny().isPresent()) {
            finalDocumentTypes.add(PLACEMENT_RESPONSES);
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
