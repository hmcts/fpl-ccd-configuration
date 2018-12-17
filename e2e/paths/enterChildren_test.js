const config = require('../config.js');

const addresses = [
  {
    lookupOption: 'Flat 2, Caversham House 15-17, Church Road, Reading',
    buildingAndStreet: {
      lineOne: 'Flat 2',
      lineTwo: 'Caversham House 15-17',
      lineThree: 'Church Road',
    },
    town: 'Reading',
    postcode: 'RG4 7AA',
    country: 'United Kingdom',
  },
  {
    lookupOption: '1 Three Tuns Wynd, High Street, Stokesley, Middlesbrough',
    buildingAndStreet: {
      lineOne: '1 Three Tuns Wynd',
      lineTwo: 'High Street',
      lineThree: 'Stokesley',
    },
    town: 'Middlesbrough',
    postcode: 'TS9 5DQ',
    country: 'United Kingdom',
  },
];

Feature('Enter children in application').retry(2);

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterChildren);
});

Scenario('completing half of the enter children in the c110a application', (I, enterChildrenPage, caseViewPage) => {
  enterChildrenPage.enterChildDetails('Timothy', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  enterChildrenPage.enterAddress(addresses[0]);
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('completing entering child information in the c110a application', (I, enterChildrenPage, caseViewPage) => {
  enterChildrenPage.enterChildDetails('Timothy', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  enterChildrenPage.enterAddress(addresses[0]);
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.defineAbilityToTakePartInProceedings();
  enterChildrenPage.addChild();
  enterChildrenPage.enterChildDetails('Susan', '01', '07', '2016');
  enterChildrenPage.defineChildSituation('02', '11', '2017');
  enterChildrenPage.enterAddress(addresses[1]);
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.defineAbilityToTakePartInProceedings();
  I.continueAndSubmit();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Child 1', 'Child\'s full name', 'Timothy');
  I.seeAnswerInTab(2, 'Child 1', 'Date of birth', '1 Aug 2015');
  I.seeAnswerInTab(3, 'Child 1', 'Gender', 'Boy');
  I.seeAnswerInTab(4, 'Child 1', 'Describe child\'s situation', 'Living with respondents');
  I.seeAnswerInTab(5, 'Child 1', 'What date did they start staying here?', '1 Nov 2017');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(7, 'Child 1', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(8, 'Child 1', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(9, 'Child 1', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(10, 'Child 1', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(11, 'Child 1', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(12, 'Child 1', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(13, 'Child 1', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(14, 'Child 1', 'Social worker\'s telephone number', '01234567');
  I.seeAnswerInTab(15, 'Child 1', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(16, 'Child 1', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(17, 'Child 1', 'Does this child have any issues with litigation capacity?', 'No');

  I.seeAnswerInTab(1, 'Additional children 1', 'Child\'s full name', 'Susan');
  I.seeAnswerInTab(2, 'Additional children 1', 'Date of birth', '1 Jul 2016');
  I.seeAnswerInTab(3, 'Additional children 1', 'Gender', 'Boy');
  I.seeAnswerInTab(4, 'Additional children 1', 'Describe child\'s situation', 'Living with respondents');
  I.seeAnswerInTab(5, 'Additional children 1', 'What date did they start staying here?', '2 Nov 2017');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '1 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(7, 'Additional children 1', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(8, 'Additional children 1', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(9, 'Additional children 1', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(10, 'Additional children 1', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(11, 'Additional children 1', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(12, 'Additional children 1', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(13, 'Additional children 1', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(14, 'Additional children 1', 'Social worker\'s telephone number', '01234567');
  I.seeAnswerInTab(15, 'Additional children 1', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(16, 'Additional children 1', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(17, 'Additional children 1', 'Does this child have any issues with litigation capacity?', 'No');
});
