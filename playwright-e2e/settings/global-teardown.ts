import {
    deleteRoleAssignments,
    fetchOrganisationUsers,
    fetchS2SToken,
    getAccessToken,
    queryRoleAssignments
} from "../utils/api-helper";
import {systemUpdateUser} from "./user-credentials";
import {getDateBeforeToday} from "../utils/document-format-helper";
import {testConfig} from "./test-config";
import { test as teardown } from '@playwright/test';


teardown('delete AM Role', async ({ }) => {
    console.log('deleting AM Role...');
    let userIds: any[] = [];
    const roleAssignments: string[] = testConfig.teardownAMRoleAssignments;
    const validAt = getDateBeforeToday( Number(testConfig.daysOlderThan) ).toISOString();
    let deleted = '0';
    let recordsFetched = '0';

    // query the AM roles for the users
    const fplServiceAuthToken = fetchS2SToken('fpl_case_service');
    const CCDServiceAuthToken = fetchS2SToken('ccd_data');
    const userBearerToken = await getAccessToken({user: systemUpdateUser});

    userIds = await fetchOrganisationUsers('W9V61CP', fplServiceAuthToken);

    recordsFetched = await queryRoleAssignments(userIds, roleAssignments, validAt, userBearerToken, CCDServiceAuthToken);

    if (recordsFetched != '0') {
        deleted = await deleteRoleAssignments(userIds, roleAssignments, validAt, userBearerToken, CCDServiceAuthToken);
    } else {
        console.log('There are no role assignments to delete');
    }
    if (recordsFetched === deleted) {
        console.log('Global teardown: role assignments deleted : ' + deleted + ' successfully');
    }
});
