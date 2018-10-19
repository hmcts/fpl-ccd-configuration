const I = actor();

module.exports = {

  fields: (id) => {
    return {
      respondent: {
        name: `#respondent_${id}_name`,
        dob: {
          day: `#respondent_${id}_dob-day`,
          month: `#respondent_${id}_dob-month`,
          year: `#respondent_${id}_dob-year`,
        },
        gender: `#respondent_${id}_gender`,
        genderIdentify: `#respondent_${id}_genderIdentify`,
        placeOfBirth: `#respondent_${id}_placeOfBirth`,
        address: `#respondent_${id}_address`,
        telephone: `#respondent_${id}_telephone`,
        relationshipToChild: `#respondent_${id}_relationshipToChild`,
      },
      contactDetailsHidden: (option) => {
        return {
          option: `#respondent_${id}_contactDetailsHidden-${option}`,
          reason: `#respondent_${id}_contactDetailsHiddenReason`,
        };
      },
      abilityToTakePartInProceedings: (option) => {
        return {
          option: `#respondent_${id}_abilityToTakePartInProceedings-${option}`,
          reason: `#respondent_${id}_abilityToTakePartInProceedingsReason`,
        };
      },
    };
  },
  addOtherRespondent: 'Add new',

  enterRespondent(id, respondent) {
    I.fillField(this.fields(id).respondent.name, respondent.name);
    I.fillField(this.fields(id).respondent.dob.day, respondent.dob.day);
    I.fillField(this.fields(id).respondent.dob.month, respondent.dob.month);
    I.fillField(this.fields(id).respondent.dob.year, respondent.dob.year);
    I.fillField(this.fields(id).respondent.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields(id).respondent.genderIdentify, '');
    }
    I.fillField(this.fields(id).respondent.placeOfBirth, respondent.placeOfBirth);
    I.fillField(this.fields(id).respondent.address, respondent.address);
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

  enterAbilityToTakePartInProceedings(id, option, reason = '') {
    I.click(this.fields(id).abilityToTakePartInProceedings(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(id).abilityToTakePartInProceedings(option).reason, reason);
    }
  },
};
