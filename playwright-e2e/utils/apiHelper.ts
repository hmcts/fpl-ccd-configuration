import axios from 'axios';
import {systemUpdateUser} from './userCredentials';
// eslint-disable-next-line no-unused-vars
const fetch = require('node-fetch');

// eslint-disable-next-line no-unused-vars
const fplServiceUrl = 'http://fpl-case-service-aat.service.core-compute-aat.internal';
const idamApiUrl = 'https://idam-api.aat.platform.hmcts.net';

export const getAuthToken = async (user = systemUpdateUser) => {
  const url = `${idamApiUrl}/loginUser?username=${user.email}&password=${user.password}`;
  console.log('111111');
  //console.log (url);
  // eslint-disable-next-line no-unused-vars
  try {
    console.log('\njjkkjkj8888');
    // const response = await axios.get('https://api.publicapis.org/entries');
    const response = await axios.get(url, {headers: {'Content-Type': 'application/x-www-form-urlencoded'}});
    console.log(response);

    console.log('inside ');
  }
  catch (error){
    console.log( error);
  }
  // fetch(url, {
  //   method: 'POST',
  //   headers: {'Content-Type': 'application/x-www-form-urlencoded'},
  // }).then(res => {
  //   if (res.ok) {
  //     console.log( res.json);
  //   } else {
  //     throw { message: `POST ${url} failed with ${res.status}` };
  //   }
  // });

};
module.exports={
  getAuthToken,
};




