import {systemUpdateUser,privateSolicitorOrgUser} from '../settings/user-credentials';
import {urlConfig} from '../settings/urls';


import axios from 'axios';
import * as qs from 'qs';
import lodash from 'lodash';

 export const  getAccessToken = async ({user}: { user: any }) => {
        try {
            let axiosConfig = {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            };
            let url = `${urlConfig.idamUrl}/loginUser?username=${user.email}&password=${user.password}`;
            return await axios.post(url, qs.stringify(axiosConfig));
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.log(error.status)
                console.error(error.response);
            } else {
                console.error(error);
            }
        }
    }

   export const createCase = async (caseName = 'e2e UI Test', user: { email: string, password: string }) => {

        let res: any;
        const url = `${urlConfig.serviceUrl}/testing-support/case/create`;
        const data = {
            caseName: caseName,
        };
        try {
            res = await apiRequest(url, user, 'post', data);
        } catch (error) {
            console.log(error);
        }

        return res.id;
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
        const systemUserAuthToke = await getAccessToken({user: authUser});
        const requestConfig = {
            method: method,
            url: postURL,
            data: data,
            headers: {
                'Authorization': `Bearer ${systemUserAuthToke?.data.access_token}`,
                'Content-Type': 'application/json'
            },
        };
        try {
            return axios.request(requestConfig).then((res) => {
                return res.data;
            });
        } catch (error) {
            if (axios.isAxiosError(error)) {
                console.log(error.status);
                console.log(error.request);
                console.log(error.response);
            }

        }

    }

export const giveAccessToCase = async (caseID: string,user: {email: string ,password: string},role: string ) => {
    let data = JSON.stringify({
        'email': user.email,
        'password': user.password,
        'role': role
    });
    let postURL : string = `${urlConfig.serviceUrl}/testing-support/case/${caseID}/access`;
    try {
        let res = await apiRequest(postURL, systemUpdateUser, 'post', data);
    } catch (error) {
        console.log(error);
    }

}

