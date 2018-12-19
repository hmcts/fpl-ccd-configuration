const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  state: {
    context: 'firstChild',
  },
  
  fields: function() {
    const childNo = this.state.context;
    
    return {
      fullName: `#children_${childNo}_childName`,
      DOB: {
        day: `#children_${childNo}_childDOB-day`,
        month: `#children_${childNo}_childDOB-month`,
        year: `#children_${childNo}_childDOB-year`,
      },
      gender: `#children_${childNo}_childGender`,
      situation: {
        selector: `#children_${childNo}_livingSituation`,
        dateStartedStaying: {
          day: `#children_${childNo}_situationDate-day`,
          month: `#children_${childNo}_situationDate-month`,
          year: `#children_${childNo}_situationDate-year`,
        },
        addressOfChild: `#children_${childNo}_address_address`,
      },
      keyDates: `#children_${childNo}_keyDates`,
      careAndContactPlan: `#children_${childNo}_careAndContact`,
      adoptionNo: `#children_${childNo}_adoption-No`,
      mothersName: `#children_${childNo}_mothersName`,
      fathersName: `#children_${childNo}_fathersName`,
      fatherResponsible: `#children_${childNo}_fathersResponsibility`,
      socialWorkerName: `#children_${childNo}_socialWorkerName`,
      socialWorkerTel: `#children_${childNo}_socialWorkerTel`,
      additionalNeedsNo: `#children_${childNo}_additionalNeeds-No`,
      contactHiddenNo: `#children_${childNo}_detailsHidden-No`,
      litigationIssues: {
        yes: `#children_${childNo}_litigationIssues-YES`,
        no: `#children_${childNo}_litigationIssues-NO`,
        dont_know: `#children_${childNo}_litigationIssues-DONT_KNOW`,
      },
      litigationIssuesDetails: `#children_${childNo}_litigationIssuesDetails`,
    };
  },
  addChildButton: 'Add new',

  addChild() {
    if (this.state.context === 'additionalChildren_0') {
      throw new Error('Adding more children is not supported in the test');
    }

    I.click(this.addChildButton);
    this.state.context = 'additionalChildren_0';
  },

  enterChildDetails(name, day, month, year, gender = 'Boy') {
    I.fillField(this.fields().fullName, name);
    I.click(this.fields().DOB.day);
    I.fillField(this.fields().DOB.day, day);
    I.fillField(this.fields().DOB.month, month);
    I.fillField(this.fields().DOB.year, year);
    I.selectOption(this.fields().gender, gender);
  },

  defineChildSituation(day, month, year, situation = 'Living with respondents') {
    I.selectOption(this.fields().situation.selector, situation);
    I.fillField(this.fields().situation.dateStartedStaying.day, day);
    I.fillField(this.fields().situation.dateStartedStaying.month, month);
    I.fillField(this.fields().situation.dateStartedStaying.year, year);
  },

  enterAddress(address) {
    within(this.fields().situation.addressOfChild, () => {
      postcodeLookup.lookupPostcode(address);
    });
  },

  enterKeyDatesAffectingHearing(keyDates = 'Tuesday the 11th') {
    I.fillField(this.fields().keyDates, keyDates);
  },

  enterSummaryOfCarePlan(carePlan = 'care plan summary') {
    I.fillField(this.fields().careAndContactPlan, carePlan);
  },

  defineAdoptionIntention() {
    I.click(this.fields().adoptionNo);
  },

  enterParentsDetails(fatherResponsible = 'Yes', motherName = 'Laura Smith', fatherName = 'David Smith') {
    I.fillField(this.fields().mothersName, motherName);
    I.fillField(this.fields().fathersName, fatherName);
    I.selectOption(this.fields().fatherResponsible, fatherResponsible);
  },

  enterSocialWorkerDetails(socialWorkerName = 'James Jackson', socialWorkerTel = '01234567') {
    I.fillField(this.fields().socialWorkerName, socialWorkerName);
    I.fillField(this.fields().socialWorkerTel, socialWorkerTel);
  },

  defineChildAdditionalNeeds() {
    I.click(this.fields().additionalNeedsNo);
  },

  defineContactDetailsVisibility() {
    I.click(this.fields().contactHiddenNo);
  },

  enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    litigationIssue = litigationIssue.toLowerCase();
    switch(litigationIssue) {
      case 'yes':
        I.checkOption(this.fields().litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields().litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields().litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields().litigationIssuesDetails, litigationIssueDetail);
    }
  },
};
