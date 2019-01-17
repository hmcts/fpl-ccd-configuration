const config = require('../config.js');

Feature('Enter order and details').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.selectOrders);
});

Scenario('Select the care order case order and continue', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab(1, 'Orders and directions needed', 'Which orders do you need?', 'Care order');
});

Scenario('Select all case orders and fill in directions & interim information', (I, caseViewPage, ordersNeededPage) => {
  ordersNeededPage.checkCareOrder();
  ordersNeededPage.checkInterimCareOrder();
  ordersNeededPage.checkSupervisionOrder();
  ordersNeededPage.checkInterimSupervisionOrder();
  ordersNeededPage.checkEducationSupervisionOrder();
  ordersNeededPage.checkEmergencyProtectionOrder();
  ordersNeededPage.checkOtherOrder();
  ordersNeededPage.checkWhereabouts();
  ordersNeededPage.checkEntry();
  ordersNeededPage.checkSearch();
  ordersNeededPage.checkProtectionOrdersOther();
  ordersNeededPage.enterProtectionOrdersDetails('Test');
  ordersNeededPage.checkContact();
  ordersNeededPage.checkAssessment();
  ordersNeededPage.checkMedicalPractitioner();
  ordersNeededPage.checkExclusion();
  ordersNeededPage.checkProtectionDirectionsOther();
  ordersNeededPage.enterProtectionDirectionsDetails('Test');
  ordersNeededPage.enterOrderDetails('Test');
  ordersNeededPage.checkDirections();
  ordersNeededPage.enterDirections('Test');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.selectOrders);
  caseViewPage.selectTab(caseViewPage.tabs.ordersHearing);
  I.seeAnswerInTab(1, 'Orders and directions needed', 'Which orders do you need?',
    ['Care order', 'Interim care order', 'Supervision order', 'Interim supervision order', 'Education supervision' +
    ' order', 'Emergency protection order', 'Other order under part 4 of the Children Act 1989']);
  I.seeAnswerInTab(2, 'Orders and directions needed', 'Do you need any of these related orders?',
    ['Information on the whereabouts of the child', 'Authorisation for entry of premises',
      'Authorisation to search for another child on the premises', 'Other order under section 48 of the Children Act 1989']);
  I.seeAnswerInTab(3, 'Orders and directions needed', 'Give details', 'Test');
  I.seeAnswerInTab(4, 'Orders and directions needed', 'Do you need any of these directions?',
    ['Contact with any named person', 'A medical or psychiatric examination, or another assessment of the child',
      'To be accompanied by a registered medical practitioner, nurse or midwife', 'An exclusion requirement',
      'Other direction relating to an emergency protection order']);
  I.seeAnswerInTab(5, 'Orders and directions needed', 'Give details', 'Test');
  I.seeAnswerInTab(6, 'Orders and directions needed', 'Which order do you need?', 'Test');
  I.seeAnswerInTab(7, 'Orders and directions needed', 'Do you need any other directions?', 'Yes');
  I.seeAnswerInTab(8, 'Orders and directions needed', 'Give details', 'Test');
});
