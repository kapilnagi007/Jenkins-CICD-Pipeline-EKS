terraform {

  backend "s3" {

    bucket = "kapil-terraform-state"

    key = "eks/terraform.tfstate"

    region = "ap-south-1"

    dynamodb_table = "terraform-lock"

    encrypt = true

  }

}