import {APIRequestContext, request} from "@playwright/test";
import {urlConfig} from "../settings/urls";
import fs from "fs";
import config from "../settings/test-docs/config";
import Ajv from 'ajv';
import {authToken} from "../settings/user-credentials";
import {expect, test} from "../fixtures/fixtures";

const DOCUMENT_DATE_TIME_FORMATTER = new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
});
export const  GAURDIAN_DETAILS  = [
    {
        "guardianName": "June Thacher",
        "telephoneNumber": "01234567890",
        "email": "june.thacher@mail.com",
        "children": [
            "Joe Bloggs"
        ]
    },
    {
        "guardianName": "Tom mac",
        "telephoneNumber": "01234567890",
        "email": "tom.mac@mail.com",
        "children": [
            "Joey"
        ]
    }
]


export const cafcassAPICaseSearch = async (request: APIRequestContext, AuthToken: string, startTime: string, endTime: string) => {
    try {
        let response = await request.get(`${urlConfig.serviceUrl}/cases`,
            {
                headers: {
                    'Authorization': `Bearer ${AuthToken}`,
                    'Content-Type': 'application/json'
                }
                , params: {
                    'startDate': `${startTime}`,
                    'endDate': `${endTime}`
                }
            })
        return response;
    } catch (error) {
        console.log("error in cafcassAPICaseSearch" + error);
        throw error;
    }
}
export const cafcassAPIDocSearch = async (request: APIRequestContext, AuthToken: string, docId: string) => {

    try {
        let response = await request.get(`${urlConfig.serviceUrl}/cases/documents/${docId}/binary`,
            {
                headers: {
                    'Authorization': `Bearer ${AuthToken}`,

                }
            })
        return await response;
    } catch (error) {
        console.log("error in cafcassAPIDocSearch" + error);
        throw error;
    }

}
export const cafcassUpdateGuardianDetails = async (request: APIRequestContext, AuthToken: string, caseID: string, data: {
    guardianName: string;
    telephoneNumber: string;
    email: string;
    children: string[];
}[] | undefined) => {
    let url = `${urlConfig.serviceUrl}/cases/${caseID}/guardians`
    try {
        let response = await request.post(url,
            {
                headers: {
                    'Authorization': `Bearer ${AuthToken}`,
                    'Content-Type': 'application/json',
                },
                data: data,

            })
        return response;
    } catch (error) {
        console.log("error in cafcassUpdateGuardianDetails" + error);
        throw error;
    }

}
export const cafcassAPICaseDocSearch = async (request: APIRequestContext, AuthToken: string, documentId: string) => {
    let url = `${urlConfig.serviceUrl}/cases/documents/{documentId}/binary`
    try {
        let response = await request.get(`${urlConfig.serviceUrl}/cases`,
            {
                headers: {
                    'Authorization': `Bearer ${AuthToken}`,
                    'Content-Type': 'application/json'
                }
            })
        return response;
    } catch (error) {
        console.log("error in cafcassAPICaseDocSearch" + error);
        throw error;
    }

}
export const cafcassAPIUploadDoc = async (request: APIRequestContext, AuthToken: string, caseID: string, docType: string, fileType: string = 'pdf') => {
    let url = `${urlConfig.serviceUrl}/cases/${caseID}/document`;
    let docUpload = fs.readFileSync(config.testPdfFile);
    try {
        let response = await request.post(url,
            {
                headers: {
                    'Authorization': `Bearer ${AuthToken}`,
                },
                multipart: {
                    file: {
                        name: config.testPdfFile,
                        mimeType: `application/${fileType}`,
                        buffer: docUpload,
                    },
                    typeOfDocument: docType
                }
            });
        return response;
    } catch (error) {
        console.log("error in cafcassAPIUploadDoc" + error);
        throw error;
    }

}

export const getDateTimePram = (dateTime: Date, interval: number) => {
    return new Date(dateTime.setMinutes(dateTime.getMinutes() + interval)).toISOString()
}

export const validateCaseItemWithSchema = async (schema: object, caseItem: object): Promise<boolean> => {
    const ajvInstance = new Ajv({allErrors: true, verbose: true});

    const validJson = await ajvInstance.validate(schema, caseItem);
    if (!validJson) {
        console.log("JSON Validation error\n" + ajvInstance.errorsText());
    }
    return validJson;
}

export const getTestDocID =  async (request: APIRequestContext) => {

    let currentDateTime = new Date();
    let docId:string='';

    let intervalStartTime = getDateTimePram(currentDateTime, -2); // getting time 2 mins before the current time for start time
    let intervalEndTime = getDateTimePram(currentDateTime, 10); // getting time 10 mins after the current time for end time
    try {
        let caseResponse = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, intervalEndTime);

        if (  caseResponse .ok()) {
            let body = await caseResponse.json();
            console.log(JSON.stringify(body));
            docId = await body.cases[0].caseData.caseDocuments[0].documentId;
            return docId;
        }
        else{
            throw new Error('Case search was not successful');
        }

    } catch (error) {
        console.log('Error during case search: ', error);
        throw new Error('Case search was not successful');
    }


}
