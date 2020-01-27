const {I} = inject();

module.exports = {
  fields: function (party, index) {
    return {
      direction: {
        complied: {
          yes: `#${party}_${index}_response_complied-Yes`,
          no: `#${party}_${index}_response_complied-No`,
        },
        file: `#${party}_${index}_response_file`,
        documentDetails: `#${party}_${index}_response_documentDetails`,
        cannotComplyReason: `#${party}_${index}_response_cannotComplyReason`,
        c2Upload: `#${party}_${index}_response_c2Uploaded-UPLOADED`,
        supportingFile: `#${party}_${index}_response_cannotComplyFile`,
        supportingDocumentDetails: `#${party}_${index}_response_cannotComplyDocumentDetails`,
      },
    };
  },

  async canComplyWithDirection(party, index = 0, response, file) {
    I.click(this.fields(party, index).direction.complied.yes);
    I.attachFile(this.fields(party, index).direction.file, file);
    I.fillField(this.fields(party, index).direction.documentDetails, response.complied.yes.documentDetails);
  },

  async cannotComplyWithDirection(party, index = 0, response, file) {
    I.click(this.fields(party, index).direction.complied.no);
    I.fillField(this.fields(party, index).direction.cannotComplyReason, response.complied.no.cannotComplyReason);
    I.checkOption(this.fields(party, index).direction.c2Upload);
    I.attachFile(this.fields(party, index).direction.supportingFile, file);
  },
};
