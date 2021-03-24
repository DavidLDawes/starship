package main

import (
	"fmt"
	"fyne.io/fyne/widget"
)

const (
	tlOffset = iota
)

var (
	tlSelect *widget.Select
	techDetails = "Tech Level A"
	detailTech *widget.Label
	techOffset = techToOffset("A")
	techLevels = []string {"8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L",}
)

func techInit() {
	thePanels.intValues["tech"] = make([]int, 1)
	thePanels.intValues["tech"][0] = techToOffset("A")
	tlSelect = widget.NewSelect(techLevels, techSelectChange)
	tlSelect.SetSelected("A")
	detailTech = widget.NewLabel(techDetails)
	
	thePanels.changes["tech"] = returnBBFalse
	thePanels.selects["tech"] = []*widget.Select{tlSelect}
	thePanels.intValues["tech"] = []int{techToOffset("A")}
	thePanels.boolValues["tech"] = make([]bool, 0)

	thePanels.details["tech"] = fmt.Sprintf("Tech Level %s", offsetToTech(thePanels.intValues["tech"][0]))
	thePanels.settings["tech"] = widget.NewForm(widget.NewFormItem("Tech Level", tlSelect))
	thePanels.detailBox["tech"] =  widget.NewVBox(widget.NewLabel(""), detailTech)
	thePanels.details["tech"] = techDetails
}

func techSelectChange(tlSelected string) {
	techOffset = techToOffset(tlSelected)
	thePanels.intValues["tech"][0] = techOffset
	thePanels.details["tech"] = fmt.Sprintf("Tech Level %s", offsetToTech(thePanels.intValues["tech"][0]))
	changes()
}

func offsetToTech(tlOffsetIn int) (result string) {
	result = "8"
	switch(tlOffsetIn) {
	default:
	case 0:
		result = "8"
	case 1:
		result = "9"
	case 2:
		result = "A"
	case 3:
		result = "B"
	case 4:
		result = "C"
	case 5:
		result = "D"
	case 6:
		result = "E"
	case 7:
		result = "F"
	case 8:
		result = "G"
	case 9:
		result = "H"
	case 10:
		result = "J"
	case 11:
		result = "K"
	case 12:
		result = "L"
	}
	return
}

func techToOffset(techIn string) (result int) {
	switch(techIn) {
	default:
	case "8":
		result = 0
	case "9":
		result = 1
	case "A":
		result = 2
	case "B":
		result = 3
	case "C":
		result = 4
	case "D":
		result = 5
	case "E":
		result = 6
	case "F":
		result = 7
	case "G":
		result = 8
	case "H":
		result = 9
	case "J":
		result = 10
	case "K":
		result = 11
	case "L":
		result = 12
	}
	return
}

func nothing() {
}

func returnBBFalse() (bool, bool) {
	return false, false
}
