/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.2.0625Tb
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

// BEGIN:getDefaultImports()
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
 
import java.security.MessageDigest
// END:  getDefaultImports()

metadata {
    definition (name: "Tasmota - Universal Switch as Motion Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-switch-as-motion-sensor-child-expanded.groovy") {
        capability "Sensor"
        capability "MotionSensor"
        capability "Refresh"

        // BEGIN:getMinimumChildAttributes()
        attribute   "driver", "string"
        // END:  getMinimumChildAttributes()
    }

    preferences {
        // BEGIN:getDefaultMetadataPreferences()
        input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences()

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
     
    Map deviceInfo = ['name': 'Tasmota - Universal Switch as Motion Sensor (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-switch-as-motion-sensor-child-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        if (it.name in ["switch"]) {
            it.name = "motion"
            it.value = (it.value == "on" ? "active" : "inactive")
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else {
            log.warn "Got '$it.name' attribute data, but doesn't know what to do with it! Did you choose the right device type?"
        }
    }
}

void updated() {
    log.info "updated()"
    // BEGIN:getChildComponentDefaultUpdatedContent()
    getDriverVersion()
    // END:  getChildComponentDefaultUpdatedContent()
    refresh()
}

void installed() {
    log.info "installed()"
    device.removeSetting("logLevel")
    device.updateSetting("logLevel", "100")
    refresh()
}

void refresh() {
    // BEGIN:getChildComponentMetaConfigCommands()
    def metaConfig = clearThingsToHide()
    metaConfig = setDatasToHide(['metaConfig', 'isComponent', 'preferences', 'label', 'name'], metaConfig=metaConfig)
    // END:  getChildComponentMetaConfigCommands()
    parent?.componentRefresh(this.device)
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothing to edit here, move along! ---------------------------------------
 * -----------------------------------------------------------------------------
 */

// BEGIN:getDefaultFunctions()
private String getDriverVersion() {
    comment = ""
    if(comment != "") state.comment = comment
    String version = "v1.0.2.0625Tb"
    logging("getDriverVersion() = ${version}", 100)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
// END:  getDefaultFunctions()

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
    cmd = jsonSlurper.parseText(cmd)
     
    r = this."${cmd['cmd']}"(*cmd['args'])
     
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

def generalInitialize() {
    logging("generalInitialize()", 100)
	unschedule("tasmota_updatePresence")
    setLogsOffTask()
    refresh()
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
