import dotenv from "dotenv";
dotenv.config();

const e2ePw = process.env.E2E_TEST_PASSWORD || 'Password1234';
const defaultPwd = process.env.SYSTEM_UPDATE_USER_PASSWORD || 'Password12';
const judgePwd = process.env.E2E_TEST_JUDGE_PASSWORD || 'Hmcts1234';


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
export const judgeWalesUser = {
  email: 'judge-wales@ejudiciary.net',
  password: judgePwd,
};
export const secondJudgeWalesUser = {
  email: 'EMP267006@ejudiciary.net',
  password: judgePwd,
};

export const privateSolicitorOrgUser = {
  email: process.env.PRIVATE_SOLICITOR_ORG_USER_USERNAME || 'private.solicitors@mailinator.com',
  password: process.env.PRIVATE_SOLICITOR_ORG_USER_PASSWORD || defaultPwd,
};

export const FPLSolicitorOrgUser = {
  email: process.env.FPL_SOLICITOR_ORG_USER_USERNAME || 'solicitoroneorg2@mailinator.com',
  password: process.env.FPL_SOLICITOR_ORG_USER_PASSWORD || defaultPwd,
};
