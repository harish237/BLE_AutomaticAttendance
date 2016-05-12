__author__ = 'humashankar'

from restApiClient import get_requests, put_request, post_request, delete_request
from utilities import constant
from collections import OrderedDict
import json
import logging

# create logger
logger = logging.getLogger(__name__)


def get_students_json():
    url = constant.get_students.format(host=constant.ip)

    students_json = get_requests(url)

    print students_json

    return students_json

