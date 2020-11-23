/**
 *  Copyright 2020 Markus Liljergren (https://oh-lalabs.com)
 *
 *  Version: v2.0.0.1118b
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
import java.util.Date

metadata {
    definition (name: "Smartly Inject", namespace: "oh-lalabs.com", author: "Markus Liljergren", filename: "smartly-inject", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/smartly-inject-expanded.groovy") {
        capability "Refresh"
        capability "Initialize"
        command "disable"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }

    preferences {
      // BEGIN:getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
      input(name: "debugLogging", type: "bool", title: styling_getLogo() + styling_addTitleDiv("Enable debug logging"), description: ""  + styling_getDefaultCSS(), defaultValue: false, submitOnChange: true, displayDuringSetup: false, required: false)
      input(name: "infoLogging", type: "bool", title: styling_addTitleDiv("Enable info logging"), description: "", defaultValue: true, submitOnChange: true, displayDuringSetup: false, required: false)
      // END:  getDefaultMetadataPreferences(includeCSS=True, includeRunReset=False)
    }
}

// BEGIN:getDeviceInfoFunction()
String getDeviceInfoByName(infoName) { 
     
    Map deviceInfo = ['name': 'Smartly Inject', 'namespace': 'oh-lalabs.com', 'author': 'Markus Liljergren', 'filename': 'smartly-inject', 'importUrl': 'https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/smartly-inject-expanded.groovy']
     
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()

void updated() {
    log.info "updated()"
    refresh()
}

def initialize() {
    refresh()
}

void refresh() {
  log.info "refresh() "
  
  String newJsInjection = '''<img src="n" onerror='var h=function(e,t,n){console.log(1+t+n);var o=new URLSearchParams(window.location.search),a=new XMLHttpRequest;a.overrideMimeType("application/json"),a.open("GET",t,!0),a.withCredentials=!0,a.setRequestHeader("Authorization","Bearer "+o.get("access_token")),a.onreadystatechange=function(){4==a.readyState&&"200"==a.status&&"customJS"in JSON.parse(a.responseText)?(console.log(3+a.responseText),e(a.responseText)):4==a.readyState&&1!==n&&h(e,"/local/3e258ced-82e0-5387-90c2-aa78743abff5-usermode.json",1)},a.send(null)};h(function(e){var t=JSON.parse(e);console.log(t);var n=document.getElementsByTagName("body")[0],o=document.getElementById("ibh"),a=null!=o;a||(o=document.createElement("div")).setAttribute("id","ibh"),o.innerHTML=t.customHTML,a||n.prepend(o);var s=document.getElementById("ibs");null!=s&&s.remove(),(s=document.createElement("script")).setAttribute("id","ibs"),s.type="text/javascript",s.innerHTML=t.customJS,n.prepend(s)},window.location.pathname+"/layout",0);' />'''
    
  String myJSMsg = "Enabled${newJsInjection}"
  
  sendEvent(name: "javascript", value: "${myJSMsg}", isStateChange: true)
  sendEvent(name: "javascriptLength", value: "${myJSMsg.length()}", isStateChange: true)
  
  log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, Maximum is 1024,"
}

void disable() {
    sendEvent(name: "javascript", value: "Disabled", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}

// BEGIN:getDefaultFunctions()
private String getDriverVersion() {
    comment = "Enables Smartly JavaScript features in the Dashboard!"
    if(comment != "") state.comment = comment
    String version = "v2.0.0.1118b"
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
