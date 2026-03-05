import { test as setup } from '@playwright/test';
import {getAccessToken} from "../utils/api-helper";
import { getDocParameter } from '../utils/api-helper';
import {users} from "./token-config-cafcassAPITest";

setup.describe.configure({ mode: 'serial' });

setup('access Token', async () => {
    const accessTokens: Record<string, string> = {};
    for (const user in users) {
        let accessToken = '';
        try {
            accessToken = await getAccessToken({user: users[user]});
            accessTokens[users[user].email] = accessToken;
        } catch (error) {
            console.error(`Error during auth token for ${users[user].email}:`, error);
            throw error;
        }
    }
    // Store all access tokens in a global environment variable as JSON
    process.env.ACCESS_TOKENS = JSON.stringify(accessTokens);
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

