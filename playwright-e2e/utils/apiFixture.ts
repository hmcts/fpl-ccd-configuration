import {systemUpdateUser,newSwanseaLocalAuthorityUserOne} from '../settings/userCredentials';
import {UrlConfig} from '../settings/urls';

const axios = require('axios').default;
import qs from 'qs';

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
      console.log(error);
    }


  }
  async createCase(caseName){
    //get the user bearer Token
    let  access_token;
    // let caseName='e2eTest';
    const fplServiceBaseUrl ='http://fpl-case-service-aat.service.core-compute-aat.internal';
    access_token = await this.getAccessToken(newSwanseaLocalAuthorityUserOne);
    const url = `${fplServiceBaseUrl}/testing-support/case/create`;

    try {
      let axiosConfig ={
        headers : {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + access_token.data.access_token,
        },
      };
      const data ={
        caseName : caseName,
      };
      const response = await axios.post(url,data,axiosConfig);
      console.log(await response.data);

    } catch (error) {
      console.log(error.status);
    }

  }
  async gettestDocDetails()

  {
    const  systemUserAuthToke = await  this.getAccessToken(systemUpdateUser);
    console.log ('systemUserAuthToke=  ' +  systemUserAuthToke.data.access_token );

  }

  async updateCase(){


  }
}
