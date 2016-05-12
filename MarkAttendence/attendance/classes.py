__author__ = 'humashankar'

from restApiClient import get_requests, put_request, post_request, delete_request
from utilities import constant
from collections import OrderedDict
import json
import logging

# create logger
logger = logging.getLogger(__name__)


def get_classes_json():
    url = constant.get_classes.format(host=constant.ip)

    course_json = get_requests(url)

    print course_json

    return course_json

def get_specific_course_json(course):
    url = constant.get_specific_course.format(host=constant.ip, course=course)

    specific_course_json = get_requests(url)

    print specific_course_json

    return specific_course_json
