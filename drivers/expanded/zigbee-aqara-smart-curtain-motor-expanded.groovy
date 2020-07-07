/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.2.0707b
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

/* 
    Inspired by a driver from shin4299 which can be found here:
    github.com/shin4299/XiaomiSJ/blob/master/devicetypes/shinjjang/xiaomi-curtain-b1.src/xiaomi-curtain-b1.groovy
*/

// BEGIN:getDefaultImports()
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
 
import java.security.MessageDigest
// END:  getDefaultImports()
import hubitat.helper.HexUtils

metadata {
	definition (name: "Zigbee - Aqara Smart Curtain Motor", namespace: "markusl", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilitiesForZigbeeDevices()
        capability "Sensor"
        capability "PresenceSensor"
        capability "Initialize"
        // END:  getDefaultMetadataCapabilitiesForZigbeeDevices()
        
        capability "Refresh"
        capability "Battery"
        capability "PowerSource"
        capability "WindowShade"
        
        capability "Actuator"
        capability "Switch"
        capability "Light"
        capability "SwitchLevel"
        capability "ChangeLevel"

        // BEGIN:getDefaultMetadataAttributes()
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        // BEGIN:getMetadataAttributesForLastCheckin()
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "number"
        attribute "notPresentCounter", "number"
        attribute "restoredCounter", "number"
        // END:  getMetadataAttributesForLastCheckin()
        
        // BEGIN:getCommandsForPresence()
        command "resetRestoredCounter"
        // END:  getCommandsForPresence()
        command "stop"
        command "manualOpenEnable"
        command "manualOpenDisable"
        command "curtainOriginalDirection"
        command "curtainReverseDirection"
        command "trackDiscoveryMode"

        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0004,0003,0005,000A,0102,000D,0013,0006,0001,0406", outClusters: "0019,000A,000D,0102,0013,0006,0001,0406", manufacturer: "LUMI", model: "lumi.curtain"
        
		fingerprint endpointId: "01", profileId: "0104", deviceId: "0202", inClusters: "0000, 0003, 0102, 000D, 0013, 0001", outClusters: "0003, 000A", manufacturer: "LUMI", model: "lumi.curtain.hagl04", deviceJoinName: "Xiaomi Curtain B1"
	}

    preferences {
        // BEGIN:getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: ""  + styling_getDefaultCSS(), defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        // BEGIN:getMetadataPreferencesForLastCheckin()
        input(name: "lastCheckinEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Date"), description: styling_addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
        input(name: "lastCheckinEpochEnable", type: "bool", title: styling_addTitleDiv("Enable Last Checkin Epoch"), description: styling_addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)
        input(name: "presenceEnable", type: "bool", title: styling_addTitleDiv("Enable Presence"), description: styling_addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
        input(name: "presenceWarningEnable", type: "bool", title: styling_addTitleDiv("Enable Presence Warning"), description: styling_addDescriptionDiv("Enables Presence Warnings in the Logs (default: true)"), defaultValue: true)
        // END:  getMetadataPreferencesForLastCheckin()
	}
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
     
    Map deviceInfo = ['name': 'Zigbee - Aqara Smart Curtain Motor', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */

ArrayList<String> refresh() {
    logging("refresh() model='${getDeviceDataByName('model')}'", 10)

    getDriverVersion()
    configurePresence()
    startCheckEventInterval()
    setLogsOffTask(noLogWarning=true)

    ArrayList<String> cmd = []
    cmd += getPosition()
    cmd += zigbee.readAttribute(CLUSTER_BASIC, 0xFF01, [mfgCode: "0x115F"])

    String model = setCleanModelName(newModelToSet=null, acceptedModels=[
        "lumi.curtain.hagl04",
        "lumi.curtain"
    ])

    if(model != "lumi.curtain") { 
        cmd += getBattery()
    }
    logging("refresh cmd: $cmd", 1)
    sendZigbeeCommands(cmd)
    
}

void initialize() {
    logging("initialize()", 100)
    makeSchedule()
    refresh()
}

void installed() {
    logging("installed()", 100)
    sendEvent(name:"windowShade", value: 'unknown')
    sendEvent(name:"switch", value: 'off')
    sendEvent(name:"level", value: 0)
    makeSchedule()
    refresh()
}

void updated() {
    logging("updated()", 100)
    refresh()
}

void makeSchedule() {
    logging("makeSchedule()", 100)
    if(getDeviceDataByName('model') != "lumi.curtain") {
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 5/12 * * ? *", 'getBattery')
    } else {
        unschedule('getBattery')
    }
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

    if(msgMap["profileId"] == "0104" && msgMap["clusterId"] == "000A") {
		logging("Xiaomi Curtain Present Event", 1)
        if(sendlastCheckinEvent(minimumMinutesToRepeat=60) == true) {
            logging("Sending request to read attribute 0x0004 from cluster 0x0000...", 1)
            sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
        }
	} else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0004") {
        //logging("Manufacturer Name Received - description:${description} | parseMap:${msgMap}", 0)
        
    } else if(msgMap["profileId"] == "0104") {
        //logging("Unhandled KNOWN 0104 event (heartbeat?)- description:${description} | parseMap:${msgMap}", 0)
        //logging("RAW: ${msgMap["attrId"]}", 0)

    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0404") {
        if(msgMap["command"] == "0A") {
            if(msgMap["value"] == "00" && getDeviceDataByName('model') == "lumi.curtain") {
                logging("HANDLED KNOWN 0A command event with Value 00 - description:${description} | parseMap:${msgMap}", 1)
                logging("Sending request for the actual position...", 1)
                sendZigbeeCommands(zigbee.readAttribute(CLUSTER_WINDOW_POSITION, 0x0055))
            } else {
                //logging("Unhandled KNOWN 0A command event - description:${description} | parseMap:${msgMap}", 0)
            }
        } else {
            //logging("Unhandled KNOWN event - description:${description} | parseMap:${msgMap}", 0)
        }
    } else if(msgMap["clusterId"] == "0013") {
        //logging("MULTISTATE CLUSTER EVENT - description:${description} | parseMap:${msgMap}", 0)

    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0005") {
        logging("Reset button pressed - description:${description} | parseMap:${msgMap}", 1)
        setCleanModelName(newModelToSet=msgMap["value"])
        refresh()
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0006") {
        logging("Got a date - description:${description} | parseMap:${msgMap}", 1)
        
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0007") {
        logging("Handled KNOWN event (BASIC_ATTR_POWER_SOURCE) - description:${description} | parseMap:${msgMap}", 1)
        if(msgMap["value"] == "03") {
            sendEvent(name:"powerSource", value: "battery")
        } else if(msgMap["value"] == "04") {
            sendEvent(name:"powerSource", value: "dc")
        } else {
            sendEvent(name:"powerSource", value: "unknown")
        }
    } else if(msgMap["cluster"] == "0102" && msgMap["attrId"] == "0008") {
        logging("Position event (after pressing stop) - description:${description} | parseMap:${msgMap}", 1)
        Long theValue = Long.parseLong(msgMap["value"], 16)
        Integer curtainPosition = theValue.intValue()
        logging("GETTING POSITION from cluster 0102: int => ${curtainPosition}", 1)
        positionEvent(curtainPosition)
    } else if(msgMap["cluster"] == "0000" && (msgMap["attrId"] == "FF01" || msgMap["attrId"] == "FF02")) {
        if(msgMap["encoding"] == "42") {
            msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: 41'))
            msgMap["encoding"] = "42"
            msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false)
        }
        //logging("KNOWN event (Xiaomi/Aqara specific data structure) - description:${description} | parseMap:${msgMap}", 0)
        
    } else if(msgMap["cluster"] == "000D" && msgMap["attrId"] == "0055") {
        logging("Cluster 000D position event - description:${description} | parseMap:${msgMap}", 100)
		if(msgMap["size"] == "16" || msgMap["size"] == "1C" || msgMap["size"] == "10") {
			Long theValue = Long.parseLong(msgMap["value"], 16)
			BigDecimal floatValue = Float.intBitsToFloat(theValue.intValue());
			logging("GOT POSITION DATA: long => ${theValue}, BigDecimal => ${floatValue}", 100)
			curtainPosition = floatValue.intValue()
            if(getDeviceDataByName('model') != "lumi.curtain" && msgMap["command"] == "0A" && curtainPosition == 0) {
                logging("Sending a request for the actual position...", 100)
                sendZigbeeCommands(zigbee.readAttribute(CLUSTER_WINDOW_POSITION, 0x0055))

            } else {
                logging("SETTING POSITION: long => ${theValue}, BigDecimal => ${floatValue}", 100)
                positionEvent(curtainPosition)
            }
		} else if(msgMap["size"] == "28" && msgMap["value"] == "00000000") {
			logging("Requesting Position", 100)
			sendZigbeeCommands(zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE))
		}
	} else if(msgMap["cluster"] == "0001" && msgMap["attrId"] == "0021") {
        if(getDeviceDataByName('model') != "lumi.curtain") {
            Map bat = msgMap["value"]
            Long value = Long.parseLong(bat, 16)/2
            logging("Battery: ${value}%, ${bat}", 100)
            sendEvent(name:"battery", value: value)
        }
	} else {
		log.warn "Unhandled Event - description:${description} | msgMap:${msgMap}"
	}
    
    // BEGIN:getGenericZigbeeParseFooter(loglevel=0)
    //logging("PARSE END-----------------------", 0)
    msgMap = null
    return cmd
    // END:  getGenericZigbeeParseFooter(loglevel=0)
}

void positionEvent(Integer curtainPosition) {
	String windowShadeStatus = ""
	if(curtainPosition <= 2) curtainPosition = 0
    if(curtainPosition >= 98) curtainPosition = 100
    if(curtainPosition == 100) {
        logging("Fully Open", 1)
        windowShadeStatus = "open"
    } else if(curtainPosition > 0) {
        logging(curtainPosition + '% Partially Open', 1)
        windowShadeStatus = "partially open"
    } else {
        logging("Closed", 1)
        windowShadeStatus = "closed"
    }
    logging("device.currentValue('position') = ${device.currentValue('position')}, curtainPosition = $curtainPosition", 1)
    if(device.currentValue('position') == null || 
        curtainPosition < device.currentValue('position') - 1 || 
        curtainPosition > device.currentValue('position') + 1) {
        
        logging("CHANGING device.currentValue('position') = ${device.currentValue('position')}, curtainPosition = $curtainPosition", 1)
        sendEvent(name:"windowShade", value: windowShadeStatus)
        sendEvent(name:"position", value: curtainPosition)
        sendEvent(name:"level", value: curtainPosition)
        if(windowShadeStatus == "closed") {
            sendEvent(name:"switch", value: 'off')
        } else {
            sendEvent(name:"switch", value: 'on')
        }
    }
}

/**
 *  --------- WRITE ATTRIBUTE METHODS ---------
 */
ArrayList<String> open() {
    logging("open()", 1)
	return setPosition(100)    
}

ArrayList<String> on() {
    logging("on()", 1)
	return open()
}

ArrayList<String> close() {
    logging("close()", 1)
	return setPosition(0)    
}

ArrayList<String> off() {
    logging("off()", 1)
	return close()
}

ArrayList<String> startLevelChange(String direction) {
    logging("startLevelChange(direction=$direction)", 1)
	if(direction == "up") {
        return open()
    } else {
        return close()
    }
}

ArrayList<String> reverseCurtain() {
    logging("reverseCurtain()", 1)
	ArrayList<String> cmd = []
	cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF28, 0x10, 0x01, [mfgCode: "0x115F"])
    logging("cmd=${cmd}", 1)
    return cmd
}

ArrayList<String> manualOpenEnable() {
    logging("manualOpenEnable()", 1)
    ArrayList<String> cmd = []
    if(getDeviceDataByName('model') == "lumi.curtain") {
        cmd += zigbeeWriteLongAttribute(CLUSTER_BASIC, 0x0401, 0x42, 0x0700080000040012, [mfgCode: "0x115F"])
    } else {
        cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x00, [mfgCode: "0x115F"])
    }
    //logging("manualOpenEnable cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> manualOpenDisable() {
    logging("manualOpenDisable()", 1)
    ArrayList<String> cmd = []
    if(getDeviceDataByName('model') == "lumi.curtain") {
        cmd += zigbeeWriteLongAttribute(CLUSTER_BASIC, 0x0401, 0x42, 0x0700080000040112, [mfgCode: "0x115F"])
    } else {
        cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x01, [mfgCode: "0x115F"])
    }
    //logging("manualOpenDisable cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> curtainOriginalDirection() {
    logging("curtainOriginalDirection()", 1)
    ArrayList<String> cmd = []
    if(getDeviceDataByName('model') == "lumi.curtain") {
        cmd += zigbeeWriteLongAttribute(CLUSTER_BASIC, 0x0401, 0x42, 0x0700020000040012, [mfgCode: "0x115F"])
    } else {
        cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF28, 0x10, 0x00, [mfgCode: "0x115F"])
    }
    //logging("curtainOriginalDirection cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> curtainReverseDirection() {
    logging("curtainReverseDirection()", 1)
    ArrayList<String> cmd = []
    if(getDeviceDataByName('model') == "lumi.curtain") {
        cmd += zigbeeWriteLongAttribute(CLUSTER_BASIC, 0x0401, 0x42, 0x0700020001040012, [mfgCode: "0x115F"])
    } else {
        cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF28, 0x10, 0x01, [mfgCode: "0x115F"])
    }
    //logging("curtainReverseDirection cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> trackDiscoveryMode() {
    logging("trackDiscoveryMode()", 1)
    ArrayList<String> cmd = []
    if(getDeviceDataByName('model') == "lumi.curtain") {
        cmd += zigbeeWriteLongAttribute(CLUSTER_BASIC, 0x0401, 0x42, 0x0700010000040012, [mfgCode: "0x115F"])
    } else {
        cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF27, 0x10, 0x00, [mfgCode: "0x115F"])
    }
    //logging("trackDiscoveryMode cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> stop() {
    logging("stop()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeCommand(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
    //logging("stop cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> stopLevelChange() {
    logging("stop()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeCommand(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
    //logging("stop cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> enableAutoClose() {
    logging("enableAutoClose()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x00, [mfgCode: "0x115F"])
    //logging("enableAutoClose cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> disableAutoClose() {
    logging("disableAutoClose()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x01, [mfgCode: "0x115F"])
    //logging("disableAutoClose cmd=${cmd}", 0)
    return cmd
}

void setPosition(position) {
    if(position == null) {position = 0}
    if(position <= 2) position = 0
    if(position >= 98) position = 100
    ArrayList<String> cmd = []
    position = position as Integer
    logging("setPosition(position: ${position})", 1)
    Integer currentPosition = device.currentValue("position")
    if(position > currentPosition) {
        sendEvent(name: "windowShade", value: "opening")
    } else if(position < currentPosition) {
        sendEvent(name: "windowShade", value: "closing")
    }
    if(position == 100 && getDeviceDataByName('model') == "lumi.curtain") {
        logging("Command: Open", 1)
        //logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_OPEN}", 0)
        cmd += zigbeeCommand(CLUSTER_ON_OFF, COMMAND_CLOSE)
    } else if(position < 1 && getDeviceDataByName('model') == "lumi.curtain") {
        logging("Command: Close", 1)
        //logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_CLOSE}", 0)
        cmd += zigbeeCommand(CLUSTER_ON_OFF, COMMAND_OPEN)
    } else {
        logging("Set Position: ${position}%", 1)
        cmd += zigbeeWriteAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE, ENCODING_SIZE, Float.floatToIntBits(position))
    }
    logging("cmd=${cmd}", 1)
    sendZigbeeCommands(cmd)
}

ArrayList<String> setLevel(level) {
    logging("setLevel(level: ${level})", 1)
    return setPosition(level)
}

ArrayList<String> setLevel(level, duration) {
    logging("setLevel(level: ${level})", 1)
    return setPosition(level)
}

/**
 *   --------- READ ATTRIBUTE METHODS ---------
 */
ArrayList<String> getPosition() {
    logging("getPosition()", 1)
	ArrayList<String> cmd = []
	cmd += zigbeeReadAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE)
    logging("cmd: $cmd", 1)
    return cmd
}

ArrayList<String> getBattery() {
    logging("getBattery()", 100)
	ArrayList<String> cmd = []
    cmd += zigbeeReadAttribute(CLUSTER_POWER, POWER_ATTR_BATTERY_PERCENTAGE_REMAINING)
    cmd += zigbeeReadAttribute(CLUSTER_BASIC, BASIC_ATTR_POWER_SOURCE)
    logging("cmd: $cmd", 1)
    return cmd 
}

/**
 *  -----------------------------------------------------------------------------
 *  Everything below here are LIBRARY includes and should NOT be edited manually!
 *  -----------------------------------------------------------------------------
 *  --- Nothing to edit here, move along! ---------------------------------------
 *  -----------------------------------------------------------------------------
 */

// BEGIN:getDefaultFunctions()
private String getDriverVersion() {
    comment = "Works with models ZNCLDJ11LM & ZNCLDJ12LM."
    if(comment != "") state.comment = comment
    String version = "v1.0.2.0707b"
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

ArrayList<String> zigbeeCommand(Integer cluster, Integer command, Map additionalParams, int delay = 200, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, additionalParams, delay, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeCommand(Integer cluster, Integer command, int delay = 200, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, [:], delay, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeCommand(Integer endpoint, Integer cluster, Integer command, int delay = 200, String... payload) {
    zigbeeCommand(endpoint, cluster, command, [:], delay, payload)
}

ArrayList<String> zigbeeCommand(Integer endpoint, Integer cluster, Integer command, Map additionalParams, int delay = 200, String... payload) {
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

ArrayList<String> zigbeeWriteAttribute(Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 200) {
    ArrayList<String> cmd = zigbee.writeAttribute(cluster, attributeId, dataType, value, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeReadAttribute(Integer cluster, Integer attributeId, Map additionalParams = [:], int delay = 200) {
    ArrayList<String> cmd = zigbee.readAttribute(cluster, attributeId, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
     
    return cmd
}

ArrayList<String> zigbeeReadAttribute(Integer endpoint, Integer cluster, Integer attributeId, int delay = 200) {
    ArrayList<String> cmd = ["he rattr 0x${device.deviceNetworkId} ${endpoint} 0x${HexUtils.integerToHexString(cluster, 2)} 0x${HexUtils.integerToHexString(attributeId, 2)} {}", "delay 200"]
     
    return cmd
}

ArrayList<String> zigbeeWriteLongAttribute(Integer cluster, Integer attributeId, Integer dataType, Long value, Map additionalParams = [:], int delay = 200) {
    logging("zigbeeWriteLongAttribute()", 1)
    String mfgCode = ""
    if(additionalParams.containsKey("mfgCode")) {
        mfgCode = " {${HexUtils.integerToHexString(HexUtils.hexStringToInt(additionalParams.get("mfgCode")), 2)}}"
    }
    String wattrArgs = "0x${device.deviceNetworkId} 0x01 0x${HexUtils.integerToHexString(cluster, 2)} " + 
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
        if(it.startsWith('delay') == true) {
            allActions.add(new hubitat.device.HubAction(it))
        } else {
            allActions.add(new hubitat.device.HubAction(it, hubitat.device.Protocol.ZIGBEE))
        }
    }
    sendHubCommand(allActions)
}

String setCleanModelName(String newModelToSet=null, List<String> acceptedModels=null) {
    String model = newModelToSet != null ? newModelToSet : getDeviceDataByName('model')
    model = model == null ? "null" : model
    String newModel = model.replaceAll("[^A-Za-z0-9.\\-_]", "")
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
        '0A': 'unknown5',
        '0B': 'unknown6',
        '0C': 'unknown6',
        '6429': 'temperature',
        '6410': 'openClose',
        '6420': 'curtainPosition',
        '65': 'humidity',
        '66': 'pressure',
        '6E': 'unknown10',
        '95': 'consumption',
        '96': 'voltage',
        '9721': 'gestureCounter1',
        '9739': 'consumption',
        '9821': 'gestureCounter2',
        '9839': 'power',
        '99': 'gestureCounter3',
        '9A21': 'gestureCounter4',
        '9A20': 'unknown7',
        '9A25': 'unknown8',
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
            throw new Exception("The Xiaomi Struct used an unrecognized tag: 0x$cTag (type: 0x$cTypeStr)")
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

ArrayList<String> zigbeeWriteHexStringAttribute(Integer cluster, Integer attributeId, Integer dataType, String value, Map additionalParams = [:], int delay = 200) {
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

ArrayList<String> zigbeeReadAttributeList(Integer cluster, List<Integer> attributeIds, Map additionalParams = [:], int delay = 2000) {
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
    Integer sign = signedByte & (1 << 7);
    return (signedByte & 0x7f) * (sign != 0 ? -1 : 1);
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

void reconnectEvent() {
    try {
        reconnectEventDeviceSpecific()
    } catch(Exception e) {
        logging("reconnectEvent()", 1)
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
    checkPresence(displayWarnings=false)
    Integer mbe = MINUTES_BETWEEN_EVENTS == null ? 90 : MINUTES_BETWEEN_EVENTS
    if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe, displayWarnings=false) == true) {
        if(presenceWarningEnable == null || presenceWarningEnable == true) log.warn("Event interval normal, reconnect mode DEACTIVATED!")
        unschedule('reconnectEvent')
    }
}

void checkEventInterval(boolean displayWarnings=true) {
    prepareCounters()
    Integer mbe = MINUTES_BETWEEN_EVENTS == null ? 90 : MINUTES_BETWEEN_EVENTS
    if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe) == false) {
        if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("Event interval INCORRECT, reconnect mode ACTIVE! If this is shown every hour for the same device and doesn't go away after three times, the device has probably fallen off and require a quick press of the reset button or possibly even re-pairing. It MAY also return within 24 hours, so patience MIGHT pay off.")
        Random rnd = new Random()
        schedule("${rnd.nextInt(15)}/15 * * * * ? *", 'reconnectEvent')
    }
    sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
}

void startCheckEventInterval() {
    logging("startCheckEventInterval()", 100)
    Random rnd = new Random()
    schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)}/59 * * * ? *", 'checkEventInterval')
    checkEventInterval(displayWarnings=true)
}
// END:  getHelperFunctions('zigbee-generic')

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
    } else {
        unschedule('checkPresence')
    }
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

boolean sendlastCheckinEvent(Integer minimumMinutesToRepeat=55) {
    boolean r = false
    if (lastCheckinEnable == true || lastCheckinEnable == null) {
        String lastCheckinVal = device.currentValue('lastCheckin')
        if(lastCheckinVal == null || isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == false || now() >= Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime() + (minimumMinutesToRepeat * 60 * 1000)) {
            r = true
		    sendEvent(name: "lastCheckin", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
            logging("Updated lastCheckin", 1)
        } else {
             
        }
	}
    if (lastCheckinEpochEnable == true) {
		if(device.currentValue('lastCheckinEpoch') == null || now() >= device.currentValue('lastCheckinEpoch').toLong() + (minimumMinutesToRepeat * 60 * 1000)) {
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
