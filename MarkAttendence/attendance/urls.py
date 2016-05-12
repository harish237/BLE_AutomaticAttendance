__author__ = 'harish'

from django.conf.urls import patterns, url

from attendance import views

urlpatterns = patterns('',
                       # ex: /testbed/
                       # Authentication

                       # Login
                       url(r'^login/$', 'django.contrib.auth.views.login', {'template_name': 'attendance/signin.html'},
                           name='login'),
                       # Logout
                       url(r'^logout/$', 'django.contrib.auth.views.logout', {'next_page': '/'}, name='logout'),
                       # url(r'^login/$', views.userAuth, name='userAuth'),
                       # Add User
                       url(r'^registerUser/$', views.register_user, name='registerUser'),
                       url(r'^signup/$', views.sign_up, name='sign_up'),
                       # url(r'^addUser/$', views.addUser, name='addUser'),

                       # Testbed and Component Details
                       url(r'^$', views.index, name='index'),

                       url(r'^student/$', views.student, name='student'),
                       url(r'^courseAttendance/$', views.course_attendance, name='course_attendance'),
                       url(r'^dayAttendance/$', views.specific_day_attendance, name='day_attendance'),


                       )