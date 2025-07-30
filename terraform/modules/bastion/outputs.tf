
output "bastion_instance_id" {
  description = "ID of the Bastion EC2 instance"
  value       = aws_instance.bastion.id
}

output "bastion_public_ip" {
  description = "Public IP address of the Bastion EC2"
  value       = aws_instance.bastion.public_ip
}

output "bastion_private_ip" {
  description = "Private IP address of the Bastion EC2"
  value       = aws_instance.bastion.private_ip
}
