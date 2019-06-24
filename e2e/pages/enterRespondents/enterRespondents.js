const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  state: {
    context: -1,
  },

  fields: function () {
    const id = this.state.context;

    return {
      respondent: {
        party: {
          partyType: `#respondents1_${id}_party_partyType`,
          title: `#respondents1_${id}_party_title`,
          firstName: `#respondents1_${id}_party_firstName`,
          lastName: `#respondents1_${id}_party_lastName`,
          dateOfBirth: {
            day: `#respondents1_${id}_party_dateOfBirth-day`,
            month: `#respondents1_${id}_party_dateOfBirth-month`,
            year: `#respondents1_${id}_party_dateOfBirth-year`,
          },
          address: `#respondents1_${id}_party_address_address`,
          telephone: `input[id="respondents1_${id}_party_telephoneNumber_telephoneNumber"]`,
          gender: `#respondents1_${id}_party_gender`,
          genderIdentification: `#respondents1_${id}_party_genderIdentification`,
          placeOfBirth: `#respondents1_${id}_party_placeOfBirth`,
          relationshipToChild: `#respondents1_${id}_party_relationshipToChild`,
          litigationIssues: {
            yes: `#respondents1_${id}_party_litigationIssues-YES`,
            no: `#respondents1_${id}_party_litigationIssues-NO`,
            dont_know: `#respondents1_${id}_party_litigationIssues-DONT_KNOW`,
          },
          litigationIssuesDetails: `#respondents1_${id}_party_litigationIssuesDetails`,
        },
      },


      contactDetailsHidden: (option) => {
        return {
          option: `#respondents1_${id}_party_contactDetailsHidden-${option}`,
          reason: `#respondents1_${id}_party_contactDetailsHiddenReason`,
        };
      },
    };
  },
  addRespondentButton: '#respondents1 > div:nth-child(1) > button:nth-child(2)',

  addRespondent() {
    I.click(this.addRespondentButton);
    this.state.context++;
  },

  enterRespondent (respondent) {
    I.fillField(this.fields().respondent.party.partyType, respondent.partyType);
    I.fillField(this.fields().respondent.party.title, respondent.title);
    I.fillField(this.fields().respondent.party.firstName, respondent.firstName);
    I.fillField(this.fields().respondent.party.lastName, respondent.lastName);
    I.fillField(this.fields().respondent.party.dateOfBirth.day, respondent.dob.day);
    I.fillField(this.fields().respondent.party.dateOfBirth.month, respondent.dob.month);
    I.fillField(this.fields().respondent.party.dateOfBirth.year, respondent.dob.year);
    within(this.fields().respondent.party.address, () => {
      postcodeLookup.enterAddressManually(respondent.address);
    });
    I.fillField(this.fields().respondent.party.telephone, respondent.telephone);
    I.selectOption(this.fields().respondent.party.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields().respondent.party.genderIdentification, '');
    }
    I.fillField(this.fields().respondent.party.placeOfBirth, respondent.placeOfBirth);
  },

  enterRelationshipToChild(relationship) {
    I.fillField(this.fields().respondent.party.relationshipToChild, relationship);
  },

  enterContactDetailsHidden(option, reason = '') {
    I.click(this.fields().contactDetailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields().contactDetailsHidden(option).reason, reason);
    }
  },

  enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    litigationIssue = litigationIssue.toLowerCase();
    switch (litigationIssue) {
      case 'yes':
        I.checkOption(this.fields().respondent.party.litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields().respondent.party.litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields().respondent.party.litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields().respondent.party.litigationIssuesDetails, litigationIssueDetail);
    }
  },
};
