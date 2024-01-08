import { test, expect } from "@playwright/test";
import { SignInPage } from "../pages/SignInPage";
import { swanseaLocalAuthorityUserOne } from "../resources/userCredentials";
import { SmokeCreateCase } from "../pages/CreateCaseCaseFilterPage";

test("Smoke Test @smoke-playwrightonly-long", async ({ page }) => {
  // 1. Sign in as local-authority user 
  const signInPage = new SignInPage(page);
  const smokeCreateCase = new SmokeCreateCase(page);
  
  await signInPage.visit();
  await signInPage.login(
    swanseaLocalAuthorityUserOne.email,
    swanseaLocalAuthorityUserOne.password,
  );
  await signInPage.isSignedIn();
  // Add application details
  // C110A APPLICATION
  // 2. Start new case, get case id and assert case id is created
  await smokeCreateCase.CaseName();
  await smokeCreateCase.CreateCase();
  await smokeCreateCase.SubmitCase(smokeCreateCase.generatedCaseName);
  await smokeCreateCase.CheckCaseIsCreated(smokeCreateCase.generatedCaseName);

  // 3. Orders and directions sought
  await page.getByRole('heading', { name: 'Orders and directions needed' }).click();
  await page.getByLabel('Care order', { exact: true }).check();
  await page.getByLabel('Interim care order').check();
  await page.getByLabel('Interim care order').uncheck();
  await page.getByRole('radio', { name: 'No' }).check();
  await page.getByRole('group', { name: 'Orders and directions needed' }).getByLabel('*Which court are you issuing for? (Optional)').selectOption('37: 262');
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.getByRole('heading', { name: 'Check your answers' }).click();
  await page.getByRole('button', { name: 'Save and continue' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970');
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1699273737290970#Start%20application');
  await page.getByText('C110a Application', { exact: true }).click();

  // 4. Hearing urgency

  // Add application details
  // 5. Grounds for the application

  // 6. Risk and harm to children 

  // 7. Grounds for the application

  // 8. Risk and harm to children

  // 9. Factors affecting parenting

  // Add application documents
  // 10. Upload documents

  // Add information about the parties
  // 11. Applicant's details

  // 12. Child's details

  // 13. Respondents' details

  // Add court requirements
  // 14. Allocation proposal

  // Add additional information
  // 15. Other proceedings

  // 16. International element

  // 17. Other people in the case

});
