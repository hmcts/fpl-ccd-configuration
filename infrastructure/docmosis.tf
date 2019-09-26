locals {
  docmosis_key_vault_uri = "https://${var.docmosis_vault_name}.vault.azure.net/"
}

data "azurerm_key_vault_secret" "docmosis_api_key" {
  name         = "docmosis-api-key"
  vault_uri = "${local.docmosis_key_vault_uri}"
}

resource "azurerm_key_vault_secret" "docmosis-api-key" {
  name         = "docmosis-api-key"
  value        = "${data.azurerm_key_vault_secret.docmosis_api_key.value}"
  key_vault_id =  "${module.key-vault.key_vault_id}"
}
