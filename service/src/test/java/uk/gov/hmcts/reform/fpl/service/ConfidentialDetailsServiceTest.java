package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.CHILD;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
class ConfidentialDetailsServiceTest {
    private static final String CONFIDENTIAL = "Yes";
    private static final String NOT_CONFIDENTIAL = "NO";
    private static final String NO_VALUE = null;
    private static final UUID ID = randomUUID();

    private ConfidentialDetailsService service = new ConfidentialDetailsService();

    @Nested
    class Children {

        @Test
        void shouldAddToListWhenConfidentialDetailsForChild() {
            List<Element<Child>> children = List.of(childWithConfidentialFields(ID, CONFIDENTIAL));

            List<Element<Child>> confidentialChildren = service.getConfidentialDetails(children);

            assertThat(confidentialChildren).containsOnly(childWithConfidentialFieldsAndShowAddress());
        }

        @Test
        void shouldNotAddToListWhenChildrenIsEmptyList() {
            List<Element<Child>> confidentialChildren = service.getConfidentialDetails(List.of());

            assertThat(confidentialChildren).isEmpty();
        }

        @Test
        void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForChild() {
            List<Element<Child>> children = List.of(
                childWithConfidentialFields(ID, CONFIDENTIAL),
                childWithConfidentialFields(ID, NOT_CONFIDENTIAL));

            List<Element<Child>> confidentialChildren = service.getConfidentialDetails(children);

            assertThat(confidentialChildren).containsExactly(childWithConfidentialFieldsAndShowAddress());
        }

        @Test
        void shouldRemoveConfidentialDetailsWhenMarkedAsConfidential() {
            List<Element<Child>> children = List.of(childWithConfidentialFields(ID, CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(children))
                .containsOnly(childWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldNotRemoveConfidentialDetailsWhenNotMarkedAsConfidential() {
            List<Element<Child>> children = List.of(childWithConfidentialFields(ID, NOT_CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(children)).isEqualTo(children);
        }

        @Test
        void shouldAddEmptyElementWhenChildrenIsEmpty() {
            List<Element<Child>> elements = service.prepareCollection(List.of(), List.of(), Child.expandCollection());

            assertThat(elements.get(0).getValue().getParty().getPartyId()).isNotNull();
        }

        @Test
        void shouldReturnChildrenIfChildrenIsPrePopulated() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithRemovedConfidentialFields(ID)))
                .build();

            List<Element<Child>> children = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(children).containsExactly(childWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldPrepareChildWithConfidentialValuesWhenConfidentialChildrenIsNotEmpty() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithRemovedConfidentialFields(ID)))
                .confidentialChildren(List.of(childWithConfidentialFields(ID, CONFIDENTIAL)))
                .build();

            List<Element<Child>> children = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(children).containsOnly(childWithConfidentialFields(ID, CONFIDENTIAL));
        }

        @Test
        void shouldReturnChildWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialChild() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithRemovedConfidentialFields(ID)))
                .confidentialChildren(List.of(childWithConfidentialFields(randomUUID(), CONFIDENTIAL)))
                .build();

            List<Element<Child>> children = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(children).containsOnly(childWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldAddExpectedChildWhenHiddenDetailsMarkedAsNo() {
            CaseData caseData = CaseData.builder()
                .children1(List.of(childWithConfidentialFields(ID, NOT_CONFIDENTIAL)))
                .confidentialChildren(List.of(childWithConfidentialFields(ID, CONFIDENTIAL)))
                .build();

            List<Element<Child>> children = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(children).containsOnly(childWithConfidentialFields(ID, NOT_CONFIDENTIAL));
        }

        @Test
        void shouldMaintainOrderingOfChildrenWhenComplexScenario() {
            UUID otherId = randomUUID();

            List<Element<Child>> children = List.of(
                childWithRemovedConfidentialFields(ID),
                childWithConfidentialFields(randomUUID(), NOT_CONFIDENTIAL),
                childWithRemovedConfidentialFields(otherId));

            List<Element<Child>> confidentialChildren = List.of(
                childWithConfidentialFields(ID, CONFIDENTIAL),
                childWithConfidentialFields(otherId, CONFIDENTIAL));

            CaseData caseData = CaseData.builder()
                .children1(children)
                .confidentialChildren(confidentialChildren)
                .build();

            List<Element<Child>> updatedChildren = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(updatedChildren.get(0)).isEqualTo(confidentialChildren.get(0));
            assertThat(updatedChildren.get(1)).isEqualTo(children.get(1));
            assertThat(updatedChildren.get(2)).isEqualTo(confidentialChildren.get(1));
        }

        @Test
        void shouldAddConfidentialDetailsToCaseDetailsWhenClassExists() {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            List<Element<Child>> children = List.of(childWithConfidentialFields(ID, CONFIDENTIAL));

            service.addConfidentialDetailsToCase(caseDetails, children, CHILD);

            assertThat(caseDetails.getData())
                .containsKeys(CHILD.getCaseDataKey(), CHILD.getConfidentialKey());
        }

        @Test
        void shouldRemoveConfidentialDetailsFromCaseDetailsWhenNoConfidentialDetails() {
            Map<String, Object> data = new HashMap<>();
            data.put(CHILD.getConfidentialKey(), "");

            CaseDetails caseDetails = CaseDetails.builder().data(data).build();

            service.addConfidentialDetailsToCase(caseDetails, emptyList(), CHILD);

            assertThat(caseDetails.getData()).isEmpty();
        }

        @Test
        void shouldNotOverrideOldDataIfLivingDetailsNotHiddenYet() {
            Element<Child> originalChild = childWithRemovedConfidentialFieldsWithLivingSituation(ID);
            Element<Child> confDetails = childWithConfidentialFieldsAndShowAddressButNoLivingSituation(ID);
            CaseData caseData = CaseData.builder()
                .children1(List.of(originalChild))
                .confidentialChildren(List.of(confDetails))
                .build();

            List<Element<Child>> updatedChildren = service.prepareCollection(caseData.getAllChildren(),
                caseData.getConfidentialChildren(), Child.expandCollection());

            assertThat(updatedChildren.get(0).getValue().getParty())
                .extracting("livingSituation", "livingSituationDetails")
                .containsExactly("Living with parents", "Details here");
        }

        private ChildParty.ChildPartyBuilder baseChildBuilder(String isAddressConfidential) {
            return ChildParty.builder()
                .firstName("John")
                .lastName("Smith")
                .isAddressConfidential(isAddressConfidential);
        }

        private Element<Child> childWithRemovedConfidentialFields(UUID id) {
            return element(id, Child.builder()
                .party(baseChildBuilder(CONFIDENTIAL).build())
                .build());
        }

        private Element<Child> childWithRemovedConfidentialFieldsWithLivingSituation(UUID id) {
            // Prior to DFPL-2639, living situation + details were not confidential
            return element(id, Child.builder()
                .party(baseChildBuilder(CONFIDENTIAL)
                    .livingSituation("Living with parents")
                    .livingSituationDetails("Details here")
                    .build())
                .build());
        }

        private Element<Child> childWithConfidentialFieldsAndShowAddressButNoLivingSituation(UUID id) {
            return element(id, Child.builder()
                .party(baseChildBuilder(NO_VALUE)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .isAddressConfidential("Yes")
                    .build())
                .build());
        }

        private Element<Child> childWithConfidentialFields(UUID id, String isAddressConfidential) {
            return element(id, Child.builder()
                .party(baseChildBuilder(isAddressConfidential)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .livingSituation("Living in a refuge")
                    .build())
                .build());
        }

        private Element<Child> childWithConfidentialFieldsAndShowAddress() {
            return element(ID, Child.builder()
                .party(baseChildBuilder(NO_VALUE)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .livingSituation("Living in a refuge")
                    .build())
                .build());
        }
    }

    @Nested
    class Respondents {

        @Test
        void shouldAddToListWhenConfidentialDetailsForRespondent() {
            List<Element<Respondent>> respondents = List.of(respondentWithConfidentialFields(ID, CONFIDENTIAL));

            List<Element<Respondent>> confidentialRespondents = service.getConfidentialDetails(respondents);

            assertThat(confidentialRespondents)
                .containsOnly(respondentWithConfidentialFields(ID, NO_VALUE, CONFIDENTIAL));
        }

        @Test
        void shouldNotAddToListWhenRespondentsIsEmptyList() {
            List<Element<Respondent>> confidentialRespondents = service.getConfidentialDetails(List.of());

            assertThat(confidentialRespondents).isEmpty();
        }

        @Test
        void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForRespondent() {
            List<Element<Respondent>> respondents = List.of(
                respondentWithConfidentialFields(ID, CONFIDENTIAL),
                respondentWithConfidentialFields(randomUUID(), NOT_CONFIDENTIAL));

            List<Element<Respondent>> confidentialRespondents = service.getConfidentialDetails(respondents);

            assertThat(confidentialRespondents)
                .containsExactly(respondentWithConfidentialFields(ID, NO_VALUE, CONFIDENTIAL));
        }

        @Test
        void shouldRemoveConfidentialDetailsWhenMarkedAsConfidential() {
            List<Element<Respondent>> respondent = List.of(respondentWithConfidentialFields(ID, CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(respondent))
                .containsOnly(respondentWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldNotRemoveConfidentialDetailsWhenNotMarkedAsConfidential() {
            List<Element<Respondent>> respondents = List.of(respondentWithConfidentialFields(ID, NOT_CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(respondents)).isEqualTo(respondents);
        }

        @Test
        void shouldAddConfidentialDetailsToCaseDetailsWhenClassExists() {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            List<Element<Respondent>> respondents = List.of(respondentWithConfidentialFields(ID, CONFIDENTIAL));

            service.addConfidentialDetailsToCase(caseDetails, respondents, RESPONDENT);

            assertThat(caseDetails.getData())
                .containsKeys(RESPONDENT.getCaseDataKey(), RESPONDENT.getConfidentialKey());
        }

        @Test
        void shouldRemoveConfidentialDetailsFromCaseDetailsWhenNoConfidentialDetails() {
            Map<String, Object> data = new HashMap<>();
            data.put(RESPONDENT.getConfidentialKey(), "");

            CaseDetails caseDetails = CaseDetails.builder().data(data).build();

            service.addConfidentialDetailsToCase(caseDetails, emptyList(), RESPONDENT);

            assertThat(caseDetails.getData()).isEmpty();
        }

        @Test
        void shouldAddEmptyElementWhenRespondentIsEmpty() {
            List<Element<Respondent>> elements = service.prepareCollection(
                List.of(), List.of(), Respondent.expandCollection());

            assertThat(elements.get(0).getValue().getParty().getPartyId()).isNotNull();
        }

        @Test
        void shouldReturnRespondentsIfRespondentsIsPrePopulated() {
            CaseData caseData = CaseData.builder()
                .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
                .build();

            List<Element<Respondent>> confidentialRespondents = service.prepareCollection(
                caseData.getAllRespondents(), caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(confidentialRespondents).containsExactly(respondentWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldPrepareRespondentWithConfidentialValuesWhenConfidentialRespondentIsNotEmpty() {
            CaseData caseData = CaseData.builder()
                .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
                .confidentialRespondents(List.of(respondentWithConfidentialFields(ID, CONFIDENTIAL)))
                .build();

            List<Element<Respondent>> confidentialRespondents = service.prepareCollection(
                caseData.getAllRespondents(), caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(confidentialRespondents).containsOnly(respondentWithConfidentialFields(ID, CONFIDENTIAL));
        }

        @Test
        void shouldReturnRespondentWithoutConfidentialDetailsWhenThereIsNoMatchingConfidentialRespondent() {
            CaseData caseData = CaseData.builder()
                .respondents1(List.of(respondentWithRemovedConfidentialFields(ID)))
                .confidentialRespondents(List.of(respondentWithConfidentialFields(randomUUID(), CONFIDENTIAL)))
                .build();

            List<Element<Respondent>> respondents = service.prepareCollection(
                caseData.getAllRespondents(), caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(respondents).containsOnly(respondentWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldAddExpectedRespondentWhenHiddenDetailsMarkedAsNo() {
            CaseData caseData = CaseData.builder()
                .respondents1(List.of(respondentWithConfidentialFields(ID, NOT_CONFIDENTIAL)))
                .confidentialRespondents(List.of(respondentWithConfidentialFields(ID, CONFIDENTIAL)))
                .build();

            List<Element<Respondent>> respondents = service.prepareCollection(
                caseData.getAllRespondents(), caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(respondents).containsOnly(respondentWithConfidentialFields(ID, NOT_CONFIDENTIAL));
        }

        @Test
        void shouldMaintainOrderingOfRespondentWhenComplexScenario() {
            UUID otherId = randomUUID();

            List<Element<Respondent>> respondents = List.of(
                respondentWithRemovedConfidentialFields(ID),
                respondentWithConfidentialFields(randomUUID(), NOT_CONFIDENTIAL),
                respondentWithRemovedConfidentialFields(otherId));

            List<Element<Respondent>> confidentialRespondent = List.of(
                respondentWithConfidentialFields(ID, CONFIDENTIAL),
                respondentWithConfidentialFields(otherId, CONFIDENTIAL));

            CaseData caseData = CaseData.builder()
                .respondents1(respondents)
                .confidentialRespondents(confidentialRespondent)
                .build();

            List<Element<Respondent>> updatedRespondent = service.prepareCollection(
                caseData.getAllRespondents(), caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(updatedRespondent.get(0)).isEqualTo(confidentialRespondent.get(0));
            assertThat(updatedRespondent.get(1)).isEqualTo(respondents.get(1));
            assertThat(updatedRespondent.get(2)).isEqualTo(confidentialRespondent.get(1));
        }

        @Test
        void shouldNotOverrideDataIfAddressKnownNotHiddenYet() {
            Element<Respondent> originalRespondent = respondentWithRemovedConfidentialFieldsWithKnownValue(ID);
            Element<Respondent> confDetails = respondentWithConfidentialFieldsButNoAddrKnownValue(ID, CONFIDENTIAL);

            CaseData caseData = CaseData.builder()
                .respondents1(List.of(originalRespondent))
                .confidentialRespondents(List.of(confDetails))
                .build();

            List<Element<Respondent>> updatedRespondents = service.prepareCollection(caseData.getAllRespondents(),
                caseData.getConfidentialRespondents(), Respondent.expandCollection());

            assertThat(updatedRespondents.get(0).getValue().getParty())
                .extracting("addressKnow").isEqualTo(IsAddressKnowType.YES);
        }

        private RespondentParty.RespondentPartyBuilder baseRespondentBuilder(String detailsHidden) {
            return RespondentParty.builder()
                .firstName("James")
                .lastName("Johnson")
                .contactDetailsHidden(detailsHidden)
                .hideAddress(detailsHidden)
                .hideTelephone(detailsHidden);
        }

        private Element<Respondent> respondentWithRemovedConfidentialFields(UUID id) {
            return element(id, Respondent.builder()
                .party(baseRespondentBuilder(CONFIDENTIAL).build())
                .build());
        }

        private Element<Respondent> respondentWithRemovedConfidentialFieldsWithKnownValue(UUID id) {
            return element(id, Respondent.builder()
                .party(baseRespondentBuilder(CONFIDENTIAL)
                    .addressKnow(IsAddressKnowType.YES)
                    .build())
                .build());
        }

        private Element<Respondent> respondentWithConfidentialFieldsButNoAddrKnownValue(UUID id, String detailsHidden) {
            return element(id, Respondent.builder()
                .party(baseRespondentBuilder(detailsHidden)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .build())
                .build());
        }


        private Element<Respondent> respondentWithConfidentialFields(UUID id, String detailsHidden) {
            return element(id, Respondent.builder()
                .party(baseRespondentBuilder(detailsHidden)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .addressKnow(IsAddressKnowType.LIVE_IN_REFUGE)
                    .build())
                .build());
        }

        private Element<Respondent> respondentWithConfidentialFields(UUID id, String detailsHidden,
                                                                     String hideAddressTelephone) {
            return element(id, Respondent.builder()
                .party(baseRespondentBuilder(detailsHidden)
                    .email(EmailAddress.builder().email("email@email.com").build())
                    .address(Address.builder().addressLine1("Address Line 1").build())
                    .telephoneNumber(Telephone.builder().telephoneNumber("01227 831393").build())
                    .addressKnow(IsAddressKnowType.LIVE_IN_REFUGE)
                    .hideTelephone(hideAddressTelephone)
                    .hideAddress(hideAddressTelephone)
                    .build())
                .build());
        }
    }


    @Nested
    class OthersTests {
        @Test
        void shouldAddToListWhenConfidentialDetailsForOther() {
            List<Element<Other>> others = List.of(otherWithConfidentialFields(ID, CONFIDENTIAL));

            List<Element<Other>> confidentialOthers = service.getConfidentialDetails(others);

            assertThat(confidentialOthers).containsOnly(otherWithConfidentialFields(ID, NO_VALUE));
        }

        @Test
        void shouldNotAddToListWhenOthersIsEmptyList() {
            List<Element<Other>> confidentialOthers = service.getConfidentialDetails(List.of());

            assertThat(confidentialOthers).isEmpty();
        }

        @Test
        void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForOther() {
            List<Element<Other>> others = List.of(
                otherWithConfidentialFields(ID, CONFIDENTIAL),
                otherWithConfidentialFields(ID, NOT_CONFIDENTIAL));

            List<Element<Other>> confidentialOthers = service.getConfidentialDetails(others);

            assertThat(confidentialOthers).containsExactly(otherWithConfidentialFields(ID, NO_VALUE));
        }

        @Test
        void shouldRemoveConfidentialDetailsWhenMarkedAsConfidential() {
            List<Element<Other>> others = List.of(otherWithConfidentialFields(ID, CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(others))
                .containsOnly(otherWithRemovedConfidentialFields(ID));
        }

        @Test
        void shouldNotRemoveConfidentialDetailsWhenNotMarkedAsConfidential() {
            List<Element<Other>> others = List.of(otherWithConfidentialFields(ID, NOT_CONFIDENTIAL));

            assertThat(service.removeConfidentialDetails(others)).isEqualTo(others);
        }

        @Test
        void shouldAddConfidentialDetailsToCaseDetailsWhenClassExists() {
            CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
            List<Element<Other>> others = List.of(otherWithConfidentialFields(ID, CONFIDENTIAL));

            service.addConfidentialDetailsToCase(caseDetails, others, OTHER);

            assertThat(caseDetails.getData()).containsKeys(OTHER.getConfidentialKey());
        }

        @Test
        void shouldRemoveConfidentialDetailsFromCaseDetailsWhenNoConfidentialDetails() {
            Map<String, Object> data = new HashMap<>();
            data.put(OTHER.getConfidentialKey(), "");

            CaseDetails caseDetails = CaseDetails.builder().data(data).build();

            service.addConfidentialDetailsToCase(caseDetails, emptyList(), OTHER);

            assertThat(caseDetails.getData()).isEmpty();
        }


        private Other.OtherBuilder baseOtherBuilder(String detailsHidden) {
            return Other.builder()
                .name("John Smith")
                .detailsHidden(detailsHidden);
        }

        private Element<Other> otherWithRemovedConfidentialFields(UUID id) {
            return element(id, baseOtherBuilder(CONFIDENTIAL).build());
        }

        private Element<Other> otherWithConfidentialFields(UUID id, String detailsHidden) {
            return element(id, baseOtherBuilder(detailsHidden)
                .address(Address.builder().addressLine1("Address Line 1").build())
                .addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE)
                .telephone("01227 831393")
                .build());
        }
    }
}
