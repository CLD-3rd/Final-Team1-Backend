resource "kubernetes_storage_class_v1" "ebs_sc" {
  provider = kubernetes.eks
  
  metadata {
    name = var.name
  }

  storage_provisioner = "ebs.csi.aws.com"

  parameters = {
    type      = var.volume_type
    fsType    = var.fs_type
  }

  reclaim_policy        = var.reclaim_policy
  volume_binding_mode   = var.binding_mode
  allow_volume_expansion = true

  mount_options = var.mount_options
}
