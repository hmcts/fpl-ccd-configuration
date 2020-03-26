package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class ConfidentialDetailsServiceTest {
    private static final String CONFIDENTIAL = "Yes";
    private static final String NOT_CONFIDENTIAL = "NO";

    private ConfidentialDetailsService service = new ConfidentialDetailsService();

    @Test
    void shouldAddToListWhenConfidentialDetailsForChild() {
        List<Element<Child>> children = ImmutableList.of(childElement(CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldNotAddToListWhenChildrenIsEmptyList() {
        List<Element<Child>> children = ImmutableList.of();

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForChild() {
        List<Element<Child>> children = ImmutableList.of(
            childElement(CONFIDENTIAL),
            childElement(NOT_CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(children);

        assertThat(confidentialChildren).hasSize(1).containsExactly(children.get(0));
    }

    @Test
    void shouldAddToListWhenConfidentialDetailsForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElement(CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldNotAddToListWhenRespondentsIsEmptyList() {
        List<Element<Respondent>> respondents = ImmutableList.of();

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElement(CONFIDENTIAL),
            respondentElement(NOT_CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(respondents);

        assertThat(confidentialRespondents).hasSize(1).containsExactly(respondents.get(0));
    }

    @Test
    void shouldAddConfidentialDetailsToCaseDetailsWhenClassExists() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        List<Element<Respondent>> confidentialDetails = ImmutableList.of(
            respondentElement(CONFIDENTIAL));

        service.addConfidentialDetailsToCase(caseDetails, confidentialDetails, RESPONDENT);

        assertThat(caseDetails.getData()).containsKeys(RESPONDENT.getCaseDataKey());
    }

    private Element<Child> childElement(String hidden) {
        return ElementUtils.element(Child.builder()
            .party(ChildParty.builder()
                .firstName("James")
                .detailsHidden(hidden)
                .address(Address.builder()
                    .addressLine1("James' House")
                    .build())
                .build())
            .build());
    }

    @Test
    void foo() {
        List<Element<Child>> children = wrapElements(Child.builder()
            .party(ChildParty.builder()
                .firstName("James")
                .lastName("Smith")
                .detailsHidden("Yes")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .additionalNeeds("Additional Needs")
                .adoption("Adoption information")
                .fathersName("Fathers name")
                .gender("Male")
                .litigationIssues("Litigation issues")
                .build())
            .build());

        List<Element<Child>> confidentialChildDetails = service.retainConfidentialDetails(children,
            () -> Child.builder().build());

        assertThat(unwrapElements(confidentialChildDetails)).containsExactly(Child.builder()
            .party(ChildParty.builder()
                .firstName("James")
                .lastName("Smith")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());

        List<Element<Respondent>> respondents = wrapElements(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Smith")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .gender("Male")
                .litigationIssues("Litigation issues")
                .build())
            .build());

        List<Element<Respondent>> confidentialRespondentDetails = service.retainConfidentialDetails(respondents,
            () -> Respondent.builder().build());

        assertThat(unwrapElements(confidentialRespondentDetails)).containsExactly(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .lastName("Smith")
                .email(EmailAddress.builder().email("email@email.com").build())
                .address(Address.builder().addressLine1("Address Line 1").build())
                .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                .build())
            .build());

        List<Element<Other>> others = wrapElements(Other.builder()
            .name("James")
            .gender("Female")
            .detailsHidden("Yes")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build()
        );

        List<Element<Other>> confidentialOtherDetails = service.retainConfidentialDetails(others,
            () -> Other.builder().build());

        assertThat(unwrapElements(confidentialOtherDetails)).containsExactly(Other.builder()
            .name("James")
            .address(Address.builder().addressLine1("Address Line 1").build())
            .telephone("01227 831393")
            .build()
        );

    }

    private Element<Respondent> respondentElement(String hidden) {
        return ElementUtils.element(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("James")
                .contactDetailsHidden(hidden)
                .address(Address.builder()
                    .addressLine1("James' House")
                    .build())
                .build())
            .build());
    }
}
