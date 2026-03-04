import { test as setup } from '@playwright/test';
import { getAccessToken } from "../utils/api-helper";
import { newSwanseaLocalAuthorityUserOne, systemUpdateUser } from "./user-credentials";
import { getDocParameter } from '../utils/api-helper';
import {each} from "lodash";
import {ServiceTokenParams} from "@hmcts/playwright-common/dist/utils/service-auth.utils";
import {ServiceAuthUtils} from "@hmcts/playwright-common";
import {TokenManager} from "../utils/token-manager";

const userMap: Record<string, any> = {
    [newSwanseaLocalAuthorityUserOne.email]: newSwanseaLocalAuthorityUserOne,
    [systemUpdateUser.email]: systemUpdateUser,
};
const service = [
    'fpl_case_service',
    'ccd_data',
];
setup.describe.configure({ mode: 'serial' });

setup('access Token', async () => {
    for (const email in userMap) {
        const envKey = email.toUpperCase().split('@')[0] + 'AUTH';
        let accessToken = '';
            try {
              accessToken = await getAccessToken({ user: userMap[email] });
                TokenManager.setAccessToken(email, accessToken);
            } catch (error) {
                console.error(`Error during auth token for ${email}:`, error);
                throw error;
            }
    }
});
setup(' Service S2S Token', async () => {
    const serviceAuth = new ServiceAuthUtils();
    let serviceS2SToken;
    for (const serv in service) {

        serviceS2SToken = await serviceAuth.retrieveToken({microservice: `${serv}`} as ServiceTokenParams);
        TokenManager.setS2SToken(serv, serviceS2SToken);
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
