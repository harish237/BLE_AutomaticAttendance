__author__ = 'humashankar'

import json
import sys
import requests
import logging


logger = logging.getLogger('networkManager')


def check_get_post_response_code(res):

    #print res.status_code
    accepted_response_code = [200, 201]
    if not res.status_code in accepted_response_code:
        logger.error("STATUS: {status} ".format(status=res.status_code))
        logger.error("ERROR: " + res.text)
        return False
    else:
        return True


def check_put_response_code(res):

    #print res.status_code
    accepted_response_code = [200, 201, 204]
    if not res.status_code in accepted_response_code:
        logger.error("STATUS: {status} ".format(status=res.status_code))
        logger.error("ERROR: " + res.text)
        return False
    else:
        return True


def check_head_response_code(res):

    #print "Check head call response"
    #print res.status_code
    successful_head_response_code = [204]
    if res.status_code in successful_head_response_code:
        return True
    else:
        logger.error("STATUS: {status} ".format(status=res.status_code))
        return False


def check_delete_response_code(res):

    successful_delete_response_code = [200, 204]
    print "Delete Response Code ----> " + str(res.status_code)
    if not res.status_code in successful_delete_response_code:
        logger.error("STATUS: {status} ".format(status=res.status_code))
        logger.error("ERROR: " + res.text)
        return False
    else:
        return True


def authenticate(host, username, password, tenant):

    #print "In Authenticate Method"

    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
    payload = {"username": username, "password": password, "tenant": tenant}
    url = 'https://' + host + '/identity/api/tokens'

    res = requests.post(url=url, data=json.dumps(payload), headers=headers, verify=False)

    check_get_post_response_code(res)
    response = res.json()

    token = response['id']

    return token

# remove
def is_token_valid(host, token):

    #print "In token_validation Method"
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
    }

    url = 'https://' + host + '/identity/api/tokens/' + token

    res = requests.head(url=url, headers=headers, verify=False)

    token_validity = check_head_response_code(res)

    return token_validity


def delete_token(host, token):

    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer ' + token
    }
    url = 'https://' + host + '/identity/api/tokens'

    res = requests.delete(url=url, headers=headers, verify=False)

    check_get_post_response_code(res)
