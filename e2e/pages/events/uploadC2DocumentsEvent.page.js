const { I } = inject();

module.exports = {
  documents: {
    c2: '#tempC2_c2upload',
  },

  fields: {
    c2Description: '#tempC2_c2UploadDescription',
  },

  uploadc2(file, description) {
    I.attachFile(this.documents.c2, file);
    I.fillField(this.fields.c2Description, description);
  },
};
