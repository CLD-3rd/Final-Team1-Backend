variable "cluster_name" {
  type        = string
  description = "EKS 클러스터 이름"
}

variable "namespace" {
  type        = string
  default     = "karpenter"
  description = "Karpenter가 설치될 네임스페이스"
}

variable "irsa_role_arn" {
  type        = string
  description = "Karpenter Controller용 IRSA Role ARN"
}


# variable "oidc_provider_url" {
#   type        = string
#   description = "OIDC provider URL for Helm chart values"
# }

variable "cluster_endpoint" {
  type        = string
  description = "EKS 클러스터의 엔드포인트 URL"
}

variable "subnet_ids" {
  type        = list(string)
  description = "List of subnet IDs for Karpenter discovery"
}

variable "cluster_security_group_id" {
  type        = string
  description = "EKS Cluster Security Group ID"
}

variable "ubuntu_ami_id" {
  type        = string
  description = "Ubuntu AMI ID to be used by Karpenter nodes"
}


# crd-nodepool.tf
# 필요한 변수들



variable "instance_profile" {
type        = string
description = "IAM instance profile name for Karpenter nodes"
}

variable "karpenter_version" {
  type        = string
  default     = "1.5.0"
}

variable "bastion_host" {
  type        = string
  description = "Bastion public IP or DNS"
}

variable "bastion_user" {
  type        = string
  description = "SSH user on Bastion (e.g., ec2-user)"
}

variable "namespace_dependency" {
  description = "Optional dependency on namespace module"
  type        = any
  default     = null
}