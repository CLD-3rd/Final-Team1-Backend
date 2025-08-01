resource "helm_release" "ebs_csi_driver" {
  provider = helm.eks
  name      = "aws-ebs-csi-driver"
  namespace = var.namespace
  repository = "https://kubernetes-sigs.github.io/aws-ebs-csi-driver"
  chart      = "aws-ebs-csi-driver"
  version    = var.chart_version
  create_namespace = false

  values = [
    yamlencode({
      controller = {
        serviceAccount = {
          create = true
          name   = var.service_account_name
          annotations = {
            "eks.amazonaws.com/role-arn" = var.irsa_role_arn
          }
        }
      }
    })
  ]  
  wait = false
}