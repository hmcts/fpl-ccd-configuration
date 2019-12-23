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
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class ConfidentialDetailsServiceTest {
    private static final String CONFIDENTIAL = "Yes";
    private static final String NOT_CONFIDENTIAL = "NO";

    @Autowired
    private ObjectMapper mapper;

    private ConfidentialDetailsService confidentialDetailsService;

    @BeforeEach
    void before() {
        confidentialDetailsService = new ConfidentialDetailsService(mapper);
    }

    @Test
    void shouldAddToListWhenConfidentialDetailsForChild() {
        List<Element<Child>> children = ImmutableList.of(childElement(CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldNotAddToListWhenChildrenIsEmptyList() {
        List<Element<Child>> children = ImmutableList.of();

        List<Element<Child>> confidentialChildren =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).isEqualTo(children);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForChild() {
        List<Element<Child>> children = ImmutableList.of(
            childElement(CONFIDENTIAL),
            childElement(NOT_CONFIDENTIAL));

        List<Element<Child>> confidentialChildren =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Child.class, children);

        assertThat(confidentialChildren).hasSize(1).containsExactly(children.get(0));
    }

    @Test
    void shouldAddToListWhenConfidentialDetailsForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(respondentElement(CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldNotAddToListWhenRespondentsIsEmptyList() {
        List<Element<Respondent>> respondents = ImmutableList.of();

        List<Element<Respondent>> confidentialRespondents =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).isEqualTo(respondents);
    }

    @Test
    void shouldOnlyAddConfidentialDetailsToListOnlyWhenAnsweredYesForRespondent() {
        List<Element<Respondent>> respondents = ImmutableList.of(
            respondentElement(CONFIDENTIAL),
            respondentElement(NOT_CONFIDENTIAL));

        List<Element<Respondent>> confidentialRespondents =
            confidentialDetailsService.addPartyMarkedConfidentialToList(Respondent.class, respondents);

        assertThat(confidentialRespondents).hasSize(1).containsExactly(respondents.get(0));
    }

    private Element<Child> childElement(String hidden) {
        return Element.<Child>builder()
            .value(Child.builder()
                .party(ChildParty.builder()
                    .firstName("James")
                    .detailsHidden(hidden)
                    .address(Address.builder()
                        .addressLine1("James' House")
                        .build())
                    .build())
                .build())
            .build();
    }

    private Element<Respondent> respondentElement(String hidden) {
        return Element.<Respondent>builder()
            .value(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("James")
                    .contactDetailsHidden(hidden)
                    .address(Address.builder()
                        .addressLine1("James' House")
                        .build())
                    .build())
                .build())
            .build();
    }
}
