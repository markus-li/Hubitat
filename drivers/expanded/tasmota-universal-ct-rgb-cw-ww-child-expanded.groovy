/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.4.0718Tb
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
    definition (name: "Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", namespace: "tasmota", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-ct-rgb-cw-ww-child-expanded.groovy") {
        capability "Actuator"
        capability "Switch"
        capability "Light"
		capability "SwitchLevel"
        capability "ChangeLevel"
        capability "ColorControl"
        capability "ColorTemperature"
        capability "ColorMode"
        capability "Refresh"
        capability "LightEffects"

        // BEGIN:getMinimumChildAttributes()
        attribute   "driver", "string"
        // END:  getMinimumChildAttributes()

        attribute  "effectNumber", "number"
        
        command "setPixelColor", [[name:"RGB*", type: "STRING", description: "RGB in HEX, eg: #FF0000"],
            [name:"Pixel Number*", type: "NUMBER", description: "Pixel to change the color of (1 to \"Addressable Pixels\")"]]
        command "setAddressableRotation", [[name:"Addressable Rotation*", type: "NUMBER", description: "1..512 = set amount of pixels to rotate (up to Addressable Pixels value)"]]
        command "setEffectWidth", [[name:"Addressable Effect Width*", type: "ENUM", description: "This width is used by Addressable pixel effects",
                                    constraints: ["0", "1", "2", "3", "4"]]]

        command "setColorByName", [[name:"Color Name*", type: "ENUM", description: "Choose a color",
                                    constraints: ["#FF0000":"Red",
                                                  "#00FF00":"Green",
                                                  "#0000FF":"Blue",
                                                  "#FFFF00":"Yellow",
                                                  "#00FFFF":"Cyan",
                                                  "#FF00FF":"Pink",
                                                  "#FFFFFFFFFF":"White"]]]

        // BEGIN:getMetadataCommandsForHandlingTasmotaRGBWDevices()
        command "setEffectWithSpeed", [[name:"Effect number*", type: "NUMBER", description: "Effect number to enable"],
            [name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setNextEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setPreviousEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setEffectSingleColor", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setEffectCycleUpColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setEffectCycleDownColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        command "setEffectRandomColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
        // END:  getMetadataCommandsForHandlingTasmotaRGBWDevices()
        // BEGIN:getMetadataCommandsForHandlingTasmotaDimmerDevices()
        command "modeWakeUp", [[name:"Wake Up Duration*", type: "NUMBER", description: "1..3000 = set wake up duration in seconds"],
                               [name:"Level", type: "NUMBER", description: "1..100 = target dimming level"] ]
        // END:  getMetadataCommandsForHandlingTasmotaDimmerDevices()
    }

    preferences {
        // BEGIN:getDefaultMetadataPreferences()
        input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences()
        input(name: "hideColorTemperatureCommands", type: "bool", title: styling_addTitleDiv("Hide Color Temperature Commands"), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "hideEffectCommands", type: "bool", title: styling_addTitleDiv("Hide Effect Commands"), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideColorCommands", type: "bool", title: styling_addTitleDiv("Hide Color Commands"), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "isAddressable", type: "bool", title: styling_addTitleDiv("Addressable Light"), description: styling_addDescriptionDiv("Treat as an Addressable Light"), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "addressablePixels", type: "number", title: styling_addTitleDiv("Addressable Pixels"), description: styling_addDescriptionDiv("1..512 = set amount of pixels in strip or ring and reset Rotation"), displayDuringSetup: false, required: false, defaultValue: 30)
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
     
    Map deviceInfo = ['name': 'Tasmota - Universal CT/RGB/RGB+CW+WW (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/tasmota-universal-ct-rgb-cw-ww-child-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        if (it.name in ["switch", "level", "RGB", "color", "colorName", "hue", "saturation",
                        "colorTemperature", "colorMode"]) {
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else if(it.name == "effectNumber") {
            logging(it.descriptionText, 100)
            sendEvent(name: "effectName", value: getLightEffectNameByNumber(it.value), isStateChange: false)
            sendEvent(it)
        } else {
            log.warn "Got '$it.name' attribute data, but doesn't know what to do with it! Did you choose the right device type?"
        }
    }
}

void installed() {
    log.info "installed()"
    device.removeSetting("logLevel")
    device.updateSetting("logLevel", "100")
    sendEvent(name: "colorMode", value: "CT")
    sendEvent(name: "colorTemperature", value: "3000")
    sendEvent(name: "hue", value: "0")
    sendEvent(name: "saturation", value: "0")
    sendEvent(name: "level", value: "100")
    sendEvent(name: "colorName", value: "Daylight")
    refresh()
}

void updated() {
    log.info "updated()"
    if(addressablePixels != null && addressablePixels != state.addressablePixels) {
        setAddressablePixels(addressablePixels.toInteger())
        state.addressablePixels = addressablePixels
    }
    // BEGIN:getChildComponentDefaultUpdatedContent()
    getDriverVersion()
    // END:  getChildComponentDefaultUpdatedContent()
    refresh()
}

void refresh() {
    // BEGIN:getChildComponentMetaConfigCommands()
    def metaConfig = clearThingsToHide()
    metaConfig = setDatasToHide(['metaConfig', 'isComponent', 'preferences', 'label', 'name'], metaConfig=metaConfig)
    // END:  getChildComponentMetaConfigCommands()

    metaConfig = setStateVariablesToHide(["mode", "effectnumber"], metaConfig=metaConfig)

    List commandsToHide = ["setEffect", "setNextEffect", "setPreviousEffect"]
    if(hideColorTemperatureCommands == true) {
        commandsToHide.addAll(["setColorTemperature"])
    }
    if(hideEffectCommands == null || hideEffectCommands == true) {
        commandsToHide.addAll(["setEffectWithSpeed", "setNextEffectWithSpeed", "setPreviousEffectWithSpeed", "modeWakeUp", "setEffectSingleColor", "setEffectCycleUpColors", "setEffectCycleDownColors", "setEffectRandomColors"])
    }
    if(hideColorCommands == null || hideColorCommands == true) {
        commandsToHide.addAll(["setColor", "setColorByName", "setHue", "setSaturation"])
    }
    
    Map lightEffects = [:]
    if(isAddressable == true) {
        lightEffects = [0: "Single Color", 1: "Wake Up", 2: "Cycle Up Colors", 3: "Cycle Down Colors", 
                        4: "Random Colors", 5: "Clock Mode", 6: "Candlelight Pattern", 7: "RGB Pattern",
                        8: "Christmas Pattern", 9: "Hanukkah Pattern", 10: "Kwanzaa Pattern",
                        11: "Rainbow Pattern", 12: "Fire Pattern"]
    } else {
        metaConfig = setStateVariablesToHide(["addressablePixels"], metaConfig=metaConfig)
        commandsToHide.addAll(["addressablePixel", "setEffectWidth", "setPixelColor", "setAddressableRotation"])
        metaConfig = setPreferencesToHide(["addressablePixels"], metaConfig=metaConfig)
        lightEffects = [0: "Single Color", 1: "Wake Up", 2: "Cycle Up Colors", 3: "Cycle Down Colors", 
                        4: "Random Colors"]
    }
    if(commandsToHide != []) metaConfig = setCommandsToHide(commandsToHide, metaConfig=metaConfig)

    sendEvent(name: "lightEffects", value: JsonOutput.toJson(lightEffects))
    parent?.componentRefresh(this.device)
}

void on() {
    parent?.componentOn(this.device)
}

void off() {
    parent?.componentOff(this.device)
}

void setLevel(BigDecimal level) {
    parent?.componentSetLevel(this.device, level)
}

void setLevel(BigDecimal level, BigDecimal duration) {
    parent?.componentSetLevel(this.device, level, duration)
}

void startLevelChange(String direction) {
    parent?.componentStartLevelChange(this.device, direction)
}

void stopLevelChange() {
    parent?.componentStopLevelChange(this.device)
}

void setColor(Map colormap) {
    parent?.componentSetColor(this.device, colormap)
}

void setHue(BigDecimal hue) {
    parent?.componentSetHue(this.device, hue)
}

void setSaturation(BigDecimal saturation) {
    parent?.componentSetSaturation(this.device, saturation)
}

void setColorTemperature(BigDecimal colortemperature) {
    parent?.componentSetColorTemperature(this.device, colortemperature)
}

void colorWhite() {
    parent?.componentWhite(this.device)
}

void colorRed() {
    parent?.componentSetRGB(this.device, 255, 0, 0)
}

void colorGreen() {
    parent?.componentSetRGB(this.device, 0, 255, 0)
}

void colorBlue() {
    parent?.componentSetRGB(this.device, 0, 0, 255)
}

void colorYellow() {
    parent?.componentSetRGB(this.device, 255, 255, 0)
}

void colorCyan() {
    parent?.componentSetRGB(this.device, 0, 255, 255)
}

void colorPink() {
    parent?.componentSetRGB(this.device, 255, 0, 255)
}

void setColorByName(String colorName) {
    logging("setColorByName(colorName ${colorName})", 1)
    String colorRGB = ""
    switch(colorName) {
        case "Red":
            colorRGB = "#FF0000"
            break
        case "Green":
            colorRGB = "#00FF00"
            break
        case "Blue":
            colorRGB = "#0000FF"
            break
        case "Yellow":
            colorRGB = "#FFFF00"
            break
        case "Cyan":
            colorRGB = "#00FFFF"
            break
        case "Pink":
            colorRGB = "#FF00FF"
            break
        default:
            colorRGB = "#FFFFFFFFFF"
    }
    setColorByRGBString(colorRGB)
}

void setColorByRGBString(String colorRGB) {
    logging("setColorByRGBString(colorRGB ${colorRGB})", 1)
    parent?.componentSetColorByRGBString(this.device, colorRGB)
}

void setEffect(BigDecimal effectnumber) {
    setEffectWithSpeed(effectnumber, 2)
}

Map getLightEffects() {
    String lightEffectsJSON = device.currentValue('lightEffects')
    Map lightEffects = [0: "Undefined"]
    if(lightEffectsJSON != null) {
        log.debug "lightEffectsJSON = $lightEffectsJSON"
        JsonSlurper jsonSlurper = new JsonSlurper()
        lightEffects = jsonSlurper.parseText(lightEffectsJSON)
    }
    return lightEffects
}

String getLightEffectNameByNumber(BigDecimal effectnumber) {
    Map lightEffects = getLightEffects()
    lightEffects.get(effectnumber.toString(), 'Unknown')
}

void setNextEffect() {
    setNextEffectWithSpeed(2)
}

void setPreviousEffect() {
    setPreviousEffectWithSpeed(2)
}

void setEffectWithSpeed(BigDecimal effectnumber, BigDecimal speed=3) {
    state.effectnumber = effectnumber
    parent?.componentSetEffect(this.device, effectnumber, speed)
}

void setNextEffectWithSpeed(BigDecimal speed=3) {
    logging("setNextEffectWithSpeed()", 10)
    if (state.effectnumber != null && state.effectnumber < getLightEffects().size() - 1) {
        state.effectnumber = state.effectnumber + 1
    } else {
        state.effectnumber = 0
    }
    setEffectWithSpeed(state.effectnumber, speed)
}

void setPreviousEffectWithSpeed(BigDecimal speed=3) {
    logging("setPreviousEffectWithSpeed()", 10)
    if (state.effectnumber != null && state.effectnumber > 0) {
        state.effectnumber = state.effectnumber - 1
    } else {
        state.effectnumber = getLightEffects().size() - 1
    }
    setEffectWithSpeed(state.effectnumber, speed)
}

void setEffectSingleColor(BigDecimal speed=3) {
    setEffectWithSpeed(0, speed)
}

void setEffectCycleUpColors(BigDecimal speed=3) {
    setEffectWithSpeed(2, speed)
}

void setEffectCycleDownColors(BigDecimal speed=3) {
    setEffectWithSpeed(3, speed)
}

void setEffectRandomColors(BigDecimal speed=3) {
    setEffectWithSpeed(4, speed)
}

void modeWakeUp(BigDecimal wakeUpDuration) {
    Integer level = device.currentValue('level')
    Integer nlevel = level > 10 ? level : 10
    modeWakeUp(wakeUpDuration, nlevel)
}

void modeWakeUp(BigDecimal wakeUpDuration, BigDecimal level) {
    state.effectnumber = 1
    parent?.componentModeWakeUp(this.device, wakeUpDuration, level)
}

void setPixelColor(String colorRGB, BigDecimal pixel) {
    parent?.componentSetPixelColor(this.device, colorRGB, pixel)
}

void setAddressablePixels(BigDecimal pixels) {
    parent?.componentSetAddressablePixels(this.device, pixels)
}

void setAddressableRotation(BigDecimal pixels) {
    parent?.componentSetAddressableRotation(this.device, pixels)
}

void setEffectWidth(String pixels) {
    setEffectWidth(pixels.toInteger())
}

void setEffectWidth(BigDecimal pixels) {
    parent?.componentSetEffectWidth(this.device, pixels)
}

/*
Fade between colours:
rule3 on Rules#Timer=1 do backlog color1 #ff0000; ruletimer2 10; endon on Rules#Timer=2 do backlog color1 #0000ff; ruletimer3 10; endon on Rules#Timer=3 do backlog color1 #00ff00; ruletimer1 10; endon
backlog rule3 1; color1 #ff0000; fade 1; speed 18; color1 #0000ff; ruletimer3 10;

Fade up to color:
backlog fade 0; dimmer 0; color2 #ff0000; fade 1; speed 20; dimmer 100;

Fade down from color:
backlog fade 0; dimmer 100; color2 #ff0000; fade 1; speed 20; dimmer 0;

Fade up and down:

rule3 on Rules#Timer=1 do backlog color2 #ff0000; dimmer 100; ruletimer2 10; endon on Rules#Timer=2 do backlog dimmer 0; ruletimer3 10; endon  on Rules#Timer=3 do backlog color2 #00ff00; dimmer 100; ruletimer4 10; endon on Rules#Timer=4 do backlog dimmer 0; ruletimer1 10; endon 
backlog rule3 1; fade 0; dimmer 0; color2 #ff0000; fade 1; speed 20; dimmer 100; ruletimer2 10;

To disable a running effect:
rule3 0
backlog

*/

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
    String version = "v1.0.4.0718Tb"
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
