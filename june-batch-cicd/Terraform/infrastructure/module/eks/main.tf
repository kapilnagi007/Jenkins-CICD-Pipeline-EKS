module "eks" {

  source = "terraform-aws-modules/eks/aws"

  version = "~>20.0"

  cluster_name = var.cluster_name

  cluster_version = "1.33"

  subnet_ids = var.subnet_ids

  vpc_id = var.vpc_id

  enable_cluster_creator_admin_permissions = true

  eks_managed_node_groups = {

    worker = {

      instance_types = ["t3.medium"]

      desired_size = 2

      min_size = 2

      max_size = 4

      capacity_type = "ON_DEMAND"

    }

  }

}