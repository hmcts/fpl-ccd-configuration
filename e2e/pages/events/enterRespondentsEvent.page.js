const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      respondent: {
        firstName: `#respondents1_${index}_party_firstName`,
        lastName: `#respondents1_${index}_party_lastName`,
        dateOfBirth: `(//*[contains(@class, "collection-title")])[${index + 1}]/parent::div//*[@id="dateOfBirth"]`,
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
      solicitor: {
        element: `#respondents1_${index}_solicitor_solicitor`,
        firstName: `#respondents1_${index}_solicitor_firstName`,
        lastName: `#respondents1_${index}_solicitor_lastName`,
        email: `#respondents1_${index}_solicitor_email`,
        regionalOfficeAddress: `#respondents1_${index}_solicitor_regionalOfficeAddress_regionalOfficeAddress`,
        unregisteredOrganisation: {
          name: `#respondents1_${index}_solicitor_unregisteredOrganisation_name`,
          address: `#respondents1_${index}_solicitor_unregisteredOrganisation_address_address`,
        },
      },
      contactDetailsHidden: (option) => {
        return {
          option: `#respondents1_${index}_party_contactDetailsHidden_${option}`,
          reason: `#respondents1_${index}_party_contactDetailsHiddenReason`,
        };
      },
      legalRepresentation: (option) => {
        return {
          option: `#respondents1_${index}_legalRepresentation_${option}`,
        };
      },
    };
  },

  async enterRespondent(respondent) {
    const elementIndex = await I.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).respondent.firstName, respondent.firstName);
    I.fillField(this.fields(elementIndex).respondent.lastName, respondent.lastName);
    I.fillDate(respondent.dob, this.fields(elementIndex).respondent.dateOfBirth);

    I.selectOption(this.fields(elementIndex).respondent.gender, respondent.gender);
    if (respondent.gender === 'They identify in another way') {
      I.fillField(this.fields(elementIndex).respondent.genderIdentification, '');
    }
    await within(this.fields(elementIndex).respondent.address, async () => {
      await postcodeLookup.enterAddressManually(respondent.address);
    });
    I.fillField(this.fields(elementIndex).respondent.telephone, respondent.telephone);
    I.fillField(this.fields(elementIndex).respondent.relationshipToChild, respondent.relationshipToChild);
  },

  async enterRelationshipToChild(relationship) {
    const elementIndex = await I.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).respondent.relationshipToChild, relationship);
  },

  async enterContactDetailsHidden(option, reason = '') {
    const elementIndex = await I.getActiveElementIndex();

    I.click(this.fields(elementIndex).contactDetailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(elementIndex).contactDetailsHidden(option).reason, reason);
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await I.getActiveElementIndex();

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

  async enterRepresentationDetails(option, respondent, index) {
    const elementIndex = (index === undefined) ? await I.getActiveElementIndex() : index;


    I.click(this.fields(elementIndex).legalRepresentation(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(elementIndex).solicitor.firstName, respondent.solicitor.firstName);
      I.fillField(this.fields(elementIndex).solicitor.lastName, respondent.solicitor.lastName);
      I.fillField(this.fields(elementIndex).solicitor.email, respondent.solicitor.email);
    }
  },

  async enterRegisteredOrganisation(respondent, index) {
    const elementIndex = (index === undefined) ? await I.getActiveElementIndex() : index;

    await within(this.fields(elementIndex).solicitor, () => {
      I.fillField('//input[@id="search-org-text"]', respondent.solicitor.organisation);
      I.click('//*[@id="organisation-table"]/caption/h3[text()="Swansea City Council"]/../../tbody//a');
    });

    await within(this.fields(elementIndex).solicitor.regionalOfficeAddress, async () => {
      await postcodeLookup.enterAddressManually(respondent.solicitor.regionalOfficeAddress);
    });
  },

  async updateRegisteredOrganisation(organisationName, index) {
    const elementIndex = (index === undefined) ? await I.getActiveElementIndex() : index;

    await within(this.fields(elementIndex).solicitor.element, () => {
      I.click('Clear');
      I.fillField('.//input[@id="search-org-text"]', organisationName.split(' ')[0]);
      I.click(`.//*[@id="organisation-table"]/caption/h3[text()="${organisationName}"]/../../tbody//a`);
    });
  },

  async enterUnregisteredOrganisation(respondent) {
    const elementIndex = await I.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).solicitor.unregisteredOrganisation.name, respondent.solicitor.unregisteredOrganisation.name);
    await within(this.fields(elementIndex).solicitor.unregisteredOrganisation.address, async () => {
      await postcodeLookup.enterAddressManually(respondent.solicitor.unregisteredOrganisation.address);
    });
  },
};
