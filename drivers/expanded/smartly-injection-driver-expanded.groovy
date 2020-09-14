/**
 *  Copyright 2020 Markus Liljergren
 *
 *  Version: v0.1.1.0914
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

        command "clear"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }
    preferences {}
}

void updated() {
    log.info "updated()"
    refresh()
}

void refresh() {
  log.info "refresh()"
  
  jsInjectionWithReInsert = '''
<img src="n" onerror='
function lJ(e){var t=new URLSearchParams(window.location.search),n=new XMLHttpRequest;n.overrideMimeType("application/json"),n.open("GET",window.location.pathname+"/layout",!0),n.withCredentials=!0,n.setRequestHeader("Authorization","Bearer "+t.get("access_token")),n.onreadystatechange=function(){4==n.readyState&&"200"==n.status&&e(n.responseText)},n.send(null)}lJ(function(e){console.log(1);var t=JSON.parse(e),n=document.getElementsByTagName("body")[0],o=document.getElementById("inserted-bootstrap-html"),r=null!=o;r||(o=document.createElement("div")).setAttribute("id","inserted-bootstrap-html"),o.innerHTML=t.customHTML,r||n.prepend(o);var a=document.getElementById("inserted-bootstrap-script");null!=a&&a.remove(),(a=document.createElement("script")).setAttribute("id","inserted-bootstrap-script"),a.type="text/javascript",a.innerHTML=t.customJS,n.prepend(a),console.log("1E")});
' />'''

  jsInjectionWithoutReInsert = '''
<svg style="display: none;" onload='
function lJ(e){var t=new URLSearchParams(window.location.search),n=new XMLHttpRequest;n.overrideMimeType("application/json"),n.open("GET",window.location.pathname+"/layout",!0),n.withCredentials=!0,n.setRequestHeader("Authorization","Bearer "+t.get("access_token")),n.onreadystatechange=function(){4==n.readyState&&"200"==n.status&&e(n.responseText)},n.send(null)}lJ(function(e){var t=JSON.parse(e),n=document.getElementsByTagName("body")[0],r=document.getElementById("inserted-bootstrap-html");null==r&&((r=document.createElement("div")).setAttribute("id","inserted-bootstrap-html"),r.innerHTML=t.customHTML,n.prepend(r));var a=document.getElementById("inserted-bootstrap-script");null==a&&((a=document.createElement("script")).setAttribute("id","inserted-bootstrap-script"),a.type="text/javascript",a.innerHTML=t.customJS,n.prepend(a))});
'></svg>'''
    
  String myJSMsg = "Refreshed(${(new Date()).format("ss")})${jsInjectionWithReInsert}"
  
  sendEvent(name: "javascript", value: "${myJSMsg}", isStateChange: true)
  sendEvent(name: "javascriptLength", value: "${myJSMsg.length()}", isStateChange: true)
  
  log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, Maximum is 1024,"
}

void clear() {
    sendEvent(name: "javascript", value: "No JS", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}