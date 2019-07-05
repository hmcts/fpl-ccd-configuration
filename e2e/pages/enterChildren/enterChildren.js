const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  state: {
    context: 0,
  },
  
  fields: function() {
    const id = this.state.context;
    
    return {
      partyTypeIndividual: `#children1_${id}_party_partyType-INDIVIDUAL`,
      firstName: `#children1_${id}_party_firstName`,
      lastName: `#children1_${id}_party_lastName`,
      DOB: {
        day: `#children1_${id}_party_dateOfBirth-day`,
        month: `#children1_${id}_party_dateOfBirth-month`,
        year: `#children1_${id}_party_dateOfBirth-year`,
      },
      address: `#children1_${id}_party_address_address`,
      gender: `select[id="children1_${id}_party_gender"]`,
      genderIdentification: `#children1_${id}_party_genderIdentification`,
      situation: {
        selector: `select[id="children1_${id}_party_livingSituation"]`,
        situationDetails: `#children1_${id}_party_situationDetails`,
        dateStartedStaying: {
          day: `#children1_${id}_party_situationDate-day`,
          month: `#children1_${id}_party_situationDate-month`,
          year: `#children1_${id}_party_situationDate-year`,
        },
        addressOfChild: `div[id="children1_${id}_party_address_address"]`,
      },
      keyDates: `#children1_${id}_party_keyDates`,
      careAndContactPlan: `#children1_${id}_party_careAndContact`,
      adoptionNo: `#children1_${id}_party_adoption-No`,
      mothersName: `#children1_${id}_party_mothersName`,
      fathersName: `#children1_${id}_party_fathersName`,
      fatherResponsible: `#children1_${id}_party_fathersResponsibility`,
      socialWorkerName: `#children1_${id}_party_socialWorkerName`,
      socialWorkerTel: `#children1_${id}_party_socialWorkerTel`,
      additionalNeedsNo: `#children1_${id}_party_additionalNeeds-No`,
      contactHiddenNo: `#children1_${id}_party_detailsHidden-No`,
      litigationIssues: {
        yes: `#children1_${id}_party_litigationIssues-YES`,
        no: `#children1_${id}_party_litigationIssues-NO`,
        dont_know: `#children1_${id}_party_litigationIssues-DONT_KNOW`,
      },
      litigationIssuesDetails: `#children1_${id}_party_litigationIssuesDetails`,
    };
  },
  addChildButton: '#children1 > div:nth-child(1) > button:nth-child(2)',

  addChild() {
    I.click(this.addChildButton);
    this.state.context++;
  },

  enterChildDetails(firstName, surname, day, month, year, gender = 'Boy') {
    I.fillField(this.fields().firstName, firstName);
    I.fillField(this.fields().lastName, surname);
    I.click(this.fields().DOB.day);
    I.fillField(this.fields().DOB.day, day);
    I.fillField(this.fields().DOB.month, month);
    I.fillField(this.fields().DOB.year, year);
    I.selectOption(this.fields().gender, gender);
  },

  selectPartyType() {
    I.click(this.fields().partyTypeIndividual);
  },

  defineChildSituation(day, month, year, situation = 'Living with respondents') {
    I.selectOption(this.fields().situation.selector, situation);
    I.fillField(this.fields().situation.dateStartedStaying.day, day);
    I.fillField(this.fields().situation.dateStartedStaying.month, month);
    I.fillField(this.fields().situation.dateStartedStaying.year, year);
  },

  enterAddress(address) {
    within(this.fields().situation.addressOfChild, () => {
      postcodeLookup.enterAddressManually(address);
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
