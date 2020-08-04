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
  Snippets used by hubitat-driver-helper-tool
"""

def getDefaultMetadataCapabilities():
    return """
// Default Capabilities
capability "Refresh"
capability "Configuration"
"""

def getDefaultMetadataCapabilitiesForZigbeeDevices():
    return """
// Default Capabilities for Zigbee Devices
capability "Sensor"
capability "PresenceSensor"
capability "Initialize"
"""

def getDefaultMetadataCapabilitiesForEnergyMonitor():
    return """
// Default Capabilities for Energy Monitor
capability "VoltageMeasurement"
capability "PowerMeter"
capability "EnergyMeter"
"""

def getDefaultMetadataCapabilitiesForTHMonitor():
    return """
// Default Capabilities for TH Monitor
capability "Sensor"
capability "TemperatureMeasurement"
capability "RelativeHumidityMeasurement"
capability "PressureMeasurement"
"""

def getDefaultParentMetadataAttributes():
    return """
// Default Parent Attributes
attribute   "ip", "string"
attribute   "ipLink", "string"
attribute   "module", "string"
attribute   "templateData", "string"
attribute   "wifiSignal", "string"
"""

def getDefaultMetadataAttributes():
    return """
// Default Attributes
attribute   "driver", "string"
"""

def getMetadataAttributesForLastCheckin():
    return """
// Device Attributes for Last Checkin
attribute "lastCheckin", "Date"
attribute "lastCheckinEpoch", "number"
attribute "notPresentCounter", "number"
attribute "restoredCounter", "number"
"""

def getZigbeeBatteryMetadataAttributes():
    return """
// Default Zigbee Battery Device Attributes
attribute "batteryLastReplaced", "String"
"""

def getDefaultMetadataAttributesForEnergyMonitor():
    return """
// Default Attributes for Energy Monitor
attribute   "current", "string"
attribute   "apparentPower", "string"
attribute   "reactivePower", "string"
attribute   "powerFactor", "string"
attribute   "energyToday", "string"
attribute   "energyYesterday", "string"
attribute   "energyTotal", "string"
attribute   "voltageWithUnit", "string"
attribute   "powerWithUnit", "string"
"""

def getDefaultMetadataAttributesForDimmableLights():
    return """
// Default Attributes for Dimmable Lights
attribute   "wakeup", "string"
"""

def getDefaultMetadataAttributesForTHMonitor():
    return """
// Default Attributes for Pressure Sensor
attribute   "pressureWithUnit", "string"
"""

def getLearningModeAttributes():
    return """
// Attributes used for Learning Mode
attribute   "status", "string"
attribute   "actionSeen", "number"
attribute   "actionData", "json_object"
"""

def getMinimumChildAttributes():
    return """
// Attributes used by all Child Drivers
attribute   "driver", "string"
"""

def getDefaultMetadataCommands():
    return """
// Default Commands
command "reboot"
"""

def getCommandsForPresence():
    return """
// Presence Commands
//command "resetNotPresentCounter"
command "resetRestoredCounter"
"""

def getCommandsForZigbeePresence():
    return """
// Zigbee Presence Commands
command "forceRecoveryMode", [[name:"Minutes*", type: "NUMBER", description: "Maximum minutes to run in Recovery Mode"]]
"""

def getMetadataCommandsForHandlingChildDevices():
    return """
// Commands for handling Child Devices
//command "childOn"
//command "childOff"
//command "recreateChildDevices"
command "deleteChildren"
"""

def getMetadataCommandsForHandlingRGBWDevices():
    return """
// Commands for handling RGBW Devices
command "colorWhite"
command "colorRed"
command "colorGreen"
command "colorBlue"
command "colorYellow"
command "colorCyan"
command "colorPink"
"""

def getMetadataCommandsForHandlingTasmotaRGBWDevices():
    return """
// Commands for handling Tasmota RGBW Devices
command "setEffectWithSpeed", [[name:"Effect number*", type: "NUMBER", description: "Effect number to enable"],
    [name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setNextEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setPreviousEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectSingleColor", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectCycleUpColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectCycleDownColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectRandomColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
"""

def getMetadataCommandsForHandlingTasmotaDimmerDevices():
    return """
// Commands for handling Tasmota Dimmer Devices
command "modeWakeUp", [[name:"Wake Up Duration*", type: "NUMBER", description: "1..3000 = set wake up duration in seconds"],
                       [name:"Level", type: "NUMBER", description: "1..100 = target dimming level"] ]
"""

def getLearningModeCommands():
    return """
// Commands used for Learning Mode
command("actionStartLearning")
command("actionSave")
command("actionPauseUnpauseLearning")
"""

def getZigbeeBatteryCommands():
    return """
// Commands used for Battery
command "resetBatteryReplacedDate"
"""

def getZigbeeGenericDeviceCommands():
    return """
// Commands used by Generic Zigbee devices
command "stopSchedules"
command "getInfo"
"""

def getMetadataCustomizationMethods():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Here getPreferences() can be used to get the above preferences
metaDataExporter()
if(isCSSDisabled() == false) {
    preferences {
        input(name: "hiddenSetting", description: "" + getDriverCSSWrapper(), title: "None", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    }
}
"""

def getDefaultParentMetadataPreferences(includeCSS=False):
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    includedCSS = ""
    if(includeCSS):
        includedCSS = " + styling_getDefaultCSS()"
    return """
// Default Parent Preferences
input(name: "runReset", description: styling_addDescriptionDiv("For details and guidance, see the release thread in the <a href=\\\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\\\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct."), title: styling_addTitleDiv("Settings"), displayDuringSetup: false, type: "paragraph", element: "paragraph")
input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" """ + includedCSS + """, defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
"""

def getDefaultMetadataPreferences(includeCSS=False, includeRunReset=False):
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    includedCSS = ""
    if(includeCSS):
        includedCSS = " + styling_getDefaultCSS()"
    includedRunReset = ""
    if(includeRunReset):
        includedRunReset = """
input(name: "runReset", description: styling_addDescriptionDiv("DISABLE BEFORE RELEASE"), title: styling_addTitleDiv("DISABLE BEFORE RELEASE"))
"""
    return """
// Default Preferences""" + includedRunReset + """
input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" """ + includedCSS + """, defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
"""

def getDefaultMetadataPreferencesLast():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Default Preferences - Last
input(name: "hideDangerousCommands", type: "bool", title: styling_addTitleDiv("Hide Dangerous Commands"), description: styling_addDescriptionDiv("Hides Dangerous Commands, such as 'Delete Children'."), defaultValue: true, displayDuringSetup: false, required: false)
input(name: "disableCSS", type: "bool", title: styling_addTitleDiv("Disable CSS"), description: styling_addDescriptionDiv("CSS makes the driver more user friendly. Disable the use of CSS in the driver by enabling this. Does NOT affect HE resource usage either way."), defaultValue: false, displayDuringSetup: false, required: false)
"""

def getMetadataPreferencesForHiding():
    return """
// Preferences for Hiding
input(name: "hideExtended", type: "bool", title: styling_addTitleDiv("Hide Extended Settings"), description: styling_addDescriptionDiv("Hides extended settings, usually not needed."), defaultValue: true, displayDuringSetup: false, required: false)
input(name: "hideAdvanced", type: "bool", title: styling_addTitleDiv("Hide Advanced Settings"), description: styling_addDescriptionDiv("Hides advanced settings, usually not needed anyway."), defaultValue: true, displayDuringSetup: false, required: false)
"""

def getDefaultMetadataPreferencesForTasmota(includeTelePeriod=True):
    return """
// Default Preferences for Tasmota
input("password", "password", title: styling_addTitleDiv("Device Password"), description: styling_addDescriptionDiv("REQUIRED if set on the Device! Otherwise leave empty."))
input(name: "ipAddress", type: "string", title: styling_addTitleDiv("Device IP Address"), description: styling_addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
input(name: "port", type: "number", title: styling_addTitleDiv("Device Port"), description: styling_addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
input(name: "override", type: "bool", title: styling_addTitleDiv("Override IP"), description: styling_addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
input(name: "useIPAsID", type: "bool", title: styling_addTitleDiv("IP as Network ID"), description: styling_addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
""" + ("""input(name: "telePeriod", type: "string", title: styling_addTitleDiv("Update Frequency"), description: styling_addDescriptionDiv("Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)"), displayDuringSetup: true, required: false)""" if includeTelePeriod else "") + """
input(name: "disableModuleSelection", type: "bool", title: styling_addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + styling_addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
input(name: "moduleNumber", type: "number", title: styling_addTitleDiv("Module Number"), description: "ADVANCED: " + styling_addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
input(name: "deviceTemplateInput", type: "string", title: styling_addTitleDiv("Device Template"), description: "ADVANCED: " + styling_addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the Device Configuration Default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\\\"NAME\\\":\\\"S120\\\",\\\"GPIO\\\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\\\"FLAG\\\":0,\\\"BASE\\\":18})"), displayDuringSetup: true, required: false)

"""

def getDefaultMetadataPreferencesForTHMonitor():
    return """
// Default Preferences for Temperature Humidity Monitor
input(name: "tempOffset", type: "decimal", title: styling_addTitleDiv("Temperature Offset"), description: styling_addDescriptionDiv("Adjust the temperature by this many degrees (in Celcius)."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "tempRes", type: "enum", title: styling_addTitleDiv("Temperature Resolution"), description: styling_addDescriptionDiv("Temperature sensor resolution (0..3 = maximum number of decimal places, default: 1)<br/>NOTE: If the 3rd decimal is a 0 (eg. 24.720) it will show without the last decimal (eg. 24.72)."), options: ["0", "1", "2", "3"], defaultValue: "1")
input(name: "tempUnitConversion", type: "enum", title: styling_addTitleDiv("Temperature Unit Conversion"), description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Celsius to Fahrenheit"], ["3":"Fahrenheit to Celsius"]])
input(name: "humidityOffset", type: "decimal", title: styling_addTitleDiv("Humidity Offset"), description: styling_addDescriptionDiv("Adjust the humidity by this many percent."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "humidityRes", type: "enum", title: styling_addTitleDiv("Humidity Resolution"), description: styling_addDescriptionDiv("Humidity sensor resolution (0..1 = maximum number of decimal places, default: 1)"), options: ["0", "1"], defaultValue: "1")
// This is not implemented except for Zigbee devices
//input(name: "reportAbsoluteHumidity", type: "bool", title: styling_addTitleDiv("Report Absolute Humidity"), description: styling_addDescriptionDiv("Also report Absolute Humidity. Default = Disabled"), defaultValue: false)
input(name: "pressureOffset", type: "decimal", title: styling_addTitleDiv("Pressure Offset"), description: styling_addDescriptionDiv("Adjust the pressure value by this much."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "pressureRes", type: "enum", title: styling_addTitleDiv("Humidity Resolution"), description: styling_addDescriptionDiv("Humidity sensor resolution (0..1 = maximum number of decimal places, default: default)"), options: ["default", "0", "1", "2"], defaultValue: "default")
input(name: "pressureUnitConversion", type: "enum", title: styling_addTitleDiv("Pressure Unit Conversion"), description: styling_addDescriptionDiv("(default: kPa)"), options: ["mbar", "kPa", "inHg", "mmHg", "atm"], defaultValue: "kPa")
"""

def getDefaultMetadataPreferencesForTHMonitorAlternative1():
    return """
// Default Preferences for Temperature Humidity Monitor
input(name: "tempUnitDisplayed", type: "enum", title: styling_addTitleDiv("Displayed Temperature Unit"), description: "", defaultValue: "0", required: true, multiple: false, options:[["0":"System Default"], ["1":"Celsius"], ["2":"Fahrenheit"], ["3":"Kelvin"]])
input(name: "tempOffset", type: "decimal", title: styling_addTitleDiv("Temperature Offset"), description: styling_addDescriptionDiv("Adjust the temperature by this many degrees."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "tempRes", type: "enum", title: styling_addTitleDiv("Temperature Resolution"), description: styling_addDescriptionDiv("Temperature sensor resolution (0..2 = maximum number of decimal places, default: 1)<br/>NOTE: If the 2nd decimal is a 0 (eg. 24.70) it will show without the last decimal (eg. 24.7)."), options: ["0", "1", "2"], defaultValue: "1", displayDuringSetup: true, required: false)
input(name: "humidityOffset", type: "decimal", title: styling_addTitleDiv("Humidity Offset"), description: styling_addDescriptionDiv("Adjust the humidity by this many percent."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "humidityRes", type: "enum", title: styling_addTitleDiv("Humidity Resolution"), description: styling_addDescriptionDiv("Humidity sensor resolution (0..1 = maximum number of decimal places, default: 1)"), options: ["0", "1"], defaultValue: "1")
input(name: "reportAbsoluteHumidity", type: "bool", title: styling_addTitleDiv("Report Absolute Humidity"), description: styling_addDescriptionDiv("Also report Absolute Humidity. Default = Disabled"), defaultValue: false)
if(getDeviceDataByName('hasPressure') == "True") {
    input(name: "pressureUnitConversion", type: "enum", title: styling_addTitleDiv("Displayed Pressure Unit"), description: styling_addDescriptionDiv("(default: kPa)"), options: ["mbar", "kPa", "inHg", "mmHg", "atm"], defaultValue: "kPa")
    input(name: "pressureRes", type: "enum", title: styling_addTitleDiv("Humidity Resolution"), description: styling_addDescriptionDiv("Humidity sensor resolution (0..1 = maximum number of decimal places, default: default)"), options: ["default", "0", "1", "2"], defaultValue: "default")
    input(name: "pressureOffset", type: "decimal", title: styling_addTitleDiv("Pressure Offset"), description: styling_addDescriptionDiv("Adjust the pressure value by this much."), displayDuringSetup: true, required: false, range: "*..*")
}
"""

def getDefaultMetadataPreferencesForDeviceTemperature():
    return """
// Default Preferences for Device Temperature Monitor
input(name: "tempUnitDisplayed", type: "enum", title: styling_addTitleDiv("Displayed Temperature Unit"), description: "", defaultValue: "0", required: true, multiple: false, options:[["0":"System Default"], ["1":"Celsius"], ["2":"Fahrenheit"], ["3":"Kelvin"]])
input(name: "tempOffset", type: "decimal", title: styling_addTitleDiv("Temperature Offset"), description: styling_addDescriptionDiv("Adjust the temperature by this many degrees."), displayDuringSetup: true, required: false, range: "*..*")
"""

def getDefaultMetadataPreferencesForContactSensor():
    return """
// Default Preferences for Contact Sensor
input(name: "invertContact", type: "bool", title: styling_addTitleDiv("Invert open/close"), description: styling_addDescriptionDiv("When open show as closed and vice versa (default: false)"), defaultValue: false)
input(name: "btnDevice1", type: "enum", title: styling_addTitleDiv("Child Device for the contact sensor"), 
            description: styling_addDescriptionDiv("Create a child device for the contact sensor. If changing from Button to Switch or vice versa you need to delete the child device manually for the change to work."),
            options: ["None", "1 virtual button", "1 virtual switch", "1 virtual momentary switch"], defaultValue: "None")
input(name: "switchMirror", type: "bool", title: styling_addTitleDiv("Switch Mirrors open/close"), description: styling_addDescriptionDiv("Switch mirrors the Open(On) / Closed(Off) state (default: false)"), defaultValue: false)
input(name: "logOpenCloseDatetime", type: "bool", title: styling_addTitleDiv("Log Open/Close Time"), description: styling_addDescriptionDiv("Logs the date and time of when the last Open/Closed event occured (default: false)"), defaultValue: false)
"""

def getDefaultMetadataPreferencesForParentDevices(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "enum", title: styling_addTitleDiv("Number of Relays"), description: styling_addDescriptionDiv("Set the number of buttons/relays on the device (default ''' + str(numSwitches) + ''')"), options: ["1", "2", "3", "4", "5", "6"], defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

def getMetadataPreferencesForLastCheckin():
    return '''
// Preferences for Last Checkin
input(name: "lastCheckinEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Date"), description: styling_addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
input(name: "lastCheckinEpochEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Epoch"), description: styling_addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)
input(name: "presenceEnable", type: "bool", title: styling_addTitleDiv("Enable Presence"), description: styling_addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
input(name: "presenceWarningEnable", type: "bool", title: styling_addTitleDiv("Enable Presence Warning"), description: styling_addDescriptionDiv("Enables Presence Warnings in the Logs (default: true)"), defaultValue: true)
'''

def getMetadataPreferencesForRecoveryMode(defaultMode="Normal"):
    return '''
// Preferences for the Recovery Feature
input(name: "recoveryMode", type: "enum", title: styling_addTitleDiv("Recovery Mode"), description: styling_addDescriptionDiv("Select Recovery mode type (default: ''' + defaultMode + ''')<br/>NOTE: The \\"Insane\\" and \\"Suicidal\\" modes may destabilize your mesh if run on more than a few devices at once!"), options: ["Disabled", "Slow", "Normal", "Insane", "Suicidal"], defaultValue: "''' + defaultMode + '''")
'''

def getMetadataPreferencesForZigbeeDevicesWithBattery():
    return '''
// Preferences for Zigbee Devices with Battery
input(name: "vMinSetting", type: "decimal", title: styling_addTitleDiv("Battery Minimum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 0% (default = 2.5V)"), defaultValue: "2.5", range: "2.1..2.8")
input(name: "vMaxSetting", type: "decimal", title: styling_addTitleDiv("Battery Maximum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 100% (default = 3.0V)"), defaultValue: "3.0", range: "2.9..3.4")
'''

def getDefaultMetadataPreferencesForParentDevicesWithUnlimitedChildren(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "number", title: styling_addTitleDiv("Number of Children"), description: styling_addDescriptionDiv("Set the number of children (default ''' + str(numSwitches) + ''')"), defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

