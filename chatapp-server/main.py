# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import webapp2
import jinja2
import os
import json
import random
import pickle
import string
import re
import hmac
import random
import logging
import sendgrid
import time

from twilio.rest import Client as TwilioClient
from sendgrid.helpers.mail import *
from google.appengine.api import mail
from xml.dom import minidom
from webapp2_extras import sessions
from webapp2_extras import sessions_memcache
from database_models import *
from google.appengine.api import xmpp
from google.appengine.api import app_identity
from google.appengine.api import mail
from config import configuration_data

template_dir = os.path.join(os.path.dirname(__file__), 'templates')
jinja_env = jinja2.Environment(loader = jinja2.FileSystemLoader(template_dir), autoescape = True)
sendgrid_api_client = sendgrid.SendGridAPIClient(apikey=configuration_data["sendgrid"]["dsquared_apikey"])
from_email = Email(configuration_data["sendgrid"]["from_email"])
twilio_sid = configuration_data["twilio"]["dsquared_sid"]
twilio_authtoken = configuration_data["twilio"]["dsquared_auth_token"]
twilio_client = TwilioClient(twilio_sid, twilio_authtoken)
twilio_from_number = "+14158959437"
server_cache = {}

class MainHandler(webapp2.RequestHandler):
    def write(self, *args, **kwargs):
        self.response.out.write(*args, **kwargs)

    def render_str(self, template, **params):
        t = jinja_env.get_template(template)
        return t.render(params)

    def render(self, template, **kwargs):
        self.write(self.render_str(template, **kwargs))

    def write_json(self, object_jsonable):
        self.write(json.dumps(object_jsonable))

    def dispatch(self):
        # Get a session store for this request.
        self.session_store = sessions.get_store(request=self.request)

        try:
            # Dispatch the request.
            webapp2.RequestHandler.dispatch(self)
        finally:
            # Save all sessions.
            self.session_store.save_sessions(self.response)

    @webapp2.cached_property
    def session(self):
        # Returns a session using the default cookie key.
        return self.session_store.get_session()

class HomePage(MainHandler):
    def get(self):
        try:
            pass
        except Exception as e:
            logging.info(str(e))
 
    def post(self):
        pass

class TermsAndConditions(MainHandler):
    def get(self):
        try:
            self.render("tc.html")
        except Exception as e:
            logging.info(str(e)) 

    def post(self):
        pass

class Logout(MainHandler):
    def get(self):
        try:
            if not self.session.get("username"):
                logging.info("[+] Session data is gone. This needs fix")
            else:
                username = self.session.get("username")
                User.set_stay_logged_in(username, "false")
                self.session.clear()
                self.write_json({"logged_out": True})

        except Exception as e:
            self.session.clear()
            logging.info(str(e))
            logging.info("Exception encountered while logging user out")
            self.write_json({"logged_out": False})

class Login(MainHandler):
    def get(self):
        try:
            if self.session.get("logged_in"):
                username = self.session.get("username")
                should_stay_logged_in = User.should_stay_logged_in(username)
                self.write_json({"stay_logged_in": should_stay_logged_in})
            else:
                self.write_json({"stay_logged_in": False})
        except Exception as e:
            logging.info(str(e))
            self.write_json({"stay_logged_in": False})
            
    def post(self):
        client_args = {}
        try:
            un = str(self.request.get("un"))
            pw = str(self.request.get("pw"))
            stay_logged_in = str(self.request.get("stay_logged_in"))
            login_type = str(self.request.get("login_type"))
            logging.info("%s, %s" % (un, pw))            

            if login_type == "un-pw-droid":
                exists, user = User.user_exists(un, pw)
                if exists:
                    User.set_stay_logged_in(un, stay_logged_in)
                    self.session["logged_in"] = True
                    self.session["username"] = un
                    self.session["email"] = user.email
                    self.write_json({"logged_in": True})
                else:
                    self.session["logged_in"] = False
                    self.write_json({"logged_in": False})
            else:
                logging.info("[+] User logging in from other device")

        except Exception as e:
            logging.info("[+] %s" % str(e))
            self.write_json({"error": True})

class GetPredefinedMessages(MainHandler):
    def get(self):
        if self.session.get("logged_in"):
            predefined_messages = DefinedMessage.get_messages()
            self.write_json({
                "defined_messages": [ message.to_dict() for message in predefined_messages ]})
        else:
            self.write_json({"access": False}) 

    def post(self):
        pass

class Register(MainHandler):
    def get(self):
        pass

    def post(self):
        client_args = {}

        try:
            # User data on account registration. Username, Password, Email, phone number, checkbox to enable two way authentication
            # Username must be unique, phone and two factor authentication are optional
            username = str(self.request.get("username")).lower()
            email = str(self.request.get("email")).lower()
            password = str(self.request.get("password"))
            phone = str(self.request.get("phone"))
            terms_and_conditions_accepted = str(self.request.get("tc_accepted"))
            receive_sms = str(self.request.get("receive_sms"))

            if User.username_exists(username):
                client_args["error"] = True
                client_args["message"] = "Username already exists. Username must be unique"
                self.write_json(client_args)

            elif User.email_exists(email):
                client_args["error"] = True
                client_args["message"] = "Email already exists. Email must be unique"
                self.write_json(client_args)

            elif not len(username) or not len(password) or not len(email):
                client_args["error"] = True
                client_args["message"] = "Username, Password, and Email are required fields."
                self.write_json(client_args)

            else:
                # Data is valid. Create the user
                key, new_user = User.create_user(
                    email, password, username, phone, receive_sms, terms_and_conditions_accepted)

                logging.info("[+] Created user with key.id: " + str(key.id()))
                client_args["success"] = True
                self.write_json(client_args)

        except Exception as e:
            logging.info("[+] %s" % str(e))
            self.write_json({"exception": True})

class FirebaseTokenUpdate(MainHandler):
    def get(self):
        pass

    def post(self):
        try:
            firebase_token = str(self.request.get("token"))
            logging.info("[+] Firebase Token Updated: " + token)
        except Exception as e:
            logging.info(str(e))

class GetMessagesHandler(MainHandler):
    def get(self):
        try:
            if not self.session.get("logged_in"):
                self.write_json({"access": False})
            else:
                username = self.session.get("username")
                email = self.session.get("email")
                logging.info("[+] Username: %s" % username)
                messages = Message.get_messages(email)
                messages_sent = Message.get_sent_messages(username)
                user = User.by_username(username)
                if user:
                    self.write_json({
                        "messages_length": len(messages),
                        "messages": [ msg.to_dict() for msg in messages ],
                        "messages_sent": [ msg.to_dict() for msg in messages_sent ],
                        "contacts": [ contact.to_dict() for contact in user.contacts ],
                        "codes": [ code.to_dict() for code in user.write_codes ],
                        "sms_enabled": user.sms_enabled})
                else:
                    self.write_json({"error": True, "message": "User does not exist"})
        except Exception as e:
            logging.info(str(e))

    def post(self):
        pass

class ContactHandler(MainHandler):
    def get(self):
        pass 

    def post(self):
        try:
            if self.session.get("logged_in"):
                option_type = str(self.request.get("option_type"))
                if option_type == "add_contact":
                    username = self.session.get("username")
                    new_contact_email = str(self.request.get("new_contact_email"))
                    User.add_contact(new_contact_email, username)
                    self.write_json({"successful": True})

                elif option_type == "add_code":
                    username = self.session.get("username")
                    new_write_code = str(self.request.get("new_write_code"))
                    User.add_code(new_write_code, username)
                    self.write_json({"successful": True})

                elif option_type == "add_contact_add_code":
                    username = self.session.get("username")
                    new_contact_email = str(self.request.get("new_contact_email"))
                    new_write_code = str(self.request.get("new_write_code"))
                    user = User.query(User.username == username).get()
                    user.contacts.append(
                        Contact(
                            email=new_contact_email,
                            online=False))
                    user.write_codes.append(
                        WriteCode(
                            write_code=new_write_code))
                    user.put()

                    self.write_json({"successful": True})
            else:
                self.write_json({"access": False})

        except Exception as e:
            logging.info(str(e))

class EmailMessageHandler(MainHandler):
    def get(self):
        pass 

    def post(self):
        try:
            if not self.session.get("logged_in"):
                self.write_json({"error": True})
            else:
                to = str(self.request.get("emailTo")).lower()
                message_body = str(self.request.get("message"))
                code = str(self.request.get("code"))

                logging.info(to)
                logging.info(message_body)
                logging.info(code)
                user = User.by_email(to)
                username_from = self.session.get("username")
                # subject = "New message from user: %s" % username_from 
                subject = "<subject here>"
                email_from = self.session.get("email")
                if user and user.sms_enabled:
                    send_sms(message_body, code, user.phone, subject)
                    self.write_json({"message_sent": True})
                elif user:
                    send_mail_sendgrid(to, message_body, code, subject)
                    Message.create_message(username_from, email_from, to, message_body, subject, code)
                    self.write_json({"message_sent": True})
                else:
                    Message.create_message(username_from, email_from, to, message_body, subject, code)
                    self.write_json({"message_sent": True})

                # app_id, username = app_identity.get_application_id(), self.session.get("username")
                # email = create_approved_mail(app_id, to, cc, subject, body, username)
                # new_message = Message.create_message(username, self.session.get("email"), to, body, subject)                
                # email.send()
                # self.write_json({"message_sent": True})

        except Exception as e:
            logging.info(str(e))
            self.write_json({"error": True})

class UpdateSmsEnabled(MainHandler):
    def get(self):
        pass

    def post(self):
        try:
            if self.session.get("logged_in"):
                username = self.session.get("username")
                sms_enabled = str(self.request.get("sms_enabled"))
                User.update_sms_enabled(username, sms_enabled)
                self.write_json({"success": True})
            else:
                self.write_json({"access": False})

        except Exception as e:
            logging.info(str(e))

class UpdateMessageRead(MainHandler):
    def get(self):
        pass 

    def post(self):
        try:
            message_id = str(self.request.get("message_id"))
            message_read = str(self.request.get("read"))
            Message.update_message_read(message_id, message_read)
            self.write_json({"successful": True})
        except Exception as e:
            logging.info(str(e))
            self.write_json({"error": True}) 

# Using google.appengine.api.mail 
def create_approved_mail(app_id, to, cc, subject, body, username):
    sender_address = 'dylan.dannenhauer@gmail.com'
    email_html = create_html(body, username)
    message = mail.EmailMessage(
        sender=sender_address,
        to=to,
        subject=subject,
        body="",
        html=email_html)
    return message

# Using sendgrid to send mail
def send_mail_sendgrid(to, body, code, subject):
    to_email = Email(to)
    content = Content("text/plain", body + " code: " + code)
    mail = Mail(from_email, subject, to_email, content)
    response = sendgrid_api_client.client.mail.send.post(request_body=mail.get())
    logging.info(response.status_code)
    logging.info(response.body)
    logging.info(response.headers)

def create_email_html(body, username):
    return """
        <html>
            <head>
                <body>
                    <script> console.log('Does this actually get run'); </script>
                    <h1>New message from user %s</h1>
                    <p class="lead">%s</p>
                    <button>Awesome button</button>
                </body>
            </head>
        </html>
    """ % (username.capitalize(), body)

def send_sms(body, code, phone, subject):
    body = subject + " " + body + " code: " + code
    to_number = "+1%s" % phone
    message = twilio_client.api.account.messages.create(
        to=to_number,
        body=body,
        from_=twilio_from_number)
    logging.info("message sid")
    logging.info(message.sid)

config = {}
config['webapp2_extras.sessions'] = {
    'secret_key': '377e72a3bef6bdb6f34788c19073ede3',
    # 'session_max_age': '2505600'
}

app = webapp2.WSGIApplication([
    ('/', HomePage),
    ('/login', Login),
    ('/logout', Logout),
    ('/register', Register),
    ('/update_token', FirebaseTokenUpdate),
    ('/get_conversations', GetMessagesHandler),
    ('/email_message', EmailMessageHandler),
    ('/terms_and_conditions', TermsAndConditions),
    ('/contact_handler', ContactHandler),
    ('/predefined_messages', GetPredefinedMessages),
    ('/update_sms_enabled', UpdateSmsEnabled),
    ('/update_message_read', UpdateMessageRead)

], debug=True, config=config)
