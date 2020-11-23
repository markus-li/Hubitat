 /**
 *  Copyright 2020 Markus Liljergren
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

/* Default imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput


metadata {
    definition (name: "Tasmota - RF/IR Water Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-rf-ir-water-sensor-child-expanded.groovy") {
        capability "WaterSensor"
        capability "Sensor"

        
        // Attributes used for Learning Mode
        attribute   "status", "string"
        attribute   "actionSeen", "number"
        attribute   "actionData", "json_object"
        
        
        // Commands used for Learning Mode
        command("actionStartLearning")
        command("actionSave")
        command("actionPauseUnpauseLearning")
        command "clear"
    }

    preferences {
        
        // Default Preferences
        input(name: "runReset", description: "<i>For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct.<br/>Type RESET and then press 'Save Preferences' to DELETE all Preferences and return to DEFAULTS.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        generateLearningPreferences()
    }
}

def getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - RF/IR Water Sensor (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-rf-ir-water-sensor-child-expanded.groovy']
    return(deviceInfo[infoName])
}

// Methods for displaying the correct Learning Preferences and returning the 
// current Action Name
def generateLearningPreferences() {
    input(name: "learningMode", type: "bool", title: "<b>Learning Mode</b>", description: '<i>Activate this to enter Learning Mode. DO NOT ACTIVATE THIS once you have learned the codes of a device, they will have to be re-learned!</i>', displayDuringSetup: false, required: false)
    if(learningMode) {
        input(name: "actionCurrentName", type: "enum", title: "<b>Action To Learn</b>", 
              description: "<i>Select which Action to save to in Learn Mode.</i>", 
              options: ["Dry", "Wet"], defaultValue: "Wet", 
              displayDuringSetup: false, required: false)
        input(name: "learningModeAdvanced", type: "bool", title: "<b>Advanced Learning Mode</b>", 
              description: '<i>Activate this to enable setting Advanced settings. Normally this is NOT needed, be careful!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        if(learningModeAdvanced) {
            input(name: "actionCodeSetManual", type: "string", title: "<b>Set Action Code Manually</b>", 
              description: '<i>WARNING! For ADVANCED users only!</i>', 
              displayDuringSetup: false, required: false)
            input(name: "actionResetAll", type: "bool", title: "<b>RESET all Saved Actions</b>", 
              description: '<i>WARNING! This will DELETE all saved/learned Actions!</i>', 
              defaultValue: false, displayDuringSetup: false, required: false)
        }
    }
}

def getCurrentActionName() {
    if(!binding.hasVariable('actionCurrentName') || 
      (binding.hasVariable('actionCurrentName') && actionCurrentName == null)) {
        logging("Doesn't have the action name defined... Using Wet!", 1)
        actionName = "Wet"
    } else {
        actionName = actionCurrentName
    }
    return(actionName)
}

/* These functions are unique to each driver */
void dry() {
    logging("dry()", 1)
    sendEvent(name: "water", value: "dry", isStateChange: true)
}

void wet() {
    logging("wet()", 1)
    sendEvent(name: "water", value: "wet", isStateChange: true)
}

void clear() {
    logging("clear()", 1)
    dry()
}

// These are called when Action occurs, called from actionHandler()
def dryAction() {
    logging("dryAction()", 1)
    dry()
}

def wetAction() {
    logging("wetAction()", 1)
    wet()
}

/* Helper functions for Code Learning */
def actionStartLearning() {
    return(actionStartLearning(true))
}

def actionStartLearning(resetActionData) {
    def cmds = []
    if(learningMode) {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
        logging("actionStartLearning", 1)
        cmds << sendEvent(name: "actionSeen", value: 0)
        if(resetActionData) cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        actionSeen = device.currentValue('actionSeen', true)
        actionData = device.currentValue('actionData', true)
        logging("actionStartLearning actionData=${actionData}", 1)
    } else {
        log.warn "Learning Mode not active, can't start Learning!"
    }
    return cmds
}

def actionPauseUnpauseLearning() {
    def cmds = []
    // This will pause/unpause Learning, good for Contact sensors for example...
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
    } else {
        cmds << sendEvent(name: "status", value: "Learning Mode: Paused")
    }
    return cmds
}

def actionSave() {
    def cmds = []
    logging("actionSave()", 1)
    if(learningMode) {
        actionName = getCurrentActionName()
        def slurper = new JsonSlurper()
        actionData = device.currentValue('actionData', true)
        if(actionData != null) actionData = slurper.parseText(actionData)

        if(actionData && actionData != "Saved") {
            frequentData = null
            maxActionNumSeen = 0
            actionData.each {
                logging("it=${it}", 1)
                if(it.containsKey('data')) {
                    if(it.containsKey('seen') && it['seen'] >= maxActionNumSeen) {
                        maxActionNumSeen = it['seen']
                        frequentData = it['data']
                    }
                }
            }
            if(frequentData != null) {
                logging("actionSave() saving this data: '${frequentData}'", 100)
                //cmds << device.clearSetting('actionCodeDefault')
                //cmds << device.removeSetting('actionCodeDefault')
                //cmds << device.updateSetting('actionCodeDefault', frequentData)
                state.actions = state.actions ?: [:]
                state.actions[actionName] = frequentData
                cmds << sendEvent(name: "actionSeen", value: 0)
                cmds << sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                cmds << sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
            } else {
                log.warn "No Action codes found in actionData!"
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        } else {
            log.warn "No Action codes found!"
            if(actionData && actionData == "Saved") {
                // Do nothing...
            } else {
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        }
    } else {
        log.warn "Learning Mode not active, can't save Action Code!"
    }
    return cmds
}


def actionLearn(data) {
    def cmds = []
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") return cmds
    actionName = getCurrentActionName()
    // Can't do this inside a mutex lock, but just don't press so quickly and it will be ok...
    def slurper = new JsonSlurper()
    actionSeen = device.currentValue('actionSeen', true)
    actionData = device.currentValue('actionData', true)
    if(actionData != null) actionData = slurper.parseText(actionData)

    if(actionSeen == null || actionSeen == 'null') {
        actionSeen = 0
    }
    if(actionData == null || actionData == 'null' || 
       actionData == 'Saved' || actionData == '"Saved"' ||
       actionData == 'N/A' || actionData == '"N/A"') {
        actionData = []
    }
    logging("actionSeen=${actionSeen}", 1)
    logging("actionData=${actionData}", 1)
    // All is same for all types, no need to have special cases...
    //if(data.type == 'parsed_portisch') {
    found = false
    actionData.each {
        logging("it=${it}", 1)
        if(it.containsKey('data') && data['Data'] == it['data']) {
            found = true
            it['seen'] = it['seen'] + 1
        }
        if(it.containsKey('seen') && actionSeen < it['seen']) actionSeen = it['seen']
    }
    if (!found) {
        actionData.add([seen: 1, data: data.Data])
        if (actionSeen < 1) actionSeen = 1
    }
    
    cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'. The most frequent Action seen ${actionSeen} time(s)!")
    cmds << sendEvent(name: "actionSeen", value: actionSeen)
    cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(actionData))
    
    if (state.containsKey("events")) {
        state.remove("events")
    }
    return cmds
}

def actionHandler(data) {
    def cmds = []
    logging("actionHandler(data='${data}')", 1)
    actionName = getCurrentActionName()
    if(data && data.containsKey('Data') && state.actions) {
        // && data['Data'] == state.actions[actionName]
        currentData = data['Data']
        state.actions.each {
            if (it.value == currentData) {
                logging('Button pushed: ${it.value)', 1)
                "${it.key[0].toLowerCase() + it.key.substring(1)}Action"()
            }
        }
    }
    return cmds
}


def parseParentData(parentData) {
    def cmds = []
    //logging("parseParentData(parentData=${parentData})", 100)
    if (parentData.containsKey("type")) {
        if(parentData.type == 'parsed_portisch' || 
           parentData.type == 'raw_portisch' || 
           parentData.type == 'rflink') {
            logging("${parentData.type}=${parentData}", 100)
            if(learningMode) {
                cmds << actionLearn(parentData)
            } else {
                cmds << actionHandler(parentData)
            }
        } else {
            log.error("Unknown Format=${parentData}")
        }
    } else {
        log.error("Unknown parentData=${parentData}")
    }
    return cmds
}

void updated() {
    logging('Inside updated()...', 1)
    if(!learningMode) {
        sendEvent(name: "actionSeen", value: 0)
        sendEvent(name: "actionData", value: JsonOutput.toJson('N/A'))
        sendEvent(name: "status", value: "Action Mode")
    } else {
        //sendEvent(name: "actionSeen", value: 0)
        //sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        //sendEvent(name: "status", value: "Learning Mode")
        actionStartLearning(false)  // Do NOT reset actionData
        if(learningModeAdvanced) {
            if(actionResetAll) {
                log.warn "ALL saved Actions have been DELETED!"
                state.actions = [:]
                device.clearSetting('actionResetAll')
                device.removeSetting('actionResetAll')
            }
            if(actionCodeSetManual && actionCodeSetManual != "") {
                actionName = getCurrentActionName()
                state.actions = state.actions ?: [:]
                state.actions[actionName] = actionCodeSetManual
                sendEvent(name: "actionSeen", value: 0)
                sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
                device.clearSetting('actionCodeSetManual')
                device.removeSetting('actionCodeSetManual')
            }
        }
    }
}

def calculateB0(inputStr, repeats) {
    // This calculates the B0 value from the B1 for use with the Sonoff RF Bridge
    logging('inputStr: ' + inputStr, 0)
    inputStr = inputStr.replace(' ', '')
    //logging('inputStr.substring(4,6): ' + inputStr.substring(4,6), 0)
    numBuckets = Integer.parseInt(inputStr.substring(4,6), 16)
    buckets = []

    logging('numBuckets: ' + numBuckets.toString(), 0)

    outAux = String.format(' %02X ', numBuckets.toInteger())
    outAux = outAux + String.format(' %02X ', repeats.toInteger())
    
    logging('outAux1: ' + outAux, 0)
    
    j = 0
    for(i in (0..numBuckets-1)){
        outAux = outAux + inputStr.substring(6+i*4,10+i*4) + " "
        j = i
    }
    logging('outAux2: ' + outAux, 0)
    outAux = outAux + inputStr.substring(10+j*4, inputStr.length()-2)
    logging('outAux3: ' + outAux, 0)

    dataStr = outAux.replace(' ', '')
    outAux = outAux + ' 55'
    length = (dataStr.length() / 2).toInteger()
    outAux = "AA B0 " + String.format(' %02X ', length.toInteger()) + outAux
    logging('outAux4: ' + outAux, 0)
    logging('outAux: ' + outAux.replace(' ', ''), 10)

    return(outAux)
}

/* Default functions go here */
private def getDriverVersion() {
    logging("getDriverVersion()", 50)
	def cmds = []
    comment = ""
    if(comment != "") state.comment = comment
    sendEvent(name: "driverVersion", value: "v0.9.3 for Tasmota 7.x/8.x (Hubitat version)")
    return cmds
}


/* Helper functions included in all drivers */
def installed() {
	logging("installed()", 50)
	configure()
    try {
        // In case we have some more to run specific to this driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/*
	initialize

	Purpose: initialize the driver
	Note: also called from updated() in most drivers
*/
void initialize()
{
    logging("initialize()", 50)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, logsOff)
    }
}

def configure() {
    logging("configure()", 50)
    def cmds = []
    cmds = update_needed_settings()
    try {
        // Run the getDriverVersion() command
        newCmds = getDriverVersion()
        if (newCmds != null && newCmds != []) cmds = cmds + newCmds
    } catch (MissingMethodException e) {
        // ignore
    }
    if (cmds != []) cmds
}

def generate_preferences(configuration_model)
{
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
                input "${it.@index}", "number",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }
        }
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${cmd.name}" = cmd.value

    if (state.settings?."${cmd.name}" != null)
    {
        if (state.settings."${cmd.name}".toString() == cmd.value)
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: false)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: false)
        }
    }
    state.currentProperties = currentProperties
}

/*
	logsOff

	Purpose: automatically disable debug logging after 30 mins.
	Note: scheduled in Initialize()
*/
void logsOff(){
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
        device.clearSetting("logLevel")
        device.removeSetting("logLevel")
        state.settings.remove("logLevel")
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0") runIn(1800, logsOff)
    }
}

private def getFilteredDeviceDriverName() {
    deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    return deviceDriverName
}

private def getFilteredDeviceDisplayName() {
    device_display_name = device.displayName.replace(' (parent)', '').replace(' (Parent)', '')
    return device_display_name
}

def configuration_model_debug()
{
'''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="0" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
    <Item label="Code Learning" value="100" />
</Value>
</configuration>
'''
}

/* Logging function included in all drivers */
private def logging(message, level) {
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level <= 100)
                log.debug "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        
        case "100": // Only special debug messages, eg IR and RF codes
            if (level == 100 )
                log.debug "$message"
        break
        }
    }
}

