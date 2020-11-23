/**
 *  Copyright 2020 Markus Liljergren (https://oh-lalabs.com)
 *
 *  Version: v1.0.1.1123
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
import hubitat.helper.HexUtils

metadata {
	definition (name: "Zigbee - Aqara Water Leak Sensor", namespace: "oh-lalabs.com", author: "Markus Liljergren", filename: "zigbee-aqara-water-leak-sensor", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-water-leak-sensor-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilitiesForZigbeeDevices()
        capability "Sensor"
        capability "PresenceSensor"
        capability "Initialize"
        capability "Refresh"
        // END:  getDefaultMetadataCapabilitiesForZigbeeDevices()
        
        capability "Battery"
        capability "WaterSensor"
        
        // BEGIN:getDefaultMetadataAttributes()
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        // BEGIN:getMetadataAttributesForLastCheckin()
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "number"
        attribute "notPresentCounter", "number"
        attribute "restoredCounter", "number"
        // END:  getMetadataAttributesForLastCheckin()
        // BEGIN:getZigbeeBatteryMetadataAttributes()
        attribute "batteryLastReplaced", "String"
        // END:  getZigbeeBatteryMetadataAttributes()
        attribute "lastWet", "String"
        attribute "lastDry", "String"

        // BEGIN:getZigbeeBatteryCommands()
        command "resetBatteryReplacedDate"
        // END:  getZigbeeBatteryCommands()
        // BEGIN:getCommandsForPresence()
        command "resetRestoredCounter"
        // END:  getCommandsForPresence()
        // BEGIN:getCommandsForZigbeePresence()
        command "forceRecoveryMode", [[name:"Minutes*", type: "NUMBER", description: "Maximum minutes to run in Recovery Mode"]]
        // END:  getCommandsForZigbeePresence()
        command "setAsDry"
		command "setAsWet"

        fingerprint deviceJoinName: "Aqara Water Leak Sensor (SJCGQ11LM)", model: "lumi.sensor_motion", profileId: "0104", endpointId: "01", inClusters: "0000,0003,0001", outClusters: "0019", manufacturer: "LUMI"
        fingerprint deviceJoinName: "Aqara Water Leak Sensor (SJCGQ11LM)", model: "lumi.sensor_wleak.aq1", profileId: "0104", endpointId: "01", inClusters: "0000,0003,0001", outClusters: "0019", manufacturer: "LUMI"
        fingerprint deviceJoinName: "Aqara Water Leak Sensor (SJCGQ11LM)", model: "lumi.sensor_wleak.aq1", endpointId: "01", application: "04"

        fingerprint model:"lumi.magnet.agl02", manufacturer:"LUMI", profileId:"0104", endpointId:"01", inClusters:"0000,0003,0500,0001", outClusters:"0019", application:"1C"
        
    }

    preferences {
        // BEGIN:getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        input(name: "debugLogging", type: "bool", title: styling_getLogo() + styling_addTitleDiv("Enable debug logging"), description: ""  + styling_getDefaultCSS(), defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        // BEGIN:getMetadataPreferencesForLastCheckin()
        input(name: "lastCheckinEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Date"), description: styling_addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
        input(name: "lastCheckinEpochEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Epoch"), description: styling_addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)
        input(name: "presenceEnable", type: "bool", title: styling_addTitleDiv("Enable Presence"), description: styling_addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
        input(name: "presenceWarningEnable", type: "bool", title: styling_addTitleDiv("Enable Presence Warning"), description: styling_addDescriptionDiv("Enables Presence Warnings in the Logs (default: true)"), defaultValue: true)
        // END:  getMetadataPreferencesForLastCheckin()
        // BEGIN:getMetadataPreferencesForRecoveryMode(defaultMode="Normal")
        input(name: "recoveryMode", type: "enum", title: styling_addTitleDiv("Recovery Mode"), description: styling_addDescriptionDiv("Select Recovery mode type (default: Normal)<br/>NOTE: The \"Insane\" and \"Suicidal\" modes may destabilize your mesh if run on more than a few devices at once!"), options: ["Disabled", "Slow", "Normal", "Insane", "Suicidal"], defaultValue: "Normal")
        // END:  getMetadataPreferencesForRecoveryMode(defaultMode="Normal")
        // BEGIN:getMetadataPreferencesForZigbeeDevicesWithBattery()
        input(name: "vMinSetting", type: "decimal", title: styling_addTitleDiv("Battery Minimum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 0% (default = 2.5V)"), defaultValue: "2.5", range: "2.1..2.8")
        input(name: "vMaxSetting", type: "decimal", title: styling_addTitleDiv("Battery Maximum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 100% (default = 3.0V)"), defaultValue: "3.0", range: "2.9..3.4")
        // END:  getMetadataPreferencesForZigbeeDevicesWithBattery()
        input(name: "logWetDryDatetime", type: "bool", title: styling_addTitleDiv("Log Wet/Dry Time"), description: styling_addDescriptionDiv("Logs the date and time of when the last Wet/Dry event occured (default: false)"), defaultValue: false)
	}
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
     
    Map deviceInfo = ['name': 'Zigbee - Aqara Water Leak Sensor', 'namespace': 'oh-lalabs.com', 'author': 'Markus Liljergren', 'filename': 'zigbee-aqara-water-leak-sensor', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-water-leak-sensor-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */

ArrayList<String> refresh() {
    logging("refresh() model='${getDeviceDataByName('model')}'", 10)
    
    getDriverVersion()
    configurePresence()
    startCheckEventInterval()
    resetBatteryReplacedDate(forced=false)
    setLogsOffTask(noLogWarning=true)
    
    String model = setCleanModelNameWithAcceptedModels()

    if(model == "lumi.flood.agl02") {
        logging("T1 Sensor Init", 100)
        bindAndRetrieveT1SensorData()
    }

    ArrayList<String> cmd = []
    
    logging("refresh cmd: $cmd", 1)
    /* refreshEvents() just sends all current states again, it's a hack for HubConnect */
    refreshEvents()
    return cmd
}

void bindAndRetrieveT1SensorData() {
    ArrayList<String> cmd = []
    String endpoint = '01'
    cmd += ["zdo bind ${device.deviceNetworkId} 0x$endpoint 0x01 0x0003 {${device.zigbeeId}} {}", "delay 187",]
    cmd += ["zdo bind ${device.deviceNetworkId} 0x$endpoint 0x01 0x0500 {${device.zigbeeId}} {}", "delay 188",]
    
    cmd += zigbee.readAttribute(0x0500, 0x0000)
    sendZigbeeCommands(cmd)
}

void initialize() {
    logging("initialize()", 100)
    unschedule()
    refresh()
}

void installed() {
    logging("installed()", 100)
    sendEvent(name:"water", value: "dry", descriptionText: "Sensor is RESET to dry", isStateChange: true)
    refresh()
}

void updated() {
    logging("updated()", 100)
    refresh()
}

String setCleanModelNameWithAcceptedModels(String newModelToSet=null) {
    return setCleanModelName(newModelToSet=newModelToSet, acceptedModels=[
        "lumi.sensor_motion.aq2",
        "lumi.sensor_wleak.aq1",
        "lumi.sensor_motion",
        "lumi.flood.agl02"
    ])
}

ArrayList<String> parse(String description) {
    // BEGIN:getGenericZigbeeParseHeader(loglevel=0)
    //logging("PARSE START---------------------", 0)
    //logging("Parsing: '${description}'", 0)
    ArrayList<String> cmd = []
    Map msgMap = null
    if(description.indexOf('encoding: 4C') >= 0) {
    
      msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 4C', 'encoding: F2'))
    
      msgMap = unpackStructInMap(msgMap)
    
    } else if(description.indexOf('attrId: FF01, encoding: 42') >= 0) {
      msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: F2'))
      msgMap["encoding"] = "41"
      msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false, hasLength=true)
    } else {
      if(description.indexOf('encoding: 42') >= 0) {
    
        List values = description.split("value: ")[1].split("(?<=\\G..)")
        String fullValue = values.join()
        Integer zeroIndex = values.indexOf("01")
        if(zeroIndex > -1) {
    
          //logging("zeroIndex: $zeroIndex, fullValue: $fullValue, string: ${values.take(zeroIndex).join()}", 0)
          msgMap = zigbee.parseDescriptionAsMap(description.replace(fullValue, values.take(zeroIndex).join()))
    
          values = values.drop(zeroIndex + 3)
          msgMap["additionalAttrs"] = [
              ["encoding": "41",
              "value": parseXiaomiStruct(values.join(), isFCC0=false, hasLength=true)]
          ]
        } else {
          msgMap = zigbee.parseDescriptionAsMap(description)
        }
      } else {
        msgMap = zigbee.parseDescriptionAsMap(description)
      }
    
      if(msgMap.containsKey("encoding") && msgMap.containsKey("value") && msgMap["encoding"] != "41" && msgMap["encoding"] != "42") {
        msgMap["valueParsed"] = zigbee_generic_decodeZigbeeData(msgMap["value"], msgMap["encoding"])
      }
      if(msgMap == [:] && description.indexOf("zone") == 0) {
    
        msgMap["type"] = "zone"
        java.util.regex.Matcher zoneMatcher = description =~ /.*zone.*status.*0x(?<status>([0-9a-fA-F][0-9a-fA-F])+).*extended.*status.*0x(?<statusExtended>([0-9a-fA-F][0-9a-fA-F])+).*/
        if(zoneMatcher.matches()) {
          msgMap["parsed"] = true
          msgMap["status"] = zoneMatcher.group("status")
          msgMap["statusInt"] = Integer.parseInt(msgMap["status"], 16)
          msgMap["statusExtended"] = zoneMatcher.group("statusExtended")
          msgMap["statusExtendedInt"] = Integer.parseInt(msgMap["statusExtended"], 16)
        } else {
          msgMap["parsed"] = false
        }
      }
    }
    //logging("msgMap: ${msgMap}", 0)
    // END:  getGenericZigbeeParseHeader(loglevel=0)
    logging("msgMap: ${msgMap}", 100)

    if(msgMap.containsKey("type") && msgMap["type"] == "zone" && msgMap["parsed"] == true) {
        logging("ZONE event - description:${description} | parseMap:${msgMap}", 1)
        if(msgMap["statusInt"] == 1) {
            sendEvent(name:"water", value: "wet", descriptionText: "Water leak detected!", isStateChange: true)
            if(logWetDryDatetime == true) {
                sendEvent(name: "lastWet", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
            }
        } else {
            sendEvent(name:"water", value: "dry", descriptionText: "Sensor is dry", isStateChange: true)
            if(logWetDryDatetime == true) {
                sendEvent(name: "lastDry", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
            }
        }
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0004") {
        logging("Manufacturer Name Received (from readAttribute command) - description:${description} | parseMap:${msgMap}", 1)
        
    } else if(msgMap["cluster"] == "0000" && (msgMap["attrId"] == "FF01" || msgMap["attrId"] == "FF02")) {
        logging("KNOWN event (Xiaomi/Aqara specific data structure with battery data) - description:${description} | parseMap:${msgMap}", 100)
        if(msgMap["encoding"] == "41") {
            parseAndSendBatteryStatus(msgMap['value']["battery"] / 1000.0)
        } else {
            parseAndSendBatteryStatus(msgMap['value'][1] / 1000.0)
        }
        
        logging("Sending request to cluster 0x0000 for attribute 0x0004 (response to attrId: 0x${msgMap["attrId"]}) 1", 1)
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))

    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0005") {
        logging("Reset button pressed - description:${description} | parseMap:${msgMap}", 1)
        setCleanModelNameWithAcceptedModels(newModelToSet=msgMap["value"])
        if(msgMap.containsKey("additionalAttrs") && msgMap["additionalAttrs"][0]["value"]["battery"] != null) {
            parseAndSendBatteryStatus(msgMap["additionalAttrs"][0]["value"]["battery"] / 1000.0)
        }
    } else if(msgMap["clusterId"] == "0013") {
        //logging("MULTISTATE CLUSTER EVENT - description:${description} | parseMap:${msgMap}", 0)
        if(getDeviceDataByName('model') == "lumi.flood.agl02") {
            logging("T1 Sensor Init", 100)
            bindAndRetrieveT1SensorData()
        }
    } else {
		log.warn "Unhandled Event PLEASE REPORT TO DEV - description:${description} | msgMap:${msgMap}"
	}

    if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=90) == false) {
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
    sendlastCheckinEvent(minimumMinutesToRepeat=30)
    
    // BEGIN:getGenericZigbeeParseFooter(loglevel=0)
    //logging("PARSE END-----------------------", 0)
    msgMap = null
    return cmd
    // END:  getGenericZigbeeParseFooter(loglevel=0)
}

void setAsDry() {
    sendEvent(name:"water", value: "dry", descriptionText: "Manually set to Dry", isStateChange: true)
    if(logWetDryDatetime == true) {
        sendEvent(name: "lastDry", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
    }
}

void setAsWet() {
    sendEvent(name:"water", value: "wet", descriptionText: "Manually set to Wet", isStateChange: true)
    if(logWetDryDatetime == true) {
        sendEvent(name: "lastWet", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
    }
}

/**
 *  --------- WRITE ATTRIBUTE METHODS ---------
 */

/**
 *   --------- READ ATTRIBUTE METHODS ---------
 */

/**
 *  -----------------------------------------------------------------------------
 *  Everything below here are LIBRARY includes and should NOT be edited manually!
 *  -----------------------------------------------------------------------------
 *  --- Nothings to edit here, move along! --------------------------------------
 *  -----------------------------------------------------------------------------
 */

// BEGIN:getDefaultFunctions()
private String getDriverVersion() {
    comment = "Works with model SJCGQ11LM."
    if(comment != "") state.comment = comment
    String version = "v1.0.1.1123"
    logging("getDriverVersion() = ${version}", 100)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
// END:  getDefaultFunctions()

// BEGIN:getLoggingFunction()
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
// END:  getLoggingFunction()

// BEGIN:getHelperFunctions('zigbee-generic')
private getCLUSTER_BASIC() { 0x0000 }
private getCLUSTER_POWER() { 0x0001 }
private getCLUSTER_WINDOW_COVERING() { 0x0102 }
private getCLUSTER_WINDOW_POSITION() { 0x000d }
private getCLUSTER_ON_OFF() { 0x0006 }
private getBASIC_ATTR_POWER_SOURCE() { 0x0007 }
private getPOWER_ATTR_BATTERY_PERCENTAGE_REMAINING() { 0x0021 }
private getPOSITION_ATTR_VALUE() { 0x0055 }
private getCOMMAND_OPEN() { 0x00 }
private getCOMMAND_CLOSE() { 0x01 }
private getCOMMAND_PAUSE() { 0x02 }
private getENCODING_SIZE() { 0x39 }

void updateNeededSettings() {
}

void refreshEvents() {
        
}

ArrayList<String> zigbeeCommand(Integer cluster, Integer command, Map additionalParams, int delay = 201, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, additionalParams, delay, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeCommand(Integer cluster, Integer command, int delay = 202, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, [:], delay, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeCommand(Integer endpoint, Integer cluster, Integer command, int delay = 203, String... payload) {
    zigbeeCommand(endpoint, cluster, command, [:], delay, payload)
}

ArrayList<String> zigbeeCommand(Integer endpoint, Integer cluster, Integer command, Map additionalParams, int delay = 204, String... payload) {
    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = " {${HexUtils.integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2)}}"
    }
    String finalPayload = payload != null && payload != [] ? payload[0] : ""
    String cmdArgs = "0x${device.deviceNetworkId} 0x${HexUtils.integerToHexString(endpoint, 1)} 0x${HexUtils.integerToHexString(cluster, 2)} " + 
                       "0x${HexUtils.integerToHexString(command, 1)} " + 
                       "{$finalPayload}" + 
                       "$mfgCode"
    ArrayList<String> cmd = ["he cmd $cmdArgs", "delay $delay"]
    return cmd
}

ArrayList<String> zigbeeWriteAttribute(Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 199) {
    ArrayList<String> cmd = zigbee.writeAttribute(cluster, attributeId, dataType, value, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeWriteAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 198) {
    logging("zigbeeWriteAttribute()", 1)
    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = " {${HexUtils.integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2)}}"
    }
    String wattrArgs = "0x${device.deviceNetworkId} $endpoint 0x${HexUtils.integerToHexString(cluster, 2)} " + 
                       "0x${HexUtils.integerToHexString(attributeId, 2)} " + 
                       "0x${HexUtils.integerToHexString(dataType, 1)} " + 
                       "{${HexUtils.integerToHexString(value, 1)}}" + 
                       "$mfgCode"
    ArrayList<String> cmd = ["he wattr $wattrArgs", "delay $delay"]
    
    logging("zigbeeWriteAttribute cmd=$cmd", 1)
    return cmd
}

ArrayList<String> zigbeeReadAttribute(Integer cluster, Integer attributeId, Map additionalParams = [:], int delay = 205) {
    ArrayList<String> cmd = zigbee.readAttribute(cluster, attributeId, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeReadAttribute(Integer endpoint, Integer cluster, Integer attributeId, int delay = 206) {
    ArrayList<String> cmd = ["he rattr 0x${device.deviceNetworkId} ${endpoint} 0x${HexUtils.integerToHexString(cluster, 2)} 0x${HexUtils.integerToHexString(attributeId, 2)} {}", "delay $delay"]
     
    return cmd
}

ArrayList<String> zigbeeWriteLongAttribute(Integer cluster, Integer attributeId, Integer dataType, Long value, Map additionalParams = [:], int delay = 207) {
    return zigbeeWriteLongAttribute(1, cluster, attributeId, dataType, value, additionalParams, delay)
}

ArrayList<String> zigbeeWriteLongAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Long value, Map additionalParams = [:], int delay = 208) {
    logging("zigbeeWriteLongAttribute()", 1)
    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = " {${HexUtils.integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2)}}"
    }
    String wattrArgs = "0x${device.deviceNetworkId} $endpoint 0x${HexUtils.integerToHexString(cluster, 2)} " + 
                       "0x${HexUtils.integerToHexString(attributeId, 2)} " + 
                       "0x${HexUtils.integerToHexString(dataType, 1)} " + 
                       "{${Long.toHexString(value)}}" + 
                       "$mfgCode"
    ArrayList<String> cmd = ["he wattr $wattrArgs", "delay $delay"]
    
    logging("zigbeeWriteLongAttribute cmd=$cmd", 1)
    return cmd
}

void sendZigbeeCommand(String cmd) {
    logging("sendZigbeeCommand(cmd=$cmd)", 1)
    sendZigbeeCommands([cmd])
}

void sendZigbeeCommands(ArrayList<String> cmd) {
    logging("sendZigbeeCommands(cmd=$cmd)", 1)
    hubitat.device.HubMultiAction allActions = new hubitat.device.HubMultiAction()
    cmd.each {
            allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
    }
    sendHubCommand(allActions)
}

String setCleanModelName(String newModelToSet=null, List<String> acceptedModels=null) {
    String model = newModelToSet != null ? newModelToSet : getDeviceDataByName('model')
    model = model == null ? "null" : model
    String newModel = model.replaceAll("[^A-Za-z0-9.\\-_ ]", "")
    boolean found = false
    if(acceptedModels != null) {
        acceptedModels.each {
            if(found == false && newModel.startsWith(it) == true) {
                newModel = it
                found = true
            }
        }
    }
    logging("dirty model = $model, clean model=$newModel", 1)
    updateDataValue('model', newModel)
    return newModel
}

void resetBatteryReplacedDate(boolean forced=true) {
    if(forced == true || device.currentValue('batteryLastReplaced') == null) {
        sendEvent(name: "batteryLastReplaced", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
    }
}

void parseAndSendBatteryStatus(BigDecimal vCurrent) {
    BigDecimal vMin = vMinSetting == null ? 2.5 : vMinSetting
    BigDecimal vMax = vMaxSetting == null ? 3.0 : vMaxSetting
    
    BigDecimal bat = 0
    if(vMax - vMin > 0) {
        bat = ((vCurrent - vMin) / (vMax - vMin)) * 100.0
    } else {
        bat = 100
    }
    bat = bat.setScale(0, BigDecimal.ROUND_HALF_UP)
    bat = bat > 100 ? 100 : bat
    
    vCurrent = vCurrent.setScale(3, BigDecimal.ROUND_HALF_UP)

    logging("Battery event: $bat% (V = $vCurrent)", 1)
    sendEvent(name:"battery", value: bat, unit: "%", isStateChange: false)
}

Map unpackStructInMap(Map msgMap, String originalEncoding="4C") {
     
    msgMap['encoding'] = originalEncoding
    List<String> values = msgMap['value'].split("(?<=\\G..)")
    logging("unpackStructInMap() values=$values", 1)
    Integer numElements = Integer.parseInt(values.take(2).reverse().join(), 16)
    values = values.drop(2)
    List r = []
    Integer cType = null
    List ret = null
    while(values != []) {
        cType = Integer.parseInt(values.take(1)[0], 16)
        values = values.drop(1)
        ret = zigbee_generic_convertStructValueToList(values, cType)
        r += ret[0]
        values = ret[1]
    }
    if(r.size() != numElements) throw new Exception("The STRUCT specifies $numElements elements, found ${r.size()}!")
     
    msgMap['value'] = r
    return msgMap
}

Map parseXiaomiStruct(String xiaomiStruct, boolean isFCC0=false, boolean hasLength=false) {
     
    Map tags = [
        '01': 'battery',
        '03': 'deviceTemperature',
        '04': 'unknown1',
        '05': 'RSSI_dB',
        '06': 'LQI',
        '07': 'unknown2',
        '08': 'unknown3',
        '09': 'unknown4',
        '0A': 'routerid',
        '0B': 'unknown5',
        '0C': 'unknown6',
        '6429': 'temperature',
        '6410': 'openClose',
        '6420': 'curtainPosition',
        '6521': 'humidity',
        '6510': 'switch2',
        '66': 'pressure',
        '6E': 'unknown10',
        '6F': 'unknown11',
        '95': 'consumption',
        '96': 'voltage',
        '98': 'power',
        '9721': 'gestureCounter1',
        '9739': 'consumption',
        '9821': 'gestureCounter2',
        '9839': 'power',
        '99': 'gestureCounter3',
        '9A21': 'gestureCounter4',
        '9A20': 'unknown7',
        '9A25': 'accelerometerXYZ',
        '9B': 'unknown9',
    ]
    if(isFCC0 == true) {
        tags['05'] = 'numBoots'
        tags['6410'] = 'onOff'
        tags['95'] = 'current'
    }

    List<String> values = xiaomiStruct.split("(?<=\\G..)")
    
    if(hasLength == true) values = values.drop(1)
    Map r = [:]
    r["raw"] = [:]
    String cTag = null
    String cTypeStr = null
    Integer cType = null
    String cKey = null
    List ret = null
    while(values != []) {
        cTag = values.take(1)[0]
        values = values.drop(1)
        cTypeStr = values.take(1)[0]
        cType = Integer.parseInt(cTypeStr, 16)
        values = values.drop(1)
        if(tags.containsKey(cTag+cTypeStr)) {
            cKey = tags[cTag+cTypeStr]
        } else if(tags.containsKey(cTag)) {
            cKey = tags[cTag]
        } else {
            cKey = "unknown${cTag}${cTypeStr}"
            log.warn("PLEASE REPORT TO DEV - The Xiaomi Struct used an unrecognized tag: 0x$cTag (type: 0x$cTypeStr) (struct: $xiaomiStruct)")
        }
        ret = zigbee_generic_convertStructValue(r, values, cType, cKey, cTag)
        r = ret[0]
        values = ret[1]
    }
     
    return r
}

Map parseAttributeStruct(List data, boolean hasLength=false) {
     
    Map tags = [
        '0000': 'ZCLVersion',
        '0001': 'applicationVersion',
        '0002': 'stackVersion',
        '0003': 'HWVersion',
        '0004': 'manufacturerName',
        '0005': 'dateCode',
        '0006': 'modelIdentifier',
        '0007': 'powerSource',
        '0010': 'locationDescription',
        '0011': 'physicalEnvironment',
        '0012': 'deviceEnabled',
        '0013': 'alarmMask',
        '0014': 'disableLocalConfig',
        '4000': 'SWBuildID',
    ]
    
    List<String> values = data
    
    if(hasLength == true) values = values.drop(1)
    Map r = [:]
    r["raw"] = [:]
    String cTag = null
    String cTypeStr = null
    Integer cType = null
    String cKey = null
    List ret = null
    while(values != []) {
        cTag = values.take(2).reverse().join()
        values = values.drop(2)
        values = values.drop(1)
        cTypeStr = values.take(1)[0]
        cType = Integer.parseInt(cTypeStr, 16)
        values = values.drop(1)
        if(tags.containsKey(cTag+cTypeStr)) {
            cKey = tags[cTag+cTypeStr]
        } else if(tags.containsKey(cTag)) {
            cKey = tags[cTag]
        } else {
            throw new Exception("The Xiaomi Struct used an unrecognized tag: 0x$cTag (type: 0x$cTypeStr)")
        }
        ret = zigbee_generic_convertStructValue(r, values, cType, cKey, cTag)
        r = ret[0]
        values = ret[1]
    }
     
    return r
}

def zigbee_generic_decodeZigbeeData(String value, String cTypeStr, boolean reverseBytes=true) {
    List values = value.split("(?<=\\G..)")
    values = reverseBytes == true ? values.reverse() : values
    Integer cType = Integer.parseInt(cTypeStr, 16)
    Map rMap = [:]
    rMap['raw'] = [:]
    List ret = zigbee_generic_convertStructValue(rMap, values, cType, "NA", "NA")
    return ret[0]["NA"]
}

List zigbee_generic_convertStructValueToList(List values, Integer cType) {
    Map rMap = [:]
    rMap['raw'] = [:]
    List ret = zigbee_generic_convertStructValue(rMap, values, cType, "NA", "NA")
    return [ret[0]["NA"], ret[1]]
}

List zigbee_generic_convertStructValue(Map r, List values, Integer cType, String cKey, String cTag) {
    String cTypeStr = cType != null ? integerToHexString(cType, 1) : null
    switch(cType) {
        case 0x10:
            r["raw"][cKey] = values.take(1)[0]
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16) != 0
            values = values.drop(1)
            break
        case 0x18:
        case 0x20:
            r["raw"][cKey] = values.take(1)[0]
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(1)
            break
        case 0x19:
        case 0x21:
            r["raw"][cKey] = values.take(2).reverse().join()
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(2)
            break
        case 0x1A:
        case 0x22:
            r["raw"][cKey] = values.take(3).reverse().join()
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(3)
            break
        case 0x1B:
        case 0x23:
            r["raw"][cKey] = values.take(4).reverse().join()
            r[cKey] = Long.parseLong(r["raw"][cKey], 16)
            values = values.drop(4)
            break
        case 0x1C:
        case 0x24:
            r["raw"][cKey] = values.take(5).reverse().join()
            r[cKey] = Long.parseLong(r["raw"][cKey], 16)
            values = values.drop(5)
            break
        case 0x1D:
        case 0x25:
            r["raw"][cKey] = values.take(6).reverse().join()
            r[cKey] = Long.parseLong(r["raw"][cKey], 16)
            values = values.drop(6)
            break
        case 0x1E:
        case 0x26:
            r["raw"][cKey] = values.take(7).reverse().join()
            r[cKey] = Long.parseLong(r["raw"][cKey], 16)
            values = values.drop(7)
            break
        case 0x1F:
        case 0x27:
            r["raw"][cKey] = values.take(8).reverse().join()
            r[cKey] = new BigInteger(r["raw"][cKey], 16)
            values = values.drop(8)
            break
        case 0x28:
            r["raw"][cKey] = values.take(1).reverse().join()
            r[cKey] = convertToSignedInt8(Integer.parseInt(r["raw"][cKey], 16))
            values = values.drop(1)
            break
        case 0x29:
            r["raw"][cKey] = values.take(2).reverse().join()
            r[cKey] = (Integer) (short) Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(2)
            break
        case 0x2B:
            r["raw"][cKey] = values.take(4).reverse().join()
            r[cKey] = (Integer) Long.parseLong(r["raw"][cKey], 16)
            values = values.drop(4)
            break
        case 0x30:
            r["raw"][cKey] = values.take(1)[0]
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(1)
            break
        case 0x31:
            r["raw"][cKey] = values.take(2).reverse().join()
            r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
            values = values.drop(2)
            break
        case 0x39:
            r["raw"][cKey] = values.take(4).reverse().join()
            r[cKey] = parseSingleHexToFloat(r["raw"][cKey])
            values = values.drop(4)
            break
        case 0x42:
            Integer strLength = Integer.parseInt(values.take(1)[0], 16)
            values = values.drop(1)
            r["raw"][cKey] = values.take(strLength)
            r[cKey] = r["raw"][cKey].collect { 
                (char)(int) Integer.parseInt(it, 16)
            }.join()
            values = values.drop(strLength)
            break
        default:
            throw new Exception("The Struct used an unrecognized type: $cTypeStr ($cType) for tag 0x$cTag with key $cKey (values: $values, map: $r)")
    }
    return [r, values]
}

ArrayList<String> zigbeeWriteHexStringAttribute(Integer cluster, Integer attributeId, Integer dataType, String value, Map additionalParams = [:], int delay = 209) {
    logging("zigbeeWriteBigIntegerAttribute()", 1)
    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = " {${integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2, reverse=true)}}"
    }
    String wattrArgs = "0x${device.deviceNetworkId} 0x01 0x${HexUtils.integerToHexString(cluster, 2)} " + 
                       "0x${HexUtils.integerToHexString(attributeId, 2)} " + 
                       "0x${HexUtils.integerToHexString(dataType, 1)} " + 
                       "{${value.split("(?<=\\G..)").reverse().join()}}" + 
                       "$mfgCode"
    ArrayList<String> cmd = ["he wattr $wattrArgs", "delay $delay"]
    
    logging("zigbeeWriteBigIntegerAttribute cmd=$cmd", 1)
    return cmd
}

ArrayList<String> zigbeeReadAttributeList(Integer cluster, List<Integer> attributeIds, Map additionalParams = [:], int delay = 211) {
    logging("zigbeeReadAttributeList()", 1)
    String mfgCode = "0000"
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = "${integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2, reverse=true)}"
        log.error "Manufacturer code support is NOT implemented!"
    }
    List<String> attributeIdsString = []
    attributeIds.each { attributeIdsString.add(integerToHexString(it, 2, reverse=true)) }
    logging("attributeIds=$attributeIds, attributeIdsString=$attributeIdsString", 100)
    String rattrArgs = "0x${device.deviceNetworkId} 1 0x01 0x${integerToHexString(cluster, 2)} " + 
                       "{000000${attributeIdsString.join()}}"
    ArrayList<String> cmd = ["he raw $rattrArgs", "delay $delay"]
    logging("zigbeeWriteLongAttribute cmd=$cmd", 1)
    return cmd
}

Float parseSingleHexToFloat(String singleHex) {
    return Float.intBitsToFloat(Long.valueOf(singleHex, 16).intValue())
}

Integer convertToSignedInt8(Integer signedByte) {
    Integer sign = signedByte & (1 << 7)
    return (signedByte & 0x7f) * (sign != 0 ? -1 : 1)
}

Integer parseIntReverseHex(String hexString) {
    return Integer.parseInt(hexString.split("(?<=\\G..)").reverse().join(), 16)
}

Long parseLongReverseHex(String hexString) {
    return Long.parseLong(hexString.split("(?<=\\G..)").reverse().join(), 16)
}

String integerToHexString(BigDecimal value, Integer minBytes, boolean reverse=false) {
    return integerToHexString(value.intValue(), minBytes, reverse=reverse)
}

String integerToHexString(Integer value, Integer minBytes, boolean reverse=false) {
    if(reverse == true) {
        return HexUtils.integerToHexString(value, minBytes).split("(?<=\\G..)").reverse().join()
    } else {
        return HexUtils.integerToHexString(value, minBytes)
    }
    
}

String bigIntegerToHexString(BigInteger value, Integer minBytes, boolean reverse=false) {
    if(reverse == true) {
        return value.toString(16).reverse().join()
    } else {
        return String.format("%0${minBytes*2}x", value)
    }
}

BigInteger hexStringToBigInteger(String hexString, boolean reverse=false) {
    if(reverse == true) {
        return new BigInteger(hexString.split("(?<=\\G..)").reverse().join(), 16)
    } else {
        return new BigInteger(hexString, 16)
    }
}

Integer miredToKelvin(Integer mired) {
    Integer t = mired
    if(t < 153) t = 153
    if(t > 500) t = 500
    t = Math.round(1000000/t)
    if(t > 6536) t = 6536
    if(t < 2000) t = 2000
    return t
}

Integer kelvinToMired(Integer kelvin) {
    Integer t = kelvin
    if(t > 6536) t = 6536
    if(t < 2000) t = 2000
    t = Math.round(1000000/t)
    if(t < 153) t = 153
    if(t > 500) t = 500
    return t
}

Integer getMaximumMinutesBetweenEvents(BigDecimal forcedMinutes=null) {
    Integer mbe = null
    if(forcedMinutes == null && (state.forcedMinutes == null || state.forcedMinutes == 0)) {
        mbe = MINUTES_BETWEEN_EVENTS == null ? 90 : MINUTES_BETWEEN_EVENTS
    } else {
        mbe = forcedMinutes != null ? forcedMinutes.intValue() : state.forcedMinutes.intValue()
    }
    return mbe
}

void reconnectEvent(BigDecimal forcedMinutes=null) {
    recoveryEvent(forcedMinutes)
}

void disableRecoveryDueToBug() {
    log.warn("Stopping Recovery feature due to Platform bug! Disabling the feature in Preferences. To use it again when the platform is stable, Enable it in Device Preferences.")
    unschedule('recoveryEvent')
    unschedule('reconnectEvent')
    device.updateSetting('recoveryMode', 'Disabled')
}

void recoveryEvent(BigDecimal forcedMinutes=null) {
    try {
        recoveryEventDeviceSpecific()
    } catch(Exception e) {
        logging("recoveryEvent()", 1)
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
    try {
        checkPresence(displayWarnings=false)
        Integer mbe = getMaximumMinutesBetweenEvents(forcedMinutes=forcedMinutes)
        if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe, displayWarnings=false) == true) {
            if(presenceWarningEnable == null || presenceWarningEnable == true) log.warn("Event interval normal, recovery mode DEACTIVATED!")
            unschedule('recoveryEvent')
            unschedule('reconnectEvent')
        }
    } catch(Exception e) {
        disableRecoveryDueToBug()
    }
    
}

void scheduleRecoveryEvent(BigDecimal forcedMinutes=null) {
    Random rnd = new Random()
    switch(recoveryMode) {
        case "Suicidal":
            schedule("${rnd.nextInt(15)}/15 * * * * ? *", 'recoveryEvent')
            break
        case "Insane":
            schedule("${rnd.nextInt(30)}/30 * * * * ? *", 'recoveryEvent')
            break
        case "Slow":
            schedule("${rnd.nextInt(59)} ${rnd.nextInt(3)}/3 * * * ? *", 'recoveryEvent')
            break
        case null:
        case "Normal":
        default:
            schedule("${rnd.nextInt(59)} ${rnd.nextInt(2)}/2 * * * ? *", 'recoveryEvent')
            break
    }
    recoveryEvent(forcedMinutes=forcedMinutes)
}

void checkEventInterval(boolean displayWarnings=true) {
    logging("recoveryMode: $recoveryMode", 1)
    if(recoveryMode == "Disabled") {
        unschedule('checkEventInterval')
    } else {
        prepareCounters()
        Integer mbe = getMaximumMinutesBetweenEvents()
        try {
            if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe) == false) {
                recoveryMode = recoveryMode == null ? "Normal" : recoveryMode
                if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("Event interval INCORRECT, recovery mode ($recoveryMode) ACTIVE! If this is shown every hour for the same device and doesn't go away after three times, the device has probably fallen off and require a quick press of the reset button or possibly even re-pairing. It MAY also return within 24 hours, so patience MIGHT pay off.")
                scheduleRecoveryEvent()
            }
        } catch(Exception e) {
            disableRecoveryDueToBug()
        }
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
}

void startCheckEventInterval() {
    logging("startCheckEventInterval()", 1)
    if(recoveryMode != "Disabled") {
        logging("Recovery feature ENABLED", 100)
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)}/59 * * * ? *", 'checkEventInterval')
        checkEventInterval(displayWarnings=true)
    } else {
        logging("Recovery feature DISABLED", 100)
        unschedule('checkEventInterval')
        unschedule('recoveryEvent')
        unschedule('reconnectEvent')
    }
}

void forceRecoveryMode(BigDecimal minutes) {
    minutes = minutes == null || minutes < 0 ? 0 : minutes
    Integer minutesI = minutes.intValue()
    logging("forceRecoveryMode(minutes=$minutesI) ", 1)
    if(minutesI == 0) {
        disableForcedRecoveryMode()
    } else if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=minutesI) == false) {
        recoveryMode = recoveryMode == null ? "Normal" : recoveryMode
        if(presenceWarningEnable == null || presenceWarningEnable == true) log.warn("Forced recovery mode ($recoveryMode) ACTIVATED!")
        state.forcedMinutes = minutes
        runIn(minutesI * 60, 'disableForcedRecoveryMode')

        scheduleRecoveryEvent(forcedMinutes=minutes)
    } else {
        log.warn("Forced recovery mode NOT activated since we already have a checkin event during the last $minutesI minute(s)!")
    }
}

void disableForcedRecoveryMode() {
    state.forcedMinutes = 0
    unschedule('recoveryEvent')
    unschedule('reconnectEvent')
    if(presenceWarningEnable == null || presenceWarningEnable == true) log.warn("Forced recovery mode DEACTIVATED!")
}

void updateManufacturer(String manfacturer) {
    if(getDataValue("manufacturer") == null) {
        updateDataValue("manufacturer", manfacturer)
    }
}

void updateApplicationId(String application) {
    if(getDataValue("application") == null) {
        updateDataValue("application", application)
    }
}

Integer retrieveEndpointId() {
    String endpointIdRaw = getDataValue("endpointId")
    BigDecimal endpointId = endpointIdRaw == null ? 1 : hexStringToBigInteger(endpointIdRaw)
    return endpointId.intValue()
}

Map parseSimpleDescriptorData(List<String> data) {
    Map<String,String> d = [:]
    if(data[1] == "00") {
        d["nwkAddrOfInterest"] = data[2..3].reverse().join()
        Integer ll = Integer.parseInt(data[4], 16)
        d["endpointId"] = data[5]
        d["profileId"] = data[6..7].reverse().join()
        d["applicationDevice"] = data[8..9].reverse().join()
        d["applicationVersion"] = data[10]
        Integer icn = Integer.parseInt(data[11], 16)
        Integer pos = 12
        Integer cPos = null
        d["inClusters"] = ""
        if(icn > 0) {
            (1..icn).each() {b->
                cPos = pos+((b-1)*2)
                d["inClusters"] += data[cPos..cPos+1].reverse().join()
                if(b < icn) {
                    d["inClusters"] += ","
                }
            }
        }
        pos += icn*2
        Integer ocn = Integer.parseInt(data[pos], 16)
        pos += 1
        d["outClusters"] = ""
        if(ocn > 0) {
            (1..ocn).each() {b->
                cPos = pos+((b-1)*2)
                d["outClusters"] += data[cPos..cPos+1].reverse().join()
                if(b < ocn) {
                    d["outClusters"] += ","
                }
            }
        }
        logging("d=$d, ll=$ll, icn=$icn, ocn=$ocn", 1)
    } else {
        log.warn("Incorrect Simple Descriptor Data received: $data")
    }
    return d
}

void updateDataFromSimpleDescriptorData(List<String> data) {
    Map<String,String> sdi = parseSimpleDescriptorData(data)
    if(sdi != [:]) {
        updateDataValue("endpointId", sdi['endpointId'])
        updateDataValue("profileId", sdi['profileId'])
        updateDataValue("inClusters", sdi['inClusters'])
        updateDataValue("outClusters", sdi['outClusters'])
        getInfo(true, sdi)
    } else {
        log.warn("No VALID Simple Descriptor Data received!")
    }
    sdi = null
}

void getInfo(boolean ignoreMissing=false, Map<String,String> sdi = [:]) {
    log.debug("Getting info for Zigbee device...")
    String endpointId = device.getEndpointId()
    endpointId = endpointId == null ? getDataValue("endpointId") : endpointId
    String profileId = getDataValue("profileId")
    String inClusters = getDataValue("inClusters")
    String outClusters = getDataValue("outClusters")
    String model = getDataValue("model")
    String manufacturer = getDataValue("manufacturer")
    String application = getDataValue("application")
    if(sdi != [:]) {
        endpointId = endpointId == null ? sdi['endpointId'] : endpointId
        profileId = profileId == null ? sdi['profileId'] : profileId
        inClusters = inClusters == null ? sdi['inClusters'] : inClusters
        outClusters = outClusters == null ? sdi['outClusters'] : outClusters
        sdi = null
    }
    String extraFingerPrint = ""
    boolean missing = false
    String requestingFromDevice = ", requesting it from the device. If it is a sleepy device you may have to wake it up and run this command again. Run this command again to get the new fingerprint."
    if(ignoreMissing==true) {
        requestingFromDevice = ". Try again."
    }
    if(manufacturer == null) {
        missing = true
        log.warn("Manufacturer name is missing for the fingerprint$requestingFromDevice")
        if(ignoreMissing==false) sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
    log.trace("Manufacturer: $manufacturer")
    if(model == null) {
        missing = true
        log.warn("Model name is missing for the fingerprint$requestingFromDevice")
        if(ignoreMissing==false) sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0005))
    }
    log.trace("Model: $model")
    if(application == null) {
        log.info("NOT IMPORTANT: Application ID is missing for the fingerprint$requestingFromDevice")
        if(ignoreMissing==false) sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0001))
    } else {
        extraFingerPrint += ", application:\"$application\""
    }
    log.trace("Application: $application")
    if(profileId == null || endpointId == null || inClusters == null || outClusters == null) {
        missing = true
        String endpointIdTemp = endpointId == null ? "01" : endpointId
        log.warn("One or multiple pieces of data needed for the fingerprint is missing$requestingFromDevice")
        if(ignoreMissing==false) sendZigbeeCommands(["he raw ${device.deviceNetworkId} 0 0 0x0004 {00 ${zigbee.swapOctets(device.deviceNetworkId)} $endpointIdTemp} {0x0000}"])
    }
    profileId = profileId == null ? "0104" : profileId
    if(missing == true) {
        log.info("INCOMPLETE - DO NOT SUBMIT THIS - TRY AGAIN: fingerprint model:\"$model\", manufacturer:\"$manufacturer\", profileId:\"$profileId\", endpointId:\"$endpointId\", inClusters:\"$inClusters\", outClusters:\"$outClusters\"" + extraFingerPrint)
    } else {
        log.info("COPY AND PASTE THIS ROW TO THE DEVELOPER: fingerprint model:\"$model\", manufacturer:\"$manufacturer\", profileId:\"$profileId\", endpointId:\"$endpointId\", inClusters:\"$inClusters\", outClusters:\"$outClusters\"" + extraFingerPrint)
    }
}
// END:  getHelperFunctions('zigbee-generic')

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
