/*
    CHILD DEVICES METHODS (helpers-childDevices)

    Helper functions included when using Child devices

    NOTE: MUCH of this is not in use any more, needs cleaning...
*/

// Get the button number
private channelNumber(String dni) {
    def ch = dni.split("-")[-1] as Integer
    return ch
}

def childOn(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(1, channelNumber(dni))
}

def childOff(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(0, channelNumber(dni))
}

private childSendState(String currentSwitchNumber, String state) {
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${currentSwitchNumber}")}
    if (childDevice) {
        logging("childDevice.sendEvent ${currentSwitchNumber} ${state}",1)
        childDevice.sendEvent(name: "switch", value: state, type: type)
    } else {
        logging("childDevice.sendEvent ${currentSwitchNumber} is missing!",1)
    }
}

private areAllChildrenSwitchedOn(Integer skip = 0) {
    def children = getChildDevices()
    boolean status = true
    Integer i = 1
    children.each {child->
        if (i!=skip) {
  		    if(child.currentState("switch")?.value == "off") {
                status = false
            }
        }
        i++
    }
    return status
}

private sendParseEventToChildren(data) {
    def children = getChildDevices()
    children.each {child->
        child.parseParentData(data)
    }
    return status
}

private void createChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("createChildDevices: creating $numSwitchesI device(s)",1)
    
    // If making changes here, don't forget that recreateDevices need to have the same settings set
    for (i in 1..numSwitchesI) {
        // https://community.hubitat.com/t/composite-devices-parent-child-devices/1925
        #!include:getCreateChildDevicesCommand()
    }
}

def recreateChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("recreateChildDevices: recreating $numSwitchesI device(s)",1)
    def childDevice = null

    for (i in 1..numSwitchesI) {
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
        if (childDevice) {
            // The device exists, just update it
            childDevice.setName("${getDeviceInfoByName('name')} #$i")
            childDevice.setDeviceNetworkId("$device.id-$i")  // This doesn't work right now...
            logging(childDevice.getData(), 10)
            // We leave the device Label alone, since that might be desired by the user to change
            //childDevice.setLabel("$device.displayName $i")
            //.setLabel doesn't seem to work on child devices???
        } else {
            // No such device, we should create it
            #!include:getCreateChildDevicesCommand()
        }
    }
    if (numSwitchesI < 4) {
        // Check if we should delete some devices
        for (i in 1..4) {
            if (i > numSwitchesI) {
                childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
                if (childDevice) {
                    logging("Removing child #$i!", 10)
                    deleteChildDevice(childDevice.deviceNetworkId)
                }
            }
        }
    }
}

def deleteChildren() {
	logging("deleteChildren()", 100)
	def children = getChildDevices()
    
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }
}

/**
 *   --END-- CHILD DEVICES METHODS (helpers-childDevices)
 */