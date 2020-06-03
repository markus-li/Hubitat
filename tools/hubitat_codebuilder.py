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
import os
from pathlib import Path
import re
import pprint
import yaml
import ruamel.yaml
import logging
import winsound
import datetime
import io
import inspect
import pickle
import hashlib
from datetime import date
from colorama import init, Fore, Style

# Local imports
#from hubitat_driver_snippets import *
#from hubitat_driver_snippets_parser import *
from hubitat_hubspider import HubitatHubSpider

# (Ab)using the Log formatter for other things...
class PrintFormatter(logging.Formatter):
    def __init__(self, fmt="%(name)s - (%(url)s) - (%(url_raw)s)", datefmt="%Y-%m-%d"):
        super().__init__(fmt=fmt, datefmt=datefmt)

class PrintRecord(logging.LogRecord):
    def __init__(self):
        super().__init__('',0,'',0,'',{},'')
    
    def update(self, dictToAdd):
        for key in dictToAdd:
            setattr(self, key, dictToAdd[key])

# Custom Log Formatter
class HubitatCodeBuilderLogFormatter(logging.Formatter):

    def __init__(self, fmt_default="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s: %(msg)s", 
                    fmt_debug="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s:%(lineno)4d:%(funcName)s: %(msg)s", 
                    fmt_error="%(time_elapsed)-11s:%(name)-20s:%(levelname)5s:%(lineno)4d:%(funcName)s: %(msg)s", 
                    error_beep=True, default_color=Fore.GREEN, debug_color=Fore.YELLOW, error_color=Fore.RED):
        init() # This is the init for Colorama
        # Another format to use: '%(asctime)s:%(name)20s:%(levelname)5s: %(message)s'
        self._error_beep = error_beep
        self._init_time = datetime.datetime.utcnow()
        self._formatter_debug = logging.Formatter(fmt=fmt_debug)
        self._formatter_error = logging.Formatter(fmt=fmt_error)
        self._default_color = default_color
        self._debug_color = debug_color
        self._error_color = error_color
        
        super().__init__(fmt=fmt_default)

    def format(self, record):
        now = datetime.datetime.utcnow()
        try:
            delta = now - self._init_time
        except AttributeError:
            delta = 0

        # First add the elapsed time
        record.time_elapsed = '{0:.2f}ms'.format(delta.total_seconds() * 1000)
        
        # Now add our colors
        if record.levelno == logging.DEBUG:
            if(self._debug_color != None):
                res = self._debug_color + self._formatter_debug.format(record) + Style.RESET_ALL
            else:
                res = self._formatter_debug.format(record)
        elif record.levelno == logging.ERROR:
            if(self._error_color != None):
                res = self._error_color + self._formatter_error.format(record) + Style.RESET_ALL
            else:
                res = self._formatter_error.format(record)
            if(self._error_beep):
                winsound.Beep(500, 300)
        elif record.levelno == logging.WARN:
            if(self._error_color != None):
                res = self._error_color + self._formatter_error.format(record) + Style.RESET_ALL
            else:
                res = self._formatter_error.format(record)
            if(self._error_beep):
                winsound.Beep(500, 300)
        else:
            if(self._default_color != None):
                res = self._default_color + super().format(record) + Style.RESET_ALL
            else:
                res = super().format(record)

        return(res)

class HubitatCodeBuilderError(Exception):
   """HubitatCodeBuilder Base Exception Class"""
   pass

class HubitatCodeBuilder:

    def __init__(self, hubitat_hubspider, id_name=None, calling_namespace=None, app_dir=Path('./apps'), app_build_dir=Path('./apps/expanded'), \
                 driver_dir=Path('./drivers'), driver_build_dir=Path('./drivers/expanded'), default_version='v0.0.1.MMDDb', \
                 build_suffix='-expanded', driver_raw_repo_url='http://127.0.0.1/', app_raw_repo_url='http://127.0.0.1/', 
                 checksum_file_suffix=None, checksum_file='__hubitat_checksums', remove_comments=True):
        self.id_name = id_name
        self.app_dir = Path(app_dir)
        self.app_build_dir = Path(app_build_dir)
        self.driver_dir = Path(driver_dir)
        self.driver_build_dir = Path(driver_build_dir)
        self.default_version = self.getFormattedVersionString(default_version)
        self._current_version = None   # This is used to store the version currently worked on
        self.build_suffix = build_suffix
        self.log = logging.getLogger(__name__)
        self.hubitat_hubspider = hubitat_hubspider
        self.he_drivers_dict = self.hubitat_hubspider.get_driver_list()
        self.he_apps_dict = self.hubitat_hubspider.get_app_list()
        #self.log.debug('he_drivers_dict: {}'.format(str(self.he_drivers_dict)))
        self.driver_checksums = {}
        self.app_checksums = {}
        self.driver_num_updated = 0
        self.app_num_updated = 0
        self.driver_new = {}
        self.app_new = {}
        self.driver_raw_repo_url = driver_raw_repo_url
        if (self.driver_raw_repo_url[-1] != '/'):
            self.driver_raw_repo_url += '/'
            self.log.warn("Had to add a '/' to the self.driver_raw_repo_url! You should specify it with a '/' at the end!")
        self.app_raw_repo_url = app_raw_repo_url
        if (self.app_raw_repo_url[-1] != '/'):
            self.app_raw_repo_url += '/'
            self.log.warn("Had to add a '/' to the self.app_raw_repo_url! You should specify it with a '/' at the end!")
        # Check if we have a saved session
        self.checksum_file = checksum_file
        if(self.id_name != None):
            self.checksum_file += '_' + id_name
        self.checksum_file_suffix = checksum_file_suffix
        if(self.checksum_file_suffix != None):
            self.checksum_file += '_' + self.checksum_file_suffix
        try:
            with open(self.checksum_file, 'rb') as f:
                (self.driver_checksums, self.app_checksums) = pickle.load(f)
        except (FileNotFoundError, pickle.UnpicklingError) as e:
            self.log.error("Couldn't restore checksums! {}".format(str(e)))
            self.driver_checksums = {}
            self.app_checksums = {}
        self.remove_comments = remove_comments
        my_locals = locals().copy()
        my_locals.pop('self')
        # Save the calling namespace for future use...
        self.calling_namespace = calling_namespace
        self.log.debug('Settings: {}'.format(str(my_locals)))

    def saveChecksums(self):
        try:
            with open(self.checksum_file, 'wb') as f:
                pickle.dump((self.driver_checksums, self.app_checksums), f)
        except FileNotFoundError:
            self.log.error("Couldn't save session to disk!")

    def clearChecksums(self):
        self.driver_checksums = {}
        self.app_checksums = {}

    def getHelperFunctions(self, helper_function_type):
        r = ''
        f = './helpers/helpers-' + helper_function_type + '.groovy'
        if(os.path.isfile(f)):
            # This could become an infinite loop if you include a file that includes itself directly or indirectly through another file... 
            # Keep track of what you import!
            r = self._innerExpandGroovyFile(f, None)
        else:
            # Yes, this should be specific, but it doesn't matter here...
            raise HubitatCodeBuilderError("Helper function type '" + helper_function_type + "' can't be included! File doesn't exist!")
        return(r)

    def getOutputGroovyFile(self, input_groovy_file, alternate_output_filename = None):
        #self.log.debug('Using "' + str(input_groovy_file) + '" to get path for "' + str(output_groovy_dir) + '"...')
        #self.log.debug('Filename stem: ' + input_groovy_file.stem)
        #self.log.debug('Filename suffix: ' + input_groovy_file.suffix)
        input_groovy_file = Path(input_groovy_file)
        if(alternate_output_filename != None):
            output_groovy_file = Path(str(alternate_output_filename) + self.build_suffix + str(input_groovy_file.suffix))
        else:
            output_groovy_file = Path(str(input_groovy_file.stem) + self.build_suffix + str(input_groovy_file.suffix))
        #print('output_groovy_file: ' + str(output_groovy_file))
        return(output_groovy_file)

    def _checkFordefinition_string(self, l):
        definition_position = l.find('definition (')
        if(definition_position != -1):
            ds = l[definition_position+11:].strip()
            # On all my drivers the definition row ends with ") {"
            self.log.debug('Parsing Definition statement')
            #print('{'+ds[1:-3]+'}')
            definition_dict = yaml.load(('{'+ds[1:-3]+' }').replace(':', ': '), Loader=yaml.FullLoader)
            self.log.debug(definition_dict)
            if(self._alternate_name != None):
                definition_dict['name'] = self._alternate_name
            if(self._alternate_namespace != None):
                definition_dict['namespace'] = self._alternate_namespace
            if(self._alternate_vid != None):
                definition_dict['vid'] = self._alternate_vid
            if(self.driver_raw_repo_url != None):
                definition_dict['importUrl'] = self.driver_raw_repo_url + str(self._output_groovy_file)
            #print(definition_dict)
            # Process this string
            # (name: "Tasmota - Tuya Wifi Touch Switch TEST (Child)", namespace: "tasmota", author: "Markus Liljergren") {
            #PATTERN = re.compile(r'''((?:[^,"']|"[^"]*"|'[^']*')+)''')
            #PATTERN2 = re.compile(r'''((?:[^(){}:"']|"[^"]*"|'[^']*')+)''')
            #l1 = PATTERN.split(ds)[1::2]
            #d = {}
            #for p1 in l1:
            #    p1 = p1.strip()
            #    i = 0
            #    previousp2 = None
            #    for p2 in PATTERN2.split(p1)[1::2]:
            #        p2 = p2.strip()
            #        if(p2 != ''):
            #            if(i==0):
            #                previousp2 = p2.strip('"')
            #            else:
            #                #self.log.debug('"' + previousp2 + '"="' + p2.strip('"') + '"')
            #                d[previousp2] = p2.strip('"')
            #            i += 1
            #self.log.debug(d)
            definition_dict_original = definition_dict.copy()
            ds = '[' + str(definition_dict)[1:-1] + ']'
            for k in definition_dict:
                definition_dict[k] = '"x' + definition_dict[k] + 'x"'
            new_definition = (l[:definition_position]) + 'definition (' + yaml.dump(definition_dict, default_flow_style=False, sort_keys=False ).replace('\'"x', '"').replace('x"\'', '"').replace('\n', ', ')[:-2] + ') {\n'
            #print(new_definition)
            output = 'String getDeviceInfoByName(infoName) { \n' + \
                '    // DO NOT EDIT: This is generated from the metadata!\n' + \
                '    // TODO: Figure out how to get this from Hubitat instead of generating this?\n' + \
                '    Map deviceInfo = ' + ds + '\n' + \
                '    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)\n' + \
                '    return(deviceInfo[infoName])\n' + \
                '}'
            #new_definition = l
            return(new_definition, output, definition_dict_original)
        else:
            return(None)

    def getBuildDir(self, code_type):
        if(code_type == 'driver'):
            return(self.driver_build_dir)
        elif(code_type == 'app'):
            return(self.app_build_dir)
        else:
            raise HubitatCodeBuilderError('Incorrect code_type: ' + str(code_type))
    
    def getInputDir(self, code_type):
        if(code_type == 'driver'):
            return(self.driver_dir)
        elif(code_type == 'app'):
            return(self.app_dir)
        else:
            raise HubitatCodeBuilderError('Incorrect code_type: ' + str(code_type))
    
    def _runEvalCmd(self, eval_cmd):
        # This will run the eval command and return the output
        # Overrides should be implemented by overriding _runEvalCmdAdditional
        # Overriding this method is not recommended.

        output = eval_cmd
        found = False
        try:
            (found, output) = self._runEvalCmdAdditional(eval_cmd)
            if(found == False):
                output = eval_cmd
        except AttributeError:
            #print(str(e))
            found = False
        # This if can be overriden in self._runEvalCmdAdditional()
        if(found == False):
            if(eval_cmd == 'getDeviceInfoFunction()'):
                self.log.debug("Executing getDeviceInfoFunction()...")
                if(self._definition_string == None):
                    raise HubitatCodeBuilderError('ERROR: Missing/incorrect Definition in file!')
                # self._definition_string contains a function that can be 
                # inserted into a driver to retrieve driver info from.
                output = self._definition_string
                found = True
        # If no special handling is needed, just run eval...
        # 1. Try if it runs without prepending anything...
        # 2. See if it works with a method in the class instance
        # 3. See if it works with a "private" method in the class instance
        # 4. If all fails, throw an exception!
        if(found == False):
            try:
                #self.log.debug("eval_cmd: " + eval_cmd)
                output = eval(eval_cmd)
            except NameError:
                try:
                    output = eval('self.calling_namespace.' + eval_cmd)
                except AttributeError:
                    try:
                        output = eval('self.' + eval_cmd)
                    except AttributeError:
                        try:
                            output = eval('self._' + eval_cmd)
                        except AttributeError:
                            self.log.error('The call "{}" was needed when parsing, but it was not available in the namespace! Have you included the required namespaces?'.format(eval_cmd))
                            raise

        return(output)

    def setUsedDriverList(self, used_driver_list):
        self.used_driver_list = used_driver_list
    
    def getCheckSumOfFile(self, file_to_check):
        m = hashlib.md5()
        with open(file_to_check, 'r', encoding='utf-8') as file:
            block = file.read(512)
            while block:
                m.update(block.encode('utf-8'))
                block = file.read(512)
        
        return(m.hexdigest())

    def getFormattedVersionString(self, version):
        if(version.find("MMDD") != -1):
            version = version.replace("MMDD", date.today().strftime("%m%d"))
        return version

    def commentRemover(self, text):
        # https://gist.github.com/ChunMinChang/88bfa5842396c1fbbc5b#file-remove_c_style_comments-py
        def replacer(match):
            s = match.group(0)
            if s.startswith('/'):
                return " " # note: a space and not an empty string
            else:
                return s
        pattern = re.compile(
            r'//.*?$|/\*.*?\*/|\'(?:\\.|[^\\\'])*\'|"(?:\\.|[^\\"])*"',
            re.DOTALL | re.MULTILINE
        )
        return re.sub(pattern, replacer, text).rstrip()

    def isHiddenLog(self, s):
        regex = re.compile(r'(.*logging\s*\(.*,\s*0\s*\))(.*,\s*.+\s*\))?')
        re_match = regex.match(s)
        if(re_match != None):
            mg = re_match.groups()
            if(mg[0] != None and mg[1] == None):
                return True
        return False

    def insertStringAfterWhitepace(self, s, to_insert):
        regex = re.compile(r'^(\s*)([^\s]*.*)$')
        re_match = regex.match(s)
        if(re_match != None):
            mg = re_match.groups()
            if(mg[0] != None and mg[1] != None):
                return mg[0] + to_insert + mg[1]
            else:
                return to_insert + s
        else:
            return to_insert + s

    def expandGroovyFile(self, config_dict, code_type = 'driver'):
        # Process the params
        input_groovy_file = Path(config_dict['file'])
        alternate_output_filename = (config_dict['alternate_output_filename'] if 'alternate_output_filename' in config_dict else None)
        
        alternate_name = (config_dict['alternate_name'] if 'alternate_name' in config_dict else None)
        alternate_module = (config_dict['alternate_module'] if 'alternate_module' in config_dict else None)
        alternate_namespace = (config_dict['alternate_namespace'] if 'alternate_namespace' in config_dict else None)
        alternate_template = (config_dict['alternate_template'] if 'alternate_template' in config_dict else None)
        alternate_vid = (config_dict['alternate_vid'] if 'alternate_vid' in config_dict else None)

        output_groovy_file = self.getOutputGroovyFile(input_groovy_file, alternate_output_filename)
        self._output_groovy_file = output_groovy_file

        r = {'file': output_groovy_file, 'name': ''}
        
        self.log.debug('Expanding "' + str(input_groovy_file) + '" to "' + str(output_groovy_file) + '"...')
        
        self._alternate_output_filename = alternate_output_filename
        self._alternate_name = alternate_name
        self._alternate_namespace = alternate_namespace
        self._alternate_vid = alternate_vid
        self._alternate_template = alternate_template
        self._alternate_module = alternate_module
        self._current_version = self.getFormattedVersionString((config_dict['version'] if 'version' in config_dict else self.default_version))
        r['version'] = self._current_version
        r['required'] = (config_dict['required'] if 'required' in config_dict else False)
        if(code_type == 'app'):
            r['oauth'] = (config_dict['oauth'] if 'oauth' in config_dict else False)
        self.log.debug("Expanding using version: {}".format(self._current_version))
        self._config_dict = config_dict
        
        # Reset the definition string
        self._definition_string = None

        r_extra = self._innerExpandGroovyFile(self.getInputDir(code_type) / input_groovy_file, self.getBuildDir(code_type) / output_groovy_file)
        r.update(r_extra)
        
        
        self.log.info('DONE expanding "' + input_groovy_file.name + '" to "' + output_groovy_file.name + '"!')
        return(r)

    def _innerExpandGroovyFile(self, input_groovy_file, output_groovy_file):
        r = {}
        self.log.debug('Build dir: ' + str(output_groovy_file))
        if(output_groovy_file != None):
            wd = open (output_groovy_file, "w")
        else:
            wd = io.StringIO()
        with open (input_groovy_file, "r") as rd:
            # Read lines in loop
            previous_line = None
            commentPosition = -1
            for l in rd:
                if(self._definition_string == None):
                    self._definition_string = self._checkFordefinition_string(l)
                    if(self._definition_string != None):

                        (l, self._definition_string, definition_dict_original) = self._definition_string
                        # self._definition_string contains a function that can be 
                        # inserted into a driver to retrieve driver info from.
                        #self.log.debug(self._definition_string)
                        r['name'] = definition_dict_original['name']
                includePosition = l.find('#!include:')
                numCharsToRemove = 10
                includeNC = False
                if(includePosition == -1):
                    includePosition = l.find('#!includeNC:')
                    numCharsToRemove = 12
                    includeNC = True
                if(self.remove_comments == True):
                    commentPosition = l.find('//')
                    if(commentPosition != -1):
                        l = self.commentRemover(l).rstrip() + "\n"
                if(self.isHiddenLog(l) == True):
                    # This is a log that would go to log 0, it would not be printed anyway
                    l = self.insertStringAfterWhitepace(l, "//") + "\n"
                if(includePosition != -1):
                    eval_cmd = l[includePosition+numCharsToRemove:].strip()
                    output = self._runEvalCmd(eval_cmd)
                    if(self.remove_comments == True and eval_cmd.startswith("getHeaderLicense") == False and eval_cmd.startswith("getAppRawRepoURL") == False):
                        output = self.commentRemover(output)
                    if(includeNC == False and (self.remove_comments == True or eval_cmd.startswith("getHelperFunctions") == False) and 
                        eval_cmd.startswith("getHeaderLicense") == False):
                        extraNewline = "\n"
                        output_lines = output.split("\n")
                        for i in range(len(output_lines)):
                            if(len(output_lines) - 1 == i and output_lines[i].strip() == ""):
                                # If all is empty, just return a zero-length string
                                return ""
                            elif(output_lines[i].strip() == ""):
                                continue
                            else:
                                output = "\n".join(output_lines[i:])
                                break
                        if(output.endswith("\n")):
                            extraNewline = ""
                        output = "// BEGIN:" + eval_cmd + "\n" + output + extraNewline + "// END:  " + eval_cmd + "\n"
                    if(includePosition > 0):
                        i = 0
                        wd.write(l[:includePosition])
                        previous_line = None
                        first_line = 1 if includeNC == False else 0
                        for nl in output.splitlines():
                            nl = nl.rstrip()
                            if(self.isHiddenLog(nl) == True):
                                # This is a log that would go to log 0, it would not be printed anyway
                                nl = self.insertStringAfterWhitepace(nl, "//")
                            if(not (nl.strip() == "" and previous_line.strip() == "")):
                                if i != 0:
                                    wd.write(' ' * (includePosition) + nl + '\n')
                                else:
                                    wd.write(nl + '\n')
                            i += 1
                            previous_line = nl
                    else:
                        for nl in output.splitlines():
                            if(self.isHiddenLog(nl) == True):
                                # This is a log that would go to log 0, it would not be printed anyway
                                nl = self.insertStringAfterWhitepace(nl, "//")
                            if(previous_line == None or not (nl.strip() == "" and previous_line.strip() == "")):
                                wd.write(nl + '\n')
                            previous_line = nl
                else:
                    if(commentPosition == -1 or (
                        commentPosition > -1 and l != '\n')):
                        if(previous_line == None or not (l.strip() == "" and previous_line.strip() == "")):
                            previous_line = l
                            wd.write(l)
                #print(l.strip())
        
        if(output_groovy_file != None):
            wd.close()
            return(r)
        else:
            content = wd.getvalue()
            wd.close()
            return(content)

    def translateID(self, code_files):
        for d in code_files:
            d['id_main'] = d['id']
            #print("translating: {}".format(d))
            if(self.id_name is not None):
                if(self.id_name in d):
                    d['id'] = d[self.id_name]
                    
                else:
                    d['id'] = 0
            #print("translated: {}".format(d))
        return code_files

    def expandGroovyFilesAndPush(self, code_files, code_type = 'driver'):
        code_files = self.translateID(code_files)
        j=0
        used_code_list = {}
        self.log.info("Starting expandGroovyFilesAndPush(code_type={})".format(code_type))
        for d in code_files:
            aof = None
            expanded_result = None
            if('alternate_output_filename' in d and d['alternate_output_filename'] != ''):
                expanded_result = self.expandGroovyFile(d, code_type=code_type)
                aof = d['alternate_output_filename']
            else:
                expanded_result = self.expandGroovyFile(d, code_type=code_type)
            self.log.debug(expanded_result)
            output_groovy_file = str(self.getBuildDir(code_type) / self.getOutputGroovyFile(d['file'], alternate_output_filename=aof))
            if(d['id'] != 0):
                self.log.info("Working on code with id: {} (id_main={})".format(d['id'], d['id_main']))
                j += 1
                # If we have an MD5 for an older version, check if the MD5 doesn't change if we generate
                # with that older version set in the file. This avoids "updating" files when the only change
                # is the version.
                output_groovy_file_md5 = self.getCheckSumOfFile(output_groovy_file)
                self.log.debug('MD5 for file {} (version: {}): {}'.format(d['id'], self._current_version, output_groovy_file_md5))
                self.log.debug('push_to_dir:' + str(output_groovy_file))
                d['version'] = self._current_version
                md5_match = False
                old_code_version = None
                old_code_checksum = None
                if(code_type == 'driver' and d['id'] in self.driver_checksums):
                    old_code_version = self.driver_checksums[d['id']][1]
                    old_code_checksum = self.driver_checksums[d['id']][0]
                    self.log.debug('Driver old code version: {}, checksum: {}'.format(old_code_version, old_code_checksum))
                elif(code_type == 'app' and d['id'] in self.app_checksums):
                    old_code_version = self.app_checksums[d['id']][1]
                    old_code_checksum = self.app_checksums[d['id']][0]
                    self.log.debug('App old code version: {}, checksum: {}'.format(old_code_version, old_code_checksum))

                self.log.debug("self._current_version = {}".format(self._current_version))

                if(old_code_version != None):
                    if(output_groovy_file_md5 == old_code_checksum):
                        if(old_code_version != self._current_version):
                            self.log.error("Correct MD5, but old_code_version ({}) != _current_version ({})".format(old_code_version, self._current_version))
                        self.log.debug('Skipping updating code id {} since the MD5 matches. (old_code_version={})'.format(d['id'], old_code_version))
                        md5_match = True
                    else:
                        self.log.debug('Checking to see if the MD5 matches when using the same version number as the MD5 for {}. (old_code_version={})'.format(d['id'], old_code_version))
                        new_version = self._current_version
                        d['version'] = old_code_version
                        expanded_result = self.expandGroovyFile(d, code_type=code_type)
                        self.log.debug(expanded_result)
                        output_groovy_file_md5 = self.getCheckSumOfFile(output_groovy_file)
                        self.log.debug('MD5 for file {} with version {}: {}'.format(d['id'], d['version'], output_groovy_file_md5))
                        if(output_groovy_file_md5 == old_code_checksum):
                            md5_match = True
                            self.log.debug('Skipping updating code id {} since the MD5 matches when using the old version ({}).'.format(d['id'], old_code_version))
                            self._current_version = old_code_version
                        else:
                            self._current_version = new_version
                            d['version'] = new_version
                            self.log.debug('Updating code id {} since the MD5 DOESN\'T match when using the old version ({}), so going for the new version: {}.'.format(d['id'], old_code_version, new_version))
                            # Remake the latest version
                            expanded_result = self.expandGroovyFile(d, code_type=code_type)
                            output_groovy_file_md5 = self.getCheckSumOfFile(output_groovy_file)
                            self.log.debug('Reverted to MD5 for file {} with version {}: {}'.format(d['id'], d['version'], output_groovy_file_md5))
                            self.log.debug(expanded_result)
                
                if(md5_match == False):
                    r = self.hubitat_hubspider.push_code(code_type, d['id'], self.getBuildDir(code_type) / self.getOutputGroovyFile(d['file'], alternate_output_filename=aof))
                    if(isinstance(r, int) != True and 'source' in r):
                        # We got a successful update, save the checksum
                        if(code_type == 'driver'):
                            self.driver_checksums[d['id']] = (output_groovy_file_md5, d['version'])
                            self.driver_num_updated += 1
                            self.log.debug('MD5 for returned code {}: {} (version: {})'.format(d['id'], self.driver_checksums[d['id']], d['version']))
                        else:
                            self.app_checksums[d['id']] = (output_groovy_file_md5, d['version'])
                            self.app_num_updated += 1
                            self.log.debug('MD5 for returned code {}: {} (version: {})'.format(d['id'], self.app_checksums[d['id']], d['version']))
                    

                if(code_type == 'driver'):
                    id = int(d['id'])
                    #log.debug("code_files 1: {}".format(str(code_files[id])))
                    #self.log.debug(str(self.he_drivers_dict))
                    self.he_drivers_dict[id].update(expanded_result)
                    if(id in self.he_drivers_dict):
                        used_code_list[id] = d.copy()
                        used_code_list[id].update(self.he_drivers_dict[id])
                    #log.debug("code_files 2: {}".format(str(code_files[id])))
                    self.log.debug("Just worked on Driver ID " + str(id) + " (id_main=" + str(d['id_main']) +")")
                elif(code_type == 'app'):
                    id = int(d['id'])
                    #print(self.he_apps_dict[id])
                    expanded_result.pop('name')
                    self.he_apps_dict[id].update(expanded_result)
                    #print(self.he_apps_dict[id])
                    if(id in self.he_apps_dict):
                        used_code_list[id] = d.copy()
                        used_code_list[id].update(self.he_apps_dict[id])
                    #print(used_code_list[id])
                    self.log.debug("Just worked on App ID " + str(id) + " (id_main=" + str(d['id_main']) + ")")
            else:
                self.log.debug("We don't have an ID for '{}' yet, so let us make one... (id_main={})".format(expanded_result['name'], d['id_main']))
                new_id = self.hubitat_hubspider.push_new_code(code_type, output_groovy_file)
                if(new_id > 0):
                    new_entry = {'id': new_id, 'id_main': d['id_main'], 'name': expanded_result['name'], 'file': str(Path(output_groovy_file).name).replace(self.build_suffix + '.', '.')}
                    if(code_type == 'driver'):
                        self.driver_new[new_id] = new_entry
                    if(code_type == 'app'):
                        self.app_new[new_id] = new_entry
                    self.log.info("Added '{}' with the new ID {} (id_main={})!".format(expanded_result['name'], new_id, d['id_main']))
                else:
                    self.log.error("FAILED to add '{}'! Something unknown went wrong... (id_main={})".format(expanded_result['name'], d['id_main']))

        self.log.info('Had '+str(j)+' {} files to work on...'.format(code_type))
        #self.setUsedDriverList(used_code_list)
        return(used_code_list)
    
    def makeDriverListDoc(self, driver_list, output_file='DRIVERLIST', base_data={}, 
                        filter_function=(lambda dict_to_check, section: True)):
        # (Ab)using the logger Format class here...
        # This will generate a file that can be used as part of documentation or for posting in a Forum
        # Usage examples in hubitat_codebuilder_tool.py
        #generic_format = "* [%(name)s](%(url)s) - Imp1rt URL: [RAW](%(url_raw)s)\n"
        #generic_format = "* [%(name)s](%(base_url)s%(file)s) - Import URL: [RAW](%(base_raw_url)s%(file)s)\n"
        with open (output_file, "w") as wd:
            for section in driver_list:
                section_formatter = PrintFormatter(fmt=section['format'])
                record = PrintRecord()
                record.update(base_data)
                record.name = section['name']
                wd.write(section_formatter.format(record))
                if('items_format' in section):
                    items_formatter = []
                    #self.log.debug('items_format type: {}'.format(str(type(section['items_format']))))
                    if(type(section['items_format']) is list):
                        i = 0
                        for fmt in section['items_format']:
                            #self.log.debug('Found format {}: {}'.format(i, fmt))
                            items_formatter.append(PrintFormatter(fmt=fmt))
                            i += 1
                    else:
                        items_formatter.append(PrintFormatter(fmt=section['items_format']))
                    for d in sorted( section['items'], key = lambda i: i['name']) :
                        record = PrintRecord()
                        record.update(base_data)
                        record.update(d)
                        if(filter_function(d, section)):
                            # Try formatters until we run out
                            for i in range(0, len(items_formatter)):
                                try:
                                    #self.log.debug('Trying formatter: {}'.format(i))
                                    wd.write(items_formatter[i].format(record))
                                    #self.log.debug('OK formatter for {}: {} "{}"'.format(d['name'], i, items_formatter[i].format(record)))
                                    #self.log.debug(d)
                                    # If that format worked, we're done
                                    break
                                except ValueError as e:
                                    #self.log.debug('Incorrect formatter: {}'.format(i))
                                    if(i+1 >= len(items_formatter)):
                                        raise ValueError(e)
                else:
                    if('items' in section):
                        self.log.error('"items" without "items_format"! skipping items in section "{}"'.format(section['name']))