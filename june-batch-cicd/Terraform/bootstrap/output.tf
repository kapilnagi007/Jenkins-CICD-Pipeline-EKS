output "bucket" {
  value = aws_s3_bucket.terraform_state.bucket
}

output "table" {
  value = aws_dynamodb_table.terraform_lock.name
}