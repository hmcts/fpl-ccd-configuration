const config = require('../config');
const fetch = require('node-fetch');
const lodash = require('lodash');
const output = require('codeceptjs').output;
const requestModule = require('request-promise-native');
const fs = require('fs');

const request = requestModule.defaults();
const wait = duration => new Promise(resolve => setTimeout(resolve, duration));

const post = async (url, data, headers, retry = 2, backoff = 500) => {
  output.debug(`Performing post to ${url}`);
  return fetch(url, {
    method: 'POST',
    body: JSON.stringify(data),
    headers: headers,
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

const get = async (url, headers, retry = 2, backoff = 500) => {
  output.debug(`Performing get to ${url}`);
  return fetch(url, {
    method: 'GET',
    headers: headers,
  }).then(res => {
    if (res.ok) {
      return res;
    } else {
      if (retry > 0) {
        return wait(backoff).then(() => get(url, headers, retry - 1, backoff * 1.5));
      }
      throw {message: `GET ${url} failed with ${res.status}`};
    }
  });
};

const updateCaseDataWithTodaysDateTime = (data) => {
  let caseData = data.caseData;
  const dateTime = new Date().toISOString();
  caseData.dateSubmitted = dateTime.slice(0, 10);
  caseData.dateAndTimeSubmitted = dateTime.slice(0, -1);
};

const updateCaseDataWithPlaceholders = async (data) => {
  const {document_binary_url, document_url} = await getTestDocument();
  const placeholders = {
    SWANSEA_ORG_ID: config.swanseaOrgId,
    TEST_DOCUMENT_URL: document_url,
    TEST_DOCUMENT_BINARY_URL: document_binary_url,
  };
  const caseData = lodash.template(JSON.stringify(data.caseData))(placeholders);
  return {state: data.state, caseData: JSON.parse(caseData)};
};

const getHeaders = authToken => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${authToken}`,
});

const populateWithData = async (caseId, data) => {
  updateCaseDataWithTodaysDateTime(data);
  data = await updateCaseDataWithPlaceholders(data);

  const authToken = await getAuthToken();
  const url = `${config.fplServiceUrl}/testing-support/case/populate/${caseId}`;
  const headers = getHeaders(authToken);

  return post(url, data, headers);
};

const createCase = async (user, caseName) => {
  const authToken = await getAuthToken(user);
  const url = `${config.fplServiceUrl}/testing-support/case/create`;
  const headers = getHeaders(authToken);
  const data = {caseName: caseName};
  const response = await post(url, data, headers);
  return await response.json();
};

const getTestDocument = async () => {
  const url = `${config.fplServiceUrl}/testing-support/test-document`;
  return getAuthToken()
    .then(token => get(url, getHeaders(token)))
    .then(response => response.json());
};

const getUser = async (user) => {
  const authToken = await getAuthToken();
  const url = `${config.fplServiceUrl}/testing-support/user`;
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authToken}`,
  };
  let response = await post(url, user, headers);
  return await response.json();
};

const grantCaseAccess = async (caseId, user, role) => {
  const authToken = await getAuthToken();
  const url = `${config.fplServiceUrl}/testing-support/case/${caseId}/access`;
  const headers = getHeaders(authToken);
  const data = {email: user.email, password: user.password, role: role};
  return await post(url, data, headers);
};

const getLastEvent = async (caseId) => {
  const authToken = await getAuthToken();
  const url = `${config.fplServiceUrl}/testing-support/case/${caseId}/lastEvent`;
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${authToken}`,
  };
  let response = await get(url, headers);
  return await response.json();
};

const pollLastEvent = async (caseId, event) => {
  let tryNumber = 0;

  while (++tryNumber < 10) {
    let lastEvent = await getLastEvent(caseId);

    if (!lastEvent || (lastEvent.id !== event)) {
      await wait(2000);
    } else {
      output.print(`Last event ${event} found in try ${tryNumber}`);
      return;
    }
  }
  output.print(`Last event ${event} not found in ${tryNumber} tries`);
};

const getAuthToken = async (user = config.systemUpdateUser) => {
  const url = `${config.idamApiUrl}/loginUser?username=${user.email}&password=${user.password}`;
  const data = {};
  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
  };

  return post(url, data, headers)
    .then(response => response.json())
    .then(data => data.access_token);
};

async function getServiceTokenForSecret(service) {
  // logger.info('Getting Service Token');
  // logger.log(`Getting Service Token${service}`);
  // logger.log(`Getting Service Token${serviceSecret}`);

  const s2sBaseUrl = `${config.s2sBaseURL}`;
  const s2sAuthPath = '/testing-support/lease';
  // eslint-disable-next-line global-require
  // const oneTimePassword = require('otp')({ secret: serviceSecret }).totp();
  /* logger.log(`Getting The one time password${oneTimePassword}`);
  logger.log(`Getting The one time password :${s2sBaseUrl}`);*/
  const serviceToken = await request({
    method: 'POST',
    uri: s2sBaseUrl + s2sAuthPath,
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({microservice: service}),
  });
  // logger.debug(serviceToken);
  // logger.log(serviceToken);
  return serviceToken;
}

async function getUserId(authToken) {
  //logger.info('Getting User Id');
  const idamBaseUrl = `${config.idamApiUrl}`;
  const idamDetailsPath = '/details';
  const userDetails = await request.get({
    uri: idamBaseUrl + idamDetailsPath,
    headers: {
      Authorization: `Bearer ${authToken}`,
    },
  });
  return JSON.parse(userDetails).id;
}

async function getCCDCaseData(authToken, serviceToken, userId, caseId) {
  const url = `${config.ccdDataStoreApiUrl}/caseworkers/${userId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/${caseId}`;
  const ccdCaseData = await request.get({
    uri: url,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`,
      'ServiceAuthorization': `Bearer ${serviceToken}`,
    },
  });
  console.log(`The value of the response ${ccdCaseData}`);
  return JSON.parse(ccdCaseData);
}

async function updateCaseForEvent(authToken, serviceToken, userId, caseId, eventId, dataLocation) {

  const ccdApiUrl = `${config.ccdDataStoreApiUrl}`;
  // eventId ==  ordersNeeded
  const ccdStartEventPath = `/caseworkers/${userId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/${caseId}/event-triggers/${eventId}/token`;
  const ccdSaveEventPath = `/caseworkers/${userId}/jurisdictions/PUBLICLAW/case-types/CARE_SUPERVISION_EPO/cases/${caseId}/events`;

  const startEventOptions = {
    method: 'GET',
    uri: ccdApiUrl + ccdStartEventPath,
    headers: {
      'Authorization': `Bearer ${authToken}`,
      'ServiceAuthorization': `Bearer ${serviceToken}`,
      'Content-Type': 'application/json',
    },
  };
  const startEventResponse = await request(startEventOptions);
  const eventToken = JSON.parse(startEventResponse).token;
  console.log('The value of the eventToken : ' + eventToken);

  // 2nd Call
  console.log(`Directory name is ${__dirname}`);
  console.log('The value of the Dir : '+fs.__dirname);
  var data = fs.readFileSync(dataLocation);
  console.log('The value of the data : ' + data);
  var saveBody = {
    data: JSON.parse(data),
    event: {
      id: eventId,
      summary: 'Updating Case',
      description: 'For CCD E2E Test',
    },
    'event_token': `${eventToken}`,
  };

  const saveEventOptions = {
    method: 'POST',
    uri: ccdApiUrl + ccdSaveEventPath,
    headers: {
      'Authorization': `Bearer ${authToken}`,
      'ServiceAuthorization': `Bearer ${serviceToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(saveBody),
  };

  const saveEventResponse = await request(saveEventOptions);
  return saveEventResponse;
}

module.exports = {
  populateWithData,
  createCase,
  grantCaseAccess,
  getUser,
  pollLastEvent,
  getAuthToken,
  getUserId,
  getServiceTokenForSecret,
  getCCDCaseData,
  updateCaseForEvent,
};
