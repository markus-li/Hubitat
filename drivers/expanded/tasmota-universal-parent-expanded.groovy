/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.4.0720Tb
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NOTE: This is an auto-generated file and most comments have been removed!
 *
 */

// BEGIN:getDefaultParentImports()
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
 
import java.security.MessageDigest
// END:  getDefaultParentImports()

metadata {
	definition (name: "Tasmota - Universal Parent", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-parent-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilities()
        capability "Refresh"
        capability "Configuration"
        // END:  getDefaultMetadataCapabilities()
        capability "PresenceSensor"
        capability "Initialize"
        
        // BEGIN:getDefaultParentMetadataAttributes()
        attribute   "ip", "string"
        attribute   "ipLink", "string"
        attribute   "module", "string"
        attribute   "templateData", "string"
        attribute   "wifiSignal", "string"
        // END:  getDefaultParentMetadataAttributes()
        // BEGIN:getDefaultMetadataAttributes()
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        // BEGIN:getMetadataAttributesForLastCheckin()
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "number"
        attribute "notPresentCounter", "number"
        attribute "restoredCounter", "number"
        // END:  getMetadataAttributesForLastCheckin()
        attribute "commandSent", "string"
        attribute "commandResult", "string"

        // BEGIN:getMetadataCommandsForHandlingChildDevices()
        command "deleteChildren"
        // END:  getMetadataCommandsForHandlingChildDevices()
        // BEGIN:getDefaultMetadataCommands()
        command "reboot"
        // END:  getDefaultMetadataCommands()
        // BEGIN:getCommandsForPresence()
        command "resetRestoredCounter"
        // END:  getCommandsForPresence()
        command "sendCommand", [[name:"Command*", type: "STRING", description: "Tasmota Command"],
            [name:"Argument", type: "STRING", description: "Argument (optional)"]]
        
        command "parseJSON", [[name:"JSON*", type: "STRING", description: "Tasmota Status as JSON"]]
	}

	preferences {
        // BEGIN:getDefaultParentMetadataPreferences()
        input(name: "runReset", description: styling_addDescriptionDiv("For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct."), title: styling_addTitleDiv("Settings"), displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultParentMetadataPreferences()
        // BEGIN:getMetadataPreferencesForLastCheckin()
        input(name: "lastCheckinEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Date"), description: styling_addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
        input(name: "lastCheckinEpochEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Epoch"), description: styling_addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)
        input(name: "presenceEnable", type: "bool", title: styling_addTitleDiv("Enable Presence"), description: styling_addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
        input(name: "presenceWarningEnable", type: "bool", title: styling_addTitleDiv("Enable Presence Warning"), description: styling_addDescriptionDiv("Enables Presence Warnings in the Logs (default: true)"), defaultValue: true)
        // END:  getMetadataPreferencesForLastCheckin()
        input(name: "deviceConfig", type: "enum", title: styling_addTitleDiv("Device Configuration"), 
            description: styling_addDescriptionDiv("Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting."), 
            options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", 
            displayDuringSetup: true, required: false)

        // BEGIN:getMetadataPreferencesForHiding()
        input(name: "hideExtended", type: "bool", title: styling_addTitleDiv("Hide Extended Settings"), description: styling_addDescriptionDiv("Hides extended settings, usually not needed."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideAdvanced", type: "bool", title: styling_addTitleDiv("Hide Advanced Settings"), description: styling_addDescriptionDiv("Hides advanced settings, usually not needed anyway."), defaultValue: true, displayDuringSetup: false, required: false)
        // END:  getMetadataPreferencesForHiding()

        // BEGIN:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        input("password", "password", title: styling_addTitleDiv("Device Password"), description: styling_addDescriptionDiv("REQUIRED if set on the Device! Otherwise leave empty."))
        input(name: "ipAddress", type: "string", title: styling_addTitleDiv("Device IP Address"), description: styling_addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
        input(name: "port", type: "number", title: styling_addTitleDiv("Device Port"), description: styling_addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
        input(name: "override", type: "bool", title: styling_addTitleDiv("Override IP"), description: styling_addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
        input(name: "useIPAsID", type: "bool", title: styling_addTitleDiv("IP as Network ID"), description: styling_addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
        input(name: "telePeriod", type: "string", title: styling_addTitleDiv("Update Frequency"), description: styling_addDescriptionDiv("Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)"), displayDuringSetup: true, required: false)
        input(name: "disableModuleSelection", type: "bool", title: styling_addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + styling_addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
        input(name: "moduleNumber", type: "number", title: styling_addTitleDiv("Module Number"), description: "ADVANCED: " + styling_addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
        input(name: "deviceTemplateInput", type: "string", title: styling_addTitleDiv("Device Template"), description: "ADVANCED: " + styling_addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the Device Configuration Default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\"NAME\":\"S120\",\"GPIO\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\"FLAG\":0,\"BASE\":18})"), displayDuringSetup: true, required: false)
        // END:  getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        input(name: "invertPowerNumber", type: "bool", title: styling_addTitleDiv("Send POWER1 events to POWER2, and vice versa"), description: styling_addDescriptionDiv("Use this if you have a dimmer AND a switch in the same device and on/off is not sent/received correctly. Normally this is NOT needed."), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "useAlternateColorCommand", type: "bool", title: styling_addTitleDiv("Use Alternate Color command in Tasmota"), description: styling_addDescriptionDiv("When enabled, this will use \"Var1\" instead of \"Color1\" in order to be able to catch the command in rules. Normally this is NOT needed."), defaultValue: false, displayDuringSetup: false, required: false)
        // BEGIN:getDefaultMetadataPreferencesLast()
        input(name: "hideDangerousCommands", type: "bool", title: styling_addTitleDiv("Hide Dangerous Commands"), description: styling_addDescriptionDiv("Hides Dangerous Commands, such as 'Delete Children'."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "disableCSS", type: "bool", title: styling_addTitleDiv("Disable CSS"), description: styling_addDescriptionDiv("CSS makes the driver more user friendly. Disable the use of CSS in the driver by enabling this. Does NOT affect HE resource usage either way."), defaultValue: false, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferencesLast()
        
	}

    // BEGIN:getMetadataCustomizationMethods()
    metaDataExporter()
    if(isCSSDisabled() == false) {
        preferences {
            input(name: "hiddenSetting", description: "" + getDriverCSSWrapper(), title: "None", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }
    // END:  getMetadataCustomizationMethods()
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
     
    Map deviceInfo = ['name': 'Tasmota - Universal Parent', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'vid': 'generic-switch', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-parent-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

// BEGIN:getHelperFunctions('device-configurations')
TreeMap getDeviceConfigurations() {
    List deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"],
         ],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home-WF500D.html'],

        [typeId: 'ce-la-2-w3-wall-outlet',
         name: 'CE Smart Home LA-2-W3 Wall Outlet',
         template: '{"NAME":"CE LA-2-W3","GPIO":[255,255,255,255,157,17,0,0,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-2-W3.html'],

        [typeId: 'ce-lq-2-w3-wall-outlet',
         name: 'CE Smart Home LQ-2-W3 Wall Outlet',
         template: '{"NAME":"CE LQ-2-W3","GPIO":[255,255,255,255,255,17,255,255,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LQ-2-W3.html'],

        [typeId: 'ce-la-wf7-pm-plug',
         name: 'CE Smart Home LA-WF7 Power Monitor Plug',
         template: '{"NAME":"CESmartHLA-WF7","GPIO":[0,56,0,17,134,132,0,0,131,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-WF7.html'],

        [typeId: 'awp02l-n-plug',
         name: 'AWP02L-N Plug',
         template: '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/hugoai_awp02l-n.html'],

        [typeId: 'cyyltf-bifans-j23-plug',
         name: 'CYYLTF BIFANS J23 Plug',
         template: '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/cyyltd_bifans_J23.html'],

        [typeId: 'gosund-wp3-plug',
         name: 'Gosund WP3 Plug',
         template: '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/gosund_wp3.html'],

        [typeId: 'sk03-pm-outdoor-plug',
         name: 'SK03 Power Monitor Outdoor Plug',
         template: '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/SK03_outdoor.html'],

        [typeId: 'aoycocr-x10s-pm-plug',
         name: 'Aoycocr X10S Power Monitor Plug',
         template: '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/aoycocr_X10S.html'],

        [typeId: 'brilliant-20699-rgbw-bulb',
         name: 'Brilliant 20699 800lm RGBW Bulb',
         template: '{"NAME":"Brilliant20699","GPIO":[0,0,0,0,141,140,0,0,37,142,0,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/brilliant_20699.html'],

        [typeId: 'sonoff-sv',
         name: 'Sonoff SV',
         template: '{"NAME":"Sonoff SV","GPIO":[17,255,0,255,255,255,0,0,21,56,255,0,0],"FLAG":1,"BASE":3}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_SV.html'],

        [typeId: 'sonoff-th',
         name: 'Sonoff TH',
         template: '{"NAME":"Sonoff TH","GPIO":[17,255,0,255,255,0,0,0,21,56,255,0,0],"FLAG":0,"BASE":4}',
         installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
         deviceLink: 'https://templates.blakadder.com/sonoff_TH.html'],

        [typeId: 'sonoff-pow',
         name: 'Sonoff POW',
         template: '{"NAME":"Sonoff Pow","GPIO":[17,0,0,0,0,130,0,0,21,132,133,52,0],"FLAG":0,"BASE":6}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_Pow.html'],

        [typeId: 'sonoff-s31',
         name: 'Sonoff S31',
         template: '{"NAME":"Sonoff S31","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":41}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_S31.html'],
        
        [typeId: 'sonoff-ifan02',
         name: 'Sonoff iFan02',
         module: 44,
         installCommands: [['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan02.html'],

        [typeId: 'sonoff-ifan03-no_beep-m71',
         name: 'Sonoff iFan03 (No Beep)',
         module: 71,
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m71',
         name: 'Sonoff iFan03 (Beep)',
         module: 71,
         installCommands: [["SetOption67", "1"], 
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'treatlife-ds01-dimmer',
         name: 'TreatLife DS01 Dimmer ',
         template: '{"NAME":"TL DS01 Dimmer","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
         installCommands: [["TuyaMCU", "21,2"], 
                           ["DimmerRange", "150,1000"]],
         deviceLink: 'https://templates.blakadder.com/treatlife_DS01.html'],
        
        [typeId: 'deta-6911ha-switch',
         name: 'Deta 6911HA Switch',
         template: '{"NAME":"Deta 1G Switch","GPIO":[0,0,0,0,157,0,0,0,0,21,0,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6911HA.html'],

        [typeId: 'deta-6912ha-switch',
         name: 'Deta 6912HA Switch',
         template: '{"NAME":"DETA 2G Switch","GPIO":[0,0,0,0,157,0,0,0,91,21,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [['Rule1', 'on system#boot do Backlog LedPower 1; LedState 0; LedPower 1; LedState 8; endon'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/deta_6912HA.html'],

        [typeId: 'deta-6903ha-switch',
         name: 'Deta 6903HA Switch',
         template: '{"NAME":"DETA 3G Switch","GPIO":[157,0,0,92,91,21,0,0,23,0,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6903HA.html'],

        [typeId: 'deta-6904ha-switch',
         name: 'Deta 6904HA Switch',
         template: '{"NAME":"Deta 4G Switch","GPIO":[157,0,0,19,18,21,0,0,23,20,22,24,17],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6904HA.html'],

        [typeId: 'deta-6922ha-outlet',
         name: 'Deta 6922HA Wall Outlet',
         template: '{"NAME":"DETA 2G GPO","GPIO":[0,0,0,17,157,0,0,0,91,21,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6922HA.html'],

        [typeId: 'lh-znb22-001-9w ',
         name: 'Lohas ZN033 9W 810lm RGBCCT Bulb ',
         template: '{"NAME":"Lohas RGBCW","GPIO":[0,0,0,0,38,37,0,0,41,39,40,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/lohas-ZN033-B22.html'],

        [typeId: 'kmc-4-pm-plug',
         name: 'KMC 4 Power Monitor Plug',
         template: '{"NAME":"KMC 4 Plug","GPIO":[0,56,0,0,133,132,0,0,130,22,23,21,17],"FLAG":0,"BASE":36}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

        [typeId: 'teckin-ss30-power-strip',
         name: 'Teckin SS30 Power Strip',
         template: '{"NAME":"Teckin SS30","GPIO":[52,255,255,57,29,17,0,0,31,30,32,255,25],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/teckin_ss30.html'],

        [typeId: 'teckin-sp10-plug',
         name: 'Teckin SP10 Plug',
         template: '{"NAME":"Teckin SP10","GPIO":[255,255,56,255,255,255,0,0,255,17,255,21,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/teckin_SP10.html'],

        [typeId: 'awp04l-pm-plug',
         name: 'AWP04L Power Monitor Plug',
         template: '{"NAME":"AWP04L","GPIO":[57,255,255,131,255,134,0,0,21,17,132,56,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/awp04l.html'],

        [typeId: 'dd001-mini-ir-v08-rgb-led-controller-no-ir',
         name: 'DD001-MINI(G)-IR-V08 RGB LED Controller (no IR)',
         template: '{"NAME":"DD001-NOIR-RGB","GPIO":[0,0,0,0,37,0,0,0,38,0,39,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/DD001-MINIG-IR-V08.html'],

        [typeId: 'sonoff-4ch-pro',
         name: 'Sonoff 4CH Pro',
         template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'unbranded-rgbwwcw-controller-type-1',
         name: 'Unbranded RGBWWCW Controller (Type 1)',
         template: '{"NAME":"CtrlRGBWWCW-T1","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: ''],
        
        [typeId: 'tuyamcu-touch-switch-1-button',
        name: 'TuyaMCU Touch Switch - 1 button',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,0"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-lucci-connect-remote-as-switches',
        name: 'TuyaMCU Lucci Connect Remote',
        template: '{"NAME":"Lucci Fan","GPIO":[0,0,0,0,0,0,0,0,0,108,0,107,0],"FLAG":0,"BASE":54}',
        installCommands: [["TuyaMCU", "11,102"], ["TuyaMCU", "12,1"], ["TuyaMCU", "13,0"], 
                          ["TuyaMCU", "14,0"], ["TuyaMCU", "15,0"], ["TuyaMCU", "21,50"], 
                          ["Rule1", "on TuyaReceived#Data=55AA00070005020400010012 do dimmer 1 endon on TuyaReceived#Data=55AA00070005020400010113 do dimmer 2 endon on TuyaReceived#Data=55AA00070005020400010214 do dimmer 3 endon on Dimmer#State=1 do TuyaSend4 2,0 endon on Dimmer#State=2 do TuyaSend4 2,1 endon on Dimmer#State=3 do TuyaSend4 2,2 endon"], 
                          ["Rule1", "1"]],
        deviceLink: 'https://templates.blakadder.com/luci-connect-remote-control.html'],

        [typeId: 'tuyamcu-touch-switch-2-button',
        name: 'TuyaMCU Touch Switch - 2 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-3-button',
        name: 'TuyaMCU Touch Switch - 3 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-4-button',
        name: 'TuyaMCU Touch Switch - 4 buttons',
        module: 54,
        template: '',
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,4"]],
        deviceLink: ''],

        [typeId: 'sonoff-powr2', 
        name: 'Sonoff POW R2',
        template: '{"NAME":"Sonoff Pow R2","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":43}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_Pow_R2.html'],

        [typeId: 'sonoff-s20', 
        name: 'Sonoff S20',
        template: '{"NAME":"Sonoff S20","GPIO":[17,255,255,255,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S20.html'],

        [typeId: 'sonoff-s26', 
        name: 'Sonoff S26',
        template: '{"NAME":"Sonoff S26","GPIO":[17,255,255,255,0,0,0,0,21,158,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S26.html'],

        [typeId: 'sonoff-mini', 
        name: 'Sonoff Mini',
        template: '{"NAME":"Sonoff Mini","GPIO":[17,0,0,0,9,0,0,0,21,56,0,0,255],"FLAG":0,"BASE":1}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_mini.html'],

        [typeId: 'sonoff-basic',
        name: 'Sonoff Basic',
        module: 1,
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_basic.html'],

        [typeId: 's120-plug' ,
        name: 'S120 USB Charger Plug',
        template: '{"NAME":"S120 Plug","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 's120-plug-bmp280' ,
        name: 'S120 USB Charger Plug + BMP280',
        template: '{"NAME":"S120THPPlug","GPIO":[0,6,0,5,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'globe-34207-bulb' ,
        name: 'Globe 34207 800lm RGBCCT Bulb',
        template: '{"NAME":"GlobeRGBWW","GPIO":[0,0,0,0,37,40,0,0,38,41,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/globe-34207.html'],

        [typeId: 'brilliantsmart-20676-plug' ,
        name: 'BrilliantSmart 20676 USB Charger Plug',
        template: '{"NAME":"Brilliant20676","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliantsmart-20741-bulb' ,
        name: 'BrilliantSmart 20741 9W 750lm RGBW Bulb',
        template: '{"NAME":"BS-20741-RGBW","GPIO":[0,0,0,0,37,40,0,0,38,0,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20741.html'],

        [typeId: 'brilliant-bl20925-pm-plug', 
        name: 'Brilliant Lighting BL20925 PM Plug',
        template: '{"NAME":"BL20925","GPIO":[0,0,0,17,133,132,0,0,131,158,21,0,0],"FLAG":0,"BASE":52}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliant_BL20925.html'],

        [typeId: 'deta-6930ha-plug', 
        name: 'Deta 6930HA Plug',
        template: '{"NAME":"Deta6930HAPlug","GPIO":[0,17,0,0,0,0,0,0,0,56,21,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/deta_6930HA.html'],

        [typeId: 'prime-ccrcwfii113pk-plug', 
        name: 'Prime CCRCWFII113PK Plug',
        template: '{"NAME":"PrimeCCRC13PK","GPIO":[0,0,0,0,57,56,0,0,21,122,0,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/prime_CCRCWFII113PK.html'],

        [typeId: 'ykyc-wj1y0-10a', 
        name: 'YKYC-WJ1Y0-10A PM Plug',
        template: '{"NAME":"YKYC-001PMPlug","GPIO":[0,17,0,57,133,132,0,0,130,56,21,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: ''],

        [typeId: 'merkury-mi-bw210-999w', 
        name: 'Merkury MI-BW210-999W',
        template: '{"NAME":"MI-BW210-999W","GPIO":[0,0,0,0,140,37,0,0,142,38,141,0,0],"FLAG":0,"BASE":48}',
        installCommands: [],
        deviceLink: ''],

        [typeId: 'lumary-rgbcct-led-strip', 
        name: 'Lumary RGBCCT LED Strip',
        template: '{"NAME":"Lumary LED","GPIO":[17,0,0,0,37,40,0,0,38,41,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/lumary_led_strip.html'],

        [typeId: 'xs-ssa06-plug', 
        name: 'XS-SSA06 Plug with RGB',
        template: '{"NAME":"XS-SSA06-RGB","GPIO":[37,0,38,0,0,39,0,0,0,90,0,21,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/XS-SSA06.html'],

        [typeId: 'tuyamcu-wifi-dimmer', 
        name: 'TuyaMCU Wifi Dimmer',
        module: 54,
        installCommands: [["SetOption66", "0"],
        ],
        deviceLink: ''],

        [typeId: 'zigbee-controller-default' ,
        name: 'Zigbee Controller (default pinout)',
        template: '{"NAME":"Zigbee","GPIO":[0,0,0,0,0,0,0,0,0,166,0,165,0],"FLAG":0,"BASE":18}',
        installCommands: [["SerialLog", "0"],
                          ],
        deviceLink: 'https://tasmota.github.io/docs/#/Zigbee'],

        [typeId: 'unbranded-rgb-controller-with-ir-type-1' ,
        name: 'Unbranded RGB Controller with IR (Type 1)',
        template: '{"NAME":"RGB Controller","GPIO":[0,0,0,0,0,38,0,0,39,51,0,37,0],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: 'sonoff-4ch',
        name: 'Sonoff 4CH',
        template: '{"NAME":"Sonoff 4CH","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":7}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_4CH.html'],

        [typeId: 'sonoff-4ch-pro-r2',
        name: 'Sonoff 4CH Pro (R2)',
        template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'nedis-ir-bridge',
        name: 'Nedis IR Bridge',
        template: '{"NAME":"Nedis IR Bridge","GPIO":[255,255,255,255,56,51,0,0,0,17,8,0,0],"FLAG":0,"BASE":62}',
        installCommands: [['SerialLog', '0']],
        deviceLink: 'https://templates.blakadder.com/nedis_WIFIRC10CBK.html'],

        [typeId: 'luminea-zx-2844-rgbw-led-controller',
        name: 'Luminea ZX-2844 RGBW LED Controller ',
        template: '{"NAME":"Luminea ZX-284","GPIO":[40,0,0,0,0,39,0,0,38,17,37,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/luminea_zx-2844.html'],

        [typeId: 'tuyamcu-znsn-wifi-curtain-wall-panel',
        comment: 'NOT GENERIC - read the instructions',
        name: 'TuyaMCU ZNSN Wifi Curtain Wall Panel',
        template: '{"NAME":"ZNSN Curtain","GPIO":[0,107,0,108,21,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"],
                        ['SetOption66', "1"],
                        ['SetOption80', "1"],
                        ["PulseTime1", "0"],
                        ["PulseTime2", "0"],
                        ["Interlock", "1,2"],
                        ["Interlock", "ON"],
                        ["ShutterMotorDelay", "4.5"],
                        ["ShutterOpenDuration", "10"],
                        ["ShutterCloseDuration", "11.2"],
                        ["Var1", "ShutterClose1"],
                        ["Var2", "ShutterStop1"],
                        ["Var3", "ShutterOpen1"],
                        ["setoption34", "50"],
                        ["Rule1", "ON Power1#state=1 DO Backlog var3 var3; var2 ShutterStop1; TuyaSend4 101,0 ENDON "],
                        ["Rule1", "+ ON Power1#state=0 DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Power2#state=0 DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Power2#state=1 DO Backlog var1 var1; var2 ShutterStop1; TuyaSend4 101,2 ENDON "],
                        ["Rule1", "+ ON ShutterStop#Data DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Shutter1#Position DO var4 %value% ENDON ON Event#Close0 DO Backlog var2 var2; TuyaSend4 101,1; ENDON "],
                        ["Rule1", "1"],
                        ["Rule2", "ON TuyaReceived#Data=55AA00070005650400010277 DO backlog var1 ShutterClose1; %var1%;  ENDON "],
                        ["Rule2", "+ ON System#Init DO Backlog setoption34 50; var1 ShutterClose1; var2 ShutterStop1; var3 ShutterOpen1; ENDON "],
                        ["Rule2", "+ ON TuyaReceived#Data=55AA00070005020400010214 DO backlog var2 ShutterStop1; %var2%; ENDON "],
                        ["Rule2", "+ ON TuyaReceived#Data=55AA00070005650400010176 DO backlog var3 ShutterOpen1; %var3%; ENDON "],
                        ["Rule2", "+ ON Event#Open100 DO Backlog var2 var2; TuyaSend4 101,1; ENDON "],
                        ["Rule2", "+ ON ShutterOpen#Data=100 DO Event Open%var4% ENDON ON ShutterClose#Data=0 DO Event Close%var4% ENDON "],
                        ["Rule2", "1"],
                        ],

        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        [typeId: 'mj-sd02-dimmer-switch',
        comment: 'WITHOUT power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02","GPIO":[19,18,0,33,34,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption3', '1'],
                        ['setoption1', '1'],
                        ['setoption32', '8'],
                        ['buttontopic', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'mj-sd02-dimmer-switch-led',
        comment: 'WITH power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02-LED","GPIO":[19,18,0,33,56,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption3', '1'],
                        ['setoption1', '1'],
                        ['setoption32', '8'],
                        ['buttontopic', '0'],
                        ['LedPower', '1'],
                        ['SetOption31', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'maxcio-diffuser-v1',
        comment: 'REQUIRES "Use Alternate Color command in Tasmota" to be set!',
        name: 'Maxcio Diffuser Wood Grain (v1)',
        template: '{"NAME":"MaxcioDiffuser","GPIO":[0,107,0,108,21,0,0,0,37,38,39,28,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption20', '1'],
                        ['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; rule2 0; power1 1; rule2 1; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO power1 1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog rule2 0; power2 0; rule2 1; power3 %var2%; var2 1; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog rule2 0; power2 1; rule2 1; var2 0; power3 0; ENDON'],
                        ['Rule2', 'ON Power1#State DO tuyasend1 5,%value% ENDON ON Power2#State=0 DO tuyasend1 1,0 ENDON ON Power2#State=1 DO backlog var2 1; tuyasend1 1,1; ENDON'],
                        ['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO power1 0 ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', '1'],
                        ['Rule3', '1']],
        deviceLink: 'https://templates.blakadder.com/maxcio_400ml_diffuser.html'],

        [typeId: 'sonoff-rf-bridge-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_RF_bridge.html'],
        
        [typeId: 'rflink-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'http://www.rflink.nl/blog2/wiring'],
        
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-rgb-rgbw-controller-bulb-dimmer', 
        comment: 'RGB+WW+CW should all work properly',
        name: 'Generic RGB/RGBW Controller/Bulb/Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: '01generic-thp-device' ,
        name: 'Generic Temperature/Humidity/Pressure Device',
        template: '',
        installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
        deviceLink: ''],

    ]

    TreeMap deviceConfigurationsMap = [:] as TreeMap
    deviceConfigurations.each{
        deviceConfigurationsMap[it["typeId"]] = it
    }
    return deviceConfigurationsMap
}

def getDeviceConfiguration(String typeId) {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    try{
        return deviceConfigurationsMap[typeId]
    } catch(e) {
        log.warn "Failed to retrieve Device Configuration '$typeId': $e"
        return null
    }
}

def getDeviceConfigurationsAsListOption() {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    def items = []
    deviceConfigurationsMap.sort({ a, b -> a.key <=> b.key }).each { k, v ->
        def label = v["name"]
        if(v.containsKey("comment") && v["comment"].length() > 0) {
            label += " (${v["comment"]})"
        }
        if(!(v.containsKey("notForUniversal") && v["notForUniversal"] == true)) {
            items << ["${v["typeId"]}":"$label"] 
        }
    }
    return items
}
// END:  getHelperFunctions('device-configurations')

/* These methods are unique to each driver */
def installed() {
	logging("installed()", 100)

    tasmota_installedPreConfigure()
    configurePresence()

}

def initialize() {
    logging("initialize()", 100)
    generalInitialize()
    configurePresence()
}

def updated() {
    logging("updated()", 100)
    setDisableCSS(disableCSS)
    configurePresence()
    unschedule("updatePresence")
    unschedule("tasmota_updatePresence")
    updateNeededSettings()
    refresh()
}

def configure() {
    generalInitialize()
    updateNeededSettings()
}

def getDriverCSS() {
    
    r = ""
    
    r += '''
    /*form[action*="preference"]::before {
        color: green;
        content: "Hi, this is my content"
    }
    form[action*="preference"] div[for^=preferences] {
        color: blue;
    }*/
    div#stateComment {
        display: inline;
    }
    /*div#stateComment:after {
        color: red;
        display: inline;
        visibility: visible;
        position: absolute;
        bottom: 150%;
        left: 400%;
        white-space: nowrap;
    }*/
    div#stateComment:after {
        color: #382e2b;
        visibility: visible;
        position: relative;
        white-space: nowrap;
        display: inline;
    }
    /*div#stateComment:after {
        color: #382e2b;
        display: inline;
        visibility: visible;
        position: fixed;
        left: 680px;
        white-space: nowrap;
        top: 95px;
    }*/
    /*
    div#stateComment:after {
        color: #5ea767;
        display: inline;
        visibility: visible;
        position: absolute;
        left: 120px;
        white-space: nowrap;
        bottom: -128px;
        height: 36px;
        vertical-align: middle;
    }*/
    div#stateCommentInside {
        display: none;
    }
    li[id*='stateCommentInside'] {
        /*visibility: hidden;*/
        /*position: absolute;*/
        display: list-item;
    }
    .property-value {
        overflow-wrap: break-word;
    }
    '''
    return r
}

def refresh() {
    def metaConfig = tasmota_refresh(metaConfig=null)

    metaConfig = setStateVariablesToHide(['mac'], metaConfig=metaConfig)
    logging("hideExtended=$hideExtended, hideAdvanced=$hideAdvanced", 1)
    if(hideExtended == null || hideExtended == true) {
        metaConfig = setPreferencesToHide(['hideAdvanced', 'ipAddress', 'override', 'useIPAsID', 'telePeriod', 'invertPowerNumber', 'useAlternateColorCommand'], metaConfig=metaConfig)
    }
    if(hideExtended == null || hideExtended == true || hideAdvanced == null || hideAdvanced == true) {
        metaConfig = setPreferencesToHide(['disableModuleSelection', 'port', 'disableCSS', 'moduleNumber'], metaConfig=metaConfig)
    }
    if(hideDangerousCommands == null || hideDangerousCommands == true) {
        metaConfig = setCommandsToHide(['deleteChildren', 'initialize'], metaConfig=metaConfig)
    } else {
        metaConfig = setCommandsToHide(['initialize'], metaConfig=metaConfig)
    }
    if(deviceConfig == null) deviceConfig = "01generic-device"
    deviceConfigMap = getDeviceConfiguration(deviceConfig)
    logging("deviceConfigMap=$deviceConfigMap", 1)
    try{
        if(deviceConfigMap.containsKey('comment') && 
           deviceConfigMap['comment'] != null &&
           deviceConfigMap['comment'].length() > 0) {
            logging("Settings state.comment...", 1)
            setStateCommentInCSS(deviceConfigMap['comment'], metaConfig=metaConfig) 
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
        } else {
            logging("Hiding state.comment...", 1)
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
            metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
        }
    } catch(e2) {
        log.warn e2
        metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
    }
    metaConfig = setDatasToHide(["preferences", "metaConfig"], metaConfig=metaConfig)
}

/* The parse(description) function is included and auto-expanded from external files */
void parse(String description) {
    // BEGIN:getGenericTasmotaNewParseHeader()
    Map descMap = tasmota_parseDescriptionAsMap(description)
    String body = null
    //logging("descMap: ${descMap}", 0)
    
    boolean missingChild = false
    
    if (state.mac != descMap["mac"]) {
        logging("Mac address of device found ${descMap["mac"]}", 10)
        state.mac = descMap["mac"]
    }
    
    sendlastCheckinEvent(minimumMinutesToRepeat=55)
    
    tasmota_prepareDNI()
    
    if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())
    
    if (body && body != "") {
        if(body.startsWith("{") || body.startsWith("[")) {
            boolean log99 = logging("========== Parsing Report ==========", 99)
            JsonSlurper slurper = new JsonSlurper()
            Map result = slurper.parseText(body)
    
            //logging("result: ${result}",0)
    // END:  getGenericTasmotaNewParseHeader()
        missingChild = parseResult(result, missingChild)
    // BEGIN:getGenericTasmotaNewParseFooter()
        result = null
    } else {
    
        }
    }
    
    if(missingChild == true) {
        log.warn "Missing a child device, run the Refresh command from the device page!"
    
    }
    if (device.currentValue("ip") == null) {
        String curIP = getDataValue("ip")
        logging("Setting IP from Data: $curIP", 1)
        sendEvent(name: 'ip', value: curIP, isStateChange: false)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$curIP\">$curIP</a>", isStateChange: false)
    }
    descMap = null
    body = null
    // END:  getGenericTasmotaNewParseFooter()
}

boolean parseResult(Map result) {
    boolean missingChild = false
    missingChild = parseResult(result, missingChild)
    return missingChild
}

void parseJSON(String jsonData) {
    boolean missingChild = false
    JsonSlurper jsonSlurper = new JsonSlurper()
    parseResult(jsonSlurper.parseText(jsonData), missingChild)
    jsonSlurper = null
}

boolean parseResult(Map result, boolean missingChild) {
    boolean log99 = logging("parseResult: $result", 99)
    logging("parseResult: $result", 100)
    // BEGIN:getTasmotaNewParserForStatusSTS()
    if (result.containsKey("StatusSTS")) {
        logging("StatusSTS: $result.StatusSTS",99)
        result << result.StatusSTS
    }
    // END:  getTasmotaNewParserForStatusSTS()
    // BEGIN:getTasmotaNewParserForParentSwitch()
    if (result.containsKey("POWER")  == true && result.containsKey("POWER1") == false) {
        logging("parser: POWER (child): $result.POWER",1)
    
        missingChild = callChildParseByTypeId("POWER1", [[name:"switch", value: result.POWER.toLowerCase()]], missingChild)
    } else {
    
        String currentPower = ""
        (1..16).each {i->
            currentPower = "POWER$i"
    
            if(result.containsKey(currentPower) == true) {
                if(i < 3 && invertPowerNumber == true) {
    
                    if(i == 1) {
                        currentPower = "POWER2"
                    } else {
                        currentPower = "POWER1"
                    }
                }
                logging("parser: $currentPower (original: POWER$i): ${result."POWER$i"}",1)
                missingChild = callChildParseByTypeId("$currentPower", [[name:"switch", value: result."POWER$i".toLowerCase()]], missingChild)
    
            }
        }
    }
    // END:  getTasmotaNewParserForParentSwitch()
    // BEGIN:getTasmotaNewParserForDimmableDevice()
    if(true) {
        com.hubitat.app.ChildDeviceWrapper childDevice = tasmota_getChildDeviceByActionType("POWER1")
        if(result.containsKey("Dimmer")) {
            def dimmer = result.Dimmer
            logging("Dimmer: ${dimmer}", 1)
            state.level = dimmer
            if(childDevice?.currentValue('level') != dimmer ) missingChild = callChildParseByTypeId("POWER1", [[name: "level", value: dimmer]], missingChild)
        }
    
        if(result.containsKey("TuyaReceived") && result.TuyaReceived.containsKey("Data")) {
    
            if(result.TuyaReceived.Data != "55AA000000010101") {
                missingChild = callChildParseByTypeId("POWER1", [[name: "tuyaData", value: result.TuyaReceived.Data]], missingChild)
            }
        }
    
        if(log99 == true && result.containsKey("Wakeup")) {
            logging("Wakeup: ${result.Wakeup}", 99)
    
        }
    }
    // END:  getTasmotaNewParserForDimmableDevice()
    // BEGIN:getTasmotaNewParserForRGBWDevice()
    if(true) {
    
        com.hubitat.app.ChildDeviceWrapper childDevice = tasmota_getChildDeviceByActionType("POWER1")
        String mode = "RGB"
        if (result.containsKey("Color")) {
            String color = result.Color
            logging("Color: ${color}, size: ${result.Color.tokenize(",").size()}", 1)
            if((color.length() > 6 && color.startsWith("000000")) ||
               (result.Color.tokenize(",").size() > 3 && color.startsWith("0,0,0"))) {
                mode = "CT"
            }
            state.colorMode = mode
            if(childDevice?.currentValue('colorMode') != mode ) missingChild = callChildParseByTypeId("POWER1", [[name: "colorMode", value: mode]], missingChild)
        }
        if (result.containsKey("Scheme")) {
            if(childDevice?.currentValue('effectNumber') != result.Scheme ) missingChild = callChildParseByTypeId("POWER1", [[name: "effectNumber", value: result.Scheme]], missingChild)
        }
        if (mode == "RGB" && result.containsKey("HSBColor")) {
            List hsbColor = result.HSBColor.tokenize(",")
            hsbColor[0] = Math.round((hsbColor[0] as Integer) / 3.6) as Integer
            hsbColor[1] = hsbColor[1] as Integer
    
            logging("hsbColor: ${hsbColor}", 1)
            if(childDevice?.currentValue('hue') != hsbColor[0] ) missingChild = callChildParseByTypeId("POWER1", [[name: "hue", value: hsbColor[0]]], missingChild)
            if(childDevice?.currentValue('saturation') != hsbColor[1] ) missingChild = callChildParseByTypeId("POWER1", [[name: "saturation", value: hsbColor[1]]], missingChild)
            String colorName = rgbw_getColorNameFromHueSaturation(hsbColor[0], hsbColor[1])
            if(childDevice?.currentValue('colorName') != colorName ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorName", value: colorName]], missingChild)
            }
        } else if (result.containsKey("CT")) {
            Integer t = Math.round(1000000/result.CT)
            if(childDevice?.currentValue('colorTemperature') != t ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorTemperature", value: t]], missingChild)
            }
            String colorName = rgbw_getColorNameFromTemperature(t)
            if(childDevice?.currentValue('colorName') != colorName ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorName", value: colorName]], missingChild)
            }
            logging("CT: $result.CT ($t)",99)
        }
    
    }
    // END:  getTasmotaNewParserForRGBWDevice()
    // BEGIN:getTasmotaNewParserForFanMode()
    if (result.containsKey("FanSpeed")) {
        String speed = "off"
        switch(result.FanSpeed) {
            case "1":
                speed = "low"
                break
            case "2":
                speed = "medium"
                break
            case "3":
                speed = "high"
                break
        }
        logging("parser: FanSpeed: $result.FanSpeed, speed = $speed", 1)
        missingChild = callChildParseByTypeId("FAN", [[name:"speed", value: speed]], missingChild)
    }
    // END:  getTasmotaNewParserForFanMode()
    // BEGIN:getTasmotaNewParserForBasicData()
    if (result.containsKey("StatusNET")) {
        logging("StatusNET: $result.StatusNET",99)
        result << result.StatusNET
    }
    if (result.containsKey("StatusFWR")) {
        logging("StatusFWR: $result.StatusFWR",99)
        result << result.StatusFWR
    }
    if (result.containsKey("StatusPRM")) {
        logging("StatusPRM: $result.StatusPRM",99)
        result << result.StatusPRM
    }
    if (false && result.containsKey("Status")) {
    
        logging("Status: $result.Status",99)
        result << result.Status
    }
    if (result.containsKey("LoadAvg")) {
        logging("LoadAvg: $result.LoadAvg",99)
    
    }
    if (log99 == true && result.containsKey("Sleep")) {
        logging("Sleep: $result.Sleep",99)
    }
    if (log99 == true && result.containsKey("SleepMode")) {
        logging("SleepMode: $result.SleepMode",99)
    }
    if (log99 == true && result.containsKey("Vcc")) {
        logging("Vcc: $result.Vcc",99)
    }
    if (log99 == true && result.containsKey("Hostname")) {
        logging("Hostname: $result.Hostname",99)
    }
    if (result.containsKey("IPAddress") && (override == false || override == null)) {
        logging("IPAddress: $result.IPAddress",99)
        sendEvent(name: "ip", value: "$result.IPAddress", isStateChange: false)
    
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>", isStateChange: false)
        updateDataValue("ip", "$result.IPAddress")
    }
    if (log99 == true && result.containsKey("WebServerMode")) {
        logging("WebServerMode: $result.WebServerMode",99)
    }
    if (result.containsKey("Version")) {
        logging("Version: $result.Version",99)
        updateDataValue("firmware", result.Version)
    }
    if (result.containsKey("Module") && !result.containsKey("Version")) {
    
        logging("Module: $result.Module",50)
        sendEvent(name: "module", value: "$result.Module", isStateChange: false)
    }
    
    if (result.containsKey("NAME") && result.containsKey("GPIO") && result.containsKey("FLAG") && result.containsKey("BASE")) {
        def n = result.toMapString()
        n = n.replaceAll(', ',',')
        n = n.replaceAll('\\[','{').replaceAll('\\]','}')
        n = n.replaceAll('NAME:', '"NAME":"').replaceAll(',GPIO:\\{', '","GPIO":\\[')
        n = n.replaceAll('\\},FLAG', '\\],"FLAG"').replaceAll('BASE', '"BASE"')
    
        logging("Template: $n",50)
        sendEvent(name: "templateData", value: "${n}", isStateChange: false)
    }
    if (log99 == true && result.containsKey("RestartReason")) {
        logging("RestartReason: $result.RestartReason",99)
    }
    if (result.containsKey("TuyaMCU")) {
        logging("TuyaMCU: $result.TuyaMCU",99)
        sendEvent(name: "tuyaMCU", value: "$result.TuyaMCU", isStateChange: false)
    }
    if (log99 == true && result.containsKey("SetOption81")) {
        logging("SetOption81: $result.SetOption81",99)
    }
    if (log99 == true && result.containsKey("SetOption113")) {
        logging("SetOption113 (Hubitat enabled): $result.SetOption113",99)
    }
    if (result.containsKey("Uptime")) {
        logging("Uptime: $result.Uptime",99)
    
        state.uptime = result.Uptime
        updateDataValue('uptime', result.Uptime)
    }
    // END:  getTasmotaNewParserForBasicData()
    // BEGIN:getTasmotaNewParserForEnergyMonitor()
    if (result.containsKey("StatusSNS")) {
        result << result.StatusSNS
    }
    if (result.containsKey("ENERGY")) {
    
        if (result.ENERGY.containsKey("Total")) {
            logging("Total: $result.ENERGY.Total kWh",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyTotal", value:"$result.ENERGY.Total kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Today")) {
            logging("Today: $result.ENERGY.Today kWh",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyToday", value:"$result.ENERGY.Today kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Yesterday")) {
            logging("Yesterday: $result.ENERGY.Yesterday kWh",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyYesterday", value:"$result.ENERGY.Yesterday kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Current")) {
            logging("Current: $result.ENERGY.Current A",99)
            def r = (result.ENERGY.Current == null) ? 0 : result.ENERGY.Current
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"current", value:"$r A"]], missingChild)
        }
        if (result.ENERGY.containsKey("ApparentPower")) {
            logging("apparentPower: $result.ENERGY.ApparentPower VA",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"apparentPower", value:"$result.ENERGY.ApparentPower VA"]], missingChild)
        }
        if (result.ENERGY.containsKey("ReactivePower")) {
            logging("reactivePower: $result.ENERGY.ReactivePower VAr",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"reactivePower", value:"$result.ENERGY.ReactivePower VAr"]], missingChild)
        }
        if (result.ENERGY.containsKey("Factor")) {
            logging("powerFactor: $result.ENERGY.Factor",99)
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"powerFactor", value:"$result.ENERGY.Factor"]], missingChild)
        }
        if (result.ENERGY.containsKey("Voltage")) {
            logging("Voltage: $result.ENERGY.Voltage V",99)
            def r = (result.ENERGY.Voltage == null) ? 0 : result.ENERGY.Voltage
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"voltageWithUnit", value:"$r V"]], missingChild)
            missingChild = callChildParseByTypeId("POWER1", [[name:"voltage", value: r, unit: "V"]], missingChild)
        }
        if (result.ENERGY.containsKey("Power")) {
            logging("Power: $result.ENERGY.Power W",99)
            def r = (result.ENERGY.Power == null) ? 0 : result.ENERGY.Power
    
            missingChild = callChildParseByTypeId("POWER1", [[name:"powerWithUnit", value:"$r W"]], missingChild)
            missingChild = callChildParseByTypeId("POWER1", [[name:"power", value: r, unit: "W"]], missingChild)
    
        }
    }
    // END:  getTasmotaNewParserForEnergyMonitor()
    // BEGIN:getTasmotaNewParserForSensors()
    for ( r in result ) {
    
        if((r.key == 'StatusSNS' || r.key == 'SENSOR') && r.value instanceof Map) {
            result << r
        }
    }
    for ( r in result ) {
        if(r.value instanceof Map && (r.value.containsKey("Temperature") ||
            r.value.containsKey("Humidity") || r.value.containsKey("Pressure") ||
            r.value.containsKey("Distance") || r.value.containsKey("Illuminance") ||
            r.value.containsKey("Gas") || r.value.containsKey("DewPoint"))) {
            if (r.value.containsKey("Humidity")) {
                logging("Humidity: RH $r.value.Humidity%", 99)
                missingChild = callChildParseByTypeId(r.key, [[name: "humidity", value: r.value.Humidity, unit: "%"]], missingChild)
            }
            if (r.value.containsKey("Temperature")) {
    
                logging("Temperature: $r.value.Temperature", 99)
                String c = String.valueOf((char)(Integer.parseInt("00B0", 16)));
                missingChild = callChildParseByTypeId(r.key, [[name: "temperature", value: r.value.Temperature, unit: "$c${location.temperatureScale}"]], missingChild)
            }
            if (r.value.containsKey("DewPoint")) {
    
                logging("DewPoint: $r.value.DewPoint", 99)
                String c = String.valueOf((char)(Integer.parseInt("00B0", 16)));
                missingChild = callChildParseByTypeId(r.key, [[name: "dewPoint", value: r.value.DewPoint, unit: "$c${location.temperatureScale}"]], missingChild)
            }
            if (r.value.containsKey("Pressure")) {
                logging("Pressure: $r.value.Pressure", 99)
                String pressureUnit = "mbar"
                missingChild = callChildParseByTypeId(r.key, [[name: "pressure", value: r.value.Pressure, unit: pressureUnit]], missingChild)
    
            }
            if (r.value.containsKey("Gas")) {
                logging("Pressure: $r.value.Gas", 99)
                String gasUnit = "ohm"
                missingChild = callChildParseByTypeId(r.key, [[name: "gas", value: r.value.Gas, unit: gasUnit]], missingChild)
            }
            if (r.value.containsKey("Distance")) {
                logging("Distance: $r.value.Distance cm", 99)
                def realDistance = Math.round((r.value.Distance as Double) * 100) / 100
    
                missingChild = callChildParseByTypeId(r.key, [[name: "distance", value: String.format("%.2f cm", realDistance), unit: "cm"]], missingChild)
            }
            if (r.value.containsKey("Illuminance")) {
                logging("Illuminance: $r.value.Illuminance lux", 99)
                def realIlluminance = Math.round(r.value.Illuminance as Double)
    
                missingChild = callChildParseByTypeId(r.key, [[name: "illuminance", value: realIlluminance, unit: "lux"]], missingChild)
            }
        }
    }
    // END:  getTasmotaNewParserForSensors()
    // BEGIN:getTasmotaNewParserForWifi()
    if (result.containsKey("Wifi")) {
        if (log99 == true && result.Wifi.containsKey("AP")) {
            logging("AP: $result.Wifi.AP",99)
        }
        if (log99 == true && result.Wifi.containsKey("BSSId")) {
            logging("BSSId: $result.Wifi.BSSId",99)
        }
        if (log99 == true && result.Wifi.containsKey("Channel")) {
            logging("Channel: $result.Wifi.Channel",99)
        }
        if (result.Wifi.containsKey("RSSI")) {
            logging("RSSI: $result.Wifi.RSSI",99)
    
            String quality = "${result.Wifi.RSSI}%"
            if(device.currentValue('wifiSignal') != quality) sendEvent(name: "wifiSignal", value: quality, isStateChange: false)
        }
        if (log99 == true && result.Wifi.containsKey("SSId")) {
            logging("SSId: $result.Wifi.SSId",99)
        }
    }
    // END:  getTasmotaNewParserForWifi()
    // BEGIN:getTasmotaNewParserForShutter()
    if (result.containsKey("Shutter1")) {
        logging("parser: Shutter1: $result.Shutter1", 1)
        missingChild = callChildParseByTypeId("SHUTTER", [[name:"shutter", value:result.Shutter1.clone()]], missingChild)
    }
    // END:  getTasmotaNewParserForShutter()
    tasmota_updatePresence("present")
    return missingChild
}

void updateNeededSettings() {
    // BEGIN:getUpdateNeededSettingsTasmotaHeader()
    Map currentProperties = state.currentProperties ?: [:]
    
    state.settings = settings
    
    String isUpdateNeeded = "NO"
    
    if(runReset != null && runReset == 'RESET') {
        for ( e in state.settings ) {
            logging("Deleting '${e.key}' with value = ${e.value} from Settings", 50)
    
            device.clearSetting("${e.key}")
            device.removeSetting("${e.key}")
            state?.settings?.remove("${e.key}")
        }
    }
    
    tasmota_prepareDNI()
    // END:  getUpdateNeededSettingsTasmotaHeader()

    if(deviceConfig == null) deviceConfig = "01generic-device"
    Map deviceConfigMap = getDeviceConfiguration(deviceConfig)
    
    String originalDeviceTemplateInput = deviceTemplateInput
    String deviceTemplateInput = deviceConfigMap?.template
    if(deviceTemplateInput == null) deviceTemplateInput = originalDeviceTemplateInput
    if(deviceTemplateInput == "") deviceTemplateInput = null

    String originalModuleNumber = moduleNumber
    String moduleNumber = deviceConfigMap?.module
    if(moduleNumber == null) moduleNumber = originalModuleNumber
    if(moduleNumber == "") moduleNumber = null

    if(deviceTemplateInput != null && moduleNumber == null) moduleNumber = 0

    logging("updateNeededSettings: deviceConfigMap=$deviceConfigMap, deviceTemplateInput=$deviceTemplateInput, moduleNumber=$moduleNumber", 1)
    
    // BEGIN:getUpdateNeededSettingsTasmotaDynamicModuleCommand()
    tasmota_getAction(tasmota_getCommandString("Module", null))
    tasmota_getAction(tasmota_getCommandString("Template", null))
    boolean disableModuleSelectionSetting = disableModuleSelection
    if(disableModuleSelectionSetting == null) disableModuleSelectionSetting = false
    
    Integer moduleNumberUsed = null
    if(moduleNumber == null || moduleNumber == '-1') {
        moduleNumberUsed = -1
    } else {
        moduleNumberUsed = moduleNumber.toInteger()
    }
    boolean useDefaultTemplate = false
    String defaultDeviceTemplate = ''
    if(deviceTemplateInput != null && deviceTemplateInput == "0") {
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput == null || deviceTemplateInput == "") {
    
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput != null) deviceTemplateInput = deviceTemplateInput.replaceAll(' ','')
    logging("disableModuleSelectionSetting=$disableModuleSelectionSetting, deviceTemplateInput=$deviceTemplateInput, moduleNumberUsed=$moduleNumberUsed, moduleNumber=$moduleNumber", 1)
    if(disableModuleSelectionSetting == false && ((deviceTemplateInput != null && deviceTemplateInput != "") ||
                                           (useDefaultTemplate && defaultDeviceTemplate != ""))) {
        def usedDeviceTemplate = defaultDeviceTemplate
        if(useDefaultTemplate == false && deviceTemplateInput != null && deviceTemplateInput != "") {
            usedDeviceTemplate = deviceTemplateInput
        }
        logging("Setting the Template (${usedDeviceTemplate}) soon...", 100)
        logging("templateData = ${device.currentValue('templateData')}", 10)
        if(usedDeviceTemplate != '') moduleNumberUsed = 0
    
        if(usedDeviceTemplate != null && device.currentValue('templateData') != usedDeviceTemplate) {
            logging("The template is currently NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'", 100)
    
            tasmota_getAction(tasmota_getCommandString("Template", usedDeviceTemplate))
        } else if (device.currentValue('module') == null){
    
            tasmota_getAction(tasmota_getCommandString("Template", null))
        }else if (usedDeviceTemplate != null) {
            logging("The template is set to '${usedDeviceTemplate}' already!", 100)
        }
    } else {
        logging("Can't set the Template...", 10)
        logging(device.currentValue('templateData'), 10)
    
    }
    if(disableModuleSelectionSetting == false && moduleNumberUsed != null && moduleNumberUsed >= 0) {
        logging("Setting the Module (${moduleNumberUsed}) soon...", 100)
        logging("device.currentValue('module'): '${device.currentValue('module')}'", 10)
    
        if(moduleNumberUsed != null && (device.currentValue('module') == null || !(device.currentValue('module').startsWith("[${moduleNumberUsed}:") || device.currentValue('module') == '0'))) {
            logging("Currently not using module ${moduleNumberUsed}, using ${device.currentValue('module')}", 100)
            tasmota_getAction(tasmota_getCommandString("Module", "${moduleNumberUsed}"))
        } else if (moduleNumberUsed != null && device.currentValue('module') != null){
            logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        } else if (device.currentValue('module') == null){
    
            tasmota_getAction(tasmota_getCommandString("Module", null))
        } else {
            logging("Module is set to '${device.currentValue('module')}', and it's set to be null, report this to the creator of this driver!",10)
        }
    } else {
        logging("Setting the Module has been disabled!", 10)
    }
    // END:  getUpdateNeededSettingsTasmotaDynamicModuleCommand()
    //logging("After getUpdateNeededSettingsTasmotaDynamicModuleCommand", 0)

    installCommands = deviceConfigMap?.installCommands
    if(installCommands == null || installCommands == '') installCommands = []
    //logging("Got to just before tasmota_runInstallCommands", 0)
    tasmota_runInstallCommands(installCommands)

    // BEGIN:getUpdateNeededSettingsTasmotaFooter()
    tasmota_getAction(tasmota_getCommandString("TelePeriod", "${tasmota_getTelePeriodValue()}"))
    
    tasmota_getAction(tasmota_getCommandString("SetOption113", "1"))
    
    tasmota_getAction(tasmota_getCommandString("Emulation", "2"))
    tasmota_getAction(tasmota_getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
    logging("HubitatPort: ${device.hub.getDataValue("localSrvPortTCP")}", 1)
    tasmota_getAction(tasmota_getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
    tasmota_getAction(tasmota_getCommandString("FriendlyName1", device.displayName.take(32)))
    
    tasmota_getAction(tasmota_getCommandString("SetOption34", "20"))
    
    int tzoffset = getLocation().timeZone.getOffset(now()) / 3600000
    String tzoffsetWithSign = tzoffset < 0 ? "${tzoffset}" : "+${tzoffset}"
    logging("Setting timezone to $tzoffsetWithSign", 10)
    tasmota_getAction(tasmota_getCommandString("Timezone", tzoffsetWithSign))
    
    logging("Scheduling tasmota_refreshChildren...", 1)
    runIn(30, "tasmota_refreshChildren")
    runIn(60, "tasmota_refreshChildrenAgain")
    logging("Done scheduling tasmota_refreshChildren...", 1)
    
    if(override == true) {
        tasmota_sync(ipAddress)
    }
    
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: false)
    // END:  getUpdateNeededSettingsTasmotaFooter()
}

/** Calls TO Child devices */
boolean callChildParseByTypeId(String deviceTypeId, List<Map> event, boolean missingChild) {
    event.each{
        if(it.containsKey("descriptionText") == false) {
            it["descriptionText"] = "'$it.name' set to '$it.value'"
        }
        it["isStateChange"] = false
    }
    com.hubitat.app.ChildDeviceWrapper cd = getChildDevice("$device.id-$deviceTypeId")
    if(cd == null && (deviceTypeId == "POWER1" || deviceTypeId == "POWER2")) {
        cd = getChildDevice("$device.id-SHUTTER")
    }
    if(cd != null) {
        cd.parse(event)
    } else {
        log.warn("callChildParseByTypeId() can't FIND the device type ${deviceTypeId}! (childId: ${"$device.id-$deviceTypeId"}) Did you delete something?")
        missingChild = true
    }
    return missingChild
}

void childParse(com.hubitat.app.DeviceWrapper cd, event) {
    try {
        getChildDevice(cd.deviceNetworkId).parse(event)
    } catch(e) {
        log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
    }
}

String getDeviceActionType(String childDeviceNetworkId) {
    return childDeviceNetworkId.tokenize("-")[1]
}

/** Calls FROM Child devices */
void componentRefresh(com.hubitat.app.DeviceWrapper cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentRefresh(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    refresh()
}

void componentOn(com.hubitat.app.DeviceWrapper cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    if(invertPowerNumber == true) {
        if(actionType == "POWER1") { 
            actionType = "POWER2"
        } else if(actionType == "POWER2"){
            actionType = "POWER1"
        }
    }
    logging("componentOn(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    tasmota_getAction(tasmota_getCommandString("$actionType", "1"))
}

void componentOff(com.hubitat.app.DeviceWrapper cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    if(invertPowerNumber == true) {
        if(actionType == "POWER1") { 
            actionType = "POWER2"
        } else if(actionType == "POWER2"){
            actionType = "POWER1"
        }
    }
    logging("componentOff(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    tasmota_getAction(tasmota_getCommandString("$actionType", "0"))
}

void componentSetLevel(com.hubitat.app.DeviceWrapper cd, BigDecimal level) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}) actionType=$actionType", 1)
    tasmota_rgbw_setLevel(level)
}

void componentSetLevel(com.hubitat.app.DeviceWrapper cd, BigDecimal level, BigDecimal duration) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}, duration=${duration}) actionType=$actionType", 1)
    tasmota_rgbw_setLevel(level, duration)
}

void componentStartLevelChange(com.hubitat.app.DeviceWrapper cd, String direction) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStartLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId}), direction=${direction}) actionType=$actionType", 1)
    tasmota_rgbw_startLevelChange(direction)
}

void componentStopLevelChange(com.hubitat.app.DeviceWrapper cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStopLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    tasmota_rgbw_stopLevelChange()
}

void componentSetColor(com.hubitat.app.DeviceWrapper cd, Map colormap) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), colormap=${colormap}) actionType=$actionType", 1)
    rgbw_setColor(colormap)
}

void componentSetHue(com.hubitat.app.DeviceWrapper cd, BigDecimal hue) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetHue(cd=${cd.displayName} (${cd.deviceNetworkId}), hue=${hue}) actionType=$actionType", 1)
    rgbw_setHue(hue)
}

void componentWhite(com.hubitat.app.DeviceWrapper cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentWhite(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    rgbw_white()
}

void componentSetRGB(com.hubitat.app.DeviceWrapper cd, r, g, b) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetRGB(cd=${cd.displayName} (${cd.deviceNetworkId}), r=${r}, g=${g}, b=${b}) actionType=$actionType", 1)
    tasmota_rgbw_setRGB(r, g, b)
}

void componentSetSaturation(com.hubitat.app.DeviceWrapper cd, BigDecimal saturation) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetSaturation(cd=${cd.displayName} (${cd.deviceNetworkId}), saturation=${saturation}) actionType=$actionType", 1)
    rgbw_setSaturation(saturation)
}

void componentSetColorTemperature(com.hubitat.app.DeviceWrapper cd, BigDecimal colortemperature) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColorTemperature(cd=${cd.displayName} (${cd.deviceNetworkId}), colortemperature=${colortemperature}) actionType=$actionType", 1)
    tasmota_rgbw_setColorTemperature(colortemperature)
}

void componentSetEffect(com.hubitat.app.DeviceWrapper cd, BigDecimal effectnumber, BigDecimal speed) {
    tasmota_rgbw_modeSet((Integer) effectnumber, speed)
}

void componentModeWakeUp(com.hubitat.app.DeviceWrapper cd, BigDecimal wakeUpDuration, BigDecimal level) {
    tasmota_rgbw_modeWakeUp(wakeUpDuration, level)
}

void componentSetSpeed(com.hubitat.app.DeviceWrapper cd, String fanspeed) {
    String fanCommand = "Dimmer"
    String cModule = device.currentValue('module')
    if(cModule != null && (cModule.startsWith('[44') == true || cModule.startsWith('[71') == true)) {
        fanCommand = "FanSpeed"
    }
    switch(fanspeed) {
        case "off":
            tasmota_getAction(tasmota_getCommandString(fanCommand, "0"))
            break
        case "on":
        case "low":
            tasmota_getAction(tasmota_getCommandString(fanCommand, "1"))
            break
        case "medium-low":
        case "medium":  
            tasmota_getAction(tasmota_getCommandString(fanCommand, "2"))
            break
        case "medium-high":
        case "high":
            tasmota_getAction(tasmota_getCommandString(fanCommand, "3"))
            break
    }  
}

void componentOpen(com.hubitat.app.DeviceWrapper cd) {
    tasmota_getAction(tasmota_getCommandString("ShutterOpen1", null))
}

void componentClose(com.hubitat.app.DeviceWrapper cd) {
    tasmota_getAction(tasmota_getCommandString("ShutterClose1", null))
}

void componentStop(com.hubitat.app.DeviceWrapper cd) {
    tasmota_getAction(tasmota_getCommandString("ShutterStop1", null))
}

void componentSetPosition(com.hubitat.app.DeviceWrapper cd, BigDecimal position) {
    position = position.setScale(0, BigDecimal.ROUND_HALF_UP)
    tasmota_getAction(tasmota_getCommandString("ShutterPosition", position.toString()))
}

void componentSetColorByRGBString(com.hubitat.app.DeviceWrapper cd, String colorRGB) {
    tasmota_rgbw_setColorByRGBString(colorRGB)
}

void componentSetPixelColor(com.hubitat.app.DeviceWrapper cd, String colorRGB, BigDecimal pixel) {
    tasmota_rgbw_setPixelColor(colorRGB, pixel)
}

void componentSetAddressablePixels(com.hubitat.app.DeviceWrapper cd, BigDecimal pixels) {
    tasmota_rgbw_setAddressablePixels(pixels)
}

void componentSetAddressableRotation(com.hubitat.app.DeviceWrapper cd, BigDecimal pixels) {
    tasmota_rgbw_setAddressableRotation(pixels)
}

void componentSetEffectWidth(com.hubitat.app.DeviceWrapper cd, BigDecimal pixels) {
    tasmota_rgbw_setEffectWidth(pixels)
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothings to edit here, move along! --------------------------------------
 * -----------------------------------------------------------------------------
 */

// BEGIN:getDefaultFunctions()
private String getDriverVersion() {
    comment = ""
    if(comment != "") state.comment = comment
    String version = "v1.0.4.0720Tb"
    logging("getDriverVersion() = ${version}", 100)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
// END:  getDefaultFunctions()

// BEGIN:getGetChildDriverNameMethod()
String getChildDriverName() {
    String deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    String childDriverName = "${deviceDriverName} (Child)"
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}
// END:  getGetChildDriverNameMethod()

// BEGIN:getLoggingFunction(specialDebugLevel=True)
private boolean logging(message, level) {
    boolean didLogging = false
     
    Integer logLevelLocal = 0
    if (infoLogging == null || infoLogging == true) {
        logLevelLocal = 100
    }
    if (debugLogging == true) {
        logLevelLocal = 1
    }
     
    if (logLevelLocal != 0){
        switch (logLevelLocal) {
        case 1:  
            if (level >= 1 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case 100:  
            if (level == 100 ) {
                log.info "$message"
                didLogging = true
            }
        break
        }
    }
    return didLogging
}
// END:  getLoggingFunction(specialDebugLevel=True)

// BEGIN:getHelperFunctions('all-default')
boolean isDriver() {
    try {
        getDeviceDataByName('_unimportant')
         
        return true
    } catch (MissingMethodException e) {
         
        return false
    }
}

void deviceCommand(String cmd) {
    def jsonSlurper = new JsonSlurper()
    def cmds = jsonSlurper.parseText(cmd)
     
    r = this."${cmds['cmd']}"(*cmds['args'])
     
    updateDataValue('appReturn', JsonOutput.toJson(r))
}

void setLogsOffTask(boolean noLogWarning=false) {
	if (debugLogging == true) {
        if(noLogWarning==false) {
            if(runReset != "DEBUG") {
                log.warn "Debug logging will be disabled in 30 minutes..."
            } else {
                log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
            }
        }
        runIn(1800, "logsOff")
    }
}

void logsOff() {
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        if(isDriver()) {
            device.clearSetting("logLevel")
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
            state?.settings?.remove("logLevel")
            device.clearSetting("debugLogging")
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            state?.settings?.remove("debugLogging")
            
        } else {
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
        }
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0" && logLevel != "100") runIn(1800, "logsOff")
    }
}

boolean isDeveloperHub() {
    return generateMD5(location.hub.zigbeeId as String) == "125fceabd0413141e34bb859cd15e067_disabled"
}

def getEnvironmentObject() {
    if(isDriver()) {
        return device
    } else {
        return app
    }
}

private def getFilteredDeviceDriverName() {
    def deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    return deviceDriverName
}

private def getFilteredDeviceDisplayName() {
    def deviceDisplayName = device.displayName.replace(' (parent)', '').replace(' (Parent)', '')
    return deviceDisplayName
}

BigDecimal round2(BigDecimal number, Integer scale) {
    Integer pow = 10;
    for (Integer i = 1; i < scale; i++)
        pow *= 10;
    BigDecimal tmp = number * pow;
    return ( (Float) ( (Integer) ((tmp - (Integer) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
}

String generateMD5(String s) {
    if(s != null) {
        return MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
    } else {
        return "null"
    }
}

Integer extractInt(String input) {
  return input.replaceAll("[^0-9]", "").toInteger()
}

String hexToASCII(String hexValue) {
    StringBuilder output = new StringBuilder("")
    for (int i = 0; i < hexValue.length(); i += 2) {
        String str = hexValue.substring(i, i + 2)
        output.append((char) Integer.parseInt(str, 16) + 30)
        logging("${Integer.parseInt(str, 16)}", 10)
    }
     
    return output.toString()
}
// END:  getHelperFunctions('all-default')

// BEGIN:getHelperFunctions('driver-metadata')
private Map getMetaConfig() {
    def metaConfig = getDataValue('metaConfig')
    if(metaConfig == null) {
        metaConfig = [:]
    } else {
        metaConfig = parseJson(metaConfig)
    }
    return metaConfig
}

boolean isCSSDisabled(Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    boolean disableCSS = false
    if(metaConfig.containsKey("disableCSS")) disableCSS = metaConfig["disableCSS"]
    return disableCSS
}

private void saveMetaConfig(Map metaConfig) {
    updateDataValue('metaConfig', JsonOutput.toJson(metaConfig))
}

private Map setSomethingToHide(String type, List something, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    def oldData = []
    something = something.unique()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = [type:something]
    } else {
        if(metaConfig["hide"].containsKey(type)) {
            metaConfig["hide"][type].addAll(something)
        } else {
            metaConfig["hide"][type] = something
        }
    }
    saveMetaConfig(metaConfig)
    logging("setSomethingToHide() = ${metaConfig}", 1)
    return metaConfig
}

private Map clearTypeToHide(String type, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = [(type):[]]
    } else {
        metaConfig["hide"][(type)] = []
    }
    saveMetaConfig(metaConfig)
    logging("clearTypeToHide() = ${metaConfig}", 1)
    return metaConfig
}

Map clearThingsToHide(Map metaConfig=null) {
    metaConfig = setSomethingToHide("other", [], metaConfig=metaConfig)
    metaConfig["hide"] = [:]
    saveMetaConfig(metaConfig)
    logging("clearThingsToHide() = ${metaConfig}", 1)
    return metaConfig
}

Map setDisableCSS(boolean value, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["disableCSS"] = value
    saveMetaConfig(metaConfig)
    logging("setDisableCSS(value = $value) = ${metaConfig}", 1)
    return metaConfig
}

Map setStateCommentInCSS(String stateComment, Map metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["stateComment"] = stateComment
    saveMetaConfig(metaConfig)
    logging("setStateCommentInCSS(stateComment = $stateComment) = ${metaConfig}", 1)
    return metaConfig
}

Map setCommandsToHide(List commands, Map metaConfig=null) {
    metaConfig = setSomethingToHide("command", commands, metaConfig=metaConfig)
    logging("setCommandsToHide(${commands})", 1)
    return metaConfig
}

Map clearCommandsToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("command", metaConfig=metaConfig)
    logging("clearCommandsToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setStateVariablesToHide(List stateVariables, Map metaConfig=null) {
    metaConfig = setSomethingToHide("stateVariable", stateVariables, metaConfig=metaConfig)
    logging("setStateVariablesToHide(${stateVariables})", 1)
    return metaConfig
}

Map clearStateVariablesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("stateVariable", metaConfig=metaConfig)
    logging("clearStateVariablesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setCurrentStatesToHide(List currentStates, Map metaConfig=null) {
    metaConfig = setSomethingToHide("currentState", currentStates, metaConfig=metaConfig)
    logging("setCurrentStatesToHide(${currentStates})", 1)
    return metaConfig
}

Map clearCurrentStatesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("currentState", metaConfig=metaConfig)
    logging("clearCurrentStatesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setDatasToHide(List datas, Map metaConfig=null) {
    metaConfig = setSomethingToHide("data", datas, metaConfig=metaConfig)
    logging("setDatasToHide(${datas})", 1)
    return metaConfig
}

Map clearDatasToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("data", metaConfig=metaConfig)
    logging("clearDatasToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

Map setPreferencesToHide(List preferences, Map metaConfig=null) {
    metaConfig = setSomethingToHide("preference", preferences, metaConfig=metaConfig)
    logging("setPreferencesToHide(${preferences})", 1)
    return metaConfig
}

Map clearPreferencesToHide(Map metaConfig=null) {
    metaConfig = clearTypeToHide("preference", metaConfig=metaConfig)
    logging("clearPreferencesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def metaDataExporter() {
    List filteredPrefs = getPreferences()['sections']['input'].name[0]
    if(filteredPrefs != []) updateDataValue('preferences', "${filteredPrefs}".replaceAll("\\s",""))
}

String getDriverCSSWrapper() {
    Map metaConfig = getMetaConfig()
    boolean disableCSS = isCSSDisabled(metaConfig=metaConfig)
    String defaultCSS = '''
    /* This is part of the CSS for replacing a Command Title */
    div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell p::after {
        visibility: visible;
        position: absolute;
        left: 50%;
        transform: translate(-50%, 0%);
        width: calc(100% - 20px);
        padding-left: 5px;
        padding-right: 5px;
        margin-top: 0px;
    }
    /* This is general CSS Styling for the Driver page */
    h3, h4, .property-label {
        font-weight: bold;
    }
    .preference-title {
        font-weight: bold;
    }
    .preference-description {
        font-style: italic;
    }
    '''
    String r = "<style>"
    
    if(disableCSS == false) {
        r += "$defaultCSS "
        try{
            r += " ${getCSSForHidingLastPreference()} "
            
            if(disableCSS == false) {
                if(metaConfig.containsKey("hide")) {
                    if(metaConfig["hide"].containsKey("command")) {
                        r += getCSSForCommandsToHide(metaConfig["hide"]["command"])
                    }
                    if(metaConfig["hide"].containsKey("stateVariable")) {
                        r += getCSSForStateVariablesToHide(metaConfig["hide"]["stateVariable"])
                    }
                    if(metaConfig["hide"].containsKey("currentState")) {
                        r += getCSSForCurrentStatesToHide(metaConfig["hide"]["currentState"])
                    }
                    if(metaConfig["hide"].containsKey("data")) {
                        r += getCSSForDatasToHide(metaConfig["hide"]["data"])
                    }
                    if(metaConfig["hide"].containsKey("preference")) {
                        r += getCSSForPreferencesToHide(metaConfig["hide"]["preference"])
                    }
                }
                if(metaConfig.containsKey("stateComment")) {
                    r += "div#stateComment:after { content: \"${metaConfig["stateComment"]}\" }"
                }
                r += " ${getDriverCSS()} "
            }
        }catch(MissingMethodException e) {
            if(!e.toString().contains("getDriverCSS()")) {
                log.warn "getDriverCSS() Error: $e"
            }
        } catch(e) {
            log.warn "getDriverCSS() Error: $e"
        }
    }
    r += " </style>"
    return r
}

Integer getCommandIndex(String cmd) {
    List commands = device.getSupportedCommands().unique()
    Integer i = commands.findIndexOf{ "$it" == cmd}+1
    return i
}

String getCSSForCommandHiding(String cmdToHide) {
    Integer i = getCommandIndex(cmdToHide)
    String r = ""
    if(i > 0) {
        r = "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i){display: none;}"
    }
    return r
}

String getCSSForCommandsToHide(List commands) {
    String r = ""
    commands.each {
        r += getCSSForCommandHiding(it)
    }
    return r
}

String getCSSToChangeCommandTitle(String cmd, String newTitle) {
    Integer i = getCommandIndex(cmd)
    String r = ""
    if(i > 0) {
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p {visibility: hidden;}"
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p::after {content: '$newTitle';}"
    }
    return r
}

Integer getStateVariableIndex(String stateVariable) {
    def stateVariables = state.keySet()
    Integer i = stateVariables.findIndexOf{ "$it" == stateVariable}+1
    return i
}

String getCSSForStateVariableHiding(String stateVariableToHide) {
    Integer i = getStateVariableIndex(stateVariableToHide)
    String r = ""
    if(i > 0) {
        r = "ul#statev li.property-value:nth-of-type($i){display: none;}"
    }
    return r
}

String getCSSForStateVariablesToHide(List stateVariables) {
    String r = ""
    stateVariables.each {
        r += getCSSForStateVariableHiding(it)
    }
    return r
}

String getCSSForCurrentStatesToHide(List currentStates) {
    String r = ""
    currentStates.each {
        r += "ul#cstate li#cstate-$it {display: none;}"
    }
    return r
}

Integer getDataIndex(String data) {
    def datas = device.getData().keySet()
    Integer i = datas.findIndexOf{ "$it" == data}+1
    return i
}

String getCSSForDataHiding(String dataToHide) {
    Integer i = getDataIndex(dataToHide)
    String r = ""
    if(i > 0) {
        r = "table.property-list tr li.property-value:nth-of-type($i) {display: none;}"
    }
    return r
}

String  getCSSForDatasToHide(List datas) {
    String r = ""
    datas.each {
        r += getCSSForDataHiding(it)
    }
    return r
}

Integer getPreferenceIndex(String preference, boolean returnMax=false) {
    def filteredPrefs = getPreferences()['sections']['input'].name[0]
    if(filteredPrefs == [] || filteredPrefs == null) {
        d = getDataValue('preferences')
        if(d != null && d.length() > 2) {
            try{
                filteredPrefs = d[1..d.length()-2].tokenize(',')
            } catch(e) {
            }
        }
        
    }
    Integer i = 0
    if(returnMax == true) {
        i = filteredPrefs.size()
    } else {
        i = filteredPrefs.findIndexOf{ "$it" == preference}+1
    }
    return i
}

String getCSSForPreferenceHiding(String preferenceToHide, Integer overrideIndex=0) {
    Integer i = 0
    if(overrideIndex == 0) {
        i = getPreferenceIndex(preferenceToHide)
    } else {
        i = overrideIndex
    }
    String r = ""
    if(i > 0) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-of-type($i) {display: none;} "
    }else if(i == -1) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-last-child(2) {display: none;} "
    }
    return r
}

String getCSSForPreferencesToHide(List preferences) {
    String r = ""
    preferences.each {
        r += getCSSForPreferenceHiding(it)
    }
    return r
}

String getCSSForHidingLastPreference() {
    return getCSSForPreferenceHiding(null, overrideIndex=-1)
}
// END:  getHelperFunctions('driver-metadata')

// BEGIN:getHelperFunctions('styling')
String styling_addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

String styling_addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

String styling_makeTextBold(s) {
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

String styling_makeTextItalic(s) {
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

String styling_getDefaultCSS(boolean includeTags=true) {
    String defaultCSS = '''
    /* This is part of the CSS for replacing a Command Title */
    div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell p::after {
        visibility: visible;
        position: absolute;
        left: 50%;
        transform: translate(-50%, 0%);
        width: calc(100% - 20px);
        padding-left: 5px;
        padding-right: 5px;
        margin-top: 0px;
    }
    /* This is general CSS Styling for the Driver page */
    h3, h4, .property-label {
        font-weight: bold;
    }
    .preference-title {
        font-weight: bold;
    }
    .preference-description {
        font-style: italic;
    }
    '''
    if(includeTags == true) {
        return "<style>$defaultCSS </style>"
    } else {
        return defaultCSS
    }
}
// END:  getHelperFunctions('styling')

// BEGIN:getHelperFunctions('driver-default')
String getDEGREE() { return String.valueOf((char)(176)) }

void refresh(String cmd) {
    deviceCommand(cmd)
}
def installedDefault() {
	logging("installedDefault()", 100)
    
    try {
        tasmota_installedPreConfigure()
    } catch (MissingMethodException e) {
    }
    try {
        installedAdditional()
    } catch (MissingMethodException e) {
    }
}

def configureDefault() {
    logging("configureDefault()", 100)
    try {
        return configureAdditional()
    } catch (MissingMethodException e) {
    }
    try {
        getDriverVersion()
    } catch (MissingMethodException e) {
    }
}

void configureDelayed() {
    runIn(10, "configure")
    runIn(30, "refresh")
}

void configurePresence() {
    prepareCounters()
    if(presenceEnable == null || presenceEnable == true) {
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 1/3 * * ? *", 'checkPresence')
        checkPresence(false)
    } else {
        sendEvent(name: "presence", value: "not present", descriptionText: "Presence Checking Disabled" )
        unschedule('checkPresence')
    }
}

void stopSchedules() {
    unschedule()
    log.info("Stopped ALL Device Schedules!")
}

void prepareCounters() {
    if(device.currentValue('restoredCounter') == null) sendEvent(name: "restoredCounter", value: 0, descriptionText: "Initialized to 0" )
    if(device.currentValue('notPresentCounter') == null) sendEvent(name: "notPresentCounter", value: 0, descriptionText: "Initialized to 0" )
    if(device.currentValue('presence') == null) sendEvent(name: "presence", value: "unknown", descriptionText: "Initialized as Unknown" )
}

boolean isValidDate(String dateFormat, String dateString) {
    try {
        Date.parse(dateFormat, dateString)
    } catch (e) {
        return false
    }
    return true
}

Integer retrieveMinimumMinutesToRepeat(Integer minimumMinutesToRepeat=55) {
    Integer mmr = null
    if(state.forcedMinutes == null || state.forcedMinutes == 0) {
        mmr = minimumMinutesToRepeat
    } else {
        mmr = state.forcedMinutes - 1 < 1 ? 1 : state.forcedMinutes.intValue() - 1
    }
    return mmr
}

boolean sendlastCheckinEvent(Integer minimumMinutesToRepeat=55) {
    boolean r = false
    Integer mmr = retrieveMinimumMinutesToRepeat(minimumMinutesToRepeat=minimumMinutesToRepeat)
    if (lastCheckinEnable == true || lastCheckinEnable == null) {
        String lastCheckinVal = device.currentValue('lastCheckin')
        if(lastCheckinVal == null || isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == false || now() >= Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime() + (mmr * 60 * 1000)) {
            r = true
		    sendEvent(name: "lastCheckin", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
            logging("Updated lastCheckin", 1)
        } else {
             
        }
	}
    if (lastCheckinEpochEnable == true) {
		if(device.currentValue('lastCheckinEpoch') == null || now() >= device.currentValue('lastCheckinEpoch').toLong() + (mmr * 60 * 1000)) {
            r = true
		    sendEvent(name: "lastCheckinEpoch", value: now())
            logging("Updated lastCheckinEpoch", 1)
        } else {
             
        }
	}
    if(r == true) setAsPresent()
    return r
}

Long secondsSinceLastCheckinEvent() {
    Long r = null
    if (lastCheckinEnable == true || lastCheckinEnable == null) {
        String lastCheckinVal = device.currentValue('lastCheckin')
        if(lastCheckinVal == null || isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == false) {
            logging("No VALID lastCheckin event available! This should be resolved by itself within 1 or 2 hours and is perfectly NORMAL as long as the same device don't get this multiple times per day...", 100)
            r = -1
        } else {
            r = (now() - Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime()) / 1000
        }
	}
    if (lastCheckinEpochEnable == true) {
		if(device.currentValue('lastCheckinEpoch') == null) {
		    logging("No VALID lastCheckin event available! This should be resolved by itself within 1 or 2 hours and is perfectly NORMAL as long as the same device don't get this multiple times per day...", 100)
            r = r == null ? -1 : r
        } else {
            r = (now() - device.currentValue('lastCheckinEpoch').toLong()) / 1000
        }
	}
    return r
}

boolean hasCorrectCheckinEvents(Integer maximumMinutesBetweenEvents=90, boolean displayWarnings=true) {
    Long secondsSinceLastCheckin = secondsSinceLastCheckinEvent()
    if(secondsSinceLastCheckin != null && secondsSinceLastCheckin > maximumMinutesBetweenEvents * 60) {
        if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("One or several EXPECTED checkin events have been missed! Something MIGHT be wrong with the mesh for this device. Minutes since last checkin: ${Math.round(secondsSinceLastCheckin / 60)} (maximum expected $maximumMinutesBetweenEvents)")
        return false
    }
    return true
}

boolean checkPresence(boolean displayWarnings=true) {
    boolean isPresent = false
    Long lastCheckinTime = null
    String lastCheckinVal = device.currentValue('lastCheckin')
    if ((lastCheckinEnable == true || lastCheckinEnable == null) && isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == true) {
        lastCheckinTime = Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime()
    } else if (lastCheckinEpochEnable == true && device.currentValue('lastCheckinEpoch') != null) {
        lastCheckinTime = device.currentValue('lastCheckinEpoch').toLong()
    }
    if(lastCheckinTime != null && lastCheckinTime >= now() - (3 * 60 * 60 * 1000)) {
        setAsPresent()
        isPresent = true
    } else {
        sendEvent(name: "presence", value: "not present")
        if(displayWarnings == true) {
            Integer numNotPresent = device.currentValue('notPresentCounter')
            numNotPresent = numNotPresent == null ? 1 : numNotPresent + 1
            sendEvent(name: "notPresentCounter", value: numNotPresent )
            if(presenceWarningEnable == null || presenceWarningEnable == true) {
                log.warn("No event seen from the device for over 3 hours! Something is not right... (consecutive events: $numNotPresent)")
            }
        }
    }
    return isPresent
}

void setAsPresent() {
    if(device.currentValue('presence') == "not present") {
        Integer numRestored = device.currentValue('restoredCounter')
        numRestored = numRestored == null ? 1 : numRestored + 1
        sendEvent(name: "restoredCounter", value: numRestored )
        sendEvent(name: "notPresentCounter", value: 0 )
    }
    sendEvent(name: "presence", value: "present")
}

void resetNotPresentCounter() {
    logging("resetNotPresentCounter()", 100)
    sendEvent(name: "notPresentCounter", value: 0, descriptionText: "Reset notPresentCounter to 0" )
}

void resetRestoredCounter() {
    logging("resetRestoredCounter()", 100)
    sendEvent(name: "restoredCounter", value: 0, descriptionText: "Reset restoredCounter to 0" )
}
// END:  getHelperFunctions('driver-default')

// BEGIN:getHelperFunctions('childDevices')
Integer channelNumber(String dni) {
    Integer ch = dni.split("-")[-1] as Integer
    return ch
}

void childOn(String dni) {
    onOffCmd(1, channelNumber(dni))
}

void childOff(String dni) {
    onOffCmd(0, channelNumber(dni))
}

void childSendState(String currentSwitchNumber, String state) {
    com.hubitat.app.ChildDeviceWrapper childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${currentSwitchNumber}")}
    if (childDevice) {
        logging("childDevice.sendEvent ${currentSwitchNumber} ${state}",1)
        childDevice.sendEvent(name: "switch", value: state, type: type)
    } else {
        logging("childDevice.sendEvent ${currentSwitchNumber} is missing!",1)
    }
}

boolean areAllChildrenSwitchedOn(Integer skip = 0) {
    List<com.hubitat.app.ChildDeviceWrapper> children = getChildDevices()
    boolean status = true
    Integer i = 1
    children.each {child->
        if (i!=skip) {
  		    if(child.currentState("switch")?.value == "off") {
                status = false
            }
        }
        i++
    }
    return status
}

void sendParseEventToChildren(data) {
    List<com.hubitat.app.ChildDeviceWrapper> children = getChildDevices()
    children.each {child->
        child.parseParentData(data)
    }
}

void createChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("createChildDevices: creating $numSwitchesI device(s)",1)
    
    for (i in 1..numSwitchesI) {
         
        try {
        addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                    log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                }
         
    }
}

void recreateChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("recreateChildDevices: recreating $numSwitchesI device(s)",1)
    com.hubitat.app.ChildDeviceWrapper childDevice = null

    for (i in 1..numSwitchesI) {
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
        if (childDevice) {
            childDevice.setName("${getDeviceInfoByName('name')} #$i")
            childDevice.setDeviceNetworkId("$device.id-$i")
            logging(childDevice.getData(), 10)
        } else {
             
            try {
            addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                    } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                        log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                    }
             
        }
    }
    if (numSwitchesI < 4) {
        for (i in 1..4) {
            if (i > numSwitchesI) {
                childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
                if (childDevice) {
                    logging("Removing child #$i!", 10)
                    deleteChildDevice(childDevice.deviceNetworkId)
                }
            }
        }
    }
}

void deleteChildren() {
	logging("deleteChildren()", 100)
	List<com.hubitat.app.ChildDeviceWrapper> children = getChildDevices()
    
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }
}
// END:  getHelperFunctions('childDevices')

// BEGIN:getHelperFunctions('tasmota')
def generalInitialize() {
    logging("generalInitialize()", 100)
	unschedule()
    setLogsOffTask()
    refresh()
}

void parse(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("tasmota: parse(asyncResponse.getJson() = \"${asyncResponse.getJson()}\")", 1)
            parseResult(asyncResponse.getJson())
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("tasmota: parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

void reboot() {
	logging("tasmota: reboot()", 1)
    tasmota_getAction(tasmota_getCommandString("Restart", "1"))
}

void sendCommand(String command, callback="tasmota_sendCommandParse") {
    logging("tasmota: sendCommand(command=$command)", 1)
    sendCommand(command, null)
}

void sendCommand(String command, String argument, callback="tasmota_sendCommandParse") {
    String descriptionText = "${command}${argument != null ? " " + argument : ""}"
    logging("tasmota: sendCommand($descriptionText)", 1)
    if(callback == "tasmota_sendCommandParse") {
        sendEvent(name: "commandSent", value: command, descriptionText: descriptionText, isStateChange: true)
    }
    tasmota_getAction(tasmota_getCommandString(command, argument), callback=callback)
}

void updatePresence(String presence) {
}

void tasmota_installedPreConfigure() {
    logging("tasmota_installedPreConfigure()", 1)
    logging("Password: ${decrypt(getDataValue('password'))}", 1)
    String pass = decrypt(getDataValue('password'))
    if(pass != null && pass != "" && pass != "[installed]") {
        device.updateSetting("password", [value: pass, type: "password"])
    }
    device.updateSetting("deviceConfig", [type: "enum", value:getDataValue('deviceConfig')])
}

void tasmota_updatedDefault() {
    log.warn("tasmota_updatedDefault() should NOT be used!")
    logging("tasmota_updatedDefault()", 10)
    updateNeededSettings()
    
    try {
    } catch (MissingMethodException e) {
    }
}

void tasmota_refreshChildren() {
    logging("tasmota_refreshChildren()", 1)
    tasmota_getAction(tasmota_getCommandString("Status", "0"), callback="tasmota_parseConfigureChildDevices")
}

void tasmota_refreshChildrenAgain() {
    logging("tasmota_refreshChildrenAgain()", 1)
    tasmota_refreshChildren()
}

Map tasmota_refresh(Map metaConfig=null) {
	logging("tasmota_refresh(metaConfig=$metaConfig)", 100)
    state.clear()

    tasmota_getAction(tasmota_getCommandString("Status", "0"), callback="tasmota_parseConfigureChildDevices")
    getDriverVersion()

    updateDataValue('namespace', getDeviceInfoByName('namespace'))

    metaConfig = clearThingsToHide()
    metaConfig = setCommandsToHide([], metaConfig=metaConfig)
    metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
        'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
    
    metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
    metaConfig = setDatasToHide(['namespace', 'appReturn', 'password'], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    try {
    } catch (MissingMethodException e1) {
        try {
        } catch (MissingMethodException e2) {
        }
    }
    return metaConfig
}

void tasmota_runInstallCommands(List installCommands) {
    logging("tasmota_runInstallCommands(installCommands=$installCommands)", 1)
    List backlogs = []
    List rule1 = []
    List rule2 = []
    List rule3 = []
    installCommands.each {cmd->
        if(cmd[0].toLowerCase() == "rule1") {
            rule1.add([command: cmd[0], value:cmd[1]])
        } else if(cmd[0].toLowerCase() == "rule2") {
            rule2.add([command: cmd[0], value:cmd[1]])
        } else if(cmd[0].toLowerCase() == "rule3") {
            rule3.add([command: cmd[0], value:cmd[1]])
        } else {
            backlogs.add([command: cmd[0], value:cmd[1]])
        }
    }

    pauseExecution(100)
    while(backlogs.size() > 0) {
        tasmota_getAction(tasmota_getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        if(backlogs.size() > 0) pauseExecution(1000)
    }

    [rule1, rule2, rule3].each {
        it.each {rule->
            tasmota_getAction(tasmota_getCommandString(rule["command"], rule["value"]))
            pauseExecution(100)
        }
    }
}

void tasmota_updatePresence(String presence) {
    logging("tasmota_updatePresence(presence=$presence) DISABLED", 1)
     
}

Map tasmota_parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) { 
            map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        } else {
            map += [(nameAndValue[0].trim()):""]
        }
	}
}

void tasmota_getAction(String uri, String callback="parse") { 
     
    tasmota_httpGetAction(uri, callback=callback)
}

void tasmota_parseConfigureChildDevices(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("tasmota_parseConfigureChildDevices(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            tasmota_configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("tasmota_parseConfigureChildDevices(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("tasmota_parseConfigureChildDevices(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

void tasmota_configureChildDevices(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    Map statusMap = asyncResponse.getJson()
    logging("tasmota_configureChildDevices() statusMap=$statusMap", 1)

    Map deviceInfo = [:]
    deviceInfo["hasEnergy"] = false
    deviceInfo["numTemperature"] = 0
    deviceInfo["numHumidity"] = 0
    deviceInfo["numPressure"] = 0
    deviceInfo["numDistance"] = 0
    deviceInfo["numSensorGroups"] = 0
    deviceInfo["sensorMap"] = [:]
    if(statusMap.containsKey("StatusSNS")) {
        sns = statusMap["StatusSNS"]
        deviceInfo["hasEnergy"] = sns.containsKey("ENERGY")
        deviceInfo["isShutter"] = sns.containsKey("Shutter1")
        deviceInfo["sensorMap"] = map_getKeysWithMapAndId(sns)
        deviceInfo["sensorMap"].remove("ENERGY")
        deviceInfo["sensorMap"].remove("Shutter1")
        deviceInfo["sensorMap"].remove("Shutter2")
        deviceInfo["sensorMap"].remove("Shutter3")
        deviceInfo["sensorMap"].remove("Shutter4")
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = map_numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = map_numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = map_numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = map_numOfKeyInSubMap(sns, "Distance")
    }

    deviceInfo["numSwitch"] = 0
    deviceInfo["isDimmer"] = false
    deviceInfo["isAddressable"] = false
    deviceInfo["isRGB"] = false
    deviceInfo["hasCT"] = false
    deviceInfo["hasFanControl"] = false
    if(statusMap["StatusSTS"] != null) {
        sts = statusMap["StatusSTS"]
        deviceInfo["isDimmer"] = sts.containsKey("Dimmer")
        deviceInfo["isAddressable"] = sts.containsKey("Width")
        if(sts.containsKey("Color")) deviceInfo["isRGB"] = sts["Color"].length() >= 6
        deviceInfo["hasCT"] = sts.containsKey("CT")
        deviceInfo["hasFanControl"] = sts.containsKey("FanSpeed")

        if(sts["POWER"] != null) {
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
        if(deviceInfo["isShutter"] == true && deviceInfo["numSwitch"] >= 2) {
            deviceInfo["numSwitch"] -= 2
        }
    }
    logging("Device info found: $deviceInfo", 100)

    List driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
    String namespace = "tasmota"
    if(deviceInfo["numSwitch"] > 0) {
         
        if(deviceInfo["hasEnergy"]  == true && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                driverName = ["Tasmota - Universal Metering Dimmer (Child)", "Generic Component Dimmer"]
            } else {
                driverName = ["Tasmota - Universal Metering Plug/Outlet (Child)", 
                              "Tasmota - Universal Metering Bulb/Light (Child)",
                              "Generic Component Metering Switch"]
            }
        } else {
            if(deviceInfo["hasEnergy"] == true) {
                log.warn "This device reports Metering Capability AND has RGB, Color Temperature or is Addressable. Metering values will be ignored... This is NOT supported and may result in errors, please report it to the developer to find a solution."
            }
            if((deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
                deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
                } else if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == true) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component CT"]
                } else if(deviceInfo["isRGB"] == true && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGB"]
                } else {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGBW"]
                }
            }
        }
        
        for(i in 1..deviceInfo["numSwitch"]) {
            namespace = "tasmota"
            String childId = "POWER$i"
            String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
            String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($i)"
            logging("createChildDevice: POWER$i", 1)
            tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
        }
    }
    
    if(deviceInfo["hasFanControl"] == true) {
         
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Fan Control (Child)"]
        String childId = "FAN"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    if(deviceInfo["isShutter"] == true) {
        logging("isShutter", 100)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Curtain (Child)"]
        String childId = "SHUTTER"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    deviceInfo["sensorMap"].each {
         
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multi Sensor (Child)"]
        String childId = "${it.key}"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }
    deviceInfo = null
    parseResult(statusMap)
    statusMap = null
}

String tasmota_getChildDeviceNameRoot(boolean keepType=false) {
    String childDeviceNameRoot = getDeviceInfoByName('name')
    if(childDeviceNameRoot.toLowerCase().endsWith(' (parent)')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-9)
    } else if(childDeviceNameRoot.toLowerCase().endsWith(' parent')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-7)
    }
    if(keepType == false && childDeviceNameRoot.toLowerCase().startsWith('tasmota - ')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(10, childDeviceNameRoot.length())
    }
    return childDeviceNameRoot
}

String tasmota_getMinimizedDriverName(String driverName) {
    logging("tasmota_getMinimizedDriverName(driverName=$driverName)", 1)
    if(driverName.toLowerCase().endsWith(' (child)')) {
        driverName = driverName.substring(0, driverName.length()-8)
    } else if(driverName.toLowerCase().endsWith(' child')) {
        driverName = driverName.substring(0, driverName.length()-6)
    }
    if(driverName.toLowerCase().endsWith(' (parent)')) {
        driverName = driverName.substring(0, driverName.length()-9)
    } else if(driverName.toLowerCase().endsWith(' parent')) {
        driverName = driverName.substring(0, driverName.length()-7)
    }
    driverName = driverName.replaceAll("(?i) \\(parent\\)", "").replaceAll("(?i) parent", "").replaceAll("(?i)parent", "")
    logging("driverName: $driverName", 1)

    driverName = driverName.replaceFirst("\\(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\)", "").replaceFirst("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "");

    if(driverName.toLowerCase().startsWith('tasmota - ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    if(driverName.toLowerCase().startsWith('universal ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    driverName = driverName.replaceAll("Generic Component ", "")
    driverName = driverName.trim()
    if(driverName == '') driverName = "Device"
    
    logging("tasmota_getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

com.hubitat.app.ChildDeviceWrapper tasmota_getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void tasmota_createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    logging("tasmota_createChildDevice(namespace=$namespace, driverName=$driverName, childId=$childId, childName=$childName, childLabel=$childLabel)", 1)
    com.hubitat.app.ChildDeviceWrapper childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice != null) {
        childDevice.setName(childName)
        logging("childDevice.getData(): ${childDevice.getData()}", 1)
    } else {
         
        Integer s = childName.size()
        for(i in 0..s) {
            def currentNamespace = namespace
            if(driverName[i].toLowerCase().startsWith('generic component')) {
                currentNamespace = "hubitat"
            }
            try {
                addChildDevice(currentNamespace, driverName[i], "$device.id-$childId", [name: childName, label: childLabel, isComponent: false])
                logging("Created child device '$childLabel' using driver '${driverName[i]}'...", 100)
            } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                if(i == s) {
                    log.error "'${driverName[i]}' driver can't be found! Did you forget to install the child driver?"
                } else {
                    log.warn "'${driverName[i]}' driver can't be found! Trying another driver: ${driverName[i+1]}..."
                }
            }
        }
    }
}

private void tasmota_updateChildDeviceSetting(String settingName, String value) {
    getChildDevices().each { cDev ->
        cDev.clearSetting(settingName)
        cDev.removeSetting(settingName)
        cDev.updateSetting(settingName, value)
    }
}

private String tasmota_determineDeviceNetworkId(String macOrIP, boolean isIP = false) {
    String myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
         
        myDNI = tasmota_convertIPtoHex(macOrIP)
    }
     
    return myDNI
}

void tasmota_prepareDNI() {
    if (useIPAsID) {
        String hexIPAddress = tasmota_determineDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            tasmota_updateDNI()
        }
    } else if (state.mac != null && state.dni != state.mac) { 
        state.dni = tasmota_determineDeviceNetworkId(state.mac)
        tasmota_updateDNI()
    }
}

private void tasmota_updateDNI() {
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
         
        device.deviceNetworkId = state.dni
    }
}

Integer tasmota_getTelePeriodValue() {
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private String tasmota_getHostAddress() {
    Integer port = 80
    if (getDeviceDataByName("port") != null) {
        port = getDeviceDataByName("port").toInteger()
    }
    if (override == true && ipAddress != null){
        return "${ipAddress}:$port"
    } else if(device.currentValue("ip") != null) {
        return "${device.currentValue("ip")}:$port"
    } else if(getDeviceDataByName("ip") != null) {
        return "${getDeviceDataByName("ip")}:$port"
    } else {
        log.warn "tasmota_getHostAddress() failed and ran out of fallbacks! If this happens, contact the developer, this is an \"impossible\" scenario!"
	    return "127.0.0.1:$port"
    }
}

private String tasmota_convertIPtoHex(String ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
         
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

private String tasmota_getFirstTwoIPBytes(String ipAddress) {
    String ipStart = null
    if(ipAddress != null) {
        ipStart = ipAddress.tokenize( '.' ).take(2).join('.') + '.'
         
    } else {
        ipStart = ''
    }
    return ipStart
}

void tasmota_sync(String ip, Integer port = null) {
    String existingIp = getDataValue("ip")
    String existingPort = getDataValue("port")
    logging("Running tasmota_sync()", 1)
    if (ip != null && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip, isStateChange: false)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$ip\">$ip</a>", isStateChange: false)
        logging("IP set to ${ip}", 1)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
        logging("Port set to ${port}", 1)
    }
}

Integer tasmota_dBmToQuality(Integer dBm) {
     
    return dBm
}

private void tasmota_httpGetAction(String uri, String callback="parse") { 
  tasmota_updateDNI()
  
  Map headers = tasmota_getHeader()
  logging("tasmota_httpGetAction for 'http://${tasmota_getHostAddress()}$uri'...", 1)
  try {
     
    asynchttpGet(
        callback,
        [uri: "http://${tasmota_getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in tasmota_httpGetAction(uri): $e ('$uri')"
  }
}

private hubitat.device.HubAction tasmota_postAction(String uri, String data) { 
  tasmota_updateDNI()

  Map headers = tasmota_getHeader()

  hubitat.device.HubAction hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in tasmota_postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

void tasmota_sendCommandParse(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            Map r = asyncResponse.getJson()
            logging("tasmota_sendCommandParse(asyncResponse.getJson() = \"${r}\")", 1)
            sendEvent(name: "commandResult", value: asyncResponse.getData(), isStateChange: true)
            parseResult(r)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("tasmota_sendCommandParse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("tasmota_sendCommandParse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("tasmota_sendCommandParse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

String tasmota_getCommandString(String command, String value) {
    String uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${tasmota_urlEscape(password)}&"
    }
	if (value != null && value != "") {
		uri += "cmnd=${tasmota_urlEscape(command)}%20${tasmota_urlEscape(value)}"
	}
	else {
		uri += "cmnd=${tasmota_urlEscape(command)}"
	}
    return uri
}

String tasmota_getMultiCommandString(List<Map> commands) {
    String uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${password}&"
    }
    uri += "cmnd=backlog%20"
    if(commands.size() > 30) {
        log.warn "Backlog only supports 30 commands, the last ${commands.size() - 30} will be ignored!"
    }
    commands.each {cmd->
        if(cmd.containsKey("value")) {
          uri += "${tasmota_urlEscape(cmd['command'])}%20${tasmota_urlEscape(cmd['value'])}%3B%20"
        } else {
          uri += "${tasmota_urlEscape(cmd['command'])}%3B%20"
        }
    }
    return uri
}

private String tasmota_urlEscape(String url) {
    return(URLEncoder.encode(url).replace("+", "%20").replace("#", "%23"))
}

private String tasmota_convertPortToHex(Integer port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private String tasmota_encodeCredentials(String username, String password) {
	String userpassascii = "${username}:${password}"
    String userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private Map tasmota_getHeader(String userpass = null) {
    Map headers = [:]
    headers.put("Host", tasmota_getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}
// END:  getHelperFunctions('tasmota')

// BEGIN:getHelperFunctions('map')
boolean map_containsKeyInSubMap(aMap, key) {
    boolean hasKey = false
    aMap.find {
        try{
            hasKey = it.value.containsKey(key)
        } catch(e) {

        }
        hasKey == true
    }
    return hasKey
}

Integer map_numOfKeyInSubMap(aMap, String key) {
    Integer numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
        }
    }
    return numKeys
}

Integer map_numOfKeysIsMap(aMap) {
    Integer numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap map_getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}
// END:  getHelperFunctions('map')

// BEGIN:getHelperFunctions('rgbw')
void rgbw_setColor(value) {
    logging("rgbw_setColor('${value}')", 10)
	if (value != null && value instanceof Map) {
        def h = value.containsKey("hue") ? value.hue : 0
        def s = value.containsKey("saturation") ? value.saturation : 0
        def b = value.containsKey("level") ? value.level : 0
        tasmota_rgbw_setHSB(h, s, b)
    } else {
        logging("rgbw_setColor('${value}') called with an INVALID argument!", 10)
    }
}

void rgbw_setHue(h) {
    logging("rgbw_setHue('${h}')", 10)
    tasmota_rgbw_setHSB(h, null, null)
}

void rgbw_setSaturation(s) {
    logging("rgbw_setSaturation('${s}')", 10)
    tasmota_rgbw_setHSB(null, s, null)
}

void rgbw_setLevel(b) {
    logging("rgbw_setLevel('${b}')", 10)
    rgbw_setLevel(b, 0)
}

def rgbw_rgbToHSB(red, green, blue) {
    BigDecimal r = red / 255f
    BigDecimal g = green / 255f
    BigDecimal b = blue / 255f
    BigDecimal max = [r, g, b].max()
    BigDecimal min = [r, g, b].min()
    BigDecimal delta = max - min
    def hue = 0
    def saturation = 0
    if (max == min) {
        hue = 0
    } else if (max == r) {
        def h1 = (g - b) / delta / 6
        def h2 = h1.asType(Integer)
        if (h1 < 0) {
            hue = Math.round(360 * (1 + h1 - h2))
        } else {
            hue = Math.round(360 * (h1 - h2))
        }
        logging("rgbToHSB: red max=${max} min=${min} delta=${delta} h1=${h1} h2=${h2} hue=${hue}", 1)
    } else if (max == g) {
        hue = 60 * ((b - r) / delta + 2)
        logging("rgbToHSB: green hue=${hue}", 1)
    } else {
        hue = 60 * ((r - g) / (max - min) + 4)
        logging("rgbToHSB: blue hue=${hue}", 1)
    }
    
    hue = Math.round((hue) / 3.6)

    if (max == 0) {
        saturation = 0
    } else {
        saturation = delta / max * 100
    }
    
    def level = max * 100
    
    return [
        "hue": hue.asType(Integer),
        "saturation": saturation.asType(Integer),
        "level": level.asType(Integer),
    ]
}

String rgbw_getColorNameFromTemperature(Integer colorTemperature){
    if (!colorTemperature) return "Undefined"
    String colorName = "Undefined"
    if (colorTemperature <= 2000) colorName = "Sodium"
    else if (colorTemperature <= 2100) colorName = "Starlight"
    else if (colorTemperature < 2400) colorName = "Sunrise"
    else if (colorTemperature < 2800) colorName = "Incandescent"
    else if (colorTemperature < 3300) colorName = "Soft White"
    else if (colorTemperature < 3500) colorName = "Warm White"
    else if (colorTemperature < 4150) colorName = "Moonlight"
    else if (colorTemperature <= 5000) colorName = "Horizon"
    else if (colorTemperature < 5500) colorName = "Daylight"
    else if (colorTemperature < 6000) colorName = "Electronic"
    else if (colorTemperature <= 6500) colorName = "Skylight"
    else if (colorTemperature < 20000) colorName = "Polar"
    return colorName
}

String rgbw_getColorNameFromHueSaturation(Integer hue, Integer saturation=null){
    if (!hue) hue = 0
    String colorName = "Undefined"
    switch (hue * 3.6 as Integer){
        case 0..15: colorName = "Red"
            break
        case 16..45: colorName = "Orange"
            break
        case 46..75: colorName = "Yellow"
            break
        case 76..105: colorName = "Chartreuse"
            break
        case 106..135: colorName = "Green"
            break
        case 136..165: colorName = "Spring"
            break
        case 166..195: colorName = "Cyan"
            break
        case 196..225: colorName = "Azure"
            break
        case 226..255: colorName = "Blue"
            break
        case 256..285: colorName = "Violet"
            break
        case 286..315: colorName = "Magenta"
            break
        case 316..345: colorName = "Rose"
            break
        case 346..360: colorName = "Red"
            break
    }
    if (saturation == 0) colorName = "White"
    return colorName
}

void rgbw_white() {
    logging("rgbw_white()", 10)
    tasmota_rgbw_whiteForPlatform()
}

void rgbw_red() {
    logging("rgbw_red()", 10)
    tasmota_rgbw_setRGB(255, 0, 0)
}

void rgbw_green() {
    logging("rgbw_green()", 10)
    tasmota_rgbw_setRGB(0, 255, 0)
}

void rgbw_blue() {
    logging("rgbw_blue()", 10)
    tasmota_rgbw_setRGB(0, 0, 255)
}

void rgbw_yellow() {
    logging("rgbw_yellow()", 10)
    tasmota_rgbw_setRGB(255, 255, 0)
}

void rgbw_cyan() {
    logging("rgbw_cyan()", 10)
    tasmota_rgbw_setRGB(0, 255, 255)
}

void rgbw_pink() {
    logging("rgbw_pink()", 10)
    tasmota_rgbw_setRGB(255, 0, 255)
}
// END:  getHelperFunctions('rgbw')

// BEGIN:getHelperFunctions('tasmota-rgbw')
void tasmota_rgbw_setColorTemperature(value) {
    logging("tasmota_rgbw_setColorTemperature('${value}')", 10)
    if(device.currentValue('colorTemperature') != value ) sendEvent(name: "colorTemperature", value: value)
    Integer t = value != null ?  (value as Integer) : 0
    if(t > 6536) t = 6536
    if(t < 2000) t = 2000
    t = Math.round(1000000/t)
    if(t < 153) t = 153
    if(t > 500) t = 500
    state.mired = t
    state.hue = 0
    state.saturation = 0
    state.colorMode = "CT"
    logging("tasmota_rgbw_setColorTemperature('${t}') ADJUSTED to Mired", 10)
    tasmota_getAction(tasmota_getCommandStringWithModeReset("CT", "${t}"))
}

void tasmota_rgbw_setHSB(h, s, b) {
    logging("tasmota_rgbw_setHSB('${h}','${s}','${b}')", 10)
    tasmota_rgbw_setHSB(h, s, b, true)
}

void tasmota_rgbw_setHSB(h, s, b, callWhite) {
    logging("tasmota_rgbw_setHSB('${h}','${s}','${b}', callWhite=${String.valueOf(callWhite)})", 10)
    boolean adjusted = False
    if(h == null || h == 'NaN') {
        h = state != null && state.containsKey("hue") ? state.hue : 0
        adjusted = True
    }
    if(s == null || s == 'NaN') {
        s = state != null && state.containsKey("saturation") ? state.saturation : 0
        adjusted = True
    }
    if(b == null || b == 'NaN') {
        b = state != null && state.containsKey("level") ? state.level : 0
        adjusted = True
    }
    if(adjusted) {
        logging("ADJUSTED tasmota_rgbw_setHSB('${h}','${s}','${b}'", 1)
    }
    Integer adjustedH = Math.round(h*3.6)
    if( adjustedH > 360 ) { adjustedH = 360 }
    if( b < 0 ) b = 0
    if( b > 100 ) b = 100
    String hsbcmd = "${adjustedH},${s},${b}"
    logging("hsbcmd = ${hsbcmd}", 1)
    state.hue = h
    state.saturation = s
    state.level = b
    state.colorMode = "RGB"
    if (hsbcmd.startsWith("0,0,")) {
        rgbw_white()
    } else {
        if(useAlternateColorCommand == true) {
            def rgbval = hubitat.helper.ColorUtils.hsvToRGB([h, s, b])
            tasmota_rgbw_setRGB(rgbval[0], rgbval[1], rgbval[2])
        } else {
            tasmota_getAction(tasmota_getCommandStringWithModeReset("HsbColor", hsbcmd))
        }
    }
}

void tasmota_rgbw_setRGB(r, g, b) {   
    logging("tasmota_rgbw_setRGB('${r}','${g}','${b}')", 10)
    boolean adjusted = False
    if(r == null || r == 'NaN') {
        r = 0
        adjusted = True
    }
    if(g == null || g == 'NaN') {
        g = 0
        adjusted = True
    }
    if(b == null || b == 'NaN') {
        b = 0
        adjusted = True
    }
    if(adjusted) {
        logging("ADJUSTED tasmota_rgbw_setRGB('${r}','${g}','${b}')", 1)
    }
    String rgbcmd = hubitat.helper.ColorUtils.rgbToHEX([r, g, b])
    logging("rgbcmd = ${rgbcmd}", 1)
    state.red = r
    state.green = g
    state.blue = b
    def hsbColor = rgbw_rgbToHSB(r, g, b)
    logging("hsbColor from RGB: ${hsbColor}", 1)
    state.colorMode = "RGB"
    state.hue = hsbColor['hue']
    state.saturation = hsbColor['saturation']
    state.level = hsbColor['level']
    tasmota_getAction(tasmota_getCommandStringWithModeReset("Color1", rgbcmd))
}

void tasmota_rgbw_setLevel(l) {
    tasmota_rgbw_setLevel(l, 0)
}

void tasmota_rgbw_setLevel(l, duration) {
    if (duration == 0) {
        if (false && state.colorMode == "RGB") {
            tasmota_rgbw_setHSB(null, null, l)
        } else {
            state.level = l
            tasmota_getAction(tasmota_getCommandString("Dimmer", "${l}"))
        }
    } else if (duration > 0) {
        if (false && state.colorMode == "RGB") {
            tasmota_rgbw_setHSB(null, null, l)
        } else {
            if (duration > 5400) {
                log.warn "Maximum supported dimming duration is 5400 seconds due to current implementation method used."
                duration = 5400
            } 
            Integer cLevel = state.level
            
            Integer levelDistance = l - cLevel
            Integer direction = 1
            if(levelDistance < 0) {
                direction = -1
                levelDistance = levelDistance * -1
            }
            Integer steps = 13
            Integer increment = Math.round(((levelDistance as Float)  / steps) as Float)
            if(increment <= 1 && Math.abs(levelDistance) < steps) {
                steps = Math.abs(levelDistance)
            }
            List fadeCommands = []
            fadeCommands.add([command: "Fade", value: "1"])
            fadeCommands.add([command: "Speed", value: "20"])
            if(steps > 0) {

                duration = ((duration as Float) - (2 * steps * 0.2) + 1) as Float
                BigDecimal stepTime = round2((duration / steps) as Float, 1)
                Integer stepTimeTasmota = Math.round((stepTime as Float) * 10)
                BigDecimal lastStepTime = round2((stepTime + (duration - (stepTime * steps)) as Float), 1)
                Integer lastStepTimeTasmota = Math.round((lastStepTime as Float) * 10)
                
                Integer cmdLevel = cLevel
                
                (1..steps).each{
                    cmdLevel += (increment * direction)
                    if(direction == 1 && (cmdLevel > l || it == steps)) cmdLevel = l
                    if(direction == -1 && (cmdLevel < l || it == steps)) cmdLevel = l
                    if(it != steps) {
                        fadeCommands.add([command: "Delay", value: "$stepTimeTasmota"])
                    } else {
                        fadeCommands.add([command: "Delay", value: "$lastStepTimeTasmota"])
                    }
                    fadeCommands.add([command: "Dimmer", value: "$cmdLevel"])
                }
            } else {
                fadeCommands.add([command: "Dimmer", value: "$l"])
            }
            fadeCommands.add([command: "Fade", value: "0"])
            logging("fadeCommands: '" + fadeCommands + "', cmdData=${[cLevel:cLevel, levelDistance:levelDistance, direction:direction, steps:steps, increment:increment, stepTime:stepTime, lastStepTime:lastStepTime]}", 1)
            tasmota_getAction(tasmota_getMultiCommandString(fadeCommands))
        }
   }
}

void tasmota_rgbw_stopLevelChange() {
    tasmota_getAction(tasmota_getCommandString("Fade", "0"))
    tasmota_getAction(tasmota_getCommandString("Backlog", null))
    tasmota_rgbw_modeSingleColor(1)
}

void tasmota_rgbw_startLevelChange(String direction) {
    Integer cLevel = state.level
    Integer delay = 30
    tasmota_rgbw_modeSingleColor(1)
    if(direction == "up") {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (100-cLevel)) as Float)
        }
        tasmota_rgbw_setLevel(100, delay)
    } else {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (cLevel)) as Float)
        }
        tasmota_rgbw_setLevel(0, delay)
    }
}

void tasmota_rgbw_whiteForPlatform() {
    logging("tasmota_rgbw_whiteForPlatform()", 10)
    Integer l = state.level
    if (l < 10) l = 10
    l = Math.round(l * 2.55).toInteger()
    if (l > 255) l = 255
    def lHex = l.toHexString(l)
    String hexCmd = "#${lHex}${lHex}${lHex}${lHex}${lHex}"
    logging("hexCmd='${hexCmd}'", 1)
    state.hue = 0
    state.saturation = 0
    state.red = l
    state.green = l
    state.blue = l
    state.colorMode = "CT"
    tasmota_getAction(tasmota_getCommandStringWithModeReset("Color1", hexCmd))
}

void tasmota_rgbw_modeSet(Integer mode, BigDecimal speed=3) {
    logging("tasmota_rgbw_modeSet('${mode}')", 10)
    tasmota_getAction(tasmota_getMultiCommandString([[command:"Speed", value:"$speed"], [command:"Scheme", value:"${mode}"]]))
}

void tasmota_rgbw_modeNext(BigDecimal speed=3) {
    logging("tasmota_rgbw_modeNext()", 10)
    if (state.mode < 4) {
        state.mode = state.mode + 1
    } else {
        state.mode = 0
    }
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modePrevious(BigDecimal speed=3) {
    if (state.mode > 0) {
        state.mode = state.mode - 1
    } else {
        state.mode = 4
    }
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modeSingleColor(BigDecimal speed=3) {
    state.mode = 0
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modeCycleUpColors(BigDecimal speed=3) {
    state.mode = 2
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modeCycleDownColors(BigDecimal speed=3) {
    state.mode = 3
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modeRandomColors(BigDecimal speed=3) {
    state.mode = 4
    tasmota_rgbw_modeSet(state.mode, speed)
}

void tasmota_rgbw_modeWakeUp(BigDecimal wakeUpDuration) {
    Integer level = device.currentValue('level')
    Integer nlevel = level > 10 ? level : 10
    logging("tasmota_rgbw_modeWakeUp(wakeUpDuration ${wakeUpDuration}, current level: ${nlevel})", 1)
    tasmota_rgbw_modeWakeUp(wakeUpDuration, nlevel)
}

void tasmota_rgbw_modeWakeUp(BigDecimal wakeUpDuration, BigDecimal level) {
    logging("tasmota_rgbw_modeWakeUp(wakeUpDuration ${wakeUpDuration}, level: ${level})", 1)
    state.mode = 1
    wakeUpDuration = wakeUpDuration < 1 ? 1 : wakeUpDuration > 3000 ? 3000 : wakeUpDuration
    level = level < 1 ? 1 : level > 100 ? 100 : level
    state.level = level
    tasmota_getAction(tasmota_getMultiCommandString([[command: "WakeupDuration", value: "${wakeUpDuration}"],
                                    [command: "Wakeup", value: "${level}"]]))
}

void tasmota_rgbw_setColorByRGBString(String colorRGB) {
    logging("tasmota_rgbw_setColorByRGBString(colorRGB ${colorRGB})", 100)
    tasmota_getAction(tasmota_getCommandStringWithModeReset("Color1", colorRGB.take(11)))
}

void tasmota_rgbw_setPixelColor(String colorRGB, BigDecimal pixel) {
    logging("tasmota_rgbw_setPixelColor(colorRGB ${colorRGB}, pixel: ${pixel})", 1)
    if(pixel < 1) pixel = 1
    if(pixel > 512) pixel = 512
    tasmota_getAction(tasmota_getCommandStringWithModeReset("Led$pixel", colorRGB.take(7)))
}

void tasmota_rgbw_setAddressablePixels(BigDecimal pixels) {
    logging("tasmota_rgbw_setAddressablePixels(pixels: ${pixels})", 100)
    if(pixels < 1) pixels = 1
    if(pixels > 512) pixels = 512
    tasmota_getAction(tasmota_getCommandString("Pixels", "$pixels"))
}

void tasmota_rgbw_setAddressableRotation(BigDecimal pixels) {
    logging("tasmota_rgbw_setAddressableRotation(pixels: ${pixels})", 100)
    if(pixels < 1) pixels = 1
    if(pixels > 512) pixels = 512
    tasmota_getAction(tasmota_getCommandString("Rotation", "$pixels"))
}

void tasmota_rgbw_setEffectWidth(BigDecimal pixels) {
    logging("tasmota_rgbw_setEffectWidth(pixels: ${pixels})", 100)
    if(pixels < 0) pixels = 0
    if(pixels > 4) pixels = 4
    tasmota_getAction(tasmota_getCommandString("Width1", "$pixels"))
}

String tasmota_getCommandStringWithModeReset(String command, String value) {
    if(useAlternateColorCommand == true && command == "Color1") {
        if(value.startsWith("#") == true) value = value.substring(1)
        return tasmota_getCommandString("Var1", "$value")
    } else {
        return tasmota_getMultiCommandString([[command: "Scheme", value: "0"], [command: "Fade", value: "0"], 
                                  [command: command, value: value]])
    }
}
// END:  getHelperFunctions('tasmota-rgbw')
