provider "azurerm" {
  version = "=1.44.0"
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

#Copying appinsights key to the valut
resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = "${azurerm_application_insights.appinsights.instrumentation_key}"
  key_vault_id = "${module.key-vault.key_vault_id}"
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

data "azurerm_key_vault_secret" "ld-sdk-key" {
  name      = "ld-sdk-key"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

data "azurerm_key_vault_secret" "ctsc-inbox" {
  name      = "ctsc-inbox"
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