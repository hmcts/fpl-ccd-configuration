import { test } from "../fixtures/fixtures";
import { apiRequest, createCase } from "../utils/api-helper";
import case01 from "../caseData/Cafcass-integration-test/01.json" assert { type: "json" };
import case02 from "../caseData/Cafcass-integration-test/02.json" assert { type: "json" };
import case03 from "../caseData/Cafcass-integration-test/03.json" assert { type: "json" };
import case04 from "../caseData/Cafcass-integration-test/04.json" assert { type: "json" };
import case05 from "../caseData/Cafcass-integration-test/05.json" assert { type: "json" };
import case06 from "../caseData/Cafcass-integration-test/06.json" assert { type: "json" };
import case07 from "../caseData/Cafcass-integration-test/07_citizen.json" assert { type: "json" };
import case08 from "../caseData/Cafcass-integration-test/08.json" assert { type: "json" };
import case09 from "../caseData/Cafcass-integration-test/09.json" assert { type: "json" };
import case10 from "../caseData/Cafcass-integration-test/10.json" assert { type: "json" };
import case11 from "../caseData/Cafcass-integration-test/11_citizen.json" assert { type: "json" };
import case12 from "../caseData/Cafcass-integration-test/12_citizen.json" assert { type: "json" };
import case13 from "../caseData/Cafcass-integration-test/13.json" assert { type: "json" };
import case14 from "../caseData/Cafcass-integration-test/14.json" assert { type: "json" };
import case15 from "../caseData/Cafcass-integration-test/15.json" assert { type: "json" };
import case16 from "../caseData/Cafcass-integration-test/16_citizen.json" assert { type: "json" };
import case17 from "../caseData/Cafcass-integration-test/17.json" assert { type: "json" };
import case26 from "../caseData/Cafcass-integration-test/26.json" assert { type: "json" };
import case27 from "../caseData/Cafcass-integration-test/27.json" assert { type: "json" };
import case28 from "../caseData/Cafcass-integration-test/28.json" assert { type: "json" };
import case30 from "../caseData/Cafcass-integration-test/30.json" assert { type: "json" };
import case31 from "../caseData/Cafcass-integration-test/31.json" assert { type: "json" };
import case32 from "../caseData/Cafcass-integration-test/32.json" assert { type: "json" };
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
    let familManNumPrefix = 'FP24A12';

    test("Integration Test", async ({page}, testInfo) => {
        let caseNo = [
            "case01", "case02", "case03", "case04", "case05", "case06", "case07", "case08", "case09", "case10",
            "case11", "case12", "case13", "case14", "case15", "case16", "case17",
            "case26", "case27", "case28",
            "case30", "case31", "case32"
        ];
        let cases = [
            case01, case02, case03, case04, case05, case06, case07, case08, case09, case10,
            case11, case12, case13, case14, case15, case16, case17,
            case26, case27, case28,
            case30, case31, case32
        ];
        for(let i = 0; i < cases.length; i++) {
            let familyNumCount = ('00' + i).slice(-3);
            try {
                caseNumber = await createCase(caseName, laUser);
                if (caseNumber != null) {
                    cases[i].caseData.familyManCaseNumber = familManNumPrefix + familyNumCount;
                    if (caseNo[i] != "case13" && caseNo[i] != "case15" && caseNo[i] != "case16" && caseNo[i] != "case17"
                        && cases[i].caseData.respondents1 != null) {
                        for (let x = 0; x < cases[i].caseData.respondents1.length; x++) {
                            cases[i].caseData.respondents1[x].value.party.firstName = caseNumber + 'respFirstname' + x;
                            cases[i].caseData.respondents1[x].value.party.lastName = caseNumber + 'respLastName' + x;
                        }
                    }

                    if (cases[i].caseData.children1 != null) {
                        for (let x = 0; x < cases[i].caseData.children1.length; x++) {
                            cases[i].caseData.children1[x].value.party.firstName = caseNumber + 'childFirstname' + x;
                            cases[i].caseData.children1[x].value.party.lastName = caseNumber + 'childLastName' + x;
                        }
                    }

                    await updateCase(caseName, caseNumber, cases[i]);
                    console.log(caseNo[i] + ': ' + caseNumber);
                } else {
                    console.log(caseNo[i] + ': Failed');
                }
            } catch (e) {
                console.log(caseNo[i] + ': Failed');
            }
        }
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
        TEST_DOCUMENT_UPLOAD_TIMESTAMP: '2024-10-18T11:58:54.951442988',
    };
    if (caseDataJson.caseData.standardDirectionOrder != null && caseDataJson.caseData.standardDirectionOrder.orderDoc != null ) {
        let sdoDocDetail = await apiRequest(urlConfig.serviceUrl + '/testing-support/test-document', systemUpdateUser);
        docParameter.TEST_SDO_URL = sdoDocDetail.document_url;
        docParameter.TEST_SDO_BINARY_URL = sdoDocDetail.document_binary_url;
        docParameter.TEST_SDO_UPLOAD_TIMESTAMP = '2024-10-18T11:58:54.951442988';
    }

    let data = lodash.template(JSON.stringify(caseDataJson))(docParameter);
    let postURL = `${urlConfig.serviceUrl}/testing-support/case/populate/${caseID}`;
    try {
        await apiRequest(postURL, systemUpdateUser, 'post', data);
    } catch (error) {
        throw error;
    }
}