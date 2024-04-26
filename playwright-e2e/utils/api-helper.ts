import {systemUpdateUser,privateSolicitorOrgUser} from '../settings/user-credentials';
import {urlConfig} from '../settings/urls';


import axios from 'axios';
import * as qs from 'qs';
import * as lodash from 'lodash'

export class Apihelp {

  async getAccessToken({user}: { user: any }) {
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
  async createCase(caseName = 'e2e UI Test', user: { email: string, password: string }) {
    let res: object;
    const url = new URL(await this.getServiceUrl() + '/case/create').href;
    const data = {
      caseName: caseName,
    };
    try {
      res = await this.apiRequest(url, user, 'post', data);
    } catch (error) {
      console.log(error);
    }
    // @ts-ignore
      return res.id;
  }
  async updateCase(caseName = 'e2e Test', caseID: string, caseData: any = {} ) {
    //This can be moved to before test hook to as same document URL will be used for all test data
    //replace the documents placeholder with document url
    let docDetail = await this.apiRequest(new URL(await this.getServiceUrl() + '/test-document').href, systemUpdateUser);
    let docParameter = {
      TEST_DOCUMENT_URL: docDetail.document_url,
      TEST_DOCUMENT_BINARY_URL: docDetail.document_binary_url
    };
    const dateTime = new Date().toISOString();
    caseData.caseData.caseName = caseName;
    caseData.caseData.dateSubmitted = dateTime.slice(0, 10);
    caseData.caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);
    let data = lodash.template(JSON.stringify(caseData))(docParameter);
    let postURL = new URL(await this.getServiceUrl() + '/case/populate/' + `${caseID}`);
    try {
      let res = await this.apiRequest(postURL.href, systemUpdateUser, 'post', data);
    } catch (error) {
      console.log(error);
    }
  }
  async giveAccessToCase(caseID: string,user: {email: string ,password: string},role: string ){
    let data = JSON.stringify({
        'email': user.email,
        'password': user.password,
        'role': role
    });
    let postURL : string = `${urlConfig.serviceUrl}/testing-support/case/${caseID}/access`;
    try {
      let res = await this.apiRequest(postURL, systemUpdateUser, 'post', data);
    } catch (error) {
      console.log(error);
    }

    }

  async getServiceUrl()  {
     const url1 = new URL(`${urlConfig.serviceUrl}` );
     url1.pathname = 'testing-support';
     return url1.href;
  }


  async apiRequest(postURL: string, authUser: any, method: string = 'get', data: any = {}) {
    const systemUserAuthToke = await this.getAccessToken({user: authUser});
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
}
