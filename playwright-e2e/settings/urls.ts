// aat, demo, perftest, ithc
const env = process.env.ENVIRONMENT || "aat";
const e2ePw: string = process.env.E2E_TEST_PASSWORD || "";
const defaultPwd: string = process.env.SYSTEM_UPDATE_USER_PASSWORD || "";
const judgePwd: string = process.env.E2E_TEST_JUDGE_PASSWORD || "";

interface UrlConfig {
  [key: string]: string;
}

export const urlConfig: UrlConfig = {
  env: env,
  idamUrl: process.env.IDAM_API_URL || `https://idam-api.${env}.platform.hmcts.net`,
  serviceUrl: process.env.CASE_SERVICE_URL || `http://fpl-case-service-${env}.service.core-compute-${env}.internal`,
  frontEndBaseURL: process.env.FE_BASE_URL || `https://manage-case.${env}.platform.hmcts.net`,
  serviceAuthURL: process.env.S2S_URL || `http://rpe-service-auth-provider-${env}.service.core-compute-${env}.internal/testing-support/lease`,
  manageOrgServiceUrl: process.env.MANAGE_ORG_SERVICE_URL ||`http://rd-professional-api-${env}.service.core-compute-${env}.internal/`,
  accessManagementUrl: process.env.ACCESS_MANAGEMENT_URL || `http://am-role-assignment-service-${env}.service.core-compute-${env}.internal/am/role-assignments`,
  idamwebURL: process.env.IDAM_WEB_URL ||`https://idam-web-public.${env}.platform.hmcts.net`


};
