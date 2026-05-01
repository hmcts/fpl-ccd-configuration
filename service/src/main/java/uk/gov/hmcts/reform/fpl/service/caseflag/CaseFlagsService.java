package uk.gov.hmcts.reform.fpl.service.caseflag;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.caseflag.FlagDetailType;
import uk.gov.hmcts.reform.fpl.model.caseflag.GenericTypeItem;
import uk.gov.hmcts.reform.fpl.model.caseflag.ListTypeItem;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlagsType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    private static final String SPACE = " ";

    public static final String INTERNAL    = "Internal";
    public static final String EXTERNAL    = "External";
    public static final String APPLICANT    = "applicant";
    public static final String RESPONDENT  = "respondent";
    public static final String RESPONDENT1  = "respondent1";
    public static final String RESPONDENT2  = "respondent2";
    public static final String RESPONDENT3  = "respondent3";
    public static final String RESPONDENT4  = "respondent4";
    public static final String RESPONDENT5  = "respondent5";
    public static final String RESPONDENT6  = "respondent6";
    public static final String RESPONDENT7  = "respondent7";
    public static final String RESPONDENT8  = "respondent8";
    public static final String RESPONDENT9  = "respondent9";
    public static final String RESPONDENT10  = "respondent10";

    public boolean caseFlagsSetupRequired(CaseData caseData) {
        int respondentCount = caseData.getAllRespondents().size();

        return caseData.getCaseFlags() == null
            || flagSetupRequired(caseData.getApplicantFlags())
            || flagSetupRequired(caseData.getApplicantExternalFlags())
            || respondentFlagSetupRequired(respondentCount, respondentFlagFields(caseData));
    }

    /**
     * Setup case flags for Applicant, Respondent and Case level.
     * @param caseData Data about the current case
     */
    public void setupCaseFlags(CaseData caseData) {
        if (caseData.getCaseFlags() == null) {
            caseData.setCaseFlags(CaseFlagsType.builder().build());
        }

        if (!caseData.getLocalAuthorities().isEmpty()) {
            setupPartyCaseFlags(caseData.getLocalAuthorities().getFirst().getValue().getName(),
                APPLICANT, APPLICANT,
                caseData::getApplicantFlags, caseData::setApplicantFlags,
                caseData::getApplicantExternalFlags, caseData::setApplicantExternalFlags);
        }

        List<PartyFlagFields> respondentFlagFields = respondentFlagFields(caseData);
        int respondentFlagsToSetup = Math.min(caseData.getAllRespondents().size(), respondentFlagFields.size());

        for (int i = 0; i < respondentFlagsToSetup; i++) {
            respondentFlagFields.get(i).setup(respondentName(caseData, i), RESPONDENT);
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

        putIfPresent(data, "caseFlags", caseData.getCaseFlags());
        putIfPresent(data, "applicantFlags", caseData.getApplicantFlags());
        putIfPresent(data, "applicantExternalFlags", caseData.getApplicantExternalFlags());
        respondentFlagFields(caseData).forEach(fields -> fields.putIfPresent(data));

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

    private static List<PartyFlagFields> respondentFlagFields(CaseData caseData) {
        return List.of(
            new PartyFlagFields(RESPONDENT1, "respondent1Flags", "respondent1ExternalFlags",
                caseData::getRespondent1Flags, caseData::setRespondent1Flags,
                caseData::getRespondent1ExternalFlags, caseData::setRespondent1ExternalFlags),
            new PartyFlagFields(RESPONDENT2, "respondent2Flags", "respondent2ExternalFlags",
                caseData::getRespondent2Flags, caseData::setRespondent2Flags,
                caseData::getRespondent2ExternalFlags, caseData::setRespondent2ExternalFlags),
            new PartyFlagFields(RESPONDENT3, "respondent3Flags", "respondent3ExternalFlags",
                caseData::getRespondent3Flags, caseData::setRespondent3Flags,
                caseData::getRespondent3ExternalFlags, caseData::setRespondent3ExternalFlags),
            new PartyFlagFields(RESPONDENT4, "respondent4Flags", "respondent4ExternalFlags",
                caseData::getRespondent4Flags, caseData::setRespondent4Flags,
                caseData::getRespondent4ExternalFlags, caseData::setRespondent4ExternalFlags),
            new PartyFlagFields(RESPONDENT5, "respondent5Flags", "respondent5ExternalFlags",
                caseData::getRespondent5Flags, caseData::setRespondent5Flags,
                caseData::getRespondent5ExternalFlags, caseData::setRespondent5ExternalFlags),
            new PartyFlagFields(RESPONDENT6, "respondent6Flags", "respondent6ExternalFlags",
                caseData::getRespondent6Flags, caseData::setRespondent6Flags,
                caseData::getRespondent6ExternalFlags, caseData::setRespondent6ExternalFlags),
            new PartyFlagFields(RESPONDENT7, "respondent7Flags", "respondent7ExternalFlags",
                caseData::getRespondent7Flags, caseData::setRespondent7Flags,
                caseData::getRespondent7ExternalFlags, caseData::setRespondent7ExternalFlags),
            new PartyFlagFields(RESPONDENT8, "respondent8Flags", "respondent8ExternalFlags",
                caseData::getRespondent8Flags, caseData::setRespondent8Flags,
                caseData::getRespondent8ExternalFlags, caseData::setRespondent8ExternalFlags),
            new PartyFlagFields(RESPONDENT9, "respondent9Flags", "respondent9ExternalFlags",
                caseData::getRespondent9Flags, caseData::setRespondent9Flags,
                caseData::getRespondent9ExternalFlags, caseData::setRespondent9ExternalFlags),
            new PartyFlagFields(RESPONDENT10, "respondent10Flags", "respondent10ExternalFlags",
                caseData::getRespondent10Flags, caseData::setRespondent10Flags,
                caseData::getRespondent10ExternalFlags, caseData::setRespondent10ExternalFlags)
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
        if (currentFlags == null) {
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

        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getApplicantFlags());
        partyLevel = appendDetailsIfPresent(partyLevel, caseData.getApplicantExternalFlags());

        for (PartyFlagFields fields : respondentFlagFields(caseData)) {
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
