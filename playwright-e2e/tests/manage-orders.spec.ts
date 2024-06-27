import { test } from  '../fixtures/create-fixture';
import {
  CTSCUser,
  newSwanseaLocalAuthorityUserOne,
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";
import {createCase, updateCase} from "../utils/api-helper";

test.describe('manage orders', () => {
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let caseName : string;
  test.beforeEach(async ()  => {
    caseNumber =  await createCase('e2e case',newSwanseaLocalAuthorityUserOne);
  });

  test('EPO order',
    async ({page,signInPage,manageHearings}) => {
      caseName = 'EPO order ' + dateTime.slice(0, 10);
          });

  });
