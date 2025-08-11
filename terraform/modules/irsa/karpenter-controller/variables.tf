variable "team_name" {
  type        = string
  description = "Prefix for naming resources"
}

variable "oidc_provider_arn" {
  type        = string
  description = "OIDC provider ARN for IRSA"
}

variable "oidc_provider_url" {
  type        = string
  description = "OIDC provider URL for IRSA (without https://)"
}

variable "cluster_name" {
  type        = string
  description = "EKS 클러스터 이름"
}