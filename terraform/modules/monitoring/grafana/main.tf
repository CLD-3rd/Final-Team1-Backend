resource "helm_release" "grafana" {
  #삭제 필요~
#  count = var.enabled ? 1 : 0
  name             = "grafana"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "grafana"
  namespace        = var.namespace
  version          = var.chart_version
  create_namespace = false

   values = [file("${path.module}/values.yaml")]  # 커스터마이징이 필요하면 사용

  depends_on = [var.namespace_dependency]
}
