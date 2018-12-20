const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  state: {
    context: 'firstRespondent',
  },

  fields: function() {
    const id = this.state.context;

    return {
      respondent: {
        name: `#respondents_${id}_name`,
        dob: {
          day: `#respondents_${id}_dob-day`,
          month: `#respondents_${id}_dob-month`,
          year: `#respondents_${id}_dob-year`,
        },
        gender: `#respondents_${id}_gender`,
        genderIdentify: `#respondents_${id}_genderIdentify`,
        placeOfBirth: `#respondents_${id}_placeOfBirth`,
        address: `#respondents_${id}_address_address`,
        telephone: `#respondents_${id}_telephone`,
        relationshipToChild: `#respondents_${id}_relationshipToChild`,
        litigationIssues: {
          yes: `#respondents_${id}_litigationIssues-YES`,
          no: `#respondents_${id}_litigationIssues-NO`,
          dont_know: `#respondents_${id}_litigationIssues-DONT_KNOW`,
        },
        litigationIssuesDetails: `#respondents_${id}_litigationIssuesDetails`,
      },
      contactDetailsHidden: (option) => {
        return {
          option: `#respondents_${id}_contactDetailsHidden-${option}`,
          reason: `#respondents_${id}_contactDetailsHiddenReason`,
        };
      },
    };
  },
  addRespondentButton: 'Add new',

  addRespondent() {
    if (this.state.context === 'additional_0') {
      throw new Error('Adding more respondents is not supported in the test');
    }

    I.click(this.addRespondentButton);
    this.state.context = 'additional_0';
  },

  enterRespondent(respondent) {
    I.fillField(this.fields().respondent.name, respondent.name);
    I.fillField(this.fields().respondent.dob.day, respondent.dob.day);
    I.fillField(this.fields().respondent.dob.month, respondent.dob.month);
    I.fillField(this.fields().respondent.dob.year, respondent.dob.year);
    I.selectOption(this.fields().respondent.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields().respondent.genderIdentify, '');
    }
    I.fillField(this.fields().respondent.placeOfBirth, respondent.placeOfBirth);
    within(this.fields().respondent.address, () => {
      postcodeLookup.lookupPostcode(respondent.address);
    });
    I.fillField(this.fields().respondent.telephone, respondent.telephone);
  },

  enterRelationshipToChild(relationship) {
    I.fillField(this.fields().respondent.relationshipToChild, relationship);
  },

  enterContactDetailsHidden(option, reason = '') {
    I.click(this.fields().contactDetailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields().contactDetailsHidden(option).reason, reason);
    }
  },

  enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    litigationIssue = litigationIssue.toLowerCase();
    switch(litigationIssue) {
      case 'yes':
        I.checkOption(this.fields().respondent.litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields().respondent.litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields().respondent.litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields().respondent.litigationIssuesDetails, litigationIssueDetail);
    }
  },
};
