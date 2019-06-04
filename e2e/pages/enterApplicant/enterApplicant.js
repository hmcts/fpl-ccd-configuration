const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: {
    applicant: {
      name: '#applicant_name',
      nameOfPersonToContact: '#applicant_personToContact',
      jobTitle: '#applicant_jobTitle',
      address: '#applicant_address_address',
      mobileNumber: '#applicant_mobile',
      telephoneNumber: '#applicant_telephone',
      email: '#applicant_email',
      pbaNumber: '#applicant_pbaNumber',
    },
    solicitor: {
      name: '#solicitor_name',
      mobileNumber: '#solicitor_mobile',
      telephoneNumber: '#solicitor_telephone',
      email: '#solicitor_email',
      dx: '#solicitor_dx',
      reference: '#solicitor_reference',
    },
  },

  enterApplicantDetails(applicant) {
    I.fillField(this.fields.applicant.name, applicant.name);
    I.fillField(this.fields.applicant.nameOfPersonToContact, applicant.nameOfPersonToContact);
    I.fillField(this.fields.applicant.jobTitle, applicant.jobTitle);
    within(this.fields.applicant.address, () => {
      postcodeLookup.enterAddressManually(applicant.address);
    });
    I.fillField(this.fields.applicant.mobileNumber, applicant.mobileNumber);
    I.fillField(this.fields.applicant.telephoneNumber, applicant.telephoneNumber);
    I.fillField(this.fields.applicant.email, applicant.email);
    I.fillField(this.fields.applicant.pbaNumber, applicant.pbaNumber);
  },

  enterSolicitorDetails(solicitor) {
    I.fillField(this.fields.solicitor.name, solicitor.name);
    I.fillField(this.fields.solicitor.mobileNumber, solicitor.mobileNumber);
    I.fillField(this.fields.solicitor.telephoneNumber, solicitor.telephoneNumber);
    I.fillField(this.fields.solicitor.email, solicitor.email);
    I.fillField(this.fields.solicitor.dx, solicitor.dx);
    I.fillField(this.fields.solicitor.reference, solicitor.reference);
  },
};
