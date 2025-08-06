# modules/karpenter/node_resources.tf

resource "kubectl_manifest" "ec2nodeclass" {
  # provider    = kubectl.eks
  depends_on = [helm_release.karpenter, var.namespace_dependency]

  yaml_body  = templatefile("${path.module}/ec2nodeclass.yaml.tpl", {
    cluster_name     = var.cluster_name,
    instance_profile = var.instance_profile,
    ubuntu_ami_id    = var.ubuntu_ami_id,
  })
}


resource "kubectl_manifest" "nodepool" {
  # provider    = kubectl.eks
  depends_on = [helm_release.karpenter, var.namespace_dependency]

  yaml_body  = templatefile("${path.module}/nodepool.yaml.tpl", {})
}
