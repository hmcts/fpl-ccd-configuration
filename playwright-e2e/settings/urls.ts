// aat, demo, perftest, ithc
const env = process.env.ENVIRONMENT || "aat";

interface UrlConfig {
    [key: string]: string;
}

export const urlConfig: UrlConfig = {
  env:env,
  idamUrl: process.env.IDAM_API_URL || `https://idam-api.${env}.platform.hmcts.net`,
  serviceUrl: process.env.CASE_SERVICE_URL || `http://fpl-case-service-${env}.service.core-compute-${env}.internal`,
  frontEndBaseURL: process.env.FE_BASE_URL || `https://manage-case.${env}.platform.hmcts.net`,
  //serviceUrl: 'https://fpl-case-service-pr-5772.preview.platform.hmcts.net',
  //frontEndBaseURL : 'https://xui-fpl-case-service-pr-5772.preview.platform.hmcts.net/cases'
  // You can add other URLs as needed

};
