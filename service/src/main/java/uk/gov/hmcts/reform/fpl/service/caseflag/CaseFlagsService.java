package uk.gov.hmcts.reform.fpl.service.caseflag;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.model.caseflag.AllPartyFlags;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlagsType;
import uk.gov.hmcts.reform.fpl.model.caseflag.FlagDetailType;
import uk.gov.hmcts.reform.fpl.model.caseflag.GenericTypeItem;
import uk.gov.hmcts.reform.fpl.model.caseflag.ListTypeItem;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.YES;

@Slf4j
@Service
public class CaseFlagsService {
    public static final String INTERNAL      = "Internal";
    public static final String EXTERNAL      = "External";
    public static final String APPLICANT     = "Applicant";
    public static final String APPLICANT_REPRESENTATIVE = "Applicant Representative";
    public static final String RESPONDENT    = "Respondent";
    public static final String RESPONDENT1   = "Respondent 1";
    public static final String RESPONDENT2   = "Respondent 2";
    public static final String RESPONDENT3   = "Respondent 3";
    public static final String RESPONDENT4   = "Respondent 4";
    public static final String RESPONDENT5   = "Respondent 5";
    public static final String RESPONDENT6   = "Respondent 6";
    public static final String RESPONDENT7   = "Respondent 7";
    public static final String RESPONDENT8   = "Respondent 8";
    public static final String RESPONDENT9   = "Respondent 9";
    public static final String RESPONDENT10  = "Respondent 10";
    public static final String REPRESENTATIVE = "Representative";
    public static final String REPRESENTATIVE1 = "Representative 1";
    public static final String REPRESENTATIVE2 = "Representative 2";
    public static final String REPRESENTATIVE3 = "Representative 3";
    public static final String REPRESENTATIVE4 = "Representative 4";
    public static final String REPRESENTATIVE5 = "Representative 5";
    public static final String REPRESENTATIVE6 = "Representative 6";
    public static final String REPRESENTATIVE7 = "Representative 7";
    public static final String REPRESENTATIVE8 = "Representative 8";
    public static final String REPRESENTATIVE9 = "Representative 9";
    public static final String REPRESENTATIVE10 = "Representative 10";
    private static final String FLAGS_SUFFIX = "Flags";
    private static final String EXTERNAL_FLAGS_SUFFIX = "ExternalFlags";
    private static final String CASE_FLAGS_FIELD = "caseFlags";
    private static final String APPLICANT_FLAGS_FIELD = "applicantFlags";
    private static final String APPLICANT_EXTERNAL_FLAGS_FIELD = "applicantExternalFlags";
    private static final String APPLICANT_REPRESENTATIVE_FLAGS_FIELD = "applicantRepresentativeFlags";
    private static final String APPLICANT_REPRESENTATIVE_EXTERNAL_FLAGS_FIELD = "applicantRepresentativeExternalFlags";

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        if (allPartyFlags == null) {
            return true;
        }

        int respondentCount = caseData.getAllRespondents().size();

        return allPartyFlags.getCaseFlags() == null
            || flagSetupRequired(allPartyFlags.getApplicantFlags())
            || flagSetupRequired(allPartyFlags.getApplicantExternalFlags())
            || applicantRepresentativeFlagSetupRequired(caseData, allPartyFlags)
            || partyFlagSetupRequired(respondentCount, respondentFlagFields(allPartyFlags))
            || representativeFlagSetupRequired(caseData, representativeFlagFields(allPartyFlags));
    }

    /**
     * Setup case flags for Applicant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        AllPartyFlags allPartyFlags = allPartyFlags(caseData);

        if (allPartyFlags.getCaseFlags() == null) {
            allPartyFlags.setCaseFlags(CaseFlagsType.builder().build());
        }

        if (!caseData.getLocalAuthorities().isEmpty()) {
            setupPartyCaseFlags(caseData.getLocalAuthorities().getFirst().getValue().getName(),
                APPLICANT, APPLICANT,
                allPartyFlags::getApplicantFlags, allPartyFlags::setApplicantFlags,
                allPartyFlags::getApplicantExternalFlags, allPartyFlags::setApplicantExternalFlags);
        }

        applicantRepresentativeName(caseData).ifPresent(representativeName -> setupPartyCaseFlags(representativeName,
                APPLICANT_REPRESENTATIVE, APPLICANT_REPRESENTATIVE,
                allPartyFlags::getApplicantRepresentativeFlags, allPartyFlags::setApplicantRepresentativeFlags,
                allPartyFlags::getApplicantRepresentativeExternalFlags,
                allPartyFlags::setApplicantRepresentativeExternalFlags));

        List<PartyFlagFields> respondentFlagFields = respondentFlagFields(allPartyFlags);
        int respondentFlagsToSetup = Math.min(caseData.getAllRespondents().size(), respondentFlagFields.size());

        for (int i = 0; i < respondentFlagsToSetup; i++) {
            respondentFlagFields.get(i).setup(respondentName(caseData, i), RESPONDENT + SPACE + (i + 1));
        }

        List<PartyFlagFields> representativeFlagFields = representativeFlagFields(allPartyFlags);
        List<ExistingRepresentativeFlag> existingRepresentativeFlags =
            existingRepresentativeFlags(representativeFlagFields);
        int representativeFlagsToSetup = representativeCount(caseData);

        for (int i = 0; i < representativeFlagsToSetup; i++) {
            int flagIndex = representativeFlagIndex(caseData, i);
            if (hasUniqueRepresentativeAtIndex(caseData, i) && flagIndex < representativeFlagFields.size()) {
                PartyFlagFields fields = representativeFlagFields.get(flagIndex);
                fields.setup(representativeName(caseData, i), fields.groupId());
            }
        }

        alignRepresentativeFlagDetails(caseData, representativeFlagFields, existingRepresentativeFlags);
        clearDuplicateRepresentativeFlags(caseData, representativeFlagFields);
    }

    /**
     * Clears flags for representatives whose case identity was removed or changed, unless the same representative
     * still represents another respondent.
     */
    public void removeObsoleteRepresentativeFlags(CaseData caseData, CaseData caseDataBefore) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        if (allPartyFlags == null || caseDataBefore.getRepresentatives() == null) {
            return;
        }

        for (int i = 0; i < caseDataBefore.getRepresentatives().size(); i++) {
            Element<Representative> representativeBefore = caseDataBefore.getRepresentatives().get(i);
            Representative oldRepresentative = representativeBefore.getValue();
            Element<Representative> currentRepresentative = findRepresentative(
                caseData.getRepresentatives(), representativeBefore.getId()
            );

            boolean removedOrChanged = currentRepresentative == null
                || !sameRepresentativeIdentity(oldRepresentative, currentRepresentative.getValue());
            Element<Representative> representativeForAnotherRespondent = caseData.getRepresentatives() == null ? null
                : caseData.getRepresentatives().stream()
                .filter(representative -> !representative.getId().equals(representativeBefore.getId()))
                .filter(representative -> sameEmail(oldRepresentative, representative.getValue()))
                .findFirst()
                .orElse(null);

            if (removedOrChanged) {
                if (representativeForAnotherRespondent == null) {
                    representativeFlagFields(allPartyFlags).stream()
                        .filter(fields -> fields.hasPartyName(oldRepresentative.getFullName()))
                        .forEach(PartyFlagFields::clear);
                } else {
                    moveRepresentativeFlagsToRemainingRespondent(caseDataBefore, i, caseData,
                        representativeForAnotherRespondent, allPartyFlags);
                }
            }
        }
    }

    /** Clears applicant-representative flags if the associated legal representative is removed or changed. */
    public void removeObsoleteApplicantRepresentativeFlags(CaseData caseData, CaseData caseDataBefore) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        if (allPartyFlags == null || (allPartyFlags.getApplicantRepresentativeFlags() == null
            && allPartyFlags.getApplicantRepresentativeExternalFlags() == null)) {
            return;
        }

        CaseFlagsType flags = allPartyFlags.getApplicantRepresentativeFlags() == null
            ? allPartyFlags.getApplicantRepresentativeExternalFlags()
            : allPartyFlags.getApplicantRepresentativeFlags();
        boolean representativeChanged = caseDataBefore.getLegalRepresentatives() != null
            && caseDataBefore.getLegalRepresentatives().stream()
            .filter(representative -> Objects.equals(flags.getPartyName(), representative.getValue().getFullName()))
            .anyMatch(representativeBefore -> {
                Element<uk.gov.hmcts.reform.fpl.model.LegalRepresentative> representative =
                    findLegalRepresentative(caseData.getLegalRepresentatives(), representativeBefore.getId());
                return (representative == null
                    || !Objects.equals(representativeBefore.getValue(), representative.getValue()));
            });
        boolean representativeStillPresent = applicantRepresentativeName(caseData)
            .filter(representativeName -> Objects.equals(flags.getPartyName(), representativeName))
            .isPresent();

        if (representativeChanged || !representativeStillPresent) {
            allPartyFlags.setApplicantRepresentativeFlags(null);
            allPartyFlags.setApplicantRepresentativeExternalFlags(null);
        }
    }

    /**
     * Sets additional flags on CaseData dependent on CaseFlags raised.
     * @param caseData Data about the current case.
     */
    public void processNewlySetCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = getPartyCaseFlags(caseData);
        caseData.setCaseInterpreterRequiredFlag(
            areAnyFlagsActive(partyLevel, SIGN_LANGUAGE_INTERPRETER, LANGUAGE_INTERPRETER) ? YES : NO
        );

        caseData.setCaseAdditionalSecurityFlag(
            areAnyFlagsActive(partyLevel, VEXATIOUS_LITIGANT, DISRUPTIVE_CUSTOMER) ? YES : NO
        );
    }

    public Map<String, Object> generate(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();
        if (caseData == null) {
            return data;
        }

        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();

        if (allPartyFlags == null) {
            return data;
        }

        putIfPresent(data, CASE_FLAGS_FIELD, allPartyFlags.getCaseFlags());
        putIfPresent(data, APPLICANT_FLAGS_FIELD, allPartyFlags.getApplicantFlags());
        putIfPresent(data, APPLICANT_EXTERNAL_FLAGS_FIELD, allPartyFlags.getApplicantExternalFlags());
        putIfPresent(data, APPLICANT_REPRESENTATIVE_FLAGS_FIELD, allPartyFlags.getApplicantRepresentativeFlags());
        putIfPresent(data, APPLICANT_REPRESENTATIVE_EXTERNAL_FLAGS_FIELD,
            allPartyFlags.getApplicantRepresentativeExternalFlags());
        respondentFlagFields(allPartyFlags).forEach(fields -> fields.putIfPresent(data));
        representativeFlagFields(allPartyFlags).forEach(fields -> fields.putIfPresent(data));

        return data;
    }

    /** Initialises all Case Flags v2.1 sections required by the parties currently on the case. */
    public void migrateCaseFlags(CaseData caseData) {
        if (caseData == null) {
            return;
        }

        if (caseFlagsSetupRequired(caseData)) {
            setupCaseFlags(caseData);
        }
        processNewlySetCaseFlags(caseData);
    }

    /** Removes all Case Flags v2.1 data from a case. */
    public void rollbackCaseFlags(CaseData caseData) {
        if (caseData == null) {
            return;
        }

        caseData.setAllPartyFlags(null);
        caseData.setCaseInterpreterRequiredFlag(null);
        caseData.setCaseAdditionalSecurityFlag(null);
    }

    /** Generates explicit null updates so CCD removes all Case Flags v2.1 fields during rollback. */
    public Map<String, Object> generateRollback() {
        Map<String, Object> data = new HashMap<>();
        data.put(CASE_FLAGS_FIELD, null);
        data.put(APPLICANT_FLAGS_FIELD, null);
        data.put(APPLICANT_EXTERNAL_FLAGS_FIELD, null);
        data.put(APPLICANT_REPRESENTATIVE_FLAGS_FIELD, null);
        data.put(APPLICANT_REPRESENTATIVE_EXTERNAL_FLAGS_FIELD, null);

        for (int i = 1; i <= 10; i++) {
            data.put("respondent" + i + FLAGS_SUFFIX, null);
            data.put("respondent" + i + EXTERNAL_FLAGS_SUFFIX, null);
            data.put("representative" + i + FLAGS_SUFFIX, null);
            data.put("representative" + i + EXTERNAL_FLAGS_SUFFIX, null);
        }

        data.put("caseInterpreterRequiredFlag", null);
        data.put("caseAdditionalSecurityFlag", null);
        return data;
    }

    private static boolean partyFlagSetupRequired(int partyCount, List<PartyFlagFields> partyFlags) {
        return partyFlags.stream()
            .limit(partyCount)
            .anyMatch(PartyFlagFields::setupRequired);
    }

    private static boolean representativeFlagSetupRequired(CaseData caseData,
                                                            List<PartyFlagFields> representativeFlags) {
        Set<Integer> populatedFlagIndexes = new HashSet<>();

        for (int i = 0; i < representativeCount(caseData); i++) {
            int flagIndex = representativeFlagIndex(caseData, i);
            if (flagIndex >= representativeFlags.size()) {
                continue;
            }

            if (hasUniqueRepresentativeAtIndex(caseData, i)) {
                PartyFlagFields fields = representativeFlags.get(flagIndex);
                if (fields.setupRequired() || !fields.hasPartyName(representativeName(caseData, i))) {
                    return true;
                }
                populatedFlagIndexes.add(flagIndex);
            }
        }

        for (int i = 0; i < representativeFlags.size(); i++) {
            if (!populatedFlagIndexes.contains(i) && !representativeFlags.get(i).isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private static boolean flagSetupRequired(CaseFlagsType flags) {
        return flags == null || StringUtils.isEmpty(flags.getRoleOnCase());
    }

    private static boolean applicantRepresentativeFlagSetupRequired(CaseData caseData, AllPartyFlags allPartyFlags) {
        return hasLegalRepresentatives(caseData)
            && (flagSetupRequired(allPartyFlags.getApplicantRepresentativeFlags())
            || flagSetupRequired(allPartyFlags.getApplicantRepresentativeExternalFlags()));
    }

    private static boolean hasLegalRepresentatives(CaseData caseData) {
        return applicantRepresentativeName(caseData).isPresent();
    }

    private static java.util.Optional<String> applicantRepresentativeName(CaseData caseData) {
        if (caseData.getLegalRepresentatives() == null) {
            return java.util.Optional.empty();
        }

        return caseData.getLegalRepresentatives().stream()
            .map(Element::getValue)
            .map(LegalRepresentative::getFullName)
            .filter(StringUtils::isNotBlank)
            .findFirst();
    }

    private static String respondentName(CaseData caseData, int index) {
        return caseData.getAllRespondents().get(index).getValue().getParty().getFirstName()
            + SPACE + caseData.getAllRespondents().get(index).getValue().getParty().getLastName();
    }

    private static String representativeName(CaseData caseData, int index) {
        return caseData.getRepresentatives().get(index).getValue().getFullName();
    }

    private static int representativeCount(CaseData caseData) {
        return caseData.getRepresentatives() == null ? 0 : caseData.getRepresentatives().size();
    }

    private static int representativeFlagIndex(CaseData caseData, int representativeIndex) {
        Representative representative = caseData.getRepresentatives().get(representativeIndex).getValue();
        RepresentativeRole role = representative.getRole();

        if (role != null && role.getType() == RepresentativeRole.Type.RESPONDENT
            && role.getSequenceNo() != null) {
            return role.getSequenceNo();
        }

        return representativeIndex;
    }

    private static boolean hasUniqueRepresentativeAtIndex(CaseData caseData, int index) {
        return representativeCount(caseData) > index && !isDuplicateRepresentativeAtIndex(caseData, index);
    }

    private static boolean isDuplicateRepresentativeAtIndex(CaseData caseData, int index) {
        String identity = representativeIdentity(caseData.getRepresentatives().get(index).getValue());
        if (identity == null) {
            return false;
        }

        for (int i = 0; i < index; i++) {
            if (Objects.equals(identity, representativeIdentity(caseData.getRepresentatives().get(i).getValue()))) {
                return true;
            }
        }
        return false;
    }

    private static String representativeIdentity(Representative representative) {
        if (representative == null) {
            return null;
        }

        String identity = StringUtils.defaultIfBlank(representative.getEmail(), representative.getFullName());
        return StringUtils.isBlank(identity) ? null : identity.trim().toLowerCase(Locale.ROOT);
    }

    private static Element<Representative> findRepresentative(List<Element<Representative>> representatives,
                                                               java.util.UUID id) {
        if (representatives == null) {
            return null;
        }

        return representatives.stream()
            .filter(representative -> representative.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private static Element<uk.gov.hmcts.reform.fpl.model.LegalRepresentative> findLegalRepresentative(
        List<Element<uk.gov.hmcts.reform.fpl.model.LegalRepresentative>> representatives, java.util.UUID id) {
        if (representatives == null) {
            return null;
        }

        return representatives.stream()
            .filter(representative -> representative.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    private static boolean sameRepresentativeIdentity(Representative first, Representative second) {
        return Objects.equals(first.getFullName(), second.getFullName())
            && Objects.equals(first.getEmail(), second.getEmail());
    }

    private static boolean sameEmail(Representative first, Representative second) {
        return StringUtils.isNotBlank(first.getEmail())
            && Strings.CS.equals(first.getEmail(), second.getEmail());
    }

    private static void moveRepresentativeFlagsToRemainingRespondent(CaseData caseDataBefore,
                                                                       int previousRepresentativeIndex,
                                                                       CaseData caseData,
                                                                       Element<Representative> remainingRepresentative,
                                                                       AllPartyFlags allPartyFlags) {
        int previousFlagIndex = representativeFlagIndex(caseDataBefore, previousRepresentativeIndex);
        int remainingRepresentativeIndex = caseData.getRepresentatives().indexOf(remainingRepresentative);
        int remainingFlagIndex = representativeFlagIndex(caseData, remainingRepresentativeIndex);
        List<PartyFlagFields> fields = representativeFlagFields(allPartyFlags);

        if (previousFlagIndex >= fields.size() || remainingFlagIndex >= fields.size()
            || previousFlagIndex == remainingFlagIndex) {
            return;
        }

        PartyFlagFields previousFields = fields.get(previousFlagIndex);
        PartyFlagFields remainingFields = fields.get(remainingFlagIndex);
        CaseFlagsType previousFlags = previousFields.flags().get();
        CaseFlagsType previousExternalFlags = previousFields.externalFlags().get();

        if (previousFlags != null || previousExternalFlags != null) {
            remainingFields.setup(remainingRepresentative.getValue().getFullName(), remainingFields.groupId());
            remainingFields.appendFlagDetails(previousFlags, previousExternalFlags);
            previousFields.clear();
        }
    }

    private static List<ExistingRepresentativeFlag> existingRepresentativeFlags(
        List<PartyFlagFields> representativeFlags) {
        return representativeFlags.stream()
            .map(fields -> {
                CaseFlagsType flags = fields.flags().get();
                CaseFlagsType externalFlags = fields.externalFlags().get();
                CaseFlagsType nameSource = flags != null ? flags : externalFlags;
                String partyName = nameSource == null ? null : nameSource.getPartyName();
                return new ExistingRepresentativeFlag(
                    fields.groupId(), partyName,
                    flags == null ? null : flags.getDetails(),
                    externalFlags == null ? null : externalFlags.getDetails());
            })
            .toList();
    }

    private static void alignRepresentativeFlagDetails(CaseData caseData,
                                                                 List<PartyFlagFields> representativeFlags,
                                                                 List<ExistingRepresentativeFlag> existingFlags) {
        for (int i = 0; i < representativeCount(caseData); i++) {
            int flagIndex = representativeFlagIndex(caseData, i);
            if (!hasUniqueRepresentativeAtIndex(caseData, i) || flagIndex >= representativeFlags.size()) {
                continue;
            }

            PartyFlagFields fields = representativeFlags.get(flagIndex);
            String partyName = representativeName(caseData, i);
            fields.restoreDetailsFrom(existingFlags, partyName);
        }
    }

    private static void clearDuplicateRepresentativeFlags(CaseData caseData,
                                                           List<PartyFlagFields> representativeFlags) {
        Set<Integer> populatedFlagIndexes = new HashSet<>();
        for (int i = 0; i < representativeCount(caseData); i++) {
            int flagIndex = representativeFlagIndex(caseData, i);
            if (hasUniqueRepresentativeAtIndex(caseData, i) && flagIndex < representativeFlags.size()) {
                populatedFlagIndexes.add(flagIndex);
            }
        }

        for (int i = 0; i < representativeFlags.size(); i++) {
            if (!populatedFlagIndexes.contains(i)) {
                representativeFlags.get(i).clear();
            }
        }
    }

    private static AllPartyFlags allPartyFlags(CaseData caseData) {
        if (caseData.getAllPartyFlags() == null) {
            caseData.setAllPartyFlags(AllPartyFlags.builder().build());
        }

        return caseData.getAllPartyFlags();
    }

    private static List<PartyFlagFields> respondentFlagFields(AllPartyFlags allPartyFlags) {
        return List.of(
            new PartyFlagFields(RESPONDENT1, "respondent1Flags", "respondent1ExternalFlags",
                allPartyFlags::getRespondent1Flags, allPartyFlags::setRespondent1Flags,
                allPartyFlags::getRespondent1ExternalFlags, allPartyFlags::setRespondent1ExternalFlags),
            new PartyFlagFields(RESPONDENT2, "respondent2Flags", "respondent2ExternalFlags",
                allPartyFlags::getRespondent2Flags, allPartyFlags::setRespondent2Flags,
                allPartyFlags::getRespondent2ExternalFlags, allPartyFlags::setRespondent2ExternalFlags),
            new PartyFlagFields(RESPONDENT3, "respondent3Flags", "respondent3ExternalFlags",
                allPartyFlags::getRespondent3Flags, allPartyFlags::setRespondent3Flags,
                allPartyFlags::getRespondent3ExternalFlags, allPartyFlags::setRespondent3ExternalFlags),
            new PartyFlagFields(RESPONDENT4, "respondent4Flags", "respondent4ExternalFlags",
                allPartyFlags::getRespondent4Flags, allPartyFlags::setRespondent4Flags,
                allPartyFlags::getRespondent4ExternalFlags, allPartyFlags::setRespondent4ExternalFlags),
            new PartyFlagFields(RESPONDENT5, "respondent5Flags", "respondent5ExternalFlags",
                allPartyFlags::getRespondent5Flags, allPartyFlags::setRespondent5Flags,
                allPartyFlags::getRespondent5ExternalFlags, allPartyFlags::setRespondent5ExternalFlags),
            new PartyFlagFields(RESPONDENT6, "respondent6Flags", "respondent6ExternalFlags",
                allPartyFlags::getRespondent6Flags, allPartyFlags::setRespondent6Flags,
                allPartyFlags::getRespondent6ExternalFlags, allPartyFlags::setRespondent6ExternalFlags),
            new PartyFlagFields(RESPONDENT7, "respondent7Flags", "respondent7ExternalFlags",
                allPartyFlags::getRespondent7Flags, allPartyFlags::setRespondent7Flags,
                allPartyFlags::getRespondent7ExternalFlags, allPartyFlags::setRespondent7ExternalFlags),
            new PartyFlagFields(RESPONDENT8, "respondent8Flags", "respondent8ExternalFlags",
                allPartyFlags::getRespondent8Flags, allPartyFlags::setRespondent8Flags,
                allPartyFlags::getRespondent8ExternalFlags, allPartyFlags::setRespondent8ExternalFlags),
            new PartyFlagFields(RESPONDENT9, "respondent9Flags", "respondent9ExternalFlags",
                allPartyFlags::getRespondent9Flags, allPartyFlags::setRespondent9Flags,
                allPartyFlags::getRespondent9ExternalFlags, allPartyFlags::setRespondent9ExternalFlags),
            new PartyFlagFields(RESPONDENT10, "respondent10Flags", "respondent10ExternalFlags",
                allPartyFlags::getRespondent10Flags, allPartyFlags::setRespondent10Flags,
                allPartyFlags::getRespondent10ExternalFlags, allPartyFlags::setRespondent10ExternalFlags)
        );
    }

    private static List<PartyFlagFields> representativeFlagFields(AllPartyFlags allPartyFlags) {
        return List.of(
            representativeFlagFields(REPRESENTATIVE1, "representative1", allPartyFlags::getRepresentative1Flags,
                allPartyFlags::setRepresentative1Flags, allPartyFlags::getRepresentative1ExternalFlags,
                allPartyFlags::setRepresentative1ExternalFlags),
            representativeFlagFields(REPRESENTATIVE2, "representative2", allPartyFlags::getRepresentative2Flags,
                allPartyFlags::setRepresentative2Flags, allPartyFlags::getRepresentative2ExternalFlags,
                allPartyFlags::setRepresentative2ExternalFlags),
            representativeFlagFields(REPRESENTATIVE3, "representative3", allPartyFlags::getRepresentative3Flags,
                allPartyFlags::setRepresentative3Flags, allPartyFlags::getRepresentative3ExternalFlags,
                allPartyFlags::setRepresentative3ExternalFlags),
            representativeFlagFields(REPRESENTATIVE4, "representative4", allPartyFlags::getRepresentative4Flags,
                allPartyFlags::setRepresentative4Flags, allPartyFlags::getRepresentative4ExternalFlags,
                allPartyFlags::setRepresentative4ExternalFlags),
            representativeFlagFields(REPRESENTATIVE5, "representative5", allPartyFlags::getRepresentative5Flags,
                allPartyFlags::setRepresentative5Flags, allPartyFlags::getRepresentative5ExternalFlags,
                allPartyFlags::setRepresentative5ExternalFlags),
            representativeFlagFields(REPRESENTATIVE6, "representative6", allPartyFlags::getRepresentative6Flags,
                allPartyFlags::setRepresentative6Flags, allPartyFlags::getRepresentative6ExternalFlags,
                allPartyFlags::setRepresentative6ExternalFlags),
            representativeFlagFields(REPRESENTATIVE7, "representative7", allPartyFlags::getRepresentative7Flags,
                allPartyFlags::setRepresentative7Flags, allPartyFlags::getRepresentative7ExternalFlags,
                allPartyFlags::setRepresentative7ExternalFlags),
            representativeFlagFields(REPRESENTATIVE8, "representative8", allPartyFlags::getRepresentative8Flags,
                allPartyFlags::setRepresentative8Flags, allPartyFlags::getRepresentative8ExternalFlags,
                allPartyFlags::setRepresentative8ExternalFlags),
            representativeFlagFields(REPRESENTATIVE9, "representative9", allPartyFlags::getRepresentative9Flags,
                allPartyFlags::setRepresentative9Flags, allPartyFlags::getRepresentative9ExternalFlags,
                allPartyFlags::setRepresentative9ExternalFlags),
            representativeFlagFields(REPRESENTATIVE10, "representative10", allPartyFlags::getRepresentative10Flags,
                allPartyFlags::setRepresentative10Flags, allPartyFlags::getRepresentative10ExternalFlags,
                allPartyFlags::setRepresentative10ExternalFlags)
        );
    }

    private static PartyFlagFields representativeFlagFields(String groupId, String fieldPrefix,
                                                             Supplier<CaseFlagsType> flags,
                                                             Consumer<CaseFlagsType> setFlags,
                                                             Supplier<CaseFlagsType> externalFlags,
                                                             Consumer<CaseFlagsType> setExternalFlags) {
        return new PartyFlagFields(groupId, fieldPrefix + FLAGS_SUFFIX, fieldPrefix + EXTERNAL_FLAGS_SUFFIX,
            flags, setFlags, externalFlags, setExternalFlags);
    }

    private static void setupPartyCaseFlags(String partyName, String roleOnCase, String groupId,
                                            Supplier<CaseFlagsType> flags,
                                            Consumer<CaseFlagsType> setFlags,
                                            Supplier<CaseFlagsType> externalFlags,
                                            Consumer<CaseFlagsType> setExternalFlags) {
        setupPartyCaseFlag(partyName, roleOnCase, groupId, INTERNAL, flags, setFlags);
        setupPartyCaseFlag(partyName, roleOnCase, groupId, EXTERNAL, externalFlags, setExternalFlags);
    }

    private static void setupPartyCaseFlag(String partyName, String roleOnCase, String groupId, String visibility,
                                           Supplier<CaseFlagsType> flags, Consumer<CaseFlagsType> setFlags) {
        CaseFlagsType currentFlags = flags.get();
        if (currentFlags == null || StringUtils.isEmpty(currentFlags.getRoleOnCase())) {
            setFlags.accept(CaseFlagsType.builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .groupId(groupId)
                .visibility(visibility)
                .details(currentFlags == null ? null : currentFlags.getDetails())
                .build());
        } else if (!Objects.equals(partyName, currentFlags.getPartyName())) {
            currentFlags.setPartyName(partyName);
        }
    }

    private record PartyFlagFields(String groupId,
                                   String fieldName,
                                   String externalFieldName,
                                   Supplier<CaseFlagsType> flags,
                                   Consumer<CaseFlagsType> setFlags,
                                   Supplier<CaseFlagsType> externalFlags,
                                   Consumer<CaseFlagsType> setExternalFlags) {
        boolean setupRequired() {
            return flagSetupRequired(flags.get()) || flagSetupRequired(externalFlags.get());
        }

        void setup(String partyName, String roleOnCase) {
            setupPartyCaseFlags(partyName, roleOnCase, groupId, flags, setFlags, externalFlags, setExternalFlags);
        }

        void putIfPresent(Map<String, Object> data) {
            CaseFlagsService.putIfPresent(data, fieldName, flags.get());
            CaseFlagsService.putIfPresent(data, externalFieldName, externalFlags.get());
        }

        ListTypeItem<FlagDetailType> appendDetailsTo(ListTypeItem<FlagDetailType> partyLevel) {
            partyLevel = appendDetailsIfPresent(partyLevel, flags.get());
            return appendDetailsIfPresent(partyLevel, externalFlags.get());
        }

        boolean hasPartyName(String partyName) {
            return (flags.get() != null && Objects.equals(partyName, flags.get().getPartyName()))
                || (externalFlags.get() != null && Objects.equals(partyName, externalFlags.get().getPartyName()));
        }

        void clear() {
            setFlags.accept(null);
            setExternalFlags.accept(null);
        }

        boolean isEmpty() {
            return flags.get() == null && externalFlags.get() == null;
        }

        void restoreDetailsFrom(List<ExistingRepresentativeFlag> existingFlags, String partyName) {
            if (flags.get() != null) {
                flags.get().setDetails(null);
            }
            if (externalFlags.get() != null) {
                externalFlags.get().setDetails(null);
            }
            for (ExistingRepresentativeFlag existingFlag : existingFlags) {
                boolean unnamedLegacySection = StringUtils.isBlank(existingFlag.partyName())
                    && Objects.equals(existingFlag.groupId(), groupId);
                if (!unnamedLegacySection && !Objects.equals(existingFlag.partyName(), partyName)) {
                    continue;
                }

                if (flags.get() != null && existingFlag.details() != null) {
                    flags.get().setDetails(ListTypeItem.concat(flags.get().getDetails(), existingFlag.details()));
                }
                if (externalFlags.get() != null && existingFlag.externalDetails() != null) {
                    externalFlags.get().setDetails(ListTypeItem.concat(
                        externalFlags.get().getDetails(), existingFlag.externalDetails()));
                }
            }
        }

        void appendFlagDetails(CaseFlagsType flagsToAppend, CaseFlagsType externalFlagsToAppend) {
            appendDetails(flags.get(), flagsToAppend);
            appendDetails(externalFlags.get(), externalFlagsToAppend);
        }

        private static void appendDetails(CaseFlagsType target, CaseFlagsType source) {
            if (target != null && source != null && source.getDetails() != null) {
                target.setDetails(ListTypeItem.concat(target.getDetails(), source.getDetails()));
            }
        }
    }

    private record ExistingRepresentativeFlag(String groupId,
                                               String partyName,
                                               ListTypeItem<FlagDetailType> details,
                                               ListTypeItem<FlagDetailType> externalDetails) {
    }

    private ListTypeItem<FlagDetailType> getPartyCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = new ListTypeItem<>();
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();

        if (allPartyFlags == null) {
            return partyLevel;
        }

        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantExternalFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantRepresentativeFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantRepresentativeExternalFlags());

        for (PartyFlagFields fields : respondentFlagFields(allPartyFlags)) {
            partyLevel = fields.appendDetailsTo(partyLevel);
        }
        for (PartyFlagFields fields : representativeFlagFields(allPartyFlags)) {
            partyLevel = fields.appendDetailsTo(partyLevel);
        }

        return partyLevel;
    }

    private static ListTypeItem<FlagDetailType> appendDetailsIfPresent(
        ListTypeItem<FlagDetailType> partyLevel, CaseFlagsType flags) {
        if (flags != null && flags.getDetails() != null) {
            return ListTypeItem.concat(partyLevel, flags.getDetails());
        }
        return partyLevel;
    }

    private static void putIfPresent(Map<String, Object> data, String fieldName, Object value) {
        if (value != null) {
            data.put(fieldName, value);
        }
    }

    private boolean areAnyFlagsActive(ListTypeItem<FlagDetailType> flags, String...names) {
        if (flags == null) {
            return false;
        }

        List<String> activeFlagNames = Arrays.asList(names);

        return flags.stream()
            .map(GenericTypeItem::getValue)
            .filter(Objects::nonNull)
            .anyMatch(flag -> activeFlagNames.contains(flag.getName()) && ACTIVE.equals(flag.getStatus()));
    }
}
