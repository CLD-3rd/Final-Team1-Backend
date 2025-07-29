# ArgoCD Namespace
resource "kubernetes_namespace" "argocd" {
  metadata {
    name = var.namespace
  }
}

# ArgoCD Helm Release
resource "helm_release" "argocd" {
  name       = var.release_name
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argo-cd"
  version    = var.chart_version
  namespace  = kubernetes_namespace.argocd.metadata[0].name
  
  # 타임아웃 증가 및 기타 설정
  timeout = 600
  wait    = true

  # ArgoCD 설정값
  values = [
    yamlencode({
      global = {
        domain = var.domain
      }
      
      configs = {
        params = {
          "server.insecure" = var.server_insecure
        }
      }
      
      server = {
        service = {
          type = var.service_type
          annotations = var.service_annotations
        }
        ingress = {
          enabled = var.ingress_enabled
          annotations = var.ingress_annotations
          hosts = var.ingress_hosts
          tls = var.ingress_tls
        }
      }
      
      redis-ha = {
        enabled = var.redis_ha_enabled
      }
      
      controller = {
        replicas = var.controller_replicas
      }
      
      repoServer = {
        replicas = var.repo_server_replicas
      }
      
      applicationSet = {
        enabled = var.application_set_enabled
      }
    })
  ]

  depends_on = [kubernetes_namespace.argocd]
}

# ArgoCD Initial Admin Secret (선택사항)
resource "kubernetes_secret" "argocd_initial_admin_secret" {
  count = var.create_initial_admin_secret ? 1 : 0
  
  metadata {
    name      = "argocd-initial-admin-secret"
    namespace = kubernetes_namespace.argocd.metadata[0].name
  }

  data = {
    password = base64encode(var.initial_admin_password)
  }

  type = "Opaque"

  depends_on = [kubernetes_namespace.argocd]
}
