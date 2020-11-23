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
    definition (name: "Tasmota - TuyaMCU Wifi Touch Switch Legacy (Child)", namespace: "tasmota-legacy", author: "Markus Liljergren", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-tuyamcu-wifi-touch-switch-legacy-child-expanded.groovy") {
        capability "Light"
        capability "Switch"
        capability "Actuator"
    }
}

def getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - TuyaMCU Wifi Touch Switch Legacy (Child)', 'namespace': 'tasmota-legacy', 'author': 'Markus Liljergren', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-tuyamcu-wifi-touch-switch-legacy-child-expanded.groovy']
    return(deviceInfo[infoName])
}

/* These functions are unique to each driver */
void on() { 
    logging("$device on",1)
    parent.childOn(device.deviceNetworkId)
}

void off() {
    logging("$device off",1)
    parent.childOff(device.deviceNetworkId)
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
        }
    }
}

