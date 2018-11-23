import boto3


def lambda_handler(event, context):
    client = boto3.client('events')
    response = client.disable_rule(Name='PollSQS1min')
    return {"status": "stopped"}
