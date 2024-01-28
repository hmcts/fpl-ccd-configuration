import {systemUpdateUser,newSwanseaLocalAuthorityUserOne, swanseaUser} from '../settings/userCredentials';
import {UrlConfig} from '../settings/urls';


import axios from 'axios';
import qs from 'qs';
import lodash from 'lodash'

export class Apihelp {

  async getAccessToken(user) {
    try {
      let axiosConfig ={
        headers : {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      };
      let  url = `${UrlConfig.idamUrl}/loginUser?username=${user.email}&password=${user.password}`;
      return await axios.post(url,qs.stringify(axiosConfig));
    } catch (error) {
      if (axios.isAxiosError(error)) {
        console.log(error.status)
        console.error(error.response);
      } else {
        console.error(error);
      }
    }


  }
  async createCase(caseName='e2e UI Test'){

    let res : object;
    const url = `${UrlConfig.serviceUrl}/testing-support/case/create`;
    const data = {
            caseName : caseName,
         };
      res= await this.apiRequest(url,swanseaUser,'post',data);
     return res.id;
  }

  async updateCase(caseName = 'e2e Test',caseID: string,caseData: {} | undefined){

    //replace the documents placeholder with docuemnt url
     let docDetail: object ;
     docDetail =await this.apiRequest(UrlConfig.serviceUrl + '/testing-support/test-document',systemUpdateUser);
       //This can be moved to before test hook to as same document URL will be used for all test data
    let docParameter= {
      TEST_DOCUMENT_URL : docDetail.document_url,
      TEST_DOCUMENT_BINARY_URL : docDetail.document_binary_url

    };
    const dateTime = new Date().toISOString();
    caseData.caseData.caseName= caseName;
  caseData.caseData.dateSubmitted = dateTime.slice(0, 10);
  caseData.caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);



     let data =lodash.template(JSON.stringify(caseData))(docParameter);
   // console.log('data  ' + data );
  const postURL = `${UrlConfig.serviceUrl}/testing-support/case/populate/${caseID}`;
      try {
        console.log (' update request')
     let   res = await this.apiRequest(postURL,systemUpdateUser,'post',data);
     console.log ('res' + res);
      } catch (error) {
          console.log(error);
      }
    }

  async apiRequest(postURL,authUser,method ='get',data={} )
  {
    const  systemUserAuthToke = await this.getAccessToken(authUser);
    console.log ('systemUserAuthToke' + systemUserAuthToke)
    const requestConfig ={
      method: method,
      url: postURL,
      data: data,
      headers : {
        'Authorization': `Bearer ${systemUserAuthToke?.data.access_token}`,
        'Content-Type' : 'application/json'
      },
    };
   try {
      return axios.request(requestConfig).then((res)=>{
      return res.data;
     });
   } catch (error) {
    if(axios.isAxiosError(error)){

      console.log(error.status);
      console.log(error.request);
      console.log(error.response);
    }

   }

  }

  async updateCase(){


  }
}
