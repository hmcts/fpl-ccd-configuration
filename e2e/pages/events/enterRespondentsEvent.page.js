const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      respondent: {
        firstName: `#respondents1_${index}_party_firstName`,
        lastName: `#respondents1_${index}_party_lastName`,
        dateOfBirth: {
          day: `#respondents1_${index}_party_dateOfBirth-day`,
          month: `#respondents1_${index}_party_dateOfBirth-month`,
          year: `#respondents1_${index}_party_dateOfBirth-year`,
        },
        address: `#respondents1_${index}_party_address_address`,
        telephone: `input[id="respondents1_${index}_party_telephoneNumber_telephoneNumber"]`,
        gender: `#respondents1_${index}_party_gender`,
        genderIdentification: `#respondents1_${index}_party_genderIdentification`,
        placeOfBirth: `#respondents1_${index}_party_placeOfBirth`,
        relationshipToChild: `#respondents1_${index}_party_relationshipToChild`,
        litigationIssues: {
          yes: `#respondents1_${index}_party_litigationIssues-YES`,
          no: `#respondents1_${index}_party_litigationIssues-NO`,
          dont_know: `#respondents1_${index}_party_litigationIssues-DONT_KNOW`,
        },
        litigationIssuesDetails: `#respondents1_${index}_party_litigationIssuesDetails`,
      },
      contactDetailsHidden: (option) => {
        return {
          option: `#respondents1_${index}_party_contactDetailsHidden-${option}`,
          reason: `#respondents1_${index}_party_contactDetailsHiddenReason`,
        };
      },
    };
  },
  addRespondentButton: '#respondents1 > div:nth-child(1) > button:nth-child(2)',

  addRespondent() {
    I.click(this.addRespondentButton);
  },

  async enterRespondent(respondent) {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).respondent.firstName, respondent.firstName);
    I.fillField(this.fields(elementIndex).respondent.lastName, respondent.lastName);
    I.fillField(this.fields(elementIndex).respondent.dateOfBirth.day, respondent.dob.day);
    I.fillField(this.fields(elementIndex).respondent.dateOfBirth.month, respondent.dob.month);
    I.fillField(this.fields(elementIndex).respondent.dateOfBirth.year, respondent.dob.year);
    I.selectOption(this.fields(elementIndex).respondent.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields(elementIndex).respondent.genderIdentification, '');
    }
    I.fillField(this.fields(elementIndex).respondent.placeOfBirth, respondent.placeOfBirth);
    within(this.fields(elementIndex).respondent.address, () => {
      if (elementIndex === 0) {
        postcodeLookup.lookupPostcode(respondent.address);
      } else {
        postcodeLookup.enterAddressManually(respondent.address);
      }
    });
    I.fillField(this.fields(elementIndex).respondent.telephone, respondent.telephone);
  },

  async enterRelationshipToChild(relationship) {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).respondent.relationshipToChild, relationship);
  },

  async enterContactDetailsHidden(option, reason = '') {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).contactDetailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(elementIndex).contactDetailsHidden(option).reason, reason);
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await this.getActiveElementIndex();

    litigationIssue = litigationIssue.toLowerCase();
    switch (litigationIssue) {
      case 'yes':
        I.checkOption(this.fields(elementIndex).respondent.litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields(elementIndex).respondent.litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields(elementIndex).respondent.litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields(elementIndex).respondent.litigationIssuesDetails, litigationIssueDetail);
    }
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
