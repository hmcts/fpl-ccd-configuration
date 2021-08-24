package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.events.CaseTransferred;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityAdded;
import uk.gov.hmcts.reform.fpl.events.SecondaryLocalAuthorityRemoved;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.service.ApplicantLocalAuthorityService;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityChangedContentProvider;
import uk.gov.hmcts.reform.fpl.testingsupport.email.EmailTemplateTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ccd.model.OrganisationPolicy.organisationPolicy;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASHARED;
import static uk.gov.hmcts.reform.fpl.enums.CaseRole.LASOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.ChildGender.BOY;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.EmailContent.emailContent;
import static uk.gov.hmcts.reform.fpl.testingsupport.email.SendEmailResponseAssert.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;

@ContextConfiguration(classes = {
    LocalAuthorityChangedHandler.class, LocalAuthorityChangedContentProvider.class, EmailNotificationHelper.class,
    CaseUrlService.class
})
@MockBeans({@MockBean(FeatureToggleService.class)})
class LocalAuthorityChangedHandlerEmailTemplateTest extends EmailTemplateTest {

    @MockBean
    private ApplicantLocalAuthorityService service;

    @Autowired
    private LocalAuthorityChangedHandler underTest;

    private final LocalAuthority designatedLocalAuthority = LocalAuthority.builder()
        .id("ORG1")
        .name("Designated LA")
        .email("designated@test.com")
        .designated("Yes")
        .build();

    private final LocalAuthority secondaryLocalAuthority = LocalAuthority.builder()
        .id("ORG2")
        .name("Scondary LA")
        .email("secondary@test.com")
        .designated("No")
        .build();

    private final OrganisationPolicy designatedPolicy = organisationPolicy("ORG1", "Designated LA", LASOLICITOR);
    private final OrganisationPolicy secondaryPolicy = organisationPolicy("ORG2", "Secondary LA", LASHARED);

    @BeforeEach
    void init() {
        when(service.getDesignatedLocalAuthority(any())).thenReturn(designatedLocalAuthority);
        when(service.getSecondaryLocalAuthority(any())).thenReturn(Optional.of(secondaryLocalAuthority));
    }

    @Test
    void notifyDesignatedLocalAuthorityAboutNewSecondaryLocalAuthority() {

        final CaseData caseData = CaseData.builder()
            .id(10L)
            .caseName("Test case")
            .localAuthorityPolicy(designatedPolicy)
            .sharedLocalAuthorityPolicy(secondaryPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .children1(List.of(testChild("Alex", "White", BOY, now())))
            .build();

        underTest.notifyDesignatedLocalAuthority(new SecondaryLocalAuthorityAdded(caseData));

        assertThat(response())
            .hasSubject("FPL case access given, White")
            .hasBody(emailContent()
                .line("Dear Designated LA")
                .line()
                .callout("Test case 10")
                .line()
                .line("This case has now been shared with Secondary LA.")
                .line()
                .line("Your organisation is still the designated local authority.")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifySecondaryLocalAuthorityAboutGettingCaseAccess() {

        final CaseData caseData = CaseData.builder()
            .id(10L)
            .caseName("Test case")
            .localAuthorityPolicy(designatedPolicy)
            .sharedLocalAuthorityPolicy(secondaryPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .build();

        underTest.notifySecondaryLocalAuthority(new SecondaryLocalAuthorityAdded(caseData));

        assertThat(response())
            .hasSubject("FPL case access given")
            .hasBody(emailContent()
                .line("Dear Secondary LA")
                .line()
                .callout("Test case 10")
                .line()
                .line("This family public law case has been shared with you and Designated LA.")
                .line()
                .line("Your organisation’s case access administrator should now assign the case to you. "
                    + "They can do this at https://manage-org.platform.hmcts.net")
                .line()
                .line("When the case is assigned to you, you can:")
                .line("* gain case access")
                .line("* give access to others in your organisation.")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Test
    void notifySecondaryLocalAuthorityAboutAccessRemoval() {

        final CaseData caseDataBefore = CaseData.builder()
            .id(10L)
            .caseName("Test case")
            .localAuthorityPolicy(designatedPolicy)
            .sharedLocalAuthorityPolicy(secondaryPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .sharedLocalAuthorityPolicy(null)
            .localAuthorities(wrapElements(designatedLocalAuthority))
            .build();

        underTest.notifySecondaryLocalAuthority(new SecondaryLocalAuthorityRemoved(caseData, caseDataBefore));

        assertThat(response())
            .hasSubject("FPL case access revoked")
            .hasBody(emailContent()
                .line("Dear Secondary LA")
                .line()
                .callout("Test case 10")
                .line()
                .line("This case has stopped being shared with you.")
                .line()
                .line("Your organisation no longer has online access to the case files.")
                .line()
                .line("HM Courts & Tribunals Service")
                .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                    + "contactfpl@justice.gov.uk")
            );
    }

    @Nested
    class CaseTransfer {

        final CaseData caseDataBefore = CaseData.builder()
            .id(10L)
            .caseName("Test case")
            .localAuthorityPolicy(designatedPolicy)
            .sharedLocalAuthorityPolicy(secondaryPolicy)
            .localAuthorities(wrapElements(designatedLocalAuthority, secondaryLocalAuthority))
            .children1(List.of(testChild("Alex", "White", BOY, now())))
            .build();

        final CaseData caseData = caseDataBefore.toBuilder()
            .localAuthorityPolicy(secondaryPolicy)
            .localAuthorities(wrapElements(secondaryLocalAuthority.toBuilder().designated("Yes").build()))
            .build();

        @Test
        void notifyPreviousDesignatedLocalAuthorityAboutCaseTransfer() {

            underTest.notifyPreviousDesignatedLocalAuthority(new CaseTransferred(caseData, caseDataBefore));

            assertThat(response())
                .hasSubject("Family public law case transfer, White")
                .hasBody(emailContent()
                    .line("Dear Designated LA")
                    .line()
                    .callout("Test case 10")
                    .line()
                    .line("This case has now been transferred to Secondary LA.")
                    .line()
                    .line("Your organisation will no longer have access to the case.")
                    .line()
                    .line("HM Courts & Tribunals Service")
                    .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
                );
        }

        @Test
        void notifyNewDesignatedLocalAuthorityAboutCaseTransfer() {

            underTest.notifyNewDesignatedLocalAuthority(new CaseTransferred(caseData, caseDataBefore));

            assertThat(response())
                .hasSubject("Family public law case transfer, White")
                .hasBody(emailContent()
                    .line("Dear Secondary LA")
                    .line()
                    .callout("Test case 10")
                    .line()
                    .line("This case has transferred to you from Designated LA.")
                    .line()
                    .line("This means your local authority is now the designated applicant.")
                    .line()
                    .line("If you don't already have access to the case,"
                        + " your organisation’s case access administrator should assign the case to you."
                        + " They can do this at https://manage-org.platform.hmcts.net")
                    .line()
                    .line("When the case is assigned to you,"
                        + " you can view case details by signing into http://fake-url/cases/case-details/10")
                    .line()
                    .line("HM Courts & Tribunals Service")
                    .end("Do not reply to this email. If you need to contact us, call 0330 808 4424 or email "
                        + "contactfpl@justice.gov.uk")
                );
        }
    }
}
