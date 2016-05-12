from django.http import HttpResponse, HttpResponseRedirect
from django.shortcuts import render
from django.shortcuts import redirect
from django.template import RequestContext, loader
# from forms import UserForm, UserRegistrationForm
from django.core.context_processors import csrf
from django.contrib.auth.models import User
from django.contrib.auth.decorators import user_passes_test
# from .forms import TestBedSimpleForm, ComponentSimpleForm, UploadUsersForm
from utilities import constant
from classes import get_classes_json, get_specific_course_json
from courseAttendance import get_attendance_json, get_total_attendance, get_present_absent_list
from students import get_students_json
from django.contrib.auth.decorators import login_required
from django.contrib.auth import authenticate, login, logout
import datetime
import logging
from django.shortcuts import get_object_or_404
from django.core.urlresolvers import reverse

# create logger
logger = logging.getLogger(__name__)


# Create your views here.

@user_passes_test(lambda u: u.is_superuser)
def register_user(request):
    if request.method == "POST":
        form = UserForm(request.POST)
        if form.is_valid():
            if request.POST['admin'] == "1":
                User.objects.create_superuser(**form.cleaned_data)
            else:
                new_user = User.objects.create_user(**form.cleaned_data)
            # login(new_user, {'template_name': 'testbed/login.html'})
            # redirect, or however you want to get to the main view
            return redirect(index)
    else:
        form = UserForm()

    return render(request, 'networkManager/registerUser.html', {'form': form})


# @user_passes_test(lambda u: u.is_superuser)
def sign_up(request):
    if request.method == "POST":
        username = request.POST['username']
        password = request.POST['password']
        email = request.POST['email']

        add_user = False
        try:
            user = User.objects.get(username=username)
            print user
        except:
            add_user = True

        if add_user:
            added_user = User.objects.create_superuser(username, email, password)
            added_user.save()

            return redirect(index)
        else:
            return render(request, 'attendance/signup.html', {'msg': "username exists"})

    return render(request, 'attendance/signup.html')


# Login
def userAuth(request):
    logout(request)
    username = password = ''
    if request.POST:
        username = request.POST['username']
        password = request.POST['password']

        user = authenticate(username=username, password=password)
        if user is not None:
            if user.is_active:
                login(request, user)
                next = request.POST['next']
                return HttpResponseRedirect(next)
    template = loader.get_template('attendance/index.html')
    context = RequestContext(request)
    return HttpResponse(template.render(context))


@login_required
def index(request):
    """
    This view is to render the homepage of the dashboard.
    Args:
        request: url request by the user.
    """

    user = request.user.get_username()

    logger.debug("This is index page.")
    classes_json = get_classes_json()



    template = loader.get_template('attendance/index.html')
    # context = RequestContext(request, {"mem_usage_percentage": 80})
    context = RequestContext(request, {'classes': classes_json["_items"]})
    return HttpResponse(template.render(context))


@login_required
def student(request):
    """
    This view is to render the homepage of the dashboard.
    Args:
        request: url request by the user.
    """

    user = request.user.get_username()

    logger.debug("This is index page.")
    students_json = get_students_json()



    template = loader.get_template('attendance/student.html')
    # context = RequestContext(request, {"mem_usage_percentage": 80})
    context = RequestContext(request, {'students': students_json["_items"]})
    return HttpResponse(template.render(context))


@login_required
def get_attendance_for_student(request):
    """
    This view is to render the homepage of the dashboard.
    Args:
        request: url request by the user.
    """

    user = request.user.get_username()

    logger.debug("This is index page.")
    students_json = get_students_json()



    template = loader.get_template('attendance/student.html')
    # context = RequestContext(request, {"mem_usage_percentage": 80})
    context = RequestContext(request, {'students': students_json["_items"]})
    return HttpResponse(template.render(context))


def specific_day_attendance(request):

    requestData = request.GET.get('course', '')

    courseDay = requestData.split("$")

    #day = request.Get.get('specificDay', '')

    attendance_json = get_attendance_json(courseDay[0])
    #specific_course_json = get_specific_course_json(course)

    presents, absents = get_present_absent_list(attendance_json["_items"], courseDay[1], courseDay[0])

    template = loader.get_template('attendance/dayAttendance.html')
    # context = RequestContext(request, {"mem_usage_percentage": 80})
    context = RequestContext(request, {'presents': presents, 'absents':absents, 'course': courseDay[0], 'day': courseDay[1]})
    return HttpResponse(template.render(context))


def course_attendance(request):

    course = request.GET.get('course', '')

    attendance_json = get_attendance_json(course)
    specific_course_json = get_specific_course_json(course)

    specific_day = get_total_attendance(attendance_json["_items"], specific_course_json["_items"])

    template = loader.get_template('attendance/courseAttendance.html')
    # context = RequestContext(request, {"mem_usage_percentage": 80})
    context = RequestContext(request, {'days': specific_day, 'course': course})
    return HttpResponse(template.render(context))
    #HttpResponseRedirect(reverse('course_att', kwargs={'course': course}))

