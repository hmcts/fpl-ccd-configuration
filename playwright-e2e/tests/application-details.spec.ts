import { test } from  '../fixtures/create-fixture';
import caseData from '../caseData/caseWithHearingDetails.json' assert { type: 'json' };
import vacatedHearingCaseData from '../caseData/caseWithVacatedHearing.json' assert { type: 'json' };
import preJudgeAllocationCaseData from '../caseData/casePreAllocationDecision.json' assert { type: 'json' };
import {
  CTSCUser,
  newSwanseaLocalAuthorityUserOne,
  judgeWalesUser
} from "../settings/user-credentials";
import {expect} from "@playwright/test";
import {testConfig} from "../settings/test-config";
import {createCase, updateCase} from "../utils/api-helper";

test.describe(' @local Add non mandatory application details', () => {
  const dateTime = new Date().toISOString();
  let caseNumber : string;
  let caseName : string;

  test(" Add risk and harm to Children details",async ({signInPage,startApplication,riskAndHarmToChildren})=>
  {
// Risk and harm to children
      caseNumber =  await createCase('Risk and harm to child added '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
      await signInPage.navigateTOCaseDetails(caseNumber);
      await startApplication.riskAndHarmToChildren();
      await riskAndHarmToChildren.riskAndHarmToChildrenSmokeTest();
     // await startApplication.assertRiskAndHarmTochildFinished();

  })
    test("  Upload application Document",async ({signInPage,startApplication,addApplicationDocuments})=>{
        // Add application documents
        //await startApplication.addApplicationDetailsHeading.isVisible();
        caseNumber =  await createCase('upload Application Document '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
        await signInPage.navigateTOCaseDetails(caseNumber);
        await startApplication.addApplicationDocuments();
        await addApplicationDocuments.uploadDocumentSmokeTest();
      //  await startApplication.addApplicationDocumentsInProgress();

    })

    test(' Add Welsh language requirement details',async({signInPage,startApplication,welshLangRequirements})=>{
        // Welsh language requirements
        caseNumber =  await createCase('Welsh languge Detail added '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
        await signInPage.navigateTOCaseDetails(caseNumber);
        await startApplication.welshLanguageReq();
        await welshLangRequirements.welshLanguageSmokeTest();
       // await startApplication.welshLanguageReqUpdated();
    })

    test('  Add international elements',async({signInPage,startApplication,internationalElement})=>{
        // International element
        caseNumber =  await createCase('International element '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
        await signInPage.navigateTOCaseDetails(caseNumber);
        await startApplication.internationalElementReqUpdated();
        await internationalElement.internationalElementSmokeTest();
    })

    test('  Add c1 -Application',async({signInPage,c1WithSupplement})=>{
        // C1 With Supplement
        caseNumber =  await createCase('C1 application in open state '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
        await signInPage.navigateTOCaseDetails(caseNumber);
        await c1WithSupplement.c1WithSupplementSmokeTest();
    })

    test(' Court Service',async({signInPage,startApplication,courtServicesNeeded})=>{
        // Court Services Needed
        caseNumber =  await createCase('Court services '+dateTime.slice(0, 10),newSwanseaLocalAuthorityUserOne);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email,newSwanseaLocalAuthorityUserOne.password)
        await signInPage.navigateTOCaseDetails(caseNumber);
        await startApplication.courtServicesNeededReqUpdated();
        await courtServicesNeeded.CourtServicesSmoketest();
       // await startApplication.assertCourtService();
    })

    //to add other people in the application
});
