const config = require('../config.js');
const response = require('../fixtures/response');

let caseId;

Feature('Comply with directions');

BeforeSuite(async (I) => {
  caseId = await I.submitNewCaseWithData('standardDirectionOrder');
});

Scenario('local authority complies with directions', async (I, caseViewPage, complyWithDirectionsEventPage) => {
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.complyWithDirections);
  await complyWithDirectionsEventPage.canComplyWithDirection('localAuthorityDirections', 0, response, config.testFile);
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.complyWithDirections);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Compliance 1', 'Party'], 'Local Authority');
  I.seeInTab(['Compliance 1', 'Has this direction been complied with?'], 'Yes');
  I.seeInTab(['Compliance 1', 'Give details'], response.complied.yes.documentDetails);
  I.seeInTab(['Compliance 1', 'Upload file'], 'mockFile.txt');
});
