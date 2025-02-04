package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.json.deserializer.YesNoDeserializer;
import uk.gov.hmcts.reform.fpl.json.serializer.YesNoSerializer;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespondentLocalAuthority {

    public static UUID DUMMY_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final String name;
    private final String email;
    private final String phoneNumber;
    private final String representativeFirstName;
    private final String representativeLastName;
    private final Address address;

    @JsonSerialize(using = YesNoSerializer.class)
    @JsonDeserialize(using = YesNoDeserializer.class)
    private final YesNo usingOtherOrg;

    private Organisation organisation;
    private List<Element<LegalCounsellor>> legalCounsellors;

    public static RespondentLocalAuthority toRespondentLocalAuthority(String name, String email) {
        return RespondentLocalAuthority.builder()
            .name(name)
            .email(email)
            .build();
    }

    /**
     * Convert the Respondent object to a RespondentLocalAuthority object, so that we can pre-populate the temporary
     * questions in the enterRespondents3rdParty journey.
     *
     * @param respondent the "fake" Respondent LA object, stored in the start of the respondents1 collection, with UUID
     *                   DUMMY_UUID.
     * @return a RespondentLocalAuthority object with the data from the Respondent object.
     */
    public static RespondentLocalAuthority fromRespondent(Respondent respondent) {
        return RespondentLocalAuthority.builder()
            .name(respondent.getParty().getFullName())
            .email(respondent.getSolicitor().getEmail())
            .phoneNumber(respondent.getSolicitor().getTelephoneNumber().getTelephoneNumber())
            .representativeFirstName(respondent.getSolicitor().getFirstName())
            .representativeLastName(respondent.getSolicitor().getLastName())
            .address(respondent.getParty().getAddress())
            .usingOtherOrg(respondent.getUsingOtherOrg())
            .organisation(YES.equals(respondent.getUsingOtherOrg())
                ? respondent.getSolicitor().getOrganisation() : null)
            .legalCounsellors(respondent.getLegalCounsellors())
            .build();
    }

    /**
     * Using a respondent as a base, override data attached to it so we don't lose any original data specific to the
     * "Respondent" instance - i.e. colleaguesToNotify.
     *
     * @param baseRespondent the base respondent object to override
     * @return an updated Element consisting of the baseRespondent + any overridden data based on this object.
     */
    public Element<Respondent> toRespondent(Respondent baseRespondent) {
        return element(DUMMY_UUID, baseRespondent.toBuilder()
            .isLocalAuthority(YES)
            .legalRepresentation(YES.getValue())
            .party(RespondentParty.builder()
                .firstName(name)
                .relationshipToChild("Local Authority")
                .address(address)
                .addressKnow(IsAddressKnowType.YES)
                .hideAddress(NO.getValue())
                .hideTelephone(NO.getValue())
                .build())
            .solicitor(RespondentSolicitor.builder()
                .organisation(organisation)
                .firstName(representativeFirstName)
                .lastName(representativeLastName)
                .email(email)
                .telephoneNumber(Telephone.builder()
                    .telephoneNumber(phoneNumber)
                    .build())
                .build())
            .legalCounsellors(legalCounsellors)
            .usingOtherOrg(usingOtherOrg)
            .build());
    }

}
