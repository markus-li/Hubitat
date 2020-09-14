/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v0.1.6.0914
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
    definition (name: "Smartly Injector", namespace: "markus-li", author: "Markus Liljergren", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/smartly-injection-driver-expanded.groovy") {
        capability "Refresh"
        capability "Initialize"
        command "disable"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }
    preferences {}
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
  
  newJsInjection = '''<img src="n" onerror='function l(){var e=function(t,n){var a=new URLSearchParams(window.location.search),o=new XMLHttpRequest;o.overrideMimeType("application/json"),o.open("GET",n,!0),o.withCredentials=!0,o.setRequestHeader("Authorization","Bearer "+a.get("access_token")),o.onreadystatechange=function(){4==o.readyState&&"200"==o.status&&"customJS"in JSON.parse(o.responseText)?t(o.responseText):"n"!==n.split("").pop()&&e(t,"/local/3e258ced-82e0-5387-90c2-aa78743abff5-usermode.json")},o.send(null)};e(function(e){var t=JSON.parse(e),n=document.getElementsByTagName("body")[0],a=document.getElementById("ibh"),o=null!=a;o||(a=document.createElement("div")).setAttribute("id","ibh"),a.innerHTML=t.customHTML,o||n.prepend(a);var r=document.getElementById("ibs");null!=r&&r.remove(),(r=document.createElement("script")).setAttribute("id","ibs"),r.type="text/javascript",r.innerHTML=t.customJS,n.prepend(r)},window.location.pathname+"/layout")}l();' />'''
    
  String myJSMsg = "Refreshed(${(new Date()).format("ss")})${newJsInjection}"
  
  sendEvent(name: "javascript", value: "${myJSMsg}", isStateChange: true)
  sendEvent(name: "javascriptLength", value: "${myJSMsg.length()}", isStateChange: true)
  
  log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, Maximum is 1024,"
}

void disable() {
    sendEvent(name: "javascript", value: "No JS", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}