const e2ePw = process.env.E2E_TEST_PASSWORD || 'Password1234';

export const newSwanseaLocalAuthorityUserOne = {
  email: 'local-authority-swansea-0001@maildrop.cc',
  password: e2ePw,
};
export const systemUpdateUser ={
  email: process.env.SYSTEM_UPDATE_USER_USERNAME  || '',
  password: process.env.SYSTEM_UPDATE_USER_PASSWORD || '',

};
export const  CTSCUser = {
  email: process.env.CTSC_USERNAME || '',
  password: process.env.CTSC_PASSWORD || '',
};
