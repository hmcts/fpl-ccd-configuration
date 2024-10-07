import {expect, test} from "../fixtures/fixtures";
import {cafcassUpdateGuardianDetails, createCase, updateCase} from "../utils/api-helper";
import {
    authToken,
    CTSCTeamLeadUser,
    newSwanseaLocalAuthorityUserOne
} from "../settings/user-credentials";
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};

test.describe('CafcassAPI Update Gaurdian Details', () => {
    let startTime = new Date().toISOString();
    let caseNumber:string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test(' Cafcass user update the gaurdian details', async ({request, page, signInPage}) => {
            await updateCase('Cafcass update gaurdian details' + startTime.slice(0, 10), caseNumber, submitCase);
            let data = [
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
            let response = await cafcassUpdateGuardianDetails(request, authToken.cafcassAuth, caseNumber, data);

            //assert the response
            expect(response.status()).toBe(200);
            await signInPage.visit();
            await page.pause();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await signInPage.tabNavigation('People in the case');

            // assert the updated gaurdian details on the screen
            await expect(page.getByText('Guardian 1', {exact: true})).toBeVisible();
            await expect(page.locator('#case-viewer-field-read--guardians')).toContainText('June Thacher');
            await expect(page.locator('#case-viewer-field-read--guardians')).toContainText('june.thacher@mail.com');
            await expect(page.getByText('Guardian 1', {exact: true})).toBeVisible();
            await expect(page.locator('#case-viewer-field-read--guardians')).toContainText('Tom mac');
            await expect(page.locator('#case-viewer-field-read--guardians')).toContainText('tom.mac@mail.com');

        })
    test('Cafcass update gaurdian details for cases in not valid state', async ({request}) => {

        let data = [
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

        let response = await cafcassUpdateGuardianDetails(request, authToken.cafcassAuth, caseNumber, data);
        //assertion
        expect(response.status()).toBe(404);
        expect(response.statusText()).toBe('Not Found');
    })
    test('Cafcass Update guardian details by user without cafcass role', async ({request}) => {

        await updateCase('Not authorised user update guardian details' + startTime.slice(0, 10), caseNumber, submitCase);
        let data = [
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

        let response = await cafcassUpdateGuardianDetails(request, authToken.systemAuth, caseNumber, data);
        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Forbidden');

    })

})
