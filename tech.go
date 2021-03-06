package main

import (
	"fmt"

	"fyne.io/fyne/widget"
)

const (
	tl8 = 0
	tl9 = 1
	tlA = 2
	tlB = 3
	tlC = 4
	tlD = 5
	tlE = 6
	tlF = 7
	tlG = 8
	tlH = 9
	tlJ = 10
	tlK = 11
	tlL = 12
)

var (
	tlSelect    *widget.Select
	techDetails string
	techOffset  = techToOffset("A")
	techLevels  = []string{"8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L"}
)

func techInit() {
	thePanels.intValues["tech"] = make([]int, 1)
	thePanels.intValues["tech"][0] = techToOffset("A")
	tlSelect = widget.NewSelect(techLevels, stringValuedNothing)
	tlSelect.SetSelected("A")

	thePanels.changes["tech"] = returnBBFalse
	thePanels.selects["tech"] = []*widget.Select{tlSelect}
	thePanels.intValues["tech"] = []int{techToOffset("A")}
	thePanels.boolValues["tech"] = make([]bool, 0)

	techDetails = fmt.Sprintf("Tech Level %s\n", offsetToTech(thePanels.intValues["tech"][0]))
	thePanels.details["tech"] = techDetails
	thePanels.indexDetails = append(thePanels.indexDetails, techDetails)
	thePanels.settings["tech"] = widget.NewForm(widget.NewFormItem("Tech Level", tlSelect))
	tlSelect.OnChanged = techSelectChange
}

func techSelectChange(tlSelected string) {
	techOffset = techToOffset(tlSelected)
	thePanels.intValues["tech"][0] = techOffset
	thePanels.details["tech"] = fmt.Sprintf("Tech Level %s\n", offsetToTech(thePanels.intValues["tech"][0]))
	changes()
}

func offsetToTech(tlOffsetIn int) (result string) {
	result = "8"
	switch tlOffsetIn {
	default:
	case tl8:
		result = "8"
	case tl9:
		result = "9"
	case tlA:
		result = "A"
	case tlB:
		result = "B"
	case tlC:
		result = "C"
	case tlD:
		result = "D"
	case tlE:
		result = "E"
	case tlF:
		result = "F"
	case tlG:
		result = "G"
	case tlH:
		result = "H"
	case tlJ:
		result = "J"
	case tlK:
		result = "K"
	case tlL:
		result = "L"
	}
	return
}

func techToOffset(techIn string) (result int) {
	switch techIn {
	default:
	case "8":
		result = tl8
	case "9":
		result = tl9
	case "A":
		result = tlA
	case "B":
		result = tlB
	case "C":
		result = tlC
	case "D":
		result = tlD
	case "E":
		result = tlE
	case "F":
		result = tlF
	case "G":
		result = tlG
	case "H":
		result = tlH
	case "J":
		result = tlJ
	case "K":
		result = tlK
	case "L":
		result = tlL
	}
	return
}

func nothing() {
}

func returnBBFalse() (bool, bool) {
	return false, false
}
