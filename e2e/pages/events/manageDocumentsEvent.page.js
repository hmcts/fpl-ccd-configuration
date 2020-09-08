// const { I } = inject();
//
// module.exports = {
//   fields: function(index, supportingDocumentType) {
//     return {
//       documentType: {
//         furtherEvidence: '#manageDocument_type-FURTHER_EVIDENCE_DOCUMENTS',
//         correspondence: '#manageDocument_type-CORRESPONDENCE',
//         c2: '#manageDocument_type-C2'
//       },
//
//       supportingEvidenceDocuments: {
//         name: `#${supportingDocumentType}_${index}_name`,
//         notes: `#${supportingDocumentType}_${index}_notes`,
//         dateAndTime: {
//           day: `${supportingDocumentType}_${index}_dateTimeReceived-day`,
//           month: `${supportingDocumentType}_${index}_dateTimeReceived-month`,
//           year: `${supportingDocumentType}_${index}_dateTimeReceived-year`,
//           hour: `${supportingDocumentType}_${index}_dateTimeReceived-hour`,
//           minute: `${supportingDocumentType}_${index}_dateTimeReceived-minute`,
//           second: `${supportingDocumentType}_${index}_dateTimeReceived-second`,
//         },
//         document: `${supportingDocumentType}_${index}_document`,
//       },
//     };
//   },
//
//   selectFurtherEvidence() {
//     I.click(this.fields(elementIndex, type).documentType.furtherEvidence);
//   },
//
//   selectCorrespondence() {
//     I.click(this.fields(elementIndex, type).documentType.correspondence);
//   },
//
//   selectC2SupportingDocuments() {
//     I.click(this.fields(elementIndex, type).documentType.c2);
//   },
//
//   enterDocumentName(documentName) {
//     I.fillField(this.fields(elementIndex, type).name, documentName);
//   },
//
//   enterDocumentName(notes) {
//     I.fillField(this.fields(elementIndex, type).notes, notes);
//   },
//
//   uploadDocument() {
//     I.fillField(this.fields(elementIndex, type).document, 'Document');
//   },
// };
