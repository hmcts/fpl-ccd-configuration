const e2ePw = process.env.E2E_TEST_PASSWORD || '';

export const newSwanseaLocalAuthorityUserOne = {
  email: 'local-authority-swansea-0001@maildrop.cc',
  password: e2ePw,
};
export const systemUpdateUser ={
  email: process.env.SYSTEM_UPDATE_USER_USERNAME  || '',
  password: process.env.SYSTEM_UPDATE_USER_PASSWORD || '',

};
