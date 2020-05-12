#  Copyright 2019 Markus Liljergren
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""
  Imports
"""
import requests
import os
import json
import pickle
import logging
from urllib.parse import urlparse
from lxml import html
from lxml import etree
from dateutil import parser

"""
  Hubitat Hub Spider class
  This class can interact with the Hubitat hub...
  WARNING: Exceptions are not properly implemented in this code, use at your own risk!
"""

class HubitatHubSpider:
  hubitat_ip = None
  username = None
  password = None
  API_base_url = None
  session = None
  test_login = False
  is_logged_in = False

  def __init__(self, hubitatIP = None, configFile = None, id_name=None):
    self.log = logging.getLogger(__name__)
    self.id_name = id_name
    if(hubitatIP == None):
      if(configFile != None):
        self.log.info("Loading settings from config file")
        try:
          with open(configFile, 'rb') as f:
            data = pickle.load(f)
            self.hubitatIP = data['hubitatIP']
            self.API_base_url = 'http://' + self.hubitatIP
            self.username = data['username']
            self.password = data['password']
        except (FileNotFoundError, pickle.UnpicklingError) as e:
          print("Failed to load config file!")
          raise
      else:
        raise Exception("No config file specified!")
    else:
      self.hubitat_ip = hubitatIP
      self.API_base_url = 'http://' + hubitatIP

  @staticmethod
  def saveConfig(hubitatIP, username, password, configFile):
    log = logging.getLogger(__name__)
    log.info("Saving data to config file: " + str(configFile))
    data = {
      'hubitatIP': hubitatIP,
      'username': username,
      'password': password
    }
    try:
      with open(configFile, 'wb') as f:
        pickle.dump(data, f)
    except FileNotFoundError:
      log.error("Couldn't save config to disk!")

  def login(self, username = None, password = None):
    APIUrl = self.API_base_url + '/login'
    if(username == None):
      username = self.username
    if(password == None):
      password = self.password
    data = {'username': username,
            'password': password,
            'submit': 'Login'}
    #print(data)
    self._prepare_session()
    if(self.test_login):
      # We might have a working login, do q quick check
      r = self.session.get(self.API_base_url + '/hub/messages')
      if(r.status_code == 200):
        try:
          r.json()
          self.log.info("Already logged in, session restored!")
          self.test_login = False
          self.is_logged_in = True
        except json.decoder.JSONDecodeError:
          self.log.info("NOT logged in! Session is old or logged out!")
          self.test_login = False
          self.is_logged_in = False
      else:
        self.log.info("NOT logged in! Session is old or logged out! Status code: " + str(r.status_code))
        self.test_login = False
        self.is_logged_in = False
    if(self.is_logged_in == False):
      r = self.session.post(APIUrl, data=data)
      self.log.debug(r)
      if(r.status_code == 200):
        self.log.info("Login succeded!")
        self.is_logged_in = True
        self.test_login = False
      else:
        self.log.error("Login failed!")
        self.is_logged_in = False
        self.test_login = False
  
  def logout(self):
    APIUrl = self.API_base_url + '/logout'
    self._prepare_session()
    # Logout
    r = self.session.get(APIUrl)
    if(r.status_code == 302 or r.status_code == 200):
      self.log.info("Logged out!")
      self.test_login = False
      self.is_logged_in = False
  
  def _prepare_session(self):
    if (self.session == None):
      # Check if we have a saved session
      try:
        hubitat_session = '__hubitat_session'
        if(self.id_name is not None):
          hubitat_session += '_' + self.id_name
        with open(hubitat_session, 'rb') as f:
          self.session = pickle.load(f)
        self.test_login = True
        self.is_logged_in = False
      except (FileNotFoundError, pickle.UnpicklingError):
        self.test_login = False
        self.is_logged_in = False
        self.session = requests.session()
  
  def save_session(self):
    if (self.session != None):
      try:
        hubitat_session = '__hubitat_session'
        if(self.id_name is not None):
          hubitat_session += '_' + self.id_name
        with open(hubitat_session, 'wb') as f:
          pickle.dump(self.session, f)
      except FileNotFoundError:
        self.log.error("Couldn't save session to disk!")

  def get_app_current_code(self, codeID):
    return(self._get_current_code('app', codeID))
  
  def get_driver_current_code(self, codeID):
    return(self._get_current_code('driver', codeID))

  def get_app_current_code_version(self, codeID):
    response = self.get_app_current_code(codeID)
    try:
      return response['version']
    except (json.decoder.JSONDecodeError, TypeError) as e:
      self.log.error("Not json in response, try to login first?")
      return(-3)

  def get_driver_current_code_version(self, codeID):
    response = self.get_driver_current_code(codeID)
    try:
      return response['version']
    except (json.decoder.JSONDecodeError, TypeError) as e:
      self.log.error("Not json in response, try to login first?")
      return(-3)

  def _get_current_code(self, codeType, codeID):
    self._prepare_session()
    if(codeType == 'driver'):
      APIUrl = self.API_base_url + '/driver/ajax/code?id=' + str(codeID)
    elif(codeType == 'app'):
      APIUrl = self.API_base_url + '/app/ajax/code?id=' + str(codeID)
    else:
      raise Exception('Unknown code type: ' + str(codeType))
    
    #print(APIUrl)
    
    response = self.session.get(APIUrl)
    if(response.status_code == 200):
      try:
        return response.json()
      except json.decoder.JSONDecodeError:
        self.log.error("Not json in response, try to login first?")
        return(-3)
    else:
      return -1

  def get_driver_list(self):
    self._prepare_session()
    APIUrl = self.API_base_url + '/device/drivers'
    
    #self.log.debug(APIUrl)
    
    response = self.session.get(APIUrl)
    if(response.status_code == 200):
      try:
        rdict = response.json()['drivers']
        ndict = {}
        for d in rdict:
          ndict[d['id']] = d
        return(ndict)
      except json.decoder.JSONDecodeError:
        self.log.error("Not json in response for get_driver_list(), try to login first?")
        return(-3)
    else:
      self.log.error("Unknown problem occured in get_driver_list(): " + str(response))
      return -1

  def get_app_list(self):
    self._prepare_session()
    APIUrl = self.API_base_url + '/app/list'
    tzinfos = {"CST": -3600}
    response = self.session.get(APIUrl)
    tree = html.fromstring(response.text)
    approw = tree.xpath('//tr[@class="app-row"]')
    ndict = {}
    for a in approw:
      #print('1--------------------')
      #print(etree.tostring(a))
      #print('2--------------------')
      ntree = html.fromstring(etree.tostring(a))
      id = int(ntree.attrib['data-app-id'])
      ndict[id] = {'id': id}
      ndict[id]['name'] = ntree.xpath('//a[@title]')[0].attrib['title']
      ndict[id]['namespace'] = ntree.xpath('//td')[1].text.strip()
      ndict[id]['oauthEnabled'] = ntree.xpath('//td/div')[1].text.strip() == 'enabled'
      ndict[id]['lastModified'] = parser.parse(ntree.xpath('//td')[3].text.strip(), tzinfos=tzinfos)
      #print(id)
      #print('Name: ' + ndict[id]['name'])
      #print(ndict[id]['namespace'])
      #print(ndict[id]['oauth'])
      #print(ndict[id]['lastModified'])
      #print('3--------------------')
    return ndict
    #s = cleaner.clean_html(response.text)

    

  # http://192.168.10.1/device/drivers
  def push_new_app(self, codeID, groovyFileToPublish):
    return(self.push_new_code('app', groovyFileToPublish))
  
  def push_new_driver(self, groovyFileToPublish):
    return(self.push_new_code('driver', groovyFileToPublish))

  def push_new_code(self, codeType, groovyFileToPublish):
    self._prepare_session()
    if(codeType == 'driver'):
      APIUrl = self.API_base_url + '/driver/save'
    elif(codeType == 'app'):
      APIUrl = self.API_base_url + '/app/save'
    else:
      raise Exception('Unknown code type: ' + str(codeType))

    with open (groovyFileToPublish, "r") as f:
      source = f.read()
    # Compare current source to the old one
    data = {
        'id': '',
        'version': '',
        'create': '',
        'source': source
      }
    #302 expected, with redirect to the driver id... Location example: http://192.168.10.1/driver/editor/611
    #200 and webpage in content when failing...
    self.log.debug("Starting the submission of '" + str(groovyFileToPublish) + "'")
    response = self.session.post(APIUrl, data=data)
    #self.log.debug(str(response))
    #self.log.debug(str(response.headers))
    #self.log.debug(str(response.links))
    self.log.debug('url:' + str(response.url))
    #self.log.debug('text: "' + str(response.text) + '"')
    # If url is http://192.168.10.1/driver/save
    # then saving failed!
    # When a success, this is the URL: http://192.168.10.1/driver/editor/617
    a = urlparse(response.url).path.split('/')[-1:][0]
    self.log.debug('The response was: {}'.format(a))
    if(a.isdigit()):
      self.log.info("The code was submitted to {} with the new ID {}!".format(codeType, int(a)))
      return(int(a))
    elif(a == 'save'):
      self.log.error("Failed to create new entry! No more is known about why!")
      return(-1)
    else:
      self.log.error("Unknown problem occured!")
      return(-2)

  def push_app_code(self, codeID, groovyFileToPublish):
    return(self.push_code('app', codeID, groovyFileToPublish))
  
  def push_driver_code(self, codeID, groovyFileToPublish):
    return(self.push_code('driver', codeID, groovyFileToPublish))

  def push_code(self, codeType, codeID, groovyFileToPublish):
    #/driver/editor/update
    # For DELETE:
    # POST: id=615&_action_delete=Delete
    # The response is empty with a redirect to /driver/list
    self._prepare_session()
    if(codeType == 'driver'):
      APIUrl = self.API_base_url + '/driver/ajax/update'
    elif(codeType == 'app'):
      APIUrl = self.API_base_url + '/app/ajax/update'
    else:
      raise Exception('Unknown code type: ' + str(codeType))
    currentCode = self._get_current_code(codeType, codeID)
    codeVersion = currentCode['version']
    
    if(codeVersion != -1):
      with open (groovyFileToPublish, "r") as f:
        source = f.read()
      # Compare current source to the old one
      if(currentCode['source'] == source):
        self.log.info("The source for code ID '" + str(codeID) + "' is already up-to-date! No update needed!")
        return(currentCode)
      else:
        data = {
          'id': codeID,
          'version': codeVersion,
          'source': source
        }
        
        self.log.debug("Starting the publishing of '" + str(groovyFileToPublish) + "' to ID '" + str(codeID) + "' with current version being '" + str(codeVersion) + "'")
        response = self.session.post(APIUrl, data=data)
        try:
          jsonResponse = response.json()
        except json.decoder.JSONDecodeError:
          self.log.error("Not json in the response for publishing '" + str(groovyFileToPublish) + "' to ID '" + str(codeID) + "' with current version '" + str(codeVersion) + "'")
          return(-3)
        if(jsonResponse['status'] == 'success'):
          jsonResponse['source'] = source
          self.log.info("The source for code ID '" + str(codeID) + "' has been UPDATED!")
          return(jsonResponse)
        elif(jsonResponse['status'] == 'error'):
          self.log.error("Failed to update code with error: '" + str(jsonResponse['errorMessage']).strip() + "'")
          return(-1)
        else:
          self.log.error("Unknown problem occured: " + str(jsonResponse))
          return(-2)
    else:
      self.log.error("Can't get the code version for ID: " + str(codeID))
