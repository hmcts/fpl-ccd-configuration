const config = require('../config.js');
const uploadDocs = require('../fragments/caseDocuments');

let caseId;

Feature('Case maintenance after submission');

Before(async (I, caseViewPage, submitApplicationEventPage) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
    await I.enterMandatoryFields();
    await caseViewPage.goToNewActions(config.applicationActions.submitCase);
    submitApplicationEventPage.giveConsent();
    await I.completeEvent('Submit');

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Case ${caseId} has been submitted`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority uploads documents', uploadDocs.uploadDocuments);

Scenario('local authority uploads court bundle', uploadDocs.uploadCourtBundle);
