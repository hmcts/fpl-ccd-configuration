const {I} = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');
const output = require('codeceptjs').output;

async function clearOldSolicitorOrg() {
  I.waitForElement(locate('h2').withText('Search for an organisation'));
  const selectedTable = locate('#organisation-selected-table');
  const numOfElements = await I.grabNumberOfVisibleElements(selectedTable);

  if (numOfElements !== 0) {
    output.debug('Clearing old solicitor info');
    I.click(locate('a').withText('Clear').inside(selectedTable));
    I.waitForInvisible(selectedTable);
  }
}

module.exports = {
  fields: function (index) {
    return {
      mainSolicitor: {
        childrenHaveLegalRepresentation: {
          group: '#childrenHaveRepresentation',
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
        childrenHaveSameRepresentation: {
          group: '#childrenHaveSameRepresentation',
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
        firstName: '#childrenMainRepresentative_firstName',
        lastName: '#childrenMainRepresentative_lastName',
        email: '#childrenMainRepresentative_email',
      },
      childSolicitor: {
        id: `#childRepresentationDetails${index}_childRepresentationDetails${index}`,
        useMainSolicitor: {
          group: `#childRepresentationDetails${index}_useMainSolicitor`,
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
        specificSolicitor: {
          firstName: `#childRepresentationDetails${index}_solicitor_firstName`,
          lastName: `#childRepresentationDetails${index}_solicitor_lastName`,
          email: `#childRepresentationDetails${index}_solicitor_email`,
        },
        unregisteredOrganisation: {
          name: `#childRepresentationDetails${index}_solicitor_unregisteredOrganisation_name`,
          address: `#childRepresentationDetails${index}_solicitor_unregisteredOrganisation_address_address`,
        },
      },
      child: {
        firstName: `#children1_${index}_party_firstName`,
        lastName: `#children1_${index}_party_lastName`,
        dateOfBirth: `(//*[contains(@class, "collection-title")])[${ index + 1 }]/parent::div//*[@id="dateOfBirth"]`,
        addressChangeDate: `(//*[contains(@class, "collection-title")])[${ index + 1 }]/parent::div//*[@id="addressChangeDate"]`,
        address: `#children1_${index}_party_address_address`,
        gender: `#children1_${index}_party_gender`,
        genderIdentification: `#children1_${index}_party_genderIdentification`,
        situation: {
          radioGroup: `#children1_${index}_party_livingSituation`,
          situationDetails: `#children1_${index}_party_livingSituationDetails`,
          dateStartedStaying: {
            day: `#children1_${index}_party_addressChangeDate-day`,
            month: `#children1_${index}_party_addressChangeDate-month`,
            year: `#children1_${index}_party_addressChangeDate-year`,
          },
          addressOfChild: `div[id="children1_${index}_party_address_address"]`,
        },
        keyDates: `#children1_${index}_party_keyDates`,
        careAndContactPlan: `#children1_${index}_party_careAndContactPlan`,
        adoptionNo: `#children1_${index}_party_adoption_No`,
        mothersName: `#children1_${index}_party_mothersName`,
        fathersName: `#children1_${index}_party_fathersName`,
        fatherResponsible: `#children1_${index}_party_fathersResponsibility`,
        socialWorkerName: `#children1_${index}_party_socialWorkerName`,
        socialWorkerTel: `#children1_${index}_party_socialWorkerTelephoneNumber_telephoneNumber`,
        additionalNeedsNo: `#children1_${index}_party_additionalNeeds_No`,
        contactHiddenNo: `#children1_${index}_party_detailsHidden_No`,
        contactHiddenYes: `#children1_${index}_party_detailsHidden_Yes`,
        litigationIssues: {
          yes: `#children1_${index}_party_litigationIssues-YES`,
          no: `#children1_${index}_party_litigationIssues-NO`,
          dont_know: `#children1_${index}_party_litigationIssues-DONT_KNOW`,
        },
        litigationIssuesDetails: `#children1_${index}_party_litigationIssuesDetails`,
      },
    };
  },

  async enterChildDetails(firstName, lastName, day, month, year, gender = 'Boy') {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).child.firstName, firstName);
    I.fillField(this.fields(elementIndex).child.lastName, lastName);

    I.fillDate({'day': day, 'month': month, 'year': year}, this.fields(elementIndex).child.dateOfBirth );
    I.selectOption(this.fields(elementIndex).child.gender, gender);
  },

  async defineChildSituation(day, month, year) {
    const elementIndex = await this.getActiveElementIndex();

    await within(this.fields(elementIndex).child.situation.radioGroup, () => {
      I.click(locate('label').withText('Living with respondents'));
    });
    await I.runAccessibilityTest();
    I.fillDate({'day': day, 'month': month, 'year': year}, this.fields(elementIndex).child.addressChangeDate);
  },

  async enterAddress(address) {
    const elementIndex = await this.getActiveElementIndex();

    await within(this.fields(elementIndex).child.situation.addressOfChild, () => {
      //XXX removed postcode lookup due to instability
      postcodeLookup.enterAddressManually(address);
    });
  },

  async enterKeyDatesAffectingHearing(keyDates = 'Tuesday the 11th') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.keyDates, keyDates);
  },

  async enterSummaryOfCarePlan(carePlan = 'care plan summary') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.careAndContactPlan, carePlan);
  },

  async defineAdoptionIntention() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).child.adoptionNo);
  },

  async enterParentsDetails(fatherResponsible = 'Yes', motherName = 'Laura Smith', fatherName = 'David Smith') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.mothersName, motherName);
    I.fillField(this.fields(elementIndex).child.fathersName, fatherName);
    I.selectOption(this.fields(elementIndex).child.fatherResponsible, fatherResponsible);
  },

  async enterSocialWorkerDetails(socialWorkerName = 'James Jackson', socialWorkerTel = '01234567890') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.socialWorkerName, socialWorkerName);
    I.fillField(this.fields(elementIndex).child.socialWorkerTel, socialWorkerTel);
  },

  async defineChildAdditionalNeeds() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).child.additionalNeedsNo);
  },

  async enterContactDetailsHidden(hideContactDetails) {
    const elementIndex = await this.getActiveElementIndex();

    switch (hideContactDetails) {
      case 'Yes':
        I.click(this.fields(elementIndex).child.contactHiddenYes);
        break;
      case 'No':
        I.click(this.fields(elementIndex).child.contactHiddenNo);
        break;
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await this.getActiveElementIndex();

    litigationIssue = litigationIssue.toLowerCase();
    switch (litigationIssue) {
      case 'yes':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields(elementIndex).child.litigationIssuesDetails, litigationIssueDetail);
    }
  },

  selectAnyChildHasLegalRepresentation(answer) {
    I.click(`${this.fields().mainSolicitor.childrenHaveLegalRepresentation.group}_${answer}`);
  },

  selectChildrenHaveSameRepresentation(answer) {
    I.click(`${this.fields().mainSolicitor.childrenHaveSameRepresentation.group}_${answer}`);
  },

  async selectChildUseMainRepresentation(answer, index, child) {
    await within(this.fields(index).childSolicitor.id, () => I.see(`Child ${index + 1} - ${child.firstName} ${child.lastName}`));
    I.click(`${this.fields(index).childSolicitor.useMainSolicitor.group}_${answer}`);
  },

  enterChildrenMainRepresentation(solicitor) {
    I.fillField(this.fields().mainSolicitor.firstName, solicitor.forename);
    I.fillField(this.fields().mainSolicitor.lastName, solicitor.surname);
    I.fillField(this.fields().mainSolicitor.email, solicitor.email);
  },

  async enterRegisteredOrganisation(solicitor) {
    await clearOldSolicitorOrg();
    I.waitForEnabled('#search-org-text');
    I.fillField('#search-org-text', solicitor.organisation);
    I.click(locate('a').withText('Select').inside(locate('#organisation-table').withDescendant(locate('h3').withText(solicitor.organisation))));
  },

  enterChildrenSpecificRepresentation(index, solicitor) {
    I.fillField(this.fields(index).childSolicitor.specificSolicitor.firstName, solicitor.forename);
    I.fillField(this.fields(index).childSolicitor.specificSolicitor.lastName, solicitor.surname);
    I.fillField(this.fields(index).childSolicitor.specificSolicitor.email, solicitor.email);
  },

  async enterSpecificRegisteredOrganisation(index, solicitor) {
    await within(`#childRepresentationDetails${index}_childRepresentationDetails${index}`, async () => await this.enterRegisteredOrganisation(solicitor));
  },

  async enterSpecificUnregisteredOrganisation(index, solicitor) {
    const indexedFields = this.fields(index);

    await within(indexedFields.childSolicitor.id, async () => {
      await clearOldSolicitorOrg();
      I.fillField(indexedFields.childSolicitor.unregisteredOrganisation.name, solicitor.unregisteredOrganisation.name);
      postcodeLookup.enterAddressManually(solicitor.unregisteredOrganisation.address);
    });
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
