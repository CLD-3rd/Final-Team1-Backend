variable "namespace" {
  type    = string
  default = "argocd"
}

variable "chart_version" {
  type    = string
  default = "5.51.6"
}

#삭제 필요~
variable "enabled" {
 description = "Enable or disable ArgoCD module"
 type        = bool
 default     = true
}
