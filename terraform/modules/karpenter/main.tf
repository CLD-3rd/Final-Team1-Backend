resource "helm_release" "karpenter" {
  name             = "karpenter"
  repository       = "oci://public.ecr.aws/karpenter"
  chart            = "karpenter"
  version          = var.karpenter_version
  namespace        = var.namespace
  create_namespace = false
  skip_crds        = false  # CRDs 자동 설치
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
      name   = "karpenter"
      create = true         # 여기 true로 변경!!
    }
    controller = {
      resources = {
        requests = { cpu = "1", memory = "1Gi" }
        limits   = { cpu = "1", memory = "1Gi" }
      }
       serviceAccount = {
      name   = "karpenter"
      create = true
      annotations = {
        "eks.amazonaws.com/role-arn" = var.irsa_role_arn
      }
      automountServiceAccountToken = true   # 여기로 이동해야 정상 작동
      }
      podSecurityContext = {
        fsGroup = 65534
      }

    }
  })
]
}


