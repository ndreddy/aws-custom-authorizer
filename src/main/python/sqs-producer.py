import json
from os import environ
from xml.etree.ElementTree import fromstring

import boto3


# Integration messages - PRODUCER
def lambda_handler(event, context):
    print("Received event: " + json.dumps(event, indent=2))

    # AWS generated unique request id
    aws_req_id = context.aws_request_id

    # 1. Extract request body
    # API Gateway is configured with Body Mapping Templates "Method Request pass through"
    # for content type application/xml
    payload = event['body-json']
    #root = fromstring(payload)
    #ptnr_id = root.find('./PARTNER/PTNR_ID').text #TODO we need to change it in generic manner
    #TODO check on the limit on sub-folder in S3 bucket

    # Invent names for payload and tokens files
    #payload_file_name = '{0}/{1}.xml'.format(ptnr_id, aws_req_id)
    #token_file_name = '{0}/{1}-tokens.json'.format(ptnr_id, aws_req_id)

    payload_file_name = '{0}.xml'.format(aws_req_id)
    token_file_name = '{0}-tokens.json'.format(aws_req_id)

    tokens = extract_tokens(event)

    # 2. Upload payload and tokens to S3
    upload_file_s3(payload_file_name, payload)
    upload_file_s3(token_file_name, tokens)

    # 3. Send message to SQS
    send_message_to_queue(aws_req_id, "AMBER_ROAD_TA_INT")

    return 'Request processed successfully. Request ID: ' + aws_req_id


def extract_tokens(event):
    ctx = event['context']
    access_token = ctx['authorizer-access-token']
    id_token = ctx['authorizer-id-token']
    refresh_token = ctx['authorizer-refresh-token']
    tokens = {
        "TokenType": "Bearer",
        "AccessToken": access_token,
        "IdToken": id_token,
        "RefreshToken": refresh_token
    }
    return json.dumps(tokens)


# Uploads file to S3
def upload_file_s3(file_name, content):
    print("Uploading file to S3 as = " + file_name)
    bucket_name = environ['S3_PARTNER'] #TODO: No hardcoding bucket and queue name. Dynamcally create.
    s3 = boto3.client('s3')
    return s3.put_object(Bucket=bucket_name, Key=file_name, Body=content)


# Sends Message to SQS
def send_message_to_queue(msg_body, msg_group_id):
    print("Sending message to SQS")
    q_name = environ['SQS_PARTNER']
    sqs = boto3.resource('sqs')
    queue = sqs.get_queue_by_name(QueueName=q_name)
    return queue.send_message(MessageGroupId=msg_group_id, MessageBody=msg_body)
