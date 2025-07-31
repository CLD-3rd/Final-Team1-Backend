terraform {
  required_providers {
    helm = {
      source = "hashicorp/helm"
      version = ">= 2.12.0"
      configuration_aliases = [helm.eks]  # ← alias 등록!
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.20.0"
    }
  }
  required_version = ">= 1.3.0"
}
