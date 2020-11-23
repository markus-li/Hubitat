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

definition(
    name: "Tasmota Connect",
    namespace: "tasmota",
    author: "Markus Liljergren (markus-li)",
    description: "Service Manager for Tasmota-based Devices",
    category: "Convenience",
    //TODO: Replace these icons
    iconUrl:   "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/sonoff-connect.src/sonoff-connect-icon.png",
    iconX2Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/sonoff-connect.src/sonoff-connect-icon-2x.png",
    iconX3Url: "https://raw.githubusercontent.com/erocm123/SmartThingsPublic/master/smartapps/erocm123/sonoff-connect.src/sonoff-connect-icon-3x.png"
)

preferences {
	page(name: "mainPage")
    page(name: "configurePDevice")
    page(name: "deletePDevice")
    page(name: "changeName")
    page(name: "discoveryPage", title: "Device Discovery", content: "discoveryPage", refreshTimeout:10)
    page(name: "addDevices", title: "Add Tasmota-based Device", content: "addDevices")
    page(name: "manuallyAdd")
    page(name: "manuallyAddConfirm")
    page(name: "deviceDiscovery")
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "Manage your Tasmota-based Devices", nextPage: null, uninstall: true, install: true) {
        section("Configure"){
           href "deviceDiscovery", title:"Discover Devices", description:""
           href "manuallyAdd", title:"Manually Add Device", description:""
           //href "deviceDiscoveryCancel", title:"Cancel Discover Device", description:""
        }
        section("Installed Devices"){
            getChildDevices().sort({ a, b -> a["deviceNetworkId"] <=> b["deviceNetworkId"] }).each {
                href "configurePDevice", title:"$it.label", description:"", params: [did: it.deviceNetworkId]
            }
        }
    }
}

def configurePDevice(params){
   if (params?.did || params?.params?.did) {
      if (params.did) {
         state.currentDeviceId = params.did
         state.currentDisplayName = getChildDevice(params.did)?.displayName
      } else {
         state.currentDeviceId = params.params.did
         state.currentDisplayName = getChildDevice(params.params.did)?.displayName
      }
   }  
   if (getChildDevice(state.currentDeviceId) != null) getChildDevice(state.currentDeviceId).configure()
   dynamicPage(name: "configurePDevice", title: "Configure Tasmota-based Devices created with this app", nextPage: null) {
		section {
            app.updateSetting("${state.currentDeviceId}_label", getChildDevice(state.currentDeviceId).label)
            input "${state.currentDeviceId}_label", "text", title:"Device Name", description: "", required: false
            href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it"
        }
        section {
              href "deletePDevice", title:"Delete $state.currentDisplayName", description: ""
        }
   }
}

def manuallyAdd(){
   dynamicPage(name: "manuallyAdd", title: "Manually add a Tasmota-based Device", nextPage: "manuallyAddConfirm") {
		section {
			paragraph "This process will manually create a Tasmota-based Device based with the entered IP address. Tasmota Connect then communicates with the device to obtain additional information from it. Make sure the device is on and connected to your wifi network."
            input "deviceType", "enum", title:"Device Type", description: "", required: false, options: 
                ["Tasmota - Sonoff Basic R3",
                "Tasmota - TuyaMCU CE Smart Home WF500D Dimmer (EXPERIMENTAL)",
                "Tasmota - CE Smart Home LA-2-W3 Wall Outlet",
                "Tasmota - CE Smart Home LQ-2-W3 Wall Outlet",
                "Tasmota - AWP02L-N Plug",
                "Tasmota - CYYLTF BIFANS J23 Plug",
                "Tasmota - Gosund WP3 Plug",
                "Tasmota - SK03 Power Monitor Outdoor Plug",
                "Tasmota - Aoycocr X10S Power Monitor Plug",
                "Tasmota - Brilliant 20699 800lm RGBW Bulb",
                "Tasmota - Sonoff SV",
                "Tasmota - Sonoff TH",
                "Tasmota - Sonoff POW",
                "Tasmota - Sonoff S31",
                "Tasmota - KMC 4 Power Monitor Plug",
                "Tasmota - AWP04L Power Monitor Plug",
                "Tasmota - Sonoff 4CH Pro (Parent)",
                "Tasmota - TuyaMCU Wifi Touch Switch",
                "Tasmota - Sonoff POW R2",
                "Tasmota - Sonoff S2X",
                "Tasmota - Sonoff Mini",
                "Tasmota - Sonoff Basic",
                "Tasmota - S120 Plug",
                "Tasmota - YKYC-001 Power Monitor Plug",
                "Tasmota - Brilliant BL20925 Power Monitor Plug",
                "Tasmota - Prime CCRCWFII113PK Plug",
                "Tasmota - TuyaMCU Wifi Dimmer",
                "Tasmota - Unbranded RGB Controller with IR",
                "Tasmota - Sonoff 4CH (Parent)",
                "Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel",
                "Tasmota - Sonoff RF Bridge (Parent)",
                "Tasmota - RFLink (Parent)",
                "Tasmota - Sensor (Distance)",
                "Tasmota - Generic Wifi Switch/Plug",
                "Tasmota - Generic Wifi Switch/Light",
                "Tasmota - Generic RGB/RGBW Controller/Bulb/Dimmer",
                "Tasmota - Generic Temperature/Humidity/Pressure Device",
                "Tasmota - Generic Power Monitor Plug",
                "Tasmota - Generic Power Monitor Plug (Parent)",
                "Tasmota - Generic Wifi Dimmer",
                ]
            input "ipAddress", "text", title:"IP Address", description: "", required: false 
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
       log.debug "Creating Tasmota-based Wifi Device with dni: ${convertIPtoHex(ipAddress)}"
       addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug", "${convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug") + " (${ipAddress})",
           "data": [
           "ip": ipAddress,
           "port": "80" 
           ]
       ])
   
       app.updateSetting("ipAddress", "")
            
       dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		   section {
			   paragraph "The device has been added. Press next to return to the main page."
	    	}
       }
    } else {
        dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		    section {
			    paragraph "The entered ip address is not valid. Please try again."
		    }
        }
    }
}

def deletePDevice(){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted. Press next to continue"
            } 
        }
    
	} catch (e) {
        dynamicPage(name: "deletePDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            } 
        }
    
    }
}

def changeName(){
    def thisDevice = getChildDevice(state.currentDeviceId)
    thisDevice.label = settings["${state.currentDeviceId}_label"]

    dynamicPage(name: "changeName", title: "Change Name Summary", nextPage: "mainPage") {
	    section {
            paragraph "The device has been renamed. Press \"Next\" to continue"
        }
    }
}

def discoveryPage(){
   return deviceDiscovery()
}

def deviceDiscoveryCancel() {
    unsubscribe()
    unschedule()
}

def deviceDiscovery(params=[:])
{
	def devices = devicesDiscovered()
    
	int deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as int
	state.deviceRefreshCount = deviceRefreshCount + 1
	def refreshInterval = 20
    
	def options = devices ?: []
	def numFound = options.size() ?: 0

	if ((numFound == 0 && state.deviceRefreshCount > 25) || params.reset == "true") {
    	log.trace "Cleaning old device memory"
    	state.devices = [:]
        state.deviceRefreshCount = 0
        app.updateSetting("selectedDevice", "")
    }

	ssdpSubscribe()

	//Tasmota-based Device discovery request every 15 //25 seconds
	if((deviceRefreshCount % 5) == 0) {
		discoverDevices()
	}

	//setup.xml request every 3 seconds except on discoveries
	if(((deviceRefreshCount % 3) == 0) && ((deviceRefreshCount % 5) != 0)) {
		verifyDevices()
	}

	return dynamicPage(name:"deviceDiscovery", title:"Discovery Started!", nextPage:"addDevices", refreshInterval:refreshInterval, uninstall: true) {
		section("Please wait while we discover your Tasmota-based Devices. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedDevices", "enum", required:false, title:"Select Tasmota-based Device (${numFound} found)", multiple:true, options:options
		}
        section("Options") {
			href "deviceDiscovery", title:"Reset list of discovered devices", description:"", params: ["reset": "true"]
		}
	}
}

Map devicesDiscovered() {
	def vdevices = getVerifiedDevices()
	def map = [:]
	vdevices.each {
		def value = "${it.value.name}"
		def key = "${it.value.mac}"
		map["${key}"] = value
	}
	map
}

def getVerifiedDevices() {
	getDevices().findAll{ it?.value?.verified == true }
}

private discoverDevices() {
	sendHubCommand(new hubitat.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", hubitat.device.Protocol.LAN))
}

def configured() {
	
}

def buttonConfigured(idx) {
	return settings["lights_$idx"]
}

def isConfigured(){
   if(getChildDevices().size() > 0) return true else return false
}

def isVirtualConfigured(did){ 
    def foundDevice = false
    getChildDevices().each {
       if(it.deviceNetworkId != null){
       if(it.deviceNetworkId.startsWith("${did}/")) foundDevice = true
       }
    }
    return foundDevice
}

private virtualCreated(number) {
    if (getChildDevice(getDeviceID(number))) {
        return true
    } else {
        return false
    }
}

private getDeviceID(number) {
    return "${state.currentDeviceId}/${app.id}/${number}"
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {
    ssdpSubscribe()
    runEvery5Minutes("ssdpDiscover")
}

void ssdpSubscribe() {
    subscribe(location, "ssdpTerm.urn:schemas-upnp-org:device:Basic:1", ssdpHandler)
}

void ssdpDiscover() {
    sendHubCommand(new hubitat.device.HubAction("lan discovery urn:schemas-upnp-org:device:Basic:1", hubitat.device.Protocol.LAN))
}

def ssdpHandler(evt) {
    def description = evt.description
    def hub = evt?.hubId
    def parsedEvent = parseLanMessage(description)
    parsedEvent << ["hub":hub]

    def devices = getDevices()
    
    String ssdpUSN = parsedEvent.ssdpUSN.toString()
    
    if (devices."${ssdpUSN}") {
        def d = devices."${ssdpUSN}"
        def child = getChildDevice(parsedEvent.mac)
        def childIP
        def childPort
        if (child) {
            childIP = child.getDeviceDataByName("ip")
            childPort = child.getDeviceDataByName("port").toString()
            //log.debug "Device data: ($childIP:$childPort) - reporting data: (${convertHexToIP(parsedEvent.networkAddress)}:${convertHexToInt(parsedEvent.deviceAddress)})."
            if("${convertHexToIP(parsedEvent.networkAddress)}" != "0.0.0.0"){
               if(childIP != convertHexToIP(parsedEvent.networkAddress) || childPort != convertHexToInt(parsedEvent.deviceAddress).toString()){
                  log.debug "Device data (${child.getDeviceDataByName("ip")}) does not match what it is reporting(${convertHexToIP(parsedEvent.networkAddress)}). Attempting to update."
                  child.sync(convertHexToIP(parsedEvent.networkAddress), convertHexToInt(parsedEvent.deviceAddress).toString())
               }
            } else {
               log.debug "Device is reporting ip address of ${convertHexToIP(parsedEvent.networkAddress)}. Not updating." 
            }
        }

        if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
            d.networkAddress = parsedEvent.networkAddress
            d.deviceAddress = parsedEvent.deviceAddress
        }
    } else {
        devices << ["${ssdpUSN}": parsedEvent]
    }
}

void verifyDevices() {
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        def ip = convertHexToIP(it.value.networkAddress)
        def port = convertHexToInt(it.value.deviceAddress)
        String host = "${ip}:${port}"
        sendHubCommand(new hubitat.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", hubitat.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
    }
}

def getDevices() {
    state.devices = state.devices ?: [:]
}

void deviceDescriptionHandler(hubitat.device.HubResponse hubResponse) {
	//log.trace "description.xml response (application/xml)"
	def body = hubResponse.xml
    log.debug body?.device?.friendlyName?.text()
	if (body?.device?.modelName?.text().startsWith("Tasmota")) {
		def devices = getDevices()
		def device = devices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (device) {
			device.value << [name:body?.device?.friendlyName?.text() + " (" + convertHexToIP(hubResponse.ip) + ")", serialNumber:body?.device?.serialNumber?.text(), verified: true]
		} else {
			log.error "/description.xml returned a device that didn't exist"
		}
	}
}

def addDevices() {
    def devices = getDevices()
    def sectionText = ""

    selectedDevices.each { dni ->bridgeLinking
        def selectedDevice = devices.find { it.value.mac == dni }
        def d
        if (selectedDevice) {
            d = getChildDevices()?.find {
                it.deviceNetworkId == selectedDevice.value.mac
            }
        }
        
        if (!d) {
            log.debug selectedDevice
            log.debug "Creating Tasmota-based Device with dni: ${selectedDevice.value.mac}"

            def deviceHandlerName
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff Basic R3"))
                deviceHandlerName = "Tasmota - Sonoff Basic R3"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - TuyaMCU CE Smart Home WF500D Dimmer (EXPERIMENTAL)"))
                deviceHandlerName = "Tasmota - TuyaMCU CE Smart Home WF500D Dimmer (EXPERIMENTAL)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - CE Smart Home LA-2-W3 Wall Outlet"))
                deviceHandlerName = "Tasmota - CE Smart Home LA-2-W3 Wall Outlet"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - CE Smart Home LQ-2-W3 Wall Outlet"))
                deviceHandlerName = "Tasmota - CE Smart Home LQ-2-W3 Wall Outlet"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - AWP02L-N Plug"))
                deviceHandlerName = "Tasmota - AWP02L-N Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - CYYLTF BIFANS J23 Plug"))
                deviceHandlerName = "Tasmota - CYYLTF BIFANS J23 Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Gosund WP3 Plug"))
                deviceHandlerName = "Tasmota - Gosund WP3 Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - SK03 Power Monitor Outdoor Plug"))
                deviceHandlerName = "Tasmota - SK03 Power Monitor Outdoor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Aoycocr X10S Power Monitor Plug"))
                deviceHandlerName = "Tasmota - Aoycocr X10S Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Brilliant 20699 800lm RGBW Bulb"))
                deviceHandlerName = "Tasmota - Brilliant 20699 800lm RGBW Bulb"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff SV"))
                deviceHandlerName = "Tasmota - Sonoff SV"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff TH"))
                deviceHandlerName = "Tasmota - Sonoff TH"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff POW"))
                deviceHandlerName = "Tasmota - Sonoff POW"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff S31"))
                deviceHandlerName = "Tasmota - Sonoff S31"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - KMC 4 Power Monitor Plug"))
                deviceHandlerName = "Tasmota - KMC 4 Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - AWP04L Power Monitor Plug"))
                deviceHandlerName = "Tasmota - AWP04L Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff 4CH Pro (Parent)"))
                deviceHandlerName = "Tasmota - Sonoff 4CH Pro (Parent)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - TuyaMCU Wifi Touch Switch"))
                deviceHandlerName = "Tasmota - TuyaMCU Wifi Touch Switch"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff POW R2"))
                deviceHandlerName = "Tasmota - Sonoff POW R2"
            if (selectedDevice?.value?.name?.startsWith("Sonoff S2"))
                deviceHandlerName = "Tasmota - Sonoff S2X"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff Mini"))
                deviceHandlerName = "Tasmota - Sonoff Mini"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff Basic"))
                deviceHandlerName = "Tasmota - Sonoff Basic"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - S120 Plug"))
                deviceHandlerName = "Tasmota - S120 Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - YKYC-001 Power Monitor Plug"))
                deviceHandlerName = "Tasmota - YKYC-001 Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Brilliant BL20925 Power Monitor Plug"))
                deviceHandlerName = "Tasmota - Brilliant BL20925 Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Prime CCRCWFII113PK Plug"))
                deviceHandlerName = "Tasmota - Prime CCRCWFII113PK Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - TuyaMCU Wifi Dimmer"))
                deviceHandlerName = "Tasmota - TuyaMCU Wifi Dimmer"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Unbranded RGB Controller with IR"))
                deviceHandlerName = "Tasmota - Unbranded RGB Controller with IR"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff 4CH (Parent)"))
                deviceHandlerName = "Tasmota - Sonoff 4CH (Parent)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel"))
                deviceHandlerName = "Tasmota - ZNSN TuyaMCU Wifi Curtain Wall Panel"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sonoff RF Bridge (Parent)"))
                deviceHandlerName = "Tasmota - Sonoff RF Bridge (Parent)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - RFLink (Parent)"))
                deviceHandlerName = "Tasmota - RFLink (Parent)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Sensor (Distance)"))
                deviceHandlerName = "Tasmota - Sensor (Distance)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Wifi Switch/Plug"))
                deviceHandlerName = "Tasmota - Generic Wifi Switch/Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Wifi Switch/Light"))
                deviceHandlerName = "Tasmota - Generic Wifi Switch/Light"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic RGB/RGBW Controller/Bulb/Dimmer"))
                deviceHandlerName = "Tasmota - Generic RGB/RGBW Controller/Bulb/Dimmer"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Temperature/Humidity/Pressure Device"))
                deviceHandlerName = "Tasmota - Generic Temperature/Humidity/Pressure Device"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Power Monitor Plug"))
                deviceHandlerName = "Tasmota - Generic Power Monitor Plug"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Power Monitor Plug (Parent)"))
                deviceHandlerName = "Tasmota - Generic Power Monitor Plug (Parent)"
            if (selectedDevice?.value?.name?.startsWith("Tasmota - Generic Wifi Dimmer"))
                deviceHandlerName = "Tasmota - Generic Wifi Dimmer"
			else if (selectedDevice?.value?.name?.startsWith("quired"))
                deviceHandlerName = "Tasmota - Generic Wifi Switch/Plug"
            else if (selectedDevice?.value?.name?.startsWith("Aquired"))
                deviceHandlerName = "Tasmota - Generic Wifi Switch/Plug"
            else 
                deviceHandlerName = "Tasmota - Generic Wifi Switch/Plug"
            try {
            def newDevice = addChildDevice("tasmota", deviceHandlerName, selectedDevice.value.mac, selectedDevice?.value.hub, [
                "label": selectedDevice?.value?.name ?: "Tasmota - Generic Wifi Switch/Plug",
                "data": [
                    "mac": selectedDevice.value.mac,
                    "ip": convertHexToIP(selectedDevice.value.networkAddress),
                    "port": "" + Integer.parseInt(selectedDevice.value.deviceAddress,16)
                ]
            ])
                sectionText = sectionText + "Succesfully added Tasmota-based Device with ip address ${convertHexToIP(selectedDevice.value.networkAddress)} \r\n"
            } catch (e) {
                sectionText = sectionText + "An error occured ${e} \r\n"
            }
            
        }
        
	} 
    log.debug sectionText
        return dynamicPage(name:"addDevices", title:"Devices Added", nextPage:"mainPage",  uninstall: true) {
        if(sectionText != ""){
		section("Add Tasmota-based Device Results:") {
			paragraph sectionText
		}
        }else{
        section("No devices added") {
			paragraph "All selected devices have previously been added"
		}
        }
}
    }

def uninstalled() {
    unsubscribe()
    getChildDevices().each {
        deleteChildDevice(it.deviceNetworkId)
    }
}



private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}
