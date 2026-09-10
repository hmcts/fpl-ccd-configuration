import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import {cafcassAPIUser, systemUpdateUser,newSwanseaLocalAuthorityUserOne} from "./user-credentials";
import { getDocParameter } from '../utils/api-helper';

const userMap: Record<string, any> = {
    [newSwanseaLocalAuthorityUserOne.email]: newSwanseaLocalAuthorityUserOne,
    [systemUpdateUser.email]: systemUpdateUser,
    [cafcassAPIUser.email]: cafcassAPIUser,
};
setup.describe.configure({ mode: 'serial' });

setup('access Token', async () => {
    for (const email in userMap) {
        const envKey = email.toUpperCase().split('@')[0] + 'AUTH';
        if (!process.env[envKey]) {
            try {
                process.env[envKey] = await getAccessToken({ user: userMap[email] });
            } catch (error) {
                console.error(`Error during auth token for ${email}:`, error);
                throw error;
            }
        }
    }
});
setup('document parameters', async () => {
    try {

        const testDoc = await getDocParameter();
        process.env.TEST_DOCUMENT_URL = testDoc.TEST_DOCUMENT_URL;
        process.env.TEST_DOCUMENT_BINARY_URL = testDoc.TEST_DOCUMENT_BINARY_URL;
    } catch (error) {
        console.error('Error fetching document parameters:', error);
        throw error;
    }
});

