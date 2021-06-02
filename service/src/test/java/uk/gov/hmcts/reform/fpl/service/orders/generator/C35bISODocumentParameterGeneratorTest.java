package uk.gov.hmcts.reform.fpl.service.orders.generator;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.service.ChildrenService;

class C35bISODocumentParameterGeneratorTest {

    @Mock
    private ChildrenService childrenService;

    @Mock
    private LocalAuthorityNameLookupConfiguration laNameLookup;

    @InjectMocks
    private C35bISODocumentParameterGenerator underTest;

}
