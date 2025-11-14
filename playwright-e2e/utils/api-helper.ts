import {systemUpdateUser} from '../settings/user-credentials';
import {urlConfig} from '../settings/urls';
import {ServiceAuthUtils} from "@hmcts/playwright-common";
import {APIRequestContext, request} from "@playwright/test";
import axios from 'axios';
import * as qs from 'qs';
import lodash from 'lodash';
import {ServiceTokenParams} from "@hmcts/playwright-common/dist/utils/service-auth.utils";

export const  getAccessToken = async ({user}: { user: { email: string; password: string } }) => {
    try {
        const axiosConfig = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        };
        const url = `${urlConfig.idamUrl}/loginUser?username=${user.email}&password=${user.password}`;
        return await axios.post(url, qs.stringify(axiosConfig));
    } catch (error) {
        if (axios.isAxiosError(error)) {
            console.error(error.response?.status, error.response?.data);
        } else {
            console.error(error);
        }
        throw error;
    }
};

export const  getServiceAuthToken = async () => {
   const params: ServiceTokenParams = { microservice: 'fpl_case_service'}
    const serviceAuth = new ServiceAuthUtils();
    return serviceAuth.retrieveToken(params);
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

export const updateCase = async (caseName = 'e2e Test', caseID: string, caseDataJson: any) => {
    //This can be moved to before test hook to as same document URL will be used for all test data
    //replace the documents placeholder with document url
    let docDetail = await apiRequest(urlConfig.serviceUrl + '/testing-support/test-document', systemUpdateUser);
    let docParameter = {
        TEST_DOCUMENT_URL: docDetail.document_url,
        TEST_DOCUMENT_BINARY_URL: docDetail.document_binary_url

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
    } catch (error) {
        console.log(error);
    }
}

export const apiRequest = async (postURL: string, authUser: any, method: string = 'get', data: any = {}) => {
    const systemUserAuthToken = await getAccessToken({user: authUser});
    const requestConfig = {
        method: method,
        url: postURL,
        data: data,
        headers: {
            'Authorization': `Bearer ${systemUserAuthToken?.data.access_token}`,
            'Content-Type': 'application/json'
        },
    };
    try {
        return axios.request(requestConfig).then((res) => {
            return res.data;
        });
    } catch (error) {
        if (axios.isAxiosError(error)) {
            console.error(error.response?.status, error.response?.data);
        } else {
            console.error(error);
        }
        throw error;

    }

}

export const giveAccessToCase = async (caseID: string,user: {email: string ,password: string},role: string ) => {
    const data = JSON.stringify({
        'email': user.email,
        'password': user.password,
        'role': role
    });
    const postURL : string = `${urlConfig.serviceUrl}/testing-support/case/${caseID}/access`;
    try {
        const res = await apiRequest(postURL, systemUpdateUser, 'post', data);
    } catch (error) {
        console.error(error);
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


