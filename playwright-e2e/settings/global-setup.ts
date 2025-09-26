import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import {cafcassAPIUser, systemUpdateUser} from "./user-credentials";

setup('access Token', async ({ }) => {
    const cafcassAuthToken = await getAccessToken({user:cafcassAPIUser});
    process.env.CAFCASSAUTH = cafcassAuthToken?.data.access_token;
    const systemUserAuthToken = await getAccessToken({user:systemUpdateUser});
    process.env.SYSUSERAUTH = systemUserAuthToken?.data.access_token;
});


