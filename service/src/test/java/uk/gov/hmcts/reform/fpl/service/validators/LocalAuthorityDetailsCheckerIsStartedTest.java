package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityDetailsChecker.class, LocalValidatorFactoryBean.class})
class LocalAuthorityDetailsCheckerIsStartedTest {

    @Autowired
    private LocalAuthorityDetailsChecker underTest;

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnFalseWhenNoLocalAuthorities(List<Element<LocalAuthority>> localAuthorities) {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities)
            .build();

        assertThat(underTest.isStarted(caseData)).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAtLeastOneLocalAuthorityIsPresent() {
        final CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder().build()))
            .build();

        assertThat(underTest.isStarted(caseData)).isTrue();
    }
}
