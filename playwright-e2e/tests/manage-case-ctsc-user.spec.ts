import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, HighCourtAdminUser, privateSolicitorOrgUser } from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert { type: "json" };
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert { type: "json" };
import { setHighCourt } from '../utils/update-case-details';
import { createCase, giveAccessToCase, updateCase } from "../utils/api-helper";
import config from "../settings/test-docs/config";
import {urlConfig} from "../settings/urls";

test.describe('Upload additional applications', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;

  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });
  //mark test as slow to give extra timeout
  test.slow();





  test('application welsh language translation requirement',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'CTSC add welsh translation request for documents ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseWithResSolicitor);
         });


})
