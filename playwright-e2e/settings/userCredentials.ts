

const e2ePw = process.env.E2E_TEST_PASSWORD || 'Password1234';
const defaultPwd = process.env.SYSTEM_UPDATE_USER_PASSWORD || 'Password12';


export const newSwanseaLocalAuthorityUserOne = {
  email: 'local-authority-swansea-0001@maildrop.cc',
  password: e2ePw,
};

export const systemUpdateUser ={
  email: process.env.SYSTEM_UPDATE_USER_USERNAME  || 'fpl-system-update@mailnesia.com',
  password: process.env.SYSTEM_UPDATE_USER_PASSWORD || defaultPwd,

};
export const  CTSCUser = {
  email: process.env.CTSC_USERNAME || 'fpl-ctsc-admin@justice.gov.uk',
  password: defaultPwd,
};
export const  judgeUser = {
  email: process.env.JUDGE_USERNAME || 'judiciary-only@mailnesia.com',
  password: defaultPwd,

};
