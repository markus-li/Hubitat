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
  Hubitat driver and app developer tool
  WARNING: Do NOT run this script unless you know what it does, it may DELETE your data!
           If you use this code, please contact me so I know there is interest in this!
  NOTE: This is a Work In Progress, feel free to use it, but don't rely on it not changing completely!
"""
# External modules
from pathlib import Path
import logging
import io
from colorama import init, Fore, Style
from os import path
from pygit2 import Repository
import sys
init()
sys.path.insert(0, "private/tools")

#logging.basicConfig(level=logging.DEBUG,
#    format="%(asctime)s:%(levelname)s:%(message)s")

# Internal modules
from hubitat_hubspider import HubitatHubSpider
from hubitat_codebuilder import HubitatCodeBuilder, HubitatCodeBuilderLogFormatter
from hubitat_codebuilder_tasmota import HubitatCodeBuilderTasmota
from hubitat_packagemanagertool import HubitatPackageManagerTool, HubitatPackageManagerPackage
import repo_tool

# Internal functions
from hubitat_driver_snippets import *
from hubitat_driver_snippets_parser import *
from hubitat_driver_snippets_new_parser import *
from hubitat_driver_snippets_zigbee_parser import *
from hubitat_driver_snippets_metadata import *

# Setup the logger
log = logging.getLogger(__name__)
errors = io.StringIO()
log.setLevel(logging.DEBUG)
log_cb = logging.getLogger(HubitatCodeBuilder.__module__)
log_cb.setLevel(logging.DEBUG)
log_hs = logging.getLogger(HubitatHubSpider.__module__)
log_hs.setLevel(logging.DEBUG)
log_rt = logging.getLogger(repo_tool.__name__)
log_rt.setLevel(logging.DEBUG)

h = logging.StreamHandler()
h.setLevel(logging.DEBUG)
h.setFormatter(HubitatCodeBuilderLogFormatter(error_beep=True))
ha = logging.StreamHandler(errors)
ha.setLevel(logging.WARN)
ha.setFormatter(HubitatCodeBuilderLogFormatter(error_beep=False))

hhs = logging.StreamHandler()
hhs.setLevel(logging.DEBUG)
hhs.setFormatter(HubitatCodeBuilderLogFormatter(error_beep=True, debug_color=Fore.CYAN, default_color=Fore.MAGENTA))

hhsa = logging.StreamHandler(errors)
hhsa.setLevel(logging.WARN)
hhsa.setFormatter(HubitatCodeBuilderLogFormatter(error_beep=False))

log.addHandler(h)
log.addHandler(ha)
log_cb.addHandler(h)
log_cb.addHandler(ha)
log_hs.addHandler(hhs)
log_hs.addHandler(hhsa)
log_rt.addHandler(h)
log_rt.addHandler(ha)


try:
    from config.driver_list_2nd_hub import driver_files_2nd
    update_2nd_hub_drivers = True
except SyntaxError as e:
    log.error("SyntaxError: 2nd hub config NOT loaded: {}".format(e))
    update_2nd_hub_drivers = False


def getExpandedDriverList(driver_selection, all_drivers):
    driver_files_new = []
    if(driver_selection != None and driver_selection != []):
        for d in driver_selection:
            if(d['id'] != 0 and ('file' in d) == False):
                for d_info in all_drivers:
                    if (d['id'] == d_info['id']):
                        d=d_info.copy()
                        break
            if('file' in d):
                driver_files_new.append(d.copy())
    return driver_files_new


# NOTE: All function names use (or at least should use) mixedCaps since this is used with Groovy and it makes
#       it less confusing not changing style all the time. 
def main():
    branch_name = Repository('.').head.shorthand
    base_repo_url = 'https://github.com/markus-li/Hubitat/blob/'+branch_name+'/drivers/expanded/'
    base_raw_repo_url = 'https://raw.githubusercontent.com/markus-li/Hubitat/'+branch_name+'/drivers/expanded/'
    app_raw_repo_url = 'https://raw.githubusercontent.com/markus-li/Hubitat/'+branch_name+'/apps/expanded/'

    # Get us a Code Builder...
    
    log.debug('Getting started...')
    #HubitatHubSpider.saveConfig('192.168.1.1', 'username', 'password', 'hhs_sample.cfg')
    hhs = HubitatHubSpider(None, 'hubitat_hubspider.cfg')
    hhs_2 = HubitatHubSpider(None, 'hhs_10_2.cfg', id_name='id_2')
    hhs_3 = HubitatHubSpider(None, 'hhs_10_3.cfg', id_name='id_3')
    # Check the result from login()
    log.debug(hhs.login())
    log.debug(hhs_2.login())
    log.debug(hhs_3.login())

    default_version = "v1.0.2.MMDDTb"
    checksum_file_suffix = None
    remove_comments = True
    is_beta = True
    if(branch_name == 'release'):
        default_version = "v1.0.2.MMDDT"
        checksum_file_suffix = "release"
        is_beta = False

    # Setup the Package Manager objects
    pm = HubitatPackageManagerTool("Markus", "2.1.7", 
        gitHubUrl="https://github.com/markus-li/Hubitat")

    t4he_pkg = HubitatPackageManagerPackage("Tasmota for Hubitat Elevation", isBeta=is_beta,
        documentationLink="https://github.com/markus-li/Hubitat/wiki", 
        communityLink="https://community.hubitat.com/t/release-tasmota-for-he-auto-detecting-tasmota-drivers-tasmota-firmware-7-x-8-x-for-he-for-use-with-tuya-sonoff-and-other-esp-devices/39322")

    # By including our namespace, anything we import in this file is available
    # to call by the include tags in the .groovy files when we process them
    cb = HubitatCodeBuilderTasmota(hhs, calling_namespace=sys.modules[__name__], driver_raw_repo_url=base_raw_repo_url,
                app_raw_repo_url=app_raw_repo_url, default_version=default_version, checksum_file_suffix=checksum_file_suffix, remove_comments=remove_comments)
    cb_2 = HubitatCodeBuilderTasmota(hhs_2, id_name='id_2', calling_namespace=sys.modules[__name__], driver_raw_repo_url=base_raw_repo_url,
                app_raw_repo_url=app_raw_repo_url, default_version=default_version, checksum_file_suffix=checksum_file_suffix, remove_comments=remove_comments)
    cb_3 = HubitatCodeBuilderTasmota(hhs_3, id_name='id_3', calling_namespace=sys.modules[__name__], driver_raw_repo_url=base_raw_repo_url,
                app_raw_repo_url=app_raw_repo_url, default_version=default_version, checksum_file_suffix=checksum_file_suffix, remove_comments=remove_comments)
                
    cb_private = HubitatCodeBuilderTasmota(hhs, calling_namespace=sys.modules[__name__], app_dir=Path('./private/apps'), 
                app_build_dir=Path('./private/apps/expanded'), driver_dir=Path('./private/drivers'), 
                driver_build_dir=Path('./private/drivers/expanded'), default_version='v0.1.1.MMDD', 
                checksum_file_suffix=checksum_file_suffix, checksum_file='./private/__hubitat_checksums')
    cb_private_2 = HubitatCodeBuilderTasmota(hhs_2, id_name='id_2', calling_namespace=sys.modules[__name__], app_dir=Path('./private/apps'), 
                app_build_dir=Path('./private/apps/expanded'), driver_dir=Path('./private/drivers'), 
                driver_build_dir=Path('./private/drivers/expanded'), default_version='v0.1.1.MMDD', 
                checksum_file_suffix=checksum_file_suffix, checksum_file='./private/__hubitat_checksums')
    cb_private_3 = HubitatCodeBuilderTasmota(hhs_3, id_name='id_3', calling_namespace=sys.modules[__name__], app_dir=Path('./private/apps'), 
                app_build_dir=Path('./private/apps/expanded'), driver_dir=Path('./private/drivers'), 
                driver_build_dir=Path('./private/drivers/expanded'), default_version='v0.1.1.MMDD', 
                checksum_file_suffix=checksum_file_suffix, checksum_file='./private/__hubitat_checksums')
    #cb = HubitatCodeBuilderTasmota()
    
    driver_files = [
        # Tasmota drivers WITHOUT their own base-file:
        {'id': 418, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child.groovy', \
         'alternate_output_filename': 'tasmota-tuyamcu-wifi-touch-switch-legacy-child', \
         'alternate_name': 'Tasmota - TuyaMCU Wifi Touch Switch Legacy (Child)', \
         'alternate_namespace': 'tasmota-legacy'},
        {'id': 556, 'file': 'tasmota-sonoff-basic.groovy', \
         'alternate_output_filename': 'tasmota-sonoff-basic-r3', \
         'alternate_name': 'Tasmota - Sonoff Basic R3',
         'deviceLink': 'https://templates.blakadder.com/sonoff_basic_R3.html'},
        {'id': 580, 'file': 'tasmota-tuyamcu-wifi-dimmer.groovy' , \
         'alternate_output_filename': 'tasmota-tuyamcu-ce-wf500d-dimmer', \
         'alternate_name': 'Tasmota - TuyaMCU CE Smart Home WF500D Dimmer (EXPERIMENTAL)', \
         'alternate_template': '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         'deviceLink': 'https://templates.blakadder.com/ce_smart_home-WF500D.html',
         'comment': 'WORKING, but need feedback from users'},
        
        {'id': 583, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-awp02l-n-plug', \
         'alternate_name': 'Tasmota - AWP02L-N Plug', \
         'alternate_template': '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}',
         'deviceLink': 'https://templates.blakadder.com/hugoai_awp02l-n.html'},
        {'id': 584, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-cyyltf-bifans-j23-plug', \
         'alternate_name': 'Tasmota - CYYLTF BIFANS J23 Plug', \
         'alternate_template': '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}',
         'deviceLink': 'https://templates.blakadder.com/cyyltd_bifans_J23.html'},
        {'id': 585, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-gosund-wp3-plug', \
         'alternate_name': 'Tasmota - Gosund WP3 Plug', \
         'alternate_template': '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}',
         'deviceLink': 'https://templates.blakadder.com/gosund_wp3.html'},
        {'id': 586, 'file': 'tasmota-generic-pm-plug.groovy' , \
         'alternate_output_filename': 'tasmota-sk03-pm-outdoor-plug', \
         'alternate_name': 'Tasmota - SK03 Power Monitor Outdoor Plug', \
         'alternate_template': '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}',
         'deviceLink': 'https://templates.blakadder.com/SK03_outdoor.html'},
        #{'id': 587, 'file': 'tasmota-generic-pm-plug.groovy' , \
        # 'alternate_output_filename': 'tasmota-aoycocr-x10s-pm-plug', \
        # 'alternate_name': 'Tasmota - Aoycocr X10S Power Monitor Plug', \
        # 'alternate_template': '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}',
        # 'deviceLink': 'https://templates.blakadder.com/aoycocr_X10S.html'},
        {'id': 592, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-sv', \
         'alternate_name': 'Tasmota - Sonoff SV', \
         'alternate_template': '{"NAME":"Sonoff SV","GPIO":[17,255,0,255,255,255,0,0,21,56,255,0,0],"FLAG":1,"BASE":3}',
         'deviceLink': 'https://templates.blakadder.com/sonoff_SV.html'},
        #{'id': 361, 'file': 'tasmota-generic-thp-device.groovy' , \
        # 'alternate_output_filename': 'tasmota-sonoff-th', \
        # 'alternate_name': 'Tasmota - Sonoff TH', \
        # 'alternate_template': '{"NAME":"Sonoff TH","GPIO":[17,255,0,255,255,0,0,0,21,56,255,0,0],"FLAG":0,"BASE":4}',
        # 'deviceLink': 'https://templates.blakadder.com/sonoff_TH.html'},
        #{'id': 547, 'file': 'tasmota-sonoff-powr2.groovy' , \
        # 'alternate_output_filename': 'tasmota-sonoff-pow', \
        # 'alternate_name': 'Tasmota - Sonoff POW', \
        # 'alternate_template': '{"NAME":"Sonoff Pow","GPIO":[17,0,0,0,0,130,0,0,21,132,133,52,0],"FLAG":0,"BASE":6}',
        # 'deviceLink': 'https://templates.blakadder.com/sonoff_Pow.html'},
        #{'id': 359, 'file': 'tasmota-sonoff-powr2.groovy' , \
        # 'alternate_output_filename': 'tasmota-sonoff-s31', \
        # 'alternate_name': 'Tasmota - Sonoff S31', \
        # 'alternate_template': '{"NAME":"Sonoff S31","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":41}',
        # 'deviceLink': 'https://templates.blakadder.com/sonoff_S31.html'},
        {'id': 643, 'file': 'tasmota-generic-pm-plug-parent.groovy' , \
         'alternate_output_filename': 'tasmota-kmc-4-pm-plug', \
         'alternate_name': 'Tasmota - KMC 4 Power Monitor Plug', \
         'alternate_template': '{"NAME":"KMC 4 Plug","GPIO":[0,56,0,0,133,132,0,0,130,22,23,21,17],"FLAG":0,"BASE":36}',
         'numSwitches': 3, 'deviceLink': 'https://templates.blakadder.com/kmc-4.html'},
        #{'id': 644, 'file': 'tasmota-generic-pm-plug-child.groovy' , \
        # 'alternate_output_filename': 'tasmota-kmc-4-pm-plug-child', \
        # 'alternate_name': 'Tasmota - KMC 4 Power Monitor Plug (Child)'},
        #{'id': 555, 'file': 'tasmota-generic-pm-plug.groovy' , \
        # 'alternate_output_filename': 'tasmota-awp04l-pm-plug', \
        # 'alternate_name': 'Tasmota - AWP04L Power Monitor Plug', \
        # 'alternate_template': '{"NAME":"AWP04L","GPIO":[57,255,255,131,255,134,0,0,21,17,132,56,255],"FLAG":0,"BASE":18}',
        # 'deviceLink': 'https://templates.blakadder.com/awp04l.html'},
        #{'id': 646, 'file': 'tasmota-sonoff-4ch-parent.groovy' , \
        # 'alternate_output_filename': 'tasmota-sonoff-4ch-pro-parent', \
        # 'alternate_name': 'Tasmota - Sonoff 4CH Pro (Parent)', \
        # 'alternate_template': '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
        # 'comment': 'UNTESTED driver', 'numSwitches': 4,
        # 'deviceLink': 'https://templates.blakadder.com/sonoff_4CH_Pro.html'},
        #{'id': 647, 'file': 'tasmota-generic-pm-plug-child.groovy' , \
        # 'alternate_output_filename': 'tasmota-sonoff-4ch-pro-child', \
        # 'alternate_name': 'Tasmota - Sonoff 4CH Pro (Child)'},

        # Tasmota Drivers WITH their own base-file
        {'id': 548, 'file': 'tasmota-tuyamcu-wifi-touch-switch.groovy' },
        {'id': 549, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child.groovy' },
        #{'id': 550, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child-test.groovy' },
        {'id': 513, 'file': 'tasmota-sonoff-powr2.groovy', 'deviceLink': 'https://templates.blakadder.com/sonoff_Pow_R2.html'},
        {'id': 551, 'file': 'tasmota-sonoff-s2x.groovy', 'comment': 'Works with both Sonoff S20 and S26.',
        'deviceLink': 'https://templates.blakadder.com/sonoff_S20.html'},
        {'id': 554, 'file': 'tasmota-sonoff-mini.groovy', 'deviceLink': 'https://templates.blakadder.com/sonoff_mini.html'},
        {'id': 560, 'file': 'tasmota-sonoff-basic.groovy', 'deviceLink': 'https://templates.blakadder.com/sonoff_basic.html'},
        {'id': 553, 'file': 'tasmota-s120-plug.groovy' },
        #{'id': 559, 'file': 'tasmota-brilliant-bl20925-pm-plug.groovy', 'deviceLink': 'https://templates.blakadder.com/brilliant_BL20925.html'},
        #{'id': 577, 'file': 'tasmota-prime-ccrcwfii113pk-plug.groovy', 'deviceLink': 'https://templates.blakadder.com/prime_CCRCWFII113PK.html'},
        {'id': 590, 'file': 'tasmota-tuyamcu-wifi-dimmer.groovy', 'comment': 'WORKING, but need feedback from users'},
        {'id': 588, 'file': 'tasmota-unbranded-rgb-controller-with-ir.groovy' },
        {'id': 362, 'file': 'tasmota-sonoff-4ch-parent.groovy' , 
         'comment': 'UNTESTED driver',
         'deviceLink': 'https://templates.blakadder.com/sonoff_4CH.html',
         'numSwitches': 4},
        {'id': 645, 'file': 'tasmota-generic-pm-plug-child.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-4ch-child', \
         'alternate_name': 'Tasmota - Sonoff 4CH (Child)'},

        #{'id': 738, 'file': 'tasmota-znsn-tuyamcu-wifi-curtain-wall-panel.groovy',
        # 'comment': 'NOT GENERIC - read the instructions', },
        # The below one is unused and safe for testing:
        {'id': 587, 'file': 'tasmota-znsn-tuyamcu-wifi-curtain-wall-panel.groovy',
         'comment': 'NOT GENERIC - read the instructions', },
        
        # https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433pi 
        {'id': 648, 'file': 'tasmota-sonoff-rf-bridge-parent.groovy' , 
         'comment': 'Functional - Need feedback',
         'deviceLink': 'https://templates.blakadder.com/sonoff_RF_bridge.html',
         'numSwitches': 1, 'specialDebugLabel': 'RF Codes', 'childType': 'not_component'},
        
        {'id': 650, 'file': 'tasmota-rflink-parent.groovy' , 
         'comment': 'Functional - Need feedback',
         'deviceLink': 'http://www.rflink.nl/blog2/wiring',
         'numSwitches': 1, 'specialDebugLabel': 'RF Codes', 'childType': 'not_component'},
        #{'id': 651, 'file': 'tasmota-sonoff-rf-bridge-child.groovy' , \
        # 'alternate_output_filename': 'tasmota-rflink-child', \
        # 'alternate_name': 'Tasmota - DO NOT USE RFLink (Child)'},
        {'id': 649, 'file': 'tasmota-rf-ir-switch-toggle-push-child.groovy', 
         'specialDebugLabel': 'Code Learning'},
        {'id': 673, 'file': 'tasmota-rf-ir-motion-sensor-child.groovy', 
         'specialDebugLabel': 'Code Learning'},
        {'id': 674, 'file': 'tasmota-rf-ir-contact-sensor-child.groovy', 
         'specialDebugLabel': 'Code Learning'},
        {'id': 675, 'file': 'tasmota-rf-ir-water-sensor-child.groovy', 
         'specialDebugLabel': 'Code Learning'},
        {'id': 676, 'file': 'tasmota-rf-ir-smoke-detector-child.groovy', 
         'specialDebugLabel': 'Code Learning'},

        # Special sensor drivers:
        {'id': 651, 'file': 'tasmota-sensor-distance.groovy' , 
         'comment': 'UNTESTED driver',
         'deviceLink': 'https://github.com/arendst/Tasmota/wiki/HC-SR04',},
        
        # Generic Tasmota Devices:
        {'id': 552, 'file': 'tasmota-generic-wifi-switch-plug.groovy',
         'comment': 'Works as Plug/Outlet with Alexa' },
        {'id': 769, 'file': 'tasmota-generic-wifi-switch-light.groovy',
         'comment': 'Works as Light with Alexa' },
        {'id': 591, 'file': 'tasmota-generic-rgb-rgbw-controller-bulb-dimmer.groovy', 'comment': 'RGB+WW+CW should all work properly, please report progress' },
        {'id': 578, 'file': 'tasmota-generic-thp-device.groovy' },
        {'id': 558, 'file': 'tasmota-generic-pm-plug.groovy'},
        {'id': 641, 'file': 'tasmota-generic-pm-plug-parent.groovy', 'comment': 'Multi-relay support'},
        {'id': 642, 'file': 'tasmota-generic-pm-plug-child.groovy' },
        {'id': 737, 'file': 'tasmota-generic-wifi-dimmer.groovy' },

        # Universal drivers
        {'id': 865, 'id_2': 328, 'id_3': 342, 'file': 'tasmota-universal-parent.groovy', 'specialDebugLabel': 'descriptionText',
         'required': True },
        
        {'id': 866, 'id_2': 364, 'id_3': 343, 'file': 'tasmota-universal-multi-sensor-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 993, 'file': 'tasmota-universal-fancontrol-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 589, 'id_2': 331, 'file': 'tasmota-universal-curtain-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 359, 'file': 'tasmota-universal-switch-as-contact-sensor-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 361, 'file': 'tasmota-universal-switch-as-motion-sensor-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 555, 'file': 'tasmota-universal-switch-as-water-sensor-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 581, 'id_2': 333, 'file': 'tasmota-universal-plug-outlet-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 582, 'id_2': 329, 'file': 'tasmota-universal-bulb-light-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        
        {'id': 577, 'id_2': 330, 'file': 'tasmota-universal-ct-rgb-cw-ww-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 646, 'id_2': 332, 'file': 'tasmota-universal-dimmer-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 559, 'file': 'tasmota-universal-metering-dimmer-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 644, 'id_2': 0, 'file': 'tasmota-universal-metering-plug-outlet-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        {'id': 647, 'id_2': 0, 'file': 'tasmota-universal-metering-bulb-light-child.groovy', 
            'specialDebugLabel': 'descriptionText' },
        
        # Testing versions
        {'id': 867, 'file': 'tasmota-universal-parent.groovy', 'specialDebugLabel': 'descriptionText',
         'alternate_output_filename': 'tasmota-universal-parent-testing', \
         'alternate_name': 'Tasmota - Universal Parent Testing' },
        {'id': 868, 'file': 'tasmota-universal-multi-sensor-child.groovy', 'specialDebugLabel': 'descriptionText',
         'alternate_output_filename': 'tasmota-universal-multi-sensor-testing-child', \
         'alternate_name': 'Tasmota - Universal Multi Sensor Testing (Child)' },

        {'id': 1057, 'file': 'testing-get-driver-runtime-data.groovy', 'version': 'v0.1.0.MMDD'  },

        # Zigbee
        {'id': 579, 'file': 'zigbee-generic-wifi-switch-plug.groovy' },
        {'id': 801, 'id_2': 368, 'id_3': 335, 'file': 'zigbee-aqara-smart-curtain-motor.groovy', 'version': 'v1.0.1.MMDD',
         'comment': 'Works with models ZNCLDJ11LM & ZNCLDJ12LM' },        
        {'id': 1122, 'id_2': 367, 'id_3': 0, 'file': 'zigbee-xiaomi-mijia-smart-light-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model GZCGQ01LM.' },
        
        
        # Virtual
        {'id': 962, 'file': 'javascript-injection-driver.groovy', 'version': 'v0.1.0.MMDDb' },

        # The following can be overwritten: 
    ]

    driver_files_private = [
        # Private drivers:
        #{'id': None, 'file': 'zigbee-generic-wifi-switch-plug-private.groovy' },
        {'id': 547, 'id_3': 341, 'file': 'testing-bare-minimum-driver.groovy', 'version': 'v0.1.0.MMDD' },

        # Zigbee:
        {'id': 1121, 'id_2': 363, 'id_3': 0, 'file': 'zigbee-xiaomi-aqara-opple-button-switch-remote.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with models WXKG01LM, WXKG11LM (2015), WXKG11LM (2018), WXKG12LM, WXKG02LM (2016 & 2018), WXKG03LM (2016 & 2018), WXCJKG11LM, WXCJKG12LM & WXCJKG13LM' },
        {'id': 1153, 'id_2': 322, 'file': 'zigbee-xiaomi-aqara-contact-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model MCCGQ01LM & MCCGQ11LM.' },
        {'id': 1154, 'id_2': 324, 'id_3': 0, 'file': 'zigbee-xiaomi-aqara-motion-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model RTCGQ01LM & RTCGQ11LM.' },
        {'id': 1155, 'id_2': 362, 'id_3': 0, 'file': 'zigbee-ihorn-motion-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model LH-992ZB.' },
        {'id': 1185, 'id_2': 325, 'file': 'zigbee-xiaomi-aqara-plug-outlet.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model ZNCZ02LM & QBCZ11LM.' },
        {'id': 1186, 'id_2': 334, 'id_3': 0, 'file': 'zigbee-aqara-bulb.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model ZNLDP12LM.' },
        {'id': 1256, 'id_2': 361, 'id_3': 0, 'file': 'zigbee-xiaomi-aqara-oppo-6-button.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model WXCJKG13LM.' },
        {'id': 1313, 'id_2': 0, 'id_3': 0, 'file': 'zigbee-ikea-5-button-remote.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model ???.' },
        {'id': 1345, 'id_2': 326, 'id_3': 0, 'file': 'zigbee-xiaomi-aqara-temperature-humidity.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model WSDCGQ01LM & WSDCGQ11LM.' },
        {'id': 1377, 'id_2': 327, 'id_3': 0, 'file': 'zigbee-aqara-water-leak-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model SJCGQ11LM.' },
        {'id': 1378, 'id_2': 360, 'id_3': 0, 'file': 'zigbee-aqara-vibration-sensor.groovy', 'version': 'v0.6.1.MMDD',
         'comment': 'Works with model DJT11LM.' },
    ]

    # Future devices to implement support for:
    # https://templates.blakadder.com/maxcio_400ml_diffuser.html
    # https://templates.blakadder.com/ytf_ir_bridge.html

    # Future devices to MAYBE implement support for, if someone asks... :
    # https://templates.blakadder.com/sonoff_SC.html

    # RF Bridge functions to implement:
    # * Motion Sensor (receive) - DONE
    # * Button (receive) - DONE
    # * RF Remote??? (receive) - DONE
    # * Door/Window Sensor (receive) - DONE
    # * Smoke Alarm (receive) - DONE
    # * Water Sensor (receive) - DONE
    # * On/Off signal transmitter (send)
    # * Custom Signal Transmitter??? (send)

    # IR Bridge functions to implement
    # * Custom IR Remote Control Button (receive, each button as a Child) 
    # * Specific IR Remote Control with full button support (SEND, many buttons per Child)
    # * IR Repeater (in on one device, out on another or same, select output device in Child?)
    # * Send Custom IR command (implement as toggle/button?)
    # 

    expected_num_drivers = len(driver_files)
    
    # Example driver: https://github.com/hubitat/HubitatPublic/blob/master/examples/drivers/GenericZigbeeRGBWBulb.groovy
    # RGB Example: https://github.com/damondins/hubitat/blob/master/Tasmota%20RGBW%20LED%20Light%20Bulb/Tasmota%20RGBW%20LED%20Light%20Bulb

    # As long as we have an id, we can just supply that here instead of the whole config...
    # 651 left over from RF Link Child
    driver_files_active = [
        {'id': 865}, {'id': 866}, # Universal Drivers RELEASE
        #{'id': 866},
        {'id': 359}, # Switch as Contact Sensor Child
        {'id': 361}, # Switch as Motion Sensor Child
        {'id': 555}, # Switch as Water Sensor Child
        {'id': 993}, # Fan Control Child
        
        {'id': 577}, # CT/RGB/RGB+CW+WW Child
        {'id': 646}, # Dimmer Child
        {'id': 559}, # Metering Dimmer Child
        {'id': 589}, # Curtain Child
        {'id': 644}, {'id': 647}, # Metering Children
        {'id': 581}, {'id': 582}, # Switch Children
        
        #{'id': 962}, # Javascript Injection Driver
        #{'id': 867}, {'id': 868},  # Universal Drivers TESTING
        
        #{'id': 865}
        {'id': 1057}, # Testing - Get Driver Runtime Data
        # Zigbee drivers :
        {'id': 1122}, # Xiaomi Mijia Smart Light Sensor
        {'id': 801}, # Aqara Smart Curtain
 
    ]
    
    driver_files_private_active = [
        #{'id': 547},
        #{'id': 866},

        # Zigbee
        {'id': 1378}, # Aqara Vibration Sensor
        {'id': 1377}, # Aqara Water Leak Sensor
        {'id': 1345}, # Xiaomi/Aqara Temperature/Humidity Sensor
        {'id': 1153}, # Xiaomi/Aqara Contact Sensors
        {'id': 1186}, # Aqara Bulb
        {'id': 1185}, # Xiaomi/Aqara Plug/Outlet
        {'id': 1154}, # Xiaomi/Aqara Motion Sensors
        {'id': 1155}, # iHorn Motion Sensor
        {'id': 1121}, # Xiaomi/Aqara Button/Switch
        #{'id': 1256}, # Aqara Oppo 6 Buttons
        #{'id': 1313}, # IKEA 5 Button Remote
        
    ]
    driver_files_active_2 = [
        {'id': 865}, {'id': 866}, # Universal Drivers RELEASE
        {'id': 577}, # CT/RGB/RGB+CW+WW Child
        {'id': 581}, {'id': 582}, # Switch Children
        {'id': 589}, # Curtain Child
        {'id': 646}, # Dimmer Child

        # Zigbee drivers :
        {'id': 1122}, # Xiaomi Mijia Smart Light Sensor
        {'id': 801}, # Aqara Smart Curtain
    ]
    driver_files_active_2 = []
    driver_files_private_active_2 = [
        #{'id': 1186}, # Aqara Bulb
        #{'id': 801}, {'id': 547},
        #{'id': 865}, {'id': 866}, # Universal Drivers RELEASE

        # Zigbee
        {'id': 1378}, # Aqara Vibration Sensor
        {'id': 1377}, # Aqara Water Leak Sensor
        {'id': 1345}, # Xiaomi/Aqara Temperature/Humidity Sensor
        {'id': 1153}, # Xiaomi/Aqara Contact Sensors
        {'id': 1186}, # Aqara Bulb
        {'id': 1185}, # Xiaomi/Aqara Plug/Outlet
        {'id': 1154}, # Xiaomi/Aqara Motion Sensors
        {'id': 1155}, # iHorn Motion Sensor
        {'id': 1121}, # Xiaomi/Aqara Button/Switch
    ]
    #driver_files_private_active_2 = []

    driver_files_active_3 = [
        #{'id': 801}, {'id': 547},
        #{'id': 865}, {'id': 866}, # Universal Drivers RELEASE
    ]
    expected_num_drivers = 1

    driver_files_active = getExpandedDriverList(driver_files_active, driver_files)
    driver_files_active_2 = getExpandedDriverList(driver_files_active_2, driver_files)
    driver_files_active_3 = getExpandedDriverList(driver_files_active_3, driver_files)

    driver_files_private_active = getExpandedDriverList(driver_files_private_active, driver_files_private)
    driver_files_private_active_2 = getExpandedDriverList(driver_files_private_active_2, driver_files_private)
    #print(driver_files)

    # Setting id to 0 will have the Code Builder submit the driver as a new one, don't forget to note the ID 
    # and put it in before submitting again. Also, if there are code errors when submitting a NEW file
    # there's no error messages explaining why, only that it failed... When UPDATING code, any failure messages
    # normally seen in the web code editor, will be seen in the build console.

    #log.debug('Testing to create a new driver...')
    #new_id = hhs.push_new_driver(cb.getBuildDir('driver') / 'tasmota-unbranded-rgb-controller-with-ir-expanded.groovy')

    #cb.clearChecksums()

    generic_drivers = []
    specific_drivers = []

    parent_drivers = []
    child_drivers = []
    print(driver_files_active_3)

    if(branch_name != 'release'):
        used_driver_list_2 = cb_2.expandGroovyFilesAndPush(driver_files_active_2, code_type='driver')
        used_driver_list_3 = cb_3.expandGroovyFilesAndPush(driver_files_active_3, code_type='driver')
    
    # The main target needs to be last to keep everything in a consistent state
    used_driver_list = cb.expandGroovyFilesAndPush(driver_files_active, code_type='driver')
    
    if(branch_name != 'release'):
        used_driver_list_private = cb_private.expandGroovyFilesAndPush(driver_files_private_active, code_type='driver')
        used_driver_list_private_2 = cb_private_2.expandGroovyFilesAndPush(driver_files_private_active_2, code_type='driver')
    #print(used_driver_list)
    #print(driver_files_active)
    sorted_driver_list = []
    for d in sorted(used_driver_list.values(), key=lambda k: k['name']):
        if(d['name'] == "Tasmota - Universal Parent"):
            sorted_driver_list.insert(0, d)
        else:
            sorted_driver_list.append(d)
    for d in sorted_driver_list:
        if(d['name'].startswith('Tasmota - ')):
            # Get all Info
            newD = d.copy()

            # Add the rest of what we know about this ID:
            for d_info in driver_files_active:
                if (d['id'] == d_info['id']):
                    #log.debug('d_info: {}'.format(d_info))
                    newD.update(d_info)
                    break
            # Modify it a little bit
            newD.update({'name': d['name'][10:], 
                        'file': d['file'].stem + d['file'].suffix,
                        'filestem': d['file'].stem})
            newD['filestem'] = newD['filestem'].replace('-expanded', '')
            newD['wikiname'] = newD['name'].replace(' ', '-').replace('/', '-')
            #log.debug('d_info 2: {}'.format(d_info))

            # TODO: Build the Zigbee packages
            t4he_pkg.addDriver(d['name'], newD['version'], newD['namespace'], 
                base_raw_repo_url + newD['file'], newD['required'], newD['id'], id=None)

            # We will modify these later, make sure we have COPIES
            if(newD['name'].startswith('Generic')):
                generic_drivers.append(newD.copy())
            else:
                specific_drivers.append(newD.copy())
            if(newD['name'].find("Parent") != -1):
                parent_drivers.append(newD.copy())
            else:
                child_drivers.append(newD.copy())
  
    # Make Driver Lists if we have all files we expect...
    if(len(used_driver_list) >= expected_num_drivers):
        log.info('Making the driver list file...')
        my_driver_list_1 = [
            {'name': '', 
             'format': 'These are the currently available drivers (updated: %(asctime)s):\n\n'},
            {'name': 'Generic Drivers',
             'format': '**%(name)s**\n',
             'items': generic_drivers,
             'items_format': [
                 "* [%(name)s](%(base_url)s%(file)s) (%(comment)s) - Import URL: [RAW](%(base_raw_url)s%(file)s)\n",
                 "* [%(name)s](%(base_url)s%(file)s) - Import URL: [RAW](%(base_raw_url)s%(file)s)\n",]},
            {'name': '\n', 'format': '%(name)s'},
            {'name': 'Specific Drivers',
             'format': '**%(name)s**\n',
             'items': specific_drivers,
             # Make sure the format requesting the most amount of data is first in the list
             'items_format': [
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s (%(comment)s) - Import URL: [RAW](%(base_raw_url)s%(file)s) - [Device Model Info](%(deviceLink)s)\n",
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s - Import URL: [RAW](%(base_raw_url)s%(file)s) - [Device Model Info](%(deviceLink)s)\n",
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s (%(comment)s) - Import URL: [RAW](%(base_raw_url)s%(file)s)\n",
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s - Import URL: [RAW](%(base_raw_url)s%(file)s)\n"]}]
        #cb.makeDriverListDoc(my_driver_list_1, output_file='DRIVERLIST_OLD', filter_function=cb.makeDriverListFilter,
        #    base_data={'base_url': base_repo_url, 'base_raw_url': base_raw_repo_url})
        full_header = '<tr><td><b>Device</b></td><td><b>Comment</b></td><td><b>Import&nbsp;URL</b></td><td><b>Model&nbsp;Info</b></td></tr>'
        my_driver_list_1b = [
            {'name': '', 
             'format': 'These are the currently available drivers (updated: %(asctime)s):\n\n'},
            {'name': '<table>\n', 'format': '%(name)s'},
            {'name': 'Generic&nbsp;Drivers',
             'format': '<tr><th><b>%(name)s</b></th><th></th><th></th><th></th></tr>' + full_header + '\n',
             'items': generic_drivers,
             'items_format': [
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</a></td><td>%(comment)s</td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td></td></tr>\n",
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</td><td></td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td></td></tr>\n",]},
            {'name': '\n', 'format': '%(name)s'},
            {'name': '</table><table>\n', 'format': '%(name)s'},
            {'name': 'Specific&nbsp;Drivers',
             'format': '<tr><th><b>%(name)s</b></th><th></th><th></th><th></th></tr>' + full_header + '\n',
             'items': specific_drivers,
             # Make sure the format requesting the most amount of data is first in the list
             'items_format': [
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</td><td>%(comment)s</td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td><a href=\"%(deviceLink)s\">Link</a></td></tr>\n",
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</td><td></td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td><a href=\"%(deviceLink)s\">Link</a></td></tr>\n",
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</td><td>%(comment)s</td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td></td></tr>\n",
                 "<tr><td><a href=\"%(base_url)s%(file)s\">%(name)s</td><td></td><td><a href=\"%(base_raw_url)s%(file)s\">RAW</a></td><td></td></tr>\n"]},
            {'name': '</table>\n', 'format': '%(name)s'},]
        #cb.makeDriverListDoc(my_driver_list_1b, filter_function=cb.makeDriverListFilter,
        #    base_data={'base_url': base_repo_url, 'base_raw_url': base_raw_repo_url})
        my_driver_list_2 = [
            {'name': 'Driver List', 'format': '# %(name)s \n'},
            {'name': '', 
             'format': 'These are the currently available drivers (updated: %(asctime)s):\n\n'},
            {'name': 'Parent Device Drivers',
             'format': '**%(name)s**\n',
             'items': parent_drivers,
             'items_format': [
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s (%(comment)s)\n",
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s\n",]},
            {'name': '\n', 'format': '%(name)s'},
            {'name': 'Child Device Drivers',
             'format': '**%(name)s**\n',
             'items': child_drivers,
             # Make sure the format requesting the most amount of data is first in the list
             'items_format': [
                 #"* [%(name)s](%(base_url)s%(file)s) (%(comment)s) - [Device Model Info](%(deviceLink)s)\n", 
                 #"* [%(name)s](%(base_url)s%(file)s) - [Device Model Info](%(deviceLink)s)\n", 
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s (%(comment)s)\n", 
                 "* [%(name)s](%(base_url)s%(file)s) %(version)s\n"]}]
        cb.makeDriverListDoc(my_driver_list_2, output_file='DRIVERLIST.md', filter_function=cb.makeDriverListFilter, 
            base_data={'base_url': base_repo_url, 'base_raw_url': base_raw_repo_url})
        #print(parent_drivers)
        my_driver_list_table = [
            {'name': '', 
             'format': 'These are the currently available drivers (updated: %(asctime)s):\n\n'},
            {'name': 'Tasmota Parent Device Drivers',
             'format': '**%(name)s**\n\n' + \
                       '| Name | URL | RAW URL | Version | Comment |\n' + \
                       '| --- | --- | --- | --- | --- |\n',
             'items': parent_drivers,
             'items_format': [
                 "| [%(name)s](https://github.com/markus-li/Hubitat/wiki/[Driver-List]-Tasmota-%(wikiname)s) | [URL](%(base_url)s%(file)s) | [RAW](%(base_raw_url)s%(file)s) | %(version)s | %(comment)s |\n",
                 "| [%(name)s](https://github.com/markus-li/Hubitat/wiki/[Driver-List]-Tasmota-%(wikiname)s) | [URL](%(base_url)s%(file)s) | [RAW](%(base_raw_url)s%(file)s) | %(version)s | |\n",]},
            {'name': '\n', 'format': '%(name)s'},
            {'name': 'Tasmota Child Device Drivers',
             'format': '**%(name)s**\n\n' + \
                       '| Name | URL | RAW URL | Version | Comment |\n' + \
                       '| --- | --- | --- | --- | --- |\n',
             'items': child_drivers,
             # Make sure the format requesting the most amount of data is first in the list
             'items_format': [
                 "| [%(name)s](https://github.com/markus-li/Hubitat/wiki/[Driver-List]-Tasmota-%(wikiname)s) | [URL](%(base_url)s%(file)s) | [RAW](%(base_raw_url)s%(file)s) | %(version)s | | %(comment)s |\n",
                 "| [%(name)s](https://github.com/markus-li/Hubitat/wiki/[Driver-List]-Tasmota-%(wikiname)s) | [URL](%(base_url)s%(file)s) | [RAW](%(base_raw_url)s%(file)s) | %(version)s | | |\n",]}]
        if(branch_name == 'release'):
            # ONLY re-build this when building in the Release branch
            cb.makeDriverListDoc(my_driver_list_table, output_file='../Hubitat.wiki/Driver-List/Driver-List.md', filter_function=cb.makeDriverListFilter, 
                base_data={'base_url': base_repo_url, 'base_raw_url': base_raw_repo_url})
            for d in parent_drivers + child_drivers:
                output_file = "../Hubitat.wiki/Driver-List/[Driver-List]-Tasmota-" + d['wikiname'] + '.md'
                #print(output_file)
                if(not path.exists(output_file)):
                    with open (output_file, "w") as wd:
                        wd.write('**Tasmota ' + d['name'] + '**')
                        wd.write('''
                    
***Commands***

***Capabilities***

***Comments***
''')
        if(update_2nd_hub_drivers):
            # Get the 2nd hub driver list id-assignments
            id_map = {}
            for d_2nd in driver_files_2nd:
                id_map[d_2nd['original_id']] = d_2nd['id']
            for d in generic_drivers + specific_drivers:
                if(d['id'] in id_map):
                    d['id_2nd'] = id_map[d['id']]
                else:
                    d['id_2nd'] = -1
            my_driver_list_3 = [
                {'name': 'driver_files_2nd = [', 'format': '%(name)s \n'},
                {'name': '', 'format': '  %(name)s\n'},
                {'name': 'DO NOT MODIFY ANYTHING EXCEPT "id", THIS FILE IS AUTOGENERATED!', 'format': '  # %(name)s\n'},
                {'name': '', 'format': '  %(name)s\n'},
                {'name': 'Tasmota - Generic Drivers',
                'format': '  # %(name)s\n',
                'items': generic_drivers,
                'items_format': [
                    "  {'id': %(id_2nd)d, 'original_id': %(id)d, 'name': '%(name)s'},\n",]},
                {'name': '\n', 'format': '%(name)s'},
                {'name': 'Tasmota - Specific Device Drivers',
                'format': '  # %(name)s\n',
                'items': specific_drivers,
                'items_format': [
                    "  {'id': %(id_2nd)d, 'original_id': %(id)d, 'name': '%(name)s'},\n",]},
                {'name': ']', 'format': '%(name)s'},]
            cb.makeDriverListDoc(my_driver_list_3, output_file='private/tools/config/driver_list_2nd_hub.py')
        else:
            log.warn("Can't update the 2nd hub drivers! Check other errors/warnings for details...")
    else:
        log.info("SKIPPING making of the driver list file since we don't have enough drivers in the list...")
    #print('Generic drivers: ' + str(generic_drivers))
    #print('Specific drivers: ' + str(specific_drivers))
    #pp.pprint(used_driver_list)
    

    
    app_files = [
        #{'id': 97, 'file': 'tasmota-connect.groovy' },
        # 163 is available for re-use
        #{'id': 163, 'file': 'tasmota-connect-test.groovy' },
        {'id': 289, 'id_2': 66, 'file': 'tasmota-device-manager.groovy', 'required': True, 'oauth': False },
    ]

    app_files_private = [
        {'id': 353, 'id_3': 67, 'file': 'custom-lighting.groovy', 'required': True, 'oauth': False },
    ]

    cb.setUsedDriverList(used_driver_list.copy())
    cb_2.setUsedDriverList(used_driver_list_2.copy())
    filtered_app_files = []
    filtered_app_files_2 = []
    for a in app_files:
        # Add this driver to the Package
        filtered_app_files.append(a.copy())
        filtered_app_files_2.append(a.copy())
        #if(a['id'] != 97):
        #    filtered_app_files.append(a)
        #if(a['id'] != 0 and len(used_driver_list) >= expected_num_drivers):
        #    filtered_app_files.append(a)
        #    log.info('Found ' + str(len(used_driver_list)) + ' driver(s)...')
        #    log.debug("Just found App ID " + str(id))
        #else:
        #    if(a['id'] == 0):
        #        log.info("Not making App updates since this app has no ID set yet! Skipped updating App with path: '" + str(a['file']) + "'")
        #    else:
        #        log.info("Not ready for App updates! Only " + str(len(used_driver_list)) + " driver(s) currently active! Skipped updating App ID " + str(a['id']))
    #print(filtered_app_files)
    if(branch_name != 'release'):
        used_app_list_private_3 = cb_private_3.expandGroovyFilesAndPush(app_files_private.copy(), code_type='app')
        used_app_list_2 = cb_2.expandGroovyFilesAndPush(filtered_app_files_2, code_type='app')
    
    # The main target needs to be last to keep everything in a consistent state
    used_app_list = cb.expandGroovyFilesAndPush(filtered_app_files.copy(), code_type='app')
    
    if(branch_name != 'release'):
        used_app_list_private = cb_private.expandGroovyFilesAndPush(app_files_private.copy(), code_type='app')

    # Add the Apps to the T4HE package
    for a in sorted(used_app_list.values(), key=lambda k: k['name']):
        a['file'] = a['file'].stem + a['file'].suffix
        t4he_pkg.addApp(a['name'], a['version'], a['namespace'], 
                app_raw_repo_url + a['file'], a['required'], a['oauth'], a['id'], id=None)

    #t4he_pkg.clearDrivers()
    pm.addPackage(t4he_pkg, "Integrations", 
        "https://raw.githubusercontent.com/markus-li/Hubitat/release/packageManifest.json",
        "Allows you to integrate Tasmota-based devices with Hubitat Elevation.",
        betaLocation="https://raw.githubusercontent.com/markus-li/Hubitat/development/packageManifestBeta.json")
    
    
    # Create the Repository file
    pm.buildRepository()
    #pm.printJSON()

    if(branch_name == 'release'):
        t4he_pkg.buildManifest(extraInput="packageManifestBeta.json")
    else:
        t4he_pkg.buildManifest(output="packageManifestBeta.json", extraInput="packageManifest.json")
        #t4he_pkg.printJSON()

    ###########################################################
    # Now let's copy all relevant files into the public repo: #
    ###########################################################

    repo_private_path = "."
    repo_public_path = "../Hubitat-Public"
    repo_internal_path = "../Hubitat-Internal"

    repo_private = Repository(repo_private_path)
    repo_public = Repository(repo_public_path)
    repo_internal = Repository(repo_internal_path)

    if(repo_public.head.shorthand != repo_private.head.shorthand):
        repo_public.checkout(repo_public.lookup_reference(repo_public.lookup_branch(repo_private.head.shorthand).name))

    if(repo_public.head.shorthand == repo_private.head.shorthand):
        log.debug("The Hubitat-Public Repo has branch \"" + repo_public.head.shorthand + "\" checked out.")
        
        # Public Drivers
        repo_tool.copy_files_by_wildcard(repo_private_path + "/drivers/expanded/javascript-injection-driver-expanded.groovy", repo_public_path + "/drivers/expanded")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/drivers/expanded/tasmota-*-expanded.groovy", repo_public_path + "/drivers/expanded")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/drivers/expanded/zigbee-*-expanded.groovy", repo_public_path + "/drivers/expanded")

        if(repo_private.head.shorthand == "development"):
            # Internal Drivers
            repo_tool.copy_files_by_wildcard(repo_private_path + "/drivers/expanded/zigbee-*-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-aqara-bulb-expanded.groovy", repo_internal_path + "/drivers")
            #repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-aqara-vibration-sensor-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-aqara-water-leak-sensor-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-xiaomi-aqara-contact-sensor-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-xiaomi-aqara-motion-sensor-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-xiaomi-aqara-opple-button-switch-remote-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-xiaomi-aqara-plug-outlet-expanded.groovy", repo_internal_path + "/drivers")
            repo_tool.copy_files_by_wildcard(repo_private_path + "/private/drivers/expanded/zigbee-xiaomi-aqara-temperature-humidity-expanded.groovy", repo_internal_path + "/drivers")

        # Public Apps
        repo_tool.copy_files_by_wildcard(repo_private_path + "/apps/expanded/tasmota-device-manager-expanded.groovy", repo_public_path + "/apps/expanded")
        
        # App Repo Manifests
        repo_tool.copy_files_by_wildcard(repo_private_path + "/packageManifest*.json", repo_public_path)
        repo_tool.copy_files_by_wildcard(repo_private_path + "/repository.json", repo_public_path)

        # README
        repo_tool.copy_files_by_wildcard(repo_private_path + "/README.md", repo_public_path)
        
        # Examples of how to use the Codebuilder toolset
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_codebuilder_tasmota.py", repo_public_path + "/tools/examples")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_codebuilder_tool.py", repo_public_path + "/tools/examples")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_driver_snippets_metadata.py", repo_public_path + "/tools/examples")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_driver_snippets_new_parser.py", repo_public_path + "/tools/examples")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_driver_snippets.py", repo_public_path + "/tools/examples")
        
        # An example of a helper-include file
        repo_tool.copy_files_by_wildcard(repo_private_path + "/helpers/helpers-childDevices.groovy", repo_public_path + "/tools/examples/helpers")

        # Main files for the Codebuilder toolset
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_codebuilder.py", repo_public_path + "/tools")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_hubspider.py", repo_public_path + "/tools")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/hubitat_packagemanagertool.py", repo_public_path + "/tools")
        repo_tool.copy_files_by_wildcard(repo_private_path + "/tools/repo_tool.py", repo_public_path + "/tools")

        log.debug("DONE updating the Hubitat-Public Repo branch \"" + repo_public.head.shorthand + "\"!")
    else:
        log.warning("The Hubitat-Public Repo couldn't switch branch to \"" + repo_private.head.shorthand + "\"! Aborting!")

    if(repo_tool.is_clean(repo_public)):
        log.info("The Hubitat-Public Repo is CLEAN!")
    else:
        log.warning("The Hubitat-Public Repo is DIRTY! Check what needs what needs to be commited...")
    
    if(repo_tool.is_clean(repo_internal)):
        log.info("The Hubitat-Internal Repo is CLEAN!")
    else:
        log.warning("The Hubitat-Internal Repo is DIRTY! Check what needs what needs to be commited...")
    

    ##################################
    ### Finally display the results: #
    ##################################

    #hhs.logout()
    if(len(cb.driver_new)>0):
        log.warning('These new drivers were created: \n{}'.format(cb.driver_new))
    else:
        log.info('No new drivers were created!')
    log.info('This many drivers were UPDATED: {}'.format(cb.driver_num_updated))
    
    if(len(cb_2.driver_new)>0):
        log.warning('These new drivers were created on 10.2: \n{}'.format(cb_2.driver_new))
    else:
        log.info('No new drivers were created on 10.2!')

    if(len(cb_3.driver_new)>0):
        log.warning('These new drivers were created on 10.3: \n{}'.format(cb_3.driver_new))
    else:
        log.info('No new drivers were created on 10.3!')

    log.info('This many drivers were UPDATED on 10.3: {}'.format(cb_3.driver_num_updated))
    if(len(cb_private.driver_new)>0):
        log.warning('These new PRIVATE drivers were created: \n{}'.format(cb_private.driver_new))
    else:
        log.info('No new PRIVATE drivers were created!')
    log.info('This many PRIVATE drivers were UPDATED: {}'.format(cb_private.driver_num_updated))

    if(len(cb_private_2.driver_new)>0):
        log.warning('These new PRIVATE drivers were created on 10.2: \n{}'.format(cb_private_2.driver_new))
    else:
        log.info('No new PRIVATE drivers were created on 10.2!')

    if(len(cb.app_new)>0):
        log.warn('These new apps were created: \n{}'.format(cb.app_new))
    else:
        log.info('No new apps were created!')
    log.info('This many apps were UPDATED: {}'.format(cb.app_num_updated))

    if(len(cb_private.app_new)>0):
        log.warn('These new PRIVATE apps were created: \n{}'.format(cb_private.app_new))
    else:
        log.info('No new PRIVATE apps were created!')
    log.info('This many PRIVATE apps were UPDATED: {}'.format(cb_private.app_num_updated))

    log.info('Current Default Version Number: {}'.format(getDriverVersion(driverVersionSpecial=cb.default_version)))
    log.info('Current Default PRIVATE Version Number: {}'.format(getDriverVersion(driverVersionSpecial=cb_private.default_version)))

    log.debug('This was built in the "'+branch_name+'" branch!')

    


    contents=errors.getvalue()
    if(len(contents) > 0):
        print('ERRORS and/or WARNINGS occured during this run:')
        print(contents)
    else:
        log.info('No ERRORS or WARNINGS occured during this run :)')
    errors.close()

    #cb.hubitat_hubspider.get_app_list()

    cb.saveChecksums()
    cb_2.saveChecksums()
    cb_3.saveChecksums()
    
    cb_private.saveChecksums()
    cb_private_2.saveChecksums()
    cb_private_3.saveChecksums()

    hhs.save_session()
    hhs_2.save_session()
    hhs_3.save_session()


    

if(Path('DEVELOPER').exists()):
    main()