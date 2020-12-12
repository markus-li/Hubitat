/**
 *  Copyright 2020 Markus Liljergren (https://oh-lalabs.com)
 *
 *  Version: v1.1.1.1212Tb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
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
    definition (name: "Tasmota - Universal Multi Sensor (Child)", namespace: "tasmota", author: "Markus Liljergren", filename: "tasmota-universal-multi-sensor-child", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-multi-sensor-child-expanded.groovy") {
        capability "Sensor"
        capability "TemperatureMeasurement"
        capability "RelativeHumidityMeasurement"
        capability "PressureMeasurement"
        capability "IlluminanceMeasurement"
        capability "MotionSensor"
        capability "WaterSensor"

        capability "Refresh"

        // BEGIN:getMinimumChildAttributes()
        attribute   "driver", "string"
        // END:  getMinimumChildAttributes()

        attribute  "dewPoint", "number"
        attribute  "gas", "number"
        attribute  "distance", "string"
        attribute  "pressureWithUnit", "string"

        command "toggle"
    }

    preferences {
        // BEGIN:getDefaultMetadataPreferences()
        input(name: "debugLogging", type: "bool", title: styling_getLogo() + styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences()
        input(name: "hideMeasurementAdjustments", type: "bool", title: styling_addTitleDiv("Hide Measurement Adjustment Preferences"), description: "", defaultValue: false, displayDuringSetup: false, required: false)
        // BEGIN:getDefaultMetadataPreferencesForTHMonitor()
        input(name: "tempOffset", type: "decimal", title: styling_addTitleDiv("Temperature Offset"), description: styling_addDescriptionDiv("Adjust the temperature by this many degrees (in Celcius)."), displayDuringSetup: true, required: false, range: "*..*")
        input(name: "tempRes", type: "enum", title: styling_addTitleDiv("Temperature Resolution"), description: styling_addDescriptionDiv("Temperature sensor resolution (0..3 = maximum number of decimal places, default: 1)<br/>NOTE: If the 3rd decimal is a 0 (eg. 24.720) it will show without the last decimal (eg. 24.72)."), options: ["0", "1", "2", "3"], defaultValue: "1")
        input(name: "tempUnitConversion", type: "enum", title: styling_addTitleDiv("Temperature Unit Conversion"), description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Celsius to Fahrenheit"], ["3":"Fahrenheit to Celsius"]])
        input(name: "humidityOffset", type: "decimal", title: styling_addTitleDiv("Humidity Offset"), description: styling_addDescriptionDiv("Adjust the humidity by this many percent."), displayDuringSetup: true, required: false, range: "*..*")
        input(name: "humidityRes", type: "enum", title: styling_addTitleDiv("Humidity Resolution"), description: styling_addDescriptionDiv("Humidity sensor resolution (0..1 = maximum number of decimal places, default: 1)"), options: ["0", "1"], defaultValue: "1")
        
        input(name: "pressureOffset", type: "decimal", title: styling_addTitleDiv("Pressure Offset"), description: styling_addDescriptionDiv("Adjust the pressure value by this much."), displayDuringSetup: true, required: false, range: "*..*")
        input(name: "pressureRes", type: "enum", title: styling_addTitleDiv("Pressure Resolution"), description: styling_addDescriptionDiv("Pressure sensor resolution (0..1 = maximum number of decimal places, default: default)"), options: ["default", "0", "1", "2"], defaultValue: "default")
        input(name: "pressureUnitConversion", type: "enum", title: styling_addTitleDiv("Pressure Unit Conversion"), description: styling_addDescriptionDiv("(default: kPa)"), options: ["mbar", "kPa", "inHg", "mmHg", "atm"], defaultValue: "kPa")
        // END:  getDefaultMetadataPreferencesForTHMonitor()
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
     
    Map deviceInfo = ['name': 'Tasmota - Universal Multi Sensor (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'filename': 'tasmota-universal-multi-sensor-child', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-multi-sensor-child-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        switch(it.name) {
            case "illuminance":
            case "motion":
            case "water": 
            case "distance":
            case "gas":
                logging(it.descriptionText, 100)
                sendEvent(it)
                break
            case "temperature": 
            case "dewPoint": 
                List res =  sensor_data_getAdjustedTemp(new BigDecimal(it.value), true)
                it.unit = res[0]
                it.value = res[1]
                logging(it.descriptionText, 100)
                sendEvent(it)
                break
            case "humidity":
                it.value = sensor_data_getAdjustedHumidity(new BigDecimal(it.value))
                logging(it.descriptionText, 100)
                sendEvent(it)
                break
            case "pressure":
                it.value = sensor_data_convertPressure(new BigDecimal(it.value))
                if(pressureUnitConversion != null) {
                    it.unit = pressureUnitConversion
                } else {
                    it.unit = "kPa"
                }
                logging(it.descriptionText, 100)
                sendEvent(it)
                sendEvent(name: "pressureWithUnit", value: "$it.value $it.unit", isStateChange: false)
                break
            default:
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
    parent?.componentRefresh(device)
    if(hideMeasurementAdjustments == true) {
        metaConfig = setPreferencesToHide(["tempOffset", "tempRes", "tempUnitConversion",
                                           "humidityOffset", "pressureOffset", "pressureUnitConversion"], metaConfig=metaConfig)
    }
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
    String version = "v1.1.1.1212Tb"
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
        logging("This IS a driver!", 1)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 1)
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

void toggle() {
    if(device.currentValue('switch') == 'on') {
        off()
    } else {
        on()
    }
}

void logsOff() {
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled... "
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

String styling_getLogo() {
    String logoCSS = '''
    #ohla_logo {
      display: block;
      width: 200px;
      height: 50px;
      position: absolute;
      top: 10px;
      right: 10px;
    }

    @media screen and (max-device-width:450px), screen and (max-width:450px) {
      #ohla_logo {
        width: 120px;
        top: 55px;
      }
    }
    
    '''
    return "<div id='ohla_logo'><a href='https://oh-lalabs.com/l/" + getDeviceInfoByName('filename') + "' target='_blank'><svg version='1.0' id='Layer_1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' viewBox='0 0 189 41' style='enable-background:new 0 0 189 41;' xml:space='preserve'><style type='text/css'>.st0{fill:#0066FF;}</style><g><g><g><g><path d='M38.5,29.7c-2.8,2.2-4.3,3.6-7.1,5.7c-0.5,0.4-1.8,0.5-2.5,0.6c-1.6,0.1-1.9-0.1-1.5-1c0.6-1.2,1.5-2.7,2.6-4.2c1-1.4,2.8-4,4.8-6.4c1.8-2.3,3.7-4.7,6.2-7.6c2.2-2.4,5-5.2,7.4-7.1c1.3-1.1,2.8-1.6,4-1.6c1.3,0,1.8,0.3,1.8,0.5c-1.8,1.1-7,6.5-10.9,11.1c-3.8,4.6-8.1,10.3-10,13.2l0.1,0.1c1.3-1.1,3.3-2.8,5.6-4.5c2-1.4,5.1-3.6,7.8-3.6c1.7,0,2.6,0.7,2.5,1.9c-0.1,1.1-1.3,2.6-2.3,3.8c-0.9,1.2-1.6,2-2,2.6c-0.4,0.8-0.5,1.2-0.2,1.4c0.3,0.2,1,0,1.6-0.3c0.9-0.4,1.9-1.1,2.7-1.8c0.7-0.5,1.6-1.3,2.3-1.9c0.3-0.3,1.1-1,1.8-1c0.6,0,0.6,0.4,1,0.6c0.4,0.2,0.2,0.3,0,0.5c-1.1-1.2-2.1,0-2.5,0.4c-0.9,0.8-2.2,1.9-3.1,2.6c-1.3,1-3.8,2.6-6,2.6c-2.5,0.1-2.9-1.2-2.7-2.3c0.1-1.2,0.9-2.2,2.3-3.8c0.9-1.1,1.9-2.2,2.6-3.1c0.3-0.3,0.3-0.7-0.1-0.8c-0.4-0.2-1.1,0.1-1.8,0.4C41.8,27.2,40.2,28.3,38.5,29.7z'/><path d='M65.2,31.5c0,0.3-0.1,0.4-0.2,0.4c-0.1,0-0.2-0.1-0.3-0.2c-0.6-0.8-1.3-0.9-2.8-0.9c-1.8,0-2.9,0.6-5.2,0.6c-0.8,0-2-0.3-2.4-0.9c-0.1-0.1-0.1-0.3-0.1-0.4c0.1-0.1,0.2,0,0.4,0.1c0.4,0.1,0.8,0.1,1.1,0.1c1.3-0.1,2.2-0.7,3.3-1.4c0.9-0.6,2-1,3-1C64.5,27.9,65.3,29.6,65.2,31.5z'/><path d='M36.4,12.9c-0.3-2.3-2.3-3.2-4.7-3.5c-6.7-0.6-15.3,2.5-21.3,8.2c-3.3,3.1-7.2,8.8-6,13.9c0.9,4.1,4.9,5.1,7.8,5.2c5.9,0.2,13.6-4.1,17.9-9.3c2.7-3.3,4-7.4,4-9.8c0-1.8-0.6-3.3-1.9-4c-2.4-1.4-6,0.2-6.6,2.9c-0.4,1.9,0.3,3.3,1.8,3.6c1.1,0.2,1.9-0.4,2.2-1.4c0.2-1-1-1.7-0.7-3c0.3-1.4,1.2-2.1,2.3-1.8c0,0,0,0,0,0c0.2,0,0.4,0.1,0.5,0.2c0.6,0.3,1.1,1,0.9,2.1c-0.3,3-2.3,6.7-5.3,10.5c-5.2,6.3-9.9,9.7-14.4,9.6C10.6,36,9.2,34.9,8.8,33c-0.9-4,2.3-9.8,5.9-13.6c3.5-3.7,10.8-9.3,17.1-9.1c1.9,0,3.7,1,4,2.7c0.1,1.1-0.3,2.1-0.8,2.8c-0.4,0.6-0.6,0.6-0.5,0.8c0.1,0.1,0.6-0.1,0.9-0.5C36,15.3,36.5,14.1,36.4,12.9z'/><path d='M102.1,9.6c-2.1,0-4.3,0.4-5.9,0.9c-0.3,0.1-0.9,0.3-1.2,0.4c-2.8,1-7.1,3.5-10.5,8.5c-0.1,0.1-0.3,0.3-0.4,0.5c-0.1,0.2-0.2,0.3-0.4,0.5c-1.3,0.1-2.5,0.3-3.7,0.5c-3.4,0.5-5.8,1.9-5.6,3.8c0.1,1.5,1.1,2.5,2.5,2.4c0.9-0.1,1.5-0.7,1.4-1.5c-0.1-0.9-1.2-1.1-1.3-2.3c-0.1-1,1.2-1.8,3.1-2c1.1-0.1,2.3-0.2,3.4-0.3c-2.4,3.6-4,7.8-6.9,10.7c-0.8-0.1-1.8-0.2-3.4-0.1c-3.8,0.1-9.3,1.5-9.4,3.6c-0.1,0.9,1.2,1.5,3.8,1.4c3.8-0.1,6.6-1.8,8.5-3.2c1,0.2,3.5,1.3,5.7,2.1c2.2,0.8,4.2,1.3,6.3,1.3c3.4,0,5.7-1.3,8.8-4.1c0.3-0.3,0.6-0.6,1-0.9c1.7-1.4,2.3-3.2,1.5-4.8c-0.7-1.4-1.9-1.9-3-1.4c-0.8,0.4-1.1,1.2-0.7,2c0.4,0.7,1.5,0.6,2,1.6c0.3,0.5,0.3,1,0.1,1.6c-0.2,0.4-0.7,0.8-1.3,1.2c-2.6,1.8-5.1,2.3-8,2.3c-1.1,0-3.5-0.4-5.2-0.8c-2.6-0.7-4.6-1.2-5.5-1.3c2.4-1.6,7.2-5.1,11.2-11.1c0.6,0,1.3,0.1,1.3,0.1c1.6,0.1,3.3,0.1,5-0.1c5-0.6,12.9-2.5,13.5-6.7C109.2,11.2,105.9,9.5,102.1,9.6z M68.6,35.7c-1.2,0.1-2-0.3-2-1c-0.1-1.3,2.1-2.3,4.1-2.3c1.6-0.1,3.1,0.2,4.5,0.6C73.5,34.5,70.7,35.7,68.6,35.7z M106.6,13.5c-0.6,3.8-7.6,6-11.1,6.4c-1.7,0.2-3.1,0.3-4.6,0.3c-0.5,0-0.9,0-1.4,0c0,0,0,0,0,0c2.3-3.3,4.7-6.2,6.9-7.7c0.3-0.2,0.7-0.5,0.9-0.6c1.3-0.9,3.4-1.6,5.1-1.6C104.8,10.1,106.8,11.2,106.6,13.5z'/><path d='M122.6,31.1c-1-1-2.7-0.9-4.5,0.2c-0.4,0.2-0.9,0.5-1.7,1.2c-1,0.8-2.1,1.6-2.7,1.8c-0.5,0.3-1.2,0.4-1.5,0.2c-0.3-0.2-0.3-0.7,0.1-1.3c0.4-0.8,1.3-2,2.1-3.1c1.3-1.6,2.3-2.8,3.4-4.2c0.6-0.8,0.4-1-1.4-1c-2,0-2.4,0.3-4,2c0.2-0.9-0.2-2.1-2-2.1c-3.7-0.1-7.6,1.9-9.6,3.5c-2,1.6-3.7,3.5-3.7,5.5c-0.1,1.9,1.4,2.5,3.4,2.5c3,0,6-2.3,7.7-3.8l0.1,0.1c-0.4,0.6-0.8,1.5-0.7,2.3c0.2,0.9,1,1.4,2.5,1.4c2,0,3.8-0.9,6-2.6c1-0.8,1.5-1.3,2.1-1.6c0.6-0.2,1.2-0.1,1.6,0.2c0.9,0.9,0.4,2,1.1,2.6c0.6,0.6,1.5,0.5,2.2-0.2C124,33.7,123.8,32.3,122.6,31.1z M107.4,32.2c-1.7,1.6-4.1,3-5.3,3c-0.4,0-0.8-0.2-0.8-0.6c-0.1-0.8,1.1-2.6,3.5-5c2.3-2.3,4.7-4.1,5.9-4.1c0.7,0,1.2,0.3,1.1,1.1C111.8,27.6,109.4,30.4,107.4,32.2z'/></g><g><path d='M17.2,9.5c-0.3-1-0.5-2.2-0.8-3.4c0-0.2-0.3-0.4-0.5-0.4c-0.1,0-0.2,0-0.3,0c-0.5,0-0.7,0.4-0.5,1c0.4,1,0.8,2,1,2.8c0.8,2,1.3,3.4,1.7,4.5c0.2-0.1,0.4-0.1,0.6-0.2C18.1,12.7,17.6,11.3,17.2,9.5z'/><g><g><path d='M4.1,8.1c0,0,0.8,0.4,2,0.2C7.3,8,9.4,6.7,11.9,6.6c2.5,0,3.5,1.7,3.7,2.4c0.2,0.7-0.4,1.1-0.4,1.1s0.8,0.9-0.7,1.8C8.6,14.4,4.1,8.1,4.1,8.1z'/><path d='M11.9,6.6c-2.2,0-4.1,1.1-5.4,1.5C7.7,8,9.8,6.9,12.2,7.1c2.1,0.2,3.1,1.7,3.4,2.5c0.1-0.2,0.1-0.3,0-0.6C15.4,8.4,14.3,6.6,11.9,6.6z'/><path d='M14.6,11.2C9.4,13.8,5,9,4.2,8.1c0,0,0,0,0,0s4.7,6.8,10.3,3.9c0.6-0.4,0.9-0.8,0.9-1.1c0,0-0.1-0.1-0.1-0.1C15,10.9,14.8,11.1,14.6,11.2z'/></g><path d='M9.7,9.4c0,0,3.3,0.2,5.4,1.1c2.1,0.9,3.1,2.9,3.1,2.9s-0.7-2.6-2.6-3.4C13.6,9.1,9.7,9.4,9.7,9.4z'/></g><g><g><path d='M22.7,4.6c0,0-0.3,0.4-0.9,0.6c-0.7,0.2-2,0.1-3.3,0.7c-1.2,0.6-1.3,1.8-1.2,2.1c0.1,0.4,0.5,0.5,0.5,0.5s-0.2,0.6,0.8,0.7C22.1,8.9,22.7,4.6,22.7,4.6z'/><path d='M18.5,5.9c1.1-0.6,2.3-0.5,3-0.6c-0.7,0.2-2,0.2-3.1,1c-1,0.7-1.1,1.6-1,2.1c-0.1-0.1-0.1-0.2-0.1-0.3C17.2,7.7,17.3,6.5,18.5,5.9z'/><path d='M18.4,8.9c3.2-0.1,4.1-3.6,4.3-4.2c0,0,0,0,0,0s-0.5,4.5-4,4.6c-0.4,0-0.6-0.1-0.7-0.3c0,0,0-0.1,0-0.1C18.1,8.8,18.2,8.8,18.4,8.9z'/></g><path d='M20,6.9c0,0-1.2,0.7-2,1.7c-0.8,0.9-0.8,2.2-0.8,2.2s-0.3-1.5,0.5-2.4C18.3,7.5,20,6.9,20,6.9z'/></g></g></g><g><path d='M112.2,20.8v-3.6h-1.1v-0.7h2.9v0.7h-1.1v3.6H112.2z'/><path d='M114.5,20.8v-4.3h1.1l0.7,3l0.7-3h1.1v4.3h-0.7v-3.4l-0.7,3.4h-0.7l-0.7-3.4v3.4H114.5z'/></g></g></g><g><path class='st0' d='M132.3,9.6v22.8h6.9v3.8h-11V9.6H132.3z'/><path class='st0' d='M150.2,36.2l-0.7-4.8h-5.1l-0.7,4.8h-3.8l4.2-26.6h6.1l4.2,26.6H150.2z M149,27.8l-2-13.4l-2,13.4H149z'/><path class='st0' d='M168.9,15.7v0.9c0,2.7-0.8,4.5-2.7,5.3c2.2,0.9,3.1,2.9,3.1,5.7v2.2c0,4.1-2.2,6.3-6.3,6.3h-6.6V9.6h6.3C167,9.6,168.9,11.7,168.9,15.7z M160.6,20.5h1.6c1.6,0,2.5-0.7,2.5-2.8v-1.5c0-1.9-0.6-2.7-2.1-2.7h-2V20.5z M160.6,32.4h2.4c1.4,0,2.2-0.6,2.2-2.6v-2.3c0-2.5-0.8-3.2-2.7-3.2h-1.9V32.4z'/><path class='st0' d='M177.4,9.3c4.1,0,6.1,2.4,6.1,6.7v0.8h-3.9v-1.1c0-1.9-0.8-2.6-2.1-2.6c-1.3,0-2.1,0.7-2.1,2.6c0,5.5,8.2,6.5,8.2,14.1c0,4.2-2.1,6.7-6.2,6.7s-6.2-2.4-6.2-6.7v-1.6h3.9v1.9c0,1.9,0.8,2.6,2.2,2.6c1.3,0,2.2-0.7,2.2-2.6c0-5.5-8.2-6.5-8.2-14.1C171.3,11.8,173.3,9.3,177.4,9.3z'/></g></svg></a></div><style>$logoCSS </style>"
}
// END:  getHelperFunctions('styling')

// BEGIN:getHelperFunctions('sensor-data')
private sensor_data_getAdjustedTemp(BigDecimal value, boolean returnUnit=false) {
    Integer res = 1
    String degree = String.valueOf((char)(176))
    String tempUnit = "${degree}C"
    if(tempRes != null && tempRes != '') {
        res = Integer.parseInt(tempRes)
    }
    if (tempUnitConversion == "2") {
        value = celsiusToFahrenheit(value)
        tempUnit = "${degree}F"
    } else if (tempUnitConversion == "3") {
        value = fahrenheitToCelsius(value)
    }
    BigDecimal r = null
	if (tempOffset != null) {
	   r = (value + new BigDecimal(tempOffset)).setScale(res, BigDecimal.ROUND_HALF_UP)
	} else {
       r = value.setScale(res, BigDecimal.ROUND_HALF_UP)
    }
    if(returnUnit == false) {
        return r
    } else {
        return [tempUnit, r]
    }
}

private List sensor_data_getAdjustedTempAlternative(BigDecimal value) {
    Integer res = 1
    BigDecimal rawValue = value
    if(tempRes != null && tempRes != '') {
        res = Integer.parseInt(tempRes)
    }
    String degree = String.valueOf((char)(176))
    String tempUnit = "${degree}C"
    String currentTempUnitDisplayed = tempUnitDisplayed
    if(currentTempUnitDisplayed == null || currentTempUnitDisplayed == "0") {
        if(location.temperatureScale == "C") {
            currentTempUnitDisplayed = "1"
        } else {
            currentTempUnitDisplayed = "2"
        }
    }

    if (currentTempUnitDisplayed == "2") {
        value = celsiusToFahrenheit(value)
        tempUnit = "${degree}F"
    } else if (currentTempUnitDisplayed == "3") {
        value = value + 273.15
        tempUnit = "${degree}K"
    }
	if (tempOffset != null) {
	   return [tempUnit, (value + new BigDecimal(tempOffset)).setScale(res, BigDecimal.ROUND_HALF_UP), rawValue]
	} else {
       return [tempUnit, value.setScale(res, BigDecimal.ROUND_HALF_UP), rawValue]
    }
}

private BigDecimal currentTemperatureInCelsiusAlternative(BigDecimal providedCurrentTemp = null) {
    String currentTempUnitDisplayed = tempUnitDisplayed
    BigDecimal currentTemp = providedCurrentTemp != null ? providedCurrentTemp : device.currentValue('temperature')
    if(currentTempUnitDisplayed == null || currentTempUnitDisplayed == "0") {
        if(location.temperatureScale == "C") {
            currentTempUnitDisplayed = "1"
        } else {
            currentTempUnitDisplayed = "2"
        }
    }

    if (currentTempUnitDisplayed == "2") {
        currentTemp = fahrenheitToCelsius(currentTemp)
    } else if (currentTempUnitDisplayed == "3") {
        currentTemp = currentTemp - 273.15
    }
    return currentTemp
}

void sendAbsoluteHumidityEvent(BigDecimal deviceTempInCelsius, BigDecimal relativeHumidity) {
    if(relativeHumidity != null && deviceTempInCelsius != null) {
        BigDecimal numerator = (6.112 * Math.exp((17.67 * deviceTempInCelsius) / (deviceTempInCelsius + 243.5)) * relativeHumidity * 2.1674) 
        BigDecimal denominator = deviceTempInCelsius + 273.15 
        BigDecimal absHumidity = numerator / denominator
        String cubeChar = String.valueOf((char)(179))
        absHumidity = absHumidity.setScale(1, BigDecimal.ROUND_HALF_UP)
        logging("Sending Absolute Humidity event (Absolute Humidity: ${absHumidity}g/m${cubeChar})", 100)
        sendEvent( name: "absoluteHumidity", value: absHumidity, unit: "g/m${cubeChar}", descriptionText: "Absolute Humidity Is ${absHumidity} g/m${cubeChar}" )
    }
}

private BigDecimal sensor_data_getAdjustedHumidity(BigDecimal value) {
    Integer res = 1
    if(humidityRes != null && humidityRes != '') {
        res = Integer.parseInt(humidityRes)
    }
    if (humidityOffset) {
	   return (value + new BigDecimal(humidityOffset)).setScale(res, BigDecimal.ROUND_HALF_UP)
	} else {
       return value.setScale(res, BigDecimal.ROUND_HALF_UP)
    }
}

private BigDecimal sensor_data_getAdjustedPressure(BigDecimal value, Integer decimals=2) {
    Integer res = decimals
    if(pressureRes != null && pressureRes != '' && pressureRes != 'default') {
        res = Integer.parseInt(pressureRes)
    }
    if (pressureOffset) {
	   return (value + new BigDecimal(pressureOffset)).setScale(res, BigDecimal.ROUND_HALF_UP)
	} else {
       return value.setScale(res, BigDecimal.ROUND_HALF_UP)
    }
}

private BigDecimal sensor_data_convertPressure(BigDecimal pressureInkPa) {
    BigDecimal pressure = pressureInkPa
    switch(pressureUnitConversion) {
        case null:
        case "kPa":
			pressure = sensor_data_getAdjustedPressure(pressure / 10)
			break
		case "inHg":
			pressure = sensor_data_getAdjustedPressure(pressure * 0.0295299)
			break
		case "mmHg":
            pressure = sensor_data_getAdjustedPressure(pressure * 0.75006157)
			break
        case "atm":
			pressure = sensor_data_getAdjustedPressure(pressure / 1013.25, 5)
			break
        default:
            pressure = sensor_data_getAdjustedPressure(pressure, 1)
            break
    }
    return pressure
}
// END:  getHelperFunctions('sensor-data')

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
