/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v0.8.2.0818b
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
import hubitat.helper.HexUtils

metadata {
	definition (name: "Zigbee - Xiaomi/Aqara/Opple Button/Switch/Remote", namespace: "markusl", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/zigbee-xiaomi-aqara-opple-button-switch-remote-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilitiesForZigbeeDevices()
        capability "Sensor"
        capability "PresenceSensor"
        capability "Initialize"
        capability "Refresh"
        // END:  getDefaultMetadataCapabilitiesForZigbeeDevices()
        
        capability "Battery"
        capability "PushableButton"
		capability "HoldableButton"
        capability "DoubleTapableButton"
        capability "ReleasableButton"
        
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

        attribute "lastHoldEpoch", "String"
        
        // BEGIN:getZigbeeBatteryCommands()
        command "resetBatteryReplacedDate"
        // END:  getZigbeeBatteryCommands()
        // BEGIN:getCommandsForPresence()
        command "resetRestoredCounter"
        // END:  getCommandsForPresence()
        // BEGIN:getCommandsForZigbeePresence()
        command "forceRecoveryMode", [[name:"Minutes*", type: "NUMBER", description: "Maximum minutes to run in Recovery Mode"]]
        // END:  getCommandsForZigbeePresence()

        fingerprint deviceJoinName: "Xiaomi Button (WXKG01LM)",                      model: "lumi.sensor_switch",     inClusters: "0000,0003,FFFF,0019", outClusters: "0000,0004,0003,0006,0008,0005,0019", manufacturer: "LUMI", profileId: "0104", endpointId: "01"

		fingerprint deviceJoinName: "Aqara Button (WXKG11LM) - 2015",                model: "lumi.sensor_switch.aq2", inClusters: "0000,FFFF,0006",      outClusters: "0000,0004,FFFF", manufacturer: "LUMI", profileId: "0104", endpointId: "01"

		fingerprint deviceJoinName: "Aqara Button (WXKG11LM) - 2018",                model: "lumi.remote.b1acn01",    inClusters: "0000,0012,0003",      outClusters: "0000", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
        
		fingerprint deviceJoinName: "Aqara Button (WXKG12LM)",                       model: "lumi.sensor_switch.aq3", inClusters: "0000,0012,0006,0001", outClusters: "0000", manufacturer: "LUMI", profileId: "0104", endpointId: "01"

		fingerprint deviceJoinName: "Aqara 1-button Light Switch (WXKG03LM) - 2016", model: "lumi.sensor_86sw1lu",    inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
		fingerprint deviceJoinName: "Aqara 1-button Light Switch (WXKG03LM) - 2016", model: "lumi.sensor_86sw1",      inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
		
		fingerprint deviceJoinName: "Aqara 2-button Light Switch (WXKG02LM) - 2016", model: "lumi.sensor_86sw2Un",    inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
		fingerprint deviceJoinName: "Aqara 2-button Light Switch (WXKG02LM) - 2016", model: "lumi.sensor_86sw2",      inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
		
		fingerprint deviceJoinName: "Aqara 1-button Light Switch (WXKG03LM) - 2018", model: "lumi.remote.b186acn01",  inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"
		
		fingerprint deviceJoinName: "Aqara 2-button Light Switch (WXKG02LM) - 2018", model: "lumi.remote.b286acn01",  inClusters: "0000,0003,0019,FFFF,0012", outClusters: "0000,0004,0003,0005,0019,FFFF,0012", manufacturer: "LUMI", profileId: "0104", endpointId: "01"

        fingerprint deviceJoinName: "Aqara Opple 2 Button Remote (WXCJKG11LM)",      model: "lumi.remote.b286opcn01",     profileId:"0104", inClusters:"0012,0003", outClusters:"0006", manufacturer:"LUMI", application: "11", endpointId: "01"
        fingerprint profileId: "0104", inClusters: "0000,0003,0001", outClusters: "003,0006,0008,0300", model: "lumi.remote.b286opcn01", deviceJoinName: "Aqara Opple 2 Button Remote (WXCJKG11LM)"

        fingerprint deviceJoinName: "Aqara Opple 4 Button Remote (WXCJKG12LM)",      model: "lumi.remote.b486opcn01",     profileId:"0104", inClusters:"0012,0003", outClusters:"0006", manufacturer:"LUMI", application: "11", endpointId: "01"
        fingerprint profileId: "0104", inClusters: "0000,0003,0001", outClusters: "003,0006,0008,0300", model: "lumi.remote.b486opcn01", deviceJoinName: "Aqara Opple 4 Button Remote (WXCJKG12LM)"

        fingerprint deviceJoinName: "Aqara Opple 6 Button Remote (WXCJKG13LM)",      model: "lumi.remote.b686opcn01",     profileId:"0104", inClusters:"0012,0003", outClusters:"0006", manufacturer:"LUMI", application: "11", endpointId: "01"
        fingerprint profileId: "0104", inClusters: "0000,0003,0001", outClusters: "003,0006,0008,0300", model: "lumi.remote.b686opcn01", deviceJoinName: "Aqara Opple 6 Button Remote (WXCJKG13LM)"

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
        // BEGIN:getMetadataPreferencesForRecoveryMode(defaultMode="Normal")
        input(name: "recoveryMode", type: "enum", title: styling_addTitleDiv("Recovery Mode"), description: styling_addDescriptionDiv("Select Recovery mode type (default: Normal)<br/>NOTE: The \"Insane\" and \"Suicidal\" modes may destabilize your mesh if run on more than a few devices at once!"), options: ["Disabled", "Slow", "Normal", "Insane", "Suicidal"], defaultValue: "Normal")
        // END:  getMetadataPreferencesForRecoveryMode(defaultMode="Normal")
        // BEGIN:getMetadataPreferencesForZigbeeDevicesWithBattery()
        input(name: "vMinSetting", type: "decimal", title: styling_addTitleDiv("Battery Minimum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 0% (default = 2.5V)"), defaultValue: "2.5", range: "2.1..2.8")
        input(name: "vMaxSetting", type: "decimal", title: styling_addTitleDiv("Battery Maximum Voltage"), description: styling_addDescriptionDiv("Voltage when battery is considered to be at 100% (default = 3.0V)"), defaultValue: "3.0", range: "2.9..3.4")
        // END:  getMetadataPreferencesForZigbeeDevicesWithBattery()
        if(isNonSwitchModel() == true) {
            input(name: "millisForHold", type: "number", title: styling_addTitleDiv("Millis for Hold"), description: styling_addDescriptionDiv("Set the minimum number of milliseconds to count as held (500 to 60000, default: 1000)<br >This setting is NOT used with model WXKG02LM and WXKG03LM or OPPLE models."), defaultValue: "1000", range: "500..60000")
        }
        input(name: "enableReleaseEvents", type: "bool", title: styling_addTitleDiv("Enable Release Events"), description: styling_addDescriptionDiv("Records button Release events (default: enabled)"), defaultValue: true)
        Integer physicalButtons = getDeviceDataByName('physicalButtons') != null ? getDeviceDataByName('physicalButtons').toInteger() :  0
        List btnDeviceInputs = []
        switch(physicalButtons) {
            case 6:
                btnDeviceInputs += [name: "btnDevice5and6", type: "enum", title: styling_addTitleDiv("Child Device(s) for button 5 & 6"), 
                    description: styling_addDescriptionDiv("Create child devices for button 5 & 6."),
                    options: ["None", "2 virtual switches", "2 virtual momentary switches", "1 virtual dimmer"], defaultValue: "None"]
            case 4:
                btnDeviceInputs += [name: "btnDevice3and4", type: "enum", title: styling_addTitleDiv("Child Device(s) for button 3 & 4"), 
                    description: styling_addDescriptionDiv("Create child devices for button 3 & 4."),
                    options: ["None", "2 virtual switches", "2 virtual momentary switches", "1 virtual dimmer"], defaultValue: "None"]
            case 2:
                btnDeviceInputs += [name: "btnDevice1and2", type: "enum", title: styling_addTitleDiv("Child Device(s) for button 1 & 2"), 
                    description: styling_addDescriptionDiv("Create child devices for button 1 & 2."),
                    options: ["None", "2 virtual switches", "2 virtual momentary switches", "1 virtual dimmer"], defaultValue: "None"]
                break
            case 1:
                btnDeviceInputs += [name: "btnDevice1", type: "enum", title: styling_addTitleDiv("Child Device for button 1"), 
                    description: styling_addDescriptionDiv("Create a child device for button 1."),
                    options: ["None", "1 virtual switch", "1 virtual momentary switch"], defaultValue: "None"]
                break
        }
        btnDeviceInputs.reverse().each { input(it) }
	}
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
     
    Map deviceInfo = ['name': 'Zigbee - Xiaomi/Aqara/Opple Button/Switch/Remote', 'namespace': 'markusl', 'author': 'Markus Liljergren', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/zigbee-xiaomi-aqara-opple-button-switch-remote-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

/* These functions are unique to each driver */

ArrayList<String> refresh() {
    return refreshActual(null)
    /* refreshEvents() just sends all current states again, it's a hack for HubConnect */
    refreshEvents()
}

ArrayList<String> refreshActual(String newModelToSet) {
    logging("refreshActual() model=$newModelToSet", 1)

    sendEvent(name:"pushed", value: 0, isStateChange: false, descriptionText: "Refresh of pushed state")
    sendEvent(name:"held", value: 0, isStateChange: false, descriptionText: "Refresh of held state")
    sendEvent(name:"lastHoldEpoch", value: 0, isStateChange: false, descriptionText: "Refresh of lastHoldEpoch")
    sendEvent(name:"doubleTapped", value: 0, isStateChange: false, descriptionText: "Refresh of double-tapped state")

    getDriverVersion()
    resetBatteryReplacedDate(forced=false)
    setLogsOffTask(noLogWarning=true)

    String model = setCleanModelNameWithAcceptedModels(newModelToSet=newModelToSet)
    switch(model) {
        case "lumi.sensor_switch":
            sendEvent(name:"numberOfButtons", value: 5, isStateChange: false, descriptionText: "Xiaomi Button (WXKG01LM) detected: set to 5 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.sensor_switch.aq2":
            sendEvent(name:"numberOfButtons", value: 4, isStateChange: false, descriptionText: "Aqara Button (WXKG11LM) 2015 detected: set to 4 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.remote.b1acn01":
            sendEvent(name:"numberOfButtons", value: 3, isStateChange: false, descriptionText: "Aqara Button (WXKG11LM) 2018 detected: set to 3 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.sensor_switch.aq3":
            sendEvent(name:"numberOfButtons", value: 4, isStateChange: false, descriptionText: "Aqara Button (WXKG12LM) detected: set to 4 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.sensor_86sw1lu":
        case "lumi.sensor_86sw1":
            sendEvent(name:"numberOfButtons", value: 3, isStateChange: false, descriptionText: "Aqara 1-button Light Switch (WXKG03LM - 2016) detected: set to 3 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.sensor_86sw2Un":
        case "lumi.sensor_86sw2":
            sendEvent(name:"numberOfButtons", value: 9, isStateChange: false, descriptionText: "Aqara 2-button Light Switch (WXKG02LM - 2016) detected: set to 9 buttons")
            updateDataValue("physicalButtons", "3")
            break
        case "lumi.remote.b186acn01":
            sendEvent(name:"numberOfButtons", value: 3, isStateChange: false, descriptionText: "Aqara 1-button Light Switch (WXKG03LM - 2018) detected: set to 3 buttons")
            updateDataValue("physicalButtons", "1")
            break
        case "lumi.remote.b286acn01":
            sendEvent(name:"numberOfButtons", value: 9, isStateChange: false, descriptionText: "Aqara 2-button Light Switch (WXKG02LM - 2018) detected: set to 9 buttons")
            updateDataValue("physicalButtons", "3")
            break
        case "lumi.remote.b286opcn01":
            sendEvent(name:"numberOfButtons", value: 10, isStateChange: false, descriptionText: "Aqara Oppo 2 Button Remote (WXCJKG11LM) detected: set to 10 buttons")
            updateDataValue("physicalButtons", "2")
            break
        case "lumi.remote.b486opcn01":
            sendEvent(name:"numberOfButtons", value: 20, isStateChange: false, descriptionText: "Aqara Oppo 4 Button Remote (WXCJKG12LM) detected: set to 20 buttons")
            updateDataValue("physicalButtons", "4")
            break
        case "lumi.remote.b686opcn01":
            sendEvent(name:"numberOfButtons", value: 30, isStateChange: false, descriptionText: "Aqara Oppo 6 Button Remote (WXCJKG13LM) detected: set to 30 buttons")
            updateDataValue("physicalButtons", "6")
            break
        default:
            sendEvent(name:"numberOfButtons", value: 0, isStateChange: false, descriptionText: "UNKNOWN Button detected: set to 1 button")
    }
    configurePresence()
    startCheckEventInterval()

    ArrayList<String> cmd = []
    logging("refresh cmd: $cmd", 1)
    return cmd
}

void initialize() {
    logging("initialize()", 100)
    unschedule()
    refreshActual(null)
}

void installed() {
    logging("installed()", 100)
    refreshActual(null)
}

void updated() {
    logging("updated()", 100)
    createAllButtonChildren()
    refreshActual(null)
}

String setCleanModelNameWithAcceptedModels(String newModelToSet=null) {
    return setCleanModelName(newModelToSet=newModelToSet, acceptedModels=[
        "lumi.sensor_switch.aq3",
        "lumi.sensor_switch.aq2",
        "lumi.sensor_switch",
        "lumi.remote.b1acn01",
        "lumi.sensor_86sw1lu",
        "lumi.sensor_86sw1",
        "lumi.sensor_86sw2Un",
        "lumi.sensor_86sw2",
        "lumi.remote.b186acn01",
        "lumi.remote.b286acn01",
        "lumi.remote.b286opcn01", 
        "lumi.remote.b486opcn01",
        "lumi.remote.b686opcn01"
    ])
}

boolean isSwitchModel(String model=null) {
    model = model != null ? model : getDeviceDataByName('model')
    switch(model) {
        case "lumi.sensor_86sw1lu":
        case "lumi.sensor_86sw1":
        case "lumi.sensor_86sw2Un":
        case "lumi.sensor_86sw2":
        case "lumi.remote.b186acn01":
        case "lumi.remote.b286acn01":
            return true
            break
        default:
            return false
    }
}

boolean isNonOppleModel(String model=null) {
    model = model != null ? model : getDeviceDataByName('model')
    switch(model) {
        case "lumi.sensor_switch":
        case "lumi.sensor_switch.aq2":
        case "lumi.remote.b1acn01":
        case "lumi.sensor_switch.aq3":
        case "lumi.sensor_86sw1lu":
        case "lumi.sensor_86sw1":
        case "lumi.sensor_86sw2Un":
        case "lumi.sensor_86sw2":
        case "lumi.remote.b186acn01":
        case "lumi.remote.b286acn01":
            return true
            break
        default:
            return false
    }
}

boolean isOppleModel(String model=null) {
    model = model != null ? model : getDeviceDataByName('model')
    switch(model) {
        case "lumi.remote.b286opcn01":
        case "lumi.remote.b486opcn01":
        case "lumi.remote.b686opcn01":
            return true
            break
        default:
            return false
    }
}

boolean isNonSwitchModel(String model=null) {
    model = model != null ? model : getDeviceDataByName('model')
    switch(model) {
        case "lumi.sensor_switch":
        case "lumi.sensor_switch.aq2":
        case "lumi.remote.b1acn01":
        case "lumi.sensor_switch.aq3":
            return true
            break
        default:
            return false
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

    String cModel = getDeviceDataByName('model')
    
    switch(msgMap["cluster"] + '_' + msgMap["attrId"]) {
        case "0000_FFF0":
            //logging("Unparsed event FFF0 - description:${description}", 0)

            break
        case "0000_FF01":
        case "0000_FF02":
            if(msgMap["encoding"] == "4C") {
                logging("KNOWN event (Xiaomi/Aqara specific data structure with battery data - 4C) - description:${description} | parseMap:${msgMap}", 100)
                parseAndSendBatteryStatus(msgMap['value'][1] / 1000.0)
                
                sendZigbeeCommands(zigbee.readAttribute(0x0000, 0x0001))

            } else if(msgMap["encoding"] == "41" || msgMap["encoding"] == "42") {
                if(msgMap["encoding"] == "42") {
                    msgMap = zigbee.parseDescriptionAsMap(description.replace('encoding: 42', 'encoding: 41'))
                    msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=false)
                }
                logging("KNOWN event (Xiaomi/Aqara specific data structure with battery data - 42) - description:${description} | parseMap:${msgMap}", 100)
                if(msgMap["value"].containsKey("battery")) {
                    parseAndSendBatteryStatus(msgMap["value"]["battery"] / 1000.0)
                }
                sendZigbeeCommands(zigbee.readAttribute(0x0000, 0x0001))

            } else {
                log.warn "Unhandled Event PLEASE REPORT TO DEV - description:${description} | msgMap:${msgMap}"
            }
            break
        case "FCC0_00F7":
            msgMap["value"] = parseXiaomiStruct(msgMap["value"], isFCC0=true)
            
            logging("KNOWN event (Xiaomi/Aqara specific data structure with battery data - FCC0-00F7) - description:${description} | parseMap:${msgMap}", 1)
            if(msgMap["value"].containsKey("battery")) {
                parseAndSendBatteryStatus(msgMap["value"]["battery"] / 1000.0)
            }
            sendZigbeeCommands(zigbee.readAttribute(0x0000, 0x0001))
            break
        case "FCC0_00FC":
            logging("KNOWN IGNORED event (Oppo Remote Event - FCC0) - description:${description} | parseMap:${msgMap}", 1)
            
            break
        case "0000_0001":
            logging("Application version (also requested when receiving hourly checkin) - description:${description} | parseMap:${msgMap}", 1)
            
            break
        case "0000_0004":
            logging("Manufacturer Name Received (from readAttribute command) - description:${description} | parseMap:${msgMap}", 1)
            break
        case "0000_0005":
            if(msgMap.containsKey("additionalAttrs") && msgMap["additionalAttrs"] != [] && msgMap["additionalAttrs"][0]["encoding"] == "42") {
                //logging("Redoing the parsing for additionalAttrs", 0)
                msgMap = zigbee.parseDescriptionAsMap(description.replace('01FF42', '01FF41'))
                msgMap["additionalAttrs"][0]["encoding"] = "42"
                msgMap["additionalAttrs"][0]["value"] = parseXiaomiStruct(msgMap["additionalAttrs"][0]["value"], isFCC0=msgMap["additionalAttrs"][0]["attrId"]=="FCC0")
            }
            logging("Model name received - description:${description} | parseMap:${msgMap}", 1)
            logging("New model to set: ${msgMap["value"]}", 1)
            String model = setCleanModelNameWithAcceptedModels(newModelToSet=msgMap["value"])
            if(isNonSwitchModel(model=model) == true) {
                sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0xFF02, [mfgCode: "0x115F"]))
            } else if(isSwitchModel() == true) {
                sendZigbeeCommands(zigbee.configureReporting(0x0000, 0xFF01, 0xff, 30, 600, 0x1, [mfgCode: "0x115F"], 800))
            } else if(isOppleModel(model) == true) {
                logging("Got Cluster 0000 attribute 0005 for $model", 1)
                initialize()
            }
            
            if(msgMap.containsKey("additionalAttrs") && msgMap["additionalAttrs"] != [] && msgMap["additionalAttrs"][0]["encoding"] == "42") {
                Map value = msgMap["additionalAttrs"][0]["value"]
                if(value.containsKey("battery")) {
                    parseAndSendBatteryStatus(value["battery"] / 1000.0)
                }
            }
            
            refreshActual(model)
            break
        case "0006_0000":
        case "0006_8000":
            logging("Description: ${description}", 1)
            parseButtonEvent(msgMap)
            
            break
        case "0012_0055":
            if(isOppleModel() == true) {
                logging("Button was pressed (value: ${msgMap["value"]}, attrId: ${msgMap["attrId"]}) | description:${description} | parseMap:${msgMap}", 1)
                parseOppoButtonEvent(msgMap)
            } else {
                logging("Description: ${description}", 1)
                parseButtonEvent(msgMap)
                
            }
            break
        default:
            switch(msgMap["clusterId"]) {
                case "0000":
                case "0001":
                case "0400":
                    //logging("Broadcast catchall - description:${description} | parseMap:${msgMap}", 0)
                    
                    break
                case "0006":
                    logging("On/OFF Cluster Catchall (value: ${msgMap["value"]}, attrId: ${msgMap["attrId"]})", 1)
                    log.warn("Configuration not yet done, if the buttons don't work yet, first try pushing ONE button and wait for a bit. If that doesn't work re-pairing might be needed. Wait a little bit and don't click to fast!")
                    oppleInit()
                    break
                case "0008":
                    logging("Level Cluster Catchall (value: ${msgMap["value"]}, attrId: ${msgMap["attrId"]})", 1)
                    log.warn("Configuration not yet done, if the buttons don't work yet, first try pushing ONE button and wait for a bit. If that doesn't work re-pairing might be needed. Wait a little bit and don't click to fast!")
                    oppleInit()
                    break
                case "FCC0":
                    //logging("Aqara catchall - description:${description} | parseMap:${msgMap}", 0)
                    if(msgMap["data"] == ["00"]) {
                        logging("Button settings MIGHT be set correctly for your Opple Remote! Try them by pushing a button!", 100)
                    }
                    break
                case "0300":
                    logging("Color Control Cluster Catchall (value: ${msgMap["value"]}, attrId: ${msgMap["attrId"]})", 1)
                    log.warn("Configuration not yet done, if the buttons don't work yet, first try pushing ONE button and wait for a bit. If that doesn't work re-pairing might be needed. Wait a little bit and don't click to fast!")
                    oppleInit()
                    break
                case "0013":
                    //logging("Device Announcement Cluster - description:${description} | parseMap:${msgMap}", 0)
                    
                    oppleInit()

                    break
                case "8005":
                    //logging("Confirmation Cluster (value: ${msgMap["value"]}, attrId: ${msgMap["attrId"]})", 0)
                    break
                case "8021":
                    //logging("Result for Reporting configuration - description:${description} | parseMap:${msgMap}", 0)
                    if(msgMap["data"][1] == "00") {
                        logging("Setting of reporting configuration SUCCESSFUL!", 100)
                    } else {
                        log.warn("Setting of reporting configuration FAILED! Try pairing again...")
                    }
                    
                    break
                case "8004":
                case "8032":
                case "000A":
                    //logging("General catchall - description:${description} | parseMap:${msgMap}", 0)
                    break
                default:
                    log.warn "Unhandled Event PLEASE REPORT TO DEV - description:${description} | msgMap:${msgMap}"
                    break
            }
            break
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

void oppleInit() {
    if(isNonOppleModel() == false) {
        logging("Sending init command for Opple Remote...", 100)
        sendZigbeeCommands([
            zigbeeReadAttribute(0x0000, 0x0001)[0],
            zigbeeReadAttribute(0x0000, 0x0005)[0], "delay 200", 
            zigbeeWriteAttribute(0xFCC0, 0x0009, 0x20, 0x01, [mfgCode: "0x115F"])[0], 
            "delay 3000", 
            zigbeeWriteAttribute(0xFCC0, 0x0009, 0x20, 0x01, [mfgCode: "0x115F"])[0],
            "delay 3000", 
            zigbeeWriteAttribute(0xFCC0, 0x0009, 0x20, 0x01, [mfgCode: "0x115F"])[0]
        ])
    }
}

boolean sendReleaseEvent(Integer btn, String logText=null, String descriptionText=null) {
    if(enableReleaseEvents == null || enableReleaseEvents == true) {
        logText = logText == null ? "Button $btn was released (same push event as held)" : logText
        descriptionText = descriptionText == null ? "Button $btn was released" : descriptionText
        logging(logText, 100)
        sendEvent(name:"released", value: btn, isStateChange: true, descriptionText: descriptionText)
        return true
    } else {
        return false
    }
}

void parseButtonEvent(Map msgMap) {
    Integer btn = Integer.parseInt(msgMap['value'], 16)
    Integer endpoint = Integer.parseInt(msgMap['endpoint'], 16)
    //logging("parseButtonEvent() (btn: ${btn}, attrId: ${msgMap["attrId"]}, endpoint: $endpoint, msgMap: $msgMap)", 0)
    btn = btn == 18 ? 4 : btn

    Integer totalButtons = device.currentValue('numberOfButtons')
    Integer physicalButtons = getDeviceDataByName("physicalButtons") != null ? getDeviceDataByName("physicalButtons").toInteger() : 1
    Integer btnModified = endpoint + ((btn-1) * physicalButtons)
    logging("parseButtonEvent() (btn: $btn, btnModified: $btnModified, endpoint: $endpoint, physicalButtons: $physicalButtons, attrId: ${msgMap["attrId"]}, msgMap: $msgMap)", 1)
    if((btn <= 4 && btn != 0 && ((isSwitchModel() && msgMap['attrId'] == '0000') || msgMap['attrId'] == '0055')) || (msgMap['attrId'] == '8000')) {
        btnModified = btn <= 8 ? btnModified : 5
        logging("Button $btnModified was pushed (t1)", 100)
        if(btnModified <= physicalButtons) buttonPushed(btnModified)
        sendEvent(name:"pushed", value: btnModified, isStateChange: true, descriptionText: "Button was clicked $btn times")
        if(btn == 2) {
            logging("Button $endpoint was double tapped", 100)
            buttonDoubleTapped(endpoint)
            sendEvent(name:"doubleTapped", value: endpoint, isStateChange: true, descriptionText: "Button $endpoint was double tapped")
        }
    } else if(isSwitchModel() && btn == 0) {
        btnModified = endpoint + (2 * physicalButtons)
        logging("Button $endpoint was held (push event: $btnModified)", 100)
        buttonHeld(endpoint)
        sendEvent(name:"held", value: endpoint, isStateChange: true, descriptionText: "Button $endpoint was held")
        sendReleaseEvent(endpoint)
        logging("Button $btnModified was pushed (t2)", 100)
        sendEvent(name:"pushed", value: btnModified, isStateChange: true, descriptionText: "Button $endpoint was held")
    } else {
        if(btn == 0 || btn == 16) {
            if(getDeviceDataByName('model') == "lumi.sensor_switch.aq2") {
                logging("Button 1 was pushed (t4)", 100)
                buttonPushed(1)
                sendEvent(name:"pushed", value: 1, isStateChange: true, descriptionText: "Button 1 was pushed")     
            } else {
                buttonDown(1)
                sendEvent(name: "lastHoldEpoch", value: now(), isStateChange: true)
            }
        } else {
            Long lastHold = 0
            String lastHoldEpoch = device.currentValue('lastHoldEpoch', true) 
            if(lastHoldEpoch != null) lastHold = lastHoldEpoch.toLong()
            sendEvent(name: "lastHoldEpoch", value: 0, isStateChange: true)
            Long millisHeld = now() - lastHold
            Long millisForHoldLong = millisForHold == null ? 1000 : millisForHold.toLong()
            if(lastHold == 0) millisHeld = 0
            logging("millisHeld = $millisHeld, millisForHold = $millisForHoldLong", 1)
            if(millisHeld > millisForHoldLong) {
                if(useTimerForHeld != true) {
                    logging("Button 1 was held", 100)
                    buttonHeld(1)
                    sendEvent(name:"held", value: 1, isStateChange: true, descriptionText: "Button 1 was held")
                    String model = model != null ? model : getDeviceDataByName('model')
                    Integer heldButton = 3
                    if(model == "lumi.sensor_switch") {
                        heldButton = 6
                    }
                    sendReleaseEvent(1)
                    logging("Button $heldButton was pushed (from hold event)", 100)
                    sendEvent(name:"pushed", value: heldButton, isStateChange: true, descriptionText: "Button $heldButton was pushed (from hold event)")
                }
            } else {
                logging("Button 1 was pushed (t3)", 100)
                buttonPushed(1)
                sendEvent(name:"pushed", value: 1, isStateChange: true, descriptionText: "Button 1 was pushed")                
            }
        }
    }
}

void setButtonAsHeld(Map data) {

}

void parseOppoButtonEvent(Map msgMap) {
    Integer btn = Integer.parseInt(msgMap['endpoint'], 16)
    Integer type = Integer.parseInt(msgMap['value'], 16)
    type = type == 0 ? 4 : type == 255 ? 5 : type
    Integer physicalButtons = getDeviceDataByName("physicalButtons") != null ? getDeviceDataByName("physicalButtons").toInteger() : 1
    Integer btnModified = btn + ((type - 1) * physicalButtons)
    logging("parseOppoButtonEvent() (btn: $btn, btnModified: $btnModified, type: $type, physicalButtons: $physicalButtons)", 1)
    
    if(type == 2) {
        logging("Button $btn was double tapped", 100)
        buttonDoubleTapped(btn)
        sendEvent(name:"doubleTapped", value: btn, isStateChange: true, descriptionText: "Button $btn was double tapped")
    }
    if(type >= 1 && type <= 3) {
        logging("Button $btn was pushed $type time(s) (push event: $btnModified)", 100)
        if(type == 1) buttonPushed(btn)
        sendEvent(name:"pushed", value: btnModified, isStateChange: true, descriptionText: "Button $btn was pushed $type time(s)")
    } else if(type == 4) {
        logging("Button $btn was held (push event: $btnModified)", 100)
        buttonHeld(btn)
        sendEvent(name:"held", value: btn, isStateChange: true, descriptionText: "Button $btn was held")
        sendEvent(name:"pushed", value: btnModified, isStateChange: true, descriptionText: "Button $btn was held")
    } else if(type == 5) {
        if(sendReleaseEvent(btn, "Button $btn was released (push event: $btnModified)")) {
            sendEvent(name:"pushed", value: btnModified, isStateChange: true, descriptionText: "Button $btn was released")
        }
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
    comment = "Works with models WXKG01LM, WXKG11LM (2015 & 2018), WXKG12LM, WXKG02LM (2016 & 2018), WXKG03LM (2016 & 2018), WXCJKG11LM, WXCJKG12LM & WXCJKG13LM."
    if(comment != "") state.comment = comment
    String version = "v0.8.2.0818b"
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

void refreshEvents() {
    List<com.hubitat.hub.domain.State> currentStatesList = device.getCurrentStates()
    currentStatesList.each {
        sendEvent(name: it.name, value: it.value, unit: it.unit, isStateChange: true, descriptionText: "Refresh Command")
        
    }
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

ArrayList<String> zigbeeWriteAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Integer value, Map additionalParams = [:], int delay = 200) {
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
    return zigbeeWriteLongAttribute(1, cluster, attributeId, dataType, value, additionalParams, delay)
}

ArrayList<String> zigbeeWriteLongAttribute(Integer endpoint, Integer cluster, Integer attributeId, Integer dataType, Long value, Map additionalParams = [:], int delay = 200) {
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

void recoveryEvent(BigDecimal forcedMinutes=null) {
    if(location.hub.firmwareVersionString.startsWith('2.2.3') == true) {
        log.warn("Stopping Recovery feature due to Platform bug in 2.2.3!")
        unschedule('recoveryEvent')
        unschedule('reconnectEvent')
    } else {
    try {
        recoveryEventDeviceSpecific()
    } catch(Exception e) {
        logging("recoveryEvent()", 1)
        sendZigbeeCommands(zigbee.readAttribute(CLUSTER_BASIC, 0x0004))
    }
    checkPresence(displayWarnings=false)
    Integer mbe = getMaximumMinutesBetweenEvents(forcedMinutes=forcedMinutes)
    if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe, displayWarnings=false) == true) {
        if(presenceWarningEnable == null || presenceWarningEnable == true) log.warn("Event interval normal, recovery mode DEACTIVATED!")
        unschedule('recoveryEvent')
        unschedule('reconnectEvent')
    }
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
    if(location.hub.firmwareVersionString.startsWith('2.2.3') == true) {
        recoveryMode = "Disabled";
        log.warn("Disabling the Recovery feature due to Platform bug in 2.2.3!")
    }
    if(recoveryMode == "Disabled") {
        unschedule('checkEventInterval')
    } else {
        prepareCounters()
        Integer mbe = getMaximumMinutesBetweenEvents()
        if(hasCorrectCheckinEvents(maximumMinutesBetweenEvents=mbe) == false) {
            recoveryMode = recoveryMode == null ? "Normal" : recoveryMode
            if(displayWarnings == true && (presenceWarningEnable == null || presenceWarningEnable == true)) log.warn("Event interval INCORRECT, recovery mode ($recoveryMode) ACTIVE! If this is shown every hour for the same device and doesn't go away after three times, the device has probably fallen off and require a quick press of the reset button or possibly even re-pairing. It MAY also return within 24 hours, so patience MIGHT pay off.")
            scheduleRecoveryEvent()
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

// BEGIN:getHelperFunctions('virtual-child-device-for-button')
void createAllButtonChildren() {
    if(btnDevice1 != null && btnDevice1 != "None") {
        logging("btnDevice1 = $btnDevice1", 1)
        createButtonChildDevice("1", null, btnDevice1)
    }
    if(btnDevice1and2 != null && btnDevice1and2 != "None") {
        logging("btnDevice1and2 = $btnDevice1and2", 1)
        createButtonChildDevice("1", "2", btnDevice1and2)
    }
    if(btnDevice3and4 != null && btnDevice3and4 != "None") {
        logging("btnDevice3and4 = $btnDevice3and4", 1)
        createButtonChildDevice("3", "4", btnDevice3and4)
    }
    if(btnDevice5and6 != null && btnDevice5and6 != "None") {
        logging("btnDevice5and6 = $btnDevice5and6", 1)
        createButtonChildDevice("5", "6", btnDevice5and6)
    }
}

void createButtonChildDevice(String id1, String id2, String type) {
    String driver = null
    String name = null
    String id = null
    if(useDimmerChildSet(type) == true) {
        driver = "Generic Component Dimmer"
        name = "Virtual Dimmer"
        id = "${id1}_$id2"
    } else if(useMomentarySwitchChildSet(type) == true) {
        driver = "Generic Component Switch"
        name = "Virtual Momentary Switch"
        id = id1
        if(id2 != null) {
            createButtonChildDevice(id2, null, type)
        }
    } else if(useVirtualButtonChildSet(type) == true) {
        driver = "Generic Component Button Controller"
        name = "Virtual Button"
        id = id1
        if(id2 != null) {
            createButtonChildDevice(id2, null, type)
        }
    } else {
        driver = "Generic Component Switch"
        name = "Virtual Switch"
        id = id1
        if(id2 != null) {
            createButtonChildDevice(id2, null, type)
        }
    }
    try {
        logging("Making device with type $type and id $device.id-$id", 100)
        com.hubitat.app.DeviceWrapper cd = addChildDevice("hubitat", driver, "$device.id-$id", [name: "$name $id", label: "$name $id", isComponent: false])
        if(useDimmerChildSet(type) == true) {
            cd.parse([[name: "switch", value: 'off', isStateChange: true, descriptionText: "Switch Initialized as OFF"]])
            cd.parse([[name: "level", value: 0, isStateChange: true, descriptionText: "Level Initialized as 0"]])
        } else if(useVirtualButtonChildSet(type) == true) {
            cd.parse([[name: "numberOfButtons ", value: 4, isStateChange: true, descriptionText: "Number of Buttons set to 4"]])
            cd.parse([[name: "held", value: 0, isStateChange: true, descriptionText: "Held Initialized as 0"]])
            cd.parse([[name: "pushed", value: 0, isStateChange: true, descriptionText: "Pushed Initialized as 0"]])
            cd.parse([[name: "doubleTapped", value: 0, isStateChange: true, descriptionText: "Double Tapped Initialized as 0"]])
            cd.parse([[name: "released", value: 0, isStateChange: true, descriptionText: "Released Initialized as 0"]])
        } else {
            cd.parse([[name: "switch", value: 'off', isStateChange: true, descriptionText: "Switch Initialized as OFF"]])
        }
    } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
        log.error "'$driver' driver can't be found! This is supposed to be built-in! Is your hub broken?"
    } catch (java.lang.IllegalArgumentException e) {
        logging("Do nothing - The device already exists", 100)
    }
}

String buildChildDeviceId(String type) {
    return "$device.id-$type"
}

boolean useSwitchChildSet(String btnSetting) {
    if(btnSetting == "2 virtual switches" || btnSetting == "1 virtual switch") {
        return true
    } else {
        return false
    }
}

boolean useMomentarySwitchChildSet(String btnSetting) {
    if(btnSetting == "2 virtual momentary switches" || btnSetting == "1 virtual momentary switch") {
        return true
    } else {
        return false
    }
}

boolean useVirtualButtonChildSet(String btnSetting) {
    if(btnSetting == "2 virtual buttons" || btnSetting == "1 virtual button") {
        return true
    } else {
        return false
    }
}

boolean useDimmerChildSet(String btnSetting) {
    if(btnSetting == "1 virtual dimmer" || btnSetting == "dimmer") {
        return true
    } else {
        return false
    }
}

void toggleChildSwitch(String deviceID) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    String cSwitch = cd.currentState("switch", true)?.value
    if(cSwitch == "on") {
        cd.parse([[name: "switch", value: "off", isStateChange: false, descriptionText: "Switch toggled OFF"]])
    } else {
        cd.parse([[name: "switch", value: "on", isStateChange: false, descriptionText: "Switch toggled ON"]])
    }
}

void setChildSwitch(String deviceID, String state, boolean levelChange = true) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    cd.parse([[name: "switch", value: state, isStateChange: false, descriptionText: "Switch set to ${state.toUpperCase()}"]])
    if(levelChange == true && state == "on") {
        String cLevelStr = cd.currentState("level", true)?.value
        Integer cLevel = cLevelStr != null ? cLevelStr.toInteger() : null
        if(cLevel != null && cLevel == 0) {
            cd.parse([[name: "level", value: 10, isStateChange: false, descriptionText: "Current level was 0, level set to 10."]])
        }
    }
}

void activateMomentarySwitch(String deviceID) {
    setMomentarySwitch(deviceID)
    runInMillis(600, "releaseMomentarySwitch", [data: ['deviceID': deviceID]])
}

void setMomentarySwitch(String deviceID) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    cd.parse([[name: "switch", value: 'on', isStateChange: true, descriptionText: "Momentary Switch set to ON"]])
}

void releaseMomentarySwitch(String deviceID) {
    releaseMomentarySwitch(['deviceID': deviceID])
}

void releaseMomentarySwitch(Map data) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(data["deviceID"])
    cd.parse([[name: "switch", value: 'off', isStateChange: true, descriptionText: "Momentary Switch set to OFF"]])
}

void sendButtonEvent(String deviceID, String name, Integer button) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    cd.parse([[name: name, value: button, isStateChange: true, descriptionText: "Virtual button event '$name' for button $button"]])
    if(name == 'held') {
        cd.parse([[name: 'pushed', value: 2, isStateChange: true, descriptionText: "Virtual button event 'pushed' for button 2 (from '$name')"]])
    }
}

Map getChildDeviceConfig() {
    logging("getChildDeviceConfig()", 1)
    Map childDeviceConfig = [
        1: ['switch': useSwitchChildSet(btnDevice1) == true || useSwitchChildSet(btnDevice1and2) == true,
            'switchMomentary': useMomentarySwitchChildSet(btnDevice1) == true || useMomentarySwitchChildSet(btnDevice1and2) == true,
            'dimmer': useDimmerChildSet(btnDevice1and2) == true,
            'button': useVirtualButtonChildSet(btnDevice1) == true],
        2: ['switch': useSwitchChildSet(btnDevice1and2) == true,
            'switchMomentary': useMomentarySwitchChildSet(btnDevice1and2) == true,
            'dimmer': useDimmerChildSet(btnDevice1and2) == true,
            'button': false],
        3: ['switch': useSwitchChildSet(btnDevice3and4) == true,
            'switchMomentary': useMomentarySwitchChildSet(btnDevice3and4) == true,
            'dimmer': useDimmerChildSet(btnDevice3and4) == true,
            'button': false],
        4: [:],
        5: ['switch': useSwitchChildSet(btnDevice5and6) == true,
            'switchMomentary': useMomentarySwitchChildSet(btnDevice5and6) == true,
            'dimmer': useDimmerChildSet(btnDevice5and6) == true,
            'button': false],
        6: [:],
    ]
    childDeviceConfig[4] = childDeviceConfig[3]
    childDeviceConfig[6] = childDeviceConfig[5]
    return childDeviceConfig
}

String getChildDeviceComboId(Integer button) {
    logging("getChildDeviceComboId(button=$button)", 1)
    if(button >= 1) {
        return button % 2 == 0 ? "${button - 1}_${button}" : "${button}_${button + 1}"
    } else {
        return null
    }
}

boolean buttonDown(Integer button, boolean useEvent=false) {
    boolean active = false
    if(useEvent == true) {
        logging("buttonDown(button=$button)", 100)
        Map childDeviceConfig = getChildDeviceConfig()
        if(childDeviceConfig[button]['switchMomentary'] == true) {
            setMomentarySwitch(buildChildDeviceId("$button"))
            active = true
        } else if(childDeviceConfig[button]['button'] == true) {
            sendButtonEvent(buildChildDeviceId("$button"), "pushed", 1)
            active = true
        }
    } else {
        logging("buttonDown(button=$button) UNUSED EVENT", 1)
    }
    return active
}

boolean buttonPushed(Integer button, boolean momentaryRelease=false) {
    logging("buttonPushed(button=$button)", 100)
    boolean active = false
    Map childDeviceConfig = getChildDeviceConfig()
    if(childDeviceConfig[button]['switch'] == true) {
        toggleChildSwitch(buildChildDeviceId("$button"))
        active = true
    } else if(childDeviceConfig[button]['switchMomentary'] == true) {
        active = true
        if(momentaryRelease == true) {
            releaseMomentarySwitch(buildChildDeviceId("$button"))
        } else {
            activateMomentarySwitch(buildChildDeviceId("$button"))
        }
    } else if(childDeviceConfig[button]['dimmer'] == true) {
        active = true
        stepLevel(buildChildDeviceId(getChildDeviceComboId(button)), button % 2 == 0 ? "up" : "down")
    } else if(childDeviceConfig[button]['button'] == true) {
        if(momentaryRelease == false) {
            sendButtonEvent(buildChildDeviceId("$button"), "pushed", 1)
        } else {
            sendButtonEvent(buildChildDeviceId("$button"), "released", 1)
        }
        active = true
    }
    return active
}

boolean buttonHeld(Integer button) {
    logging("buttonHeld(button=$button)", 100)
    boolean active = false
    Map childDeviceConfig = getChildDeviceConfig()
    if(childDeviceConfig[button]['switch'] == true) {
        active = true
        setChildSwitch(buildChildDeviceId("$button"), "off")
    } else if(childDeviceConfig[button]['dimmer'] == true) {
        active = true
        setChildSwitch(buildChildDeviceId(getChildDeviceComboId(button)), button % 2 == 0 ? "on" : "off")
    } else if(childDeviceConfig[button]['button'] == true) {
        sendButtonEvent(buildChildDeviceId("$button"), "held", 1)
        active = true
    }
    return active
}

boolean buttonDoubleTapped(Integer button) {
    logging("buttonDoubleTapped(button=$button)", 100)
    boolean active = false
    Map childDeviceConfig = getChildDeviceConfig()
    if(childDeviceConfig[button]['switch'] == true) {
        active = true
        setChildSwitch(buildChildDeviceId("$button"), "on")
    } else if(childDeviceConfig[button]['dimmer'] == true) {
        active = true
        prepareStartLevelChange(buildChildDeviceId(getChildDeviceComboId(button)), button % 2 == 0 ? "up" : "down")
    }
    return active
}

void componentRefresh(com.hubitat.app.DeviceWrapper cd) {
    logging("componentRefresh() from $cd.deviceNetworkId", 1)
}

void componentOn(com.hubitat.app.DeviceWrapper cd) {
    logging("componentOn() from $cd.deviceNetworkId", 1)
    getChildDevice(cd.deviceNetworkId).parse([[name: "switch", value: "on", isStateChange: false, descriptionText: "Switch turned ON"]])
}

void componentOff(com.hubitat.app.DeviceWrapper cd) {
    logging("componentOff() from $cd.deviceNetworkId", 1)
    getChildDevice(cd.deviceNetworkId).parse([[name: "switch", value: "off", isStateChange: false, descriptionText: "Switch turned OFF"]])
}

void componentStopLevelChange(com.hubitat.app.DeviceWrapper cd) {
    logging("componentStopLevelChange() from $cd.deviceNetworkId", 1)
    unschedule("runLevelChange_${cd.deviceNetworkId.split("-")[1]}")
}

void componentStartLevelChange(com.hubitat.app.DeviceWrapper cd, String direction) {
    logging("componentStartLevelChange() from $cd.deviceNetworkId (direction=$direction)", 1)
    prepareStartLevelChange(cd.deviceNetworkId, direction)
}

void componentSetLevel(com.hubitat.app.DeviceWrapper cd, BigDecimal level) {
    componentSetLevel(cd, level, null)
}

void componentSetLevel(com.hubitat.app.DeviceWrapper cd, BigDecimal level, BigDecimal duration) {
    level = level > 100 ? 100 : level < 0 ? 0 : level
    logging("componentSetLevel() from $cd.deviceNetworkId (level=$level, duration=$duration)", 1)
    prepareLevelChange(cd.deviceNetworkId, level, duration)
}

void prepareStartLevelChange(String deviceID, String direction) {
    logging("prepareStartLevelChange() from $deviceID (direction=$direction)", 1)
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    String cLevelStr = cd.currentState("level", true)?.value
    logging("cLevelStr = $cLevelStr", 1)
    Integer cLevel = cLevelStr != null ? cLevelStr.toInteger() : 50
    logging("cLevel = $cLevel", 1)
    if(direction == "up") {
        prepareLevelChange(cd.deviceNetworkId, 100, (20 / 100.0) * (100 - cLevel))
    } else {
        prepareLevelChange(cd.deviceNetworkId, 0, (20 / 100.0) * cLevel)
    }
}

void stepLevel(String deviceID, String direction) {
    logging("runLevelChange() from $deviceID (direction=$direction)", 1)
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    unschedule("runLevelChange_${deviceID.split("-")[1]}")
    String cLevelStr = cd.currentState("level", true)?.value
    logging("cLevelStr = $cLevelStr", 1)
    Integer cLevel = cLevelStr != null ? cLevelStr.toInteger() : 50
    if(direction == "up") {
        cLevel = cLevel + 5 > 100 ? 100 : cLevel + 5
        prepareLevelChange(cd.deviceNetworkId, cLevel, 0)
    } else {
        cLevel = cLevel - 5 < 0 ? 0 : cLevel - 5
        prepareLevelChange(cd.deviceNetworkId, cLevel, 0)
    }
    logging("cLevel = $cLevel", 1)
}

void prepareLevelChange(String deviceID, BigDecimal level, BigDecimal duration) {
    level = level > 100 ? 100 : level < 0 ? 0 : level
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    if(duration == null || duration <= 1) {
        cd.parse([[name: "level", value: level, isStateChange: false, descriptionText: "Level set to $level"]])
    } else {
        String cLevelStr = cd.currentState("level", true)?.value
        Integer cLevel = cLevelStr != null ? cLevelStr.toInteger() : null
        logging("cLevel = $cLevel, level = $level, duration = $duration", 1)
        if(cLevel == null || level == cLevel) {
            cd.parse([[name: "level", value: level, isStateChange: false, descriptionText: "Current level was null, level set to $level"]])
        } else {
            Integer levelDiff = Math.abs(cLevel - level)
            duration = duration > 3600 ? 3600 : duration
            BigDecimal changePerStep = levelDiff / duration
            Integer numSteps = duration
            Integer timeBetweenSteps = 1
            if(changePerStep > 0 && changePerStep < 1) {
                timeBetweenSteps = (1 / changePerStep).intValue()
                changePerStep = 1
                numSteps = duration / timeBetweenSteps
            }
            changePerStep = changePerStep.setScale(2, BigDecimal.ROUND_HALF_UP)
            changePerStepInt = changePerStep.intValue()
            changePerStepInt = level < cLevel ? changePerStepInt * -1 : changePerStepInt
            Integer missingSteps = levelDiff - (numSteps * changePerStepInt)
             
            runIn(timeBetweenSteps, "runLevelChange_${deviceID.split("-")[1]}", [data: [deviceID: deviceID, level:level, changePerStep:changePerStepInt, timeBetweenSteps:timeBetweenSteps]])
        }
        
    }
}

void runLevelChange_1_2(Map data) {
     
    runLevelChange(data["deviceID"], "runLevelChange_1_2", data["level"], data["changePerStep"], data["timeBetweenSteps"])
}

void runLevelChange_3_4(Map data) {
     
    runLevelChange(data["deviceID"], "runLevelChange_3_4", data["level"], data["changePerStep"], data["timeBetweenSteps"])
}

void runLevelChange_5_6(Map data) {
     
    runLevelChange(data["deviceID"], "runLevelChange_5_6", data["level"], data["changePerStep"], data["timeBetweenSteps"])
}

void runLevelChange(Map data) {
     
    runLevelChange(data["deviceID"], "runLevelChange", data["level"], data["changePerStep"], data["timeBetweenSteps"])
}

void runLevelChange(String deviceID, String methodName, BigDecimal level, Integer changePerStep, Integer timeBetweenSteps) {
    com.hubitat.app.DeviceWrapper cd = getChildDevice(deviceID)
    String cLevelStr = cd.currentState("level", true)?.value
    Integer cLevel = cLevelStr != null ? cLevelStr.toInteger() : null
    if(cLevel == null) {
        cd.parse([[name: "level", value: level, isStateChange: false, descriptionText: "Current level was null, can't use duration, level set to $level"]])
        if(level == 0) {
            setChildSwitch(cd.deviceNetworkId, "off")
        } else {
            setChildSwitch(cd.deviceNetworkId, "on")
        }
    } else {
        Integer nextLevel = cLevel + changePerStep
        if(changePerStep > 0) {
            nextLevel = nextLevel > level ? level : nextLevel
        } else {
            nextLevel = nextLevel < level ? level : nextLevel
        }
        if(nextLevel == 0) {
            setChildSwitch(cd.deviceNetworkId, "off")
        } else {
            setChildSwitch(cd.deviceNetworkId, "on")
        }
        if(nextLevel == level) {
            cd.parse([[name: "level", value: level, isStateChange: false, descriptionText: "Levelchange done, level set to $level"]])
        } else {
            cd.parse([[name: "level", value: nextLevel, isStateChange: false, descriptionText: "Levelchange in progress, level set to $nextLevel"]])
            runIn(timeBetweenSteps, methodName, [data: [deviceID: deviceID, level:level, changePerStep:changePerStep, timeBetweenSteps:timeBetweenSteps]])
        }
    }
}
// END:  getHelperFunctions('virtual-child-device-for-button')
