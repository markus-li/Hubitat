/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.1.0503
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

/* 
    Inspired by a driver from shin4299 which can be found here:
    https://github.com/shin4299/XiaomiSJ/blob/master/devicetypes/shinjjang/xiaomi-curtain-b1.src/xiaomi-curtain-b1.groovy
*/

// BEGIN:getDefaultImports()
/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
// Used for MD5 calculations
import java.security.MessageDigest
// END:  getDefaultImports()

import hubitat.helper.HexUtils

metadata {
	definition (name: "Zigbee - Aqara Smart Curtain Motor", namespace: "markusl", author: "Markus Liljergren", vid: "generic-shade", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilitiesForZigbeeDevices()
        // Default Capabilities for Zigbee Devices
        capability "Sensor"
        capability "PresenceSensor"
        capability "Initialize"
        // END:  getDefaultMetadataCapabilitiesForZigbeeDevices()
        
        // Device Specific Capabilities
        capability "Refresh"
        capability "Battery"
        capability "PowerSource"
        capability "WindowShade"
        
        // These 4 capabilities are included to be compatible with integrations like Alexa:
        capability "Actuator"
        capability "Switch"
        capability "Light"
        capability "SwitchLevel"

        // BEGIN:getDefaultMetadataAttributes()
        // Default Attributes
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        // BEGIN:getDefaultZigbeeMetadataAttributes()
        // Default Zigbee Device Attributes
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "String"
        // END:  getDefaultZigbeeMetadataAttributes()
        
        command "stop"
        command "manualOpenEnable"
        command "manualOpenDisable"
        command "curtainOriginalDirection"
        command "curtainReverseDirection"
        command "trackDiscoveryMode"

        // Uncomment these Commands for TESTING, not needed normally:
        //command "getBattery"    // comment before release!
        //command "installed"     // just used for testing that Installed runs properly, comment before release!
        //command "sendAttribute", [[name:"Attribute*", type: "STRING", description: "Zigbee Attribute"]]
        //command "parse", [[name:"Description*", type: "STRING", description: "description"]]

        // Aqara Smart Curtain Motor (ZNCLDJ11LM)
        fingerprint profileId: "0104", endpointId: "01", inClusters: "0000,0004,0003,0005,000A,0102,000D,0013,0006,0001,0406", outClusters: "0019,000A,000D,0102,0013,0006,0001,0406", manufacturer: "LUMI", model: "lumi.curtain"
        
        // Aqara B1 Smart Curtain Motor (ZNCLDJ12LM)
		fingerprint endpointId: "01", profileId: "0104", deviceId: "0202", inClusters: "0000, 0003, 0102, 000D, 0013, 0001", outClusters: "0003, 000A", manufacturer: "LUMI", model: "lumi.curtain.hagl04", deviceJoinName: "Xiaomi Curtain B1"
	}

    preferences {
        // BEGIN:getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        // Default Preferences
        input(name: "debugLogging", type: "bool", title: addTitleDiv("Enable debug logging"), description: ""  + getDefaultCSS(), defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
        input(name: "infoLogging", type: "bool", title: addTitleDiv("Enable descriptionText logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
        // BEGIN:getDefaultMetadataPreferencesForZigbeeDevices()
        // Default Preferences for Zigbee Devices
        input(name: "lastCheckinEnable", type: "bool", title: addTitleDiv("Enable Last Checkin Date"), description: addDescriptionDiv("Records Date events if enabled"), defaultValue: true)
        input(name: "lastCheckinEpochEnable", type: "bool", title: addTitleDiv("Enable Last Checkin Epoch"), description: addDescriptionDiv("Records Epoch events if enabled"), defaultValue: false)
        input(name: "presenceEnable", type: "bool", title: addTitleDiv("Enable Presence"), description: addDescriptionDiv("Enables Presence to indicate if the device has sent data within the last 3 hours (REQUIRES at least one of the Checkin options to be enabled)"), defaultValue: true)
        // END:  getDefaultMetadataPreferencesForZigbeeDevices()
	}
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    Map deviceInfo = ['name': 'Zigbee - Aqara Smart Curtain Motor', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'vid': 'generic-shade', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-aqara-smart-curtain-motor-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()


/* These functions are unique to each driver */
// https://github.com/zigbeer/zcl-id/blob/master/definitions/cluster_defs.json
// https://github.com/zigbeer/zcl-id/blob/master/definitions/common.json

ArrayList<String> refresh() {
    logging("refresh() model='${getDeviceDataByName('model')}'", 10)
    // http://ftp1.digi.com/support/images/APP_NOTE_XBee_ZigBee_Device_Profile.pdf
    // https://docs.hubitat.com/index.php?title=Zigbee_Object
    // https://docs.smartthings.com/en/latest/ref-docs/zigbee-ref.html
    // https://www.nxp.com/docs/en/user-guide/JN-UG-3115.pdf

    getDriverVersion()
    configurePresence()
    setLogsOffTask(noLogWarning=true)

    ArrayList<String> cmd = []
    cmd += getPosition()
    cmd += zigbee.readAttribute(CLUSTER_BASIC, 0xFF01, [mfgCode: "0x115F"])
    //cmd += zigbee.readAttribute(CLUSTER_BASIC, 0xFF02, [mfgCode: "0x115F"])

    // Make sure the order of accepted models doesn't allow for an incorrect match
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

// Called from initialize()
void initializeAdditional() {
    logging("initializeAdditional()", 100)
    setCleanModelName()
    // Setting endpointId doesn't help
    //updateDataValue("endpointId", "01")
    makeSchedule()
    getDriverVersion()
}

// Called from installed()
void installedAdditional() {
    logging("installedAdditional()", 100)
    setCleanModelName()
    sendEvent(name:"windowShade", value: 'unknown')
    sendEvent(name:"switch", value: 'off')
    sendEvent(name:"level", value: 0)
    //sendEvent(name:"position", value: null)     // This set it to the string "null" in current versions of HE (2.2.0 and earlier)
}

void makeSchedule() {
    logging("makeSchedule()", 100)
    // https://www.freeformatter.com/cron-expression-generator-quartz.html
    if(getDeviceDataByName('model') != "lumi.curtain") {
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 5/12 * * ? *", 'getBattery')
    } else {
        unschedule('getBattery')
    }
}

ArrayList<String> parse(String description) {
    // BEGIN:getGenericZigbeeParseHeader(loglevel=1)
    // parse() Generic Zigbee-device header BEGINS here
    logging("PARSE START---------------------", 1)
    logging("Parsing: '${description}'", 1)
    ArrayList<String> cmd = []
    Map msgMap = null
    if(description.indexOf('encoding: 4C') >= 0) {
      // Parsing of STRUCT (4C) is broken in HE, for now we need a workaround
      msgMap = unpackStructInMap(zigbee.parseDescriptionAsMap(description.replace('encoding: 4C', 'encoding: F2')))
    } else if(description.indexOf('attrId: FF01, encoding: 42') >= 0) {
      msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: F2'))
      msgMap["encoding"] = "41"
      msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false, hasLength=true)
    } else {
      msgMap = zigbee.parseDescriptionAsMap(description)
    }
    logging("msgMap: ${msgMap}", 1)
    // parse() Generic header ENDS here
    // END:  getGenericZigbeeParseHeader(loglevel=1)
    //logging("msgMap: ${msgMap}", 1)

    if(msgMap["profileId"] == "0104" && msgMap["clusterId"] == "000A") {
		logging("Xiaomi Curtain Present Event", 1)
        sendlastCheckinEvent(minimumMinutesToRepeat=60)
	} else if(msgMap["profileId"] == "0104") {
        // TODO: Check if this is just a remnant and that we don't just catch this in the clause above?
        // This is probably just a heartbeat event...
        logging("Unhandled KNOWN 0104 event (heartbeat?)- description:${description} | parseMap:${msgMap}", 0)
        logging("RAW: ${msgMap["attrId"]}", 0)
        // Heartbeat event Description:
        // catchall: 0104 000A 01 01 0040 00 63A1 00 00 0000 00 00 0000

        // parseMap:[raw:catchall: 0104 000A 01 01 0040 00 63A1 00 00 0000 00 00 0000, profileId:0104, clusterId:000A, clusterInt:10, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:63A1, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:00, data:[00, 00]]
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0404") {
        if(msgMap["command"] == "0A") {
            if(msgMap["value"] == "00" && getDeviceDataByName('model') == "lumi.curtain") {
                // The position event that comes after this one is a real position
                logging("HANDLED KNOWN 0A command event with Value 00 - description:${description} | parseMap:${msgMap}", 1)
                logging("Sending request for the actual position...", 1)
                cmd += zigbee.readAttribute(CLUSTER_WINDOW_POSITION, 0x0055)
            } else {
                logging("Unhandled KNOWN 0A command event - description:${description} | parseMap:${msgMap}", 0)
            }
        } else {
            // Received after sending open/close/setposition commands
            logging("Unhandled KNOWN event - description:${description} | parseMap:${msgMap}", 0)
            //read attr - raw: 63A10100000804042000, dni: 63A1, endpoint: 01, cluster: 0000, size: 08, attrId: 0404, encoding: 20, command: 0A, value: 00, parseMap:[raw:63A10100000804042000, dni:63A1, endpoint:01, cluster:0000, size:08, attrId:0404, encoding:20, command:0A, value:00, clusterInt:0, attrInt:1028]
        }
    } else if(msgMap["clusterId"] == "0013" && msgMap["command"] == "00") {
        logging("Unhandled KNOWN event - description:${description} | parseMap:${msgMap}", 0)
        // Event Description:
        // read attr - raw: 63A1010000200500420C6C756D692E6375727461696E, dni: 63A1, endpoint: 01, cluster: 0000, size: 20, attrId: 0005, encoding: 42, command: 0A, value: 0C6C756D692E6375727461696E
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0005") {
        logging("Reset button pressed - description:${description} | parseMap:${msgMap}", 1)
        // The value from this command is the device model string
        setCleanModelName(newModelToSet=msgMap["value"])
        refresh()
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0006") {
        logging("Got a date - description:${description} | parseMap:${msgMap}", 1)
        // Sends a date, maybe product release date since it is the same on different devices?
        
        // This is sent when entering Track Discovery Mode

        // Original Curtain Description:
        // read attr - raw: 25D80100001C0600420A30382D31332D32303138, dni: 25D8, endpoint: 01, cluster: 0000, size: 1C, attrId: 0006, encoding: 42, command: 0A, value: 0A30382D31332D32303138
        // msgMap:[raw:25D80100001C0600420A30382D31332D32303138, dni:25D8, endpoint:01, cluster:0000, size:1C, attrId:0006, encoding:42, command:0A, value:08-13-2018, clusterInt:0, attrInt:6]
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0007") {
        logging("Handled KNOWN event (BASIC_ATTR_POWER_SOURCE) - description:${description} | parseMap:${msgMap}", 1)
        if(msgMap["value"] == "03") {
            sendEvent(name:"powerSource", value: "battery")
        } else if(msgMap["value"] == "04") {
            sendEvent(name:"powerSource", value: "dc")
        } else {
            sendEvent(name:"powerSource", value: "unknown")
        }
        // Description received for zigbee.readAttribute(CLUSTER_BASIC, BASIC_ATTR_POWER_SOURCE):
        // read attr - raw: 63A10100000A07003001, dni: 63A1, endpoint: 01, cluster: 0000, size: 0A, attrId: 0007, encoding: 30, command: 01, value: 01
    } else if(msgMap["cluster"] == "0102" && msgMap["attrId"] == "0008") {
        logging("Position event (after pressing stop) - description:${description} | parseMap:${msgMap}", 0)
        Long theValue = Long.parseLong(msgMap["value"], 16)
        curtainPosition = theValue.intValue()
        logging("GETTING POSITION from cluster 0102: int => ${curtainPosition}", 1)
        positionEvent(curtainPosition)
        // Position event Descriptions:
        //read attr - raw: 63A1010102080800204E, dni: 63A1, endpoint: 01, cluster: 0102, size: 08, attrId: 0008, encoding: 20, command: 0A, value: 4E
        //read attr - raw: 63A1010102080800203B, dni: 63A1, endpoint: 01, cluster: 0102, size: 08, attrId: 0008, encoding: 20, command: 0A, value: 3B
    } else if(msgMap["cluster"] == "0000" && (msgMap["attrId"] == "FF01" || msgMap["attrId"] == "FF02")) {
        if(msgMap["encoding"] == "42") {
            // First redo the parsing using a different encoding:
            msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: 41'))
            msgMap["encoding"] = "42"
            msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false)
        }
        logging("KNOWN event (Xiaomi/Aqara specific data structure) - description:${description} | parseMap:${msgMap}", 0)
        // Xiaomi/Aqara specific data structure, contains data we probably don't need
        // FF01 event Description from Original Curtain:
        // read attr - raw: A5C50100004001FF421C03281F05212B00642058082120110727000000000000000009210304, dni: A5C5, endpoint: 01, cluster: 0000, size: 40, attrId: FF01, encoding: 42, command: 0A, value: 1C03281F05212B00642058082120110727000000000000000009210304
        
        // read attr - raw: 25D80100004001FF421C03281E05212F00642064082120110727000000000000000009210104, dni: 25D8, endpoint: 01, cluster: 0000, size: 40, attrId: FF01, encoding: 42, command: 0A, value: 1C03281E05212F00642064082120110727000000000000000009210104
        // parseMap:[raw:25D80100004001FF421C03281E05212F00642064082120110727000000000000000009210104, dni:25D8, endpoint:01, cluster:0000, size:40, attrId:FF01, encoding:41, command:0A, value:[raw:[deviceTemperature:1E, RSSI_dB:002F, curtainPosition:64, unknown3:1120, unknown2:0000000000000000, unknown4:0401], deviceTemperature:30, RSSI_dB:47, curtainPosition:100, unknown3:4384, unknown2:0, unknown4:1025], clusterInt:0, attrInt:65281]
    } else if(msgMap["cluster"] == "000D" && msgMap["attrId"] == "0055") {
        logging("cluster 000D", 1)
		if(msgMap["size"] == "16" || msgMap["size"] == "1C" || msgMap["size"] == "10") {
            // This is sent just after sending a command to open/close and just after the curtain is done moving
			Long theValue = Long.parseLong(msgMap["value"], 16)
			BigDecimal floatValue = Float.intBitsToFloat(theValue.intValue());
			logging("GOT POSITION DATA: long => ${theValue}, BigDecimal => ${floatValue}", 1)
			curtainPosition = floatValue.intValue()
            if(getDeviceDataByName('model') != "lumi.curtain" && msgMap["command"] == "0A" && curtainPosition == 0) {
                logging("Sending a request for the actual position...", 1)
                cmd += zigbee.readAttribute(CLUSTER_WINDOW_POSITION, 0x0055)

            } else {
                logging("SETTING POSITION: long => ${theValue}, BigDecimal => ${floatValue}", 1)
                positionEvent(curtainPosition)
            }
		} else if(msgMap["size"] == "28" && msgMap["value"] == "00000000") {
			logging("Requesting Position", 1)
			cmd += zigbee.readAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE)
		}
	} else if(msgMap["cluster"] == "0001" && msgMap["attrId"] == "0021") {
        if(getDeviceDataByName('model') != "lumi.curtain") {
            def bat = msgMap["value"]
            Long value = Long.parseLong(bat, 16)/2
            logging("Battery: ${value}%, ${bat}", 1)
            sendEvent(name:"battery", value: value)
        }

	} else {
		log.warn "Unhandled Event - description:${description} | msgMap:${msgMap}"
	}
    
    // BEGIN:getGenericZigbeeParseFooter(loglevel=0)
    // parse() Generic Zigbee-device footer BEGINS here
    logging("PARSE END-----------------------", 0)
    return cmd
    // parse() Generic footer ENDS here
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
        // For Alexa:
        sendEvent(name:"level", value: curtainPosition)
        if(windowShadeStatus == "closed") {
            sendEvent(name:"switch", value: 'off')
        } else {
            sendEvent(name:"switch", value: 'on')
        }
    }
}

void updated() {
    logging("updated()", 10)
    try {
        // Also run initialize(), if it exists...
        initialize()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/*
    --------- WRITE ATTRIBUTE METHODS ---------
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
    logging("manualOpenEnable cmd=${cmd}", 0)
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
    logging("manualOpenDisable cmd=${cmd}", 0)
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
    logging("curtainOriginalDirection cmd=${cmd}", 0)
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
    logging("curtainReverseDirection cmd=${cmd}", 0)
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
    logging("trackDiscoveryMode cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> stop() {
    logging("stop()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeCommand(CLUSTER_WINDOW_COVERING, COMMAND_PAUSE)
    logging("stop cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> enableAutoClose() {
    logging("enableAutoClose()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x00, [mfgCode: "0x115F"])
    logging("enableAutoClose cmd=${cmd}", 0)
    return cmd
}

ArrayList<String> disableAutoClose() {
    logging("disableAutoClose()", 1)
    ArrayList<String> cmd = []
	cmd += zigbeeWriteAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x01, [mfgCode: "0x115F"])
    logging("disableAutoClose cmd=${cmd}", 0)
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
        logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_OPEN}", 0)
        cmd += zigbeeCommand(CLUSTER_ON_OFF, COMMAND_CLOSE)
    } else if(position < 1 && getDeviceDataByName('model') == "lumi.curtain") {
        logging("Command: Close", 1)
        logging("cluster: ${CLUSTER_ON_OFF}, command: ${COMMAND_CLOSE}", 0)
        cmd += zigbeeCommand(CLUSTER_ON_OFF, COMMAND_OPEN)
    } else {
        logging("Set Position: ${position}%", 1)
        //logging("zigbee.writeAttribute(getCLUSTER_WINDOW_POSITION()=${CLUSTER_WINDOW_POSITION}, getPOSITION_ATTR_VALUE()=${POSITION_ATTR_VALUE}, getENCODING_SIZE()=${ENCODING_SIZE}, position=${Float.floatToIntBits(position)})", 1)
        cmd += zigbeeWriteAttribute(CLUSTER_WINDOW_POSITION, POSITION_ATTR_VALUE, ENCODING_SIZE, Float.floatToIntBits(position))
    }
    logging("cmd=${cmd}", 1)
    sendZigbeeCommands(cmd)
    //return cmd
}

ArrayList<String> setLevel(level) {
    logging("setLevel(level: ${level})", 1)
    return setPosition(level)
}

ArrayList<String> setLevel(level, duration) {
    logging("setLevel(level: ${level})", 1)
    return setPosition(level)
}


/*
    --------- READ ATTRIBUTE METHODS ---------
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


/*
    -----------------------------------------------------------------------------
    Everything below here are LIBRARY includes and should NOT be edited manually!
    -----------------------------------------------------------------------------
    --- Nothings to edit here, move along! --------------------------------------
    -----------------------------------------------------------------------------
*/

// BEGIN:getDefaultFunctions()
/* Default Driver Methods go here */
private String getDriverVersion() {
    comment = "Works with models ZNCLDJ11LM & ZNCLDJ12LM"
    if(comment != "") state.comment = comment
    String version = "v1.0.1.0503"
    logging("getDriverVersion() = ${version}", 100)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
// END:  getDefaultFunctions()


// BEGIN:getLoggingFunction()
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
// END:  getLoggingFunction()


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
 * ZIGBEE GENERIC METHODS (helpers-zigbee-generic)
 *
 * Helper functions included in all Zigbee drivers
 */

/* --------- STATIC DEFINES --------- */
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


/* --------- GENERIC METHODS --------- */
void updateNeededSettings() {
    // Ignore, included for compatinility with the driver framework
}

// Used as a workaround to replace an incorrect endpoint
ArrayList<String> zigbeeCommand(Integer cluster, Integer command, Map additionalParams, int delay = 200, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, additionalParams, delay, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
    logging("zigbeeCommand() cmd=${cmd}", 0)
    return cmd
}

// Used as a workaround to replace an incorrect endpoint
ArrayList<String> zigbeeCommand(Integer cluster, Integer command, String... payload) {
    ArrayList<String> cmd = zigbee.command(cluster, command, payload)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
    logging("zigbeeCommand() cmd=${cmd}", 0)
    return cmd
}

// Used as a workaround to replace an incorrect endpoint
ArrayList<String> zigbeeWriteAttribute(Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 200) {
    ArrayList<String> cmd = zigbee.writeAttribute(cluster, attributeId, dataType, value, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
    logging("zigbeeWriteAttribute() cmd=${cmd}", 0)
    return cmd
}

// Used as a workaround to replace an incorrect endpoint
ArrayList<String> zigbeeReadAttribute(Integer cluster, Integer attributeId, Map additionalParams = [:], int delay = 200) {
    ArrayList<String> cmd = zigbee.readAttribute(cluster, attributeId, additionalParams, delay)
    cmd[0] = cmd[0].replace('0xnull', '0x01')
    logging("zigbeeReadAttribute() cmd=${cmd}", 0)
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
    // Clean the model name
    String model = newModelToSet != null ? newModelToSet : getDeviceDataByName('model')
    String newModel = model.replaceAll("[^A-Za-z0-9.\\-_]", "")
    if(acceptedModels != null) {
        acceptedModels.each {
            if(newModel.startsWith(it) == true) {
                newModel = it
            }
        }
    }
    logging("dirty model = $model, cleaned model=$newModel", 1)
    updateDataValue('model', newModel)
    return newModel
}

boolean isValidDate(String dateFormat, String dateString) {
    // TODO: Replace this with something NOT using try catch?
    try {
        Date.parse(dateFormat, dateString)
    } catch (e) {
        return false
    }
    return true
}

void sendlastCheckinEvent(Integer minimumMinutesToRepeat=55) {
    if (lastCheckinEnable == true || lastCheckinEnable == null) {
        String lastCheckinVal = device.currentValue('lastCheckin')
        if(lastCheckinVal == null || isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == false || now() >= Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime() + (minimumMinutesToRepeat * 60 * 1000)) {
		    sendEvent(name: "lastCheckin", value: new Date().format('yyyy-MM-dd HH:mm:ss'))
            logging("Updated lastCheckin", 1)
        } else {
            logging("Not updating lastCheckin since at least $minimumMinutesToRepeat minute(s) has not yet passed since last checkin.", 0)
        }
	}
    if (lastCheckinEpochEnable == true) {
		if(device.currentValue('lastCheckinEpoch') == null || now() >= device.currentValue('lastCheckinEpoch').toLong() + (minimumMinutesToRepeat * 60 * 1000)) {
		    sendEvent(name: "lastCheckinEpoch", value: now())
            logging("Updated lastCheckinEpoch", 1)
        } else {
            logging("Not updating lastCheckinEpoch since at least $minimumMinutesToRepeat minute(s) has not yet passed since last checkin.", 0)
        }
	}
}

void checkPresence() {
    Long lastCheckinTime = null
    String lastCheckinVal = device.currentValue('lastCheckin')
    if ((lastCheckinEnable == true || lastCheckinEnable == null) && isValidDate('yyyy-MM-dd HH:mm:ss', lastCheckinVal) == true) {
        lastCheckinTime = Date.parse('yyyy-MM-dd HH:mm:ss', lastCheckinVal).getTime()
    } else if (lastCheckinEpochEnable == true && device.currentValue('lastCheckinEpoch') != null) {
        lastCheckinTime = device.currentValue('lastCheckinEpoch').toLong()
    }
    if(lastCheckinTime != null && lastCheckinTime >= now() - (3 * 60 * 60 * 1000)) {
        // There was an event within the last 3 hours, all is well
        sendEvent(name: "presence", value: "present")
    } else {
        sendEvent(name: "presence", value: "not present")
        log.warn("No event seen from the device for over 3 hours! Something is not right...")
    }
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
    bat = bat.setScale(1, BigDecimal.ROUND_HALF_UP)
    bat = bat > 100 ? 100 : bat
    
    vCurrent = vCurrent.setScale(3, BigDecimal.ROUND_HALF_UP)

    logging("Battery event: $bat% (V = $vCurrent)", 1)
    sendEvent(name:"battery", value: bat, unit: "%", isStateChange: false)
}

Map unpackStructInMap(Map msgMap, String originalEncoding="4C") {
    // This is a LIMITED implementation, it only does what is needed by any of my drivers so far
    // This is NOT optimized for speed, it is just a convenient way of doing things
    logging("unpackStructInMap()", 0)
    msgMap['encoding'] = originalEncoding
    List<String> values = msgMap['value'].split("(?<=\\G..)")
    Integer numElements = Integer.parseInt(values.take(2).reverse().join(), 16)
    values = values.drop(2)
    List r = []
    while(values != []) {
        Integer cType = Integer.parseInt(values.take(1)[0], 16)
        values = values.drop(1)
        switch(cType) {
            case 0x10:
                // BOOLEAN
                r += Integer.parseInt(values.take(1)[0], 16) != 0
                values = values.drop(1)
                break
            case 0x20:
                // UINT8
                r += Integer.parseInt(values.take(1)[0], 16)
                values = values.drop(1)
                break
            case 0x21:
                // UINT16
                r += Integer.parseInt(values.take(2).reverse().join(), 16)
                values = values.drop(2)
                break
            case 0x22:
                // UINT24
                r += Integer.parseInt(values.take(3).reverse().join(), 16)
                values = values.drop(3)
                break
            case 0x23:
                // UINT24
                r += Long.parseLong(values.take(4).reverse().join(), 16)
                values = values.drop(4)
                break
            case 0x24:
                // UINT40
                r += Long.parseLong(values.take(5).reverse().join(), 16)
                values = values.drop(5)
                break
            case 0x25:
                // UINT48
                r += Long.parseLong(values.take(6).reverse().join(), 16)
                values = values.drop(6)
                break
            case 0x26:
                // UINT56
                r += Long.parseLong(values.take(7).reverse().join(), 16)
                values = values.drop(7)
                break
            case 0x27:
                // UINT64
                r += new BigInteger(values.take(8).reverse().join(), 16)
                values = values.drop(8)
                break
            case 0x28:
                // INT8
                r += convertToSignedInt8(Integer.parseInt(values.take(1).reverse().join(), 16))
                values = values.drop(1)
                break
            case 0x29:
                // INT16 - Short forces the sign
                r += (Integer) (short) Integer.parseInt(values.take(2).reverse().join(), 16)
                values = values.drop(2)
                break
            case 0x2B:
                // INT32 - Long to Integer forces the sign
                r += (Integer) Long.parseLong(values.take(4).reverse().join(), 16)
                values = values.drop(4)
                break
            case 0x39:
                // FLOAT - Single Precision
                r += Float.intBitsToFloat(Long.valueOf(values.take(4).reverse().join(), 16).intValue())
                values = values.drop(4)
                break
            default:
                throw new Exception("The STRUCT used an unrecognized type: $cType (0x${Long.toHexString(cType)})")
        }
    }
    if(r.size() != numElements) throw new Exception("The STRUCT specifies $numElements elements, found ${r.size()}!")
    logging("split: ${r}, numElements: $numElements", 0)
    msgMap['value'] = r
    return msgMap
}

Map parseXiaomiStruct(String xiaomiStruct, boolean isFCC0=false, boolean hasLength=false) {
    logging("parseXiaomiStruct()", 0)
    // https://github.com/dresden-elektronik/deconz-rest-plugin/wiki/Xiaomi-manufacturer-specific-clusters,-attributes-and-attribute-reporting
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
        switch(cType) {
            case 0x10:
                // BOOLEAN
                r["raw"][cKey] = values.take(1)[0]
                r[cKey] = Integer.parseInt(r["raw"][cKey], 16) != 0
                values = values.drop(1)
                break
            case 0x20:
                // UINT8
                r["raw"][cKey] = values.take(1)[0]
                r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
                values = values.drop(1)
                break
            case 0x21:
                // UINT16
                r["raw"][cKey] = values.take(2).reverse().join()
                r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
                values = values.drop(2)
                break
            case 0x22:
                // UINT24
                r["raw"][cKey] = values.take(3).reverse().join()
                r[cKey] = Integer.parseInt(r["raw"][cKey], 16)
                values = values.drop(3)
                break
            case 0x23:
                // UINT32
                r["raw"][cKey] = values.take(4).reverse().join()
                r[cKey] = Long.parseLong(r["raw"][cKey], 16)
                values = values.drop(4)
                break
            case 0x24:
                // UINT40
                r["raw"][cKey] = values.take(5).reverse().join()
                r[cKey] = Long.parseLong(r["raw"][cKey], 16)
                values = values.drop(5)
                break
            case 0x25:
                // UINT48
                r["raw"][cKey] = values.take(6).reverse().join()
                r[cKey] = Long.parseLong(r["raw"][cKey], 16)
                values = values.drop(6)
                break
            case 0x26:
                // UINT56
                r["raw"][cKey] = values.take(7).reverse().join()
                r[cKey] = Long.parseLong(r["raw"][cKey], 16)
                values = values.drop(7)
                break
            case 0x27:
                // UINT64
                r["raw"][cKey] = values.take(8).reverse().join()
                r[cKey] = new BigInteger(r["raw"][cKey], 16)
                values = values.drop(8)
                break
            case 0x28:
                // INT8
                r["raw"][cKey] = values.take(1).reverse().join()
                r[cKey] = convertToSignedInt8(Integer.parseInt(r["raw"][cKey], 16))
                values = values.drop(1)
                break
            case 0x29:
                // INT16 - Short forces the sign
                r["raw"][cKey] = values.take(2).reverse().join()
                r[cKey] = (Integer) (short) Integer.parseInt(r["raw"][cKey], 16)
                values = values.drop(2)
                break
            case 0x2B:
                // INT32 - Long to Integer forces the sign
                r["raw"][cKey] = values.take(4).reverse().join()
                r[cKey] = (Integer) Long.parseLong(r["raw"][cKey], 16)
                values = values.drop(4)
                break
            case 0x39:
                // FLOAT - Single Precision
                r["raw"][cKey] = values.take(4).reverse().join()
                r[cKey] = parseSingleHexToFloat(r["raw"][cKey])
                values = values.drop(4)
                break
            default:
                throw new Exception("The Xiaomi Struct used an unrecognized type: 0x$cTypeStr for tag 0x$cTag with key $cKey")
        }
    }
    logging("Values: $r", 0)
    return r
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

void configurePresence() {
    if(presenceEnable == null || presenceEnable == true) {
        sendEvent(name: "presence", value: "present")
        Random rnd = new Random()
        schedule("${rnd.nextInt(59)} ${rnd.nextInt(59)} 1/3 * * ? *", 'checkPresence')
    } else {
        unschedule('checkPresence')
    }
}

 /**
 * --END-- ZIGBEE GENERIC METHODS (helpers-zigbee-generic)
 */

// Not using the CSS styling features in this driver, so driver-metadata can be omitted
//#include:getHelperFunctions('driver-metadata')

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
