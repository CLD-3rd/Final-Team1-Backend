// modules/karpenter/crd-install.tf

resource "helm_release" "karpenter_crds" {
  provider = helm.eks
  name             = "karpenter-crd"
  repository       = "oci://public.ecr.aws/karpenter"
  chart            = "karpenter-crd"
  version          = var.karpenter_version
  namespace        = var.namespace
  create_namespace = false
  skip_crds        = false
  wait             = true


}



