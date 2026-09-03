// Terraform import blocks are TRANSIENT.
//
// Add an entry here ONLY to reconcile a resource that already exists in Azure
// but is not yet in Terraform state (fixes "A resource with the ID ... already
// exists" apply errors). Once it has been applied, the entry is redundant and
// should be removed.
//
// Entries are keyed by environment so that a one-off import for a single
// environment cannot affect the plan for any other environment.

locals {
  // env => set of key vault access policy object ids to adopt into state
  managed_identity_access_policy_imports = {
    aat = ["a289f989-29fd-46c0-a590-d4bb2be50d39"]
  }
}

import {
  for_each = toset(lookup(local.managed_identity_access_policy_imports, var.env, []))

  to = module.key-vault.azurerm_key_vault_access_policy.managed_identity_access_policy[each.value]
  id = "${module.key-vault.key_vault_id}/objectId/${each.value}"
}
