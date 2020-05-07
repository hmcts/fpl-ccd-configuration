const axios = require('axios');
const config = require('../config');

const normalizeCaseId = caseId => caseId.replace(/\D/g, '');

const populateWithData = async (caseId, data) => {
  const authToken = await getAuthToken();
  await axios.post(`${config.fplServiceUrl}/testingSupport/populateCase/${normalizeCaseId(caseId)}`, data,
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
