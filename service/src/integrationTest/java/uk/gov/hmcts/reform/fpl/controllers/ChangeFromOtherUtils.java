package uk.gov.hmcts.reform.fpl.controllers;

import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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
                .telephone("123456789")
                .address(buildHiddenAddress("selected other"))
                .build())
        );
    }

    public static OtherToRespondentEventData otherToRespondentEventData(Respondent transformedRespondent, Others others,
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
            .designated(YesNo.YES.getValue())
            .build());
    }

    public static List<Element<Other>> prepareConfidentialOthersFromAllOthers(List<Element<Other>> allOthers) {
        return allOthers.stream().map(
            e -> element(
                e.getId(),
                e.getValue()
                    .toBuilder()
                    .detailsHidden(null)
                    .telephone("123456789")
                    .address(buildHiddenAddress(e.getValue().getName()))
                    .build())
        ).collect(toList());
    }

    public static Respondent prepareExpectedTransformedConfidentialRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .addressKnow(IsAddressKnowType.YES)
                .address(buildHiddenAddress("Converting"))
                .telephoneNumber(Telephone.builder().telephoneNumber("123456789").build())
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

    public static Respondent prepareExpectedTransformedRespondent(boolean contactDeatilsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(contactDeatilsHidden ? null : Telephone.builder()
                    .telephoneNumber("123456789").build())
                .address(contactDeatilsHidden ? null : buildHiddenAddress("Converting"))
                .addressKnow(IsAddressKnowType.YES)
                .contactDetailsHidden(YesNo.from(contactDeatilsHidden).getValue())
                .build())
            .legalRepresentation("No")
            .build();
    }

    public static List<Element<Other>> prepareConfidentialOthersTestingData(
        Others others, boolean firstOtherDetailsHidden,
        Predicate<Integer> additionalOtherDetailsHiddenDecider) {
        List<Element<Other>> ret = new ArrayList<>();
        if (firstOtherDetailsHidden) {
            ret.add(element(Other.builder()
                .address(Address.builder().addressLine1("FIRST OTHER SECRET ADDRESS 1").build())
                .build()));
        }
        if (others.getAdditionalOthers() != null) {
            for (int i = 0; i < others.getAdditionalOthers().size(); i++) {
                if (additionalOtherDetailsHiddenDecider.test(i)) {
                    Element<Other> ao = others.getAdditionalOthers().get(i);
                    ret.add(element(ao.getId(), Other.builder()
                        .address(Address.builder().addressLine1("ADDITIONAL OTHER SECRET ADDRESS 1").build())
                        .build()));
                }
            }
        }
        return ret;
    }

    public static Others prepareOthersTestingData(int numberOfAdditionalOther, boolean firstOtherDetailsHidden,
                                                  boolean additionalOtherDetailsHidden) {
        return prepareOthersTestingData(numberOfAdditionalOther, firstOtherDetailsHidden,
            (i) -> additionalOtherDetailsHidden);
    }

    public static Others prepareOthersTestingData(int numberOfAdditionalOther, boolean firstOtherDetailsHidden,
                                                  Predicate<Integer> additionalOtherDetailsHiddenDecider) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder()
                .name(String.format("Marco %s", i + 1))
                .detailsHidden(YesNo.from(additionalOtherDetailsHiddenDecider.test(i)).getValue())
                .build()));
        }
        Other firstOther = Other.builder()
            .name("Marco 0")
            .detailsHidden(YesNo.from(firstOtherDetailsHidden).getValue())
            .build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();
        return others;
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
