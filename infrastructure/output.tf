output "microserviceName" {
  value = "${var.component}"
}

output "vaultName" {
  value = "${local.vault_name}"
}

output "vaultUri" {
  value = "${data.azurerm_key_vault.key_vault.vault_uri}"
}
