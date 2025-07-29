resource "aws_instance" "bastion" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  subnet_id                   = var.public_subnet_id
  vpc_security_group_ids      = [var.security_group_id]
  associate_public_ip_address = true

  iam_instance_profile = var.iam_instance_profile_name
  key_name             = var.key_name

  user_data = templatefile("${path.module}/scripts/bastion-setup.sh", {
  cluster_name = var.cluster_name
  eks_node_role_arn   = var.eks_node_role_arn   
    bastion_role_arn    = var.bastion_role_arn 
  })
  tags = {
    Name = "${var.team_name}-bastion"
  }
}
