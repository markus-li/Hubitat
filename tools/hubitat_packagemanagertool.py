#  Copyright 2020 Markus Liljergren
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
import uuid
import logging
from urllib.parse import urlparse
from datetime import date

"""
  Hubitat Package Manager Tool class
  This class generates Manifest for Dominick Meglio's HE Package Manager (https://github.com/dcmeglio/hubitat-packagemanager)
  WARNING: Exceptions are not properly implemented in this code, use at your own risk!
"""

class HubitatPackageManagerTool:
  def __init__(self, author, minimumHEVersionDefault, dateReleasedDefault=None, 
               gitHubUrl=None, payPalUrl=None):
    self.log = logging.getLogger(__name__)
    self.minimumHEVersionDefault = minimumHEVersionDefault
    self.authorDefault = author
    if(dateReleasedDefault==None):
      self.dateReleasedDefault = date.today().strftime('%Y-%m-%d')
    else:
      self.dateReleasedDefault = dateReleasedDefault
    
    self.repository = {
      "author": author,
    }
    if(gitHubUrl != None):
      self.repository['gitHubUrl'] = gitHubUrl
    if(payPalUrl != None):
      self.repository['payPalUrl'] = payPalUrl
    self.repository['packages'] = []
    self.packages = []
    
  def addPackage(self, package):
    package.applyDefaults(minimumHEVersion=self.minimumHEVersionDefault, author=self.authorDefault,
        dateReleased=self.dateReleasedDefault)
    
    self.packages.append(package)
    repository = {
      "name": package.manifestDict['packageName'],
      "category": package.category,
      "location": package.location,
      "betaLocation": package.betaLocation,
      "description": package.description
    }
    if(package.betaLocation == None):
      del repository["betaLocation"]
    self.repository['packages'].append(repository)

  def buildRepository(self, output="repository.json"):
    with open(output, 'w') as f:
      f.write(json.dumps(self.repository, indent=2))
  
  def printJSON(self):
    json_formatted_str = json.dumps(self.repository, indent=2)
    print(json_formatted_str)


class HubitatPackageManagerPackage:
  def __init__(self, packageName, category, location, description, isBeta=False, minimumHEVersion=None, author=None, dateReleased=None, releaseNotes=None,
               documentationLink=None, communityLink=None, betaLocation=None):
    self.log = logging.getLogger(__name__)
    self.category = category
    self.location = location
    self.description = description
    self.betaLocation = betaLocation
    self.manifestDict = {
      "packageName": packageName,
      "minimumHEVersion": minimumHEVersion,
      "author": author,
      "dateReleased": dateReleased,
      "betaLocation": betaLocation,
    }
    self.isBeta = isBeta
    if(releaseNotes != None):
      self.manifestDict['releaseNotes'] = releaseNotes
    if(documentationLink != None):
      self.manifestDict['documentationLink'] = documentationLink
    if(communityLink != None):
      self.manifestDict['communityLink'] = communityLink
    self.manifestDict['apps'] = []
    self.manifestDict['drivers'] = []
    if(betaLocation == None or isBeta == True):
      del self.manifestDict["betaLocation"]
    
  def applyDefaults(self, minimumHEVersion=None, author=None, dateReleased=None):
    if(self.manifestDict['minimumHEVersion'] == None):
      if(minimumHEVersion == None):
        self.log.error("minimumHEVersion has to be set!")
      else:
        self.manifestDict['minimumHEVersion'] = minimumHEVersion
    
    if(self.manifestDict['author'] == None):
      if(author == None):
        self.log.error("author has to be set!")
      else:
        self.manifestDict['author'] = author
    
    if(self.manifestDict['dateReleased'] == None):
      if(dateReleased == None):
        self.manifestDict['dateReleased'] = date.today().strftime('%Y-%m-%d')
      else:
        self.manifestDict['dateReleased'] = dateReleased

  def addApp(self, name, version, namespace, location, required, oauth, internalId, id=None):
    newApp = {
      "id" : id,
      "internalId" : internalId,
      "name": name,
      "version": version,
      "betaVersion": version,
      "namespace": namespace,
      "location": location,
      "betaLocation": location,
      "required": required,
      "oauth": oauth
    }
    if(self.isBeta == False):
      del newApp["betaVersion"]
      del newApp["betaLocation"]
    else:
      del newApp["version"]
      del newApp["location"]
    self.manifestDict['apps'].append(newApp)
  
  def clearApps(self):
    self.manifestDict['apps'] = []

  def addDriver(self, name, version, namespace, location, required, internalId, id=None):
    newDriver = {
      "id" : id,
      "internalId" : internalId,
      "name": name,
      "version": version,
      "betaVersion": version,
      "namespace": namespace,
      "location": location,
      "betaLocation": location,
      "required": required
    }
    if(self.isBeta == False):
      del newDriver["betaVersion"]
      del newDriver["betaLocation"]
    else:
      del newDriver["version"]
      del newDriver["location"]

    self.manifestDict['drivers'].append(newDriver)
  
  def _extractSavedManifest(self, savedManifest):
    mdataFull = None
    if(os.path.isfile(savedManifest) and os.access(savedManifest, os.R_OK)):
      mdata = {}
      with open(savedManifest, 'r') as f:
        mdata = json.load(f)
      mdataFull = mdata.copy()
      if('drivers' in mdata):
        drivers_dict = {}
        # Extract the driver data from the manifest and store it based on the UUID
        for d in mdata['drivers']:
          drivers_dict[d['internalId']] = d
        for d in self.manifestDict['drivers']:
          # Make sure we don't have fields that don't belong
          if(self.isBeta == False):
            if "betaVersion" in d:
              del d["betaVersion"]
            if "betaLocation" in d:
              del d["betaLocation"]
          else:
            if "version" in d:
              del d["version"]
            if "location" in d:
              del d["location"]
          if(d['internalId'] in drivers_dict and drivers_dict[d['internalId']]['id'] != None):
            d['id'] = drivers_dict[d['internalId']]['id']
            #print('Found this Driver ID: ' + drivers_dict[d['internalId']]['id'])

      if('apps' in mdata):
        apps_dict = {}
        for d in mdata['apps']:
          apps_dict[d['internalId']] = d
        for d in self.manifestDict['apps']:
          if(self.isBeta == False):
            if "betaVersion" in d:
              del d["betaVersion"]
            if "betaLocation" in d:
              del d["betaLocation"]
          else:
            if "version" in d:
              del d["version"]
            if "location" in d:
              del d["location"]
          if(d['internalId'] in apps_dict and apps_dict[d['internalId']]['id'] != None):
            d['id'] = apps_dict[d['internalId']]['id']
            #print('Found this App ID: ' + apps_dict[d['internalId']]['id'])
    return mdataFull

  def buildManifest(self, output="packageManifest.json", extraInput=None):
    # First check if it already exists and have IDs set
    mdataSaved = None
    if(self.isBeta == True and extraInput != None):
      self._extractSavedManifest(extraInput)
    
    mdataSaved = self._extractSavedManifest(output)

    if(self.isBeta == False and extraInput != None):
      self._extractSavedManifest(extraInput)
    
    for d in self.manifestDict['drivers']:
      if(d['id'] == None):
        d['id'] = str(uuid.uuid5(uuid.NAMESPACE_DNS, d['name'] + d['namespace']))
        #print('Made this Driver ID: ' + d['id'])

    for d in self.manifestDict['apps']:
      if(d['id'] == None):
        d['id'] = str(uuid.uuid5(uuid.NAMESPACE_DNS, d['name'] + d['namespace']))
        #print('Made this App ID: ' + d['id'])

    
    tmpManifestDict = self.manifestDict.copy()
    if(mdataSaved != None):
      tmpManifestDict['dateReleased'] = mdataSaved['dateReleased']
      if(json.dumps(tmpManifestDict, indent=2) == json.dumps(mdataSaved, indent=2)):
        # No changes, so don't update
        #print("same output: " + output + ", " + mdataSaved['dateReleased'])
        self.log.info("No change for '" + output + "'")
      else:
        # There were changes, so update
        #print("NOT same output: " + output + ", " + mdataSaved['dateReleased'])
        # Then write the update
        self.log.warn("CHANGE for '" + output + "'")
        with open(output, 'w') as f:
          f.write(json.dumps(self.manifestDict, indent=2))

  def clearDrivers(self):
    self.manifestDict['drivers'] = []
  
  def printJSON(self):
    json_formatted_str = json.dumps(self.manifestDict, indent=2)
    print(json_formatted_str)