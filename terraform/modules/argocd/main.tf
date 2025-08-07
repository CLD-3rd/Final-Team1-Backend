# resource "kubernetes_namespace" "argocd" {
#   metadata {
#     name = var.namespace
#   }
# }


resource "helm_release" "argocd" {
  #삭제 필요~
  count            = var.enabled ? 1 : 0
  name             = "argocd"
  namespace        = var.namespace
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.chart_version
  create_namespace = false

  # values = [file("${path.module}/values.yaml")]

}


resource "kubernetes_cluster_role_binding" "argocd_controller_admin" {
  #삭제 필요~
  count            = var.enabled ? 1 : 0
  provider = kubernetes

  metadata {
    name = "argocd-application-controller-cluster-admin"
  }

  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = "cluster-admin"
  }

  subject {
    kind      = "ServiceAccount"
    name      = "argocd-application-controller"
    namespace = var.namespace
  }

  depends_on = [helm_release.argocd]
}
