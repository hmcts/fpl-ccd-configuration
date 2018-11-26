//package uk.gov.hmcts.reform.fpl.service;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
//import uk.gov.hmcts.reform.ccd.client.model.UserId;
//import uk.gov.hmcts.reform.fpl.config.LocalAuthorityUserLookupConfiguration;
//
//import java.util.List;
//
//import static org.mockito.BDDMockito.given;
//
//@ExtendWith(SpringExtension.class)
//class LocalAuthorityUserServiceTest {
//
//    private static final String AUTH_TOKEN = "Bearer token";
//    private static final String SERVICE_AUTH_TOKEN = "Bearer service token";
//    private static final String JURISDICTION = "PUBLICLAW";
//    private static final String CASE_TYPE = "Shared_Storage_DRAFTType";
//    private static final UserId USER_ID = new UserId("2");
//
//    @Mock
//    private CaseAccessApi caseAccessApi;
//
//    @Mock
//    private LocalAuthorityUserLookupConfiguration localAuthorityUserLookupConfiguration;
//
//    @InjectMocks
//    private LocalAuthorityUserService localAuthorityUserService;
//
//    @Test
//    void shouldReadUserLookUpTable() {
//        given(localAuthorityUserLookupConfiguration.getLookupTable()).willReturn(
//            ImmutableMap.<String, List<String>>builder()
//                .put("EX", ImmutableList.<String>builder().add("1").add("2").build())
//                .build()
//        );
//
//        localAuthorityUserService.grantUserAccess()
//
//        given(caseAccessApi.grantAccessToCase(
//            AUTH_TOKEN,
//            SERVICE_AUTH_TOKEN,
//            "1",
//            JURISDICTION,
//            CASE_TYPE,
//            "1",
//            USER_ID)).willReturn();
//
//    }
//}
