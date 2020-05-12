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

# Local imports
from hubitat_driver_snippets import *
from hubitat_driver_snippets_parser import *
from hubitat_driver_snippets_new_parser import *
from hubitat_driver_snippets_zigbee_parser import *
from hubitat_driver_snippets_metadata import *
from hubitat_codebuilder import HubitatCodeBuilder, PrintFormatter, PrintRecord

# This class extends HubitatCodeBuilder with functions specific for my 
# Tasmota Drivers and Apps. It should not be used directly, but can serve as
# an example of how to use HubitatCodeBuilder for your own code
#
class HubitatCodeBuilderTasmota(HubitatCodeBuilder):

    #def __init__(self, **kwargs):
        # Use this for extracting any arguments you need
        #self.data = kwargs.pop('data', True)
    #    super().__init__(**kwargs)

    def _makeTasmotaConnectDriverListV1(self):
        ts_driver_list = '['
        for d in self.used_driver_list:
            name = self.used_driver_list[d]['name']
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and self.used_driver_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += '"' + name + '",\n'
        ts_driver_list += ']'
        return(ts_driver_list)

    def _makeTasmotaConnectDriverListV2(self):
        short_driver_map = {
            'Tasmota - Sonoff TH Wifi Switch': 'Sonoff TH',
            'Tasmota - Sonoff PowR2': 'Sonoff POW',
            'Tasmota - Sonoff 2CH Wifi Switch': 'Sonoff Dual',
            'Tasmota - Sonoff 4CH Wifi Switch': 'Sonoff 4CH',
            'Tasmota - Sonoff IFan02 Wifi Controller': 'Sonoff IFan02',
            'Tasmota - Sonoff S31 Wifi Switch': 'Sonoff S31',
            'Tasmota - Sonoff S2X': 'Sonoff S2',
            'Tasmota - Sonoff SC': 'Sonoff SC',
            'Tasmota - Sonoff Bridge': 'Sonoff Bridge',
            'Tasmota - Tuya Wifi Touch Switch': 'Tuya',
        }
        i = 0
        ts_driver_list = ''
        for d in self.used_driver_list:
            name = self.used_driver_list[d]['name']
            try:
                name_short = short_driver_map[name]
            except Exception:
                name_short = name
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and self.used_driver_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += ('else ' if i > 0 else '') + \
                    'if (selectedDevice?.value?.name?.startsWith("' + name_short + '"))\n' + \
                    '    deviceHandlerName = "' + name + '"\n'
        return(ts_driver_list)

    def _runEvalCmdAdditional(self, eval_cmd):
        # Here we can filter eval commands we want special handling of
        # Anything not handled here will run in eval...
        # We have access to the following, just so we don't forget:
        #self._alternate_output_filename = alternate_output_filename
        #self._alternate_name = alternate_name
        #self._alternate_namespace = alternate_namespace
        #self._alternate_vid = alternate_vid
        #self._alternate_template = alternate_template
        #self._alternate_module = alternate_module
        #self._config_dict = config_dict

        if(eval_cmd == 'makeTasmotaConnectDriverListV1()'):
            self.log.debug("Executing makeTasmotaConnectDriverListV1()...")
            output = self._makeTasmotaConnectDriverListV1()
            return(True, output)
        elif(eval_cmd == 'makeTasmotaConnectDriverListV2()'):
            self.log.debug("Executing makeTasmotaConnectDriverListV2()...")
            output = self._makeTasmotaConnectDriverListV2()
            return(True, output)
        elif(eval_cmd == 'getAppRawRepoURL()'):
            self.log.debug("Executing appRawRepoURL()...")
            output = "// IMPORT URL: {}{}".format(self.app_raw_repo_url, str(self._output_groovy_file))
            return(True, output)
        elif('numSwitches' in self._config_dict and eval_cmd.startswith('getDefaultMetadataPreferencesForParentDevices(')):
            self.log.debug("Executing getDefaultMetadataPreferencesForParentDevices(numSwitches={})...".format(self._config_dict['numSwitches']))
            output = self.calling_namespace.getDefaultMetadataPreferencesForParentDevices(self._config_dict['numSwitches'])
            return(True, output)
        elif(eval_cmd.startswith('getDefaultFunctions(')):
            comment = ''
            separator = ''
            if('comment' in self._config_dict):
                comment = self._config_dict['comment']
                separator = ' - '
            if('deviceLink' in self._config_dict):
                comment = '{}{}<a target=\\"blakadder\\" href=\\"{}\\">Device Model Info</a>'.format(comment, separator, self._config_dict['deviceLink'])
            self.log.debug("Executing getDefaultFunctions(comment={}, driverVersionSpecial={})...".format(comment, self._current_version))
            output = self.calling_namespace.getDefaultFunctions(comment=comment, driverVersionSpecial=self._current_version)
            return(True, output) 
        elif(eval_cmd.startswith('getHeaderLicense(')):
            self.log.debug("Executing getHeaderLicense(driverVersionSpecial={})...".format(self._current_version))
            output = self.calling_namespace.getHeaderLicense(driverVersionSpecial=self._current_version)
            return(True, output)
        elif(eval_cmd.startswith('getDefaultAppMethods(')):
            self.log.debug("Executing getDefaultAppMethods(driverVersionSpecial={})...".format(self._current_version))
            output = self.calling_namespace.getDefaultAppMethods(driverVersionSpecial=self._current_version)
            return(True, output)
        #getSpecialDebugEntry
        elif(('specialDebugLabel' in self._config_dict) and eval_cmd.startswith('getSpecialDebugEntry(')):
            self.log.debug("Executing getSpecialDebugEntry(label={})...".format(self._config_dict['specialDebugLabel']))
            output = self.calling_namespace.getSpecialDebugEntry(label=self._config_dict['specialDebugLabel'])
            return(True, output)
        elif(('childType' in self._config_dict) and eval_cmd.startswith('getCreateChildDevicesCommand(')):
            self.log.debug("Executing getCreateChildDevicesCommand(childType={})...".format(self._config_dict['childType']))
            output = self.calling_namespace.getCreateChildDevicesCommand(childType=self._config_dict['childType'])
            return(True, output)
        elif(self._alternate_template != None and self._alternate_template != '' and eval_cmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
            self.log.debug("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '" + self._alternate_template + "')...")
            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, self._alternate_template)
            return(True, output)
        elif(self._alternate_module != None and self._alternate_module != '' and eval_cmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
            self.log.debug("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(" + self._alternate_module + ")...")
            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(self._alternate_module)
            return(True, output)
        else:
            return(False, None)

    def makeDriverListFilter(self, dict_to_check, section):
        # Filter for the DriverList
        if(dict_to_check['name'] != 'TuyaMCU Wifi Touch Switch Legacy (Child)' and \
            dict_to_check['name'].startswith('DO NOT USE') == 0):
            return True
        else:
            return False

    
 
    