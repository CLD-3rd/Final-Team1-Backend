

#subnet
module "subnet" {
  source              = "./modules/network/subnet"
  vpc_id              = module.vpc.vpc_id
  public_cidr_block   = "192.168.1.0/24"
  private_cidr_block  = "192.168.2.0/24"
  availability_zone   = "ap-northeast-2a"
  team_name           = var.team_name
  #environment         = var.environment
}