resource "helm_release" "prometheus" {
  name             = "prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "prometheus"
  namespace        = var.namespace
  version          = var.chart_version
  create_namespace = false


  values = [file("${path.module}/values.yaml")] # values.yaml 커스터마이징 필요시 주석 해제

  depends_on = [var.namespace_dependency]
}
