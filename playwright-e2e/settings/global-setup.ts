import { test as setup } from '@playwright/test';
import { getAccessToken } from "../utils/api-helper";
import { newSwanseaLocalAuthorityUserOne, systemUpdateUser } from "./user-credentials";
import { getDocParameter } from '../utils/api-helper';
import {each} from "lodash";
import {ServiceTokenParams} from "@hmcts/playwright-common/dist/utils/service-auth.utils";
import {ServiceAuthUtils} from "@hmcts/playwright-common";
import {users,services} from "./token-config";


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
setup(' Service S2S Token', async () => {
    const serviceS2STokens: Record<string, string> = {};
    const serviceAuth = new ServiceAuthUtils();
    let serviceS2SToken = '';
    for (const serv in services) {

        serviceS2SToken = await serviceAuth.retrieveToken({microservice: `${services[serv]}`} as ServiceTokenParams);
        serviceS2STokens[services[serv]] = serviceS2SToken;
    }
    process.env.S2S_TOKENS = JSON.stringify(serviceS2STokens);
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
