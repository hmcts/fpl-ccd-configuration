const I = actor();

module.exports = {

  fields: {
    applicant: {
      name: '#enterApplicant_applicant_name',
      nameOfPersonToContact: '#enterApplicant_applicant_personToContact',
      jobTitle: '#enterApplicant_applicant_jobTitle',
      address: '#enterApplicant_applicant_address',
      mobileNumber: '#enterApplicant_applicant_mobile',
      telephoneNumber: '#enterApplicant_applicant_telephone',
      email: '#enterApplicant_applicant_email'
    },
    solicitor: {
      name: '#enterApplicant_solicitor_name',
      mobileNumber: '#enterApplicant_solicitor_mobile',
      telephoneNumber: '#enterApplicant_solicitor_telephone',
      email: '#enterApplicant_solicitor_email',
      dx: '#enterApplicant_solicitor_dx',
      reference: '#enterApplicant_solicitor_reference'
    }
  },

  enterApplicantDetails(applicant) {
    I.fillField(this.fields.applicant.name, applicant.name);
    I.fillField(this.fields.applicant.nameOfPersonToContact, applicant.nameOfPersonToContact);
    I.fillField(this.fields.applicant.jobTitle, applicant.jobTitle);
    I.fillField(this.fields.applicant.address, applicant.address);
    I.fillField(this.fields.applicant.mobileNumber, applicant.mobileNumber);
    I.fillField(this.fields.applicant.telephoneNumber, applicant.telephoneNumber);
    I.fillField(this.fields.applicant.email, applicant.email);
  },

  enterSolicitorDetails(solicitor) {
    I.fillField(this.fields.solicitor.name, solicitor.name);
    I.fillField(this.fields.solicitor.mobileNumber, solicitor.mobileNumber);
    I.fillField(this.fields.solicitor.telephoneNumber, solicitor.telephoneNumber);
    I.fillField(this.fields.solicitor.email, solicitor.email);
    I.fillField(this.fields.solicitor.dx, solicitor.dx);
    I.fillField(this.fields.solicitor.reference, solicitor.reference);
  }
};
