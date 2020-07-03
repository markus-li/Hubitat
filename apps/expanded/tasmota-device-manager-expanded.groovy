// IMPORT URL: https://raw.githubusercontent.com/markus-li/Hubitat/development/apps/expanded/tasmota-device-manager-expanded.groovy
/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v1.0.2.0630Tb
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

// BEGIN:getDefaultParentImports()
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
 
import java.security.MessageDigest
// END:  getDefaultParentImports()

definition(
    name: "Tasmota Device Manager",
    namespace: "tasmota",
    author: "Markus Liljergren (markus-li)",
    description: "Device Manager for Tasmota",
    category: "Convenience",
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
     
     page(name: "disableDebugLoggingOnDevices")
     page(name: "disableInfoLoggingOnDevices")
}

// BEGIN:getHelperFunctions('device-configurations')
TreeMap getDeviceConfigurations() {
    List deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"],
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
         installCommands: [['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan02.html'],

        [typeId: 'sonoff-ifan03-no_beep-m71',
         name: 'Sonoff iFan03 (No Beep)',
         module: 71,
         installCommands: [["SetOption67", "0"], ['Rule1', '0']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'sonoff-ifan03-beep-m71',
         name: 'Sonoff iFan03 (Beep)',
         module: 71,
         installCommands: [["SetOption67", "1"], 
                           ['Rule1', 'ON Fanspeed#Data>=1 DO Buzzer %value%; ENDON ON Fanspeed#Data==0 DO Buzzer 1; ENDON'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/sonoff_ifan03.html'],

        [typeId: 'treatlife-ds01-dimmer',
         name: 'TreatLife DS01 Dimmer ',
         template: '{"NAME":"TL DS01 Dimmer","GPIO":[0,107,0,108,0,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
         installCommands: [["TuyaMCU", "21,2"], 
                           ["DimmerRange", "150,1000"]],
         deviceLink: 'https://templates.blakadder.com/treatlife_DS01.html'],
        
        [typeId: 'deta-6911ha-switch',
         name: 'Deta 6911HA Switch',
         template: '{"NAME":"Deta 1G Switch","GPIO":[0,0,0,0,157,0,0,0,0,21,0,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6911HA.html'],

        [typeId: 'deta-6912ha-switch',
         name: 'Deta 6912HA Switch',
         template: '{"NAME":"DETA 2G Switch","GPIO":[0,0,0,0,157,0,0,0,91,21,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [['Rule1', 'on system#boot do Backlog LedPower 1; LedState 0; LedPower 1; LedState 8; endon'],
                           ['Rule1', '1']],
         deviceLink: 'https://templates.blakadder.com/deta_6912HA.html'],

        [typeId: 'deta-6903ha-switch',
         name: 'Deta 6903HA Switch',
         template: '{"NAME":"DETA 3G Switch","GPIO":[157,0,0,92,91,21,0,0,23,0,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6903HA.html'],

        [typeId: 'deta-6904ha-switch',
         name: 'Deta 6904HA Switch',
         template: '{"NAME":"Deta 4G Switch","GPIO":[157,0,0,19,18,21,0,0,23,20,22,24,17],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6904HA.html'],

        [typeId: 'deta-6922ha-outlet',
         name: 'Deta 6922HA Wall Outlet',
         template: '{"NAME":"DETA 2G GPO","GPIO":[0,0,0,17,157,0,0,0,91,21,22,0,90],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/deta_6922HA.html'],

        [typeId: 'lh-znb22-001-9w ',
         name: 'Lohas ZN033 9W 810lm RGBCCT Bulb ',
         template: '{"NAME":"Lohas RGBCW","GPIO":[0,0,0,0,38,37,0,0,41,39,40,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/lohas-ZN033-B22.html'],

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
        installCommands: [["SetOption66", "0"],
        ],
        deviceLink: ''],

        [typeId: 'zigbee-controller-default' ,
        name: 'Zigbee Controller (default pinout)',
        template: '{"NAME":"Zigbee","GPIO":[0,0,0,0,0,0,0,0,0,166,0,165,0],"FLAG":0,"BASE":18}',
        installCommands: [["SerialLog", "0"],
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
        template: '{"NAME":"ZNSN Curtain","GPIO":[0,107,0,108,21,0,0,0,0,0,0,0,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"],
                        ['SetOption66', "1"],
                        ['SetOption80', "1"],
                        ["PulseTime1", "0"],
                        ["PulseTime2", "0"],
                        ["Interlock", "1,2"],
                        ["Interlock", "ON"],
                        ["ShutterMotorDelay", "4.5"],
                        ["ShutterOpenDuration", "10"],
                        ["ShutterCloseDuration", "11.2"],
                        ["Var1", "ShutterClose1"],
                        ["Var2", "ShutterStop1"],
                        ["Var3", "ShutterOpen1"],
                        ["setoption34", "50"],
                        ["Rule1", "ON Power1#state=1 DO Backlog var3 var3; var2 ShutterStop1; TuyaSend4 101,0 ENDON "],
                        ["Rule1", "+ ON Power1#state=0 DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Power2#state=0 DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Power2#state=1 DO Backlog var1 var1; var2 ShutterStop1; TuyaSend4 101,2 ENDON "],
                        ["Rule1", "+ ON ShutterStop#Data DO Backlog var2 var2; TuyaSend4 101,1; var1 ShutterClose1; var3 ShutterOpen1; ENDON "],
                        ["Rule1", "+ ON Shutter1#Position DO var4 %value% ENDON ON Event#Close0 DO Backlog var2 var2; TuyaSend4 101,1; ENDON "],
                        ["Rule1", "1"],
                        ["Rule2", "ON TuyaReceived#Data=55AA00070005650400010277 DO backlog var1 ShutterClose1; %var1%;  ENDON "],
                        ["Rule2", "+ ON System#Init DO Backlog setoption34 50; var1 ShutterClose1; var2 ShutterStop1; var3 ShutterOpen1; ENDON "],
                        ["Rule2", "+ ON TuyaReceived#Data=55AA00070005020400010214 DO backlog var2 ShutterStop1; %var2%; ENDON "],
                        ["Rule2", "+ ON TuyaReceived#Data=55AA00070005650400010176 DO backlog var3 ShutterOpen1; %var3%; ENDON "],
                        ["Rule2", "+ ON Event#Open100 DO Backlog var2 var2; TuyaSend4 101,1; ENDON "],
                        ["Rule2", "+ ON ShutterOpen#Data=100 DO Event Open%var4% ENDON ON ShutterClose#Data=0 DO Event Close%var4% ENDON "],
                        ["Rule2", "1"],
                        ],

        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        [typeId: 'mj-sd02-dimmer-switch',
        comment: 'WITHOUT power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02","GPIO":[19,18,0,33,34,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption3', '1'],
                        ['setoption1', '1'],
                        ['setoption32', '8'],
                        ['buttontopic', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'mj-sd02-dimmer-switch-led',
        comment: 'WITH power status LED active by design',
        name: 'Martin Jerry MJ-SD02 Dimmer Switch',
        template: '{"NAME":"MJ-SD02-LED","GPIO":[19,18,0,33,56,32,255,255,31,37,30,126,29],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption3', '1'],
                        ['setoption1', '1'],
                        ['setoption32', '8'],
                        ['buttontopic', '0'],
                        ['LedPower', '1'],
                        ['SetOption31', '0'],
                        ['Rule1', 'on Button3#state=2 do dimmer + endon on Button2#state=2 do dimmer - endon '],
                        ['Rule1', '+ on Button2#state=3 do dimmer 20 endon on Button3#state=3 do dimmer 100 endon '],
                        ['Rule1', '+ on Button1#state=2 do power1 2 endon on Button1#state=3 do power1 0 endon'],
                        ['Rule1', '1']],
        deviceLink: ''],

        [typeId: 'maxcio-diffuser-v1',
        comment: 'REQUIRES "Use Alternate Color command in Tasmota" to be set!',
        name: 'Maxcio Diffuser Wood Grain (v1)',
        template: '{"NAME":"MaxcioDiffuser","GPIO":[0,107,0,108,21,0,0,0,37,38,39,28,0],"FLAG":0,"BASE":54}',
        installCommands: [["WebLog", "2"],
                        ['SerialLog', '0'],
                        ['setoption20', '1'],
                        ['Rule1', 'ON Var1#State DO backlog tuyasend3 8,%value%00ffff00; color %value%; rule2 0; power1 1; rule2 1; ENDON ON Scheme#Data=0 DO TuyaSend4 6,0 ENDON ON Scheme#Data>0 DO TuyaSend4 6,1 ENDON ON TuyaReceived#Data=55AA03070005050100010116 DO power1 1 ENDON ON TuyaReceived#Data=55AA03070005010100010011 DO backlog rule2 0; power2 0; rule2 1; power3 %var2%; var2 1; ENDON ON TuyaReceived#Data=55AA03070005010100010112 DO backlog rule2 0; power2 1; rule2 1; var2 0; power3 0; ENDON'],
                        ['Rule2', 'ON Power1#State DO tuyasend1 5,%value% ENDON ON Power2#State=0 DO tuyasend1 1,0 ENDON ON Power2#State=1 DO backlog var2 1; tuyasend1 1,1; ENDON'],
                        ['Rule3', 'ON TuyaReceived#Data=55AA03070005050100010015 DO power1 0 ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', '1'],
                        ['Rule3', '1']],
        deviceLink: 'https://templates.blakadder.com/maxcio_400ml_diffuser.html'],

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
        
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        installCommands: [],
        deviceLink: ''],

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
// END:  getHelperFunctions('device-configurations')

def initialize() {
    logging("initialize()", 1)
}

Long getMillisSinceDate(myDate) {
    
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
private String getAppVersion() {
    String version = "v1.0.2.0630Tb"
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
        makeAppTitle()
        logging("Building mainPage", 1)
        installCheck()
        initializeAdditional()
        if (state.appInstalled == 'COMPLETE') {
            section(getElementStyle('header', getMaterialIcon('settings_applications') + "Configure App"), hideable: true, hidden: false){
                getElementStyle('separator')
                // BEGIN:getDefaultMetadataPreferences()
                input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
                input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
                // END:  getDefaultMetadataPreferences()
                input("passwordDefault", "password", title:"Default Tasmota Password", submitOnChange: true, displayDuringSetup: true)
            
            }
            section(getElementStyle('header', getMaterialIcon('library_add') + "Install New Devices"), hideable: true, hidden: false){
                href("deviceDiscovery", title:getMaterialIcon('', 'he-discovery_1') + "Discover Devices (using SSDP)", description:"")
                href("manuallyAdd", title:getMaterialIcon('', 'he-add_1') + "Manually Install Device", description:"")
            }
            section(getElementStyle('header', getMaterialIcon('keyboard') + "Device Actions"), hideable: true, hidden: true){
                href("disableDebugLoggingOnDevices", title:getMaterialIcon('block') + "Disable Debug Logging on ALL Tasmota devices", description:"")
                href("disableInfoLoggingOnDevices", title:getMaterialIcon('block') + "Disable Info Logging on ALL Tasmota devices", description:"")
            }
            section(getElementStyle('header', getMaterialIcon('playlist_add') + 'Grant Access to Additional Devices'), hideable: true, hidden: true){
                paragraph("Select the devices to grant access to, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                input(name:	"devicesSelected", type: "capability.initialize", title: "Available Devices", multiple: true, required: false, submitOnChange: true)
            }
            section(getElementStyle('header', getMaterialIcon('', 'he-settings1') + "Configure Devices"), hideable: true, hidden: false){ 
                paragraph('<div style="margin: 8px;">All devices below use a compatible driver, if any device is missing, add them above in "Grant Access to Additional Devices". Newly selected devices will not be shown until after you\'ve pressed Done. \"Refresh Devices\" runs the \"Refresh\" command on all devices in the list, this can take a bit of time if you have many devices...</div>')
                
                href("resultPage", title:getMaterialIcon('autorenew') + "Result Page", description: "")
                href("refreshDevices", title:getMaterialIcon('autorenew') + "Refresh Devices", description: "")
                Integer numDevices = 0
                Integer numChildDevices = 0
                getAllTasmotaDevices().each { rawDev ->
                    def cDev = getTasmotaDevice(rawDev.deviceNetworkId)
                    if(cDev != null) {
                        href("configureTasmotaDevice", title:"${getMaterialIcon('', 'he-bulb_1 icon-small')} $cDev.label", description:"", params: [did: cDev.deviceNetworkId])
                        
                        numDevices += 1

                        Map lastActivity = getTimeStringSinceDateWithMaximum(cDev.getLastActivity(), 2*60*60*1000)
                        def deviceStatus = cDev.currentState('presence')?.value
                        logging("$cDev.id - deviceStatus = $deviceStatus", 1)
                        if(deviceStatus == null || deviceStatus == "not present") {
                            deviceStatus = "Timeout"
                        } else {
                            deviceStatus = "Available"
                        }

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
                                        uptime:         [data:uptime, red:uptime == "null"],
                                        lastActivity:   [data:lastActivity['time'], red:lastActivity['red']],
                                        wifi:           [data:"${wifiSignalQuality}", red:wifiSignalQualityRed],
                                        firmware:       [data:firmware, red:firmware == "null"],
                                        driverVersion:  [data:driverVersion, red:driverVersion == "null"],
                                        deviceStatus:   [data:deviceStatus, red:deviceStatus != "Available"],
                                        driverName:     [data:driverName, red:driverName == "null"],
                                        childDevices:   childDevsFiltered,])
                        
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
                input(name: "debugLogging", type: "bool", title: styling_addTitleDiv("Enable debug logging"), description: "" , defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
                input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
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

Map disableDebugLoggingOnDevices(){
    logging("disableDebugLoggingOnDevices()", 1)
    updateAllChildrenWithDeviceSetting("debugLogging", "false")
    return resultPage("disableDebugLoggingOnDevices", "Disable DEBUG Logging Result Page", "DEBUG logging has been DISABLED in all child and grandchild devices")
}

Map disableInfoLoggingOnDevices(){
    logging("disableInfoLoggingOnDevices()", 1)
    updateAllChildrenWithDeviceSetting("infoLogging", "false")
    updateAllChildrenWithDeviceSetting("txtEnable", "false")
    return resultPage("disableInfoLoggingOnDevices", "Disable INFO Logging Result Page", "INFO logging has been DISABLED in all child and grandchild devices")
}

void updateAllChildrenWithDeviceSetting(String settingName, String value) {
    getAllTasmotaDevices().each { rawDev ->
        def cDev = getTasmotaDevice(rawDev.deviceNetworkId)
        if(cDev != null) {
            cDev.clearSetting(settingName)
            cDev.removeSetting(settingName)
            cDev.updateSetting(settingName, value)
            try{
                cDev.getChildDevices().each { gcDev ->
                    gcDev.clearSetting(settingName)
                    gcDev.removeSetting(settingName)
                    gcDev.updateSetting(settingName, value)
                }
            } catch(e) {
                try{
                    runDeviceCommand(cDev, "tasmota_updateChildDeviceSetting", args=[settingName, value])
                } catch(e2) {
                    log.warn("Failed to set the Setting of child (error: $e2):\"${cDev.label}\" (${cDev.id})")
                }
            }
        }
    }
}

Map resultPage(name, title, result, nextPage = "mainPage", otherReturnPage = null, otherReturnTitle="Return Page"){
    logging("resultPage(name = $name, title = $title, result = $result, nextPage = $nextPage)", 1)

    return dynamicPage(name: name, title: "", nextPage: nextPage) {
        makeAppTitle(btnDone=true)

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
        makeAppTitle(btnDone=true)

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
    return '<i class="material-icons icon-position ' + extraClass + '">' + iconName + '</i>'
}

Map btnParagraph(buttons, extra="") {
    String content = '<table style="border-spacing: 10px 0px"><tr>'
    buttons.each {
        content += '<td>'
        
        content += '<a style="color: #000;" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
        
        content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
        
        content += "${it['title']}"
        content += '</button></a></td>'

    }
    content += '</tr></table>'
    return paragraph(content) 
}

String getDeviceTableCell(deviceInfoEntry, link=true) {
    def it = deviceInfoEntry
    String content = '<td class="device-config_td ' + "${it['td_class']}" + '">'
        
    if(link == true) {
        content += '<a class="device-config_btn ' + "${it['class']}" + '" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
    }
    
    String extraTitle = ""
    if(it['title'] != null && it['title'].indexOf('material-icons') == -1) {
        extraTitle = "title=\"${it['title']}\""
    }
    if(it['red'] == true) {
        
            content += "<div ${extraTitle} style=\"color: red;\" >${it['title']}</div>"
        
    } else {
        content += "<div ${extraTitle} >${it['title']}</div>"
    }
    if(link == true) {
        content += '</a>'
    }
    content += '</td>'

    return content
}

String getDeviceTable(deviceInfo, String extra="") {
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

    content += getDeviceTableCell([href:deviceInfo['href']['href'], 
        target:'_blank', title:getMaterialIcon('', 'he-settings1 icon-tiny device-config_btn_icon')])

    content += getDeviceTableCell([class:'device-config_btn', href:getDeviceTasmotaConfigLink(deviceInfo['ip']['data']), 
        target:'_blank', title:deviceInfo['ip']['data']])

    content += getDeviceTableCell([title:deviceInfo['uptime']['data'], red:deviceInfo['uptime']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['lastActivity']['data'], red:deviceInfo['lastActivity']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['wifi']['data'], red:deviceInfo['wifi']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['firmware']['data'], red:deviceInfo['firmware']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['driverVersion']['data'], red:deviceInfo['driverVersion']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['deviceStatus']['data'], red:deviceInfo['deviceStatus']['red']], false)

    content += getDeviceTableCell([title:deviceInfo['driverName']['data'], red:deviceInfo['driverName']['red']], false)

    content += '</tr>'
    content += '<tr>'
    content += "<td class=\"childlist-cell\" colspan=\"9\" >${getChildDeviceHREFList(deviceInfo['childDevices'])}</ td>"
    content += '</tr></table>'
    paragraph(content) 
}

String getChildDeviceHREFList(childDevices) {
    String r = ''
    String prefix = ''
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
}

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
        makeAppTitle()
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
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
        logging("Creating Tasmota-based Wifi Device with dni: ${tasmota_convertIPtoHex(ipAddress)}", 1)
        if(passwordDevice == null || passwordDevice == "") {
           passwordDevice = "[installed]"
        }
        def child = addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Universal Parent", "${tasmota_convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceLabel ? deviceLabel : "Tasmota - Universal Parent (%device_ip%)").replace("%device_ip%", "${ipAddress}"),
           "data": [
                "ip": ipAddress,
                "port": "80",
                "password": encrypt(passwordDevice),
                "deviceConfig": deviceConfig
           ]
        ])

        child.configureDelayed()
        child.refresh()
        def tmpIpAddress = ipAddress
        app.updateSetting("ipAddress", [type: "string", value:tasmota_getFirstTwoIPBytes(ipAddress)])
        app.updateSetting("deviceLabel", "")
        app.updateSetting("passwordDevice", "")
        
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
    //logging("Found Driver Name: '$driverName'", 0)
    return driverName
}

def getDeviceConfigLink(deviceId) {
    return("http://${location.hub.localIP}/device/edit/${deviceId}")
}

def getDeviceLogLink(deviceId) {
    return("http://${location.hub.localIP}/logs#dev${deviceId}")
}

def getDeviceTasmotaConfigLink(deviceIP) {
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
    
    state.devices = devices.sort({ a, b -> a["label"] <=> b["label"] })
    def devicesSelectable = []
    state.devices.each { devicesSelectable << ["${it.deviceNetworkId}":"${it.label}"] }

    logging("devicesSelectable: ${devicesSelectable}", 1)
    state.devicesSelectable = devicesSelectable
	initialize()
}

def runDeviceCommand(device, cmd, args=[]) {
    def jsonSlurper = new JsonSlurper()
    //logging("runDeviceCommand(device=${device.deviceId.toString()}, cmd=${cmd}, args=${args})", 0)
    
    device.refresh(JsonOutput.toJson([cmd: cmd, args: args]))
    r = null
    r = jsonSlurper.parseText(device.getDataValue('appReturn'))

    device.updateDataValue('appReturn', null)
    return r
}

def getAllTasmotaDevices() {
    def toRemove = []
    def devicesFiltered = []
    devicesSelected.eachWithIndex { it, i ->
        def namespace = 'unknown'
        try {
            namespace = it.getDataValue('namespace')
        } catch(e) {
            logging("Device ID: ${it.deviceId.toString()}, e: ${e}", 1)
        }

        //logging("Device ID: ${it.deviceId.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        if(namespace == 'tasmota' && it.parentAppId != app.id) {
            devicesFiltered << it
        }
    }
    def childDevices = getChildDevices()
    logging("getChildDevices: ${getChildDevices()}", 1)
    childDevices.eachWithIndex { it, i ->
        def namespace = 'unknown'
        try {
            namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
        } catch(e) {
            logging("Device ID: ${it.id.toString()}, e: ${e}", 1)
        }

        //logging("Device ID: ${it.id.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        if(namespace == 'tasmota') {
            devicesFiltered << it
        }
    }
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
            if(it.deviceNetworkId == deviceNetworkId) {
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
    deviceDiscoveryCancel()
}

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

// BEGIN:getHelperFunctions('app-default')
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
 
def installed() {
	logging("installed()", 100)
	try {
        installedAdditional()
    } catch (MissingMethodException e) {
    }
}
// END:  getHelperFunctions('app-default')

// BEGIN:getHelperFunctions('app-css')
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
#collapse5 .hrefElem::before {
    filter: invert(100%);
}
#collapse5 .hrefElem:hover::before, #collapse5 .hrefElem:focus::before {
    filter: invert(0%);
}
#collapse5 table .hrefElem::before {
    filter: invert(0%);
}
#collapse5 .btn-block {
    color: #f5f5f5;
    background-color: #382e2b;
    
    font-size: 14px;
    /*font-size: calc(100% + 0.08vw);*/
    max-width: inherit;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
#collapse5 .btn-block span {
    white-space: nowrap !important;
    max-width: inherit;
}
#collapse5 .btn-block:hover, #collapse5 .btn-block:focus {
    color: #212121;
    background-color: #e0e0e0;
}
#collapse5 div.mdl-textfield {
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
#collapse5 [name*="refreshDevices"] {
    float: right !important;
}
#collapse5 [name*="resultPage"] {
    float: left !important;
    visibility: hidden;
}
#collapse5 [name*="refreshDevices"], #collapse5 [name*="resultPage"] {
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
#collapse5 .mdl-cell--12-col:nth-of-type(2), #collapse5 .mdl-cell--12-col:nth-of-type(3) {
    width: 50% !important;
    display:inline !important;
}
#collapse5 [name*="refreshDevices"] span, #collapse5 [name*="resultPage"] span {
    font-weight: 500;
    text-align: center !important;
    white-space: nowrap !important;
}
#collapse5 [name*="refreshDevices"]::before, #collapse5 [name*="resultPage"]::before {
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
// END:  getHelperFunctions('app-css')

// BEGIN:getHelperFunctions('app-tasmota-device-discovery')
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
        runIn(1800, "deviceDiscoveryCancel")
        verifyDevices()
        state.deviceRefreshStart = now()
    }

	def refreshInterval = 10
    
	if((deviceRefreshCount % 3) == 0) {
		verifyDevices()
	}
    
	return dynamicPage(name:"deviceDiscovery", title:"", nextPage:"deviceDiscoveryPage2", refreshInterval:refreshInterval) {
        makeAppTitle()
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
            
		}
	}
}

String getAvailableDevicesList() {
    
    def vdevices = getVerifiedDevices()
	def options = [:]
	vdevices.each {
		def value = "${it.value.name}"
		def key = "${it.value.networkAddress}"
		options["${key}"] = value
	}
    String title = "Available Devices (${options.size() ?: 0} found)"
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
    return paragraph(header + deviceList + footer, submitOnChange: false)
}

def deviceDiscoveryPage2() {
    Integer deviceRefreshCount = !state.deviceRefreshCount ? 0 : state.deviceRefreshCount as Integer

    def devices = devicesDiscovered()
    
	def options = devices ?: [:]
	def numFound = options.size() ?: 0
    
    return dynamicPage(name:"deviceDiscoveryPage2", title:"", nextPage:"discoveredAddConfirm") {
        makeAppTitle()
		section(getElementStyle('header', getMaterialIcon('', 'he-discovery_1') + "Discover a Tasmota Device"), hideable: true, hidden: false) {
            paragraph("Installed devices are not displayed (if Tasmota Device Manager has access to them). Previously discovered devices will show quickly, devices never seen by Tasmota Device Manager before may take time to discover.")
            input("deviceType", "enum", title:"Device Type", description: "", required: true, submitOnChange: true, options: 
                 
                ["Tasmota - Universal Parent",
                ]
                 
            )
            input(name: "deviceConfig", type: "enum", title: "Device Configuration", 
                description: "Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting.", 
                options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", required: false, submitOnChange: true)
            input("selectedDiscoveredDevice", "enum", required:false, title:"Select a Tasmota Device (${numFound} found)", multiple:false, options:options, submitOnChange: true)
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
    if ( ipAddress != null && ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
        logging("Creating Tasmota-based Wifi Device with dni: ${tasmota_convertIPtoHex(ipAddress)}", 1)
        selectedDevice.value["installed"] = true

        if(passwordDevice == null || passwordDevice == "") {
           passwordDevice = "[installed]"
        }
        com.hubitat.app.ChildDeviceWrapper child = addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Universal Parent", "${tasmota_convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceLabel ? deviceLabel : "Tasmota - Universal Parent (%device_ip%)").replace("%device_ip%", "${ipAddress}"),
           "data": [
                "ip": ipAddress,
                "port": "80",
                "password": encrypt(passwordDevice),
                "deviceConfig": deviceConfig
           ]
        ])

        child.configureDelayed()
        child.refresh()

        app.updateSetting("ipAddress", [type: "string", value:tasmota_getFirstTwoIPBytes(ipAddress)])
        app.updateSetting("passwordDevice", "")
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
    state.devices = state.devicesCached ?: [:]
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
    def devices = getDevices().findAll { it?.value?.verified != true }
    devices.each {
        try{
            def ip = convertHexToIP(it.value.networkAddress)
            def port = convertHexToInt(it.value.deviceAddress)
            String host = "${ip}:${port}"
            sendHubCommand(new hubitat.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", hubitat.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
        } catch(e) {
        }
    }
    devices = getDevices().findAll { it?.value?.verified == true && it?.value?.installed == null }
    if(devices != [:]) {
        def installedDeviceIPs = getAllTasmotaDeviceIPs()
        devices.each {
            def ip = convertHexToIP(it.value.networkAddress)
            if(ip in installedDeviceIPs) {
                it.value << [installed:true]
            } else {
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
	def body = hubResponse.xml
	if (body?.device?.manufacturer?.text().startsWith("iTead")) {
		def devices = getDevices()
		def device = devices.find {it?.key?.contains(body?.device?.UDN?.text())}
		if (device) {
			device.value << [name:body?.device?.friendlyName?.text() + " (" + convertHexToIP(hubResponse.ip) + ")", serialNumber:body?.device?.serialNumber?.text(), verified: true]
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
// END:  getHelperFunctions('app-tasmota-device-discovery')

// BEGIN:getHelperFunctions('tasmota')
void parse(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("tasmota: parse(asyncResponse.getJson() = \"${asyncResponse.getJson()}\")", 1)
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
        logging("tasmota: parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

void reboot() {
	logging("tasmota: reboot()", 1)
    tasmota_getAction(tasmota_getCommandString("Restart", "1"))
}

void sendCommand(String command, callback="tasmota_sendCommandParse") {
    logging("tasmota: sendCommand(command=$command)", 1)
    sendCommand(command, null)
}

void sendCommand(String command, String argument, callback="tasmota_sendCommandParse") {
    String descriptionText = "${command}${argument != null ? " " + argument : ""}"
    logging("tasmota: sendCommand($descriptionText)", 1)
    if(callback == "tasmota_sendCommandParse") {
        sendEvent(name: "commandSent", value: command, descriptionText: descriptionText, isStateChange: true)
    }
    tasmota_getAction(tasmota_getCommandString(command, argument), callback=callback)
}

void updatePresence(String presence) {
}

void tasmota_installedPreConfigure() {
    logging("tasmota_installedPreConfigure()", 1)
    logging("Password: ${decrypt(getDataValue('password'))}", 1)
    String pass = decrypt(getDataValue('password'))
    if(pass != null && pass != "" && pass != "[installed]") {
        device.updateSetting("password", [value: pass, type: "password"])
    }
    device.updateSetting("deviceConfig", [type: "enum", value:getDataValue('deviceConfig')])
}

void tasmota_updatedDefault() {
    log.warn("tasmota_updatedDefault() should NOT be used!")
    logging("tasmota_updatedDefault()", 10)
    updateNeededSettings()
    
    try {
    } catch (MissingMethodException e) {
    }
}

void tasmota_refreshChildren() {
    logging("tasmota_refreshChildren()", 1)
    tasmota_getAction(tasmota_getCommandString("Status", "0"), callback="tasmota_parseConfigureChildDevices")
}

void tasmota_refreshChildrenAgain() {
    logging("tasmota_refreshChildrenAgain()", 1)
    tasmota_refreshChildren()
}

Map tasmota_refresh(Map metaConfig=null) {
	logging("tasmota_refresh(metaConfig=$metaConfig)", 100)
    state.clear()

    tasmota_getAction(tasmota_getCommandString("Status", "0"), callback="tasmota_parseConfigureChildDevices")
    getDriverVersion()

    updateDataValue('namespace', getDeviceInfoByName('namespace'))

    metaConfig = clearThingsToHide()
    metaConfig = setCommandsToHide([], metaConfig=metaConfig)
    metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
        'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
    
    metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
    metaConfig = setDatasToHide(['namespace', 'appReturn', 'password'], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    try {
    } catch (MissingMethodException e1) {
        try {
        } catch (MissingMethodException e2) {
        }
    }
    return metaConfig
}

void tasmota_runInstallCommands(List installCommands) {
    logging("tasmota_runInstallCommands(installCommands=$installCommands)", 1)
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

    pauseExecution(100)
    while(backlogs.size() > 0) {
        tasmota_getAction(tasmota_getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        if(backlogs.size() > 0) pauseExecution(1000)
    }

    [rule1, rule2, rule3].each {
        it.each {rule->
            tasmota_getAction(tasmota_getCommandString(rule["command"], rule["value"]))
            pauseExecution(100)
        }
    }
}

void tasmota_updatePresence(String presence) {
    logging("tasmota_updatePresence(presence=$presence) DISABLED", 1)
     
}

Map tasmota_parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) { 
            map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        } else {
            map += [(nameAndValue[0].trim()):""]
        }
	}
}

void tasmota_getAction(String uri, String callback="parse") { 
     
    tasmota_httpGetAction(uri, callback=callback)
}

void tasmota_parseConfigureChildDevices(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("tasmota_parseConfigureChildDevices(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            tasmota_configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("tasmota_parseConfigureChildDevices(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("tasmota_parseConfigureChildDevices(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

void tasmota_configureChildDevices(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    Map statusMap = asyncResponse.getJson()
    logging("tasmota_configureChildDevices() statusMap=$statusMap", 1)

    Map deviceInfo = [:]
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
        deviceInfo["isShutter"] = sns.containsKey("Shutter1")
        deviceInfo["sensorMap"] = map_getKeysWithMapAndId(sns)
        deviceInfo["sensorMap"].remove("ENERGY")
        deviceInfo["sensorMap"].remove("Shutter1")
        deviceInfo["sensorMap"].remove("Shutter2")
        deviceInfo["sensorMap"].remove("Shutter3")
        deviceInfo["sensorMap"].remove("Shutter4")
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = map_numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = map_numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = map_numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = map_numOfKeyInSubMap(sns, "Distance")
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
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
        if(deviceInfo["isShutter"] == true && deviceInfo["numSwitch"] >= 2) {
            deviceInfo["numSwitch"] -= 2
        }
    }
    logging("Device info found: $deviceInfo", 100)

    List driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
    String namespace = "tasmota"
    if(deviceInfo["numSwitch"] > 0) {
         
        if(deviceInfo["hasEnergy"]  == true && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
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
            String childId = "POWER$i"
            String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
            String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($i)"
            logging("createChildDevice: POWER$i", 1)
            tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            driverName = ["Tasmota - Universal Plug/Outlet (Child)", "Generic Component Switch"]
        }
    }
    
    if(deviceInfo["hasFanControl"] == true) {
         
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Fan Control (Child)"]
        String childId = "FAN"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    if(deviceInfo["isShutter"] == true) {
        logging("isShutter", 100)
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Curtain (Child)"]
        String childId = "SHUTTER"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    deviceInfo["sensorMap"].each {
         
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multi Sensor (Child)"]
        String childId = "${it.key}"
        String childName = tasmota_getChildDeviceNameRoot(keepType=true) + " ${tasmota_getMinimizedDriverName(driverName[0])} ($childId)"
        String childLabel = "${tasmota_getMinimizedDriverName(device.getLabel())} ($childId)"
        tasmota_createChildDevice(namespace, driverName, childId, childName, childLabel)
    }
    deviceInfo = null
    parseResult(statusMap)
    statusMap = null
}

String tasmota_getChildDeviceNameRoot(boolean keepType=false) {
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

String tasmota_getMinimizedDriverName(String driverName) {
    logging("tasmota_getMinimizedDriverName(driverName=$driverName)", 1)
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
    driverName = driverName.replaceAll("(?i) \\(parent\\)", "").replaceAll("(?i) parent", "").replaceAll("(?i)parent", "")
    logging("driverName: $driverName", 1)

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
    
    logging("tasmota_getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

com.hubitat.app.ChildDeviceWrapper tasmota_getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void tasmota_createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    logging("tasmota_createChildDevice(namespace=$namespace, driverName=$driverName, childId=$childId, childName=$childName, childLabel=$childLabel)", 1)
    com.hubitat.app.ChildDeviceWrapper childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice != null) {
        childDevice.setName(childName)
        logging("childDevice.getData(): ${childDevice.getData()}", 1)
    } else {
         
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

private void tasmota_updateChildDeviceSetting(String settingName, String value) {
    getChildDevices().each { cDev ->
        cDev.clearSetting(settingName)
        cDev.removeSetting(settingName)
        cDev.updateSetting(settingName, value)
    }
}

private String tasmota_determineDeviceNetworkId(String macOrIP, boolean isIP = false) {
    String myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
         
        myDNI = tasmota_convertIPtoHex(macOrIP)
    }
     
    return myDNI
}

void tasmota_prepareDNI() {
    if (useIPAsID) {
        String hexIPAddress = tasmota_determineDeviceNetworkId(ipAddress, true)
        if(hexIPAddress != null && state.dni != hexIPAddress) {
            state.dni = hexIPAddress
            tasmota_updateDNI()
        }
    } else if (state.mac != null && state.dni != state.mac) { 
        state.dni = tasmota_determineDeviceNetworkId(state.mac)
        tasmota_updateDNI()
    }
}

private void tasmota_updateDNI() {
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
         
        device.deviceNetworkId = state.dni
    }
}

Integer tasmota_getTelePeriodValue() {
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private String tasmota_getHostAddress() {
    Integer port = 80
    if (getDeviceDataByName("port") != null) {
        port = getDeviceDataByName("port").toInteger()
    }
    if (override == true && ipAddress != null){
        return "${ipAddress}:$port"
    } else if(device.currentValue("ip") != null) {
        return "${device.currentValue("ip")}:$port"
    } else if(getDeviceDataByName("ip") != null) {
        return "${getDeviceDataByName("ip")}:$port"
    } else {
        log.warn "tasmota_getHostAddress() failed and ran out of fallbacks! If this happens, contact the developer, this is an \"impossible\" scenario!"
	    return "127.0.0.1:$port"
    }
}

private String tasmota_convertIPtoHex(String ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
         
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

private String tasmota_getFirstTwoIPBytes(String ipAddress) {
    String ipStart = null
    if(ipAddress != null) {
        ipStart = ipAddress.tokenize( '.' ).take(2).join('.') + '.'
         
    } else {
        ipStart = ''
    }
    return ipStart
}

void tasmota_sync(String ip, Integer port = null) {
    String existingIp = getDataValue("ip")
    String existingPort = getDataValue("port")
    logging("Running tasmota_sync()", 1)
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

Integer tasmota_dBmToQuality(Integer dBm) {
     
    return dBm
}

private void tasmota_httpGetAction(String uri, String callback="parse") { 
  tasmota_updateDNI()
  
  Map headers = tasmota_getHeader()
  logging("tasmota_httpGetAction for 'http://${tasmota_getHostAddress()}$uri'...", 1)
  try {
     
    asynchttpGet(
        callback,
        [uri: "http://${tasmota_getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in tasmota_httpGetAction(uri): $e ('$uri')"
  }
}

private hubitat.device.HubAction tasmota_postAction(String uri, String data) { 
  tasmota_updateDNI()

  Map headers = tasmota_getHeader()

  hubitat.device.HubAction hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in tasmota_postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

void tasmota_sendCommandParse(hubitat.scheduling.AsyncResponse asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            Map r = asyncResponse.getJson()
            logging("tasmota_sendCommandParse(asyncResponse.getJson() = \"${r}\")", 1)
            sendEvent(name: "commandResult", value: asyncResponse.getData(), isStateChange: true)
            parseResult(r)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("tasmota_sendCommandParse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("tasmota_sendCommandParse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("tasmota_sendCommandParse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

String tasmota_getCommandString(String command, String value) {
    String uri = "/cm?"
    if (password != null) {
        uri += "user=admin&password=${tasmota_urlEscape(password)}&"
    }
	if (value != null && value != "") {
		uri += "cmnd=${tasmota_urlEscape(command)}%20${tasmota_urlEscape(value)}"
	}
	else {
		uri += "cmnd=${tasmota_urlEscape(command)}"
	}
    return uri
}

String tasmota_getMultiCommandString(List<Map> commands) {
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
          uri += "${tasmota_urlEscape(cmd['command'])}%20${tasmota_urlEscape(cmd['value'])}%3B%20"
        } else {
          uri += "${tasmota_urlEscape(cmd['command'])}%3B%20"
        }
    }
    return uri
}

private String tasmota_urlEscape(String url) {
    return(URLEncoder.encode(url).replace("+", "%20").replace("#", "%23"))
}

private String tasmota_convertPortToHex(Integer port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private String tasmota_encodeCredentials(String username, String password) {
	String userpassascii = "${username}:${password}"
    String userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private Map tasmota_getHeader(String userpass = null) {
    Map headers = [:]
    headers.put("Host", tasmota_getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}
// END:  getHelperFunctions('tasmota')

// BEGIN:getHelperFunctions('map')
boolean map_containsKeyInSubMap(aMap, key) {
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

Integer map_numOfKeyInSubMap(aMap, String key) {
    Integer numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
        }
    }
    return numKeys
}

Integer map_numOfKeysIsMap(aMap) {
    Integer numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap map_getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}
// END:  getHelperFunctions('map')

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
