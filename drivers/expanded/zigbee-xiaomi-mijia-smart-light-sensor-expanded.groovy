/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v0.6.1.0503
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

// BEGIN:getDefaultImports()
/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
// Used for MD5 calculations
import java.security.MessageDigest
// END:  getDefaultImports()

import hubitat.helper.HexUtils

metadata {
	definition (name: "Zigbee - Xiaomi Mijia Smart Light Sensor (Zigbee 3.0)", namespace: "markusl", author: "Markus Liljergren", vid: "generic-shade", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-xiaomi-mijia-smart-light-sensor-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilitiesForZigbeeDevices()
        // Default Capabilities for Zigbee Devices
        capability "Sensor"
        capability "PresenceSensor"
        capability "Initialize"
        // END:  getDefaultMetadataCapabilitiesForZigbeeDevices()
        
        // Device Specific Capabilities
        //capability "Configuration"
        capability "Battery"
        capability "IlluminanceMeasurement"
        
        // BEGIN:getDefaultMetadataAttributes()
        // Default Attributes
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()
        // BEGIN:getDefaultZigbeeMetadataAttributes()
        // Default Zigbee Device Attributes
        attribute "lastCheckin", "Date"
        attribute "lastCheckinEpoch", "String"
        // END:  getDefaultZigbeeMetadataAttributes()
        // BEGIN:getZigbeeBatteryMetadataAttributes()
        // Default Zigbee Battery Device Attributes
        attribute "batteryLastReplaced", "String"
        // END:  getZigbeeBatteryMetadataAttributes()

        // BEGIN:getZigbeeBatteryCommands()
        // Commands used for Battery
        command "resetBatteryReplacedDate"
        // END:  getZigbeeBatteryCommands()

        // Uncomment these Commands for TESTING, not needed normally:
        //command "getBattery"    // comment before release!
        //command "installed"     // just used for testing that Installed runs properly, comment before release!
        //command "sendAttribute", [[name:"Attribute*", type: "STRING", description: "Zigbee Attribute"]]
        //command "parse", [[name:"Description*", type: "STRING", description: "description"]]
        //command "configureAdditional"
        //command "testLinear"
        //command "filterDatasetFromString", [[name:"Dataset*", type: "STRING", description: "dataset"]]

        // Xiaomi Mijia Smart Light Sensor (GZCGQ01LM)
        fingerprint deviceJoinName: "Xiaomi Mijia Smart Light Sensor (GZCGQ01LM)", model: "lumi.sen_ill.mgl01", profileId: "0104", inClusters: "0000,0400,0003,0001", outClusters: "0003", manufacturer: "LUMI", endpointId: "01", deviceId: "0104"	
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
        // BEGIN:getMetadataPreferencesForZigbeeDevicesWithBattery()
        // Preferences for Zigbee Devices with Battery
        input(name: "vMinSetting", type: "decimal", title: addTitleDiv("Battery Minimum Voltage"), description: addDescriptionDiv("Voltage when battery is considered to be at 0% (default = 2.5V)"), defaultValue: "2.5", range: "2.1..2.8")
        input(name: "vMaxSetting", type: "decimal", title: addTitleDiv("Battery Maximum Voltage"), description: addDescriptionDiv("Voltage when battery is considered to be at 100% (default = 3.0V)"), defaultValue: "3.0", range: "2.9..3.4")
        // END:  getMetadataPreferencesForZigbeeDevicesWithBattery()
        input(name: "secondsMinLux", type: "number", title: addTitleDiv("Minimum Update Time"), description: addDescriptionDiv("Set the minimum number of seconds between Lux updates (5 to 3600, default: 10)"), defaultValue: "10", range: "5..3600")
	}
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    Map deviceInfo = ['name': 'Zigbee - Xiaomi Mijia Smart Light Sensor (Zigbee 3.0)', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'vid': 'generic-shade', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/zigbee-xiaomi-mijia-smart-light-sensor-expanded.groovy']
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
    
    // Make sure the order of accepted models doesn't allow for an incorrect match
    setCleanModelName(newModelToSet=null, acceptedModels=[
        "lumi.sen_ill.mgl01"
    ])

    ArrayList<String> cmd = []
    //cmd += zigbee.readAttribute(0x001, 0)
    
    // Specific to the Xiaomi Light Sensor
    //cmd += zigbee.readAttribute(0xFCC0, 0x0007, [mfgCode: "0x126E"])

    // This mfg-specific attribute is written to with an octet String (0x41) - this is NOT the way to send it:
    //cmd += zigbee.writeAttribute(0xFCC0, 0x0008, 0x41, "1035b63376ed5b8df8f8b4f5b2550b7c4a", [mfgCode: "0x126E"])

    logging("refresh cmd: $cmd", 1)
    return cmd
}

// Called from installed()
void installedAdditional() {
    logging("installedAdditional()", 100)
    refresh()
    resetBatteryReplacedDate()
}

ArrayList<String> parse(String description) {
    //log.debug "in parse"
    // BEGIN:getGenericZigbeeParseHeader(loglevel=0)
    // parse() Generic Zigbee-device header BEGINS here
    logging("PARSE START---------------------", 0)
    logging("Parsing: '${description}'", 0)
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
    logging("msgMap: ${msgMap}", 0)
    // parse() Generic header ENDS here
    // END:  getGenericZigbeeParseHeader(loglevel=0)
    //logging("msgMap: ${msgMap}", 1)

    sendlastCheckinEvent(minimumMinutesToRepeat=55)

    // description:catchall: 0000 0006 00 00 0040 00 930E 00 00 0000 00 00 D6FDFF040101190000 | 
    // msgMap:[raw:catchall: 0000 0006 00 00 0040 00 930E 00 00 0000 00 00 D6FDFF040101190000, profileId:0000, 
    // clusterId:0006, clusterInt:6, sourceEndpoint:00, destinationEndpoint:00, options:0040, messageType:00, 
    // dni:930E, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, 
    // direction:00, data:[D6, FD, FF, 04, 01, 01, 19, 00, 00]]
    
    // Together: cluster: 0000 and attrId: 0005
    // description:catchall: 0104 0003 01 FF 0040 00 930E 01 00 0000 01 00  | 
    // msgMap:[raw:catchall: 0104 0003 01 FF 0040 00 930E 01 00 0000 01 00 , profileId:0104, clusterId:0003, 
    // clusterInt:3, sourceEndpoint:01, destinationEndpoint:FF, options:0040, messageType:00, dni:930E, 
    // isClusterSpecific:true, isManufacturerSpecific:false, manufacturerId:0000, command:01, direction:00, data:[]]

    // description:catchall: 0000 0013 00 00 0040 00 7361 00 00 0000 00 00 D36173BC29773CDF8CCF0484 
    // | msgMap:[raw:catchall: 0000 0013 00 00 0040 00 7361 00 00 0000 00 00 D36173BC29773CDF8CCF0484, 
    // profileId:0000, clusterId:0013, clusterInt:19, sourceEndpoint:00, destinationEndpoint:00, options:0040, 
    // messageType:00, dni:7361, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, 
    // command:00, direction:00, data:[D3, 61, 73, BC, 29, 77, 3C, DF, 8C, CF, 04, 84]]

    

    if(msgMap["clusterId"] == "8021") {
        logging("CONFIGURE CONFIRMATION - description: ${description} | parseMap:${msgMap}", 0)
        // catchall: 0000 8021 00 00 0040 00 5DF0 00 00 0000 00 00 9000
        // catchall: 0000 8021 00 00 0040 00 5DF0 00 00 0000 00 00 9100
        if(msgMap["data"] != []) {
            logging("Received BIND Confirmation with sequence number 0x${msgMap["data"][0]} (a total minimum of FOUR unique numbers expected, same number may repeat).", 100)
        }
    } else if(msgMap["clusterId"] == "8034") {
        logging("CLUSTER LEAVE REQUEST - description: ${description} | parseMap:${msgMap}", 0)
    } else if(msgMap["clusterId"] == "0013") {
        logging("Pairing event - description: ${description} | parseMap:${msgMap}", 1)
        sendZigbeeCommands(configureAdditional())
        refresh()
        // Getting this during install:
        // catchall: 0000 0013 00 00 0040 00 CE89 00 00 0000 00 00 D389CE0932773CDF8CCF0484
        // msgMap:[raw:catchall: 0000 0013 00 00 0040 00 CE89 00 00 0000 00 00 D389CE0932773CDF8CCF0484, profileId:0000, clusterId:0013, clusterInt:19, sourceEndpoint:00, destinationEndpoint:00, options:0040, messageType:00, dni:CE89, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:00, data:[D3, 89, CE, 09, 32, 77, 3C, DF, 8C, CF, 04, 84]]
    } else if((msgMap["clusterId"] == "0000" || msgMap["clusterId"] == "0001" || msgMap["clusterId"] == "0003" || msgMap["clusterId"] == "0400") && msgMap["command"] == "07" && msgMap["data"] != [] && msgMap["data"][0] == "00") {
        logging("CONFIGURE CONFIRMATION - description:${description} | parseMap:${msgMap}", 1)
        if(msgMap["clusterId"] == "0400") {
            logging("Device confirmed LUX Report configuration ACCEPTED by the device", 100)
        } else if(msgMap["clusterId"] == "0000") {
            logging("Device confirmed BASIC Report configuration ACCEPTED by the device", 100)
        } else if(msgMap["clusterId"] == "0001") {
            logging("Device confirmed BATTERY Report configuration ACCEPTED by the device", 100)
        } else if(msgMap["clusterId"] == "0003") {
            logging("Device confirmed IDENTIFY Report configuration ACCEPTED by the device", 100)
        }
        // Configure Confirmation event Description cluster 0001:
        // catchall: 0104 0001 01 01 0040 00 5DF0 00 00 0000 07 01 00
        // msgMap:[raw:catchall: 0104 0001 01 01 0040 00 5DF0 00 00 0000 07 01 00, profileId:0104, clusterId:0001, clusterInt:1, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:5DF0, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:07, direction:01, data:[00]]

        // Configure Confirmation event Description cluster 0400:
        // catchall: 0104 0400 01 01 0040 00 5DF0 00 00 0000 07 01 00
        // msgMap:[raw:catchall: 0104 0400 01 01 0040 00 5DF0 00 00 0000 07 01 00, profileId:0104, clusterId:0400, clusterInt:1024, sourceEndpoint:01, destinationEndpoint:01, options:0040, messageType:00, dni:5DF0, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:07, direction:01, data:[00]]
    } else if(msgMap["cluster"] == "0000" && msgMap["attrId"] == "0005") {
        logging("Reset button pressed - description:${description} | parseMap:${msgMap}", 1)
        // The value from this command is the device model string
        setCleanModelName(newModelToSet=msgMap["value"])
        sendZigbeeCommands(configureAdditional())
        refresh()
        //sendZigbeeCommands(zigbee.readAttribute(CLUSTER_POWER, 0x0020))
        // Reset button event Description:
        // read attr - raw: 5DF00100002C050042126C756D692E73656E5F696C6C2E6D676C3031, dni: 5DF0, endpoint: 01, cluster: 0000, size: 2C, attrId: 0005, encoding: 42, command: 0A, value: 126C756D692E73656E5F696C6C2E6D676C3031
    } else if(msgMap["clusterId"] == "0006") {
        logging("Match Descriptor Request - description:${description} | parseMap:${msgMap}", 1)
        // This is usually the 0x0019 OTA Upgrade Request, safe to ignore

        // Data == data:[D5, FD, FF, 04, 01, 01, 19, 00, 00] == OTA Upgrade Request

        // catchall: 0000 0006 00 00 0040 00 F0AE 00 00 0000 00 00 D5FDFF040101190000
        // msgMap:[raw:catchall: 0000 0006 00 00 0040 00 F0AE 00 00 0000 00 00 D5FDFF040101190000, profileId:0000, clusterId:0006, clusterInt:6, sourceEndpoint:00, destinationEndpoint:00, options:0040, messageType:00, dni:F0AE, isClusterSpecific:false, isManufacturerSpecific:false, manufacturerId:0000, command:00, direction:00, data:[D5, FD, FF, 04, 01, 01, 19, 00, 00]]
    } else if(msgMap["clusterId"] == "0003" && msgMap["command"] == "01") {
        logging("IDENTIFY QUERY - description:${description} | parseMap:${msgMap}", 1)
        // This is responded to with a Manufacturer Specific command
        // Command: Default Response
        sendZigbeeCommands(["he raw ${device.deviceNetworkId} 1 1 0xFCC0 {04 6E 12 00 0B 03 83}"])  // 12 00 0B = the 00 is replaced with the sequence number
        // Identify Query event Description:
        // catchall: 0104 0003 01 FF 0040 00 5DF0 01 00 0000 01 00
    } else if(msgMap["cluster"] == "0400" && msgMap["attrId"] == "0000") {
        Integer rawValue = Integer.parseInt(msgMap['value'], 16)
        Integer variance = 190  // Normally we should never hit this, but this is an extra filter to protect the event log
        
        BigDecimal lux = rawValue > 0 ? Math.pow(10, rawValue / 10000.0) - 1.0 : 0
        BigDecimal oldLux = device.currentValue('illuminance') == null ? null : device.currentValue('illuminance')
        Integer oldRaw = oldLux == null ? null : oldLux == 0 ? 0 : Math.log10(oldLux + 1) * 10000
        lux = lux.setScale(1, BigDecimal.ROUND_HALF_UP)
        if(oldLux != null) oldLux = oldLux.setScale(1, BigDecimal.ROUND_HALF_UP)
        BigDecimal luxChange = null
        if(oldRaw == null) {
            logging("Lux: $lux (raw: $rawValue, oldRaw: $oldRawold lux: $oldLux)", 1)
        } else {
            luxChange = lux - oldLux
            luxChange = luxChange.setScale(1, BigDecimal.ROUND_HALF_UP)
            logging("Lux: $lux (raw: $rawValue, oldRaw: $oldRaw, diff: ${rawValue - oldRaw}, lower: ${oldRaw - variance}, upper: ${oldRaw + variance}, old lux: $oldLux)", 1)
        }
        
        if(oldLux == null || rawValue < oldRaw - variance || rawValue > oldRaw + variance) {
            logging("Sending lux event (lux: $lux, change: $luxChange)", 100)
            sendEvent(name:"illuminance", value: lux, unit: "lux", isStateChange: true)
        } else {
            logging("SKIPPING lux event since change wasn't large enough (lux: $lux, change: $luxChange)", 100)
        }
        // Lux event Description:
        // read attr - raw: 5DF00104000A0000219F56, dni: 5DF0, endpoint: 01, cluster: 0400, size: 0A, attrId: 0000, encoding: 21, command: 0A, value: 9F56
    } else if(msgMap["cluster"] == "0000" && (msgMap["attrId"] == "FF01" || msgMap["attrId"] == "FF02")) {
        logging("KNOWN event (Xiaomi/Aqara specific data structure with battery data) - description:${description} | parseMap:${msgMap}", 1)
        // Xiaomi/Aqara specific data structure, contains battery info
    } else if(msgMap["cluster"] == "0001" && msgMap["attrId"] == "0020") {
        logging("Battery voltage received - description:${description} | parseMap:${msgMap}", 1)
        parseAndSendBatteryStatus(Integer.parseInt(msgMap['value'], 16) / 10.0)
        // Battery event Description:
        // read attr - raw: 5DF00100010820002020, dni: 5DF0, endpoint: 01, cluster: 0001, size: 08, attrId: 0020, encoding: 20, command: 0A, value: 20
    } else {
		log.warn "Unhandled Event PLEASE REPORT TO DEV - description:${description} | msgMap:${msgMap}"
	}
    
    // BEGIN:getGenericZigbeeParseFooter(loglevel=0)
    // parse() Generic Zigbee-device footer BEGINS here
    logging("PARSE END-----------------------", 0)
    return cmd
    // parse() Generic footer ENDS here
    // END:  getGenericZigbeeParseFooter(loglevel=0)
}

void updated() {
    logging("updated()", 10)
    configurePresence()
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
ArrayList<String> configureAdditional() {
    logging("configureAdditional()", 1)
    // List configureReporting(Integer clusterId, Integer attributeId, Integer dataType, Integer minReportTime, 
    //        Integer maxReportTime, Integer reportableChange = null, Map additionalParams=[:], 
    //        int delay = STANDARD_DELAY_INT)
    Integer msDelay = 50
    Integer variance = 300   // RAW Lux change needed before a new Lux event is sent, this works MOST of the time.
    ArrayList<String> cmd = [
		"zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0000 {${device.zigbeeId}} {}", "delay $msDelay",
        "zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0001 {${device.zigbeeId}} {}", "delay $msDelay",
		"zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0003 {${device.zigbeeId}} {}", "delay $msDelay",
		"zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0400 {${device.zigbeeId}} {}", "delay $msDelay",
		"zdo send ${device.deviceNetworkId} 0x01 0x01", "delay $msDelay"
    ]
    // CLUSTER: ILLUMINANCE
    cmd += zigbee.configureReporting(0x0400, 0x0000, 0x21, (secondsMinLux == null ? 10 : secondsMinLux).intValue(), 3600, variance, [:], msDelay)
    // CLUSTER: POWER, 60 min report interval (original default 5), 3600 max report interval (original default 3600), Voltage measured: 0.1V
    cmd += zigbee.configureReporting(0x0001, 0x0020, 0x20, 3600, 3600, null, [:], msDelay)
    // CLUSTER: BASIC (Response is unreportable attribute, so no use setting this)
	//cmd += zigbee.configureReporting(0x0000, 0x0005, 0xff, 30, 3600, null, [:], msDelay)
    // CLUSTER: IDENTIFY (Response is unreportable attribute, so no use setting this)
    //cmd += zigbee.configureReporting(0x0003, 0x0000, 0xff, 0, 0, null, [:], msDelay)
    
    // Request the current lux value
	cmd += zigbeeReadAttribute(0x0400, 0x0000)
    // Request the current Battery level
    cmd += zigbeeReadAttribute(0x0001, 0x0020)

	//cmd += zigbee.writeAttribute(CLUSTER_BASIC, 0xFF29, 0x10, 0x01, [mfgCode: "0x126E"])
    logging("configure cmd=${cmd}", 1)
    return cmd
}

/*
    --------- READ ATTRIBUTE METHODS ---------
*/


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
    comment = "Works with model GZCGQ01LM."
    if(comment != "") state.comment = comment
    String version = "v0.6.1.0503"
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

//#include:getHelperFunctions('filter-linear-regression')
