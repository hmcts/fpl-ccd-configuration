package uk.gov.hmcts.reform.fpl.service.caseflag;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.caseflag.AllPartyFlags;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlagsType;
import uk.gov.hmcts.reform.fpl.model.caseflag.FlagDetailType;
import uk.gov.hmcts.reform.fpl.model.caseflag.GenericTypeItem;
import uk.gov.hmcts.reform.fpl.model.caseflag.ListTypeItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();
        if (allPartyFlags == null) {
            return true;
        }

        int respondentCount = caseData.getAllRespondents().size();

        return allPartyFlags.getCaseFlags() == null
            || flagSetupRequired(allPartyFlags.getApplicantFlags())
            || flagSetupRequired(allPartyFlags.getApplicantExternalFlags())
            || respondentFlagSetupRequired(respondentCount, respondentFlagFields(allPartyFlags));
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

        List<PartyFlagFields> respondentFlagFields = respondentFlagFields(allPartyFlags);
        int respondentFlagsToSetup = Math.min(caseData.getAllRespondents().size(), respondentFlagFields.size());

        for (int i = 0; i < respondentFlagsToSetup; i++) {
            respondentFlagFields.get(i).setup(respondentName(caseData, i), RESPONDENT + SPACE + (i + 1));
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
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();

        if (allPartyFlags == null) {
            return data;
        }

        putIfPresent(data, "caseFlags", allPartyFlags.getCaseFlags());
        putIfPresent(data, "applicantFlags", allPartyFlags.getApplicantFlags());
        putIfPresent(data, "applicantExternalFlags", allPartyFlags.getApplicantExternalFlags());
        respondentFlagFields(allPartyFlags).forEach(fields -> fields.putIfPresent(data));

        return data;
    }

    private static boolean respondentFlagSetupRequired(int respondentCount, List<PartyFlagFields> respondentFlags) {
        return respondentFlags.stream()
            .limit(respondentCount)
            .anyMatch(PartyFlagFields::setupRequired);
    }

    private static boolean flagSetupRequired(CaseFlagsType flags) {
        return flags == null || StringUtils.isEmpty(flags.getRoleOnCase());
    }

    private static String respondentName(CaseData caseData, int index) {
        return caseData.getAllRespondents().get(index).getValue().getParty().getFirstName()
            + SPACE + caseData.getAllRespondents().get(index).getValue().getParty().getLastName();
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
    }

    private ListTypeItem<FlagDetailType> getPartyCaseFlags(CaseData caseData) {
        ListTypeItem<FlagDetailType> partyLevel = new ListTypeItem<>();
        AllPartyFlags allPartyFlags = caseData.getAllPartyFlags();

        if (allPartyFlags == null) {
            return partyLevel;
        }

        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, allPartyFlags.getApplicantExternalFlags());

        for (PartyFlagFields fields : respondentFlagFields(allPartyFlags)) {
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
