/**
 *  Copyright 2020 Markus Liljergren (https://oh-lalabs.com)
 *
 *  Version: v0.1.0.1118b
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
    definition (name: "JavaScript Injector", namespace: "oh-lalabs.com", author: "Markus Liljergren", filename: "javascript-injection-driver", importUrl: "https://raw.githubusercontent.com/markus-li/Hubitat/development/drivers/expanded/javascript-injection-driver-expanded.groovy") {
        capability "TemperatureMeasurement"
        capability "Refresh"

        command "clear"
        command "setTemperature", [[name: "NUMBER", type: "NUMBER"]]
        command "test"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
    }
    preferences {
      input(name: "useDegreeC", type: "bool", title: "Use &deg;C? (Off for &deg;F)", description: "", defaultValue: true, displayDuringSetup: false, required: false)
    }
}

void updated() {
    log.info "updated()"
    refresh()
}

void refresh() {
  log.info "refresh()"
  /* EXAMPLE DATA for Dashboard */
  String exampleCSS = '''
.modal {
  font-family: -apple-system,BlinkMacSystemFont,avenir next,avenir,helvetica neue,helvetica,ubuntu,roboto,noto,segoe ui,arial,sans-serif;
  display: none;
  position: absolute;
  background-color: yellow;
  z-index: 9999;
  top: 50%;
  left: 50%;
  width: 200px;
  height: 200px;
}

.modal.is-open {
  display: block;
}
.modal-close-btn {
  background-color: grey;
}

#open-modal-btn {
position: absolute;
background-color: whitesmoke;
top: 10px;
left: 400px;
}
'''

  /* Insert this EXAMPLE into the JSON for the Dashboard:
  "customCSS": ".modal {\n  font-family: -apple-system,BlinkMacSystemFont,avenir next,avenir,helvetica neue,helvetica,ubuntu,roboto,noto,segoe ui,arial,sans-serif;\n  display: none;\n  position: absolute;\n  background-color: yellow;\n  z-index: 9999;\n  top: 50%;\n  left: 50%;\n  width: 200px;\n  height: 200px;\n}\n\n.modal.is-open {\n  display: block;\n}\n.modal-close-btn {\n  background-color: grey;\n}\n\n#open-modal-btn {\nposition: absolute;\nbackground-color: whitesmoke;\ntop: 10px;\nleft: 400px;\n}",
  "customJS": "\nvar body = document.getElementsByTagName(\"body\")[0];\nvar script = document.getElementById(\"inserted-body-script\");\nvar hasScript = script != null;\nif(!hasScript) {\nscript = document.createElement(\"script\");\nscript.setAttribute(\"id\", \"inserted-body-script\")\n}\n\nscript.type = \"text/javascript\";\n\nscript.src = \"https://cdn.jsdelivr.net/npm/micromodal/dist/micromodal.min.js\";\nif(!hasScript) {\nbody.appendChild(script);\n//alert(6);\n} else {\nMicroModal.show(\"modal-1\");\n//alert(10);\n}\n\nvar div = document.getElementById(\"inserted-body-html\");\nvar hasDiv = div != null;\nif(!hasDiv) {\ndiv = document.createElement(\"div\")\ndiv.setAttribute(\"id\", \"inserted-body-html\")\n}\n\ndiv.innerHTML = \"\";\nif(!hasDiv) {\nbody.prepend(div);\n}\nscript.onload = function() {\nMicroModal.init({debugMode: true});\nMicroModal.show(\"modal-1\");\nalert(2);\n}",
  "customHTML": "<div id=\"open-modal-btn\"><a href=\"#\" data-micromodal-trigger=\"modal-1\">Open Modal</a></div><div id=\"modal-1\" class=\"modal\" aria-hidden=\"true\"><div tabindex=\"-1\" data-micromodal-close><div role=\"dialog\" aria-modal=\"true\" aria-labelledby=\"modal-1-title\" ><header><h2 id=\"modal-1-title\">Modal Title &#229;&#228;&#246;</h2><button class=\"modal-close-btn\" aria-label=\"Close modal\" data-micromodal-close>Close Me</button><img /> <img /></header><div id=\"modal-1-content\">Modal Content</div></div></div></div>",
  */
  
  String jsInjectionWithReInsert = '''
<img src="n" onerror='
var h = function (c, r, number) {
  console.log(1+r+number);
    var u = new URLSearchParams(window.location.search);
    var x = new XMLHttpRequest();
    x.overrideMimeType("application/json");
    x.open("GET", r, true);
    x.withCredentials = true;
    x.setRequestHeader("Authorization","Bearer " + u.get("access_token"));
    x.onreadystatechange = function () {
          if (x.readyState == 4 && x.status == "200" && "customJS" in JSON.parse(x.responseText)) {
            console.log(3+x.responseText);
            c(x.responseText);
          } else if (x.readyState == 4 && number !== 1) {
            h(c, "/local/3e258ced-82e0-5387-90c2-aa78743abff5-usermode.json", 1)
          }};
    x.send(null);  
 }
h(function(response) {
      var data = JSON.parse(response);
      console.log(data);
      var body = document.getElementsByTagName("body")[0];
      var div = document.getElementById("ibh");
      var hasDiv = div != null;
      if(!hasDiv) {
          div = document.createElement("div");
          div.setAttribute("id", "ibh");
      }
      div.innerHTML = data.customHTML;
      if(!hasDiv) {
          body.prepend(div);
      }

      var script = document.getElementById("ibs");
      var hasScript = script != null;
      if(script != null) {
          script.remove();
      }
      script = document.createElement("script");
      script.setAttribute("id", "ibs")
      script.type = "text/javascript";
      script.innerHTML = data.customJS;
      body.prepend(script);
    }, window.location.pathname + "/layout", 0);
' />'''

  String jsInjectionWithoutReInsert = '''
<img src="n" onerror='
function lJ(callback) {
    var urlParams = new URLSearchParams(window.location.search);
    var xobj = new XMLHttpRequest();
        xobj.overrideMimeType("application/json");
    xobj.open("GET", window.location.pathname + "/layout", true);
    xobj.withCredentials = true;
    xobj.setRequestHeader("Authorization","Bearer " + urlParams.get("access_token"));
    xobj.onreadystatechange = function () {
          if (xobj.readyState == 4 && xobj.status == "200") {
            callback(xobj.responseText);
          }
    };
    xobj.send(null);  
 }
lJ(function(response) {
      var data = JSON.parse(response);
      var body = document.getElementsByTagName("body")[0];
      var div = document.getElementById("inserted-bootstrap-html");
      if(div == null) {
          div = document.createElement("div");
          div.setAttribute("id", "inserted-bootstrap-html");
          div.innerHTML = data.customHTML;
          body.prepend(div);
      }
      var script = document.getElementById("inserted-bootstrap-script");
      if(script == null) {
          script = document.createElement("script");
          script.setAttribute("id", "inserted-bootstrap-script");
          script.type = "text/javascript";
          script.innerHTML = data.customJS;
          body.prepend(script);
      }
    });
' />'''

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
  
  log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, Maximum is 1024"
}

void setTemperature(BigDecimal number) {
  String jsInjectionWithoutReInsert = '''
<svg style="display: none;" onload='
function lJ(e){var t=new URLSearchParams(window.location.search),n=new XMLHttpRequest;n.overrideMimeType("application/json"),n.open("GET",window.location.pathname+"/layout",!0),n.withCredentials=!0,n.setRequestHeader("Authorization","Bearer "+t.get("access_token")),n.onreadystatechange=function(){4==n.readyState&&"200"==n.status&&e(n.responseText)},n.send(null)}lJ(function(e){var t=JSON.parse(e),n=document.getElementsByTagName("body")[0],r=document.getElementById("inserted-bootstrap-html");null==r&&((r=document.createElement("div")).setAttribute("id","inserted-bootstrap-html"),r.innerHTML=t.customHTML,n.prepend(r));var a=document.getElementById("inserted-bootstrap-script");null==a&&((a=document.createElement("script")).setAttribute("id","inserted-bootstrap-script"),a.type="text/javascript",a.innerHTML=t.customJS,n.prepend(a))});
'></svg>'''
  String unitDegree = "${(char)176}F"
  if(useDegreeC == true) {
    unitDegree = "${(char)176}C"
  }
  String myJSMsg = "<span class=\"jsi-temp\">$number <span class=\"small\"><span> $unitDegree </span></span></span>${jsInjectionWithoutReInsert}"
  sendEvent(name: "temperature", value: "${number}", unit: unitDegree)
  sendEvent(name: "javascript", value: "${myJSMsg}", unit: unitDegree)
  
}

void clear() {
    sendEvent(name: "javascript", value: "No JS", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}

void test() {
  log.debug "test()"
  String test = "exec"
  if(test == "exec") {
    test += "ute"
  }
  def methods = String.declaredMethods.findAll { !it.synthetic }.name
  log.debug "ls"."$test"()
}