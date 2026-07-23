module "vpc" {

  source = "./modules/vpc"

  cidr = var.vpc_cidr

  public_subnets = var.public_subnets

  private_subnets = var.private_subnets

}

module "eks" {

  source = "./modules/eks"

  cluster_name = var.cluster_name

  subnet_ids = module.vpc.private_subnet_ids

  vpc_id = module.vpc.vpc_id

}