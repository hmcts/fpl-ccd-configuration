const axios = require('axios');
const config = require('../config');

const normalizeCaseId = caseId => caseId.replace(/\D/g, '');
const documentData = filename => {
  return {
    document_url: `${config.dmStoreUrl}/documents/fakeUrl`,
    document_filename: filename,
    document_binary_url: `${config.dmStoreUrl}/documents/fakeUrl/binary`,
  };
};

const populateWithData = async (caseId, data) => {
  updateCaseDataWithTodaysDateTime(data.caseData);
  updateCaseDataWithDocuments(data.caseData);

  const authToken = await getAuthToken();
  await axios.post(`${config.fplServiceUrl}/testing-support/case/populate/${normalizeCaseId(caseId)}`, data,
    {
      headers: {
        'Authorization': `Bearer ${authToken}`,
      },
    }).catch(e => {
    console.log('Update case request failed:');
    console.log(e.response.data);
    throw e;
  });
};

const updateCaseDataWithTodaysDateTime = (caseData) => {
  const dateTime = new Date().toISOString();
  caseData.dateSubmitted = dateTime.slice(0, 10);
  caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);
};

const updateCaseDataWithDocuments = (caseData) => {
  caseData.submittedForm = documentData('mockSubmittedForm.pdf');
  caseData.documents_checklist_document.typeOfDocument = documentData('mockChecklist.pdf');
  caseData.documents_threshold_document.typeOfDocument = documentData('mockThreshold.pdf');
  caseData.documents_socialWorkCarePlan_document.typeOfDocument = documentData('mockSWCP.pdf');
  caseData.documents_socialWorkAssessment_document.typeOfDocument = documentData('mockSWA.pdf');
  caseData.documents_socialWorkChronology_document.typeOfDocument = documentData('mockSWC.pdf');
  caseData.documents_socialWorkEvidenceTemplate_document.typeOfDocument = documentData('mockSWET.pdf');
  if (caseData.standardDirectionOrder) {
    caseData.standardDirectionOrder.orderDoc = documentData('sdo.pdf');
  }
};

const getAuthToken = async () => {
  const response = await axios.post(`${config.idamApiUrl}/loginUser?username=${config.systemUpdateUser.email}&password=${config.systemUpdateUser.password}`, {},
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    }).catch(e => {
    console.log('IDAM call for auth token failed:');
    console.log(e.response.data);
    throw e;
  });

  return response.data.access_token;
};

module.exports = {
  populateWithData,
};
