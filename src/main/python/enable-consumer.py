import boto3


def lambda_handler(event, context):
    client = boto3.client('events')
    response = client.enable_rule(Name='PollSQS1min')
    return {"status": "started"}
