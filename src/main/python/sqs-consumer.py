import boto3
import json
from os import environ
from botocore.vendored import requests


def lambda_handler(event, context):
    messages = get_messages_from_sqs()
    for message in messages:
        process_message(message)

    return 'Messages processed'


def get_messages_from_sqs():
    q_name = environ['SQS_PARTNER']
    sqs = boto3.resource('sqs')
    queue = sqs.get_queue_by_name(QueueName=q_name)
    return queue.receive_messages(MaxNumberOfMessages=10, WaitTimeSeconds=10, AttributeNames=['MessageGroupId'])


def process_message(message):
    print("Message body = {0}".format(message.attributes))

    if message.attributes is not None:
        msg_grp_id = message.attributes['MessageGroupId']
        print("MessageGroupId = {0}".format(msg_grp_id))

        if msg_grp_id:
            payload_file_name = '{0}.xml'.format(message.body)
            tokens_file_name = '{0}-tokens.json'.format(message.body)

            print("Message received: {0}".format(payload_file_name))
            payload_file = download_file(payload_file_name)
            tokens_file = download_file(tokens_file_name)
            
            content = payload_file["Body"].read()
            tokens = json.loads(tokens_file["Body"].read().decode('utf-8'))
            access_token = tokens['AccessToken']
            print("ACCESS_TOKENS = {0}".format(access_token))

            res = post_to_ta(content, access_token)
            if res.status_code == requests.codes.ok:
                print("Deleting message from SQS - ")
                message.delete()
                #print("Deleting payload file from S3 - {0}".format(payload_file_name))
                #delete_file(payload_file_name)
                #print("Deleting tokens file from  - {0}".format(tokens_file_name))
                #delete_file(tokens_file_name)


# File upload to S3
def download_file(file_name):
    bucket_name = environ['S3_PARTNER']
    s3 = boto3.client('s3')
    return s3.get_object(Bucket=bucket_name, Key=file_name)


# Delete from S3
def delete_file(file_name):
    bucket_name = environ['S3_PARTNER']
    s3 = boto3.client('s3')
    res = s3.delete_object(Bucket=bucket_name, Key=file_name)
    print("Delete response = {0}".format(res))


def post_to_ta(content, token):
    api_endpoint = environ['TA_INTEGRATION_API']
    # headers = {"Authorization": token, 'Content-Type': 'application/xml'}
    headers = {"Authorization": "Bearer {0}".format(token), 'Content-Type': 'application/xml'}
    print("Sending POST req to {0}".format(api_endpoint))
    res = requests.post(api_endpoint, data=content, headers=headers)
    print("POST response = {0}".format(res.status_code))
    return res

