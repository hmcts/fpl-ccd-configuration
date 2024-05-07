import { test, expect } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne } from "../settings/user-credentials";


test.describe('Case outsourced another Localauthority or Solicitor Organisation',()=> {

test('Managing LA creates case for their own organisation',async (  {signInPage,createCase})=>{

  await signInPage.visit();
    await signInPage.login(
        newSwanseaLocalAuthorityUserOne.email,
        newSwanseaLocalAuthorityUserOne.password,
    );
  await createCase.submitCase(createCase.generatedCaseName);
  await createCase.checkCaseIsCreated(createCase.generatedCaseName);



})

})





