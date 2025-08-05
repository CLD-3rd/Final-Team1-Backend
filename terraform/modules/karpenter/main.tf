# modules/karpenter/main.tf

resource "helm_release" "karpenter" {
  //depends_on       = [helm_release.karpenter_crds]
  name             = "karpenter"
  repository       = "oci://public.ecr.aws/karpenter"
  chart            = "karpenter"
  version          = var.karpenter_version
  namespace        = var.namespace
  create_namespace = false
  skip_crds        = false
  wait             = true

  values = [
    yamlencode({
      settings = {
        clusterName     = var.cluster_name
        clusterEndpoint = var.cluster_endpoint 
      }
      serviceAccount = {
        annotations = {
          "eks.amazonaws.com/role-arn" = var.irsa_role_arn
        }
      }
      controller = {
        resources = {
          requests = { cpu = "1", memory = "1Gi" }
          limits   = { cpu = "1", memory = "1Gi" }
        }
      }
    })
  ]
}
