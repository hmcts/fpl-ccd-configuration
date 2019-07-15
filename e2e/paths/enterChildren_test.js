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
    lookupOption: '2 Three Tuns Wynd, High Street, Stokesley, Middlesbrough',
    buildingAndStreet: {
      lineOne: '2 Three Tuns Wynd',
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
  enterChildrenPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  enterChildrenPage.enterAddress(addresses[0]);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('completing entering child information in the c110a application', (I, enterChildrenPage, caseViewPage) => {
  enterChildrenPage.enterChildDetails('Bran', 'Stark', '01', '08', '2015');
  enterChildrenPage.defineChildSituation('01', '11', '2017');
  enterChildrenPage.enterAddress(addresses[0]);
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.enterLitigationIssues('Yes', 'mock reason');
  enterChildrenPage.addChild();
  enterChildrenPage.enterChildDetails('Susan', 'Wilson', '01', '07', '2016', 'Girl');
  enterChildrenPage.defineChildSituation('02', '11', '2017');
  enterChildrenPage.enterAddress(addresses[1]);
  enterChildrenPage.enterKeyDatesAffectingHearing();
  enterChildrenPage.enterSummaryOfCarePlan();
  enterChildrenPage.defineAdoptionIntention();
  enterChildrenPage.enterParentsDetails();
  enterChildrenPage.enterSocialWorkerDetails();
  enterChildrenPage.defineChildAdditionalNeeds();
  enterChildrenPage.defineContactDetailsVisibility();
  enterChildrenPage.enterLitigationIssues('No');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Party', 'First name', 'Bran');
  I.seeAnswerInTab(2, 'Party', 'Last name', 'Stark');
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Aug 2015');
  I.seeAnswerInTab(4, 'Party', 'Gender', 'Boy');
  I.seeAnswerInTab(5, 'Party', 'Describe child\'s situation', 'Living with respondents');
  I.seeAnswerInTab(6, 'Party', 'What date did they start staying here?', '1 Nov 2017');
  I.seeAnswerInTab(8, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(9, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(10, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(11, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(12, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(13, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(14, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(15, 'Party', 'Social worker\'s telephone number', '01234567');
  I.seeAnswerInTab(16, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(17, 'Party', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(18, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'Yes');
  I.seeAnswerInTab(19, 'Party', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  I.seeAnswerInTab(1, 'Party', 'First name', 'Susan');
  I.seeAnswerInTab(2, 'Party', 'Last name', 'Wilson');
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Jul 2016');
  I.seeAnswerInTab(4, 'Party', 'Gender', 'Girl');
  I.seeAnswerInTab(5, 'Party', 'Describe child\'s situation', 'Living with respondents');
  I.seeAnswerInTab(6, 'Party', 'What date did they start staying here?', '2 Nov 2017');
  I.seeAnswerInTab(8, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '2 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(9, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(10, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(11, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(12, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(13, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(14, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(15, 'Party', 'Social worker\'s telephone number', '01234567');
  I.seeAnswerInTab(16, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(17, 'Party', 'Do you need contact details hidden from other parties?', 'No');
});
