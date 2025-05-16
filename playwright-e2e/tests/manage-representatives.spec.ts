import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' with { type: "json" };
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Manage representatives @sessionreuse', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Manage representatives',
        async ({ ctscUser, manageRepresentatives }) => {
            caseName = 'CTSC Manage representatives ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await manageRepresentatives.switchUser(ctscUser.page);
            await manageRepresentatives.navigateTOCaseDetails(caseNumber);

            await manageRepresentatives.gotoNextStep('Manage representatives');
            await manageRepresentatives.updateRepresentatives();
            await manageRepresentatives.tabNavigation('People in the case');
            await expect(manageRepresentatives.page.getByText('Representatives 1')).toBeVisible();
            await expect(manageRepresentatives.page.getByText('Charlie Chaplin')).toBeVisible();
        })
    })

