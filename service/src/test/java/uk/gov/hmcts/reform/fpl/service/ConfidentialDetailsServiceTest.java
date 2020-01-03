package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.*;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ConfidentialPartyType.getCaseDataKeyFromClass;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class ConfidentialDetailsServiceTest {
    private static final String CONFIDENTIAL = "Yes";
    private static final String NOT_CONFIDENTIAL = "NO";

    @Autowired
    private ObjectMapper mapper;

    private ConfidentialDetailsService service;

    @BeforeEach
    void before() {
        service = new ConfidentialDetailsService(mapper);
    }

    @Test
    void shouldAddToListWhenConfidentialDetailsForChild() {
        List<Element<Child>> children = ImmutableList.of(childElement(CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldNotAddToListWhenChildrenIsEmptyList() {
        List<Element<Child>> children = ImmutableList.of();

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForChild() {
        List<Element<Child>> children = ImmutableList.of(
            childElement(CONFIDENTIAL),
            childElement(NOT_CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            service.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).hasSize(1).containsExactly(children.get(0));
    }

    @Test
    void shouldAddToListWhenConfidentialDetailsForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElement(CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldNotAddToListWhenRespondentsIsEmptyList() {
        List<Element<Respondent>> respondents = ImmutableList.of();

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElement(CONFIDENTIAL),
            respondentElement(NOT_CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            service.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).hasSize(1).containsExactly(respondents.get(0));
    }

    @Test
    void shouldAddConfidentialDetailsToCaseDetailsWhenClassExists() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        List<Element<Respondent>> confidentialDetails = ImmutableList.of(
            respondentElement(CONFIDENTIAL));

        service.addConfidentialDetailsToCaseDetails(caseDetails, confidentialDetails, Respondent.class);

        assertThat(caseDetails.getData()).containsKeys(getCaseDataKeyFromClass(Respondent.class));
    }

    @Test
    void shouldThrowExceptionWhenClassIsNotConfidential() {
        CaseDetails caseDetails = CaseDetails.builder().data(new HashMap<>()).build();
        List<Element<Applicant>> confidentialDetails = emptyList();

        service.addConfidentialDetailsToCaseDetails(caseDetails, confidentialDetails, Applicant.class);

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
