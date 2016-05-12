__author__ = 'humashankar'


ip = "52.26.47.116:5000"


get_classes = "http://{host}/classCollection"

get_specific_course = "http://{host}/classCollection?where=course=='{course}'"

get_students = "http://{host}/studentCollection"

#get_attendance = "http://{host}/attendanceCollection?where=sjsuId=='{sjsuid}' and course=='{course}"
get_attendance = "http://{host}/attendanceCollection?where=course=='{course}'"