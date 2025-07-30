resource "kubernetes_namespace" "argocd" {
  provider = kubernetes.eks

  metadata {
    name = var.namespace
  }
}
resource "helm_release" "argocd" {
  provider = helm.eks
  name             = "argocd"
  namespace        = var.namespace
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.chart_version
  create_namespace = false

  # values = [file("${path.module}/values.yaml")]
}
