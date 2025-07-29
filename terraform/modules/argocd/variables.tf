variable "namespace" {
  description = "Kubernetes namespace for ArgoCD"
  type        = string
  default     = "argocd"
}

variable "release_name" {
  description = "Helm release name for ArgoCD"
  type        = string
  default     = "argocd"
}

variable "chart_version" {
  description = "ArgoCD Helm chart version"
  type        = string
  default     = "5.51.6"
}

variable "domain" {
  description = "Domain for ArgoCD server"
  type        = string
  default     = ""
}

variable "server_insecure" {
  description = "Run ArgoCD server in insecure mode"
  type        = bool
  default     = true
}

variable "service_type" {
  description = "Service type for ArgoCD server"
  type        = string
  default     = "ClusterIP"
}

variable "service_annotations" {
  description = "Annotations for ArgoCD server service"
  type        = map(string)
  default     = {}
}

variable "ingress_enabled" {
  description = "Enable ingress for ArgoCD server"
  type        = bool
  default     = false
}

variable "ingress_annotations" {
  description = "Annotations for ArgoCD server ingress"
  type        = map(string)
  default     = {}
}

variable "ingress_hosts" {
  description = "Hosts for ArgoCD server ingress"
  type        = list(string)
  default     = []
}

variable "ingress_tls" {
  description = "TLS configuration for ArgoCD server ingress"
  type        = list(object({
    secretName = string
    hosts      = list(string)
  }))
  default = []
}

variable "redis_ha_enabled" {
  description = "Enable Redis HA for ArgoCD"
  type        = bool
  default     = false
}

variable "controller_replicas" {
  description = "Number of ArgoCD controller replicas"
  type        = number
  default     = 1
}

variable "repo_server_replicas" {
  description = "Number of ArgoCD repo server replicas"
  type        = number
  default     = 1
}

variable "application_set_enabled" {
  description = "Enable ArgoCD ApplicationSet controller"
  type        = bool
  default     = true
}

variable "create_initial_admin_secret" {
  description = "Create initial admin secret for ArgoCD"
  type        = bool
  default     = false
}

variable "initial_admin_password" {
  description = "Initial admin password for ArgoCD"
  type        = string
  default     = "admin123"
  sensitive   = true
}

variable "team_name" {
  description = "Team name for resource naming"
  type        = string
}
