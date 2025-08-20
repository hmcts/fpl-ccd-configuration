import {deleteRoleAssignments, fetchOrganisationUsers, getAccessToken, queryRoleAssignments} from "../utils/api-helper";
import {systemUpdateUser} from "./user-credentials";
import {getDateWeekBeforeToday} from "../utils/document-format-helper";
import {ServiceAuthUtils} from "@hmcts/playwright-common";
import {ServiceTokenParams} from "@hmcts/playwright-common/dist/utils/service-auth.utils";

export default async () => {

    let userIds: any[] = [];
    let roleAssignments: any[] = ["[LASOLICITOR]",
        "[SOLICITORA]",
        "[CHILDSOLICITORA]"];
    let validAt = getDateWeekBeforeToday().toISOString();
    let deleted = '0';
    let recordsFetched = '0';

    // query the AM roles for the users
    const serviceAuth = new ServiceAuthUtils();
    const fplServiceAuthToken = await serviceAuth.retrieveToken({microservice: 'fpl_case_service'} as ServiceTokenParams);
    const CCDServiceAuthToken = await serviceAuth.retrieveToken({microservice: 'ccd_data'} as ServiceTokenParams);
    const userBearerToken = await getAccessToken({user: systemUpdateUser});

    userIds = await fetchOrganisationUsers('W9V61CP', fplServiceAuthToken);
    // console.log('Global teardown: userids fetched', userIds);

    recordsFetched = await queryRoleAssignments(userIds, roleAssignments, validAt, userBearerToken?.data.access_token, CCDServiceAuthToken);
    //  console.log('Global teardown: role assignments fetched', recordsFetched);


    if (recordsFetched != '0') {
        deleted = await deleteRoleAssignments(userIds, roleAssignments, validAt, userBearerToken?.data.access_token, CCDServiceAuthToken);
    } else {
        console.log('There are no role assignments to delete');
    }

    if (recordsFetched === deleted) {
        console.log('Global teardown: role assignments deleted' + deleted + 'successfully');
    }

};
