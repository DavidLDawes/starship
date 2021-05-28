package main

import (
	"fmt"
	"strconv"

	"fyne.io/fyne/widget"
)

type selectionDetails struct {
	description string
	tlMin       int
	massByTL    []float32
	military    bool
	advanced    bool
	array       bool
}

var (
	noElectronics = selectionDetails{
		"None", 0,
		[]float32{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		false, false, false,
	}
	LowCommElectronics = selectionDetails{
		"Low End Comms", 0,
		[]float32{1, 1, .8, .7, .6, .5, .4, .3, .2, .1, .1, .1, .1, .1, .1},
		false, false, false,
	}
	LowSensorElectronics = selectionDetails{
		"Low End Sensors", 0,
		[]float32{2, 2, 2, 1.9, 1.8, 1.7, 1.6, 1.5, 1.4, 1.3, 1.2, 1.1, 1, .9, .8},
		false, false, false,
	}
	LowElectronics = selectionDetails{
		"Low End Comms & Sensors", 0,
		[]float32{3, 3, 2.8, 2.6, 2.5, 2.3, 2.1, 2, 1.8, 1.7, 1.5, 1.4, 1.2, 1.1, 1},
		false, false, false,
	}
	CommercialElectronics = selectionDetails{
		"Commercial Comms & Sensors", 0,
		[]float32{6, 6, 5.7, 5.5, 5.2, 5, 4.6, 4.2, 3.9, 3.5, 3.3, 3, 2.7, 2.5, 2.2},
		false, false, false,
	}
	AdvancedElectronics = selectionDetails{
		"Advanced Comms & Sensors", 3,
		[]float32{50, 50, 50, 12, 11, 10, 10, 10, 9.2, 8.6, 8, 7.6, 7.3, 7, 6.8},
		false, true, false,
	}
	CompactElectronics = selectionDetails{
		"Compact Comms & Sensors", 5,
		[]float32{100, 100, 100, 100, 100, 3, 2, 2, 1.8, 1.7, 1.5, 1.4, 1.3, 1.2, 1.1},
		false, false, false,
	}
	AdvancedCompactElectronics = selectionDetails{
		"Advanced Compact Comms & Sensors", 7,
		[]float32{200, 200, 200, 200, 200, 200, 200, 4, 4, 3.8, 3.5, 3.2, 3, 2.7, 2.5},
		false, true, false,
	}
	MilitaryElectronics = selectionDetails{
		"Military Comms & Sensors", 1,
		[]float32{300, 30, 24, 20, 20, 19.5, 19.5, 19.2, 19, 18.5, 18, 17.5, 17, 16.5, 16},
		false, false, false,
	}
	AdvancedMilitaryElectronics = selectionDetails{
		"Advanced Military Comms & Sensors", 4,
		[]float32{400, 400, 400, 400, 50, 48, 45, 40, 36, 32, 30, 28, 27, 26, 25},
		false, false, false,
	}
	CommercialWithArrayElectronics = selectionDetails{
		"Commercial Comms & Sensors", 0,
		[]float32{6, 6, 5.7, 5.5, 5.2, 5, 4.6, 4.2, 3.9, 3.5, 3.3, 3, 2.7, 2.5, 2.2},
		false, false, true,
	}
	AdvancedWithArrayElectronics = selectionDetails{
		"Advanced Comms & Sensors", 3,
		[]float32{50, 50, 50, 12, 11, 10, 10, 10, 9.2, 8.6, 8, 7.6, 7.3, 7, 6.8},
		false, true, true,
	}
	MilitaryWithArrayElectronics = selectionDetails{
		"Military Comms & Sensors", 1,
		[]float32{300, 30, 24, 20, 20, 19.5, 19.5, 19.2, 19, 18.5, 18, 17.5, 17, 16.5, 16},
		false, false, true,
	}
	AdvancedMilitaryWithArrayElectronics = selectionDetails{
		"Advanced Military Comms & Sensors", 4,
		[]float32{400, 400, 400, 400, 50, 48, 45, 40, 36, 32, 30, 28, 27, 26, 25},
		false, false, true,
	}

	defaultElectronics = CommercialElectronics

	generalSelections = []selectionDetails{
		noElectronics, LowCommElectronics, LowSensorElectronics, LowElectronics,
		CommercialElectronics, AdvancedElectronics, CompactElectronics, AdvancedCompactElectronics,
		MilitaryElectronics, AdvancedMilitaryElectronics,
	}
	capitalSelections = []selectionDetails{
		CommercialElectronics, CommercialWithArrayElectronics,
		AdvancedElectronics, AdvancedWithArrayElectronics,
		MilitaryElectronics, MilitaryWithArrayElectronics,
		AdvancedMilitaryElectronics, AdvancedMilitaryWithArrayElectronics,
	}

	latestSelection   = defaultElectronics
	latestComputer    = "2"
	latestComputerInt = 2

	computerSelections = []string{
		"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
		"11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
	}

	computerMass = []float32{
		1.0, 2, 3.0, 4.0, 5.0, 7.0, 9.0, 11.0, 12.0, 13.0,
		15.0, 18.0, 20.0, 22.0, 24.0, 26.0, 28.0, 33.0, 36.0, 40.0, 45.0, 50.0, 56.0,
	}

	compeuterTechDiscount = []float32{}
	electronicsSelect     *widget.Select
	computerSelect        *widget.Select
)

func electronicsInit() {
	thePanels.changes["electronics"] = returnBBFalse
	thePanels.getTons["electronics"] = getelectronicsTons
	thePanels.getStaff["electronics"] = getelectronicsCrew

	electronicsSelect = widget.NewSelect(getSelections(generalSelections), stringValuedNothing)
	electronicsSelect.SetSelected(defaultElectronics.description)
	computerSelect = widget.NewSelect(computerSelections, stringValuedNothing)
	computerSelect.SetSelected("2")

	thePanels.changes["electronics"] = changesElectronics
	updateelectronics()
	electronicsSelect.OnChanged = electronicsChanged
	computerSelect.OnChanged = computerChanged
	thePanels.settings["electronics"] = widget.NewForm(
		widget.NewFormItem("eSuite", electronicsSelect),
		widget.NewFormItem("Computer", computerSelect))
}

func changesElectronics() (change1 bool, change2 bool) {
	if isCapital() {
		electronicsSelect = widget.NewSelect(getSelections(generalSelections), stringValuedNothing)
	} else {
		electronicsSelect = widget.NewSelect(getSelections(capitalSelections), stringValuedNothing)
	}
	updateelectronics()
	// electronics never triggers further changes
	electronicsSelect.OnChanged = electronicsChanged
	return false, false
}

func updateelectronics() {
	electronicsDetails := fmt.Sprintf("E Suite: %s, %.1f tons; Computer %s, %.1f tons\n",
		latestSelection.description,
		latestSelection.massByTL[thePanels.intValues["tech"][0]],
		latestComputer,
		computerMass[latestComputerInt],
	)
	thePanels.details["electronics"] = electronicsDetails
	thePanels.indexDetails = append(thePanels.indexDetails, electronicsDetails)
	thePanels.floatValues["electronics"] = []float32{latestSelection.massByTL[thePanels.intValues["tech"][0]]}
}

func computerChanged(newSelection string) {
	for _, comp := range computerSelections {
		if comp == newSelection {
			convert, err := strconv.Atoi(comp)
			if err == nil {
				latestComputer = newSelection
				latestComputerInt = convert
				changes()
			}
			break
		}
	}
}

func electronicsChanged(newSelection string) {
	id := electronicsIndex(newSelection)
	if id > -1 {
		latestSelection = getSelectionDetails(newSelection)
	}
	changes()
}

func getElectronicSelections() (available []selectionDetails) {
	available = make([]selectionDetails, 0)
	if isCapital() {
		for _, nextDetails := range capitalSelections {
			if nextDetails.tlMin <= thePanels.intValues["tech"][0] {
				available = append(available, nextDetails)
			}
		}
	} else {
		for _, nextDetails := range generalSelections {
			if nextDetails.tlMin <= thePanels.intValues["tech"][0] {
				available = append(available, nextDetails)
			}
		}
	}
	return
}

func electronicsIndex(selection string) int {
	for index, match := range getElectronicSelections() {
		if match.description == selection {
			return index
		}
	}
	return -1
}

func getSelectionDetails(selection string) (details selectionDetails) {
	for _, match := range getElectronicSelections() {
		if match.description == selection {
			details = match
			return
		}
	}
	return
}

func getSelections(choices []selectionDetails) (selections []string) {
	selections = make([]string, 0)
	for _, nextSelection := range choices {
		selections = append(selections, nextSelection.description)
	}
	return
}

func getelectronicsTons() float32 {
	return 0
}

func getelectronicsCrew() (int, string) {
	return 0, ""
}
