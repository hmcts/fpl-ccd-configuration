provider "azurerm" {
  alias = "docmosis"
  subscription_id = "${var.docmosis_subscription_id}"
}

data "azurerm_key_vault" "docmosis_vault" {
  provider = "azurerm.docmosis"
  name = "${var.docmosis_vault}"
  resource_group_name = "${var.docmosis_resource_group}"
}

data "azurerm_key_vault_secret" "docmosis-api-key" {
  provider = "azurerm.docmosis"
  name         = "docmosis-api-key"
  key_vault_id = "${data.azurerm_key_vault.docmosis_vault.id}"
}

resource "azurerm_key_vault_secret" "docmosis-api-key" {
  name         = "docmosis-api-key"
  value        = "${data.azurerm_key_vault_secret.docmosis-api-key.value}"
  key_vault_id =  "${module.key-vault.key_vault_id}"
}
