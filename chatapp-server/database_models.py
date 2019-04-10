from google.appengine.ext import ndb
import hmac
import string
import random
import logging
import json 
import time

class Ip(ndb.Expando):
    ip = ndb.StringProperty()

# Database class model for storing messages
class Message(ndb.Expando):
    message_text = ndb.TextProperty()
    message_subject = ndb.TextProperty()
    sender_username = ndb.StringProperty(indexed=True)
    receiver = ndb.StringProperty(indexed=True)
    time_sent = ndb.StringProperty(indexed=True)
    sender_email = ndb.StringProperty()
    parent_message_id = ndb.StringProperty()
    message_code = ndb.StringProperty()
    read = ndb.BooleanProperty()

    @classmethod
    def create_message(cls, username_from, useremail_from, user_email_to, message, subject, code):
        new_message = Message(
            time_sent=create_time(),
            message_text=message,
            message_subject=subject,
            sender_username=username_from,
            receiver=user_email_to,
            sender_email=useremail_from,
            parent_message_id="",
            message_code=code,
            read=False)
        new_message.put()
        return new_message

    @classmethod  
    def get_messages(cls, email):
        messages = cls.query(cls.receiver == email)
        ordered_messages = messages.order(-cls.time_sent)
        return ordered_messages.fetch()

    @classmethod
    def get_sent_messages(cls, username):
        sent_messages = cls.query(cls.sender_username == username)
        ordered_messages = sent_messages.order(-cls.time_sent)
        return ordered_messages.fetch()

    @classmethod 
    def update_message_read(cls, message_id, read):
        is_read = True if read == "true" else False 
        message = cls.get_by_id(int(message_id))
        message.read = is_read
        message.put()

    def to_dict(self):
        entity_jsonable = super(Message, self).to_dict()
        entity_jsonable["id"] = str(self.key.id())
        return entity_jsonable


# Database class model for storing a users contacts
class Contact(ndb.Expando):
    time_created = ndb.StringProperty()
    email = ndb.StringProperty()
    online = ndb.BooleanProperty()

class WriteCode(ndb.Expando):
    time_created = ndb.StringProperty()
    write_code = ndb.StringProperty()

# Database for storing users. Using Hmac for password hashing.
class User(ndb.Expando):
    time_created = ndb.StringProperty()
    tc_accepted = ndb.BooleanProperty()
    username = ndb.StringProperty()
    password = ndb.StringProperty()
    email = ndb.StringProperty()
    phone = ndb.StringProperty()
    sms_enabled = ndb.BooleanProperty()
    stay_logged_in = ndb.BooleanProperty()
    messages = ndb.StructuredProperty(Message, repeated=True)
    ip_addresses = ndb.StructuredProperty(Ip, repeated=True)
    contacts = ndb.StructuredProperty(Contact, repeated=True)
    write_codes = ndb.StructuredProperty(WriteCode, repeated=True)

    # Return true or false if the user exists
    # @params username, password
    @classmethod
    def user_exists(cls, un, pw):
        user_query = cls.query(cls.username == un, cls.password == hash_password(pw))
        single_user = user_query.get()
        logging.info("[+] fetched user")
        logging.info(single_user)
        exists = single_user != None 
        return exists, single_user

    @classmethod
    def by_username(cls, username):
        return cls.query(cls.username == username).get()
        
    @classmethod 
    def by_email(cls, email):
        return cls.query(cls.email == email).get()

    @classmethod
    def create_user(cls, email, pw, un, phone, sms_enabled, tc_accepted):
        sms_is_enabled = True if sms_enabled == "true" else False 
        tc_is_accepted = True if tc_accepted == "true" else False 
        new_user = User(
            time_created=create_time(),
            username=un,
            password=hash_password(pw),
            email=email,
            phone=phone,
            sms_enabled=sms_is_enabled,
            tc_accepted=tc_is_accepted,
            stay_logged_in=False,
            messages=[],
            ip_addresses=[],
            contacts=[],
            write_codes=[])
        key = new_user.put()
        logging.info(new_user)
        return key, new_user

    @classmethod
    def add_code(cls, write_code, username):
        user = User.query(cls.username == username).get()
        if user:
            user.write_codes.append(
                WriteCode(
                    time_created=create_time(),
                    write_code=write_code))
            user.put()

    @classmethod
    def add_contact(cls, contact_email, username):
        user = cls.query(cls.username == username).get()
        if user:
            user.contacts.append(
                Contact(
                    time_created=create_time(),
                    email=contact_email,
                    online=False))
            user.put()

    @classmethod 
    def get_contacts(cls, username):
        user = cls.query(cls.username == username).get()
        return user.contacts if user else []

    @classmethod
    def username_exists(cls, username):
        user = cls.query(cls.username == username).get()
        return user != None

    @classmethod
    def email_exists(cls, email):
        user = cls.query(cls.email == email).get()
        return user != None

    @classmethod
    def set_stay_logged_in(cls, username, stay_logged_in):
        stay_loggedin = True if stay_logged_in == "true" else False
        user = cls.query(cls.username == username).get()
        user.stay_logged_in = stay_loggedin
        user.put()

    @classmethod 
    def should_stay_logged_in(cls, username):
        user = cls.query(cls.username == username).get()
        return user.stay_logged_in

    @classmethod 
    def update_sms_enabled(cls, username, sms_enabled):
        updated_sms_enabled = True if sms_enabled == "true" else False
        user = cls.by_username(username)
        user.sms_enabled = updated_sms_enabled
        user.put()
        
class TwilioSmsSid(ndb.Expando):
    sid = ndb.StringProperty()

    @classmethod
    def new_message_sid(cls, sid):
        new_sid = cls(sid=sid)
        sid.put()
        
class DefinedMessage(ndb.Expando):
    message = ndb.StringProperty()

    @classmethod
    def get_messages(cls):
        messages = cls.query().fetch()
        if not len(messages):
            for i in range(10):
                message_text = "Message %s" % i
                new_message = DefinedMessage(message=message_text)
                new_message.put()
            messages = cls.query().fetch()
        return messages

class Requests(ndb.Expando):
    username_from = ndb.StringProperty()
    username_to = ndb.StringProperty()
    time_created = ndb.DateTimeProperty()
    active = ndb.BooleanProperty()

    @classmethod
    def get_requests_by_username(cls, username):
        requests = cls.query(cls.username_to == username).fetch()
        logging.info("[+] Found %s requests for user %s" % (len(requests), username))

# Our users will be thanking us for taking the proper security measures
def hash_password(password):
    return hmac.new(password).hexdigest()

def create_time():
    return str(int(time.time()) * 1000)

# make_salt to add some randomness to our encryption
def make_salt():
    base_string = string.letters + string.digits + string.letters + string.digits
    salt = []

    # Pick a random number betwen 10 and 100
    random_number = random.randint(10, 100)

    for i in range(random_number):
        random_letter_index = random.randint(0, len(base_string) -1)
        salt.append(base_string[random_letter_index])

    return "".join(salt)
