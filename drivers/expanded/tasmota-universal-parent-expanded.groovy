/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.2.0503T
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
 */

// BEGIN:getDefaultParentImports()
/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
// Used for MD5 calculations
import java.security.MessageDigest
/* Default Parent Imports */
// END:  getDefaultParentImports()


metadata {
	definition (name: "Tasmota - Universal Parent", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-universal-parent-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilities()
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        // END:  getDefaultMetadataCapabilities()
        capability "PresenceSensor"
        capability "Initialize"         // This makes initialize() run on hub start, only needed in the Parent
        
        // BEGIN:getDefaultParentMetadataAttributes()
        // Default Parent Attributes
        attribute   "ip", "string"
        attribute   "ipLink", "string"
        attribute   "module", "string"
        attribute   "templateData", "string"
        attribute   "wifiSignal", "string"
        // END:  getDefaultParentMetadataAttributes()
        // BEGIN:getDefaultMetadataAttributes()
        // Default Attributes
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        attribute "commandSent", "string"
        attribute "commandResult", "string"

        // BEGIN:getMetadataCommandsForHandlingChildDevices()
        // Commands for handling Child Devices
        //command "childOn"
        //command "childOff"
        //command "recreateChildDevices"
        command "deleteChildren"
        // END:  getMetadataCommandsForHandlingChildDevices()
        // BEGIN:getDefaultMetadataCommands()
        // Default Commands
        command "reboot"
        // END:  getDefaultMetadataCommands()
        command "sendCommand", [[name:"Command*", type: "STRING", description: "Tasmota Command"],
            [name:"Argument", type: "STRING", description: "Argument (optional)"]]
        
        command "parseJSON", [[name:"JSON*", type: "STRING", description: "Tasmota Status as JSON"]]
	}

	preferences {
        // BEGIN:getDefaultParentMetadataPreferences()
        // Default Parent Preferences
        input(name: "runReset", description: addDescriptionDiv("For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct."), title: addTitleDiv("Settings"), displayDuringSetup: false, type: "paragraph", element: "paragraph")
        input(name: "debugLogging", type: "bool", title: addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: addTitleDiv("Enable descriptionText logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultParentMetadataPreferences()
        input(name: "deviceConfig", type: "enum", title: addTitleDiv("Device Configuration"), 
            description: addDescriptionDiv("Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting."), 
            options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", 
            displayDuringSetup: true, required: false)

        // BEGIN:getMetadataPreferencesForHiding()
        // Preferences for Hiding
        input(name: "hideExtended", type: "bool", title: addTitleDiv("Hide Extended Settings"), description: addDescriptionDiv("Hides extended settings, usually not needed."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideAdvanced", type: "bool", title: addTitleDiv("Hide Advanced Settings"), description: addDescriptionDiv("Hides advanced settings, usually not needed anyway."), defaultValue: true, displayDuringSetup: false, required: false)
        // END:  getMetadataPreferencesForHiding()

        // BEGIN:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        // Default Preferences for Tasmota
        input("password", "password", title: addTitleDiv("Device Password"), description: addDescriptionDiv("REQUIRED if set on the Device! Otherwise leave empty."))
        input(name: "ipAddress", type: "string", title: addTitleDiv("Device IP Address"), description: addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
        input(name: "port", type: "number", title: addTitleDiv("Device Port"), description: addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
        input(name: "override", type: "bool", title: addTitleDiv("Override IP"), description: addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
        input(name: "useIPAsID", type: "bool", title: addTitleDiv("IP as Network ID"), description: addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
        input(name: "telePeriod", type: "string", title: addTitleDiv("Update Frequency"), description: addDescriptionDiv("Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)"), displayDuringSetup: true, required: false)
        input(name: "disableModuleSelection", type: "bool", title: addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
        input(name: "moduleNumber", type: "number", title: addTitleDiv("Module Number"), description: "ADVANCED: " + addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
        input(name: "deviceTemplateInput", type: "string", title: addTitleDiv("Device Template"), description: "ADVANCED: " + addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the Device Configuration Default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\"NAME\":\"S120\",\"GPIO\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\"FLAG\":0,\"BASE\":18})"), displayDuringSetup: true, required: false)
        
        // END:  getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        input(name: "invertPowerNumber", type: "bool", title: addTitleDiv("Send POWER1 events to POWER2, and vice versa"), description: addDescriptionDiv("Use this if you have a dimmer AND a switch in the same device and on/off is not sent/received correctly. Normally this is NOT needed."), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "useAlternateColorCommand", type: "bool", title: addTitleDiv("Use Alternate Color command in Tasmota"), description: addDescriptionDiv("When enabled, this will use \"Var1\" instead of \"Color1\" in order to be able to catch the command in rules. Normally this is NOT needed."), defaultValue: false, displayDuringSetup: false, required: false)
        // BEGIN:getDefaultMetadataPreferencesLast()
        // Default Preferences - Last
        input(name: "hideDangerousCommands", type: "bool", title: addTitleDiv("Hide Dangerous Commands"), description: addDescriptionDiv("Hides Dangerous Commands, such as 'Delete Children'."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "disableCSS", type: "bool", title: addTitleDiv("Disable CSS"), description: addDescriptionDiv("CSS makes the driver more user friendly. Disable the use of CSS in the driver by enabling this. Does NOT affect HE resource usage either way."), defaultValue: false, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferencesLast()
	}

    // The below line needs to exist in ALL drivers for custom CSS to work!
    // BEGIN:getMetadataCustomizationMethods()
    // Here getPreferences() can be used to get the above preferences
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
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    Map deviceInfo = ['name': 'Tasmota - Universal Parent', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'vid': 'generic-switch', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-universal-parent-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()


/**
 * DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 *
 *   Device configurations and functions for using them
 */
TreeMap getDeviceConfigurations() {
    // To add more devices, just add them below ;)
    // Don't forget that BOTH the App and the Universal driver needs to have this configuration.
    // If you add a device and it works well for you, please share your
    // configuration in the Hubitat Community Forum.
    //
    // typeId HAS to be unique
    // 
    // For the rest of the settings, see below to figure it out :P
    List deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
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
         //template: '{"NAME":"Sonoff iFan02","GPIO":[17,255,0,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":44}',
         installCommands: [['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan02.html'],

        /*[typeId: 'sonoff-ifan03-no_beep-m44',
         name: 'Sonoff iFan03 (No Beep) M44',
         template: '{"NAME":"Sonoff iFan03","GPIO":[17,255,0,255,0,0,29,33,23,56,22,24,0],"FLAG":0,"BASE":44}',
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m44',
         name: 'Sonoff iFan03 (Beep) M44',
         template: '{"NAME":"Sonoff iFan03","GPIO":[17,255,0,255,0,0,29,33,23,56,22,24,0],"FLAG":0,"BASE":44}',
         installCommands: [["SetOption67", "0"],
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],*/

        [typeId: 'sonoff-ifan03-no_beep-m71',
         name: 'Sonoff iFan03 (No Beep)',
         module: 71,
         //template: '{"NAME":"SonoffiFan03","GPIO":[17,148,0,149,0,0,29,161,23,56,22,24,0],"FLAG":0,"BASE":71}',
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m71',
         name: 'Sonoff iFan03 (Beep)',
         module: 71,
         //template: '{"NAME":"SonoffiFan03","GPIO":[17,148,0,149,0,0,29,161,23,56,22,24,0],"FLAG":0,"BASE":71}',
         installCommands: [["SetOption67", "1"], 
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'treatlife-ds01-dimmer',
         name: 'TreatLife DS01 Dimmer ',
         template: '{"NAME":"TL DS01 Dimmer","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
         installCommands: [["TuyaMCU", "21,2"], 
                           ["DimmerRange", "150,1000"]],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

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

        // {"NAME":"Lucci Fan","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":44}
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
        installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
        ],
        deviceLink: ''],

        [typeId: 'zigbee-controller-default' ,
        name: 'Zigbee Controller (default pinout)',
        template: '{"NAME":"Zigbee","GPIO":[0,0,0,0,0,0,0,0,0,166,0,165,0],"FLAG":0,"BASE":18}',
        installCommands: [["SerialLog", "0"],
                          //['setoption3', '1'], // enable MQTT - REQUIRED for this Zigbee devices to work!
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
        module: 54,
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        //SetOption66 - Set publishing TuyaReceived to MQTT  »6.7.0
                        //0 = disable publishing TuyaReceived over MQTT (default)
                        //1 = enable publishing TuyaReceived over MQTT
                        ['SetOption66', "1"], // This is REQUIRED to get the Tuya Data
                        ['Mem1', '100'],   // Updated with the current Curtain location
                        ['Mem2', '11'],    // Step for each increase
                        ['Mem3', '1'],     // delay in 10th of a second (1 = 100ms)
                        ['Mem4', '9'],     // Motor startup steps
                        ['Mem5', '1'],     // Extra step when opening
                        ['Delay', '15'],   // Set delay between Backlog commands
                        ['Rule1', 'ON Dimmer#State DO Mem1 %value%; ENDON'],
                        ['Rule1', '+ ON TuyaReceived#Data=55AA00070005650400010277 DO Backlog Var1 %mem1%; Var2 Go; Var5 C; Add1 %mem2%; Sub1 %mem4%; Var4 %mem2%; Event Go; ENDON'],
                        ['Rule1', '+ ON Event#Go DO Backlog Dimmer %var1%; Event %var5%%var1%; Event %var2%2; ENDON'],
                        ['Rule1', '+ ON Event#Go2 DO Backlog Add1 %var4%; Delay %mem3%; Event %var1%; Event %var2%;  ENDON'],
                        ['Rule1', '+ ON Event#O-7 DO Var2 sC; ENDON ON Event#O-8 DO Var2 sC; ENDON ON Event#O-9 DO Var2 sC; ENDON ON Event#O-10 DO Var2 sC; ENDON ON Event#O-11 DO Var2 sC; ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', 'ON TuyaReceived#Data=55AA00070005650400010176 DO Backlog Var1 %mem1%; Var2 Go; Var5 O; Sub1 %mem2%; Add1 %mem4%; Var4 %mem2%; Add4 %mem5%; Mult4 -1; Event Go; ENDON'],
                        ['Rule2', '+ ON Event#sC DO Backlog Var2 sC2; Event sC2; ENDON'],
                        ['Rule2', '+ ON Event#sC2 DO Backlog Var2 sC2; TuyaSend4 101,1; ENDON'],
                        ['Rule2', '+ ON TuyaReceived#Data=55AA00070005650400010075 DO Var2 sC3; ENDON'],
                        ['Rule2', '+ ON Event#C107 DO Var2 sC; ENDON ON Event#C108 DO Var2 sC; ENDON ON Event#C109 DO Var2 sC; ENDON ON Event#C110 DO Var2 sC; END ON ON Event#C111 DO Var2 sC; ENDON'],
                        ['Rule2', '1'],
                        ['Rule3', 'ON Event#C100 DO Var2 sC; ENDON ON Event#C101 DO Var2 sC; ENDON ON Event#C102 DO Var2 sC; ENDON ON Event#C103 DO Var2 sC; ENDON ON Event#C104 DO Var2 sC; ENDON ON Event#C105 DO Var2 sC; ENDON ON Event#C106 DO Var2 sC; ENDON ON Event#O0 DO Var2 sC; ENDON ON Event#O-1 DO Var2 sC; ENDON ON Event#O-2 DO Var2 sC; ENDON ON Event#O-3 DO Var2 sC; ENDON ON Event#O-4 DO Var2 sC; ENDON ON Event#O-5 DO Var2 sC; ENDON ON Event#O-6 DO Var2 sC; ENDON ON Event#O-12 DO Var2 sC; ENDON'],
                        ['Rule3', '1']],
        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        [typeId: 'mj-sd02-dimmer-switch',
        comment: 'WITHOUT power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02","GPIO":[19,18,0,33,34,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        // Possible alternative: {"NAME":"MJ-SD02","GPIO":[19,18,0,35,36,34,255,255,33,37,32,126,29],"FLAG":15,"BASE":18}
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption3', '1'], // enable MQTT - REQUIRED for these rules to work!
                        ['setoption1', '1'], // restrict to single, double and hold actions (i.e., disable inadvertent reset due to long press)
                        ['setoption32', '8'],     // Number of 0.1 seconds to hold button before sending HOLD action message.
                        ['buttontopic', '0'],   // This enables the below Rule triggers
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'mj-sd02-dimmer-switch-led',
        comment: 'WITH power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02-LED","GPIO":[19,18,0,33,56,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        // Possible alternative: {"NAME":"MJ-SD02","GPIO":[19,18,0,35,36,34,255,255,33,37,32,126,29],"FLAG":15,"BASE":18}
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption3', '1'], // enable MQTT - REQUIRED for these rules to work!
                        ['setoption1', '1'], // restrict to single, double and hold actions (i.e., disable inadvertent reset due to long press)
                        ['setoption32', '8'],     // Number of 0.1 seconds to hold button before sending HOLD action message.
                        ['buttontopic', '0'],   // This enables the below Rule triggers
                        ['LedPower', '1'],
                        ['SetOption31', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        //https://templates.blakadder.com/oil_diffuser_550ml.html

        
        [typeId: 'maxcio-diffuser-v1',
        comment: 'REQUIRES "Use Alternate Color command in Tasmota" to be set!',
        name: 'Maxcio Diffuser Wood Grain (v1)',
        template: '{"NAME":"MaxcioDiffuser","GPIO":[0,107,0,108,21,0,0,0,37,38,39,28,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption20', '1'], // Update of Dimmer/Color/CT without turning power on
                        //['SetOption59', '0'],
                        //['SwitchMode', '1'],
                        //['SetOption66', '0'],   // Set publishing TuyaReceived to MQTT, 0 = disable, 1 = enable
                        //['SetOption34', '100'],  // 0..255 = set Backlog inter-command delay in milliseconds (default = 200)
                        ['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; rule2 0; power1 1; rule2 1; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO power1 1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog rule2 0; power2 0; rule2 1; power3 %var2%; var2 1; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog rule2 0; power2 1; rule2 1; var2 0; power3 0; ENDON'],
                        ['Rule2', 'ON Power1#State DO tuyasend1 5,%value% ENDON ON Power2#State=0 DO tuyasend1 1,0 ENDON ON Power2#State=1 DO backlog var2 1; tuyasend1 1,1; ENDON'],
                        ['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO power1 0 ENDON'],
                        //['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; var7 no; power1 1; ENDON'],
                        //['Rule2', 'ON Power1#State DO backlog var5 0; var7 1; tuyasend%var7% 5,%value%; ENDON ON Power2#State=0 DO backlog var6 0; tuyasend1 1,0; ENDON ON Power2#State=1 DO backlog var6 0; var2 1; tuyasend1 1,1; ENDON'],
                        //['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO backlog powe%var5% 0; var5 r1; ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO backlog powe%var5% 1; var5 r1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog powe%var6% 0; power3 %var2%; var2 1; var6 r2; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog powe%var6% 1; var2 0; power3 0; var6 r2; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', '1'],
                        ['Rule3', '1']],
        deviceLink: 'https://templates.blakadder.com/maxcio_400ml_diffuser.html'],

        // https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433pi 
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
        
        // Generic Tasmota Devices:
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        installCommands: [],
        deviceLink: ''],

        /*[typeId: '01generic-switch-plug',
        comment: 'Works as Plug/Outlet with Alexa' ,
        name: 'Generic Switch/Plug',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-light',
         comment: 'Works as Light with Alexa' ,
        name: 'Generic Switch/Light',
        template: '',
        installCommands: [],
        deviceLink: ''],*/

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

        /*[typeId: '01generic-dimmer' ,
        name: 'Generic Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],*/
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

/**
 * --END-- DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 */

/* These functions are unique to each driver */

// Called from installed()
def installedAdditional() {
    // This runs from installed()
	logging("installedAdditional()", 50)

    // Do NOT call updatedAdditional() from here!

    //createChildDevices()
}

// Called from updated()
def updatedAdditional() {
    logging("updatedAdditional()", 1)
    //Runs when saving settings
    setDisableCSS(disableCSS)
}

def getDriverCSS() {
    // Executed on page load, put CSS used by the driver here.
    
    // This does NOT execute in the NORMAL scope of the driver!

    r = ""
    // "Data" is available when this runs
    
    //r += getCSSForCommandsToHide(["deleteChildren"])
    //r += getCSSForCommandsToHide(["overSanta", "on", "off"])
    //r += getCSSForStateVariablesToHide(["settings", "mac"])
    //r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
    //r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
    //r += getCSSForDatasToHide(["metaConfig2", "preferences", "appReturn", "namespace"])
    //r += getCSSToChangeCommandTitle("configure", "Run Configure3")
    //r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
    //r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
    //r += getCSSForHidingLastPreference()
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

def refreshAdditional(metaConfig) {
    
    //logging("this.binding.variables = ${this.binding.variables}", 1)
    //logging("settings = ${settings}", 1)
    //logging("getDefinitionData() = ${getDefinitionData()}", 1)
    //logging("getPreferences() = ${getPreferences()}", 1)
    //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
    //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
    
    metaConfig = setStateVariablesToHide(['mac'], metaConfig=metaConfig)
    logging("hideExtended=$hideExtended, hideAdvanced=$hideAdvanced", 1)
    if(hideExtended == null || hideExtended == true) {
        metaConfig = setPreferencesToHide(['hideAdvanced', 'ipAddress', 'override', 'useIPAsID', 'telePeriod', 'invertPowerNumber', 'useAlternateColorCommand'], metaConfig=metaConfig)
    }
    if(hideExtended == null || hideExtended == true || hideAdvanced == null || hideAdvanced == true) {
        //  'deviceTemplateInput',
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
            //state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\">${deviceConfigMap['comment']}</div></div>"
            //metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
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

    

    /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
    metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
    metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/
    //metaConfig = clearThingsToHide()
    //setDisableCSS(false, metaConfig=metaConfig)
    /*metaConfig = setCommandsToHide([])
    metaConfig = setStateVariablesToHide([], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide([], metaConfig=metaConfig)
    metaConfig = setDatasToHide([], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)*/
}

/* The parse(description) function is included and auto-expanded from external files */
void parse(description) {
    // BEGIN:getGenericTasmotaNewParseHeader()
    // parse() Generic Tasmota-device header BEGINS here
    //logging("Parsing: ${description}", 0)
    def descMap = parseDescriptionAsMap(description)
    def body
    logging("descMap: ${descMap}", 0)
    
    boolean missingChild = false
    
    if (state.mac != descMap["mac"]) {
        logging("Mac address of device found ${descMap["mac"]}", 10)
        state.mac = descMap["mac"]
    }
    
    prepareDNI()
    
    if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())
    
    if (body && body != "") {
        if(body.startsWith("{") || body.startsWith("[")) {
            boolean log99 = logging("========== Parsing Report ==========", 99)
            def slurper = new JsonSlurper()
            def result = slurper.parseText(body)
    
            logging("result: ${result}",0)
            // parse() Generic header ENDS here
    
    // END:  getGenericTasmotaNewParseHeader()
        missingChild = parseResult(result, missingChild)
    // BEGIN:getGenericTasmotaNewParseFooter()
    // parse() Generic Tasmota-device footer BEGINS here
    } else {
            //log.debug "Response is not JSON: $body"
        }
    }
    
    if(missingChild == true) {
        log.warn "Missing a child device, run the Refresh command from the device page!"
        // It is dangerous to do the refresh automatically from here, it could cause an eternal loop
        // Until a safe and non-resource hungry way can be created to do this automatically, a log message will
        // have to be enough.
        //refresh()
    }
    if (device.currentValue("ip") == null) {
        def curIP = getDataValue("ip")
        logging("Setting IP from Data: $curIP", 1)
        sendEvent(name: 'ip', value: curIP, isStateChange: false)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$curIP\">$curIP</a>", isStateChange: false)
    }
    
    // parse() Generic footer ENDS here
    // END:  getGenericTasmotaNewParseFooter()
}

boolean parseResult(result) {
    boolean missingChild = false
    missingChild = parseResult(result, missingChild)
    return missingChild
}

void parseJSON(jsonData) {
    boolean missingChild = false
    def jsonSlurper = new JsonSlurper()
    parseResult(jsonSlurper.parseText(jsonData), missingChild)
}

boolean parseResult(result, missingChild) {
    //logging("Entered parseResult 1", 100)
    boolean log99 = logging("parseResult: $result", 99)
    // BEGIN:getTasmotaNewParserForStatusSTS()
    // Get some Maps out to where we need them
    if (result.containsKey("StatusSTS")) {
        logging("StatusSTS: $result.StatusSTS",99)
        result << result.StatusSTS
    }
    // END:  getTasmotaNewParserForStatusSTS()
    //logging("Entered parseResult 1a", 100)
    // BEGIN:getTasmotaNewParserForParentSwitch()
    // Standard Switch Data parsing
    if (result.containsKey("POWER")  == true && result.containsKey("POWER1") == false) {
        logging("parser: POWER (child): $result.POWER",1)
        //childSendState("1", result.POWER.toLowerCase())
        missingChild = callChildParseByTypeId("POWER1", [[name:"switch", value: result.POWER.toLowerCase()]], missingChild)
    } else {
        // each is the fastest itterator to use, evn though we can't escape it
        String currentPower = ""
        (1..16).each {i->
            currentPower = "POWER$i"
            //logging("POWER$i:${result."$currentPower"} '$result' containsKey:${result.containsKey("POWER$i")}", 1)
            if(result.containsKey(currentPower) == true) {
                if(i < 3 && invertPowerNumber == true) {
                    // This is used when Tasmota mixes things up with a dimmer and relay in the same device
                    if(i == 1) {
                        currentPower = "POWER2"
                    } else {
                        currentPower = "POWER1"
                    }
                }
                logging("parser: $currentPower (original: POWER$i): ${result."POWER$i"}",1)
                missingChild = callChildParseByTypeId("$currentPower", [[name:"switch", value: result."POWER$i".toLowerCase()]], missingChild)
                //events << childSendState("1", result.POWER1.toLowerCase())
                //sendEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER1.toLowerCase() == "on"?1:0) && result.POWER1.toLowerCase() == "on"? "on" : "off"))
            }
        }
    }
    // END:  getTasmotaNewParserForParentSwitch()
    //logging("Entered parseResult 1b", 100)
    // BEGIN:getTasmotaNewParserForDimmableDevice()
    // Standard Dimmable Device Data parsing
    if(true) {
        def childDevice = getChildDeviceByActionType("POWER1")
        if(result.containsKey("Dimmer")) {
            def dimmer = result.Dimmer
            logging("Dimmer: ${dimmer}", 1)
            state.level = dimmer
            if(childDevice?.currentValue('level') != dimmer ) missingChild = callChildParseByTypeId("POWER1", [[name: "level", value: dimmer]], missingChild)
        }
        // When handling Tuya Data directly, the dimmer is often used
        if(result.containsKey("TuyaReceived") && result.TuyaReceived.containsKey("Data")) {
            // If this is the "heartbeat", ignore it...
            if(result.TuyaReceived.Data != "55AA000000010101") {
                missingChild = callChildParseByTypeId("POWER1", [[name: "tuyaData", value: result.TuyaReceived.Data]], missingChild)
            }
        }
        // Just if we need to log it
        if(log99 == true && result.containsKey("Wakeup")) {
            logging("Wakeup: ${result.Wakeup}", 99)
            //sendEvent(name: "wakeup", value: wakeup)
        }
    }
    // END:  getTasmotaNewParserForDimmableDevice()
    //logging("Entered parseResult 1c", 100)
    // BEGIN:getTasmotaNewParserForRGBWDevice()
    // Standard RGBW Device Data parsing
    if(true) {
        //[POWER:ON, Dimmer:100, Color:0,0,255,0,0, HSBColor:240,100,100, Channel:[0, 0, 100, 0, 0], CT:167]
        //[POWER:ON, Dimmer:100, Color:0,0,0,245,10, HSBColor:240,100,0, Channel:[0, 0, 0, 96, 4], CT:167]
        def childDevice = getChildDeviceByActionType("POWER1")
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
            def hsbColor = result.HSBColor.tokenize(",")
            hsbColor[0] = Math.round((hsbColor[0] as Integer) / 3.6) as Integer
            hsbColor[1] = hsbColor[1] as Integer
            //hsbColor[2] = hsbColor[2] as Integer
            logging("hsbColor: ${hsbColor}", 1)
            if(childDevice?.currentValue('hue') != hsbColor[0] ) missingChild = callChildParseByTypeId("POWER1", [[name: "hue", value: hsbColor[0]]], missingChild)
            if(childDevice?.currentValue('saturation') != hsbColor[1] ) missingChild = callChildParseByTypeId("POWER1", [[name: "saturation", value: hsbColor[1]]], missingChild)
            String colorName = getColorNameFromHueSaturation(hsbColor[0], hsbColor[1])
            if(childDevice?.currentValue('colorName') != colorName ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorName", value: colorName]], missingChild)
            }
        } else if (result.containsKey("CT")) {
            Integer t = Math.round(1000000/result.CT)
            if(childDevice?.currentValue('colorTemperature') != t ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorTemperature", value: t]], missingChild)
            }
            String colorName = getColorNameFromTemperature(t)
            if(childDevice?.currentValue('colorName') != colorName ) {
                missingChild = callChildParseByTypeId("POWER1", [[name: "colorName", value: colorName]], missingChild)
            }
            logging("CT: $result.CT ($t)",99)
        }
    
    }
    // END:  getTasmotaNewParserForRGBWDevice()
    //logging("Entered parseResult 1d", 100)
    // BEGIN:getTasmotaNewParserForFanMode()
    // Fan Mode parsing
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
    //logging("Entered parseResult 2", 100)
    // BEGIN:getTasmotaNewParserForBasicData()
    // Standard Basic Data parsing
    
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
        // We shouldn't do this, we don't need this data to be moved out
        // It will only cause issues with the "Module" setting.
        logging("Status: $result.Status",99)
        result << result.Status
    }
    if (result.containsKey("LoadAvg")) {
        logging("LoadAvg: $result.LoadAvg",99)
        //if(result.LoadAvg.toInteger() > 60) log.warn "Load average of the Device is unusually high: $result.LoadAvg"
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
        //logging("ipLink: <a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>",10)
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
        // The check for Version is here to avoid using the wrong message
        logging("Module: $result.Module",50)
        sendEvent(name: "module", value: "$result.Module", isStateChange: false)
    }
    // When it is a Template, it looks a bit different and is NOT valid JSON...
    if (result.containsKey("NAME") && result.containsKey("GPIO") && result.containsKey("FLAG") && result.containsKey("BASE")) {
        def n = result.toMapString()
        n = n.replaceAll(', ',',')
        n = n.replaceAll('\\[','{').replaceAll('\\]','}')
        n = n.replaceAll('NAME:', '"NAME":"').replaceAll(',GPIO:\\{', '","GPIO":\\[')
        n = n.replaceAll('\\},FLAG', '\\],"FLAG"').replaceAll('BASE', '"BASE"')
        // TODO: Learn how to do this the right way in Groovy
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
        // Even with "displayed: false, archivable: false" these events still show up under events... There is no way of NOT having it that way...
        //sendEvent(name: 'uptime', value: result.Uptime, displayed: false, archivable: false)
    
        state.uptime = result.Uptime
        updateDataValue('uptime', result.Uptime)
    }
    // END:  getTasmotaNewParserForBasicData()
    // BEGIN:getTasmotaNewParserForEnergyMonitor()
    // Standard Energy Monitor Data parsing
    if (result.containsKey("StatusSNS")) {
        result << result.StatusSNS
    }
    if (result.containsKey("ENERGY")) {
        //logging("Has ENERGY...", 1)
        //if (!state.containsKey('energy')) state.energy = {}
        if (result.ENERGY.containsKey("Total")) {
            logging("Total: $result.ENERGY.Total kWh",99)
            //sendEvent(name: "energyTotal", value: "$result.ENERGY.Total kWh")
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyTotal", value:"$result.ENERGY.Total kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Today")) {
            logging("Today: $result.ENERGY.Today kWh",99)
            //sendEvent(name: "energyToday", value: "$result.ENERGY.Today kWh")
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyToday", value:"$result.ENERGY.Today kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Yesterday")) {
            logging("Yesterday: $result.ENERGY.Yesterday kWh",99)
            //sendEvent(name: "energyYesterday", value: "$result.ENERGY.Yesterday kWh")
            missingChild = callChildParseByTypeId("POWER1", [[name:"energyYesterday", value:"$result.ENERGY.Yesterday kWh"]], missingChild)
        }
        if (result.ENERGY.containsKey("Current")) {
            logging("Current: $result.ENERGY.Current A",99)
            def r = (result.ENERGY.Current == null) ? 0 : result.ENERGY.Current
            //sendEvent(name: "current", value: "$r A")
            missingChild = callChildParseByTypeId("POWER1", [[name:"current", value:"$r A"]], missingChild)
        }
        if (result.ENERGY.containsKey("ApparentPower")) {
            logging("apparentPower: $result.ENERGY.ApparentPower VA",99)
            //sendEvent(name: "apparentPower", value: "$result.ENERGY.ApparentPower VA")
            missingChild = callChildParseByTypeId("POWER1", [[name:"apparentPower", value:"$result.ENERGY.ApparentPower VA"]], missingChild)
        }
        if (result.ENERGY.containsKey("ReactivePower")) {
            logging("reactivePower: $result.ENERGY.ReactivePower VAr",99)
            //sendEvent(name: "reactivePower", value: "$result.ENERGY.ReactivePower VAr")
            missingChild = callChildParseByTypeId("POWER1", [[name:"reactivePower", value:"$result.ENERGY.ReactivePower VAr"]], missingChild)
        }
        if (result.ENERGY.containsKey("Factor")) {
            logging("powerFactor: $result.ENERGY.Factor",99)
            //sendEvent(name: "powerFactor", value: "$result.ENERGY.Factor")
            missingChild = callChildParseByTypeId("POWER1", [[name:"powerFactor", value:"$result.ENERGY.Factor"]], missingChild)
        }
        if (result.ENERGY.containsKey("Voltage")) {
            logging("Voltage: $result.ENERGY.Voltage V",99)
            def r = (result.ENERGY.Voltage == null) ? 0 : result.ENERGY.Voltage
            //sendEvent(name: "voltageWithUnit", value: "$r V")
            //sendEvent(name: "voltage", value: r, unit: "V")
            missingChild = callChildParseByTypeId("POWER1", [[name:"voltageWithUnit", value:"$r V"]], missingChild)
            missingChild = callChildParseByTypeId("POWER1", [[name:"voltage", value: r, unit: "V"]], missingChild)
        }
        if (result.ENERGY.containsKey("Power")) {
            logging("Power: $result.ENERGY.Power W",99)
            def r = (result.ENERGY.Power == null) ? 0 : result.ENERGY.Power
            //sendEvent(name: "powerWithUnit", value: "$r W")
            //sendEvent(name: "power", value: r, unit: "W")
            missingChild = callChildParseByTypeId("POWER1", [[name:"powerWithUnit", value:"$r W"]], missingChild)
            missingChild = callChildParseByTypeId("POWER1", [[name:"power", value: r, unit: "W"]], missingChild)
            //state.energy.power = r
        }
    }
    // StatusPTH:[PowerDelta:0, PowerLow:0, PowerHigh:0, VoltageLow:0, VoltageHigh:0, CurrentLow:0, CurrentHigh:0]
    // END:  getTasmotaNewParserForEnergyMonitor()
    // BEGIN:getTasmotaNewParserForSensors()
    // Standard Sensor Data parsing
    // AM2301
    // BME280
    // BMP280
    //logging("result instanceof Map: ${result instanceof Map}", 1)
    for ( r in result ) {
        //logging("${r.key} instanceof Map: ${r.value instanceof Map}", 1)
        if((r.key == 'StatusSNS' || r.key == 'SENSOR') && r.value instanceof Map) {
            result << r
        }
    }
    for ( r in result ) {
        if(r.value instanceof Map && (r.value.containsKey("Temperature") ||
            r.value.containsKey("Humidity") || r.value.containsKey("Pressure") ||
            r.value.containsKey("Distance"))) {
            if (r.value.containsKey("Humidity")) {
                logging("Humidity: RH $r.value.Humidity%", 99)
                missingChild = callChildParseByTypeId(r.key, [[name: "humidity", value: r.value.Humidity, unit: "%"]], missingChild)
            }
            if (r.value.containsKey("Temperature")) {
                //Probably need this line below
                logging("Temperature: $r.value.Temperature", 99)
                String c = String.valueOf((char)(Integer.parseInt("00B0", 16)));
                missingChild = callChildParseByTypeId(r.key, [[name: "temperature", value: r.value.Temperature, unit: "$c${location.temperatureScale}"]], missingChild)
            }
            if (r.value.containsKey("Pressure")) {
                logging("Pressure: $r.value.Pressure", 99)
                String pressureUnit = "mbar"
                missingChild = callChildParseByTypeId(r.key, [[name: "pressure", value: r.value.Pressure, unit: pressureUnit]], missingChild)
                // Since there is no Pressure tile yet, we need an attribute with the unit as well... But that is NOT the responsibility of the Parent
                //missingChild = callChildParseByTypeId(r.key, [[name: "pressureWithUnit", value: "$r.value.Pressure $pressureUnit"]], missingChild)
            }
            if (r.value.containsKey("Distance")) {
                logging("Distance: $r.value.Distance cm", 99)
                def realDistance = Math.round((r.value.Distance as Double) * 100) / 100
                //sendEvent(name: "distance", value: "${realDistance}", unit: "cm")
                missingChild = callChildParseByTypeId(r.key, [[name: "distance", value: String.format("%.2f cm", realDistance), unit: "cm"]], missingChild)
            }
        }
    }
    // END:  getTasmotaNewParserForSensors()
    // BEGIN:getTasmotaNewParserForWifi()
    // Standard Wifi Data parsing
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
            // In Tasmota RSSI is actually the % already, no conversion needed...
            String quality = "${result.Wifi.RSSI}%"
            if(device.currentValue('wifiSignal') != quality) sendEvent(name: "wifiSignal", value: quality, isStateChange: false)
        }
        if (log99 == true && result.Wifi.containsKey("SSId")) {
            logging("SSId: $result.Wifi.SSId",99)
        }
    }
    // END:  getTasmotaNewParserForWifi()
    //logging("Entered parseResult 3", 100)
    updatePresence("present")
    return missingChild
}

// Call order: installed() -> configure() -> initialize() -> updated() -> updateNeededSettings()
void updateNeededSettings() {
    // BEGIN:getUpdateNeededSettingsTasmotaHeader()
    // updateNeededSettings() Generic header BEGINS here
    def currentProperties = state.currentProperties ?: [:]
    
    state.settings = settings
    
    def configuration = new XmlSlurper().parseText(configuration_model_tasmota())
    def isUpdateNeeded = "NO"
    
    if(runReset != null && runReset == 'RESET') {
        for ( e in state.settings ) {
            logging("Deleting '${e.key}' with value = ${e.value} from Settings", 50)
            // Not sure which ones are needed, so doing all...
            device.clearSetting("${e.key}")
            device.removeSetting("${e.key}")
            state?.settings?.remove("${e.key}")
        }
    }
    
    prepareDNI()
    
    // updateNeededSettings() Generic header ENDS here
    // END:  getUpdateNeededSettingsTasmotaHeader()

    // Get the Device Configuration
    if(deviceConfig == null) deviceConfig = "01generic-device"
    def deviceConfigMap = getDeviceConfiguration(deviceConfig)
    
    def deviceTemplateInput = deviceConfigMap?.template
    def moduleNumber = deviceConfigMap?.module
    if(deviceTemplateInput == "") deviceTemplateInput = null
    if(moduleNumber == "") moduleNumber = null

    if(deviceTemplateInput != null && moduleNumber == null) moduleNumber = 0

    logging("updateNeededSettings: deviceConfigMap=$deviceConfigMap, deviceTemplateInput=$deviceTemplateInput, moduleNumber=$moduleNumber", 1)

    // BEGIN:getUpdateNeededSettingsTasmotaDynamicModuleCommand()
    // Tasmota Module and Template selection command (autogenerated)
    getAction(getCommandString("Module", null))
    getAction(getCommandString("Template", null))
    if(disableModuleSelection == null) disableModuleSelection = false
    def moduleNumberUsed = moduleNumber
    if(moduleNumber == null || moduleNumber == -1) moduleNumberUsed = -1
    boolean useDefaultTemplate = false
    def defaultDeviceTemplate = ''
    if(deviceTemplateInput != null && deviceTemplateInput == "0") {
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput == null || deviceTemplateInput == "") {
        // We should use the default of the driver
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput != null) deviceTemplateInput = deviceTemplateInput.replaceAll(' ','')
    if(disableModuleSelection == false && ((deviceTemplateInput != null && deviceTemplateInput != "") ||
                                           (useDefaultTemplate && defaultDeviceTemplate != ""))) {
        def usedDeviceTemplate = defaultDeviceTemplate
        if(useDefaultTemplate == false && deviceTemplateInput != null && deviceTemplateInput != "") {
            usedDeviceTemplate = deviceTemplateInput
        }
        logging("Setting the Template (${usedDeviceTemplate}) soon...", 100)
        logging("templateData = ${device.currentValue('templateData')}", 10)
        if(usedDeviceTemplate != '') moduleNumberUsed = 0  // This activates the Template when set
        // Checking this makes installs fail: device.currentValue('templateData') != null
        if(usedDeviceTemplate != null && device.currentValue('templateData') != usedDeviceTemplate) {
            logging("The template is currently NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'", 100)
            // The NAME part of th Device Template can't exceed 14 characters! More than that and they will be truncated.
            // TODO: Parse and limit the size of NAME???
            getAction(getCommandString("Template", usedDeviceTemplate))
        } else if (device.currentValue('module') == null){
            // Update our stored value!
            getAction(getCommandString("Template", null))
        }else if (usedDeviceTemplate != null) {
            logging("The template is set to '${usedDeviceTemplate}' already!", 100)
        }
    } else {
        logging("Can't set the Template...", 10)
        logging(device.currentValue('templateData'), 10)
        //logging("deviceTemplateInput: '${deviceTemplateInput}'", 10)
        //logging("disableModuleSelection: '${disableModuleSelection}'", 10)
    }
    if(disableModuleSelection == false && moduleNumberUsed != null && moduleNumberUsed >= 0) {
        logging("Setting the Module (${moduleNumberUsed}) soon...", 100)
        logging("device.currentValue('module'): '${device.currentValue('module')}'", 10)
        // Don't filter in this case: device.currentValue('module') != null
        if(moduleNumberUsed != null && (device.currentValue('module') == null || !(device.currentValue('module').startsWith("[${moduleNumberUsed}:") || device.currentValue('module') == '0'))) {
            logging("Currently not using module ${moduleNumberUsed}, using ${device.currentValue('module')}", 100)
            getAction(getCommandString("Module", "${moduleNumberUsed}"))
        } else if (moduleNumberUsed != null && device.currentValue('module') != null){
            logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        } else if (device.currentValue('module') == null){
            // Update our stored value!
            getAction(getCommandString("Module", null))
        } else {
            logging("Module is set to '${device.currentValue('module')}', and it's set to be null, report this to the creator of this driver!",10)
        }
    } else {
        logging("Setting the Module has been disabled!", 10)
    }
    // END:  getUpdateNeededSettingsTasmotaDynamicModuleCommand()
    logging("After getUpdateNeededSettingsTasmotaDynamicModuleCommand", 1)
    // TODO: Process device-type specific settings here...

    installCommands = deviceConfigMap?.installCommands
    if(installCommands == null || installCommands == '') installCommands = []
    logging("Got to just before runInstallCommands", 1)
    runInstallCommands(installCommands)

    //
    // https://tasmota.github.io/docs/#/Commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    //getAction(getCommandString("SetOption66", "1"))

    //getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)

    // BEGIN:getUpdateNeededSettingsTasmotaFooter()
    getAction(getCommandString("TelePeriod", "${getTelePeriodValue()}"))
    // updateNeededSettings() Generic footer BEGINS here
    getAction(getCommandString("SetOption113", "1")) // Hubitat Enabled
    // Disabling Emulation so that we don't flood the logs with upnp traffic
    getAction(getCommandString("Emulation", "2")) // Hue Emulation Enabled, REQUIRED for device discovery
    getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
    logging("HubitatPort: ${device.hub.getDataValue("localSrvPortTCP")}", 1)
    getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
    getAction(getCommandString("FriendlyName1", device.displayName.take(32))) // Set to a maximum of 32 characters
    // We need the Backlog inter-command delay to be 20ms instead of 200...
    getAction(getCommandString("SetOption34", "20"))
    
    // Set the timezone
    int tzoffset = getLocation().timeZone.getOffset(now()) / 3600000
    String tzoffsetWithSign = tzoffset < 0 ? "${tzoffset}" : "+${tzoffset}"
    logging("Setting timezone to $tzoffsetWithSign", 10)
    getAction(getCommandString("Timezone", tzoffsetWithSign))
    
    // Just make sure we update the child devices
    logging("Scheduling refreshChildren...", 1)
    runIn(30, "refreshChildren")
    runIn(60, "refreshChildrenAgain")
    logging("Done scheduling refreshChildren...", 1)
    
    if(override == true) {
        sync(ipAddress)
    }
    
    //logging("Cmds: " +cmds,1)
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: false)
    // updateNeededSettings() Generic footer ENDS here
    // END:  getUpdateNeededSettingsTasmotaFooter()
}

/** Calls TO Child devices */
boolean callChildParseByTypeId(String deviceTypeId, event, boolean missingChild) {
    //logging("Before callChildParseByTypeId()", 100)
    event.each{
        if(it.containsKey("descriptionText") == false) {
            it["descriptionText"] = "'$it.name' set to '$it.value'"
        }
        it["isStateChange"] = false
    }
    // Try - Catch is expensive since it won't be optimized
    //try {
    //logging("Before getChildDevice()", 100)
    cd = getChildDevice("$device.id-$deviceTypeId")
    if(cd != null) {
        //logging("Before Child parse()", 100)
        // It takes 30 to 40ms to just call into the child device parse
        cd.parse(event)
        //logging("After Child parse()", 100)
    } else {
        // We're missing a device...
        log.warn("childParse() can't FIND the device ${cd?.displayName}! (childId: ${"$device.id-$deviceTypeId"}) Did you delete something?")
        missingChild = true
    }
    //} catch(e) {
    //    log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
    //    missingChild = true
    //}
    return missingChild
}

void childParse(cd, event) {
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
void componentRefresh(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentRefresh(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    refresh()
}

void componentOn(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    if(invertPowerNumber == true) {
        // This is used when Tasmota mixes things up with a dimmer and relay in the same device
        if(actionType == "POWER1") { 
            actionType = "POWER2"
        } else if(actionType == "POWER2"){
            actionType = "POWER1"
        }
    }
    logging("componentOn(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    getAction(getCommandString("$actionType", "1"))
    //childParse(cd, [[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on"]])
}

void componentOff(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    if(invertPowerNumber == true) {
        // This is used when Tasmota mixes things up with a dimmer and relay in the same device
        if(actionType == "POWER1") { 
            actionType = "POWER2"
        } else if(actionType == "POWER2"){
            actionType = "POWER1"
        }
    }
    logging("componentOff(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    getAction(getCommandString("$actionType", "0"))
    //childParse(cd, [[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off"]])
}

void componentSetLevel(cd, BigDecimal level) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}) actionType=$actionType", 1)
    setLevel(level)
}

void componentSetLevel(cd, BigDecimal level, BigDecimal duration) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}, duration=${duration}) actionType=$actionType", 1)
    setLevel(level, duration)
}

void componentStartLevelChange(cd, String direction) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStartLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId}), direction=${direction}) actionType=$actionType", 1)
    startLevelChange(direction)
}

void componentStopLevelChange(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStopLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    stopLevelChange()
}

void componentSetColor(cd, Map colormap) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), colormap=${colormap}) actionType=$actionType", 1)
    setColor(colormap)
}

void componentSetHue(cd, BigDecimal hue) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetHue(cd=${cd.displayName} (${cd.deviceNetworkId}), hue=${hue}) actionType=$actionType", 1)
    setHue(hue)
}

void componentWhite(cd) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentWhite(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=$actionType", 1)
    white()
}

void componentSetRGB(cd, r, g, b) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetRGB(cd=${cd.displayName} (${cd.deviceNetworkId}), r=${r}, g=${g}, b=${b}) actionType=$actionType", 1)
    setRGB(r, g, b)
}

void componentSetSaturation(cd, BigDecimal saturation) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetSaturation(cd=${cd.displayName} (${cd.deviceNetworkId}), saturation=${saturation}) actionType=$actionType", 1)
    setSaturation(saturation)
}

void componentSetColorTemperature(cd, BigDecimal colortemperature) {
    String actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColorTemperature(cd=${cd.displayName} (${cd.deviceNetworkId}), colortemperature=${colortemperature}) actionType=$actionType", 1)
    setColorTemperature(colortemperature)
}

void componentSetEffect(cd, BigDecimal effectnumber, BigDecimal speed) {
    modeSet((Integer) effectnumber, speed)
}

void componentModeWakeUp(cd, BigDecimal wakeUpDuration, BigDecimal level) {
    modeWakeUp(wakeUpDuration, level)
}

void componentSetSpeed(cd, String fanspeed) {
    String fanCommand = "Dimmer"
    String cModule = device.currentValue('module')
    if(cModule != null && (cModule.startsWith('[44') == true || cModule.startsWith('[71') == true)) {
        fanCommand = "FanSpeed"
    }
    switch(fanspeed) {
        case "off":
            getAction(getCommandString(fanCommand, "0"))
            break
        case "on":
        case "low":
            getAction(getCommandString(fanCommand, "1"))
            break
        case "medium-low":
        case "medium":  
            getAction(getCommandString(fanCommand, "2"))
            break
        case "medium-high":
        case "high":
            getAction(getCommandString(fanCommand, "3"))
            break
    }  
}

void componentOpen(cd) {
    //TODO: Get this command from the device config!
    getAction(getCommandString("TuyaSend4", "101,0"))
}

void componentClose(cd) {
    getAction(getCommandString("TuyaSend4", "101,2"))
}

void componentStop(cd) {
    getAction(getCommandString("TuyaSend4", "101,1"))
}

void componentSetPosition(cd, BigDecimal position) {
    // This is run in the child for now...
}

void componentSetColorByRGBString(cd, String colorRGB) {
    setColorByRGBString(colorRGB)
}

void componentSetPixelColor(cd, String colorRGB, BigDecimal pixel) {
    setPixelColor(colorRGB, pixel)
}

void componentSetAddressablePixels(cd, BigDecimal pixels) {
    setAddressablePixels(pixels)
}

void componentSetAddressableRotation(cd, BigDecimal pixels) {
    setAddressableRotation(pixels)
}

void componentSetEffectWidth(cd, BigDecimal pixels) {
    setEffectWidth(pixels)
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothings to edit here, move along! --------------------------------------
 * -----------------------------------------------------------------------------
 */

// BEGIN:getDefaultFunctions()
/* Default Driver Methods go here */
private String getDriverVersion() {
    comment = ""
    if(comment != "") state.comment = comment
    String version = "v1.0.2.0503T"
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
/* Logging function included in all drivers */
private boolean logging(message, level) {
    boolean didLogging = false
    //Integer logLevelLocal = (logLevel != null ? logLevel.toInteger() : 0)
    //if(!isDeveloperHub()) {
    Integer logLevelLocal = 0
    if (infoLogging == null || infoLogging == true) {
        logLevelLocal = 100
    }
    if (debugLogging == true) {
        logLevelLocal = 1
    }
    //}
    if (logLevelLocal != 0){
        switch (logLevelLocal) {
        case -1: // Insanely verbose
            if (level >= 0 && level < 100) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case 1: // Very verbose
            if (level >= 1 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case 10: // A little less
            if (level >= 10 && level < 99) {
                log.debug "$message"
                didLogging = true
            } else if (level == 100) {
                log.info "$message"
                didLogging = true
            }
        break
        case 50: // Rather chatty
            if (level >= 50 ) {
                log.debug "$message"
                didLogging = true
            }
        break
        case 99: // Only parsing reports
            if (level >= 99 ) {
                log.debug "$message"
                didLogging = true
            }
        break
        
        case 100: // Only special debug messages, eg IR and RF codes
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


// Don't need this include anymore:
//#include:getHelperFunctions('all-debug')

/**
 * ALL DEFAULT METHODS (helpers-all-default)
 *
 * Helper functions included in all drivers/apps
 */

boolean isDriver() {
    try {
        // If this fails, this is not a driver...
        getDeviceDataByName('_unimportant')
        logging("This IS a driver!", 0)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 0)
        return false
    }
}

void deviceCommand(cmd) {
    def jsonSlurper = new JsonSlurper()
    cmd = jsonSlurper.parseText(cmd)
    logging("deviceCommand: ${cmd}", 0)
    r = this."${cmd['cmd']}"(*cmd['args'])
    logging("deviceCommand return: ${r}", 0)
    updateDataValue('appReturn', JsonOutput.toJson(r))
}

void setLogsOffTask(boolean noLogWarning=false) {
    // disable debug logs after 30 min, unless override is in place
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

/*
	initialize

	Purpose: initialize the driver/app
	Note: also called from updated()
    This is called when the hub starts, DON'T declare it with return as void,
    that seems like it makes it to not run? Since testing require hub reboots
    and this works, this is not conclusive...
*/
// Call order: installed() -> configure() -> updated() -> initialize()
def initialize() {
    logging("initialize()", 100)
	unschedule("updatePresence")
    setLogsOffTask()
    try {
        // In case we have some more to run specific to this driver/app
        initializeAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    refresh()
}

/**
 * Automatically disable debug logging after 30 mins.
 *
 * Note: scheduled in Initialize()
 */
void logsOff() {
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
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
            //app.clearSetting("logLevel")
            // To be able to update the setting, it has to be removed first, clear does NOT work, at least for Apps
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

/*
    General Mathematical and Number Methods
*/
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
    logging("hexToASCII: ${output.toString()}", 0)
    return output.toString()
}

/**
 * --END-- ALL DEFAULT METHODS (helpers-all-default)
 */

/**
 * DRIVER METADATA METHODS (helpers-driver-metadata)
 *
 * These methods are to be used in (and/or with) the metadata section of drivers and
 * is also what contains the CSS handling and styling.
 */

// These methods can be executed in both the NORMAL driver scope as well
// as the Metadata scope.
private Map getMetaConfig() {
    // This method can ALSO be executed in the Metadata Scope
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

// These methods are used to set which elements to hide. 
// They have to be executed in the NORMAL driver scope.
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
        //logging("setSomethingToHide 1 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
        if(metaConfig["hide"].containsKey(type)) {
            //logging("setSomethingToHide 1 hasKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type].addAll(something)
        } else {
            //logging("setSomethingToHide 1 noKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type] = something
        }
        //metaConfig["hide"]["$type"] = oldData
        //logging("setSomethingToHide 2 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
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

// These methods are for executing inside the metadata section of a driver.
def metaDataExporter() {
    //log.debug "getEXECUTOR_TYPE = ${getEXECUTOR_TYPE()}"
    List filteredPrefs = getPreferences()['sections']['input'].name[0]
    //log.debug "filteredPrefs = ${filteredPrefs}"
    if(filteredPrefs != []) updateDataValue('preferences', "${filteredPrefs}".replaceAll("\\s",""))
}

// These methods are used to add CSS to the driver page
// This can be used for, among other things, to hide Commands
// They HAVE to be run in getDriverCSS() or getDriverCSSWrapper()!

/* Example usage:
r += getCSSForCommandsToHide(["off", "refresh"])
r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
r += getCSSForDatasToHide(["preferences", "appReturn"])
r += getCSSToChangeCommandTitle("configure", "Run Configure2")
r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
r += getCSSForHidingLastPreference()
r += '''
form[action*="preference"]::before {
    color: green;
    content: "Hi, this is my content"
}
form[action*="preference"] div.mdl-grid div.mdl-cell:nth-of-type(2) {
    color: green;
}
form[action*="preference"] div[for^=preferences] {
    color: blue;
}
h3, h4, .property-label {
    font-weight: bold;
}
'''
*/

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
            // We always need to hide this element when we use CSS
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
    //log.debug "getCommandIndex: Seeing these commands: '${commands}', index=$i}"
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
    //log.debug "getStateVariableIndex: Seeing these State Variables: '${stateVariables}', index=$i}"
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
    //log.debug "getDataIndex: Seeing these Data Keys: '${datas}', index=$i}"
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
    //log.debug "getPreferenceIndex: Seeing these Preferences first: '${filteredPrefs}'"
    if(filteredPrefs == [] || filteredPrefs == null) {
        d = getDataValue('preferences')
        //log.debug "getPreferenceIndex: getDataValue('preferences'): '${d}'"
        if(d != null && d.length() > 2) {
            try{
                filteredPrefs = d[1..d.length()-2].tokenize(',')
            } catch(e) {
                // Do nothing
            }
        }
        

    }
    Integer i = 0
    if(returnMax == true) {
        i = filteredPrefs.size()
    } else {
        i = filteredPrefs.findIndexOf{ "$it" == preference}+1
    }
    //log.debug "getPreferenceIndex: Seeing these Preferences: '${filteredPrefs}', index=$i"
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

/**
 * --END-- DRIVER METADATA METHODS (helpers-driver-metadata)
 */

/**
 * STYLING (helpers-styling)
 *
 * Helper functions included in all Drivers and Apps using Styling
 */
String addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

String addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

String makeTextBold(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

String makeTextItalic(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

String getDefaultCSS(boolean includeTags=true) {
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

/**
 * --END-- STYLING METHODS (helpers-styling)
 */

/**
 * DRIVER DEFAULT METHODS (helpers-driver-default)
 *
 * General Methods used in ALL drivers except some CHILD drivers
 * Though some may have no effect in some drivers, they're here to
 * maintain a general structure
 */

// Since refresh, with any number of arguments, is accepted as we always have it declared anyway, 
// we use it as a wrapper
// All our "normal" refresh functions take 0 arguments, we can declare one with 1 here...
void refresh(cmd) {
    deviceCommand(cmd)
}
// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
// Calls installed() -> [configure() -> [updateNeededSettings(), updated() -> [updatedAdditional(), initialize() -> refresh() -> refreshAdditional()], installedAdditional()]
def installed() {
	logging("installed()", 100)
    try {
        // Used by certain types of drivers, like Tasmota Parent drivers
        installedPreConfigure()
    } catch (MissingMethodException e) {
        // ignore
    }
	//configure()
    try {
        // In case we have some more to run specific to this Driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
def configure() {
    logging("configure()", 100)
    if(isDriver()) {
        // Do NOT call updateNeededSettings() here!
        try {
            return configureAdditional()
        } catch (MissingMethodException e) {
            updated()
        }
        try {
            // Run the getDriverVersion() command
            getDriverVersion()
        } catch (MissingMethodException e) {
            // ignore
        }
    }
}

void configureDelayed() {
    runIn(10, "configure")
    runIn(30, "refresh")
}

/**
 * --END-- DRIVER DEFAULT METHODS (helpers-driver-default)
 */

/*
    CHILD DEVICES METHODS (helpers-childDevices)

    Helper functions included when using Child devices

    NOTE: MUCH of this is not in use any more, needs cleaning...
*/

// Get the button number
private channelNumber(String dni) {
    def ch = dni.split("-")[-1] as Integer
    return ch
}

def childOn(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(1, channelNumber(dni))
}

def childOff(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(0, channelNumber(dni))
}

private childSendState(String currentSwitchNumber, String state) {
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${currentSwitchNumber}")}
    if (childDevice) {
        logging("childDevice.sendEvent ${currentSwitchNumber} ${state}",1)
        childDevice.sendEvent(name: "switch", value: state, type: type)
    } else {
        logging("childDevice.sendEvent ${currentSwitchNumber} is missing!",1)
    }
}

private areAllChildrenSwitchedOn(Integer skip = 0) {
    def children = getChildDevices()
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

private sendParseEventToChildren(data) {
    def children = getChildDevices()
    children.each {child->
        child.parseParentData(data)
    }
    return status
}

private void createChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("createChildDevices: creating $numSwitchesI device(s)",1)
    
    // If making changes here, don't forget that recreateDevices need to have the same settings set
    for (i in 1..numSwitchesI) {
        // https://community.hubitat.com/t/composite-devices-parent-child-devices/1925
        // BEGIN:getCreateChildDevicesCommand()
        try {
        addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                    log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                }
        // END:  getCreateChildDevicesCommand()
    }
}

def recreateChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("recreateChildDevices: recreating $numSwitchesI device(s)",1)
    def childDevice = null

    for (i in 1..numSwitchesI) {
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
        if (childDevice) {
            // The device exists, just update it
            childDevice.setName("${getDeviceInfoByName('name')} #$i")
            childDevice.setDeviceNetworkId("$device.id-$i")  // This doesn't work right now...
            logging(childDevice.getData(), 10)
            // We leave the device Label alone, since that might be desired by the user to change
            //childDevice.setLabel("$device.displayName $i")
            //.setLabel doesn't seem to work on child devices???
        } else {
            // No such device, we should create it
            // BEGIN:getCreateChildDevicesCommand()
            try {
            addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                    } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                        log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                    }
            // END:  getCreateChildDevicesCommand()
        }
    }
    if (numSwitchesI < 4) {
        // Check if we should delete some devices
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

def deleteChildren() {
	logging("deleteChildren()", 100)
	def children = getChildDevices()
    
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }
}

/**
 *   --END-- CHILD DEVICES METHODS (helpers-childDevices)
 */

/**
 * TASMOTA METHODS (helpers-tasmota)
 *
 * Helper functions included in all Tasmota drivers
 */

// Calls installed() -> installedPreConfigure()
void installedPreConfigure() {
    // This is run FIRST in installed()
    if(isDriver()) {
        // This is only run ONCE after install
        
        logging("Inside installedPreConfigure()", 1)
        logging("Password: ${decrypt(getDataValue('password'))}", 1)
        String pass = decrypt(getDataValue('password'))
        if(pass != null && pass != "" && pass != "[installed]") {
            device.updateSetting("password", [value: pass, type: "password"])
        }
        device.updateSetting("deviceConfig", [type: "enum", value:getDataValue('deviceConfig')])
    }
}

// Call order: installed() -> configure() -> updated() 
void updated() {
    logging("updated()", 10)
    if(isDriver()) {
        logging("before updateNeededSettings()", 10)
        updateNeededSettings()
        logging("after updateNeededSettings()", 10)
        //sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
        //sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: false)
    }
    try {
        // Also run initialize(), if it exists...
        initialize()
        updatedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

void refreshChildren() {
    logging("refreshChildren()", 1)
    getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
}

void refreshChildrenAgain() {
    logging("refreshChildrenAgain()", 1)
    refreshChildren()
}

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
void refresh() {
	logging("refresh()", 100)
    def metaConfig = null
    if(isDriver()) {
        // Clear all old state variables, but ONLY in a driver!
        state.clear()

        // Retrieve full status from Tasmota
        getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
        getDriverVersion()

        updateDataValue('namespace', getDeviceInfoByName('namespace'))

        //logging("this.binding.variables = ${this.binding.variables}", 1)
        //logging("settings = ${settings}", 1)
        //logging("getDefinitionData() = ${getDefinitionData()}", 1)
        //logging("getPreferences() = ${getPreferences()}", 1)
        //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
        //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
        
        /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
        metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
        metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
        metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/

        // This should be the first place we access metaConfig here, so clear and reset...
        metaConfig = clearThingsToHide()
        metaConfig = setCommandsToHide([], metaConfig=metaConfig)
        metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
            'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
        
        metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
        //metaConfig = setDatasToHide(['preferences', 'namespace', 'appReturn', 'metaConfig'], metaConfig=metaConfig)
        metaConfig = setDatasToHide(['namespace', 'appReturn', 'password'], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    }
    try {
        // In case we have some more to run specific to this driver
        refreshAdditional(metaConfig)
    } catch (MissingMethodException e1) {
        // ignore
        try {
            // In case we have some more to run specific to this driver
            refreshAdditional()
        } catch (MissingMethodException e2) {
            // ignore
        }
    }
}

void reboot() {
	logging("reboot()", 10)
    getAction(getCommandString("Restart", "1"))
}

/*
    // Stress-testing runInstallCommands() 
    installCommands = []
    installCommands.add(["rule1", 'ON Var1#Value DO Var4 0; ENDON'])
    installCommands.add(["rule2", 'ON Var2#Value DO Var4 0; ENDON'])
    installCommands.add(["rule3", 'ON Var3#Value DO Var4 0; ENDON'])
    installCommands.add(["var1", "0"])
    installCommands.add(["var2", "0"])
    installCommands.add(["var3", "0"])
    (1..8).each {
        installCommands.add(["rule1", "+ ON Var1#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule2", "+ ON Var2#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule3", "+ ON Var3#Value DO Var4 $it; ENDON"])
        installCommands.add(["add1", "1"])
        installCommands.add(["add2", "1"])
        installCommands.add(["add3", "1"])
    }
    installCommands.add(["rule1", '0'])
    installCommands.add(["rule2", '0'])
    installCommands.add(["rule3", '0'])
    logging("refreshAdditional installCommands=$installCommands", 1)
    runInstallCommands(installCommands)
*/
void runInstallCommands(installCommands) {
    // Runs install commands as defined in helpers-device-configurations
    // Called from updateNeededSettings() in parent drivers
    logging("runInstallCommands(installCommands=$installCommands)", 1)
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

    // Backlog inter-command delay in milliseconds
    //getAction(getCommandString("SetOption34", "20"))
    pauseExecution(100)
    // Maximum 30 commands per backlog call
    while(backlogs.size() > 0) {
        getAction(getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        // If we run this too fast Tasmota can't keep up, 1000ms is enough when 20ms between commands...
        if(backlogs.size() > 0) pauseExecution(1000)
        // REALLY don't use pauseExecution often... NOT good for performance...
    }

    [rule1, rule2, rule3].each {
        //logging("rule: $it", 1)
        it.each {rule->
            // Rules can't run in backlog!
            getAction(getCommandString(rule["command"], rule["value"]))
            //logging("cmd=${rule["command"]}, value=${rule["value"]}", 1)
            pauseExecution(100)
            // REALLY don't use pauseExecution often... NOT good for performance...
        }
    }
    //getAction(getCommandString("SetOption34", "200"))
}

void updatePresence(String presence) {
    // presence - ENUM ["present", "not present"]
    logging("updatePresence(presence=$presence)", 1)
    Integer timeout = getTelePeriodValue()
    timeout += (timeout * 1.1 > 120 ? Math.round(timeout * 1.1) : 120) + 60
    String descriptionText = "No update received from the Tasmota device for ${timeout} seconds..."
    if(presence == "present") {    
        descriptionText = "Device is available"
        //log.warn "Setting as present with timeout: $timeout"
        runIn(timeout, "updatePresence", [data: "not present"])
    } else {
        log.warn "Presence time-out reached, setting device as 'not present'!"
    }
    sendEvent(name: "presence", value: presence, isStateChange: false, descriptionText: descriptionText)
}

Map parseDescriptionAsMap(description) {
    // Used by parse(description) to get descMap
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) { 
            map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        } else {
            map += [(nameAndValue[0].trim()):""]
        }
	}
}

private getAction(uri, callback="parse") { 
    logging("Using getAction for '${uri}'...", 0)
    httpGetAction(uri, callback=callback)
}

def parse(asyncResponse, data) {
    // Parse called by default when using asyncHTTP
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() = \"${asyncResponse.getJson()}\")", 100)
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
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}


/*
    Methods related to configureChildDevices()

    configureChildDevices() detects which child devices to create/update and does the creation/updating
*/

void parseConfigureChildDevices(asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            configureChildDevices(asyncResponse, data)
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
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

boolean containsKeyInSubMap(aMap, key) {
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

Integer numOfKeyInSubMap(aMap, String key) {
    Integer numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
            // Do nothing
        }
    }
    return numKeys
}

Integer numOfKeysIsMap(aMap) {
    Integer numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}

void configureChildDevices(asyncResponse, data) {
    // This detects which child devices to create/update and does the creation/updating
    def statusMap = asyncResponse.getJson()
    logging("configureChildDevices() statusMap=$statusMap", 1)
    // Use statusMap to determine which Child Devices we should create

    // The built-in Generic Components are:
    //
    // Acceleration Sensor  - ID: 189
    // Button Controller    - ID: 1029
    // Central Scene Dimmer - ID: 912
    // Central Scene Switch - ID: 913
    // Contact Sensor       - ID: 192
    // Contact/Switch       - ID: 199
    // CT                   - ID: 198
    // Dimmer               - ID: 187
    // Metering Switch      - ID: 188
    // Motion Sensor        - ID: 197
    // RGB                  - ID: 195
    // RGBW                 - ID: 191
    // Smoke Detector       - ID: 196
    // Switch               - ID: 190
    // Temperature Sensor   - ID: 200
    // Water Sensor         - ID: 194
    

    // {"StatusSTS":{"Time":"2020-01-26T01:13:27","Uptime":"15T02:59:27","UptimeSec":1306767,
    // "Heap":26,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER1":"OFF",
    // "POWER2":"OFF","POWER3":"OFF","POWER4":"OFF","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:CF:11","Channel":1,"RSSI":62,"LinkCount":37,"Downtime":"0T00:05:48"}}}

    // With a dimmer:
    // {"StatusSTS":{"Time":"2020-01-26T11:58:10","Uptime":"0T00:01:20","UptimeSec":80,"Heap":26,
    // "SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER":"OFF","Dimmer":0,
    // "Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB+CW+WW light:
    // {"StatusSTS":{"Time":"2020-01-26T12:07:57","Uptime":"0T00:06:58","UptimeSec":418,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF62","HSBColor":"0,0,0","Channel":[0,0,0,100,38],"CT":250,"Scheme":0,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":96,"LinkCount":1,"Downtime":"0T00:00:06"}}}
    

    // With an RGB+W light:
    // {"StatusSTS":{"Time":"2020-01-26T12:11:56","Uptime":"0T00:00:26","UptimeSec":26,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF","HSBColor":"0,0,0","Channel":[0,0,0,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":90,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB light:
    // {"StatusSTS":{"Time":"2020-01-26T12:14:15","Uptime":"0T00:00:19","UptimeSec":19,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FFFFFF","HSBColor":"0,0,100","Channel":[100,100,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":98,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW+WW ("CT" is available):
    // {"StatusSTS":{"Time":"2020-01-26T12:16:48","Uptime":"0T00:00:17","UptimeSec":17,"Heap":28,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FF62","HSBColor":"0,0,0","Channel":[100,38],"CT":250,"Fade":"ON","Speed":10,
    // "LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":94,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW or WW (PWM1 configured on the correct pin), just the same as a normal dimmer...
    // {"StatusSTS":{"Time":"2020-01-26T12:19:51","Uptime":"0T00:01:15","UptimeSec":75,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":71,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":88,"LinkCount":1,"Downtime":"0T00:00:25"}}}

    // Addressable RGB light (has the attribute "Width")
    // {"StatusSNS":{"Time":"2020-01-26T12:57:30","SR04":{"Distance":8.579}}}
    // {"StatusSTS":{"Time":"2020-01-26T12:57:30","Uptime":"0T00:02:14","UptimeSec":134,"Heap":21,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":113,"MqttCount":0,"POWER1":"ON","POWER2":"ON",
    // "Dimmer":100,"Color":"00FF00","HSBColor":"120,100,100","Channel":[0,100,0],"Scheme":13,
    // "Width":2,"Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"Signal":-40,"LinkCount":1,
    // "Downtime":"0T00:00:09"}}}

    // {"StatusSNS":{"Time":"2020-01-26T01:24:16","BMP280":{"Temperature":23.710,"Pressure":1017.6},
    // "PressureUnit":"hPa","TempUnit":"C"}}

    // Multiple temperature sensors:
    // {"Time":"2020-01-26T17:45:30","DS18B20-1":{"Id":"000008BD38BF","Temperature":26.1},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":25.1},"DS18B20-3":{"Id":"000008C02C3A",
    // "Temperature":25.3},"TempUnit":"C"}
    
    // For DS18B20, us ID to distinguish them? Then you can't replace them...
    // For AM2301 the GPIO used is appended.
    // {"StatusSNS":{"Time":"2020-01-26T20:54:10","DS18B20-1":{"Id":"000008BD38BF","Temperature":25.8},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":24.7},"DS18B20-3":{"Id":"000008C02C3A","Temperature":24.9},
    // "AM2301-12":{"Temperature":25.1,"Humidity":66.4},"AM2301-14":{"Temperature":null,"Humidity":null},"TempUnit":"C"}}

    // D5 = GPIO14
    // D6 = GPIO12
    // D7 = GPIO13

    // Distance Sensor
    // {"StatusSNS":{"Time":"2020-01-26T13:52:19","SR04":{"Distance":11.667}}}

    // {"NAME":"ControlRGBWWCW","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}

    // result: [Time:2020-01-30T11:30:43, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // result: [Time:2020-01-30T11:31:12, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.0], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // [hasEnergy:false, numTemperature:5, numHumidity:2, numPressure:0, numDistance:0, sensorMap:[AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4]], numSwitch:0, isDimmer:false, isAddressable:false, isRGB:false, hasCT:false]

    // SENSOR = {"Time":"2020-01-30T19:15:08","SR04":{"Distance":73.702}}

    // Switch or Metering Switch are the two most likely ones
    def deviceInfo = [:]
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
        deviceInfo["sensorMap"] = getKeysWithMapAndId(sns)
        // Energy is the only one that doesn't belong... Just remove it...
        deviceInfo["sensorMap"].remove("ENERGY")
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = numOfKeyInSubMap(sns, "Distance")
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
            // This only exist if there is ONLY one switch/bulb
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
    }
    logging("Device info found: $deviceInfo", 100)

    // Create the devices, if needed

    // Switches
    def driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
    def namespace = "tasmota"
    if(deviceInfo["numSwitch"] > 0) {
        /*if(deviceInfo["numSwitch"] > 1 && (
            deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
            deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                // This is supported now ;)
                // log.warn "There's more than one switch and the device is either dimmable, addressable, RGB or has CT capability. This is not fully supported yet, please report which device and settings you're using to the developer so that a solution can be found."

        }*/
        if(deviceInfo["hasEnergy"]  == true && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                // TODO: Make a Component Dimmer with Metering
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
            def childId = "POWER$i"
            def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
            def childLabel = "${getMinimizedDriverName(device.getLabel())} ($i)"
            logging("createChildDevice: POWER$i", 1)
            createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            // Once the first switch is created we only support one type... At least for now...
            driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
        }
    }
    
    // Fan Control
    if(deviceInfo["hasFanControl"] == true) {
        logging("hasFanControl", 0)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Fan Control (Child)"]
        def childId = "FAN"
        def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    // Sensors
    logging("Available in sensorMap: ${deviceInfo["sensorMap"]}, size:${deviceInfo["numSensorGroups"]}", 0)
    deviceInfo["sensorMap"].each {
        logging("sensorMap: $it.key", 0)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multi Sensor (Child)"]
        def childId = "${it.key}"
        def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }
    //logging("After sensor creation...", 0)
    // Finally let the default parser have the data as well...
    parseResult(statusMap)
}

String getChildDeviceNameRoot(boolean keepType=false) {
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

String getMinimizedDriverName(String driverName) {
    // Remove parts we don't need from the string 
    logging("getMinimizedDriverName(driverName=$driverName)", 1)
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
    // Just replace all Occurrances of Parent
    driverName = driverName.replaceAll("(?i) \\(parent\\)", "").replaceAll("(?i) parent", "").replaceAll("(?i)parent", "")
    logging("driverName: $driverName", 1)

    // Remove IP as well
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
    
    logging("getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

def getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    logging("createChildDevice(namespace=$namespace, driverName=$driverName, childId=$childId, childName=$childName, childLabel=$childLabel)", 1)
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        // If this driver was used to replace an "old" parent driver, rename the child Network ID
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice) {
        // The device exists, just update it
        childDevice.setName(childName)
        //Setting isComponent to false doesn't change how the device is treated...
        //childDevice.updateDataValue('isComponent', "false")
        logging("childDevice.getData(): ${childDevice.getData()}", 1)
    } else {
        logging("The child device doesn't exist, create it...", 0)
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

/*
    Tasmota IP Settings and Wifi status
*/
private String setDeviceNetworkId(String macOrIP, boolean isIP = false) {
    String myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
        logging("About to convert ${macOrIP}...", 0)
        myDNI = convertIPtoHex(macOrIP)
    }
    logging("Device Network Id should be set to ${myDNI} from ${macOrIP}", 0)
    return myDNI
}

void prepareDNI() {
    // Called from updateNeededSettings() and parse(description)
    if (useIPAsID) {
        def hexIPAddress = setDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            updateDNI()
        }
    } else if (state.mac != null && state.dni != state.mac) { 
        state.dni = setDeviceNetworkId(state.mac)
        updateDNI()
    }
}

private void updateDNI() {
    // Called from:
    // prepareDNI()
    // httpGetAction(uri, callback="parse")
    // postAction(uri, data)
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
        logging("Device Network Id will be set to ${state.dni} from ${device.deviceNetworkId}", 0)
        device.deviceNetworkId = state.dni
    }
}

Integer getTelePeriodValue() {
    // Naming this getTelePeriod() will cause Error 500 and other unexpected behavior when
    // telePeriod isn't set to anything...
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private String getHostAddress() {
    Integer port = 80
    if (getDeviceDataByName("port") != null) {
        port = getDeviceDataByName("port").toInteger()
    }
    if (override == true && ipAddress != null){
        // Preferences
        return "${ipAddress}:$port"
    } else if(device.currentValue("ip") != null) {
        // Current States
        return "${device.currentValue("ip")}:$port"
    } else if(getDeviceDataByName("ip") != null) {
        // Data Section
        return "${getDeviceDataByName("ip")}:$port"
    } else {
        // There really is no fallback here, if we get here, something went WRONG, probably with the DB...
        log.warn "getHostAddress() failed and ran out of fallbacks! If this happens, contact the developer, this is an \"impossible\" scenario!"
	    return "127.0.0.1:$port"
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        logging("Got this IP in hex: ${hex}", 0)
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

private String getFirstTwoIPBytes(ipAddress) {
    String ipStart = null
    if(ipAddress != null) {
        ipStart = ipAddress.tokenize( '.' ).take(2).join('.') + '.'
        logging("Got these IP bytes: ${ipStart}", 0)
    } else {
        ipStart = ''
    }
    return ipStart
}

void sync(String ip, Integer port = null) {
    String existingIp = getDataValue("ip")
    String existingPort = getDataValue("port")
    logging("Running sync()", 1)
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

Integer dBmToQuality(Integer dBm) {
    // In Tasmota RSSI is actually % already, so just returning the received value here
    // Keeping this around if this behavior changes
    /*Integer quality = 0
    if(dBm > 0) dBm = dBm * -1
    if(dBm <= -100) {
        quality = 0
    } else if(dBm >= -50) {
        quality = 100
    } else {
        quality = 2 * (dBm + 100)
    }
    logging("DBM: $dBm (${quality}%)", 0)*/
    return dBm
}

/*
    HTTP Tasmota API Related
*/
private void httpGetAction(String uri, callback="parse") { 
  updateDNI()
  
  def headers = getHeader()
  logging("Using httpGetAction for 'http://${getHostAddress()}$uri'...", 100)
  try {
    /*hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: uri,
        headers: headers
    )*/
    asynchttpGet(
        callback,
        [uri: "http://${getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in httpGetAction(uri): $e ('$uri')"
  }
}

private postAction(String uri, String data) { 
  updateDNI()

  def headers = getHeader()

  def hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

void sendCommand(String command) {
    sendCommand(command, null)
}

void sendCommand(String command, String argument) {
    String descriptionText = "${command}${argument != null ? " " + argument : ""}"
    logging("sendCommand: $descriptionText", 100)
    sendEvent(name: "commandSent", value: command, descriptionText: descriptionText, isStateChange: true)
    getAction(getCommandString(command, argument), callback="sendCommandParse")
}

def sendCommandParse(asyncResponse, data) {
    // Parse called using sendCommand
    if(asyncResponse != null) {
        try{
            def r = asyncResponse.getJson()
            logging("sendCommandParse(asyncResponse.getJson() = \"${r}\")", 100)
            sendEvent(name: "commandResult", value: asyncResponse.getData(), isStateChange: true)
            parseResult(r)
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
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

String getCommandString(String command, String value) {
    def uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${urlEscape(password)}&"
    }
	if (value != null && value != "") {
		uri += "cmnd=${urlEscape(command)}%20${urlEscape(value)}"
	}
	else {
		uri += "cmnd=${urlEscape(command)}"
	}
    return uri
}

String getMultiCommandString(commands) {
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
          uri += "${urlEscape(cmd['command'])}%20${urlEscape(cmd['value'])}%3B%20"
        } else {
          uri += "${urlEscape(cmd['command'])}%3B%20"
        }
    }
    return uri
}

private String urlEscape(String url) {
    //logging("urlEscape(url = $url)", 1)
    return(URLEncoder.encode(url).replace("+", "%20").replace("#", "%23"))
}

private String convertPortToHex(Integer port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private encodeCredentials(String username, String password) {
	String userpassascii = "${username}:${password}"
    String userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private Map getHeader(String userpass = null) {
    Map headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

/**
 * --END-- TASMOTA METHODS (helpers-tasmota)
 */

/**
 * RGBW METHODS (helpers-rgbw)
 *
 * Helper functions included in all drivers using RGB, RGBW or Dimmers
 * These methods are NOT specific to Tasmota
 */
void setColor(value) {
    logging("setColor('${value}')", 10)
	if (value != null && value instanceof Map) {
        def h = value.containsKey("hue") ? value.hue : 0
        def s = value.containsKey("saturation") ? value.saturation : 0
        def b = value.containsKey("level") ? value.level : 0
        setHSB(h, s, b)
    } else {
        logging("setColor('${value}') called with an INVALID argument!", 10)
    }
}

void setHue(h) {
    logging("setHue('${h}')", 10)
    setHSB(h, null, null)
}

void setSaturation(s) {
    logging("setSaturation('${s}')", 10)
    setHSB(null, s, null)
}

void setLevel(b) {
    logging("setLevel('${b}')", 10)
    //return(setHSB(null, null, b))
    setLevel(b, 0)
}

def rgbToHSB(red, green, blue) {
    // All credits for this function goes to Joe Julian (joejulian):
    // https://gist.github.com/joejulian/970fcd5ecf3b792bc78a6d6ebc59a55f
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
        //logging("h1 = $h1, h2 = $h2, (1 + h1 - h2) = ${(1 + h1 - h2)}", 1)
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
    
    // Convert hue to Hubitat value:
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

String getColorNameFromTemperature(Integer colorTemperature){
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

String getColorNameFromHueSaturation(Integer hue, Integer saturation=null){
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

// Fixed colours
void white() {
    logging("white()", 10)
    // This is separated to be able to reuse functions between platforms
    whiteForPlatform()
}

void red() {
    logging("red()", 10)
    setRGB(255, 0, 0)
}

void green() {
    logging("green()", 10)
    setRGB(0, 255, 0)
}

void blue() {
    logging("blue()", 10)
    setRGB(0, 0, 255)
}

void yellow() {
    logging("yellow()", 10)
    setRGB(255, 255, 0)
}

void cyan() {
    logging("cyan()", 10)
    setRGB(0, 255, 255)
}

void pink() {
    logging("pink()", 10)
    setRGB(255, 0, 255)
}

/**
 * --END-- RGBW METHODS (helpers-rgbw)
 */

/**
 * TASMOTA RGBW METHODS (helpers-tasmota-rgbw)
 *
 * Helper functions included in all Tasmota drivers using RGB, RGBW or Dimmers
 * These methods ARE specific to Tasmota
 */
void setColorTemperature(value) {
    logging("setColorTemperature('${value}')", 10)
    if(device.currentValue('colorTemperature') != value ) sendEvent(name: "colorTemperature", value: value)
    // 153..500 = set color temperature from 153 (cold) to 500 (warm) for CT lights
    // Tasmota use mired to measure color temperature
    Integer t = value != null ?  (value as Integer) : 0
    // First make sure we have a Kelvin value we can more or less handle
    // 153 mired is approx. 6536K
    // 500 mired = 2000K
    if(t > 6536) t = 6536
    if(t < 2000) t = 2000
    t = Math.round(1000000/t)
    if(t < 153) t = 153
    if(t > 500) t = 500
    state.mired = t
    state.hue = 0
    state.saturation = 0
    state.colorMode = "CT"
    //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
    logging("setColorTemperature('${t}') ADJUSTED to Mired", 10)
    getAction(getCommandStringWithModeReset("CT", "${t}"))
}

void setHSB(h, s, b) {
    logging("setHSB('${h}','${s}','${b}')", 10)
    setHSB(h, s, b, true)
}

void setHSB(h, s, b, callWhite) {
    logging("setHSB('${h}','${s}','${b}', callWhite=${String.valueOf(callWhite)})", 10)
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
        logging("ADJUSTED setHSB('${h}','${s}','${b}'", 1)
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
        //state.colorMode = "white"
        //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
        white()
        //getAction(getCommandString("hsbcolor", hsbcmd))
    } else {
        //if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
        if(useAlternateColorCommand == true) {
            def rgbval = hubitat.helper.ColorUtils.hsvToRGB([h, s, b])
            setRGB(rgbval[0], rgbval[1], rgbval[2])
        } else {
            getAction(getCommandStringWithModeReset("HsbColor", hsbcmd))
        }
    }
}

void setRGB(r, g, b) {   
    logging("setRGB('${r}','${g}','${b}')", 10)
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
        logging("ADJUSTED setRGB('${r}','${g}','${b}')", 1)
    }
    //String rgbcmd = "${r},${g},${b}"
    String rgbcmd = hubitat.helper.ColorUtils.rgbToHEX([r, g, b])
    logging("rgbcmd = ${rgbcmd}", 1)
    state.red = r
    state.green = g
    state.blue = b
    // Calculate from RGB values
    def hsbColor = rgbToHSB(r, g, b)
    logging("hsbColor from RGB: ${hsbColor}", 1)
    state.colorMode = "RGB"
    //if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
    //if (hsbcmd == "${hsbColor[0]},${hsbColor[1]},${hsbColor[2]}") state.colorMode = "white"
    state.hue = hsbColor['hue']
    state.saturation = hsbColor['saturation']
    state.level = hsbColor['level']
    getAction(getCommandStringWithModeReset("Color1", rgbcmd))
}

void setLevel(l, duration) {
    if (duration == 0) {
        if (false && state.colorMode == "RGB") {
            setHSB(null, null, l)
        } else {
            state.level = l
            getAction(getCommandString("Dimmer", "${l}"))
        }
    } else if (duration > 0) {
        if (false && state.colorMode == "RGB") {
            setHSB(null, null, l)
        } else {
            if (duration > 5400) {
                log.warn "Maximum supported dimming duration is 5400 seconds due to current implementation method used."
                duration = 5400 // Maximum duration is 1.5 hours
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
                // If we have less than 1 step, we shouldn't execute any of the below...

                // Each Backlog command has 200ms delay, deduct that delay and add 1 second extra
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
            //fadeCommands = "Fade 1;Speed ${speed};Dimmer ${l};Delay ${duration};Fade 0"
            logging("fadeCommands: '" + fadeCommands + "', cmdData=${[cLevel:cLevel, levelDistance:levelDistance, direction:direction, steps:steps, increment:increment, stepTime:stepTime, lastStepTime:lastStepTime]}", 1)
            getAction(getMultiCommandString(fadeCommands))
        }
   }
}

void stopLevelChange() {
    // Since sending a backlog command without arguments will cancel any current level change we have, 
    // so that is what we do...
    getAction(getCommandString("Fade", "0"))
    getAction(getCommandString("Backlog", null))
    modeSingleColor(1)
}

void startLevelChange(String direction) {
    Integer cLevel = state.level
    Integer delay = 30
    modeSingleColor(1)
    if(direction == "up") {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (100-cLevel)) as Float)
        }
        setLevel(100, delay)
    } else {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (cLevel)) as Float)
        }
        setLevel(0, delay)
    }
}

void whiteForPlatform() {
    logging("whiteForPlatform()", 10)
    Integer l = state.level
    //state.colorMode = "white"
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
    //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
    getAction(getCommandStringWithModeReset("Color1", hexCmd))
}

// Functions to set RGBW Mode
void modeSet(Integer mode, BigDecimal speed=3) {
    logging("modeSet('${mode}')", 10)
    getAction(getMultiCommandString([[command:"Speed", value:"$speed"], [command:"Scheme", value:"${mode}"]]))
}

void modeNext(BigDecimal speed=3) {
    logging("modeNext()", 10)
    if (state.mode < 4) {
        state.mode = state.mode + 1
    } else {
        state.mode = 0
    }
    modeSet(state.mode, speed)
}

void modePrevious(BigDecimal speed=3) {
    if (state.mode > 0) {
        state.mode = state.mode - 1
    } else {
        state.mode = 4
    }
    modeSet(state.mode, speed)
}

void modeSingleColor(BigDecimal speed=3) {
    state.mode = 0
    modeSet(state.mode, speed)
}

void modeCycleUpColors(BigDecimal speed=3) {
    state.mode = 2
    modeSet(state.mode, speed)
}

void modeCycleDownColors(BigDecimal speed=3) {
    state.mode = 3
    modeSet(state.mode, speed)
}

void modeRandomColors(BigDecimal speed=3) {
    state.mode = 4
    modeSet(state.mode, speed)
}

void modeWakeUp(BigDecimal wakeUpDuration) {
    Integer level = device.currentValue('level')
    Integer nlevel = level > 10 ? level : 10
    logging("modeWakeUp(wakeUpDuration ${wakeUpDuration}, current level: ${nlevel})", 1)
    modeWakeUp(wakeUpDuration, nlevel)
}

void modeWakeUp(BigDecimal wakeUpDuration, BigDecimal level) {
    logging("modeWakeUp(wakeUpDuration ${wakeUpDuration}, level: ${level})", 1)
    state.mode = 1
    wakeUpDuration = wakeUpDuration < 1 ? 1 : wakeUpDuration > 3000 ? 3000 : wakeUpDuration
    level = level < 1 ? 1 : level > 100 ? 100 : level
    state.level = level
    getAction(getMultiCommandString([[command: "WakeupDuration", value: "${wakeUpDuration}"],
                                    [command: "Wakeup", value: "${level}"]]))
}

void setColorByRGBString(String colorRGB) {
    logging("setColorByRGBString(colorRGB ${colorRGB})", 100)
    getAction(getCommandStringWithModeReset("Color1", colorRGB.take(11)))
}

void setPixelColor(String colorRGB, BigDecimal pixel) {
    logging("setPixelColor(colorRGB ${colorRGB}, pixel: ${pixel})", 1)
    if(pixel < 1) pixel = 1
    if(pixel > 512) pixel = 512
    getAction(getCommandStringWithModeReset("Led$pixel", colorRGB.take(7)))
}

void setAddressablePixels(BigDecimal pixels) {
    logging("setAddressablePixels(pixels: ${pixels})", 100)
    if(pixels < 1) pixels = 1
    if(pixels > 512) pixels = 512
    getAction(getCommandString("Pixels", "$pixels"))
}

void setAddressableRotation(BigDecimal pixels) {
    logging("setAddressableRotation(pixels: ${pixels})", 100)
    if(pixels < 1) pixels = 1
    if(pixels > 512) pixels = 512
    getAction(getCommandString("Rotation", "$pixels"))
}

void setEffectWidth(BigDecimal pixels) {
    logging("setEffectWidth(pixels: ${pixels})", 100)
    if(pixels < 0) pixels = 0
    if(pixels > 4) pixels = 4
    getAction(getCommandString("Width1", "$pixels"))
}

String getCommandStringWithModeReset(String command, String value) {
    if(useAlternateColorCommand == true && command == "Color1") {
        if(value.startsWith("#") == true) value = value.substring(1)
        return getCommandString("Var1", "$value")
    } else {
        return getMultiCommandString([[command: "Scheme", value: "0"], [command: "Fade", value: "0"], 
                                  [command: command, value: value]])
    }
}

/**
 * --END-- TASMOTA RGBW METHODS (helpers-tasmota-rgbw)
 */
