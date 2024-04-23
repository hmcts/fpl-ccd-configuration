import dotenv from "dotenv";
dotenv.config();

const e2ePw = process.env.E2E_TEST_PASSWORD || '';
const defaultPwd = process.env.SYSTEM_UPDATE_USER_PASSWORD || '';
const judgePwd = process.env.E2E_TEST_JUDGE_PASSWORD || '';


export const newSwanseaLocalAuthorityUserOne = {
  email: 'local-authority-swansea-0001@maildrop.cc',
  password: e2ePw,
};

export const systemUpdateUser = {
  email: process.env.SYSTEM_UPDATE_USER_USERNAME || 'fpl-system-update@mailnesia.com',
  password: process.env.SYSTEM_UPDATE_USER_PASSWORD || defaultPwd,

};
export const CTSCUser = {
  email: process.env.CTSC_USERNAME || 'fpl-ctsc-admin@justice.gov.uk',
  password: defaultPwd,
};
export const CTSCTeamLeadUser = {
  email: process.env.CTSC_TL_USERNAME || 'fpl-ctsc-team-leader@justice.gov.uk',
  password: defaultPwd,
};
export const judgeUser = {
  email: process.env.JUDGE_USERNAME || 'judiciary-only@mailnesia.com',
  password: defaultPwd,
};
export const judgeUserWithAdminRole = {
    email: process.env.JUDGE_USER_WITH_ADMIN || 'judiciary@mailnesia.com',
    password: defaultPwd,
};
export const judgeWalesUser = {
  email: 'judge-wales@ejudiciary.net',
  password: judgePwd,
};
export const secondJudgeWalesUser = {
  email: 'EMP267006@ejudiciary.net',
  password: judgePwd,
};
