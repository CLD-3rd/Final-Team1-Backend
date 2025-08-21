resource "helm_release" "prometheus" {
  #삭제 필요~
 # count = var.enabled ? 1 : 0
  name             = "prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  namespace        = var.namespace
  version          = var.chart_version
  create_namespace = false


//  values = [file("${path.module}/values.yaml")] # values.yaml 커스터마이징 필요시 주석 해제

  depends_on = [var.namespace_dependency]
}
