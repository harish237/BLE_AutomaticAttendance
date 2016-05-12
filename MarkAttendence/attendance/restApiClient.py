__author__ = 'humashankar'

import requests
import json
from requests_ntlm import HttpNtlmAuth
from collections import OrderedDict
from restApiHelper import authenticate, check_put_response_code, check_get_post_response_code, \
    check_delete_response_code


# Combining all the get call (reuse the code)
def get_requests(url):
    """
    To make all the get calls.
    Args:

    """

    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    }

    res = requests.get(url=url, headers=headers, verify=False)
    check_get_post_response_code(res)

    return res.json()


def put_request(url, data_json):
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    }

    res = requests.put(url=url, headers=headers, data=data_json, verify=False)
    success = check_put_response_code(res)

    return res, success


def post_request(url, data_json):

    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    }

    message = ""
    res = requests.post(url=url, headers=headers, data=data_json, verify=False)
    success = check_put_response_code(res)

    return res, success


def delete_request(url, data_json):
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
    }

    res = requests.delete(url=url, headers=headers, data=data_json, verify=False)

    return check_delete_response_code(res)
