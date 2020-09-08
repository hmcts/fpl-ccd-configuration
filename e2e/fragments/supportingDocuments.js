
module.exports = {

  supportingDocuments (index, document) {
    return {
      name: `#${document}_supportingEvidenceBundle_${index}_name`,
      notes: `#${document}_supportingEvidenceBundle_${index}_notes`,
      dateAndTime: {
        day: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-day`,
        month: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-month`,
        year: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-year`,
        hour: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-hour`,
        minute: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-minute`,
        second: `#${document}_supportingEvidenceBundle_${index}_dateTimeReceived-second`,
      },
      document: `#${document}_supportingEvidenceBundle_${index}_document`,
    };
  },
};
