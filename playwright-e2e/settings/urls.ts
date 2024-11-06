// aat, demo, perftest, ithc
const env = process.env.ENVIRONMENT || "aat";
const e2ePw : string = process.env.E2E_TEST_PASSWORD ||"Password12";
const defaultPwd : string = process.env.SYSTEM_UPDATE_USER_PASSWORD || "Password1234";
const judgePwd : string = process.env.E2E_TEST_JUDGE_PASSWORD || "Password12";

interface UrlConfig {
    [key: string]: string;
}

export const urlConfig: UrlConfig = {
  env:env,
  idamUrl: process.env.IDAM_API_URL || `https://idam-api.${env}.platform.hmcts.net`,
//serviceUrl: process.env.CASE_SERVICE_URL || `http://fpl-case-service-${env}.service.core-compute-${env}.internal`,
  serviceUrl: process.env.CASE_SERVICE_URL || `https://fpl-case-service-pr-5534.preview.platform.hmcts.net`,
  //frontEndBaseURL: process.env.FE_BASE_URL || `https://manage-case.${env}.platform.hmcts.net`,
  frontEndBaseURL: process.env.FE_BASE_URL || `https://xui-fpl-case-service-pr-5534.preview.platform.hmcts.net`,
  // You can add other URLs as needed

};
