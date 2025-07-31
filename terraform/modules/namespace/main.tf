resource "kubernetes_namespace" "this" {
  provider = kubernetes.eks

  metadata {
    name = var.name
    labels = var.labels
  }
}