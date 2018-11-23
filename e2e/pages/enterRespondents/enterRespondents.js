const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: (id) => {
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
      },
      contactDetailsHidden: (option) => {
        return {
          option: `#respondents_${id}_contactDetailsHidden-${option}`,
          reason: `#respondents_${id}_contactDetailsHiddenReason`,
        };
      },
      litigationIssues: (option) => {
        return {
          option: `#respondents_${id}_litigationIssues-${option}`,
          reason: `#respondents_${id}_litigationIssuesReason`,
        };
      },
    };
  },
  addRespondent: 'Add new',

  enterRespondent(id, respondent) {
    I.fillField(this.fields(id).respondent.name, respondent.name);
    I.fillField(this.fields(id).respondent.dob.day, respondent.dob.day);
    I.fillField(this.fields(id).respondent.dob.month, respondent.dob.month);
    I.fillField(this.fields(id).respondent.dob.year, respondent.dob.year);
    I.selectOption(this.fields(id).respondent.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields(id).respondent.genderIdentify, '');
    }
    I.fillField(this.fields(id).respondent.placeOfBirth, respondent.placeOfBirth);
    within(this.fields(id).respondent.address, () => {
      postcodeLookup.lookupPostcode(respondent.address);
    });
    I.fillField(this.fields(id).respondent.telephone, respondent.telephone);
  },

  enterRelationshipToChild(id, relationship) {
    I.fillField(this.fields(id).respondent.relationshipToChild, relationship);
  },

  enterContactDetailsHidden(id, option, reason = '') {
    I.click(this.fields(id).contactDetailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(id).contactDetailsHidden(option).reason, reason);
    }
  },

  enterLitigationIssues(id, option, reason = '') {
    I.click(this.fields(id).litigationIssues(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(id).litigationIssues(option).reason, reason);
    }
  },
};
