__author__ = 'humashankar'

from restApiClient import get_requests, put_request, post_request, delete_request
from utilities import constant
from collections import OrderedDict
import json
import logging

# create logger
logger = logging.getLogger(__name__)


def get_attendance_json(course):
    url = constant.get_attendance.format(host=constant.ip, course=course)

    classes_json = get_requests(url)

    print classes_json

    return classes_json


def get_total_attendance(attendance_json, course_json):

    present_attendance = []

    if course_json:
        dates = course_json[0]['dates']
        for date in dates:
            present = 0
            if attendance_json:
                for student_attendance in attendance_json:
                    student_dates = student_attendance['dates']
                    for student_date in student_dates:
                        if student_date == date:
                            present += 1

            specific_day = ({"date": date, "present": present})

            present_attendance.append(specific_day)

    return present_attendance


def get_present_absent_list(attendance_json, day, course):

    present = []
    absent = []

    if attendance_json:
        for student_attendance in attendance_json:
            added = 0
            while True:
                student_dates = student_attendance['dates']
                for student_date in student_dates:
                    if student_date == day:
                        present.append(student_attendance['sjsuId'])
                        added = 1
                        break
                if added == 0:
                    absent.append(student_attendance['sjsuId'])
                break

    return present, absent





