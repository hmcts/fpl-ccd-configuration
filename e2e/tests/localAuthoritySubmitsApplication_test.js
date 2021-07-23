const config = require('../config.js');

const children = require('../fixtures/children.js');
const respondents = require('../fixtures/respondents.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');
const localAuthority = require('../fixtures/localAuthority.js');
const others = require('../fixtures/others.js');
const otherProceedings = require('../fixtures/otherProceedingData');
const ordersAndDirectionsNeeded = require('../fixtures/ordersAndDirectionsNeeded.js');

let caseId;

Feature('Local authority creates application');

async function setupScenario(I) {
  if (!caseId) { caseId = await I.submitNewCase(config.swanseaLocalAuthorityUserOne); }
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
}

Scenario('local authority sees task list', async ({I, caseViewPage}) => {
  await setupScenario(I);

  I.dontSeeEvent(config.applicationActions.selectCourt);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);

  caseViewPage.checkTaskIsNoPresent(config.applicationActions.selectCourt);
  await caseViewPage.checkTaskIsFinished(config.applicationActions.changeCaseName);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterOrdersAndDirectionsNeeded);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterHearingNeeded);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterGrounds);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterRiskAndHarmToChildren);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterFactorsAffectingParenting);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.uploadDocuments);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterLocalAuthority);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterChildren);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterRespondents);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterAllocationProposal);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterOtherProceedings);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterInternationalElement);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterOthers);
  caseViewPage.checkTaskIsNotStarted(config.applicationActions.enterAttendingHearing);

  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the orders and directions sought in the Orders and directions sought',
    'Add the hearing urgency details in the Hearing urgency',
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the child\'s details in the Child\'s details',
    'Add the respondents\' details in the Respondents\' details',
    'Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority changes case name @create-case-with-mandatory-sections-only @cross-browser', async ({I, caseViewPage, changeCaseNameEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.changeCaseName);
  await changeCaseNameEventPage.changeCaseName('New case name');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  await I.seeEventSubmissionConfirmation(config.applicationActions.changeCaseName);
  caseViewPage.seeInCaseTitle('New case name');
  caseViewPage.seeInCaseTitle(caseId);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  await caseViewPage.checkTaskIsFinished(config.applicationActions.changeCaseName);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.changeCaseName);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters orders and directions @create-case-with-mandatory-sections-only @cross-browser', async ({I, caseViewPage, enterOrdersAndDirectionsNeededEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
  await enterOrdersAndDirectionsNeededEventPage.checkCareOrder();
  enterOrdersAndDirectionsNeededEventPage.checkInterimCareOrder();
  enterOrdersAndDirectionsNeededEventPage.checkSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkInterimSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkEducationSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkEmergencyProtectionOrder();
  enterOrdersAndDirectionsNeededEventPage.selectPreventRemovalFromAddressEPOType();
  await enterOrdersAndDirectionsNeededEventPage.enterAddress(ordersAndDirectionsNeeded.address);
  enterOrdersAndDirectionsNeededEventPage.checkOtherOrder();
  enterOrdersAndDirectionsNeededEventPage.checkWhereabouts();
  enterOrdersAndDirectionsNeededEventPage.checkEntry();
  enterOrdersAndDirectionsNeededEventPage.checkSearch();
  enterOrdersAndDirectionsNeededEventPage.checkProtectionOrdersOther();
  enterOrdersAndDirectionsNeededEventPage.enterProtectionOrdersDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.checkContact();
  enterOrdersAndDirectionsNeededEventPage.checkAssessment();
  enterOrdersAndDirectionsNeededEventPage.checkMedicalPractitioner();
  enterOrdersAndDirectionsNeededEventPage.checkExclusion();
  enterOrdersAndDirectionsNeededEventPage.enterWhoIsExcluded('John Doe');
  enterOrdersAndDirectionsNeededEventPage.checkProtectionDirectionsOther();
  enterOrdersAndDirectionsNeededEventPage.enterProtectionDirectionsDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.enterOrderDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.checkDirections();
  enterOrdersAndDirectionsNeededEventPage.enterDirections('Test');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterOrdersAndDirectionsNeeded);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Orders and directions needed', 'Which orders do you need?'], ['Care order', 'Interim care order', 'Supervision order', 'Interim supervision order', 'Education supervision order', 'Emergency protection order', 'Variation or discharge of care or supervision order']);
  I.seeInTab(['Orders and directions needed', 'What type of EPO are you requesting?'], 'Prevent removal from an address');
  I.seeInTab(['Orders and directions needed', 'Do you need any of these related orders?'], ['Information on the whereabouts of the child', 'Authorisation for entry of premises', 'Authorisation to search for another child on the premises', 'Other order under section 48 of the Children Act 1989']);
  I.seeInTab(['Orders and directions needed', 'Give details'], 'Test');
  I.seeInTab(['Orders and directions needed', 'Do you need any of these directions?'], ['Contact with any named person', 'A medical or psychiatric examination, or another assessment of the child', 'To be accompanied by a registered medical practitioner, nurse or midwife', 'An exclusion requirement', 'Other direction relating to an emergency protection order']);
  I.seeInTab(['Orders and directions needed', 'Who\'s excluded?'], 'John Doe');
  I.seeInTab(['Orders and directions needed', 'Give details'], 'Test');
  I.seeInTab(['Orders and directions needed', 'Which order do you need?'], 'Test');
  I.seeInTab(['Orders and directions needed', 'Do you need any other directions?'], 'Yes');
  I.seeInTab(['Orders and directions needed', 'Give details'], 'Test');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterOrdersAndDirectionsNeeded);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterOrdersAndDirectionsNeeded);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the hearing urgency details in the Hearing urgency',
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the child\'s details in the Child\'s details',
    'Add the respondents\' details in the Respondents\' details',
    'Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority enters hearing @create-case-with-mandatory-sections-only @cross-browser', async ({I, caseViewPage, enterHearingNeededEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
  await enterHearingNeededEventPage.enterTimeFrame();
  enterHearingNeededEventPage.enterHearingType();
  enterHearingNeededEventPage.enterWithoutNoticeHearing();
  enterHearingNeededEventPage.enterReducedHearing();
  enterHearingNeededEventPage.enterRespondentsAware();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterHearingNeeded);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Hearing needed', 'When do you need a hearing?'], 'Same day');
  I.seeInTab(['Hearing needed', 'Give reason'], 'test reason');
  I.seeInTab(['Hearing needed', 'What type of hearing do you need?'], 'Contested interim care order');
  I.seeInTab(['Hearing needed', 'Do you need a without notice hearing?'], 'No');
  I.seeInTab(['Hearing needed', 'Do you need a hearing with reduced notice?'], 'No');
  I.seeInTab(['Hearing needed', 'Are respondents aware of proceedings?'], 'No');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterHearingNeeded);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterHearingNeeded);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the child\'s details in the Child\'s details',
    'Add the respondents\' details in the Respondents\' details',
    'Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority enters children @create-case-with-mandatory-sections-only @cross-browser', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterChildren);
  await enterChildrenEventPage.enterChildDetails('Bran', 'Stark', '01', '08', '2015');
  await enterChildrenEventPage.defineChildSituation('01', '11', '2017');
  await enterChildrenEventPage.enterAddress(children[0].address);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.enterContactDetailsHidden('No');
  await enterChildrenEventPage.enterLitigationIssues('Yes', 'mock reason');
  await I.addAnotherElementToCollection();
  await enterChildrenEventPage.enterChildDetails('Susan', 'Wilson', '01', '07', '2016', 'Girl');
  await enterChildrenEventPage.defineChildSituation('02', '11', '2017');
  await enterChildrenEventPage.enterAddress(children[1].address);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.enterContactDetailsHidden('Yes');
  await enterChildrenEventPage.enterLitigationIssues('No');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Child 1', 'Party', 'First name'], 'Bran');
  I.seeInTab(['Child 1', 'Party', 'Last name'], 'Stark');
  I.seeInTab(['Child 1', 'Party', 'Date of birth'], '1 Aug 2015');
  I.seeInTab(['Child 1', 'Party', 'Gender'], 'Boy');
  I.seeInTab(['Child 1', 'Party', 'Child\'s living situation'], 'Living with respondents');
  I.seeInTab(['Child 1', 'Party', 'What date did they start staying here?'], '1 Nov 2017');
  I.seeInTab(['Child 1', 'Current address', 'Building and Street'], 'Flat 2');
  I.seeInTab(['Child 1', 'Current address', 'Address Line 2'], 'Caversham House 15-17');
  I.seeInTab(['Child 1', 'Current address', 'Address Line 3'], 'Church Road');
  I.seeInTab(['Child 1', 'Current address', 'Town or City'], 'Reading');
  I.seeInTab(['Child 1', 'Current address', 'Postcode/Zipcode'], 'RG4 7AA');
  I.seeInTab(['Child 1', 'Current address', 'Country'], 'United Kingdom');
  I.seeInTab(['Child 1', 'Key dates for this child'], 'Tuesday the 11th');
  I.seeInTab(['Child 1', 'Brief summary of care and contact plan'], 'care plan summary');
  I.seeInTab(['Child 1', 'Are you considering adoption at this stage?'], 'No');
  I.seeInTab(['Child 1', 'Mother\'s full name'], 'Laura Smith');
  I.seeInTab(['Child 1', 'Father\'s full name'], 'David Smith');
  I.seeInTab(['Child 1', 'Does the father have parental responsibility?'], 'Yes');
  I.seeInTab(['Child 1', 'Name of social worker'], 'James Jackson');
  I.seeInTab(['Child 1', 'Social worker\'s telephone number', 'Telephone number'], '01234567890');
  I.seeInTab(['Child 1', 'Does the child have any additional needs?'], 'No');
  I.seeInTab(['Child 1', 'Do you need contact details hidden from other parties?'], 'No');
  I.seeInTab(['Child 1', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'Yes');
  I.seeInTab(['Child 1', 'Give details, including assessment outcomes and referrals to health services'], 'mock reason');

  I.seeInTab(['Child 2', 'Party', 'First name'], 'Susan');
  I.seeInTab(['Child 2', 'Party', 'Last name'], 'Wilson');
  I.seeInTab(['Child 2', 'Party', 'Date of birth'], '1 Jul 2016');
  I.seeInTab(['Child 2', 'Party', 'Gender'], 'Girl');
  I.seeInTab(['Child 2', 'Party', 'Child\'s living situation'], 'Living with respondents');
  I.seeInTab(['Child 2', 'Party', 'What date did they start staying here?'], '2 Nov 2017');
  I.seeInTab(['Child 2', 'Party', 'Key dates for this child'], 'Tuesday the 11th');
  I.seeInTab(['Child 2', 'Party', 'Brief summary of care and contact plan'], 'care plan summary');
  I.seeInTab(['Child 2', 'Party', 'Are you considering adoption at this stage?'], 'No');
  I.seeInTab(['Child 2', 'Party', 'Mother\'s full name'], 'Laura Smith');
  I.seeInTab(['Child 2', 'Party', 'Father\'s full name'], 'David Smith');
  I.seeInTab(['Child 2', 'Party', 'Does the father have parental responsibility?'], 'Yes');
  I.seeInTab(['Child 2', 'Party', 'Name of social worker'], 'James Jackson');
  I.seeInTab(['Child 2', 'Social worker\'s telephone number', 'Telephone number'], '01234567890');
  I.seeInTab(['Child 2', 'Does the child have any additional needs?'], 'No');
  I.seeInTab(['Child 2', 'Do you need contact details hidden from other parties?'], 'Yes');
  I.seeInTab(['Child 2', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'No');

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeInTab(['Child 1', 'Party', 'First name'], 'Susan');
  I.seeInTab(['Child 1', 'Party', 'Last name'], 'Wilson');
  I.seeInTab(['Child 1', 'Current address', 'Building and Street'], '2 Three Tuns Wynd');
  I.seeInTab(['Child 1', 'Current address', 'Address Line 2'], 'High Street');
  I.seeInTab(['Child 1', 'Current address', 'Address Line 3'], 'Stokesley');
  I.seeInTab(['Child 1', 'Current address', 'Town or City'], 'Middlesbrough');
  I.seeInTab(['Child 1', 'Current address', 'Postcode/Zipcode'], 'TS9 5DQ');
  I.seeInTab(['Child 1', 'Current address', 'Country'], 'United Kingdom');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsCompleted(config.applicationActions.enterChildren);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterChildren);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the respondents\' details in the Respondents\' details',
    'Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority enters respondents @create-case-with-mandatory-sections-only', async ({I, caseViewPage, enterRespondentsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
  await enterRespondentsEventPage.enterRespondent(respondents[0]);
  await enterRespondentsEventPage.enterContactDetailsHidden('No', 'mock reason');
  await enterRespondentsEventPage.enterLitigationIssues('Yes', 'mock reason');
  await enterRespondentsEventPage.enterRepresentationDetails('Yes', respondents[0]);
  await enterRespondentsEventPage.enterRegisteredOrganisation(respondents[0]);

  await I.addAnotherElementToCollection();
  await enterRespondentsEventPage.enterRespondent(respondents[1]);
  await enterRespondentsEventPage.enterContactDetailsHidden('Yes', 'mock reason');
  await enterRespondentsEventPage.enterLitigationIssues('No');
  await enterRespondentsEventPage.enterRepresentationDetails('No');

  await I.addAnotherElementToCollection();
  await enterRespondentsEventPage.enterRespondent(respondents[2]);
  await enterRespondentsEventPage.enterContactDetailsHidden('No', 'mock reason');
  await enterRespondentsEventPage.enterLitigationIssues('No');
  await enterRespondentsEventPage.enterRepresentationDetails('Yes', respondents[2]);
  await enterRespondentsEventPage.enterUnregisteredOrganisation(respondents[2]);

  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Respondents 1', 'Party', 'First name'], respondents[0].firstName);
  I.seeInTab(['Respondents 1', 'Party', 'Last name'], respondents[0].lastName);
  I.seeInTab(['Respondents 1', 'Party', 'Date of birth'], '1 Jan 1980');
  I.seeInTab(['Respondents 1', 'Party', 'Gender'], respondents[0].gender);
  I.seeInTab(['Respondents 1', 'Current address', 'Building and Street'], respondents[0].address.buildingAndStreet.lineOne);
  I.seeInTab(['Respondents 1', 'Current address', 'Address Line 2'], respondents[0].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Respondents 1', 'Current address', 'Address Line 3'], respondents[0].address.buildingAndStreet.lineThree);
  I.seeInTab(['Respondents 1', 'Current address', 'Town or City'], respondents[0].address.town);
  I.seeInTab(['Respondents 1', 'Current address', 'Postcode/Zipcode'], respondents[0].address.postcode);
  I.seeInTab(['Respondents 1', 'Current address', 'Country'], respondents[0].address.country);
  I.seeInTab(['Respondents 1', 'Telephone', 'Telephone number'], respondents[0].telephone);
  I.seeInTab(['Respondents 1', 'What is the respondent\'s relationship to the child or children in this case?'], respondents[0].relationshipToChild);
  I.seeInTab(['Respondents 1', 'Do you need contact details hidden from other parties?'], 'No');
  I.seeInTab(['Respondents 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'Yes');
  I.seeInTab(['Respondents 1', 'Give details, including assessment outcomes and referrals to health services'], 'mock reason');
  I.seeInTab(['Respondents 1', 'Representative', 'Representative\'s first name'], respondents[0].solicitor.firstName);
  I.seeInTab(['Respondents 1', 'Representative', 'Representative\'s last name'], respondents[0].solicitor.lastName);
  I.seeInTab(['Respondents 1', 'Representative', 'Email address'], respondents[0].solicitor.email);
  I.seeOrganisationInTab(['Respondents 1', 'Representative', 'Name'], 'Swansea City Council');
  let address = Object.values(respondents[0].solicitor.organisationAddress);
  I.seeOrganisationInTab(['Respondents 1', 'Representative', 'Address'], address);
  I.seeInTab(['Respondents 1', 'Managing office', 'Building and Street'], respondents[0].solicitor.regionalOfficeAddress.buildingAndStreet.lineOne);
  I.seeInTab(['Respondents 1', 'Managing office', 'Address Line 2'], respondents[0].solicitor.regionalOfficeAddress.buildingAndStreet.lineTwo);
  I.seeInTab(['Respondents 1', 'Managing office', 'Address Line 3'], respondents[0].solicitor.regionalOfficeAddress.buildingAndStreet.lineThree);
  I.seeInTab(['Respondents 1', 'Managing office', 'Town or City'], respondents[0].solicitor.regionalOfficeAddress.town);
  I.seeInTab(['Respondents 1', 'Managing office', 'Postcode/Zipcode'], respondents[0].solicitor.regionalOfficeAddress.postcode);

  I.seeInTab(['Respondents 2', 'Party', 'First name'], respondents[1].firstName);
  I.seeInTab(['Respondents 2', 'Party', 'Last name'], respondents[1].lastName);
  I.seeInTab(['Respondents 2', 'Party', 'Date of birth'], '1 Jan 1955');
  I.seeInTab(['Respondents 2', 'Party', 'Gender'], respondents[1].gender);
  I.seeInTab(['Respondents 2', 'What is the respondent\'s relationship to the child or children in this case?'], respondents[1].relationshipToChild);
  I.seeInTab(['Respondents 2', 'Do you need contact details hidden from other parties?'], 'Yes');
  I.seeInTab(['Respondents 2', 'Give reason'], 'mock reason');
  I.seeInTab(['Respondents 2', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'No');

  I.seeInTab(['Respondents 3', 'Party', 'First name'], respondents[2].firstName);
  I.seeInTab(['Respondents 3', 'Party', 'Last name'], respondents[2].lastName);
  I.seeInTab(['Respondents 3', 'Party', 'Date of birth'], '4 Apr 1978');
  I.seeInTab(['Respondents 3', 'Party', 'Gender'], respondents[2].gender);
  I.seeInTab(['Respondents 3', 'What is the respondent\'s relationship to the child or children in this case?'], respondents[2].relationshipToChild);
  I.seeInTab(['Respondents 3', 'Do you need contact details hidden from other parties?'], 'No');
  I.seeInTab(['Respondents 3', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'No');
  I.seeInTab(['Respondents 3', 'Representative', 'Representative\'s first name'], respondents[2].solicitor.firstName);
  I.seeInTab(['Respondents 3', 'Representative', 'Representative\'s last name'], respondents[2].solicitor.lastName);
  I.seeInTab(['Respondents 3', 'Representative', 'Email address'], respondents[2].solicitor.email);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation name'], respondents[2].solicitor.unregisteredOrganisation.name);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Building and Street'], respondents[2].solicitor.unregisteredOrganisation.address.buildingAndStreet.lineOne);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Address Line 2'], respondents[2].solicitor.unregisteredOrganisation.address.buildingAndStreet.lineTwo);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Address Line 3'], respondents[2].solicitor.unregisteredOrganisation.address.buildingAndStreet.lineThree);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Town or City'], respondents[2].solicitor.unregisteredOrganisation.address.town);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Postcode/Zipcode'], respondents[2].solicitor.unregisteredOrganisation.address.postcode);
  I.seeInTab(['Respondents 3', 'Representative', 'Organisation (unregistered)', 'Organisation address', 'Country'], respondents[2].solicitor.unregisteredOrganisation.address.country);

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeInTab(['Respondents 1', 'Party', 'First name'], respondents[1].firstName);
  I.seeInTab(['Respondents 1', 'Party', 'Last name'], respondents[1].lastName);
  I.seeInTab(['Respondents 1', 'Current address', 'Building and Street'], respondents[1].address.buildingAndStreet.lineOne);
  I.seeInTab(['Respondents 1', 'Current address', 'Address Line 2'], respondents[1].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Respondents 1', 'Current address', 'Address Line 3'], respondents[1].address.buildingAndStreet.lineThree);
  I.seeInTab(['Respondents 1', 'Current address', 'Town or City'], respondents[1].address.town);
  I.seeInTab(['Respondents 1', 'Current address', 'Postcode/Zipcode'], respondents[1].address.postcode);
  I.seeInTab(['Respondents 1', 'Current address', 'Country'], respondents[1].address.country);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsCompleted(config.applicationActions.enterRespondents);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterRespondents);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the grounds for the application in the Grounds for the application',
    'Add local authority\'s details in the Local authority\'s details',
    'Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority enters applicant @create-case-with-mandatory-sections-only @deprecated', async ({I, caseViewPage, enterApplicantEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  await enterApplicantEventPage.enterApplicantDetails(applicant);
  await enterApplicantEventPage.enterSolicitorDetails(solicitor);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicant);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Applicants 1', 'Party', 'Name of applicant'], applicant.name);
  I.seeInTab(['Applicants 1', 'Party', 'Payment by account (PBA) number'], applicant.pbaNumber);
  I.seeInTab(['Applicants 1', 'Party', 'Client code'], applicant.clientCode);
  I.seeInTab(['Applicants 1', 'Party', 'Customer reference'], applicant.customerReference);
  I.seeInTab(['Applicants 1', 'Address', 'Building and Street'], applicant.address.buildingAndStreet.lineOne);
  I.seeInTab(['Applicants 1', 'Address', 'Address Line 2'], applicant.address.buildingAndStreet.lineTwo);
  I.seeInTab(['Applicants 1', 'Address', 'Address Line 3'], applicant.address.buildingAndStreet.lineThree);
  I.seeInTab(['Applicants 1', 'Address', 'Town or City'], applicant.address.townCity);
  I.seeInTab(['Applicants 1', 'Address', 'County'], applicant.address.county);
  I.seeInTab(['Applicants 1', 'Address', 'Postcode/Zipcode'], applicant.address.postcode);
  I.seeInTab(['Applicants 1', 'Address', 'Country'], applicant.address.country);
  I.seeInTab(['Applicants 1', 'Telephone number', 'Telephone number'], applicant.telephoneNumber);
  I.seeInTab(['Applicants 1', 'Telephone number', 'Name of person to contact'], applicant.nameOfPersonToContact);
  I.seeInTab(['Applicants 1', 'Job title'], applicant.jobTitle);
  I.seeInTab(['Applicants 1', 'Mobile number', 'Mobile number'], applicant.mobileNumber);
  I.seeInTab(['Applicants 1', 'Email', 'Email'], applicant.email);
  I.seeInTab(['Solicitor', 'Solicitor\'s full name'], 'John Smith');
  I.seeInTab(['Solicitor', 'Solicitor\'s mobile number'], '7000000000');
  I.seeInTab(['Solicitor', 'Solicitor\'s telephone number'], '00000000000');
  I.seeInTab(['Solicitor', 'Solicitor\'s email'], 'solicitor@email.com');
  I.seeInTab(['Solicitor', 'DX number'], '160010 Kingsway 7');
  I.seeInTab(['Solicitor', 'Solicitor\'s reference'], 'reference');
});

Scenario('local authority enters its details @create-case-with-mandatory-sections-only', async ({I, caseViewPage, enterLocalAuthorityEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);
  await enterLocalAuthorityEventPage.enterDetails(localAuthority);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague(localAuthority.colleagues[0], 0);
  await I.goToNextPage();
  I.see('Check your answers');
  await I.goToPreviousPage();
  await enterLocalAuthorityEventPage.enterColleague(localAuthority.colleagues[1], 1);
  await I.goToNextPage();
  enterLocalAuthorityEventPage.selectMainContact(localAuthority.colleagues[0]);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);

  I.seeInTab(['Local authority 1', 'Name'], localAuthority.name);
  I.seeInTab(['Local authority 1', 'Group email address'], localAuthority.email);
  I.seeInTab(['Local authority 1', 'Legal team manager\'s name and last name'], localAuthority.legalTeamManager);
  I.seeInTab(['Local authority 1', 'PBA number'], localAuthority.pbaNumber);
  I.seeInTab(['Local authority 1', 'Customer reference'], localAuthority.customerReference);
  I.seeInTab(['Local authority 1', 'Client code'], localAuthority.clientCode);
  I.seeInTab(['Local authority 1', 'Address', 'Building and Street'], localAuthority.address.buildingAndStreet.lineOne);
  I.seeInTab(['Local authority 1', 'Address', 'Address Line 2'], localAuthority.address.buildingAndStreet.lineTwo);
  I.seeInTab(['Local authority 1', 'Address', 'Address Line 3'], localAuthority.address.buildingAndStreet.lineThree);
  I.seeInTab(['Local authority 1', 'Address', 'Town or City'], localAuthority.address.townCity);
  I.seeInTab(['Local authority 1', 'Address', 'County'], localAuthority.address.county);
  I.seeInTab(['Local authority 1', 'Address', 'Postcode/Zipcode'], localAuthority.address.postcode);
  I.seeInTab(['Local authority 1', 'Address', 'Country'], localAuthority.address.country);
  I.seeInTab(['Local authority 1', 'Phone number'], localAuthority.phone);

  I.seeInTab(['Local authority 1', 'Colleague 1', 'Role'], localAuthority.colleagues[0].role);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Full name'], localAuthority.colleagues[0].fullName);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Email address'], localAuthority.colleagues[0].email);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'DX code'], localAuthority.colleagues[0].dx);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Solicitor reference'], localAuthority.colleagues[0].reference);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Phone number'], localAuthority.colleagues[0].phone);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Send them case update notifications?'], localAuthority.colleagues[0].notificationRecipient);
  I.seeTagInTab(['Local authority 1', 'Colleague 1', 'Main contact']);

  I.seeInTab(['Local authority 1', 'Colleague 2', 'Role'], localAuthority.colleagues[1].role);
  I.seeInTab(['Local authority 1', 'Colleague 2', 'Title'], localAuthority.colleagues[1].title);
  I.seeInTab(['Local authority 1', 'Colleague 2', 'Full name'], localAuthority.colleagues[1].fullName);
  I.seeInTab(['Local authority 1', 'Colleague 2', 'Email address'], localAuthority.colleagues[1].email);
  I.seeInTab(['Local authority 1', 'Colleague 2', 'Send them case update notifications?'], localAuthority.colleagues[1].notificationRecipient);
  I.dontSeeTagInTab(['Local authority 1', 'Colleague 2', 'Main contact']);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsCompleted(config.applicationActions.enterLocalAuthority);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterLocalAuthority);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
  await caseViewPage.checkTasksHaveErrors([
    'Add the grounds for the application in the Grounds for the application',
    'Add the allocation proposal in the Allocation proposal']);
}).retry(0);

Scenario('local authority enters others to be given notice', async ({I, caseViewPage, enterOthersEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterOthers);
  await enterOthersEventPage.enterOtherDetails(others[0]);
  await enterOthersEventPage.enterRelationshipToChild('Tim Smith');
  await enterOthersEventPage.enterContactDetailsHidden('No');
  await enterOthersEventPage.enterLitigationIssues('No');
  await I.addAnotherElementToCollection('Other person');
  await enterOthersEventPage.enterOtherDetails(others[1]);
  await enterOthersEventPage.enterRelationshipToChild('Tim Smith');
  await enterOthersEventPage.enterContactDetailsHidden('Yes');
  await enterOthersEventPage.enterLitigationIssues('Yes', 'mock reason');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Others to be given notice', 'Person 1', 'Full name'], 'John Smith');
  I.seeInTab(['Others to be given notice', 'Person 1', 'Date of birth'], '1 Jan 1985');
  I.seeInTab(['Others to be given notice', 'Person 1', 'Gender'], 'Male');
  I.seeInTab(['Others to be given notice', 'Person 1', 'Place of birth'], 'Scotland');
  I.seeInTab(['Others to be given notice', 'Current address', 'Building and Street'], 'Flat 2');
  I.seeInTab(['Others to be given notice', 'Current address', 'Address Line 2'], 'Caversham House 15-17');
  I.seeInTab(['Others to be given notice', 'Current address', 'Address Line 3'], 'Church Road');
  I.seeInTab(['Others to be given notice', 'Current address', 'Town or City'], 'Reading');
  I.seeInTab(['Others to be given notice', 'Current address', 'Postcode/Zipcode'], 'RG4 7AA');
  I.seeInTab(['Others to be given notice', 'Current address', 'Country'], 'United Kingdom');
  I.seeInTab(['Others to be given notice', 'Telephone number'], '07888288288');
  I.seeInTab(['Others to be given notice', 'What is this person\'s relationship to the child or children in this case?'], 'Tim Smith');
  I.seeInTab(['Others to be given notice', 'Do you need contact details hidden from other parties?'], 'No');
  I.seeInTab(['Others to be given notice', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'No');

  I.seeInTab(['Other person 1', 'Full name'], 'Paul Wilsdon');
  I.seeInTab(['Other person 1', 'Date of birth'], '1 Jan 1984');
  I.seeInTab(['Other person 1', 'Gender'], 'Male');
  I.seeInTab(['Other person 1', 'Place of birth'], 'Wales');
  I.seeInTab(['Other person 1', 'What is this person\'s relationship to the child or children in this case?'], 'Tim Smith');
  I.seeInTab(['Other person 1', 'Do you need contact details hidden from other parties?'], 'Yes');
  I.seeInTab(['Other person 1', 'Give reason'], 'mock reason');
  I.seeInTab(['Other person 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?'], 'Yes');
  I.seeInTab(['Other person 1', 'Give details, including assessment outcomes and referrals to health services'], 'mock reason');

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeInTab(['Others 1', 'Full name'], 'Paul Wilsdon');
  I.seeInTab(['Others 1', 'Current address', 'Building and Street'], '2 Three Tuns Wynd');
  I.seeInTab(['Others 1', 'Current address', 'Address Line 2'], 'High Street');
  I.seeInTab(['Others 1', 'Current address', 'Address Line 3'], 'Stokesley');
  I.seeInTab(['Others 1', 'Current address', 'Town or City'], 'Middlesbrough');
  I.seeInTab(['Others 1', 'Current address', 'Postcode/Zipcode'], 'TS9 5DQ');
  I.seeInTab(['Others 1', 'Current address', 'Country'], 'United Kingdom');
  I.seeInTab(['Others 1', 'Telephone number'], '07888288288');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsInProgress(config.applicationActions.enterOthers);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterOthers);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters grounds for application @create-case-with-mandatory-sections-only', async ({I, caseViewPage, enterGroundsForApplicationEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  await enterGroundsForApplicationEventPage.enterThresholdCriteriaDetails();
  await enterGroundsForApplicationEventPage.enterGroundsForEmergencyProtectionOrder();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['How does this case meet the threshold criteria?', 'The child concerned is suffering or is likely to suffer significant harm because they are:'], 'Not receiving care that would be reasonably expected from a parent');
  I.seeInTab(['How are there grounds for an emergency protection order?', ''], [enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfNotMoved, enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfMoved, enterGroundsForApplicationEventPage.fields.groundsForApplication.urgentAccessRequired]);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterGrounds);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterGrounds);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);

  await caseViewPage.checkTasksHaveErrors(['Add the allocation proposal in the Allocation proposal']);
});

Scenario('local authority enters risk and harm to children', async ({I, caseViewPage, enterRiskAndHarmToChildrenEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterRiskAndHarmToChildren);
  await enterRiskAndHarmToChildrenEventPage.completePhysicalHarm();
  enterRiskAndHarmToChildrenEventPage.completeEmotionalHarm();
  enterRiskAndHarmToChildrenEventPage.completeSexualAbuse();
  enterRiskAndHarmToChildrenEventPage.completeNeglect();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterRiskAndHarmToChildren);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Risks and harm to children', 'Physical harm including non-accidental injury'], 'Yes');
  I.seeInTab(['Risks and harm to children', 'Select all that apply'], 'Past harm');
  I.seeInTab(['Risks and harm to children', 'Emotional harm'], 'No');
  I.seeInTab(['Risks and harm to children', 'Sexual abuse'], 'No');
  I.seeInTab(['Risks and harm to children', 'Neglect'], 'Yes');
  I.seeInTab(['Risks and harm to children', 'Select all that apply'], ['Past harm', 'Future risk of harm']);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterRiskAndHarmToChildren);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterRiskAndHarmToChildren);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters factors affecting parenting', async ({I, caseViewPage, enterFactorsAffectingParentingEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
  await enterFactorsAffectingParentingEventPage.completeAlcoholOrDrugAbuse();
  enterFactorsAffectingParentingEventPage.completeDomesticViolence();
  enterFactorsAffectingParentingEventPage.completeAnythingElse();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Factors affecting parenting', 'Alcohol or drug abuse'], 'Yes');
  I.seeInTab(['Factors affecting parenting', 'Give details'], 'mock reason');
  I.seeInTab(['Factors affecting parenting', 'Domestic violence'], 'Yes');
  I.seeInTab(['Factors affecting parenting', 'Give details'], 'mock reason');
  I.seeInTab(['Factors affecting parenting', 'Anything else'], 'Yes');
  I.seeInTab(['Factors affecting parenting', 'Give details'], 'mock reason');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterFactorsAffectingParenting);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterFactorsAffectingParenting);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters international element', async ({I, caseViewPage, enterInternationalElementEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
  await enterInternationalElementEventPage.fillForm();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);

  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['International element', 'Are there any suitable carers outside of the UK?'], 'Yes');
  I.seeInTab(['International element', 'Give reason'], 'test');
  I.seeInTab(['International element', 'Are you aware of any significant events that have happened outside the UK?'], 'Yes');
  I.seeInTab(['International element', 'Give reason'], 'test');
  I.seeInTab(['International element', 'Are you aware of any issues with the jurisdiction of this case - for example under the Brussels 2 regulation?'], 'No');
  I.seeInTab(['International element', 'Are you aware of any proceedings outside the UK?'], 'Yes');
  I.seeInTab(['International element', 'Give reason'], 'test');
  I.seeInTab(['International element', 'Has, or should, a government or central authority in another country been involved in this case?'], 'Yes');
  I.seeInTab(['International element', 'Give reason'], 'International involvement reason');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterInternationalElement);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterInternationalElement);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters other proceedings', async ({I, caseViewPage, enterOtherProceedingsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
  enterOtherProceedingsEventPage.selectYesForProceeding();
  await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedings[0]);
  await I.addAnotherElementToCollection();
  await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedings[1]);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Other proceedings', 'Are there any past or ongoing proceedings relevant to this case?'], 'Yes');
  I.seeInTab(['Other proceedings', 'Are these previous or ongoing proceedings?'], 'Ongoing');
  I.seeInTab(['Other proceedings', 'Case number'], '000000');
  I.seeInTab(['Other proceedings', 'Date started'], '01/01/01');
  I.seeInTab(['Other proceedings', 'Date ended'], '02/01/01');
  I.seeInTab(['Other proceedings', 'Orders made'], 'Yes');
  I.seeInTab(['Other proceedings', 'Judge'], 'District Judge Martin Brown');
  I.seeInTab(['Other proceedings', 'Names of children involved'], 'Joe Bloggs');
  I.seeInTab(['Other proceedings', 'Name of guardian'], 'John Smith');
  I.seeInTab(['Other proceedings', 'Is the same guardian needed?'], 'Yes');
  I.seeInTab(['Additional proceedings 1', 'Are these previous or ongoing proceedings?'], 'Previous');
  I.seeInTab(['Additional proceedings 1', 'Case number'], '000123');
  I.seeInTab(['Additional proceedings 1', 'Date started'], '02/02/02');
  I.seeInTab(['Additional proceedings 1', 'Date ended'], '03/03/03');
  I.seeInTab(['Additional proceedings 1', 'Orders made'], 'Yes');
  I.seeInTab(['Additional proceedings 1', 'Judge'], 'District Judge Martin Brown');
  I.seeInTab(['Additional proceedings 1', 'Names of children involved'], 'James Simpson');
  I.seeInTab(['Additional proceedings 1', 'Name of guardian'], 'David Burns');
  I.seeInTab(['Additional proceedings 1', 'Is the same guardian needed?'], 'Yes');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterOtherProceedings);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterOtherProceedings);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters language requirement', async ({I, caseViewPage, enterLanguageRequirementsEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.languageRequirement);
  await enterLanguageRequirementsEventPage.enterLanguageRequirement();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.languageRequirement);

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.languageRequirement);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.languageRequirement);
  await caseViewPage.checkTaskIsUnavailable(config.applicationActions.submitCase);
});

Scenario('local authority enters allocation proposal @create-case-with-mandatory-sections-only', async ({I, caseViewPage, enterAllocationProposalEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
  await enterAllocationProposalEventPage.selectAllocationProposal('Magistrate');
  await enterAllocationProposalEventPage.enterProposalReason('test');
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterAllocationProposal);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterAllocationProposal);

  await caseViewPage.checkTasksHaveNoErrors();
});

Scenario('local authority enters attending hearing', async ({I, caseViewPage, enterAttendingHearingEventPage}) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.enterAttendingHearing);
  await enterAttendingHearingEventPage.enterInterpreter();
  enterAttendingHearingEventPage.enterWelshProceedings();
  enterAttendingHearingEventPage.enterIntermediary();
  enterAttendingHearingEventPage.enterDisabilityAssistance();
  enterAttendingHearingEventPage.enterExtraSecurityMeasures();
  enterAttendingHearingEventPage.enterSomethingElse();
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.enterAttendingHearing);
  caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
  I.seeInTab(['Attending the hearing', 'Interpreter'], 'Yes');
  I.seeInTab(['Attending the hearing', 'Give details including person, language and dialect'], 'French translator');
  I.seeInTab(['Attending the hearing', 'Spoken or written Welsh'], 'No');
  I.seeInTab(['Attending the hearing', 'Intermediary'], 'No');
  I.seeInTab(['Attending the hearing', 'Facilities or assistance for a disability'], 'Yes');
  I.seeInTab(['Attending the hearing', 'Give details'], 'learning difficulty');
  I.seeInTab(['Attending the hearing', 'Separate waiting room or other security measures'], 'Yes');
  I.seeInTab(['Attending the hearing', 'Give details'], 'Separate waiting rooms');
  I.seeInTab(['Attending the hearing', 'Something else'], 'Yes');
  I.seeInTab(['Attending the hearing', 'Give details'], 'I need this for this person');

  caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  caseViewPage.checkTaskIsFinished(config.applicationActions.enterAttendingHearing);
  await caseViewPage.checkTaskIsAvailable(config.applicationActions.enterAttendingHearing);
});

Scenario('local authority adds multiple application documents @cross-browser', async ({I, caseViewPage, addApplicationDocumentsEventPage}) => {
  await setupScenario(I);
  const browser = await I.getBrowser();
  // Both edge and safari fail to upload files in Saucelabs. Excluded for now.
  if (browser !== 'MicrosoftEdge' && browser !== 'safari') {
    await caseViewPage.goToNewActions(config.applicationActions.addApplicationDocuments);

    await addApplicationDocumentsEventPage.addApplicationDocument('Threshold', config.testPdfFile);
    await addApplicationDocumentsEventPage.addApplicationDocument('SWET', config.testPdfFile, undefined, 'Genogram included');
    await addApplicationDocumentsEventPage.addApplicationDocument('Other', config.testPdfFile, 'Medical report');
    await I.seeCheckAnswersAndCompleteEvent('Save and continue');

    I.seeEventSubmissionConfirmation(config.applicationActions.addApplicationDocuments);
    caseViewPage.selectTab(caseViewPage.tabs.viewApplication);
    I.seeInTab(['Documents 1', 'Type of document'], 'Threshold');
    I.seeInTab(['Documents 1', 'File'], 'mockFile.pdf');
    I.seeInTab(['Documents 3', 'Uploaded by'], 'kurt@swansea.gov.uk');

    I.seeInTab(['Documents 2', 'Type of document'], 'SWET');
    I.seeInTab(['Documents 2', 'File'], 'mockFile.pdf');
    I.seeInTab(['Documents 3', 'Uploaded by'], 'kurt@swansea.gov.uk');

    I.seeInTab(['Documents 3', 'Type of document'], 'Other');
    I.seeInTab(['Documents 3', 'File'], 'mockFile.pdf');
    I.seeInTab(['Documents 3', 'Uploaded by'], 'kurt@swansea.gov.uk');

    caseViewPage.selectTab(caseViewPage.tabs.startApplication);
    caseViewPage.checkTaskIsInProgress(config.applicationActions.uploadDocuments);
  }
});

let feeToPay = '2055'; //Need to remember this between tests.. default in case the test below fails

Scenario('local authority submits application @create-case-with-mandatory-sections-only', async ({I, caseViewPage, submitApplicationEventPage}) => {
  await setupScenario(I);
  await caseViewPage.selectTab(caseViewPage.tabs.startApplication);
  await caseViewPage.startTask(config.applicationActions.submitCase);

  feeToPay = await submitApplicationEventPage.getFeeToPay();
  submitApplicationEventPage.seeDraftApplicationFile();
  await submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit', null, true);

  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  caseViewPage.selectTab(caseViewPage.tabs.furtherEvidence);
  I.see('New_case_name.pdf');
});

Scenario('HMCTS admin check the payment', async ({I, caseViewPage, paymentHistoryPage}) => {
  await setupScenario(I);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.paymentHistory);
  await paymentHistoryPage.checkPayment(feeToPay, applicant.pbaNumber);
}).retry(1); // retry due to async nature of the payment and the payment could be still processing..
