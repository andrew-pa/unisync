terraform{

    required_version = ">= 1.2.0"

    required_providers {
      aws = {
        source = "hashicorp/aws"
        version = "~> 4.16"
      }
    }
}  

provider "aws" {
  region = "us-west-2"
}

resource "aws_dynamodb_table" "sync_table" {
 name = var.table_name
 hash_key = "syncId"
 billing_mode = var.table_billing_mode

 attribute {
    name = "syncId"
  type = "S"
 }

 tags = {
   environment = "${var.environment}"
 }
}