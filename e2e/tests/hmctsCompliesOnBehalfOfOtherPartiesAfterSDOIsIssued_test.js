const config = require('../config.js');
const standardDirectionOrder = require('./fixtures/standardDirectionOrder.json');

let caseId;

Feature('Comply with directions');

BeforeSuite(async I => caseId = await I.submitNewCaseWithData(standardDirectionOrder, 'PREPARE_FOR_HEARING'));

Scenario('HMCTS admin complies with directions on behalf of other parties', async (I, caseViewPage, complyOnBehalfOfOthersEventPage) => {
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.applicationActions.complyOnBehalfOf);
  await complyOnBehalfOfOthersEventPage.addNewResponseOnBehalfOf('respondentDirectionsCustom', 'Respondent 1', 'Yes');
  await I.retryUntilExists(() => I.click('Continue'), '#otherPartiesDirectionsCustom');
  await I.retryUntilExists(() => I.click('Continue'), '#cafcassDirectionsCustom');
  await I.completeEvent('Save and continue');
  await I.seeEventSubmissionConfirmation(config.applicationActions.complyOnBehalfOf);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab(['Compliance 1', 'Party'], 'Court');
  I.seeInTab(['Compliance 1', 'Complying on behalf of'], 'Respondent 1');
  I.seeInTab(['Compliance 1', 'Has this direction been complied with?'], 'Yes');
});
