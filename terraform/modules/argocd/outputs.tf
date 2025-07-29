output "namespace" {
  description = "ArgoCD namespace"
  value       = kubernetes_namespace.argocd.metadata[0].name
}

output "release_name" {
  description = "ArgoCD Helm release name"
  value       = helm_release.argocd.name
}

output "release_status" {
  description = "ArgoCD Helm release status"
  value       = helm_release.argocd.status
}

output "server_service_name" {
  description = "ArgoCD server service name"
  value       = "${helm_release.argocd.name}-server"
}

output "server_service_port" {
  description = "ArgoCD server service port"
  value       = 80
}

output "grpc_service_port" {
  description = "ArgoCD server gRPC service port"
  value       = 443
}
