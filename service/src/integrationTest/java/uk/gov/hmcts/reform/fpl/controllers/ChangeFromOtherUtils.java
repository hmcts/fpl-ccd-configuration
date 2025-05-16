package uk.gov.hmcts.reform.fpl.controllers;

import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.time.Month.JUNE;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_10;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_3;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_4;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_5;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_6;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_7;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_8;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_9;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public abstract class ChangeFromOtherUtils {

    public static RepresentativeRole resolveOtherRepresentativeRole(int i) {
        switch (i) {
            case 0:
                return RepresentativeRole.REPRESENTING_PERSON_1;
            case 1:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
            case 2:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_2;
            case 3:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_3;
            case 4:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_4;
            case 5:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_5;
            case 6:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_6;
            case 7:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_7;
            case 8:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_8;
            case 9:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_9;
            default:
                return null;
        }
    }

    public static RepresentativeRole resolveRespondentRepresentativeRole(int i) {
        switch (i) {
            case 1:
                return REPRESENTING_RESPONDENT_1;
            case 2:
                return REPRESENTING_RESPONDENT_2;
            case 3:
                return REPRESENTING_RESPONDENT_3;
            case 4:
                return REPRESENTING_RESPONDENT_4;
            case 5:
                return REPRESENTING_RESPONDENT_5;
            case 6:
                return REPRESENTING_RESPONDENT_6;
            case 7:
                return REPRESENTING_RESPONDENT_7;
            case 8:
                return REPRESENTING_RESPONDENT_8;
            case 9:
                return REPRESENTING_RESPONDENT_9;
            case 10:
                return REPRESENTING_RESPONDENT_10;
            default:
                return null;
        }
    }

    public static List<Element<Other>> prepareSingleConfidentialOther(int selectedOtherSeq,
                                                                      Other firstOther,
                                                                      List<Element<Other>> additionalOthers) {
        return List.of(element(
            (selectedOtherSeq == 0 ? UUID.randomUUID() : additionalOthers.get(selectedOtherSeq - 1).getId()),
            (selectedOtherSeq == 0 ? firstOther : additionalOthers.get(selectedOtherSeq - 1).getValue())
                .toBuilder()
                .detailsHidden(null)
                .addressKnowV2(IsAddressKnowType.YES)
                .telephone("123456789")
                .address(buildHiddenAddress("selected other"))
                .build())
        );
    }

    public static OtherToRespondentEventData otherToRespondentEventData(Respondent transformedRespondent,
                                                                        List<Element<Other>> others,
                                                                        int selectedOtherSeq) {
        return OtherToRespondentEventData.builder()
            .transformedRespondent(transformedRespondent)
            .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
            .build();
    }

    public static List<Element<Respondent>> prepareConfidentialRespondentsFromRespondents1(
        List<Element<Respondent>> respondents1) {
        return respondents1.stream().map(
            e -> element(e.getId(), Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(e.getValue().getParty().getFirstName())
                    .addressKnow(IsAddressKnowType.YES)
                    .address(buildHiddenAddress(e.getValue().getParty().getFirstName()))
                    .telephoneNumber(Telephone.builder().telephoneNumber("777777777").build())
                    .build())
                .build())
        ).collect(toList());
    }

    public static List<Element<LocalAuthority>> localAuthorities() {
        return wrapElements(LocalAuthority.builder()
            .name(LOCAL_AUTHORITY_1_NAME)
            .email(LOCAL_AUTHORITY_1_INBOX)
            .designated(YES.getValue())
            .build());
    }

    public static Respondent prepareExpectedTransformedConfidentialRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .addressKnow(IsAddressKnowType.YES)
                .address(buildHiddenAddress("Converting"))
                .telephoneNumber(Telephone.builder().telephoneNumber("123456789").build())
                .hideTelephone(YES.getValue())
                .hideAddress(YES.getValue())
                .build())
            .build();
    }

    public static Respondent prepareExpectedExistingConfidentialRespondent(int seqNo) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(String.format("existing respondent %s", seqNo))
                .addressKnow(IsAddressKnowType.YES)
                .address(buildHiddenAddress(String.format("existing respondent %s", seqNo)))
                .telephoneNumber(Telephone.builder().telephoneNumber("777777777").build())
                .hideTelephone(YES.getValue())
                .hideAddress(YES.getValue())
                .build())
            .build();
    }

    public static Representative prepareOtherRepresentative(int otherSequence) {
        RepresentativeRole role = resolveOtherRepresentativeRole(otherSequence);
        return Representative.builder()
            .fullName("Joyce")
            .role(role)
            .build();
    }

    public static Respondent prepareTransformedRespondentTestingData(boolean contactDetailsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber("123456789").build())
                .address(buildHiddenAddress("Converting"))
                .addressKnow(IsAddressKnowType.YES)
                .contactDetailsHidden(YesNo.from(contactDetailsHidden).getValue())
                .build())
            .legalRepresentation("No")
            .build();
    }

    public static Respondent prepareExpectedTransformedRespondent(boolean contactDetailsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(contactDetailsHidden ? null : Telephone.builder()
                    .telephoneNumber("123456789").build())
                .address(contactDetailsHidden ? null : buildHiddenAddress("Converting"))
                .addressKnow(contactDetailsHidden ? null : IsAddressKnowType.YES)
                .contactDetailsHidden(YesNo.from(contactDetailsHidden).getValue())
                .build())
            .legalRepresentation("No")
            .build();
    }

    public static List<Element<Other>> prepareConfidentialOthers(List<Element<Other>> others) {
        return IntStream.range(0, nullSafeList(others).size())
            .mapToObj(i -> {
                Element<Other> otherElm = others.get(i);
                Other other = otherElm.getValue();
                if (other.containsConfidentialDetails()) {
                    return element(otherElm.getId(), other.toBuilder()
                        .telephone(YES.getValue().equals(other.getHideTelephone()) ? "123456789" : null)
                        .addressKnowV2(YES.getValue().equals(other.getHideAddress()) ? IsAddressKnowType.YES : null)
                        .address(YES.getValue().equals(other.getHideAddress())
                            ? buildHiddenAddress(String.valueOf(i + 1)) : null)
                        .build());
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static List<Element<Other>> prepareOthers(int numOfOthers, List<Integer> confidentialOtherIdx) {
        return IntStream.range(0, numOfOthers)
            .mapToObj(idx -> {
                final String isConfidential = nullSafeList(confidentialOtherIdx).contains(idx) ? "Yes" : "No";
                return Other.builder()
                    .firstName("Marco")
                    .lastName(String.valueOf(idx + 1))
                    .hideAddress(isConfidential)
                    .hideTelephone(isConfidential)
                    .build();
            })
            .map(ElementUtils::element)
            .toList();
    }

    public static Address buildHiddenAddress(String identifier) {
        return Address.builder()
            .addressLine1(String.format("Secret Address %s", identifier))
            .country("United Kingdom")
            .postcode("XXX")
            .postTown("Town")
            .build();
    }

    public static List<Respondent> prepareRespondentsTestingData(int numberOfRespondent) {
        return prepareRespondentsTestingData(numberOfRespondent, false);
    }

    public static List<Respondent> prepareRespondentsTestingData(int numberOfRespondent,
                                                                 boolean respondentDetailsHidden) {
        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(String.format("existing respondent %s", j))
                        .dateOfBirth(LocalDate.of(1989, JUNE, 4))
                        .telephoneNumber(respondentDetailsHidden ? null : Telephone.builder()
                            .telephoneNumber("777777777")
                            .build())
                        .address(respondentDetailsHidden ? null : buildHiddenAddress("" + j))
                        .addressKnow(respondentDetailsHidden ? IsAddressKnowType.YES : IsAddressKnowType.NO)
                        .contactDetailsHidden(YesNo.from(respondentDetailsHidden).getValue())
                        .build())
                    .legalRepresentation("No")
                    .build()
            );
        }
        return respondents;
    }
}
