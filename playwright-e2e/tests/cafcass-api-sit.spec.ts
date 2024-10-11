import { test } from "../fixtures/fixtures";
import { apiRequest, createCase } from "../utils/api-helper";
import case01 from "../caseData/Cafcass-integration-test/01.json" assert { type: "json" };
import case02 from "../caseData/Cafcass-integration-test/02.json" assert { type: "json" };
import case03 from "../caseData/Cafcass-integration-test/03.json" assert { type: "json" };
import { urlConfig } from "../settings/urls";
import { systemUpdateUser } from "../settings/user-credentials";
import lodash from "lodash";


const laUser = {
    email: 'sam@hillingdon.gov.uk',
    password: 'Password12',
};

test.describe('Cafcass API Integration test', () => {
    const dateTime = new Date().toISOString();
    let caseNumber : string;
    let caseName = 'Cafcass Integration Test ' + dateTime;
    test.beforeEach(async ()  => {
        caseNumber = await createCase(caseName, laUser);
        if (caseNumber != null) {
            // do nothing
        } else {
            throw "Fail to create case";
        }
    });

    // test("Integration Test - 01", async ({page}, testInfo) => {
    //     await updateCase(caseName, caseNumber, case01);
    //     console.log('Case 01: ' + caseNumber);
    // });
    //
    // test("Integration Test - 02", async ({page}, testInfo) => {
    //     await updateCase(caseName, caseNumber, case02);
    //     console.log('Case 02: ' + caseNumber);
    // });

    test("Integration Test - 03", async ({page, internationalElement}, testInfo) => {
        await updateCase(caseName, caseNumber, case03);
        console.log('Case 03: ' + caseNumber);
    });
});

const updateCase = async (caseName = 'e2e Test', caseID: string, caseDataJson: any) => {
    const dateTime = new Date().toISOString();
    caseDataJson.caseData.caseName = caseName;
    caseDataJson.caseData.dateSubmitted = dateTime.slice(0, 10);
    caseDataJson.caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);

    //This can be moved to before test hook to as same document URL will be used for all test data
    //replace the documents placeholder with document url
    let docDetail = await apiRequest(urlConfig.serviceUrl + '/testing-support/test-document', systemUpdateUser);
    let docParameter : any = {
        TEST_DOCUMENT_URL: docDetail.document_url,
        TEST_DOCUMENT_BINARY_URL: docDetail.document_binary_url,
        TEST_DOCUMENT_UPLOAD_TIMESTAMP: docDetail.upload_timestamp,
    };
    if (caseDataJson.caseData.standardDirectionOrder != null && caseDataJson.caseData.standardDirectionOrder.orderDoc != null ) {
        let sdoDocDetail = await apiRequest(urlConfig.serviceUrl + '/testing-support/test-document', systemUpdateUser);
        docParameter.TEST_SDO_URL = sdoDocDetail.document_url;
        docParameter.TEST_SDO_BINARY_URL = sdoDocDetail.document_binary_url;
        docParameter.TEST_SDO_UPLOAD_TIMESTAMP = sdoDocDetail.upload_timestamp;
    }

    let data = lodash.template(JSON.stringify(caseDataJson))(docParameter);
    console.log('data ' + data);
    let postURL = `${urlConfig.serviceUrl}/testing-support/case/populate/${caseID}`;
    try {
        await apiRequest(postURL, systemUpdateUser, 'post', data);
    } catch (error) {
        console.log("Fail case: " + caseID);
        console.log(error);
        throw error;
    }
}