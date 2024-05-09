import { test, expect } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne,wiltshireCountyUserTwo,wiltshireCountyUserOne,swanseaOrgCAAUser } from "../settings/user-credentials";


test.describe('Case outsourced another Localauthority or Solicitor Organisation',()=> {

test('@local Managing LA creates case for their own organisation',async ({ signInPage,createCase})=>{

    await signInPage.visit();
    await signInPage.login(
        wiltshireCountyUserOne.email,
        wiltshireCountyUserOne.password,
    );
    await createCase.createCase();
    await pause();
    await createCase.submitCase(createCase.generatedCaseName);
    await createCase.selectLocalAuthority('');
    await createCase.clickContinue();
    await c

    await page.getByLabel('Select the local authority you\'re representing').selectOption('2: HN');
    await page.getByRole('button', { name: 'Continue' }).click();
    await page.getByLabel('Case name').click();
    await page.getByLabel('Case name').fill('sdfsaf');
    await page.getByRole('button', { name: 'Submit' }).click();
    await page.getByLabel('Yes').check();
    await page.getByRole('button', { name: 'Submit' }).click();

    //login in as wiltshire 2nd user and find case
  //await createCase.checkCaseIsCreated(createCase.generatedCaseName);

})

})





