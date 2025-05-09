import { test } from "../../fixtures/api-test-fixture";
import caseDetailsJson from '../../caseData/apiTest/mandatoryOpenCase.json' assert { type: 'json' };
import { swanseaOrgCAAUser } from "../../settings/user-credentials";
import { expect } from "@playwright/test";
import { formatDateToStringInDocument, getAge } from "../../utils/format-helper";

const EVENT = "case-submission";
const APPLICATION_FEE = "251500";

const IGNORE_TEXTS = ["C110A"];

test.describe('Submit case API test @apiTest', () => {
    let caseDetailsBefore : any;
    let placeHolderForDocument: { [index: string]: any; };
    test.beforeAll(async ({ callback }) => {
        caseDetailsBefore = await callback.createCase(swanseaOrgCAAUser, caseDetailsJson, "Submit case API test");
        placeHolderForDocument = {
            "id": `${caseDetailsBefore.caseData.id}`,
            "issueDate": formatDateToStringInDocument(new Date()),
            "age": getAge("2020-01-01")
        };
    });

    test('about-to-start', async ({ callback, documentService }) => {
        let caseDetails: any;
        await test.step("callAboutToStart", async () => {
            caseDetails = await callback.callAboutToStart(EVENT, swanseaOrgCAAUser, caseDetailsBefore);
        });

        await test.step("verify updated case data", async () => {
            expect(caseDetails.caseData?.amountToPay).toEqual(APPLICATION_FEE);
            expect(caseDetails.caseData?.draftApplicationDocument).toBeDefined();
        });

        await test.step("verify generated document", async () => {
            await documentService.expectPdfContentSame(caseDetails.caseData?.draftApplicationDocument, "expectedApplication.txt", 
                placeHolderForDocument, IGNORE_TEXTS);
        });
    });

    test('mid-event with missing required field', async ({ callback }) => {
        let inCompleteCaseDetails = Object.assign({}, caseDetailsBefore);
        inCompleteCaseDetails.caseData = Object.assign({}, caseDetailsBefore.caseData, {"allocationProposal" : null});
        let caseDetails = await callback.callMidEvent(EVENT, swanseaOrgCAAUser, inCompleteCaseDetails);

        expect(caseDetails.errors).toEqual([
            "In the allocation proposal section:",
            "• Add the allocation proposal"]);
    });

    test('mid-event', async ({ callback }) => {
        let caseDetails = await callback.callMidEvent(EVENT, swanseaOrgCAAUser, caseDetailsBefore);

        expect(caseDetails.errors).toEqual([]);
    });

    test('about-to-submit', async ({ callback, documentService }) => {
        let caseDetails: any;
        await test.step("callAboutToSubmit", async () => {
            caseDetails = await callback.callAboutToSubmit(EVENT, swanseaOrgCAAUser, caseDetailsBefore);
        });

        await test.step("verify updated case data", async () => {
            expect(caseDetails.errors).toEqual([]);
            expect(caseDetails.caseData?.dateSubmitted).toEqual(new Date().toISOString().split('T')[0]);
            expect(caseDetails.caseData?.submittedForm).toBeDefined();
        });

        await test.step("verify generated document", async () => {
            await documentService.expectPdfContentSame(caseDetails.caseData.submittedForm, "expectedApplication.txt", 
                placeHolderForDocument, IGNORE_TEXTS);
        });
    });
});