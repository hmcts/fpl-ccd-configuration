import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import {cafcassAPIUser, systemUpdateUser} from "../settings/user-credentials";

setup('access Token', async ({ }) => {
    const cafcassAuthToken = await getAccessToken(cafcassAPIUser);
    process.env.CAFCASSAUTH = cafcassAuthToken;
    const systemUserAuthToken = await getAccessToken(systemUpdateUser);
    process.env.SYSUSERAUTH = systemUserAuthToken;
});


