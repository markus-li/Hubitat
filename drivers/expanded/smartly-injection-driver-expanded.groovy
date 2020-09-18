/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v0.2.2.0918b
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
import java.util.Date

metadata {
    definition (name: "Smartly Injector", namespace: "markus-li", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/smartly-injection-driver-expanded.groovy") {
        capability "Refresh"
        capability "Initialize"
        command "disable"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }
}

void updated() {
    log.info "updated()"
    refresh()
}

def initialize() {
    refresh()
}

void refresh() {
  log.info "refresh()"
  
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