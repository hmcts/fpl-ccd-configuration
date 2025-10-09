import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import {cafcassAPIUser, systemUpdateUser} from "./user-credentials";

setup('access Token', async ({ }) => {
    try {
        const cafcassAuthToken = await getAccessToken({ user: cafcassAPIUser });
        process.env.CAFCASSAUTH = cafcassAuthToken?.data.access_token;
    } catch (error) {
        console.error('Error during cafcassUser auth token:', error);
        throw error;
    }
    try{
        const systemUserAuthToken = await getAccessToken({ user: systemUpdateUser });
        process.env.SYSUSERAUTH = systemUserAuthToken?.data.access_token;
    }
    catch (error) {
        console.error('Error during systemUser auth token:', error);
        throw error;
    }
});
