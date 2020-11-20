const config = require('../config.js');

const finalHearingCaseData = require('../fixtures/caseData/finalHearing.json');
const moment = require('moment');

Feature('Order removal by super user');

Scenario('@run-me HMCTS super user removes a cmo from a case', async ({I, caseViewPage, removeOrderEventPage}) => {
  const newCaseId = await I.submitNewCaseWithData(finalHearingCaseData);

  await I.signIn(config.hmctsSuperUser);
  await I.navigateToCaseDetailsAs(config.hmctsSuperUser, newCaseId);

  await caseViewPage.goToNewActions(config.superUserActions.removeOrder);

  let order = finalHearingCaseData.caseData.sealedCMOs[0].value;
  const labelToSelect = 'Case management order - ' + moment(order.dateIssued).format('D MMMM YYYY');
  removeOrderEventPage.selectOrderToRemove(labelToSelect);
  await I.goToNextPage();
  removeOrderEventPage.addRemoveOrderReason('Entered incorrect order');
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.superUserActions.removeOrder);

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  await assertRemovedSealedCMO(I, '1', '1 January 2020');

});

const assertRemovedSealedCMO = function (I, collectionId, hearingDate) {
  const sealedCMO = `Removed case management orders ${collectionId}`;

  I.seeInTab([sealedCMO, 'Order'], 'mockFile.pdf');
  I.seeInTab([sealedCMO, 'Hearing'], `Case management hearing, ${hearingDate}`);
  I.seeInTab([sealedCMO, 'Date sent'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Date issued'], '27 Aug 2020');
  I.seeInTab([sealedCMO, 'Judge'], 'Her Honour Judge Reed');
  I.seeInTab([sealedCMO, 'Reason for removal'], 'Entered incorrect order');
};
