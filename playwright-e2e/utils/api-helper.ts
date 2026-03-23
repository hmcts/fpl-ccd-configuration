import {systemUpdateUser} from '../settings/user-credentials';
import {urlConfig} from '../settings/urls';
import {testConfig} from '../settings/test-config';
import {IdamTokenParams} from "@hmcts/playwright-common/dist/utils/idam.utils";
import {IdamUtils, withRetry} from "@hmcts/playwright-common";
import {isRetryableError} from "@hmcts/playwright-common/dist/utils/retry.utils.js"
import {APIRequestContext, request} from "@playwright/test";
import axios from 'axios';
import lodash from 'lodash';


export const  getAccessToken = async ({user}: { user: { email: string; password: string } }) => {

const idamTokenParams: IdamTokenParams = {
    clientId: "fpl_case_service",
    clientSecret: testConfig.idamClientSecret,
    grantType: "password",
    scope: "openid profile roles",
    username: user.email,
    password: user.password

}
    const idamUtils = new IdamUtils();
    try {
        const token = await idamUtils.generateIdamToken(idamTokenParams);
        return token;
    } catch (error) {
        console.error('Error generating IDAM token:', error);
        throw error;
    }

export const  getServiceAuthToken = async () => {
   const params: ServiceTokenParams = { microservice: 'fpl_case_service'}
    const serviceAuth = new ServiceAuthUtils();
    return await serviceAuth.retrieveToken(params);

};


export const createCase = async (caseName = 'e2e UI Test', user: { email: string, password: string }) => {

    const url = `${urlConfig.serviceUrl}/testing-support/case/create`;
    const data = {
        caseName: caseName,
    };
    try {
      const  res = await apiRequest(url, user, 'post', data);
        return res.id;
    } catch (error) {
        console.error(error);
    }
}

export async function getDocParameter() {
    let docDetail = await apiRequest(urlConfig.serviceUrl + '/testing-support/test-document', systemUpdateUser);
    return {
        TEST_DOCUMENT_URL: docDetail.document_url,
        TEST_DOCUMENT_BINARY_URL: docDetail.document_binary_url
    };
}
export const updateCase = async (caseName = 'e2e Test', caseID: string, caseDataJson: any) => {
    let docParameter = {
        TEST_DOCUMENT_URL: process.env.TEST_DOCUMENT_URL,
        TEST_DOCUMENT_BINARY_URL: process.env.TEST_DOCUMENT_BINARY_URL

    };
    const dateTime = new Date().toISOString();
    caseDataJson.caseData.caseName = caseName;

    caseDataJson.caseData.dateSubmitted = dateTime.slice(0, 10);
    caseDataJson.caseData.lastGenuineUpdateTime = dateTime

    caseDataJson.caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);
    let data = lodash.template(JSON.stringify(caseDataJson))(docParameter);
    let postURL = `${urlConfig.serviceUrl}/testing-support/case/populate/${caseID}`;
    try {
        await apiRequest(postURL, systemUpdateUser, 'post', data);
        return true;

    } catch (error) {
        console.log(error);
        return false;
    }
}
export const fetchAccessToken = async (user: { email: string; password: string; }) => {
    const envKey = user.email.toUpperCase().split('@')[0] + 'AUTH';
    let accessToken = process.env[envKey];
    if (accessToken === undefined) {
        accessToken = await getAccessToken({user});
        process.env[envKey] = accessToken;
    }
    return accessToken;
}
const apiRequest = async (postURL: string, user: { email: string,password:string }, method: string = 'get', data: any = {}) => {

    const accessToken = await fetchAccessToken(user);
    const requestConfig = {
        method,
        url: postURL,
        data,
        headers: {
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        },
        timeout: 30000,
    };
    const exec = async()=> {
        return await axios.request({...requestConfig});
    };
    try {

        let attempts =2;
        const response = attempts > 1
            ? await withRetry(exec, attempts, 300, 2000, 15000, isRetryableError)
            : await exec();
      //  const res = await axios.request(requestConfig);
        if (response.status === 200) return response.data;
    } catch (error) {
        if (axios.isAxiosError(error)) {
            console.error(error.response?.status, error.response?.data);
        } else {
            console.error(error);
        }
        throw error;
    }
};


export const giveAccessToCase = async (caseID: string,user: {email: string ,password: string},role: string ) => {
    const data = JSON.stringify({
        'email': user.email,
        'password': user.password,
        'role': role
    });
    const postURL : string = `${urlConfig.serviceUrl}/testing-support/case/${caseID}/access`;
    try {
        const res = await apiRequest(postURL, systemUpdateUser, 'post', data);
        return true
    } catch (error) {
        console.error(error);
        return false;
    }
}

export async function fetchOrganisationUsers(orgId:string,serviceAuthToken :string) {
    const requestContext = await request.newContext();
    const postURL= `${urlConfig.manageOrgServiceUrl}/refdata/internal/v2/organisations/users`;
    const response = await requestContext.post( postURL ,
        {
            headers: {
                'ServiceAuthorization': serviceAuthToken,
                'Content-Type': 'application/json',
            },
            data: {
                organisationIdentifiers: [orgId],
            },
        }
    );
    if (!response.ok()) {
        throw new Error(`Failed to fetch organisation users: ${response.status()} ${response.statusText()}`);
    }
    else
    {
        const responseBody = await response.json();
        return responseBody.organisationInfo[0].users.map((user: any) => user.userIdentifier);
    }


}


export async function queryRoleAssignments(
    actorIds: string[],
    roleNames: string[],
    validAt: string,
    bearerToken: string,
    serviceAuthToken: string
) {
    const requestContext: APIRequestContext = await request.newContext();
    const postURL = `${urlConfig.accessManagementUrl}/query`;
    const response = await requestContext.post(
        postURL,
        {
            headers: {
                'Authorization': `Bearer ${bearerToken}`,
                'ServiceAuthorization': `Bearer ${serviceAuthToken}`,
                'Content-Type': 'application/json',
            },
            data: {
                actorId: actorIds,
                roleName: roleNames,
                validAt: validAt,
            },
        }
    );
    if (!response.ok()) {
        throw new Error(`Failed to query role assignments: ${response.status()} ${response.statusText()}`);
    }
    const totalRecords = await response.headers()['total-records'] || response.headers()['Total-Records'];
    console.log('Total-Records:', totalRecords);
    return  totalRecords ;
}

export async function deleteRoleAssignments(
    actorIds: string[],
    roleNames: string[],
    validAt: string,
    bearerToken: string,
    serviceAuthToken: string
) {
    const requestContext: APIRequestContext = await request.newContext();
    const postURL = `${urlConfig.accessManagementUrl}/query/delete`;
    const response = await requestContext.post(
        postURL,
        {
            headers: {
                'Authorization': `Bearer ${bearerToken}`,
                'ServiceAuthorization': `Bearer ${serviceAuthToken}`,
                'Content-Type': 'application/json',
            },
            data: {
                queryRequests: [
                    {
                        actorId: actorIds,
                        roleName: roleNames,
                        validAt: validAt,
                    }
                ]
            },
        }
    );
    if (!response.ok()) {
        throw new Error(`Failed to delete role assignments: ${response.status()} ${response.statusText()}`);

    }
    const deletedRecords = await  response.headers()['total-records'] || response.headers()['Total-Records'];
    console.log('deleted-Records:', deletedRecords);
     return deletedRecords;
}

export async function assignAMJudicialRole(caseID: string, judicialUser: { email: string; password: string; }) {
    const serviceAuthToken = await getServiceAuthToken();
    const bearerToken = await getAccessToken({user: systemUpdateUser}).then(res => `Bearer ${res.data.access_token}`);
    const judgeID = await getIdamUserId(judicialUser);
    const assignerId = await getIdamUserId(systemUpdateUser);
    const roleStartTime = new Date().toISOString();
    const headers = {
        'ServiceAuthorization': serviceAuthToken,
        'Authorization': bearerToken,
        'Content-Type': 'application/json'
    };
    const data = {
        "roleRequest": {
            "assignerId": assignerId,
            "replaceExisting": false,
            "process": "fpl-case-service",
            "reference": "fpl-case-role-assignment"
        },
        "requestedRoles": [
            {
                "attributes": {
                    "jurisdiction": "PUBLICLAW",
                    "caseType": "CARE_SUPERVISION_EPO",
                    "caseId": caseID,
                    "substantive": "Y"
                },
                "actorIdType": "IDAM",
                "status": "CREATE_REQUESTED",
                "actorId": judgeID,
                "beginTime": roleStartTime,
                "classification": "PUBLIC",
                "grantType": "SPECIFIC",
                "roleCategory": "JUDICIAL",
                "roleName": "hearing-judge",
                "roleType": "CASE",
                "readOnly": false
            },
            {
                "attributes": {
                    "jurisdiction": "PUBLICLAW",
                    "caseType": "CARE_SUPERVISION_EPO",
                    "caseId": caseID,
                    "substantive": "Y"
                },
                "actorIdType": "IDAM",
                "status": "CREATE_REQUESTED",
                "actorId": judgeID,
                "beginTime": roleStartTime,
                "classification": "PUBLIC",
                "grantType": "SPECIFIC",
                "roleCategory": "JUDICIAL",
                "roleName": "allocated-judge",
                "roleType": "CASE",
                "readOnly": false
            }
        ]
    };
    const requestContext: APIRequestContext = await request.newContext();
    const postURL = `${urlConfig.accessManagementUrl}`;
    try {
        const response = await requestContext.post(
            postURL,
            {
                headers: headers,
                data: data,
            }
        );
        if (response.statusText() === 'Created' || response.status() === 201) {
            return true
        }
        // handle response if needed
    } catch (error) {
        console.error('POST request failed:', error);
        throw error;
    }
}

export async function getIdamUserId(user: { email: string; password: string; }): Promise<any> {
    const requestContext: APIRequestContext = await request.newContext();
    const bearerToken = await getAccessToken({user}).then(res => `Bearer ${res.data.access_token}`);
    const url = `${urlConfig.idamUrl}/details`;

    const headers = {
        'Authorization': bearerToken,
        'Content-Type': 'application/json'
    };
    const response = await requestContext.get(url, {headers});
    if (!response.ok()) {
        throw new Error(`Failed to fetch user id: ${response.status()} ${response.statusText()}`);
    }
    try {
        const data = await response.json();
        return data.id;
    } catch (err) {
        console.error('Failed to parse user id response:', err);
        throw err;
    }

}
