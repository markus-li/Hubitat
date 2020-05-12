// IMPORT URL: https://raw.githubusercontent.com/markus-li/Hubitat/release/apps/expanded/tasmota-device-manager-expanded.groovy

/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.2.0503T
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

// BEGIN:getDefaultParentImports()
/** Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
// Used for MD5 calculations
import java.security.MessageDigest
/* Default Parent Imports */
// END:  getDefaultParentImports()


definition(
    name: "Tasmota Device Manager",
    namespace: "tasmota",
    author: "Markus Liljergren (markus-li)",
    description: "Device Manager for Tasmota",
    category: "Convenience",
    //TODO: Replace these icons???
    iconUrl:   "",
    iconX2Url: "",
    iconX3Url: "",
    documentationLink: "https://github.com/markus-li/Hubitat/wiki"
) {
    appSetting "defaultTasmotaPassword"
}

preferences {
     page(name: "mainPage", title: "Tasmota Device Manager", install: true, uninstall: true)
     page(name: "deleteDevice")
     page(name: "refreshDevices")
     page(name: "resultPage")
     page(name: "configureTasmotaDevice")
     page(name: "addDevices", title: "Add Tasmota-based Device", content: "addDevices")
     page(name: "manuallyAdd")
     page(name: "manuallyAddConfirm")
     page(name: "changeName")

     page(name: "discoveryPage", title: "Device Discovery", content: "discoveryPage", refreshTimeout:10)
     page(name: "deviceDiscovery")
     page(name: "deviceDiscoveryPage2")
     page(name: "deviceDiscoveryReset")
     page(name: "discoveredAddConfirm")
     
}

// https://docs.smartthings.com/en/latest/smartapp-developers-guide/preferences-and-settings.html#preferences-and-settings

// This App is ONLY used for installation and management tasks, it DOES NOT RUN in the background when not used by the user.
// The code in this App has not been optimized for performance and is in many ways very "unclean".
// One day I might take the time to clean it up and optimize it, but it works as intended already and since it
// doesn't affect resource use it really isn't that important.

/**
 * DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 *
 *   Device configurations and functions for using them
 */
TreeMap getDeviceConfigurations() {
    // To add more devices, just add them below ;)
    // Don't forget that BOTH the App and the Universal driver needs to have this configuration.
    // If you add a device and it works well for you, please share your
    // configuration in the Hubitat Community Forum.
    //
    // typeId HAS to be unique
    // 
    // For the rest of the settings, see below to figure it out :P
    List deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
         ],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home-WF500D.html'],

        [typeId: 'ce-la-2-w3-wall-outlet',
         name: 'CE Smart Home LA-2-W3 Wall Outlet',
         template: '{"NAME":"CE LA-2-W3","GPIO":[255,255,255,255,157,17,0,0,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-2-W3.html'],

        [typeId: 'ce-lq-2-w3-wall-outlet',
         name: 'CE Smart Home LQ-2-W3 Wall Outlet',
         template: '{"NAME":"CE LQ-2-W3","GPIO":[255,255,255,255,255,17,255,255,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LQ-2-W3.html'],

        [typeId: 'ce-la-wf7-pm-plug',
         name: 'CE Smart Home LA-WF7 Power Monitor Plug',
         template: '{"NAME":"CESmartHLA-WF7","GPIO":[0,56,0,17,134,132,0,0,131,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-WF7.html'],

        [typeId: 'awp02l-n-plug',
         name: 'AWP02L-N Plug',
         template: '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/hugoai_awp02l-n.html'],

        [typeId: 'cyyltf-bifans-j23-plug',
         name: 'CYYLTF BIFANS J23 Plug',
         template: '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/cyyltd_bifans_J23.html'],

        [typeId: 'gosund-wp3-plug',
         name: 'Gosund WP3 Plug',
         template: '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/gosund_wp3.html'],

        [typeId: 'sk03-pm-outdoor-plug',
         name: 'SK03 Power Monitor Outdoor Plug',
         template: '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/SK03_outdoor.html'],

        [typeId: 'aoycocr-x10s-pm-plug',
         name: 'Aoycocr X10S Power Monitor Plug',
         template: '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/aoycocr_X10S.html'],

        [typeId: 'brilliant-20699-rgbw-bulb',
         name: 'Brilliant 20699 800lm RGBW Bulb',
         template: '{"NAME":"Brilliant20699","GPIO":[0,0,0,0,141,140,0,0,37,142,0,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/brilliant_20699.html'],

        [typeId: 'sonoff-sv',
         name: 'Sonoff SV',
         template: '{"NAME":"Sonoff SV","GPIO":[17,255,0,255,255,255,0,0,21,56,255,0,0],"FLAG":1,"BASE":3}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_SV.html'],

        [typeId: 'sonoff-th',
         name: 'Sonoff TH',
         template: '{"NAME":"Sonoff TH","GPIO":[17,255,0,255,255,0,0,0,21,56,255,0,0],"FLAG":0,"BASE":4}',
         installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
         deviceLink: 'https://templates.blakadder.com/sonoff_TH.html'],

        [typeId: 'sonoff-pow',
         name: 'Sonoff POW',
         template: '{"NAME":"Sonoff Pow","GPIO":[17,0,0,0,0,130,0,0,21,132,133,52,0],"FLAG":0,"BASE":6}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_Pow.html'],

        [typeId: 'sonoff-s31',
         name: 'Sonoff S31',
         template: '{"NAME":"Sonoff S31","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":41}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_S31.html'],
        
        [typeId: 'sonoff-ifan02',
         name: 'Sonoff iFan02',
         module: 44,
         //template: '{"NAME":"Sonoff iFan02","GPIO":[17,255,0,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":44}',
         installCommands: [['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan02.html'],

        /*[typeId: 'sonoff-ifan03-no_beep-m44',
         name: 'Sonoff iFan03 (No Beep) M44',
         template: '{"NAME":"Sonoff iFan03","GPIO":[17,255,0,255,0,0,29,33,23,56,22,24,0],"FLAG":0,"BASE":44}',
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m44',
         name: 'Sonoff iFan03 (Beep) M44',
         template: '{"NAME":"Sonoff iFan03","GPIO":[17,255,0,255,0,0,29,33,23,56,22,24,0],"FLAG":0,"BASE":44}',
         installCommands: [["SetOption67", "0"],
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],*/

        [typeId: 'sonoff-ifan03-no_beep-m71',
         name: 'Sonoff iFan03 (No Beep)',
         module: 71,
         //template: '{"NAME":"SonoffiFan03","GPIO":[17,148,0,149,0,0,29,161,23,56,22,24,0],"FLAG":0,"BASE":71}',
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m71',
         name: 'Sonoff iFan03 (Beep)',
         module: 71,
         //template: '{"NAME":"SonoffiFan03","GPIO":[17,148,0,149,0,0,29,161,23,56,22,24,0],"FLAG":0,"BASE":71}',
         installCommands: [["SetOption67", "1"], 
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'treatlife-ds01-dimmer',
         name: 'TreatLife DS01 Dimmer ',
         template: '{"NAME":"TL DS01 Dimmer","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
         installCommands: [["TuyaMCU", "21,2"], 
                           ["DimmerRange", "150,1000"]],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

        [typeId: 'kmc-4-pm-plug',
         name: 'KMC 4 Power Monitor Plug',
         template: '{"NAME":"KMC 4 Plug","GPIO":[0,56,0,0,133,132,0,0,130,22,23,21,17],"FLAG":0,"BASE":36}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

        [typeId: 'teckin-ss30-power-strip',
         name: 'Teckin SS30 Power Strip',
         template: '{"NAME":"Teckin SS30","GPIO":[52,255,255,57,29,17,0,0,31,30,32,255,25],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/teckin_ss30.html'],

        [typeId: 'teckin-sp10-plug',
         name: 'Teckin SP10 Plug',
         template: '{"NAME":"Teckin SP10","GPIO":[255,255,56,255,255,255,0,0,255,17,255,21,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/teckin_SP10.html'],

        [typeId: 'awp04l-pm-plug',
         name: 'AWP04L Power Monitor Plug',
         template: '{"NAME":"AWP04L","GPIO":[57,255,255,131,255,134,0,0,21,17,132,56,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/awp04l.html'],

        [typeId: 'dd001-mini-ir-v08-rgb-led-controller-no-ir',
         name: 'DD001-MINI(G)-IR-V08 RGB LED Controller (no IR)',
         template: '{"NAME":"DD001-NOIR-RGB","GPIO":[0,0,0,0,37,0,0,0,38,0,39,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/DD001-MINIG-IR-V08.html'],

        [typeId: 'sonoff-4ch-pro',
         name: 'Sonoff 4CH Pro',
         template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'unbranded-rgbwwcw-controller-type-1',
         name: 'Unbranded RGBWWCW Controller (Type 1)',
         template: '{"NAME":"CtrlRGBWWCW-T1","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: ''],
        
        [typeId: 'tuyamcu-touch-switch-1-button',
        name: 'TuyaMCU Touch Switch - 1 button',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,0"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        // {"NAME":"Lucci Fan","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":44}
        [typeId: 'tuyamcu-lucci-connect-remote-as-switches',
        name: 'TuyaMCU Lucci Connect Remote',
        template: '{"NAME":"Lucci Fan","GPIO":[0,0,0,0,0,0,0,0,0,108,0,107,0],"FLAG":0,"BASE":54}',
        installCommands: [["TuyaMCU", "11,102"], ["TuyaMCU", "12,1"], ["TuyaMCU", "13,0"], 
                          ["TuyaMCU", "14,0"], ["TuyaMCU", "15,0"], ["TuyaMCU", "21,50"], 
                          ["Rule1", "on TuyaReceived#Data=55AA00070005020400010012 do dimmer 1 endon on TuyaReceived#Data=55AA00070005020400010113 do dimmer 2 endon on TuyaReceived#Data=55AA00070005020400010214 do dimmer 3 endon on Dimmer#State=1 do TuyaSend4 2,0 endon on Dimmer#State=2 do TuyaSend4 2,1 endon on Dimmer#State=3 do TuyaSend4 2,2 endon"], 
                          ["Rule1", "1"]],
        deviceLink: 'https://templates.blakadder.com/luci-connect-remote-control.html'],

        [typeId: 'tuyamcu-touch-switch-2-button',
        name: 'TuyaMCU Touch Switch - 2 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-3-button',
        name: 'TuyaMCU Touch Switch - 3 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,0"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-4-button',
        name: 'TuyaMCU Touch Switch - 4 buttons',
        module: 54,
        template: '',
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,4"]],
        deviceLink: ''],

        [typeId: 'sonoff-powr2', 
        name: 'Sonoff POW R2',
        template: '{"NAME":"Sonoff Pow R2","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":43}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_Pow_R2.html'],

        [typeId: 'sonoff-s20', 
        name: 'Sonoff S20',
        template: '{"NAME":"Sonoff S20","GPIO":[17,255,255,255,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S20.html'],

        [typeId: 'sonoff-s26', 
        name: 'Sonoff S26',
        template: '{"NAME":"Sonoff S26","GPIO":[17,255,255,255,0,0,0,0,21,158,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S26.html'],

        [typeId: 'sonoff-mini', 
        name: 'Sonoff Mini',
        template: '{"NAME":"Sonoff Mini","GPIO":[17,0,0,0,9,0,0,0,21,56,0,0,255],"FLAG":0,"BASE":1}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_mini.html'],

        [typeId: 'sonoff-basic',
        name: 'Sonoff Basic',
        module: 1,
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_basic.html'],

        [typeId: 's120-plug' ,
        name: 'S120 USB Charger Plug',
        template: '{"NAME":"S120 Plug","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 's120-plug-bmp280' ,
        name: 'S120 USB Charger Plug + BMP280',
        template: '{"NAME":"S120THPPlug","GPIO":[0,6,0,5,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'globe-34207-bulb' ,
        name: 'Globe 34207 800lm RGBCCT Bulb',
        template: '{"NAME":"GlobeRGBWW","GPIO":[0,0,0,0,37,40,0,0,38,41,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/globe-34207.html'],

        [typeId: 'brilliantsmart-20676-plug' ,
        name: 'BrilliantSmart 20676 USB Charger Plug',
        template: '{"NAME":"Brilliant20676","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliantsmart-20741-bulb' ,
        name: 'BrilliantSmart 20741 9W 750lm RGBW Bulb',
        template: '{"NAME":"BS-20741-RGBW","GPIO":[0,0,0,0,37,40,0,0,38,0,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20741.html'],

        [typeId: 'brilliant-bl20925-pm-plug', 
        name: 'Brilliant Lighting BL20925 PM Plug',
        template: '{"NAME":"BL20925","GPIO":[0,0,0,17,133,132,0,0,131,158,21,0,0],"FLAG":0,"BASE":52}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliant_BL20925.html'],

        [typeId: 'deta-6930ha-plug', 
        name: 'Deta 6930HA Plug',
        template: '{"NAME":"Deta6930HAPlug","GPIO":[0,17,0,0,0,0,0,0,0,56,21,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/deta_6930HA.html'],

        [typeId: 'prime-ccrcwfii113pk-plug', 
        name: 'Prime CCRCWFII113PK Plug',
        template: '{"NAME":"PrimeCCRC13PK","GPIO":[0,0,0,0,57,56,0,0,21,122,0,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/prime_CCRCWFII113PK.html'],

        [typeId: 'ykyc-wj1y0-10a', 
        name: 'YKYC-WJ1Y0-10A PM Plug',
        template: '{"NAME":"YKYC-001PMPlug","GPIO":[0,17,0,57,133,132,0,0,130,56,21,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: ''],

        [typeId: 'merkury-mi-bw210-999w', 
        name: 'Merkury MI-BW210-999W',
        template: '{"NAME":"MI-BW210-999W","GPIO":[0,0,0,0,140,37,0,0,142,38,141,0,0],"FLAG":0,"BASE":48}',
        installCommands: [],
        deviceLink: ''],

        [typeId: 'lumary-rgbcct-led-strip', 
        name: 'Lumary RGBCCT LED Strip',
        template: '{"NAME":"Lumary LED","GPIO":[17,0,0,0,37,40,0,0,38,41,39,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/lumary_led_strip.html'],

        [typeId: 'xs-ssa06-plug', 
        name: 'XS-SSA06 Plug with RGB',
        template: '{"NAME":"XS-SSA06-RGB","GPIO":[37,0,38,0,0,39,0,0,0,90,0,21,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/XS-SSA06.html'],

        [typeId: 'tuyamcu-wifi-dimmer', 
        name: 'TuyaMCU Wifi Dimmer',
        module: 54,
        installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
        ],
        deviceLink: ''],

        [typeId: 'zigbee-controller-default' ,
        name: 'Zigbee Controller (default pinout)',
        template: '{"NAME":"Zigbee","GPIO":[0,0,0,0,0,0,0,0,0,166,0,165,0],"FLAG":0,"BASE":18}',
        installCommands: [["SerialLog", "0"],
                          //['setoption3', '1'], // enable MQTT - REQUIRED for this Zigbee devices to work!
                          ],
        deviceLink: 'https://tasmota.github.io/docs/#/Zigbee'],

        [typeId: 'unbranded-rgb-controller-with-ir-type-1' ,
        name: 'Unbranded RGB Controller with IR (Type 1)',
        template: '{"NAME":"RGB Controller","GPIO":[0,0,0,0,0,38,0,0,39,51,0,37,0],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: 'sonoff-4ch',
        name: 'Sonoff 4CH',
        template: '{"NAME":"Sonoff 4CH","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":7}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_4CH.html'],

        [typeId: 'sonoff-4ch-pro-r2',
        name: 'Sonoff 4CH Pro (R2)',
        template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'nedis-ir-bridge',
        name: 'Nedis IR Bridge',
        template: '{"NAME":"Nedis IR Bridge","GPIO":[255,255,255,255,56,51,0,0,0,17,8,0,0],"FLAG":0,"BASE":62}',
        installCommands: [['SerialLog', '0']],
        deviceLink: 'https://templates.blakadder.com/nedis_WIFIRC10CBK.html'],

        [typeId: 'luminea-zx-2844-rgbw-led-controller',
        name: 'Luminea ZX-2844 RGBW LED Controller ',
        template: '{"NAME":"Luminea ZX-284","GPIO":[40,0,0,0,0,39,0,0,38,17,37,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/luminea_zx-2844.html'],

        [typeId: 'tuyamcu-znsn-wifi-curtain-wall-panel',
        comment: 'NOT GENERIC - read the instructions',
        name: 'TuyaMCU ZNSN Wifi Curtain Wall Panel',
        module: 54,
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        //SetOption66 - Set publishing TuyaReceived to MQTT  »6.7.0
                        //0 = disable publishing TuyaReceived over MQTT (default)
                        //1 = enable publishing TuyaReceived over MQTT
                        ['SetOption66', "1"], // This is REQUIRED to get the Tuya Data
                        ['Mem1', '100'],   // Updated with the current Curtain location
                        ['Mem2', '11'],    // Step for each increase
                        ['Mem3', '1'],     // delay in 10th of a second (1 = 100ms)
                        ['Mem4', '9'],     // Motor startup steps
                        ['Mem5', '1'],     // Extra step when opening
                        ['Delay', '15'],   // Set delay between Backlog commands
                        ['Rule1', 'ON Dimmer#State DO Mem1 %value%; ENDON'],
                        ['Rule1', '+ ON TuyaReceived#Data=55AA00070005650400010277 DO Backlog Var1 %mem1%; Var2 Go; Var5 C; Add1 %mem2%; Sub1 %mem4%; Var4 %mem2%; Event Go; ENDON'],
                        ['Rule1', '+ ON Event#Go DO Backlog Dimmer %var1%; Event %var5%%var1%; Event %var2%2; ENDON'],
                        ['Rule1', '+ ON Event#Go2 DO Backlog Add1 %var4%; Delay %mem3%; Event %var1%; Event %var2%;  ENDON'],
                        ['Rule1', '+ ON Event#O-7 DO Var2 sC; ENDON ON Event#O-8 DO Var2 sC; ENDON ON Event#O-9 DO Var2 sC; ENDON ON Event#O-10 DO Var2 sC; ENDON ON Event#O-11 DO Var2 sC; ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', 'ON TuyaReceived#Data=55AA00070005650400010176 DO Backlog Var1 %mem1%; Var2 Go; Var5 O; Sub1 %mem2%; Add1 %mem4%; Var4 %mem2%; Add4 %mem5%; Mult4 -1; Event Go; ENDON'],
                        ['Rule2', '+ ON Event#sC DO Backlog Var2 sC2; Event sC2; ENDON'],
                        ['Rule2', '+ ON Event#sC2 DO Backlog Var2 sC2; TuyaSend4 101,1; ENDON'],
                        ['Rule2', '+ ON TuyaReceived#Data=55AA00070005650400010075 DO Var2 sC3; ENDON'],
                        ['Rule2', '+ ON Event#C107 DO Var2 sC; ENDON ON Event#C108 DO Var2 sC; ENDON ON Event#C109 DO Var2 sC; ENDON ON Event#C110 DO Var2 sC; END ON ON Event#C111 DO Var2 sC; ENDON'],
                        ['Rule2', '1'],
                        ['Rule3', 'ON Event#C100 DO Var2 sC; ENDON ON Event#C101 DO Var2 sC; ENDON ON Event#C102 DO Var2 sC; ENDON ON Event#C103 DO Var2 sC; ENDON ON Event#C104 DO Var2 sC; ENDON ON Event#C105 DO Var2 sC; ENDON ON Event#C106 DO Var2 sC; ENDON ON Event#O0 DO Var2 sC; ENDON ON Event#O-1 DO Var2 sC; ENDON ON Event#O-2 DO Var2 sC; ENDON ON Event#O-3 DO Var2 sC; ENDON ON Event#O-4 DO Var2 sC; ENDON ON Event#O-5 DO Var2 sC; ENDON ON Event#O-6 DO Var2 sC; ENDON ON Event#O-12 DO Var2 sC; ENDON'],
                        ['Rule3', '1']],
        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        [typeId: 'mj-sd02-dimmer-switch',
        comment: 'WITHOUT power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02","GPIO":[19,18,0,33,34,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        // Possible alternative: {"NAME":"MJ-SD02","GPIO":[19,18,0,35,36,34,255,255,33,37,32,126,29],"FLAG":15,"BASE":18}
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption3', '1'], // enable MQTT - REQUIRED for these rules to work!
                        ['setoption1', '1'], // restrict to single, double and hold actions (i.e., disable inadvertent reset due to long press)
                        ['setoption32', '8'],     // Number of 0.1 seconds to hold button before sending HOLD action message.
                        ['buttontopic', '0'],   // This enables the below Rule triggers
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'mj-sd02-dimmer-switch-led',
        comment: 'WITH power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02-LED","GPIO":[19,18,0,33,56,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        // Possible alternative: {"NAME":"MJ-SD02","GPIO":[19,18,0,35,36,34,255,255,33,37,32,126,29],"FLAG":15,"BASE":18}
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption3', '1'], // enable MQTT - REQUIRED for these rules to work!
                        ['setoption1', '1'], // restrict to single, double and hold actions (i.e., disable inadvertent reset due to long press)
                        ['setoption32', '8'],     // Number of 0.1 seconds to hold button before sending HOLD action message.
                        ['buttontopic', '0'],   // This enables the below Rule triggers
                        ['LedPower', '1'],
                        ['SetOption31', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        //https://templates.blakadder.com/oil_diffuser_550ml.html

        
        [typeId: 'maxcio-diffuser-v1',
        comment: 'REQUIRES "Use Alternate Color command in Tasmota" to be set!',
        name: 'Maxcio Diffuser Wood Grain (v1)',
        template: '{"NAME":"MaxcioDiffuser","GPIO":[0,107,0,108,21,0,0,0,37,38,39,28,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"], // A good idea for dimmers
                        ['SerialLog', '0'],
                        ['setoption20', '1'], // Update of Dimmer/Color/CT without turning power on
                        //['SetOption59', '0'],
                        //['SwitchMode', '1'],
                        //['SetOption66', '0'],   // Set publishing TuyaReceived to MQTT, 0 = disable, 1 = enable
                        //['SetOption34', '100'],  // 0..255 = set Backlog inter-command delay in milliseconds (default = 200)
                        ['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; rule2 0; power1 1; rule2 1; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO power1 1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog rule2 0; power2 0; rule2 1; power3 %var2%; var2 1; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog rule2 0; power2 1; rule2 1; var2 0; power3 0; ENDON'],
                        ['Rule2', 'ON Power1#State DO tuyasend1 5,%value% ENDON ON Power2#State=0 DO tuyasend1 1,0 ENDON ON Power2#State=1 DO backlog var2 1; tuyasend1 1,1; ENDON'],
                        ['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO power1 0 ENDON'],
                        //['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; var7 no; power1 1; ENDON'],
                        //['Rule2', 'ON Power1#State DO backlog var5 0; var7 1; tuyasend%var7% 5,%value%; ENDON ON Power2#State=0 DO backlog var6 0; tuyasend1 1,0; ENDON ON Power2#State=1 DO backlog var6 0; var2 1; tuyasend1 1,1; ENDON'],
                        //['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO backlog powe%var5% 0; var5 r1; ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO backlog powe%var5% 1; var5 r1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog powe%var6% 0; power3 %var2%; var2 1; var6 r2; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog powe%var6% 1; var2 0; power3 0; var6 r2; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', '1'],
                        ['Rule3', '1']],
        deviceLink: 'https://templates.blakadder.com/maxcio_400ml_diffuser.html'],

        // https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433pi 
        [typeId: 'sonoff-rf-bridge-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_RF_bridge.html'],
        
        [typeId: 'rflink-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'http://www.rflink.nl/blog2/wiring'],
        
        // Generic Tasmota Devices:
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        installCommands: [],
        deviceLink: ''],

        /*[typeId: '01generic-switch-plug',
        comment: 'Works as Plug/Outlet with Alexa' ,
        name: 'Generic Switch/Plug',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-light',
         comment: 'Works as Light with Alexa' ,
        name: 'Generic Switch/Light',
        template: '',
        installCommands: [],
        deviceLink: ''],*/

        [typeId: '01generic-rgb-rgbw-controller-bulb-dimmer', 
        comment: 'RGB+WW+CW should all work properly',
        name: 'Generic RGB/RGBW Controller/Bulb/Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: '01generic-thp-device' ,
        name: 'Generic Temperature/Humidity/Pressure Device',
        template: '',
        installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
        deviceLink: ''],

        /*[typeId: '01generic-dimmer' ,
        name: 'Generic Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],*/
    ]

    TreeMap deviceConfigurationsMap = [:] as TreeMap
    deviceConfigurations.each{
        deviceConfigurationsMap[it["typeId"]] = it
    }
    return deviceConfigurationsMap
}

def getDeviceConfiguration(String typeId) {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    try{
        return deviceConfigurationsMap[typeId]
    } catch(e) {
        log.warn "Failed to retrieve Device Configuration '$typeId': $e"
        return null
    }
}

def getDeviceConfigurationsAsListOption() {
    TreeMap deviceConfigurationsMap = getDeviceConfigurations()
    def items = []
    deviceConfigurationsMap.sort({ a, b -> a.key <=> b.key }).each { k, v ->
        def label = v["name"]
        if(v.containsKey("comment") && v["comment"].length() > 0) {
            label += " (${v["comment"]})"
        }
        if(!(v.containsKey("notForUniversal") && v["notForUniversal"] == true)) {
            items << ["${v["typeId"]}":"$label"] 
        }
    }
    return items
}

/**
 * --END-- DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
 */

Long getMillisSinceDate(myDate) {
    
    //myDate
    return now() - myDate.getTime()
}

String getTimeStringSinceMillis(millis) {
    Integer seconds = (Integer) (millis / 1000) % 60
    Integer minutes = (Integer) (millis / (1000*60)) % 60
    Integer hours = (Integer) (millis / (1000*60*60)) % 24
    Integer days = (Integer) (millis / (1000*60*60*24))
    return String.format("%dT%02d:%02d:%02d", days, hours, minutes, seconds)
}

String getTimeStringSinceDate(myDate) {
    return getTimeStringSinceMillis(getMillisSinceDate(myDate))
}

Map getTimeStringSinceDateWithMaximum(myDate, maxMillis) {
    def millis = getMillisSinceDate(myDate)
    return [time:getTimeStringSinceMillis(millis), red:millis > maxMillis]
}

// BEGIN:getDefaultAppMethods()
/* Default App Methods go here */
private String getAppVersion() {
    String version = "v1.0.2.0503T"
    logging("getAppVersion() = ${version}", 50)
    return version
}
// END:  getDefaultAppMethods()

 
void makeAppTitle(btnDone=false) {
    section(getElementStyle('title', getMaterialIcon('build', 'icon-large') + "${app.label} <span id='version'>${getAppVersion()}</span>" + getCSSStyles(btnDone))){
        }
}

Map mainPage() {
    return dynamicPage(name: "mainPage", title: "", nextPage: null, uninstall: true, install: true) {
        makeAppTitle() // Also contains the CSS
        logging("Building mainPage", 1)
        // Hubitat green: #81BC00
        // box-shadow: 2px 3px #A9A9A9
        installCheck()
        initializeAdditional()
        if (state.appInstalled == 'COMPLETE') {
            section(getElementStyle('header', getMaterialIcon('settings_applications') + "Configure App"), hideable: true, hidden: false){
                getElementStyle('separator')
                // BEGIN:getDefaultMetadataPreferences()
                // Default Preferences
                input(name: "debugLogging", type: "bool", title: addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
                input(name: "infoLogging", type: "bool", title: addTitleDiv("Enable descriptionText logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
                // END:  getDefaultMetadataPreferences()
                input("passwordDefault", "password", title:"Default Tasmota Password", submitOnChange: true, displayDuringSetup: true)
            
            //input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
            //href "deviceDiscoveryCancel", title:"Cancel Discover Device", description:""
            }
            section(getElementStyle('header', getMaterialIcon('library_add') + "Install New Devices"), hideable: true, hidden: false){
                href("deviceDiscovery", title:getMaterialIcon('', 'he-discovery_1') + "Discover Devices (using SSDP)", description:"")
                href("manuallyAdd", title:getMaterialIcon('', 'he-add_1') + "Manually Install Device", description:"")
            }
            section(getElementStyle('header', getMaterialIcon('playlist_add') + 'Grant Access to Additional Devices'), hideable: true, hidden: true){
                paragraph("Select the devices to grant access to, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                input(name:	"devicesSelected", type: "capability.initialize", title: "Available Devices", multiple: true, required: false, submitOnChange: true)
            }
            section(getElementStyle('header', getMaterialIcon('', 'he-settings1') + "Configure Devices"), hideable: true, hidden: false){ 
                paragraph('<div style="margin: 8px;">All devices below use a compatible driver, if any device is missing, add them above in "Grant Access to Additional Devices". Newly selected devices will not be shown until after you\'ve pressed Done. \"Refresh Devices\" runs the \"Refresh\" command on all devices in the list, this can take a bit of time if you have many devices...</div>')
                
                //input(name: "refreshDevices", type: "bool", defaultValue: "false", submitOnChange: true, title: "Refresh Devices", description: "Refresh Devices Desc")
                href("resultPage", title:getMaterialIcon('autorenew') + "Result Page", description: "")
                href("refreshDevices", title:getMaterialIcon('autorenew') + "Refresh Devices", description: "")
                Integer numDevices = 0
                Integer numChildDevices = 0
                getAllTasmotaDevices().each { rawDev ->
                //state.devices.each { rawDev ->
                    def cDev = getTasmotaDevice(rawDev.deviceNetworkId)
                    //getLastActivity()
                    if(cDev != null) {
                        href("configureTasmotaDevice", title:"${getMaterialIcon('', 'he-bulb_1 icon-small')} $cDev.label", description:"", params: [did: cDev.deviceNetworkId])
                        
                        numDevices += 1

                        Map lastActivity = getTimeStringSinceDateWithMaximum(cDev.getLastActivity(), 2*60*60*1000)
                        // Status
                        def deviceStatus = cDev.currentState('presence')?.value
                        logging("$cDev.id - deviceStatus = $deviceStatus", 1)
                        if(deviceStatus == null || deviceStatus == "not present") {
                            deviceStatus = "Timeout"
                        } else {
                            deviceStatus = "Available"
                        }

                        // Wifi
                        def wifiSignalQuality = cDev.currentState('wifiSignal')
                        
                        boolean wifiSignalQualityRed = true
                        if(wifiSignalQuality != null) {
                            wifiSignalQuality = wifiSignalQuality.value
                            wifiSignalQualityRed = extractInt(wifiSignalQuality) < 50
                        }
                        logging("$cDev.id - wifiSignalQuality = $wifiSignalQuality", 1)
                        String uptime = "${cDev.getDeviceDataByName('uptime')}"
                        String firmware = "${cDev.getDeviceDataByName('firmware')}"
                        String driverVersion = "${cDev.getDeviceDataByName('driver')}"
                        String driverName = "${getDeviceDriverName(cDev)}"
                        List childDevs = runDeviceCommand(rawDev, 'getChildDevices')
                        //logging("childDevs: $childDevs", 1)
                        List childDevsFiltered = []
                        childDevs.each {
                            logging("Child: $it.id, label: $it.label, driver: $it.data.driver", 1)
                            childDevsFiltered += ['id': it.id.toInteger(), 'label': it.label, 'driver': it.data.driver]
                            numChildDevices += 1
                        }
                        childDevsFiltered.sort({ a, b -> a["id"] <=> b["id"] })
                        logging("Children: $childDevsFiltered", 1)
                        getDeviceTable([href:           [href:getDeviceConfigLink(cDev.id)],
                                        ip:             [data:rawDev['data']['ip']],
                                        //[data:runDeviceCommand(getTasmotaDevice(cDev.deviceNetworkId), 'getDeviceDataByName', ['uptime'])],])
                                        uptime:         [data:uptime, red:uptime == "null"],
                                        lastActivity:   [data:lastActivity['time'], red:lastActivity['red']],
                                        wifi:           [data:"${wifiSignalQuality}", red:wifiSignalQualityRed],
                                        firmware:       [data:firmware, red:firmware == "null"],
                                        driverVersion:  [data:driverVersion, red:driverVersion == "null"],
                                        deviceStatus:   [data:deviceStatus, red:deviceStatus != "Available"],
                                        driverName:     [data:driverName, red:driverName == "null"],
                                        childDevices:   childDevsFiltered,])
                                // it.label
                            //btnParagraph([[href:getDeviceConfigLink(cDev.id), target:"_blank", title:"Config"],
                            //            [href:getDeviceTasmotaConfigLink(cDev['data']['ip']), target:"_blank", title:'Tasmota&nbsp;Web&nbsp;Config (' + cDev['data']['ip'] + ')']],
                            //)
                                //paragraph('<table style="border-spacing: 10px 0px"><tr><td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceConfigLink(it.id)}" + '" target="deviceConfig">Config</a></td>' + 
                                        //' | <a href="' + "${getDeviceLogLink(it.id)}" + '" target="deviceConfig">Log</a>' +
                                //        '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td></tr></table>' )
                        
                        //%7B%22did%22%3A%225002915AFD0A%22%7D
                        //%7B%22did%22%3A%222CF43222E6AD%22%7D
                    }
                }
                paragraph("<div style=\"margin: 8px; text-align: right;\">$numDevices Parent device${numDevices == 1 ? '' : 's'}" + 
                          " + $numChildDevices Child device${numChildDevices == 1 ? '' : 's'} installed</div>")
            }
            /*section(getElementStyle('header', "More things"), hideable: true, hidden: true){
                paragraph("Select the devices to configure, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                
                input(name:	"devicesAvailable", type: "enum", title: "Available Devices", multiple: true, required: false, submitOnChange: true, options: state.devicesSelectable)
            }*/
        } else {
            section(getElementStyle('subtitle', "Configure")){
                // BEGIN:getDefaultMetadataPreferences()
                // Default Preferences
                input(name: "debugLogging", type: "bool", title: addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
                input(name: "infoLogging", type: "bool", title: addTitleDiv("Enable descriptionText logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
                // END:  getDefaultMetadataPreferences()
            }
        }
        footer()
    }
}

def refreshDevices(){
    logging("refreshDevices()", 1)
    Integer numDevices = 0
    Integer numDevicesSuccess = 0
    getAllTasmotaDevices().each {
        numDevices += 1
        try{
            //logging("BEFORE Refreshing Device \"${it.label}\" (${it.id})", 1)
            it.refresh()
            logging("AFTER Refreshing Device \"${it.label}\" (${it.id})", 1)
            numDevicesSuccess += 1
        } catch(e) {
            log.warn("Failed to Refresh Device \"${it.label}\" (${it.id})")
        }
    }
    String result = "COMPLETE REFRESH FAILURE!"
    if(numDevicesSuccess == numDevices) {
        result = "All $numDevices Device(s) have been refreshed!"
    } else {
        result = "PARTIAL FAILURE: $numDevicesSuccess of $numDevices Device(s) have been refreshed! (${numDevices - numDevicesSuccess} failed!)"
    }
    updatedAdditional()
    return resultPage("refreshDevices", "Devices Refreshed", result)
}

Map resultPage(){
    logging("resultPage()", 1)
    return resultPage("resultPage", "Result Page", "My little result...")
}

Map resultPage(name, title, result, nextPage = "mainPage", otherReturnPage = null, otherReturnTitle="Return Page"){
    logging("resultPage(name = $name, title = $title, result = $result, nextPage = $nextPage)", 1)

    return dynamicPage(name: name, title: "", nextPage: nextPage) {
        makeAppTitle(btnDone=true) // Also contains the CSS

        section(getElementStyle('header', getMaterialIcon('done') + "Action Completed"), hideable: true, hidden: false){
            paragraph("<div style=\"font-size: 16px;\">${result}</div>")
        }
        if(otherReturnPage != null) {
            section(getElementStyle('header', getMaterialIcon('dns') + "Actions"), hideable: true, hidden: false){ 
                href("$otherReturnPage", title:"$otherReturnTitle", description:"")
            }
        }
    }
}

Map resultPageFailed(name, title, result, nextPage = "mainPage", otherReturnPage = null, otherReturnTitle="Return Page"){
    logging("resultPage(name = $name, title = $title, result = $result, nextPage = $nextPage)", 1)

    return dynamicPage(name: name, title: "", nextPage: nextPage) {
        makeAppTitle(btnDone=true) // Also contains the CSS

        section(getElementStyle('header', getMaterialIcon('warning') + "Action Failed!"), hideable: true, hidden: false){
            paragraph("<div style=\"font-size: 16px;\">${result}</div>")
        }
        if(otherReturnPage != null) {
            section(getElementStyle('header', getMaterialIcon('dns') + "Actions"), hideable: true, hidden: false){ 
                href("$otherReturnPage", title:"$otherReturnTitle", description:"")
            }
        }
    }
}

String getElementStyle(style, String content=""){
    switch (style) {
        case 'header':
            //return '<div style="font-weight: bold; color:#fff;">' + content + '</div>'
            return content
            break
        case 'title':
            return '<h2 style="font-weight: bold; color:#382e2b;">' + content + '</h2>'
            break
        case 'subtitle':
            return '<div style="font-weight: bold; color:#382e2b;">' + content + '</div>'
            break
        case 'line':
            return '<hr style="height: 1px; border: 0px; background-color:#382e2b;"></hr>'
        case 'separator':
            return '\n<hr style="background-color:#1A77C9; height: 1px; border: 0;"></hr>'
            break
    }
}

String getMaterialIcon(String iconName, String extraClass='') {
    // Browse icons here
    // https://material.io/resources/icons/?style=baseline
    // known HE icons (set as class): he-bulb_1, he-settings1, he-file1, he-default_dashboard_icon, he-calendar1
    // he-discovery_1, he-add_2, he-door_closed
    return '<i class="material-icons icon-position ' + extraClass + '">' + iconName + '</i>'
}

Map btnParagraph(buttons, extra="") {
    //getDeviceConfigLink(it.id)
    String content = '<table style="border-spacing: 10px 0px"><tr>'
    buttons.each {
        //content += '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp">'
        content += '<td>'
        
        content += '<a style="color: #000;" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
        
        content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
        
        content += "${it['title']}"
        //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
        content += '</button></a></td>'

//                                '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td> )
    }
    content += '</tr></table>' // + extra
    return paragraph(content) 
}

String getDeviceTableCell(deviceInfoEntry, link=true) {
    def it = deviceInfoEntry
    String content = '<td class="device-config_td ' + "${it['td_class']}" + '">'
        
    if(link == true) {
        content += '<a class="device-config_btn ' + "${it['class']}" + '" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
    }
    
    //content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
    String extraTitle = ""
    if(it['title'] != null && it['title'].indexOf('material-icons') == -1) {
        extraTitle = "title=\"${it['title']}\""
    }
    if(it['red'] == true) {
        
            content += "<div ${extraTitle} style=\"color: red;\" >${it['title']}</div>"
        
    } else {
        content += "<div ${extraTitle} >${it['title']}</div>"
    }
    //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
    //content += '</button></a></td>'
    if(link == true) {
        content += '</a>'
    }
    content += '</td>'

    return content
}

String getDeviceTable(deviceInfo, String extra="") {
    //getDeviceConfigLink(it.id)
    String content = '<table class="device-config_table"><tr>'
    content += '<th style="width: 40px;"><div>Config</div></th>'
    content += '<th style="width: 100px;"><div>Tasmota&nbsp;Config</div></th>'
    content += '<th style="width: 80px;"><div>Uptime</div></th>'
    content += '<th style="width: 80px;"><div>Heartbeat</div></th>'
    content += '<th style="width: 33px;"><div>Wifi</div></th>'
    content += '<th style="width: 100px;"><div>Firmware</div></th>'
    content += '<th style="width: 100px;"><div>Driver</div></th>'
    content += '<th style="width: 60px;"><div>Status</div></th>'
    content += '<th style=""><div>Type</div></th>'
    content += '</tr><tr>'

    // Config Link
    content += getDeviceTableCell([href:deviceInfo['href']['href'], 
        target:'_blank', title:getMaterialIcon('', 'he-settings1 icon-tiny device-config_btn_icon')])

    // Tasmota Web Config Link
    content += getDeviceTableCell([class:'device-config_btn', href:getDeviceTasmotaConfigLink(deviceInfo['ip']['data']), 
        target:'_blank', title:deviceInfo['ip']['data']])

    // Tasmota Device Uptime
    content += getDeviceTableCell([title:deviceInfo['uptime']['data'], red:deviceInfo['uptime']['red']], false)

    // Tasmota Heartbeat
    content += getDeviceTableCell([title:deviceInfo['lastActivity']['data'], red:deviceInfo['lastActivity']['red']], false)

    // Wifi Signal Quality
    content += getDeviceTableCell([title:deviceInfo['wifi']['data'], red:deviceInfo['wifi']['red']], false)

    // Firmware Version
    content += getDeviceTableCell([title:deviceInfo['firmware']['data'], red:deviceInfo['firmware']['red']], false)

    // Driver Version
    content += getDeviceTableCell([title:deviceInfo['driverVersion']['data'], red:deviceInfo['driverVersion']['red']], false)

    // Status
    content += getDeviceTableCell([title:deviceInfo['deviceStatus']['data'], red:deviceInfo['deviceStatus']['red']], false)

    // Driver Type
    content += getDeviceTableCell([title:deviceInfo['driverName']['data'], red:deviceInfo['driverName']['red']], false)

    content += '</tr>'
    content += '<tr>'
    content += "<td class=\"childlist-cell\" colspan=\"9\" >${getChildDeviceHREFList(deviceInfo['childDevices'])}</ td>"
    content += '</tr></table>' // + extra
    paragraph(content) 
}

String getChildDeviceHREFList(childDevices) {
    String r = ''
    String prefix = ''
    // id:1091, label:Parent Testing (192.168.10.146) (POWER1), driver:v1.0.0222Tb
    childDevices.each {
        r += prefix
        r += "<a href=\"${getDeviceConfigLink(it.id)}\" target=\"_blank\">"
        r += "${getMaterialIcon('', 'he-settings1 icon-tiny-compact')}"
        r += "${it.label.replace(' ', '&nbsp;')}"
        r += "</a>"
        prefix = ',&nbsp; '
    }
    return r
}

def configureTasmotaDevice(params) {
    if (params?.did || params?.params?.did) {
        if (params.did) {
            //getTasmotaDevice
            //
            state.currentDeviceId = params.did
            state.currentDisplayName = getTasmotaDevice(params.did).label
            logging("params.did: $params.did, label: ${getTasmotaDevice(params.did)?.label}", 1)
        } else {
            logging("params.params.did: $params.params.did", 1)
            state.currentDeviceId = params.params.did
            state.currentDisplayName = getTasmotaDevice(params.params.did)?.label
        }
    }
    def device = getTasmotaDevice(state.currentDeviceId)
    state.currentDisplayName = device.label
    logging("state.currentDeviceId: ${state.currentDeviceId}, label: ${device.label}", 1)
    //if (device != null) device.configure()
    dynamicPage(name: "configureTasmotaDevice", title: "Configure Tasmota-based Devices created with this app", nextPage: "mainPage") {
            section {
                app.updateSetting("${state.currentDeviceId}_label", device.label)
                input "${state.currentDeviceId}_label", "text", title:"Device Name" + getCSSStyles(), description: "", required: false
                href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it"
            }
            section {
                href "deleteDevice", title:"Delete \"$device.label\"", description: ""
            }
    }
    // device = getChildDevice(did)
    //dynamicPage(name: "configureTasmotaDevice", nextPage: null, uninstall: false, install: false) {
    //    paragraph('Device: <a href="' + "${getDeviceConfigLink(device.deviceId)}" + '" target="deviceConfig">' + device.label + '</a>' + 
    //                          ' - <a href="' + "${getDeviceLogLink(device.deviceId)}" + '" target="deviceConfig">Device&nbsp;Log</a>')
    //}
}

//
def deviceDiscoveryTEMP() {
   dynamicPage(name: "deviceDiscoveryTEMP", title: "Discover Tasmota-based Devices", nextPage: "mainPage") {
		section {
			paragraph "NOT FUNCTIONAL: This process will automatically discover your device, this may take a few minutes. Please be patient. Tasmota Device Manager then communicates with the device to obtain additional information from it. Make sure the device is on and connected to your WiFi network."
            /*input "deviceType", "enum", title:"Device Type", description: "", required: true, options: 
                // BEGIN:makeTasmotaConnectDriverListV1()
                ["Tasmota - Universal Parent",
                ]
                // END:  makeTasmotaConnectDriverListV1()
            input "ipAddress", "text", title:"IP Address", description: "", required: true */
		}
    }
}


def manuallyAdd() {
    dynamicPage(name: "manuallyAdd", title: "", nextPage: "manuallyAddConfirm", previousPage: "mainPage") {
        makeAppTitle() // Also contains the CSS
		section(getElementStyle('header', getMaterialIcon('', 'he-add_1') + "Manually Install a Tasmota-based Device"), hideable: true, hidden: false) {
            paragraph("This process will install a Tasmota-based Device with the entered IP address. Tasmota Device Manager then communicates with the device to obtain additional information from it. Make sure the device is on and connected to your wifi network.")
            
            input("deviceType", "enum", title:"Device Type", description: "", required: true, submitOnChange: false, options: 
                // BEGIN:makeTasmotaConnectDriverListV1()
                ["Tasmota - Universal Parent",
                ]
                // END:  makeTasmotaConnectDriverListV1()
            )
            input(name: "deviceConfig", type: "enum", title: "Device Configuration", 
                description: "Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting.", 
                options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", required: false)
            input("ipAddress", "text", title:"IP Address", description: "", required: false, submitOnChange: false)
            input("deviceLabel", "text", title:"Device Label", description: "", required: true, defaultValue: (deviceType ? deviceType : "Tasmota - Universal Parent") + " (%device_ip%)")
            paragraph("'%device_ip%' = insert device IP here")
            input("passwordDevice", "password", title:"Tasmota Device Password", description: "Only needed if set in Tasmota.", defaultValue: passwordDefault, submitOnChange: true, displayDuringSetup: true)            
            paragraph("Only needed if set in Tasmota.")
            paragraph("To exit without installing a device, complete the required fields but DON'T enter a correct IP, then click \"Next\".")
            // Have to find a way to leave this page without filling in the required fields...
            //href("mainPage", title:getMaterialIcon('cancel') + "Cancel", description: "")
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
        logging("Creating Tasmota-based Wifi Device with dni: ${convertIPtoHex(ipAddress)}", 1)
        if(passwordDevice == null || passwordDevice == "") {
           passwordDevice = "[installed]"
        }
        def child = addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Universal Parent", "${convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceLabel ? deviceLabel : "Tasmota - Universal Parent (%device_ip%)").replace("%device_ip%", "${ipAddress}"),
           "data": [
                "ip": ipAddress,
                "port": "80",
                "password": encrypt(passwordDevice),
                "deviceConfig": deviceConfig
           ]
        ])

        // We do this to get everything setup correctly
        //child.updateSetting("deviceConfig", [type: "enum", value:deviceConfig])
        // After adding preferences, Configure() needs to run to apply them, but we have to wait a little bit.
        child.configureDelayed()
        // This will refresh and detect child devices based on the above config
        child.refresh()
        def tmpIpAddress = ipAddress
        // Restore for next time
        app.updateSetting("ipAddress", [type: "string", value:getFirstTwoIPBytes(ipAddress)])
        app.updateSetting("deviceLabel", "")
        app.updateSetting("passwordDevice", "")
        //app.updateSetting("deviceConfig", [type: "enum", value:"01generic-device"])
        
        resultPage("manuallyAddConfirm", "Manual Installation Summary", 
                   "The device with IP \"$tmpIpAddress\" has been installed. It may take up to a minute or so before all child devices have been created if many are needed. Be patient. If all child devices are not created as expected, press Configure and Refresh in the Universal Parent and wait again. Don't click multiple times, it takes time for the device to reconfigure itself. Click \"Done\" to Continue.", 
                   nextPage="mainPage")
    } else {
        resultPageFailed("manuallyAddConfirm", "Manual Installation Summary", 
                   "The entered ip address ($ipAddress) is not valid. Please try again. To add another device click \"Add Another Device\". Click \"Done\" to Continue.", 
                   nextPage="mainPage", otherReturnPage="manuallyAdd", otherReturnPageTitle="Add Another Device")
    }
}

def deleteDevice(){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        resultPage("deleteDevice", "Deletion Summary", 
                   "The device with DNI $state.currentDeviceId has been deleted. Click \"Done\" to Continue.", 
                   nextPage="mainPage")
	} catch (e) {
        resultPage("deleteDevice", "Deletion Summary", 
                   "Error: ${(e as String).split(":")[1]}.", 
                   nextPage="mainPage")    
    }
}

def changeName(){
    def thisDevice = getChildDevice(state.currentDeviceId)
    thisDevice.label = settings["${state.currentDeviceId}_label"]

    resultPage("changeName", "Change Name Summary", 
                   "The device has been renamed to \"$thisDevice.label\". Click \"Done\" to Continue.", 
                   nextPage="mainPage")
}

def getDeviceDriverName(device) {
    //getTasmotaDevice device.deviceNetworkId
    String driverName = 'Unknown'
    try {
        driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
    } catch(e) {
        logging("Failed getting DriverName ($e), trying again...", 1)
        device = getTasmotaDevice(device.deviceNetworkId)
        try{
            driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
        } catch(e1) {
            driverName = "Unknown"
        }
    }
    if (driverName.startsWith("Tasmota - ")) driverName = driverName.substring(10)
    logging("Found Driver Name: '$driverName'", 0)
    return driverName
}

def getDeviceConfigLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/device/edit/${deviceId}")
}

def getDeviceLogLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/logs#dev${deviceId}")
}

def getDeviceTasmotaConfigLink(deviceIP) {
    ///device/edit/
    return("http://${deviceIP}/")
}

def installCheck() {
	state.appInstalled = app.getInstallationState()
	
	if (state.appInstalled != 'COMPLETE') {
		section{paragraph "Please hit 'Done' to finish installing '${app.label}'"}
  	}
  	else {
    	logging("Parent Installed OK", 1)
  	}
}

def footer() {
    section() {
        paragraph(getElementStyle('line'))
        paragraph('<div style="color:#382e2b; text-align:center">' + app.label + " ${getAppVersion()} " + '- Copyright&nbsp;2020&nbsp;Markus&nbsp;Liljergren - <a href="https://github.com/markus-li/Hubitat/tree/release" target="_blank">GitHub repo</a></div>')
    }
}

/*
	installedAdditional

	Purpose: initialize the app
	Note: if present, called from installed() in all drivers/apps
    installed() does NOT call initalize() by default, so if needed, call it here.
*/
def installedAdditional() {
    logging("installedAdditional()", 1)
	initialize()
}

def uninstalled() {
    logging("uninstalled()", 1)
    unsubscribe()
    unschedule()
}

def updatedAdditional() {
    logging("updatedAdditional()", 1)
	unsubscribe()
    unschedule()
    def devices = getAllTasmotaDevices()
    
    //app.removeSetting("devicesAvailable")
    //app.updateSetting("devicesAvailable", devices)
    //devicesSelected = devices
    state.devices = devices.sort({ a, b -> a["label"] <=> b["label"] })
    def devicesSelectable = []
    state.devices.each { devicesSelectable << ["${it.deviceNetworkId}":"${it.label}"] }

    logging("devicesSelectable: ${devicesSelectable}", 1)
    state.devicesSelectable = devicesSelectable
	initialize()
}

def runDeviceCommand(device, cmd, args=[]) {
    def jsonSlurper = new JsonSlurper()
    logging("runDeviceCommand(device=${device.deviceId.toString()}, cmd=${cmd}, args=${args})", 0)
    // Since Refresh is defined as a command in all my drivers, 
    // it can be used to forward the call to deviceCommand.
    //device.parse(JsonOutput.toJson([cmd: cmd, args: args]), 1)
    
    device.refresh(JsonOutput.toJson([cmd: cmd, args: args]))
    //device.deviceCommand(JsonOutput.toJson([cmd: cmd, args: args]))
    r = null
    r = jsonSlurper.parseText(device.getDataValue('appReturn'))

    device.updateDataValue('appReturn', null)
    return r
}

// 
def getAllTasmotaDevices() {
    def toRemove = []
    def devicesFiltered = []
    devicesSelected.eachWithIndex { it, i ->
        def namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            //namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.deviceId.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.deviceId.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota' && it.parentAppId != app.id) {
            devicesFiltered << it
        }
    }
    def childDevices = getChildDevices()
    logging("getChildDevices: ${getChildDevices()}", 1)
    childDevices.eachWithIndex { it, i ->
        def namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            //namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.id.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.id.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota') {
            devicesFiltered << it
        }
    }
    //devicesFiltered << childDevices
    return devicesFiltered.sort({ a, b -> a.label <=> b.label })
}

def getAllTasmotaDeviceIPs() {
    def deviceIPs = []
    getAllTasmotaDevices().each { rawDev ->
        def cDev = getTasmotaDevice(rawDev.deviceNetworkId)
        if(cDev != null) {
            deviceIPs << rawDev['data']['ip']
        }
    }
    return deviceIPs
}

def getTasmotaDevice(deviceNetworkId) {
    def r = getChildDevice(deviceNetworkId)
    if(r == null) {
        devicesSelected.each {
            //logging("'" + it.deviceNetworkId + "' =? '" + deviceNetworkId + "'", 1)
            if(it.deviceNetworkId == deviceNetworkId) {
                //logging("'" + it.deviceNetworkId + "' == '" + deviceNetworkId + "' (dev: ${it})", 1)
                r = it
            }
        }
    }
    return r
}

/*
	initializeAdditional

	Purpose: initialize the app
	Note: if present, called from initialize() in all drivers/apps
    Called when Done is pressed in the App
*/
def initializeAdditional() {
    logging("initializeAdditional()", 1)
    // Do NOT start SSDP discovery here! That should ONLY be done when searching for devices
    deviceDiscoveryCancel()
}

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
 * APP DEFAULT METHODS (helpers-app-default)
 *
 * Methods used in all APPS
 */

def installed() {
	logging("installed()", 100)
	try {
        // In case we have some more to run specific to this App
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/**
 * --END-- APP DEFAULT METHODS (helpers-app-default)
 */

/**
 * APP CSS METHODS (helpers-app-css)
 *
 * Helper functions for App CSS
 */

String getCSSStyles(btnDone=false) {
    String css = '''<style>
/* General App Styles */
#version {
    font-size: 50%;
}
.btn {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
/*#formApp h3:first-child {
    display: none;
}*/
.mdl-card, .mdl-switch__label, .mdl-textfield  {
    font-size: 14px;
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
.btn-sub {
    padding: 2px 30px 2px 2px;
}
div.mdl-button--raised {
    font-weight: bold; 
    color:#fff; 
    background-color:#81bc00; 
    border: 1px solid;
}
div.mdl-button--raised:hover, div.mdl-button--raised:focus {
    color: #212121;
    background-color:#91d844; 
}
.btn-sub.hrefElem:before {
    top: calc(50% - 0.75625rem);
}
div.mdl-button--raised h4.pre {
    font-weight: bold; 
    color: #fff;
    vertical-align: middle;
}

/* Icon Styles */
.icon-position {
    margin-right: 12px;
    vertical-align: middle;
}
.icon-tiny {
    margin-right: 8px;
    font-size: 14px;
}
.icon-small {
    margin-right: 8px;
    font-size: 18px;
}
.icon-large {
    margin-right: 12px;
    font-size: 32px;
}

/* Configure Devices List Styles */
#collapse4 .hrefElem::before {
    filter: invert(100%);
}
#collapse4 .hrefElem:hover::before, #collapse4 .hrefElem:focus::before {
    filter: invert(0%);
}
#collapse4 table .hrefElem::before {
    filter: invert(0%);
}
#collapse4 .btn-block {
    color: #f5f5f5;
    background-color: #382e2b;
    
    font-size: 14px;
    /*font-size: calc(100% + 0.08vw);*/
    max-width: inherit;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
#collapse4 .btn-block span {
    white-space: nowrap !important;
    max-width: inherit;
}
#collapse4 .btn-block:hover, #collapse4 .btn-block:focus {
    color: #212121;
    background-color: #e0e0e0;
}
#collapse4 div.mdl-textfield {
    margin: 0px;
}
.device-config_table {
    border-spacing: 2px 0px;
    table-layout:fixed;
    width: 100%
}
.device-config_td {
    text-align: center;
    vertical-align: middle;
}
.device-config_btn {
    width: 100%;
}
.device-config_table th, .device-config_table td {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
    font-size: 13px;
    vertical-align: middle;
    width: 100%;
}
.device-config_table th div, .device-config_td div, .device-config_table td a {
    text-align: center;
    white-space: nowrap !important;
    max-width: inherit;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
    display: block;
}
.device-config_btn_icon {
    text-align: center;
    width: 100%;
}

/* Action Buttons */
#collapse4 [name*="refreshDevices"] {
    float: right !important;
}
#collapse4 [name*="resultPage"] {
    float: left !important;
    visibility: hidden;
}
#collapse4 [name*="refreshDevices"], #collapse4 [name*="resultPage"] {
    color: #000;
    width: 170px !important;
    min-width: 170px;
    background: rgba(158,158,158,.2);
    border: none;
    margin-left: 0px;
    text-align: center !important;
    vertical-align: middle;
    line-height: 36px;
    padding-right: unset;
    padding: 0px 16px;
    display:inline;
}
#collapse4 .mdl-cell--12-col:nth-of-type(2), #collapse4 .mdl-cell--12-col:nth-of-type(3) {
    width: 50% !important;
    display:inline !important;
}
#collapse4 [name*="refreshDevices"] span, #collapse4 [name*="resultPage"] span {
    font-weight: 500;
    text-align: center !important;
    white-space: nowrap !important;
}
#collapse4 [name*="refreshDevices"]::before, #collapse4 [name*="resultPage"]::before {
    content: "";
}

.mdl-cell.mdl-cell--12-col.mdl-textfield.mdl-js-textfield {
    width: 100%;
}

td.childlist-cell {
    border-top: 2px dotted #000000;
    padding-left: 8px;
    padding-right: 8px;
}
td.childlist-cell a {
    display: inline;
}
.icon-tiny-compact {
    margin-right: 4px;
    font-size: 14px;
}
@media (min-width: 840px)
.mdl-cell--8-col, .mdl-cell--8-col-desktop.mdl-cell--8-col-desktop {
    width: calc(76.6666666667% - 16px);
}'''
if(btnDone == true) {
    css += '''
button#btnNext {
    font-size: 0;
    display: flex;
}
button#btnNext::after {
    content: "Done";
    font-size: 14px;
}'''
}
css += '</style>'

return css
}

 /**
 * --END-- APP CSS METHODS (helpers-app-css)
 */

/**
 * APP TASMOTA DEVICE DISCOVERY METHODS (helpers-app-tasmota-device-discovery)
 *
 * Methods used in all APPS
 */
def discoveryPage() {
   return deviceDiscovery()
}

def deviceDiscoveryCancel() {
    logging("deviceDiscoveryCancel()", 100)
    unsubscribe()
    unschedule()
    state.deviceRefreshCount = 0
    state.devices = state.devicesCached ?: [:]
    state.devices.each {
        it.value["installed"] = null
    }
    state.devicesCached.each {
        it.value["installed"] = null
    }
}

def deviceDiscovery(params=[:]) {
    Integer deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as Integer
    state.deviceRefreshCount = deviceRefreshCount + 1

    if(deviceRefreshCount == 0) {
	    ssdpSubscribe()
        runEvery1Minute("ssdpDiscover")
        // This is our failsafe since we REALLY don't want this to be left on...
        runIn(1800, "deviceDiscoveryCancel")
        verifyDevices()
        state.deviceRefreshStart = now()
    }

	//def devices = devicesDiscovered()
    
	def refreshInterval = 10
    
	//def options = devices ?: [:]
	//def numFound = options.size() ?: 0
    
	//if ((numFound == 0 && state.deviceRefreshCount > 25) || params.reset == "true") {
    //	log.trace "Cleaning old device memory"
    //	state.devices = [:]
    //    state.deviceRefreshCount = 0
    //    app.updateSetting("selectedDevice", "")
    //}

	//Tasmota-based Device discovery request every 15 //25 seconds
	//if((deviceRefreshCount % 5) == 0) {
	//	discoverDevices()
	//}

	//XML request and Install check every 30 seconds
	if((deviceRefreshCount % 3) == 0) {
		verifyDevices()
	}
    
	return dynamicPage(name:"deviceDiscovery", title:"", nextPage:"deviceDiscoveryPage2", refreshInterval:refreshInterval) {
    //return dynamicPage(name:"deviceDiscovery", title:"", nextPage:"deviceDiscoveryPage2") {
        makeAppTitle() // Also contains the CSS
		section(getElementStyle('header', getMaterialIcon('', 'he-discovery_1') + "Discover a Tasmota Device"), hideable: true, hidden: false) {
            paragraph("Please wait while we discover your Tasmota-based Devices using SSDP. Discovery can take five minutes or more, so sit back and relax!")
            
			paragraph("<span style=\"font-weight: 500\">Time elapsed since starting SSDP Discovery:</ span> ${new BigDecimal((now() - Long.valueOf(state.deviceRefreshStart))/1000).setScale(0, BigDecimal.ROUND_HALF_UP)} seconds")
            
            paragraph("Please note that Hue Bridge Emulation (Configuration->Configure Other->Emulation) must be turned on in Tasmota for discovery to work (this is the default with the Hubitat version of Tasmota).")

            paragraph("Installed devices are not displayed (if Tasmota Device Manager has access to them). Previously discovered devices will show quickly, devices never seen by Tasmota Device Manager before may take time to discover.")
            getAvailableDevicesList()
            paragraph("Once the device you want to install is available in the above list, click \"Next\" to go to the Installation Page.")
            paragraph("If the device you expect to find is not found within 10 minutes, use the Manual Install method instead.")
		}
        section(getElementStyle('header', getMaterialIcon('dns') + "Actions"), hideable: true, hidden: false){ 
            href("deviceDiscoveryReset", title:"Reset list of Discovered Devices", description:"")
            href("mainPage", title:"Return to the Main Page", description:"")
			//href "deviceDiscovery", title:"Reset list of discovered devices", description:"", params: ["reset": "true"]
            
		}
	}
}

String getAvailableDevicesList() {
    
    
    def vdevices = getVerifiedDevices()
	def options = [:]
	vdevices.each {
        //log.debug("Device: $it.value")
		def value = "${it.value.name}"
		def key = "${it.value.networkAddress}"
		options["${key}"] = value
	}
    String title = "Available Devices (${options.size() ?: 0} found)"
    //List optionsList = []
    //options.each{optionsList << [key: it.key, value: it.value]}
    String deviceList = ""
    options.sort({ a, b -> a.key <=> b.key }).each{
        deviceList += "<a href=\"http://${convertHexToIP(it.key)}\" target=\"_blank\" >" + it.value + "</a><br>"
    }

    String header = """<style>
        div.btn-listing,
        div.btn-listing:hover,
        div.btn-listing:active,
        div.btn-listing:focus,
        div.btn-listing:focus:not(:active),
        div.btn-listing:active:focus,
        div.btn-listing:active:hover {
            background: rgba(158,158,158,.2);
            background-color: rgba(158,158,158,.2);
            border-color: rgb(227, 227, 227);
            cursor: unset;
            touch-action: none;
            border-left: none;
            text-align:left;
            color:#212121;
            border-left-color: rgb(227, 227, 227);
            font-weight: 400;
            box-sizing: border-box;
            user-select: none;
            border-left-style: outset;
            border-left-width: 2px;
        }
        </style><div class="btn-listing btn btn-default btn-lg btn-block device-btn-filled mdl-shadow--2dp">
            <span style="">""" + title + """</span><br>
            <span id="devicesSelecteddevlist" class="device-text" style="text-align: left;">"""
    String footer = "</span></div>"
    //log.debug("devices2: ${devices} , options: ${options}")
    return paragraph(header + deviceList + footer, submitOnChange: false)
}

def deviceDiscoveryPage2() {
    Integer deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as Integer

    def devices = devicesDiscovered()
    
	def options = devices ?: [:]
	def numFound = options.size() ?: 0
    
	//return dynamicPage(name:"deviceDiscovery", title:"", nextPage:"discoveredAddConfirm", refreshInterval:refreshInterval) {
    return dynamicPage(name:"deviceDiscoveryPage2", title:"", nextPage:"discoveredAddConfirm") {
        makeAppTitle() // Also contains the CSS
		section(getElementStyle('header', getMaterialIcon('', 'he-discovery_1') + "Discover a Tasmota Device"), hideable: true, hidden: false) {
            paragraph("Installed devices are not displayed (if Tasmota Device Manager has access to them). Previously discovered devices will show quickly, devices never seen by Tasmota Device Manager before may take time to discover.")
            input("deviceType", "enum", title:"Device Type", description: "", required: true, submitOnChange: true, options: 
                // BEGIN:makeTasmotaConnectDriverListV1()
                ["Tasmota - Universal Parent",
                ]
                // END:  makeTasmotaConnectDriverListV1()
            )
            input(name: "deviceConfig", type: "enum", title: "Device Configuration", 
                description: "Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting.", 
                options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", required: false, submitOnChange: true)
            input("selectedDiscoveredDevice", "enum", required:false, title:"Select a Tasmota Device (${numFound} found)", multiple:false, options:options, submitOnChange: true)
            //input("ipAddress", "text", title:"IP Address", description: "", required: true, submitOnChange: false)
            input("deviceLabel", "text", title:"Device Label", description: "", required: true, defaultValue: (deviceType ? deviceType : "Tasmota - Universal Parent") + " (%device_ip%)", submitOnChange: true)
            paragraph("'%device_ip%' = insert device IP here")
            paragraph("Suffixes \" (Parent)\" and \" Parent\" at the end of the Device Label will be removed from the Child Device Label.")
            input("passwordDevice", "password", title:"Tasmota Device Password", description: "Only needed if set in Tasmota.", defaultValue: passwordDefault, submitOnChange: true, displayDuringSetup: true)            
            paragraph("Only needed if set in Tasmota.")
            paragraph("<br/>To exit without installing a device, complete the required fields and DON'T select a device, then click \"Next\".")
		}
	}
}

def discoveredAddConfirm() {
    def devices = getDevices()
    def selectedDevice = devices.find { it.value.mac == selectedDiscoveredDevice }
    def ipAddress = convertHexToIP(selectedDevice?.value?.networkAddress)
    //log.debug("Discovered IP: $ipAddress")
    if ( ipAddress != null && ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
        logging("Creating Tasmota-based Wifi Device with dni: ${convertIPtoHex(ipAddress)}", 1)
        selectedDevice.value["installed"] = true

        if(passwordDevice == null || passwordDevice == "") {
           passwordDevice = "[installed]"
        }
        def child = addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Universal Parent", "${convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceLabel ? deviceLabel : "Tasmota - Universal Parent (%device_ip%)").replace("%device_ip%", "${ipAddress}"),
           "data": [
                "ip": ipAddress,
                "port": "80",
                "password": encrypt(passwordDevice),
                "deviceConfig": deviceConfig
           ]
        ])

        // We do this to get everything setup correctly
        //child.updateSetting("deviceConfig", [type: "enum", value:deviceConfig])
        // After adding preferences, Configure() needs to run to apply them, but we have to wait a little bit.
        child.configureDelayed()
        // This will refresh and detect child devices based on the above config
        child.refresh()

        // Restore for next time
        app.updateSetting("ipAddress", [type: "string", value:getFirstTwoIPBytes(ipAddress)])
        //app.updateSetting("deviceLabel", "")
        app.updateSetting("passwordDevice", "")
        //app.updateSetting("deviceConfig", [type: "enum", value:"01generic-device"])
        verifyDevices()

        resultPage("discoveredAddConfirm", "Discovered Tasmota-based Device", 
                   'The device with ip ' + ipAddress + ' has been added. To add another device click "Add Next Device". Click "Done" to return to the Main Page.<br/>It may take up to a minute or so before all child devices have been created if many are needed. Be patient. If all child devices are not created as expected, press Configure and Refresh in the Universal Parent and wait again. Don\'t click multiple times, it takes time for the device to reconfigure itself.', 
                   nextPage="mainPage", otherReturnPage="deviceDiscovery", otherReturnPageTitle="Add Next Device")
    } else {
        resultPageFailed("discoveredAddConfirm", "Discovered Tasmota-based Device", "No device was selected. To add another device click \"Add Another Device\". Click \"Done\" to return to the Main page.", 
                   nextPage="mainPage", otherReturnPage="deviceDiscovery", otherReturnPageTitle="Add Another Device")
    }
}

Map deviceDiscoveryReset() {
    logging("deviceDiscoveryReset()", 1)
    resetDeviceDiscovery()
    return resultPage("deviceDiscoveryReset", "Device Discovery Reset", "Device Discovery Reset Done!", nextPage="deviceDiscovery")
}

void resetDeviceDiscovery(){
    logging("Cleaning old device from the list...", 100)
    //log.debug("resetDeviceDiscovery()")
    state.devices = state.devicesCached ?: [:]
    //state.devices = [:]
    state.devices.each {
        it.value["verified"] = null
    }
    state.devices.each {
        it.value["installed"] = null
    }
    state.deviceRefreshCount = 0
    verifyDevices()
    app.updateSetting("selectedDiscoveredDevice", "")
}

Map devicesDiscovered() {
	def vdevices = getVerifiedDevices()
	def map = [:]
    
	vdevices.sort({ a, b -> a.value.networkAddress <=> b.value.networkAddress }).each {
		def value = "${it.value.name}"
		def key = "${it.value.mac}"
		map["${key}"] = value
	}
	return map
}

def getVerifiedDevices() {
    //return getDevices()
	return getDevices().findAll{ it?.value?.verified == true && it?.value?.installed == false }
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
    //log.debug("ssdpHandler parsedEvent: $parsedEvent")

    def devices = getDevices()
    def devicesCache = getDevicesCache()
    
    String ssdpUSN = parsedEvent.ssdpUSN.toString()
    
    if (devices."${ssdpUSN}" == null) {
        devices << ["${ssdpUSN}": parsedEvent]
    }
    if (devicesCache."${ssdpUSN}" == null) {
        devicesCache << ["${ssdpUSN}": parsedEvent]
    }
}

void verifyDevices() {
    // First check if the devices actually are Tasmota devices
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        try{
            def ip = convertHexToIP(it.value.networkAddress)
            def port = convertHexToInt(it.value.deviceAddress)
            String host = "${ip}:${port}"
            sendHubCommand(new hubitat.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", hubitat.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
        } catch(e) {
            // Do nothing
            //log.debug("Device incorrect=$it")
        }
    }
    // Then check if we have already installed them
    devices = getDevices().findAll { it?.value?.verified == true && it?.value?.installed == null }
    //log.trace "devices = $devices"
    if(devices != [:]) {
        def installedDeviceIPs = getAllTasmotaDeviceIPs()
        //log.trace "installedDeviceIPs = $installedDeviceIPs"
        devices.each {
            def ip = convertHexToIP(it.value.networkAddress)
            if(ip in installedDeviceIPs) {
                //log.warn "Already installed: $ip"
                it.value << [installed:true]
            } else {
                //log.warn "NOT installed: $ip"
                it.value << [installed:false]
            }
        }
    }
}

def getDevices() {
    return state.devices = state.devices ?: [:]
}

def getDevicesCache() {
    return state.devicesCached = state.devicesCached ?: [:]
}

void deviceDescriptionHandler(hubitat.device.HubResponse hubResponse) {
	//log.trace "description.xml response (application/xml)"
	def body = hubResponse.xml
    //log.debug body?.device?.manufacturer?.text()
	if (body?.device?.manufacturer?.text().startsWith("iTead")) {
		def devices = getDevices()
		def device = devices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (device) {
			device.value << [name:body?.device?.friendlyName?.text() + " (" + convertHexToIP(hubResponse.ip) + ")", serialNumber:body?.device?.serialNumber?.text(), verified: true]
            //log.debug("deviceDescriptionHandler: $device.value")
		} else {
			log.error "/description.xml returned a device that didn't exist"
		}
	}
}

private String convertHexToIP(hex) {
    if(hex != null) {
	    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
    } else {
        return null
    }
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

 /**
 * --END-- APP TASMOTA DEVICE DISCOVERY METHODS (helpers-app-tasmota-device-discovery)
 */

/**
 * TASMOTA METHODS (helpers-tasmota)
 *
 * Helper functions included in all Tasmota drivers
 */

// Calls installed() -> installedPreConfigure()
void installedPreConfigure() {
    // This is run FIRST in installed()
    if(isDriver()) {
        // This is only run ONCE after install
        
        logging("Inside installedPreConfigure()", 1)
        logging("Password: ${decrypt(getDataValue('password'))}", 1)
        String pass = decrypt(getDataValue('password'))
        if(pass != null && pass != "" && pass != "[installed]") {
            device.updateSetting("password", [value: pass, type: "password"])
        }
        device.updateSetting("deviceConfig", [type: "enum", value:getDataValue('deviceConfig')])
    }
}

// Call order: installed() -> configure() -> updated() 
void updated() {
    logging("updated()", 10)
    if(isDriver()) {
        logging("before updateNeededSettings()", 10)
        updateNeededSettings()
        logging("after updateNeededSettings()", 10)
        //sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
        //sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: false)
    }
    try {
        // Also run initialize(), if it exists...
        initialize()
        updatedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

void refreshChildren() {
    logging("refreshChildren()", 1)
    getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
}

void refreshChildrenAgain() {
    logging("refreshChildrenAgain()", 1)
    refreshChildren()
}

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
void refresh() {
	logging("refresh()", 100)
    def metaConfig = null
    if(isDriver()) {
        // Clear all old state variables, but ONLY in a driver!
        state.clear()

        // Retrieve full status from Tasmota
        getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
        getDriverVersion()

        updateDataValue('namespace', getDeviceInfoByName('namespace'))

        //logging("this.binding.variables = ${this.binding.variables}", 1)
        //logging("settings = ${settings}", 1)
        //logging("getDefinitionData() = ${getDefinitionData()}", 1)
        //logging("getPreferences() = ${getPreferences()}", 1)
        //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
        //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
        
        /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
        metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
        metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
        metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/

        // This should be the first place we access metaConfig here, so clear and reset...
        metaConfig = clearThingsToHide()
        metaConfig = setCommandsToHide([], metaConfig=metaConfig)
        metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
            'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
        
        metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
        //metaConfig = setDatasToHide(['preferences', 'namespace', 'appReturn', 'metaConfig'], metaConfig=metaConfig)
        metaConfig = setDatasToHide(['namespace', 'appReturn', 'password'], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    }
    try {
        // In case we have some more to run specific to this driver
        refreshAdditional(metaConfig)
    } catch (MissingMethodException e1) {
        // ignore
        try {
            // In case we have some more to run specific to this driver
            refreshAdditional()
        } catch (MissingMethodException e2) {
            // ignore
        }
    }
}

void reboot() {
	logging("reboot()", 10)
    getAction(getCommandString("Restart", "1"))
}

/*
    // Stress-testing runInstallCommands() 
    installCommands = []
    installCommands.add(["rule1", 'ON Var1#Value DO Var4 0; ENDON'])
    installCommands.add(["rule2", 'ON Var2#Value DO Var4 0; ENDON'])
    installCommands.add(["rule3", 'ON Var3#Value DO Var4 0; ENDON'])
    installCommands.add(["var1", "0"])
    installCommands.add(["var2", "0"])
    installCommands.add(["var3", "0"])
    (1..8).each {
        installCommands.add(["rule1", "+ ON Var1#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule2", "+ ON Var2#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule3", "+ ON Var3#Value DO Var4 $it; ENDON"])
        installCommands.add(["add1", "1"])
        installCommands.add(["add2", "1"])
        installCommands.add(["add3", "1"])
    }
    installCommands.add(["rule1", '0'])
    installCommands.add(["rule2", '0'])
    installCommands.add(["rule3", '0'])
    logging("refreshAdditional installCommands=$installCommands", 1)
    runInstallCommands(installCommands)
*/
void runInstallCommands(installCommands) {
    // Runs install commands as defined in helpers-device-configurations
    // Called from updateNeededSettings() in parent drivers
    logging("runInstallCommands(installCommands=$installCommands)", 1)
    List backlogs = []
    List rule1 = []
    List rule2 = []
    List rule3 = []
    installCommands.each {cmd->
        if(cmd[0].toLowerCase() == "rule1") {
            rule1.add([command: cmd[0], value:cmd[1]])
        } else if(cmd[0].toLowerCase() == "rule2") {
            rule2.add([command: cmd[0], value:cmd[1]])
        } else if(cmd[0].toLowerCase() == "rule3") {
            rule3.add([command: cmd[0], value:cmd[1]])
        } else {
            backlogs.add([command: cmd[0], value:cmd[1]])
        }
    }

    // Backlog inter-command delay in milliseconds
    //getAction(getCommandString("SetOption34", "20"))
    pauseExecution(100)
    // Maximum 30 commands per backlog call
    while(backlogs.size() > 0) {
        getAction(getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        // If we run this too fast Tasmota can't keep up, 1000ms is enough when 20ms between commands...
        if(backlogs.size() > 0) pauseExecution(1000)
        // REALLY don't use pauseExecution often... NOT good for performance...
    }

    [rule1, rule2, rule3].each {
        //logging("rule: $it", 1)
        it.each {rule->
            // Rules can't run in backlog!
            getAction(getCommandString(rule["command"], rule["value"]))
            //logging("cmd=${rule["command"]}, value=${rule["value"]}", 1)
            pauseExecution(100)
            // REALLY don't use pauseExecution often... NOT good for performance...
        }
    }
    //getAction(getCommandString("SetOption34", "200"))
}

void updatePresence(String presence) {
    // presence - ENUM ["present", "not present"]
    logging("updatePresence(presence=$presence)", 1)
    Integer timeout = getTelePeriodValue()
    timeout += (timeout * 1.1 > 120 ? Math.round(timeout * 1.1) : 120) + 60
    String descriptionText = "No update received from the Tasmota device for ${timeout} seconds..."
    if(presence == "present") {    
        descriptionText = "Device is available"
        //log.warn "Setting as present with timeout: $timeout"
        runIn(timeout, "updatePresence", [data: "not present"])
    } else {
        log.warn "Presence time-out reached, setting device as 'not present'!"
    }
    sendEvent(name: "presence", value: presence, isStateChange: false, descriptionText: descriptionText)
}

Map parseDescriptionAsMap(description) {
    // Used by parse(description) to get descMap
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) { 
            map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        } else {
            map += [(nameAndValue[0].trim()):""]
        }
	}
}

private getAction(uri, callback="parse") { 
    logging("Using getAction for '${uri}'...", 0)
    httpGetAction(uri, callback=callback)
}

def parse(asyncResponse, data) {
    // Parse called by default when using asyncHTTP
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() = \"${asyncResponse.getJson()}\")", 100)
            parseResult(asyncResponse.getJson())
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}


/*
    Methods related to configureChildDevices()

    configureChildDevices() detects which child devices to create/update and does the creation/updating
*/

void parseConfigureChildDevices(asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

boolean containsKeyInSubMap(aMap, key) {
    boolean hasKey = false
    aMap.find {
        try{
            hasKey = it.value.containsKey(key)
        } catch(e) {

        }
        hasKey == true
    }
    return hasKey
}

Integer numOfKeyInSubMap(aMap, String key) {
    Integer numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
            // Do nothing
        }
    }
    return numKeys
}

Integer numOfKeysIsMap(aMap) {
    Integer numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}

void configureChildDevices(asyncResponse, data) {
    // This detects which child devices to create/update and does the creation/updating
    def statusMap = asyncResponse.getJson()
    logging("configureChildDevices() statusMap=$statusMap", 1)
    // Use statusMap to determine which Child Devices we should create

    // The built-in Generic Components are:
    //
    // Acceleration Sensor  - ID: 189
    // Button Controller    - ID: 1029
    // Central Scene Dimmer - ID: 912
    // Central Scene Switch - ID: 913
    // Contact Sensor       - ID: 192
    // Contact/Switch       - ID: 199
    // CT                   - ID: 198
    // Dimmer               - ID: 187
    // Metering Switch      - ID: 188
    // Motion Sensor        - ID: 197
    // RGB                  - ID: 195
    // RGBW                 - ID: 191
    // Smoke Detector       - ID: 196
    // Switch               - ID: 190
    // Temperature Sensor   - ID: 200
    // Water Sensor         - ID: 194
    

    // {"StatusSTS":{"Time":"2020-01-26T01:13:27","Uptime":"15T02:59:27","UptimeSec":1306767,
    // "Heap":26,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER1":"OFF",
    // "POWER2":"OFF","POWER3":"OFF","POWER4":"OFF","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:CF:11","Channel":1,"RSSI":62,"LinkCount":37,"Downtime":"0T00:05:48"}}}

    // With a dimmer:
    // {"StatusSTS":{"Time":"2020-01-26T11:58:10","Uptime":"0T00:01:20","UptimeSec":80,"Heap":26,
    // "SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER":"OFF","Dimmer":0,
    // "Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB+CW+WW light:
    // {"StatusSTS":{"Time":"2020-01-26T12:07:57","Uptime":"0T00:06:58","UptimeSec":418,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF62","HSBColor":"0,0,0","Channel":[0,0,0,100,38],"CT":250,"Scheme":0,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":96,"LinkCount":1,"Downtime":"0T00:00:06"}}}
    

    // With an RGB+W light:
    // {"StatusSTS":{"Time":"2020-01-26T12:11:56","Uptime":"0T00:00:26","UptimeSec":26,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF","HSBColor":"0,0,0","Channel":[0,0,0,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":90,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB light:
    // {"StatusSTS":{"Time":"2020-01-26T12:14:15","Uptime":"0T00:00:19","UptimeSec":19,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FFFFFF","HSBColor":"0,0,100","Channel":[100,100,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":98,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW+WW ("CT" is available):
    // {"StatusSTS":{"Time":"2020-01-26T12:16:48","Uptime":"0T00:00:17","UptimeSec":17,"Heap":28,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FF62","HSBColor":"0,0,0","Channel":[100,38],"CT":250,"Fade":"ON","Speed":10,
    // "LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":94,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW or WW (PWM1 configured on the correct pin), just the same as a normal dimmer...
    // {"StatusSTS":{"Time":"2020-01-26T12:19:51","Uptime":"0T00:01:15","UptimeSec":75,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":71,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":88,"LinkCount":1,"Downtime":"0T00:00:25"}}}

    // Addressable RGB light (has the attribute "Width")
    // {"StatusSNS":{"Time":"2020-01-26T12:57:30","SR04":{"Distance":8.579}}}
    // {"StatusSTS":{"Time":"2020-01-26T12:57:30","Uptime":"0T00:02:14","UptimeSec":134,"Heap":21,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":113,"MqttCount":0,"POWER1":"ON","POWER2":"ON",
    // "Dimmer":100,"Color":"00FF00","HSBColor":"120,100,100","Channel":[0,100,0],"Scheme":13,
    // "Width":2,"Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"Signal":-40,"LinkCount":1,
    // "Downtime":"0T00:00:09"}}}

    // {"StatusSNS":{"Time":"2020-01-26T01:24:16","BMP280":{"Temperature":23.710,"Pressure":1017.6},
    // "PressureUnit":"hPa","TempUnit":"C"}}

    // Multiple temperature sensors:
    // {"Time":"2020-01-26T17:45:30","DS18B20-1":{"Id":"000008BD38BF","Temperature":26.1},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":25.1},"DS18B20-3":{"Id":"000008C02C3A",
    // "Temperature":25.3},"TempUnit":"C"}
    
    // For DS18B20, us ID to distinguish them? Then you can't replace them...
    // For AM2301 the GPIO used is appended.
    // {"StatusSNS":{"Time":"2020-01-26T20:54:10","DS18B20-1":{"Id":"000008BD38BF","Temperature":25.8},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":24.7},"DS18B20-3":{"Id":"000008C02C3A","Temperature":24.9},
    // "AM2301-12":{"Temperature":25.1,"Humidity":66.4},"AM2301-14":{"Temperature":null,"Humidity":null},"TempUnit":"C"}}

    // D5 = GPIO14
    // D6 = GPIO12
    // D7 = GPIO13

    // Distance Sensor
    // {"StatusSNS":{"Time":"2020-01-26T13:52:19","SR04":{"Distance":11.667}}}

    // {"NAME":"ControlRGBWWCW","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}

    // result: [Time:2020-01-30T11:30:43, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // result: [Time:2020-01-30T11:31:12, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.0], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // [hasEnergy:false, numTemperature:5, numHumidity:2, numPressure:0, numDistance:0, sensorMap:[AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4]], numSwitch:0, isDimmer:false, isAddressable:false, isRGB:false, hasCT:false]

    // SENSOR = {"Time":"2020-01-30T19:15:08","SR04":{"Distance":73.702}}

    // Switch or Metering Switch are the two most likely ones
    def deviceInfo = [:]
    deviceInfo["hasEnergy"] = false
    deviceInfo["numTemperature"] = 0
    deviceInfo["numHumidity"] = 0
    deviceInfo["numPressure"] = 0
    deviceInfo["numDistance"] = 0
    deviceInfo["numSensorGroups"] = 0
    deviceInfo["sensorMap"] = [:]
    if(statusMap.containsKey("StatusSNS")) {
        sns = statusMap["StatusSNS"]
        deviceInfo["hasEnergy"] = sns.containsKey("ENERGY")
        deviceInfo["sensorMap"] = getKeysWithMapAndId(sns)
        // Energy is the only one that doesn't belong... Just remove it...
        deviceInfo["sensorMap"].remove("ENERGY")
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = numOfKeyInSubMap(sns, "Distance")
    }

    deviceInfo["numSwitch"] = 0
    deviceInfo["isDimmer"] = false
    deviceInfo["isAddressable"] = false
    deviceInfo["isRGB"] = false
    deviceInfo["hasCT"] = false
    deviceInfo["hasFanControl"] = false
    if(statusMap["StatusSTS"] != null) {
        sts = statusMap["StatusSTS"]
        deviceInfo["isDimmer"] = sts.containsKey("Dimmer")
        deviceInfo["isAddressable"] = sts.containsKey("Width")
        if(sts.containsKey("Color")) deviceInfo["isRGB"] = sts["Color"].length() >= 6
        deviceInfo["hasCT"] = sts.containsKey("CT")
        deviceInfo["hasFanControl"] = sts.containsKey("FanSpeed")

        if(sts["POWER"] != null) {
            // This only exist if there is ONLY one switch/bulb
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
    }
    logging("Device info found: $deviceInfo", 100)

    // Create the devices, if needed

    // Switches
    def driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
    def namespace = "tasmota"
    if(deviceInfo["numSwitch"] > 0) {
        /*if(deviceInfo["numSwitch"] > 1 && (
            deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
            deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                // This is supported now ;)
                // log.warn "There's more than one switch and the device is either dimmable, addressable, RGB or has CT capability. This is not fully supported yet, please report which device and settings you're using to the developer so that a solution can be found."

        }*/
        if(deviceInfo["hasEnergy"]  == true && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                // TODO: Make a Component Dimmer with Metering
                driverName = ["Tasmota - Universal Metering Dimmer (Child)", "Generic Component Dimmer"]
            } else {
                driverName = ["Tasmota - Universal Metering Plug/Outlet (Child)", 
                              "Tasmota - Universal Metering Bulb/Light (Child)",
                              "Generic Component Metering Switch"]
            }
        } else {
            if(deviceInfo["hasEnergy"] == true) {
                log.warn "This device reports Metering Capability AND has RGB, Color Temperature or is Addressable. Metering values will be ignored... This is NOT supported and may result in errors, please report it to the developer to find a solution."
            }
            if((deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
                deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
                } else if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == true) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component CT"]
                } else if(deviceInfo["isRGB"] == true && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGB"]
                } else {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGBW"]
                }
            }
        }
        
        
        for(i in 1..deviceInfo["numSwitch"]) {
            namespace = "tasmota"
            def childId = "POWER$i"
            def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
            def childLabel = "${getMinimizedDriverName(device.getLabel())} ($i)"
            logging("createChildDevice: POWER$i", 1)
            createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            // Once the first switch is created we only support one type... At least for now...
            driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
        }
    }
    
    // Fan Control
    if(deviceInfo["hasFanControl"] == true) {
        logging("hasFanControl", 0)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Fan Control (Child)"]
        def childId = "FAN"
        def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    // Sensors
    logging("Available in sensorMap: ${deviceInfo["sensorMap"]}, size:${deviceInfo["numSensorGroups"]}", 0)
    deviceInfo["sensorMap"].each {
        logging("sensorMap: $it.key", 0)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multi Sensor (Child)"]
        def childId = "${it.key}"
        def childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        def childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }
    //logging("After sensor creation...", 0)
    // Finally let the default parser have the data as well...
    parseResult(statusMap)
}

String getChildDeviceNameRoot(boolean keepType=false) {
    String childDeviceNameRoot = getDeviceInfoByName('name')
    if(childDeviceNameRoot.toLowerCase().endsWith(' (parent)')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-9)
    } else if(childDeviceNameRoot.toLowerCase().endsWith(' parent')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-7)
    }
    if(keepType == false && childDeviceNameRoot.toLowerCase().startsWith('tasmota - ')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(10, childDeviceNameRoot.length())
    }
    return childDeviceNameRoot
}

String getMinimizedDriverName(String driverName) {
    // Remove parts we don't need from the string 
    logging("getMinimizedDriverName(driverName=$driverName)", 1)
    if(driverName.toLowerCase().endsWith(' (child)')) {
        driverName = driverName.substring(0, driverName.length()-8)
    } else if(driverName.toLowerCase().endsWith(' child')) {
        driverName = driverName.substring(0, driverName.length()-6)
    }
    if(driverName.toLowerCase().endsWith(' (parent)')) {
        driverName = driverName.substring(0, driverName.length()-9)
    } else if(driverName.toLowerCase().endsWith(' parent')) {
        driverName = driverName.substring(0, driverName.length()-7)
    }
    // Just replace all Occurrances of Parent
    driverName = driverName.replaceAll("(?i) \\(parent\\)", "").replaceAll("(?i) parent", "").replaceAll("(?i)parent", "")
    logging("driverName: $driverName", 1)

    // Remove IP as well
    driverName = driverName.replaceFirst("\\(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\)", "").replaceFirst("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", "");

    if(driverName.toLowerCase().startsWith('tasmota - ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    if(driverName.toLowerCase().startsWith('universal ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    driverName = driverName.replaceAll("Generic Component ", "")
    driverName = driverName.trim()
    if(driverName == '') driverName = "Device"
    
    logging("getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

def getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    logging("createChildDevice(namespace=$namespace, driverName=$driverName, childId=$childId, childName=$childName, childLabel=$childLabel)", 1)
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        // If this driver was used to replace an "old" parent driver, rename the child Network ID
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice) {
        // The device exists, just update it
        childDevice.setName(childName)
        //Setting isComponent to false doesn't change how the device is treated...
        //childDevice.updateDataValue('isComponent', "false")
        logging("childDevice.getData(): ${childDevice.getData()}", 1)
    } else {
        logging("The child device doesn't exist, create it...", 0)
        Integer s = childName.size()
        for(i in 0..s) {
            def currentNamespace = namespace
            if(driverName[i].toLowerCase().startsWith('generic component')) {
                currentNamespace = "hubitat"
            }
            try {
                addChildDevice(currentNamespace, driverName[i], "$device.id-$childId", [name: childName, label: childLabel, isComponent: false])
                logging("Created child device '$childLabel' using driver '${driverName[i]}'...", 100)
            } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                if(i == s) {
                    log.error "'${driverName[i]}' driver can't be found! Did you forget to install the child driver?"
                } else {
                    log.warn "'${driverName[i]}' driver can't be found! Trying another driver: ${driverName[i+1]}..."
                }
            }
        }
    }
}

/*
    Tasmota IP Settings and Wifi status
*/
private String setDeviceNetworkId(String macOrIP, boolean isIP = false) {
    String myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
        logging("About to convert ${macOrIP}...", 0)
        myDNI = convertIPtoHex(macOrIP)
    }
    logging("Device Network Id should be set to ${myDNI} from ${macOrIP}", 0)
    return myDNI
}

void prepareDNI() {
    // Called from updateNeededSettings() and parse(description)
    if (useIPAsID) {
        def hexIPAddress = setDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            updateDNI()
        }
    } else if (state.mac != null && state.dni != state.mac) { 
        state.dni = setDeviceNetworkId(state.mac)
        updateDNI()
    }
}

private void updateDNI() {
    // Called from:
    // prepareDNI()
    // httpGetAction(uri, callback="parse")
    // postAction(uri, data)
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
        logging("Device Network Id will be set to ${state.dni} from ${device.deviceNetworkId}", 0)
        device.deviceNetworkId = state.dni
    }
}

Integer getTelePeriodValue() {
    // Naming this getTelePeriod() will cause Error 500 and other unexpected behavior when
    // telePeriod isn't set to anything...
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private String getHostAddress() {
    Integer port = 80
    if (getDeviceDataByName("port") != null) {
        port = getDeviceDataByName("port").toInteger()
    }
    if (override == true && ipAddress != null){
        // Preferences
        return "${ipAddress}:$port"
    } else if(device.currentValue("ip") != null) {
        // Current States
        return "${device.currentValue("ip")}:$port"
    } else if(getDeviceDataByName("ip") != null) {
        // Data Section
        return "${getDeviceDataByName("ip")}:$port"
    } else {
        // There really is no fallback here, if we get here, something went WRONG, probably with the DB...
        log.warn "getHostAddress() failed and ran out of fallbacks! If this happens, contact the developer, this is an \"impossible\" scenario!"
	    return "127.0.0.1:$port"
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        logging("Got this IP in hex: ${hex}", 0)
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

private String getFirstTwoIPBytes(ipAddress) {
    String ipStart = null
    if(ipAddress != null) {
        ipStart = ipAddress.tokenize( '.' ).take(2).join('.') + '.'
        logging("Got these IP bytes: ${ipStart}", 0)
    } else {
        ipStart = ''
    }
    return ipStart
}

void sync(String ip, Integer port = null) {
    String existingIp = getDataValue("ip")
    String existingPort = getDataValue("port")
    logging("Running sync()", 1)
    if (ip != null && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip, isStateChange: false)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$ip\">$ip</a>", isStateChange: false)
        logging("IP set to ${ip}", 1)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
        logging("Port set to ${port}", 1)
    }
}

Integer dBmToQuality(Integer dBm) {
    // In Tasmota RSSI is actually % already, so just returning the received value here
    // Keeping this around if this behavior changes
    /*Integer quality = 0
    if(dBm > 0) dBm = dBm * -1
    if(dBm <= -100) {
        quality = 0
    } else if(dBm >= -50) {
        quality = 100
    } else {
        quality = 2 * (dBm + 100)
    }
    logging("DBM: $dBm (${quality}%)", 0)*/
    return dBm
}

/*
    HTTP Tasmota API Related
*/
private void httpGetAction(String uri, callback="parse") { 
  updateDNI()
  
  def headers = getHeader()
  logging("Using httpGetAction for 'http://${getHostAddress()}$uri'...", 100)
  try {
    /*hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: uri,
        headers: headers
    )*/
    asynchttpGet(
        callback,
        [uri: "http://${getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in httpGetAction(uri): $e ('$uri')"
  }
}

private postAction(String uri, String data) { 
  updateDNI()

  def headers = getHeader()

  def hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

void sendCommand(String command) {
    sendCommand(command, null)
}

void sendCommand(String command, String argument) {
    String descriptionText = "${command}${argument != null ? " " + argument : ""}"
    logging("sendCommand: $descriptionText", 100)
    sendEvent(name: "commandSent", value: command, descriptionText: descriptionText, isStateChange: true)
    getAction(getCommandString(command, argument), callback="sendCommandParse")
}

def sendCommandParse(asyncResponse, data) {
    // Parse called using sendCommand
    if(asyncResponse != null) {
        try{
            def r = asyncResponse.getJson()
            logging("sendCommandParse(asyncResponse.getJson() = \"${r}\")", 100)
            sendEvent(name: "commandResult", value: asyncResponse.getData(), isStateChange: true)
            parseResult(r)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

String getCommandString(String command, String value) {
    def uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${urlEscape(password)}&"
    }
	if (value != null && value != "") {
		uri += "cmnd=${urlEscape(command)}%20${urlEscape(value)}"
	}
	else {
		uri += "cmnd=${urlEscape(command)}"
	}
    return uri
}

String getMultiCommandString(commands) {
    String uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${password}&"
    }
    uri += "cmnd=backlog%20"
    if(commands.size() > 30) {
        log.warn "Backlog only supports 30 commands, the last ${commands.size() - 30} will be ignored!"
    }
    commands.each {cmd->
        if(cmd.containsKey("value")) {
          uri += "${urlEscape(cmd['command'])}%20${urlEscape(cmd['value'])}%3B%20"
        } else {
          uri += "${urlEscape(cmd['command'])}%3B%20"
        }
    }
    return uri
}

private String urlEscape(String url) {
    //logging("urlEscape(url = $url)", 1)
    return(URLEncoder.encode(url).replace("+", "%20").replace("#", "%23"))
}

private String convertPortToHex(Integer port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private encodeCredentials(String username, String password) {
	String userpassascii = "${username}:${password}"
    String userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private Map getHeader(String userpass = null) {
    Map headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

/**
 * --END-- TASMOTA METHODS (helpers-tasmota)
 */

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
