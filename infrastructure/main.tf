provider "azurerm" {}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  instance_size = "${var.env == "prod" || var.env == "sprod" || var.env == "aat" ? "I2" : "I1"}"
  capacity      = "${var.env == "prod" || var.env == "sprod" || var.env == "aat" ? 2 : 1}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.ase_name}"

  vault_name = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "${var.raw_product}-aat" : "${var.raw_product}-saat" : "${var.raw_product}-${var.env}"}"

  # URLs
  IDAM_S2S_AUTH_URL       = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  DOCUMENT_MANAGEMENT_URL = "http://dm-store-${local.local_env}.service.${local.local_ase}.internal"
  CORE_CASE_DATA_API_URL  = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
  RD_PROFESSIONAL_API_URL  = "http://rd-professional-api-${local.local_env}.service.${local.local_ase}.internal"
  DOCMOSIS_API_URL        = "https://docmosis-development.platform.hmcts.net"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"

  tags = "${var.common_tags}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = "${var.appinsights_location}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  application_type    = "Web"

  tags = "${var.common_tags}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name      = "fpl-case-service-s2s-secret"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "idam_client_secret" {
  name      = "fpl-case-service-idam-client-secret"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_email_to_code_mapping" {
  name      = "local-authority-email-to-code-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_code_to_name_mapping" {
  name      = "local-authority-code-to-name-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_user_mapping" {
  name      = "local-authority-user-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_code_to_hmcts_court_mapping" {
  name      = "local-authority-code-to-hmcts-court-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_code_to_cafcass_mapping" {
  name      = "local-authority-code-to-cafcass-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_code_to_shared_inbox_mapping" {
  name      = "local-authority-code-to-shared-inbox-mapping"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_fallback_inbox" {
  name      = "local-authority-fallback-inbox"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "docmosis_api_key" {
  name      = "docmosis-api-key"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "notify_api_key" {
  name      = "notify-api-key"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "system_update_user_username" {
  name      = "system-update-user-username"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "system_update_user_password" {
  name      = "system-update-user-password"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "robotics-notification-recipient" {
  name          = "robotics-notification-recipient"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "robotics-notification-sender" {
  name          = "robotics-notification-sender"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "fpl-${var.env}"
  product                 = "${var.product}"
  env                     = "${var.env}"
  tenant_id               = "${var.tenant_id}"
  object_id               = "${var.jenkins_AAD_objectId}"
  resource_group_name     = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "bb778c38-9e7a-4d03-8dad-4fe0b207e8a3"
  common_tags             = "${var.common_tags}"

  #aks migration
  managed_identity_object_id = "${var.managed_identity_object_id}"
}

module "case-service" {
  source              = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  enable_ase          = "${var.enable_ase}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  subscription        = "${var.subscription}"
  instance_size       = "${local.instance_size}"
  capacity            = "${local.capacity}"
  common_tags         = "${var.common_tags}"
  java_version        = "11"

  app_settings = {
    IDAM_API_URL                                                = "${var.idam_api_url}"
    IDAM_S2S_AUTH_URL                                           = "${local.IDAM_S2S_AUTH_URL}"
    IDAM_S2S_AUTH_TOTP_SECRET                                   = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IDAM_CLIENT_SECRET                                          = "${data.azurerm_key_vault_secret.idam_client_secret.value}"
    IDAM_CLIENT_ID                                              = "${var.idam_client_id}"
    IDAM_CLIENT_REDIRECT_URI                                    = "${var.idam_client_redirect_uri}"
    DOCMOSIS_TORNADO_URL                                        = "${local.DOCMOSIS_API_URL}"
    DOCMOSIS_TORNADO_KEY                                        = "${data.azurerm_key_vault_secret.docmosis_api_key.value}"
    DOCUMENT_MANAGEMENT_URL                                     = "${local.DOCUMENT_MANAGEMENT_URL}"
    CORE_CASE_DATA_API_URL                                      = "${local.CORE_CASE_DATA_API_URL}"
    RD_PROFESSIONAL_API_URL                                     = "${local.RD_PROFESSIONAL_API_URL}"
    CCD_UI_BASE_URL                                             = "${var.ccd_ui_base_url}"
    FPL_LOCAL_AUTHORITY_EMAIL_TO_CODE_MAPPING                   = "${data.azurerm_key_vault_secret.local_authority_email_to_code_mapping.value}"
    FPL_LOCAL_AUTHORITY_CODE_TO_NAME_MAPPING                    = "${data.azurerm_key_vault_secret.local_authority_code_to_name_mapping.value}"
    FPL_LOCAL_AUTHORITY_USER_MAPPING                            = "${data.azurerm_key_vault_secret.local_authority_user_mapping.value}"
    FPL_LOCAL_AUTHORITY_CODE_TO_HMCTS_COURT_MAPPING             = "${data.azurerm_key_vault_secret.local_authority_code_to_hmcts_court_mapping.value}"
    FPL_LOCAL_AUTHORITY_CODE_TO_CAFCASS_MAPPING                 = "${data.azurerm_key_vault_secret.local_authority_code_to_cafcass_mapping.value}"
    FPL_LOCAL_AUTHORITY_CODE_TO_SHARED_INBOX_MAPPING            = "${data.azurerm_key_vault_secret.local_authority_code_to_shared_inbox_mapping.value}"
    FPL_LOCAL_AUTHORITY_FALLBACK_INBOX                          = "${data.azurerm_key_vault_secret.local_authority_fallback_inbox.value}"
    NOTIFY_API_KEY                                              = "${data.azurerm_key_vault_secret.notify_api_key.value}"
    FPL_SYSTEM_UPDATE_USERNAME                                  = "${data.azurerm_key_vault_secret.system_update_user_username.value}"
    FPL_SYSTEM_UPDATE_PASSWORD                                  = "${data.azurerm_key_vault_secret.system_update_user_password.value}"
    ROBOTICS_NOTIFICATION_SENDER                                = "${data.azurerm_key_vault_secret.robotics-notification-sender.value}"
    ROBOTICS_NOTIFICATION_RECIPIENT                             = "${data.azurerm_key_vault_secret.robotics-notification-recipient.value}"
    SPRING_SECURITY_ENABLED                                     = "${var.security_enabled}"
    SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI         = "${var.idam_token_issuer_uri}"
    SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWKSETURI         = "${var.idam_token_jwk_set_uri}"
    SPRING_MAIL_HOST                                            = "${var.mail_host}"
    SPRING_MAIL_PORT                                            = "${var.mail_port}"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE            = "${var.mail_properties_smtp_starttls_enable}"
    SPRING_MAIL_PROPERTIES_MAIL_SMTP_SSL_TRUST                  = "${var.mail_host}"
    GATEWAY_URL                                                 = "${var.gateway_url}"
    FEATURE_TOGGLE_ROBOTICS_CASE_NUMBER_NOTIFICATION_ENABLED    = "${var.feature_toggle_robotics_case_number_notification_enabled}"
    FEATURE_TOGGLE_ROBOTICS_SUPPORT_API_ENABLED                 = "${var.feature_toggle_robotics_support_api_enabled}"
    AUTH_IDAM_CLIENT_BASEURL                                    = "${var.idam_api_url}"
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                        = "${local.IDAM_S2S_AUTH_URL}"

    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false
  }
}
