output "microserviceName" {
  value = "${var.component}"
}

output "vaultName" {
  value = "${local.vault_name}"
}

output "vaultUri" {
  value = "${module.key-vault.key_vault_uri}"
}
