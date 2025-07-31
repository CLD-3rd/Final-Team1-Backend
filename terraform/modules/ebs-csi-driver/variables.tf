variable "namespace" {
  description = "Namespace to install EBS CSI Driver"
  type        = string
}

variable "chart_version" {
  description = "Helm chart version for EBS CSI Driver"
  type        = string
  default     = "2.30.0"
}

variable "service_account_name" {
  description = "ServiceAccount name for EBS CSI Driver"
  type        = string
  default     = "ebs-csi-controller-sa"
}

variable "irsa_role_arn" {
  description = "IRSA Role ARN for EBS CSI Driver"
  type        = string
}