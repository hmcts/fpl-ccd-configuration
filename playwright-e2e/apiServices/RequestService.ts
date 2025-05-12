import { APIRequestContext, expect } from "@playwright/test";
import { urlConfig } from "../settings/urls";
import { systemUpdateUser } from "../settings/user-credentials";
import { getAccessToken } from "../utils/api-helper";

const ACCESS_TOKEN_SIMPLE_CACHE: {[index: string]: string} = {};

export type UserCredential = {
    email: string,
    password: string
};

export class RequestService {
    readonly request: APIRequestContext;

    constructor(request: APIRequestContext) {
        this.request = request;
    }

    async createCase(user: UserCredential, caseName: string = "API test case", caseDetails?: any, caseState?: string) {
        const createCaseRsp = await this.sendRequest("testing-support/case/create", user, {caseName: caseName});
        expect(createCaseRsp.ok()).toBeTruthy();

        let caseDetailsCreated = await createCaseRsp.json();

        expect(caseDetailsCreated).toBeDefined();
        expect(caseDetailsCreated.id).toBeDefined();

        let populatedCaseDetails = {
            id: caseDetailsCreated.id,
            state: 'Open',
            caseData: caseDetailsCreated.data
        };

        if (caseDetails) {
            populatedCaseDetails = {
                id: caseDetailsCreated.id,
                state: (caseState) ? caseState : caseDetails.state,
                caseData: Object.assign(caseDetailsCreated.data, caseDetails.caseData) as any
            };

            let updateRsp = await this.sendRequest(`testing-support/case/populate/${populatedCaseDetails.id}`, systemUpdateUser, populatedCaseDetails);
            expect(updateRsp.ok()).toBeTruthy();
        }

        populatedCaseDetails.caseData.id = populatedCaseDetails.id;
        populatedCaseDetails.caseData.state = populatedCaseDetails.state;

        return populatedCaseDetails;
    }

    async callAboutToStart(eventName: string, user: UserCredential, caseDetails: any, caseDetailsBefore?: any) {
        return await this.callback(`${eventName}/about-to-start`, user, caseDetails, caseDetailsBefore);
    }

    async callMidEvent(eventName: string, user: UserCredential, caseDetails: any, caseDetailsBefore?: any) {
        return await this.callback(`${eventName}/mid-event`, user, caseDetails, caseDetailsBefore);
    }

    async callAboutToSubmit(eventName: string, user: UserCredential, caseDetails: any, caseDetailsBefore?: any) {
        return await this.callback(`${eventName}/about-to-submit`, user, caseDetails, caseDetailsBefore);
    }

    async callSubmitted(eventName: string, user: UserCredential, caseDetails: any, caseDetailsBefore?: any) {
        return await this.callback(`${eventName}/submitted`, user, caseDetails, caseDetailsBefore);
    }

    async callback(eventUrl: string, user: UserCredential, caseDetails: any, caseDetailsBefore?: any) {
        let data = {
            case_details: {
                id: caseDetails.id,
                state: caseDetails.state,
                data: caseDetails.caseData
            }
        } as any;

        if (caseDetailsBefore) {
            data.case_details_before = {
                id: caseDetailsBefore.id,
                state: caseDetailsBefore.state,
                data: caseDetailsBefore.caseData
            };
        } else {
            data.case_details_before = data.case_details;
        }

        let response = await this.sendRequest(`callback/${eventUrl}`, user, data);
        expect(response.ok()).toBeTruthy();

        try {
            let aboutToStartOrSubmitCallbackResponse = await response.json();
            return {
                caseData: aboutToStartOrSubmitCallbackResponse?.data,
                dataClassification: aboutToStartOrSubmitCallbackResponse?.data_classification,
                securityClassification: aboutToStartOrSubmitCallbackResponse?.security_classification,
                errors: aboutToStartOrSubmitCallbackResponse?.errors,
                warnings: aboutToStartOrSubmitCallbackResponse?.warnings,
                state: aboutToStartOrSubmitCallbackResponse?.state,
                httpStatus: response.status()
            };
        } catch (e) {
            return {
                httpStatus: response.status()
            };
        }
    }

    async sendRequest(urlPath: string, user: UserCredential, data: any, method: "get" | "post" = "post") {
        let accessToken = await this.getAccessToken(user);
        let url = `${urlConfig.serviceUrl}/${urlPath}`
        let httpOptions = {
            headers: {
                'Authorization': `Bearer ${accessToken}`
            }
        } as any;

        if (data) {
            httpOptions.data = data;
        }

        if (method === "get") {
            return await this.request.get(url, httpOptions);
        } else {
            httpOptions.headers['Content-Type'] = 'application/json';
            return await this.request.post(url, httpOptions);
        }
    }

    async getAccessToken(user: UserCredential): Promise<string> {
        if (ACCESS_TOKEN_SIMPLE_CACHE[user.email]) {
            console.log('return Idam token from cache');
            return ACCESS_TOKEN_SIMPLE_CACHE[user.email];
        } else {
            console.log('getting Idam token');
            let token = await getAccessToken({user: user})
                .then(tokenRsp => tokenRsp?.data.access_token as string);
            ACCESS_TOKEN_SIMPLE_CACHE[user.email] = token;
            return token;
        }
    }
}
