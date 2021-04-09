package main

import (
	"fmt"
)

func operationsInit() {
	thePanels.changes["operations"] = wrapUpdateOperations
	thePanels.getTons["operations"] = getOperationsTons
	thePanels.getStaff["operations"] = getOperationsCrew
	thePanels.floatValues["operations"] = []float32{20.0}
	thePanels.boolValues["operations"] = make([]bool, 0)

	thePanels.changes["operations"] = changesOperations
	updateOperations()
}

func changesOperations() (change1 bool, change2 bool) {
	updateOperations()
	// operations never triggers further changes
	return false, false
}

func updateOperations() {
	operationsDetails := getOperationsDetails()
	thePanels.details["operations"] = operationsDetails
	thePanels.indexDetails = append(thePanels.indexDetails, operationsDetails)
}

func wrapUpdateOperations() (bool, bool) {
	updateOperations()
	return false, false
}

func getOperationsDetails() string {
	tonsUsed := float32(0.0)
	for _, tons := range thePanels.getTons {
		tonsUsed += tons()
	}
	totalStaff := 0
	description := ""
	for _, staff := range thePanels.getStaff {
		staffCount, staffDescription := staff()
		totalStaff += staffCount
		description += staffDescription + "\n"
	}

	thePanels.floatValues["operations"][0] = float32(20.0)
	if thePanels.floatValues["operations"][0] < float32(thePanels.intValues["hull"][hull])*float32(0.02) {
		thePanels.floatValues["operations"][0] = float32(thePanels.intValues["hull"][hull]) * float32(0.02)
	}
	return fmt.Sprintf("Bridge %.1f tons\n",
		thePanels.floatValues["operations"][0])
}

func getOperationsTons() float32 {
	return thePanels.floatValues["operations"][0]
}

func getOperationsCrew() (int, string) {
	if thePanels.intValues["hull"][hull] < 1000 {
		// pilot, nav
		return 2, "Pilot, Navigator\n"
	} else if thePanels.intValues["hull"][hull] < 5000 {
		// purser/captain, comms, pilot, 2xnav
		return 5, "Purser/Captain, Comms, Pilot, 2xNavigator\n"
	} else if thePanels.intValues["hull"][hull] < 25000 {
		// purser, captain, comms, 2xpilot, 2xnav, 2xsecurity, support
		return 10, "Purser, Captain, Comms, 2xPilot, 2xNavigator, 2xSecurity, Support\n"
	} else if thePanels.intValues["hull"][hull] < 100000 {
		// purser, captain, 2xcomms, sensors, 2xpilot, 2xnav, 8xsecurity, 2xsupport
		return 19, "Purser, Captain, 2xComms, Sensors, 2xPilot, 2xNavigator, 8xSecurity, 2xSupport\n"
	} else if thePanels.intValues["hull"][hull] < 500000 {
		// purser, captain, 2xcomms, 2xsensors, 2xpilot, 2xnav, 12xsecurity, 8xsupport
		return 28, "Purser, Captain, 2xComms, 2xSensors, 2xPilot, 2xNavigator, 12xSecurity, 6xSupport\n"
	} else if thePanels.intValues["hull"][hull] < 2500000 {
		// purser, captain, 4xcomms, 2xsensors, 4xpilot, 4xnav, 20xsecurity, 10xsupport
		return 46, "Purser, Captain, 4xComms, 2xSensors, 4xPilot, 4xNavigator, 20xSecurity, 10xSupport\n"
		// purser, captain, 4x dept. heads, 8xcomms, 4xsensors, 4xpilot, 4xnav, 40xsecurity, 20xsupport, 10 maint
		return 96, "Purser, Captain, 4xDept. Heads, 8xComms, 4xSensors, 4xPilot, 4xNavigator, 40xSecurity, 20xSupport, 10 Maintenance\n"
	} else if thePanels.intValues["hull"][hull] < 10000000 {
		// purser, bursar, 2xbankers, commander, captain, 4xlt., 8x dept. heads, 12xcomms, 8xsensors, 8xpilot, 4xnav, 80xsecurity, 40xsupport, 20 maint
		return 190, "Purser, Bursar, 2xBankers, Commander, Captain, 4xLt., 8xDept. Heads, 12xComms, 8xSensors, 8xPilot, 4xNavigator, 80xSecurity, 40xSupport, 20 Maintenance\n"
	} else if thePanels.intValues["hull"][hull] < 25000000 {
		// 4x pursers, 2xbursers, 4xbankers, commander, captain, 12xlt., 20x dept. heads, 12xcomms, 16xsensors, 10xpilot, 6xnav, 120xsecurity, 60xsupport, 40 maint
		return 308, "4x Pursers, 2xBursars, 4xBankers, Commander, Captain, 12xLt., 20xDept. Heads, 12xComms, 16xSensors, 10xPilot, 6xNavigator, 120xSecurity, 60xSupport, 40 Maintenance\n"
	} else {
		// 8x pursers, 4xbursers, 8xbankers, 4xcommander, 4xcaptain, 36xlt., 24x dept. heads, 16xcomms, 16xsensors, 12xpilot, 8xnav, 200xsecurity, 100xsupport, 50 maint
		return 490, "8x Pursers, 4xBursars, 8xBankers, 4xCommanders, 4xCaptains, 24xLt., 24xDept. Heads, 16xComms, 16xSensors, 12xPilot, 8xNavigator, 200xSecurity, 100xSupport, 40 Maintenance\n"
	}
}
