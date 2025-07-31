variable "name" {
  description = "The name of the StorageClass"
  type        = string
  default     = "ebs-sc"
}

variable "volume_type" {
  description = "The type of EBS volume (e.g., gp3, gp2, io1)"
  type        = string
  default     = "gp3"
}

variable "fs_type" {
  description = "The file system type to use (e.g., ext4, xfs)"
  type        = string
  default     = "ext4"
}

variable "reclaim_policy" {
  description = "Reclaim policy for the volume (e.g., Retain, Delete)"
  type        = string
  default     = "Delete"
}

variable "binding_mode" {
  description = "Volume binding mode"
  type        = string
  default     = "WaitForFirstConsumer"
}

variable "mount_options" {
  description = "List of mount options"
  type        = list(string)
  default     = []
}
