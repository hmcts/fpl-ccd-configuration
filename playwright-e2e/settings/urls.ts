// aat, demo, perftest, ithc
const env = process.env.ENVIRONMENT || "aat";

interface UrlConfig {
  [key: string]: string;
}

export const urlConfig: UrlConfig = {
  idamUrl: process.env.AAT_IDAM_URL || `https://idam-api.${env}.platform.hmcts.net/`,
  serviceUrl: process.env.AAT_SERVICE_URL || `http://fpl-case-service-${env}.service.core-compute-${env}.internal`,
  frontEndBaseURL: process.env.FE_BASE_URL || `https://manage-case.${env}.platform.hmcts.net/`,
  // You can add other URLs as needed
};
