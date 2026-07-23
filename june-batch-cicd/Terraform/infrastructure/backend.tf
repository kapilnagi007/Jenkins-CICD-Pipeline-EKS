terraform {

  backend "s3" {

    bucket = "innerpeace-terraform-state-ap-south-1-cicd-project"

    key = "eks/terraform.tfstate"

    region = "ap-south-1"

    dynamodb_table = "terraform-lock"

    encrypt = true

  }

}
