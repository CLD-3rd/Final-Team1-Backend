output "grafana_name" {

  #value = var.enabled ? helm_release.grafana[0].name : null
  value = helm_release.grafana.name

}
