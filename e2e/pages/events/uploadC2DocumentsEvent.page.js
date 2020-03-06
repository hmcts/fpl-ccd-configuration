const { I } = inject();

module.exports = {
  fields: {
    uploadC2: '#temporaryC2Document_document',
    description: '#temporaryC2Document_description',
  },
  applicationTypePrefix: '#c2ApplicationType_type-',

  selectApplicationType(type) {
    I.click(this.applicationTypePrefix + type);
  },

  uploadC2Document(file, description) {
    I.attachFile(this.fields.uploadC2, file);
    I.fillField(this.fields.description, description);
  },
};
