package uk.gov.hmcts.reform.fpl.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Slf4j
public class QueryManagementUtils {

    private QueryManagementUtils() {

    }

    public static String getCurrentCollectionByUserService(CaseData caseData,
                                        UserService userService,
                                        Map<CaseRole, String> collectionMapping) {
        Set<CaseRole> currentUserRoles = userService.getCaseRoles(caseData.getId());
        log.info("Current logged-in user's case roles are: {}", currentUserRoles);

        CaseRole userQueryCollectionRole = currentUserRoles.stream()
            .filter(collectionMapping::containsKey)
            .findFirst()
            .orElse(null);

        return (userQueryCollectionRole != null) ? collectionMapping.get(userQueryCollectionRole) : null;
    }

    public static Map<String,Object> getQueryCollection(CaseDetails caseDetails, String queryCollection) {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.convertValue(
            caseDetails.getData().getOrDefault(queryCollection, null),
            new TypeReference<Map<String,Object>>() {}
        );
    }

    public static List<Element<Map<String,Object>>> getCaseMessages(Map<String,Object> collection) {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.convertValue(
            collection.getOrDefault("caseMessages", null),
            new TypeReference<List<Element<Map<String,Object>>>>() {}
        );
    }

    public static List<Map<String,Object>> getAllCaseMessages(CaseDetails caseDetails) {
        List<Map<String,Object>> caseMessages = new ArrayList<>();

        for (String collection : queryCollectionList) {
            if (caseDetails.getData().getOrDefault(collection, null) != null) {
                caseMessages.addAll(unwrapElements(getCaseMessages(getQueryCollection(caseDetails, collection))));
            }
        }

        return caseMessages;
    }

    public static Map<String,Object> getQueryByQueryId(CaseDetails caseDetails, String queryId) {
        List<Map<String,Object>> allQueries = getAllCaseMessages(caseDetails);

        return allQueries.stream()
            .filter(query -> query.getOrDefault("id", null).equals(queryId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No query found for queryId " + queryId));
    }

    public static Map<String,Object> getQueryResponseFromCaseDetails(CaseDetails caseDetailsBefore,
                                                                     CaseDetails caseDetailsAfter) {
        List<Map<String,Object>> caseMessagesBefore = getAllCaseMessages(caseDetailsBefore);
        List<Map<String,Object>> caseMessagesAfter = getAllCaseMessages(caseDetailsAfter);

        List<Map<String,Object>> newCaseMessages = caseMessagesAfter.stream()
            .filter(query -> !caseMessagesBefore.contains(query))
            .toList();

        return newCaseMessages.stream().findFirst().orElse(null);
    }

    public static Map<String,Object> getParentQueryFromResponse(CaseDetails caseDetails,
                                                                Map<String,Object> queryResponse) {
        String parentId = queryResponse.getOrDefault("parentId", null).toString();

        return parentId != null
            ? getQueryByQueryId(caseDetails, parentId)
            : null;
    }

    public static String getUserIdFromQuery(Map<String,Object> query) {
        return query != null ? query.getOrDefault("createdBy", null).toString() : null;
    }

    public static String getQueryDateFromQuery(Map<String,Object> query) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        return query != null
            ? LocalDateTime.parse(query.get("createdOn").toString(), formatter).toLocalDate().toString()
            : null;
    }

    public static Map<String,Object> getLatestQueryInCollection(CaseDetails caseDetails, String queryCollection) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

        Map<String,Object> collection = getQueryCollection(caseDetails, queryCollection);

        List<Element<Map<String,Object>>> caseMessages = getCaseMessages(collection);

        return unwrapElements(caseMessages).stream()
            .filter(caseMessage -> !caseMessage.containsKey("parentId")) //filtering out responses
            .max(Comparator.comparing(caseMessage -> LocalDateTime.parse(caseMessage.get("createdOn").toString(),
                formatter)))
            .orElse(null);
    }

    public static String getLatestQueryIDForCollection(CaseDetails caseDetails, String queryCollection) {
        Map<String,Object> latestQuery = getLatestQueryInCollection(caseDetails, queryCollection);

        return latestQuery != null ? latestQuery.getOrDefault("id", null).toString() : null;
    }

    public static void logQueryCollection(CaseDetails caseDetails, String collectionKey) {
        if (caseDetails.getData().containsKey(collectionKey)) {
            log.info("{} is present with value: {}", collectionKey, caseDetails.getData().get(collectionKey));
        } else {
            log.info("{} is not present on case data.", collectionKey);
        }
    }

    public static void logAllQueryCollections(CaseDetails caseDetails) {
        for (String collection : queryCollectionList) {
            logQueryCollection(caseDetails, collection);
        }
    }

    public static Map<CaseRole, String> getRoleToCollectionMapping() {
        Map<CaseRole, String> collectionMapping = new LinkedHashMap<>();
        collectionMapping.put(CaseRole.LASOLICITOR, "qmCaseQueriesCollectionLASol");
        collectionMapping.put(CaseRole.EPSMANAGING, "qmCaseQueriesCollectionEPSManaging");
        collectionMapping.put(CaseRole.LAMANAGING, "qmCaseQueriesCollectionLAManaging");
        collectionMapping.put(CaseRole.LABARRISTER, "qmCaseQueriesCollectionLABarrister");
        collectionMapping.put(CaseRole.LASHARED, "qmCaseQueriesCollectionLAShared");
        collectionMapping.put(CaseRole.BARRISTER, "qmCaseQueriesCollectionBarrister");
        collectionMapping.put(CaseRole.SOLICITOR, "qmCaseQueriesCollectionSolicitor");
        collectionMapping.put(CaseRole.SOLICITORA, "qmCaseQueriesCollectionSolicitorA");
        collectionMapping.put(CaseRole.SOLICITORB, "qmCaseQueriesCollectionSolicitorB");
        collectionMapping.put(CaseRole.SOLICITORC, "qmCaseQueriesCollectionSolicitorC");
        collectionMapping.put(CaseRole.SOLICITORD, "qmCaseQueriesCollectionSolicitorD");
        collectionMapping.put(CaseRole.SOLICITORE, "qmCaseQueriesCollectionSolicitorE");
        collectionMapping.put(CaseRole.SOLICITORF, "qmCaseQueriesCollectionSolicitorF");
        collectionMapping.put(CaseRole.SOLICITORG, "qmCaseQueriesCollectionSolicitorG");
        collectionMapping.put(CaseRole.SOLICITORH, "qmCaseQueriesCollectionSolicitorH");
        collectionMapping.put(CaseRole.SOLICITORI, "qmCaseQueriesCollectionSolicitorI");
        collectionMapping.put(CaseRole.SOLICITORJ, "qmCaseQueriesCollectionSolicitorJ");
        collectionMapping.put(CaseRole.CHILDSOLICITORA, "qmCaseQueriesCollectionChildSolA");
        collectionMapping.put(CaseRole.CHILDSOLICITORB, "qmCaseQueriesCollectionChildSolB");
        collectionMapping.put(CaseRole.CHILDSOLICITORC, "qmCaseQueriesCollectionChildSolC");
        collectionMapping.put(CaseRole.CHILDSOLICITORD, "qmCaseQueriesCollectionChildSolD");
        collectionMapping.put(CaseRole.CHILDSOLICITORE, "qmCaseQueriesCollectionChildSolE");
        collectionMapping.put(CaseRole.CHILDSOLICITORF, "qmCaseQueriesCollectionChildSolF");
        collectionMapping.put(CaseRole.CHILDSOLICITORG, "qmCaseQueriesCollectionChildSolG");
        collectionMapping.put(CaseRole.CHILDSOLICITORH, "qmCaseQueriesCollectionChildSolH");
        collectionMapping.put(CaseRole.CHILDSOLICITORI, "qmCaseQueriesCollectionChildSolI");
        collectionMapping.put(CaseRole.CHILDSOLICITORJ, "qmCaseQueriesCollectionChildSolJ");
        collectionMapping.put(CaseRole.CHILDSOLICITORK, "qmCaseQueriesCollectionChildSolK");
        collectionMapping.put(CaseRole.CHILDSOLICITORL, "qmCaseQueriesCollectionChildSolL");
        collectionMapping.put(CaseRole.CHILDSOLICITORM, "qmCaseQueriesCollectionChildSolM");
        collectionMapping.put(CaseRole.CHILDSOLICITORN, "qmCaseQueriesCollectionChildSolN");
        collectionMapping.put(CaseRole.CHILDSOLICITORO, "qmCaseQueriesCollectionChildSolO");
        collectionMapping.put(CaseRole.CAFCASSSOLICITOR, "qmCaseQueriesCollectionCafcassSol");
        return collectionMapping;
    }

    public static List<String> queryCollectionList = List.of(
        "qmCaseQueriesCollectionCafcass",
        "qmCaseQueriesCollectionLASol",
        "qmCaseQueriesCollectionEPSManaging",
        "qmCaseQueriesCollectionLAManaging",
        "qmCaseQueriesCollectionLABarrister",
        "qmCaseQueriesCollectionLAShared",
        "qmCaseQueriesCollectionBarrister",
        "qmCaseQueriesCollectionSolicitor",
        "qmCaseQueriesCollectionSolicitorA",
        "qmCaseQueriesCollectionSolicitorB",
        "qmCaseQueriesCollectionSolicitorC",
        "qmCaseQueriesCollectionSolicitorD",
        "qmCaseQueriesCollectionSolicitorE",
        "qmCaseQueriesCollectionSolicitorF",
        "qmCaseQueriesCollectionSolicitorG",
        "qmCaseQueriesCollectionSolicitorH",
        "qmCaseQueriesCollectionSolicitorI",
        "qmCaseQueriesCollectionSolicitorJ",
        "qmCaseQueriesCollectionChildSolA",
        "qmCaseQueriesCollectionChildSolB",
        "qmCaseQueriesCollectionChildSolC",
        "qmCaseQueriesCollectionChildSolD",
        "qmCaseQueriesCollectionChildSolE",
        "qmCaseQueriesCollectionChildSolF",
        "qmCaseQueriesCollectionChildSolG",
        "qmCaseQueriesCollectionChildSolH",
        "qmCaseQueriesCollectionChildSolI",
        "qmCaseQueriesCollectionChildSolJ",
        "qmCaseQueriesCollectionChildSolK",
        "qmCaseQueriesCollectionChildSolL",
        "qmCaseQueriesCollectionChildSolM",
        "qmCaseQueriesCollectionChildSolN",
        "qmCaseQueriesCollectionChildSolO",
        "qmCaseQueriesCollectionCafcassSol"
    );
}
