/*global process*/
const config = require('../config');
const fetch = require('node-fetch');
const HttpsProxyAgent = require('https-proxy-agent');
const HttpProxyAgent = require('http-proxy-agent');

const proxy = process.env.proxy || process.env.http_proxy || process.env.https_proxy;

const getAgent = function (url) {
  if(proxy){
    return url.startsWith('https:') ? new HttpsProxyAgent(proxy) : new HttpProxyAgent(proxy);
  }
};

const wait = duration => new Promise(resolve => setTimeout(resolve, duration));

const post = async (url, data, headers, retry = 2, backoff = 500) => {
  return fetch(url, {
    method: 'POST',
    body: JSON.stringify(data),
    headers: headers,
    agent: getAgent(url),
  }).then(res => {
    if (res.ok) {
      return res;
    } else {
      if (retry > 0) {
        return wait(backoff).then(() => post(url, data, headers, retry - 1, backoff * 1.5));
      }
      throw {message: `POST ${url} failed with ${res.status}`};
    }
  });
};

const documentData = filename => {
  return {
    document_url: `${config.dmStoreUrl}/documents/fakeUrl`,
    document_filename: filename,
    document_binary_url: `${config.dmStoreUrl}/documents/fakeUrl/binary`,
  };
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
  if (caseData.orderCollection) {
    for (const order of caseData.orderCollection) {
      order.value.document = documentData(order.value.type + '.pdf');
    }
  }
  if (caseData.sealedCMOs) {
    for (const cmo of caseData.sealedCMOs) {
      cmo.value.order = documentData('mockFile.pdf');
    }
  }
};

const populateWithData = async (caseId, data) => {
  updateCaseDataWithTodaysDateTime(data.caseData);
  updateCaseDataWithDocuments(data.caseData);

  const authToken = await getAuthToken();
  const url = `${config.fplServiceUrl}/testing-support/case/populate/${caseId}`;
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authToken}`,
  };

  return post(url, data, headers);
};

const getAuthToken = async () => {
  const url = `${config.idamApiUrl}/loginUser?username=${config.systemUpdateUser.email}&password=${config.systemUpdateUser.password}`;
  const data = {};
  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
  };

  return post(url, data, headers)
    .then(response => response.json())
    .then(data => data.access_token);
};

module.exports = {
  populateWithData,
};
