import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import {cafcassAPIUser} from "../settings/user-credentials";

setup('access Token', async ({ }) => {
    const UserAuthToken = await getAccessToken(cafcassAPIUser);
    process.env.CAFCASSAUTH = UserAuthToken;
});
