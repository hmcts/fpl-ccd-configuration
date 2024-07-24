package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiApplicant;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper.getCafcassApiAddress;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiApplicantsConverterTest extends CafcassApiConverterTestBase{
    private static final LocalAuthority LA_1 = LocalAuthority.builder()
        .name("Applicant 1 name")
        .email("applicant1@test.com")
        .phone("0123456789")
        .address(Address.builder().addressLine1("applicant 1 address").build())
        .designated(YesNo.YES.getValue())
        .colleagues(wrapElements(
            Colleague.builder()
                .role(ColleagueRole.SOLICITOR)
                .title("applicant 1 colleague")
                .email("colleague1@test.com")
                .fullName("colleague 1")
                .mainContact(YesNo.YES.getValue())
                .notificationRecipient(YesNo.YES.getValue())
                .build()))
        .build();
    private static final Element<LocalAuthority> LA_1_ELEMENT = element(LA_1);

    private static final LocalAuthority LA_2 = LocalAuthority.builder()
        .name("Applicant 2 name")
        .email("applicant2@test.com")
        .phone("0123456789")
        .address(Address.builder().addressLine1("applicant 2 address").build())
        .designated(YesNo.NO.getValue())
        .colleagues(wrapElements(
            Colleague.builder()
                .role(ColleagueRole.OTHER)
                .title("applicant 2 colleague")
                .email("colleague2@test.com")
                .fullName("colleague 2")
                .mainContact(YesNo.NO.getValue())
                .notificationRecipient(YesNo.NO.getValue())
                .build()))
        .build();
    private static final Element<LocalAuthority> LA_2_ELEMENT = element(LA_2);

    CafcassApiApplicantsConverterTest() {
        super(new CafcassApiApplicantsConverter());
    }

    @Test
    void shouldConvertAllApplicant() {
        CaseData caseData = CaseData.builder()
            .localAuthorities(List.of(LA_1_ELEMENT, LA_2_ELEMENT))
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .applicants(List.of(
                CafcassApiApplicant.builder()
                    .id(LA_1_ELEMENT.getId().toString())
                    .name("Applicant 1 name")
                    .email("applicant1@test.com")
                    .phone("0123456789")
                    .address(getCafcassApiAddress(LA_1.getAddress()))
                    .designated(true)
                    .colleagues(List.of(
                        CafcassApiColleague.builder()
                            .role(ColleagueRole.SOLICITOR.toString())
                            .title("applicant 1 colleague")
                            .email("colleague1@test.com")
                            .fullName("colleague 1")
                            .mainContact(true)
                            .notificationRecipient(true)
                            .build()))
                    .build(),
                CafcassApiApplicant.builder()
                    .id(LA_2_ELEMENT.getId().toString())
                    .name("Applicant 2 name")
                    .email("applicant2@test.com")
                    .phone("0123456789")
                    .address(getCafcassApiAddress(LA_2.getAddress()))
                    .designated(false)
                    .colleagues(List.of(
                        CafcassApiColleague.builder()
                            .role(ColleagueRole.OTHER.toString())
                            .title("applicant 2 colleague")
                            .email("colleague2@test.com")
                            .fullName("colleague 2")
                            .mainContact(false)
                            .notificationRecipient(false)
                            .build()))
                    .build()))
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldBuildEmptyListIfNoApplicant() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder().applicants(List.of()).build();

        testConvert(CaseData.builder().localAuthorities(null).build(), expected);
        testConvert(CaseData.builder().localAuthorities(List.of()).build(), expected);
    }
}
