

//HMCTS admin send message to Judge

// to go next step to send message
// Add the page for function of send message page
// eslint-disable-next-line no-unused-vars
import {test,expect,request,APIRequestContext,APIResponse } from '@playwright/test';
import { SignInPage } from '../pages/sign-in';
import {systemUpdateUser} from '../utils/userCredentials';

{
  test('HMCTSAdmin send message to Judge',
    async ({page}) => {


      const idamApiUrl = 'https://idam-api.aat.platform.hmcts.net';
      console.log('hghjgjhgjhjjhhj');
      const user = systemUpdateUser,APIRequestContext = await request.newContext();
      // getAuthToken();
      const response = await APIRequestContext.post(`${idamApiUrl}/loginUser?username=${user.email}&password=${user.password}`);

      //  response = await APIRequestContext.get('https://api.publicapis.org/entries'), responsebody = await response.json();
      console.log(response.status);


      // eslint-disable-next-line no-unused-vars
      const login = new SignInPage(page);
      // login.visit();
      // login.login('email@email','pass');

      // await login.visit();
      // await login.login(
      //   newSwanseaLocalAuthorityUserOne.email,
      //   newSwanseaLocalAuthorityUserOne.password,
      // );

      // set up the test fixture

      // Login as HMCts user

      // go to next step


    }
  );
}
