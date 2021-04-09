package main

import (
	"fyne.io/fyne/app"
	"fyne.io/fyne/widget"
)

var details *widget.Label

func main() {
	a := app.New()
	w := a.NewWindow("Designer")
	techInit()
	hullInit()
	drivesInit()
	operationsInit()
	summaryInit()
	if details == nil {
		details = widget.NewLabel("")
	}
	assignDetails(getDetails())


	ui := widget.NewHBox()
	left := widget.NewVBox()
	right := details
	for _, nextSetting := range thePanels.settings {
		left.Append(nextSetting)
	}
	ui.Append(left)
	ui.Append(right)

	w.SetContent(ui)

	w.ShowAndRun()
}

func changes() {
	for _, nextChange := range thePanels.changes {
		nextChange()
	}
	if details == nil {
		details = widget.NewLabel("")
	}
	assignDetails(getDetails())
}

func getDetails() (designDetails string) {
	designDetails = ""
	for _, nextDetail := range thePanels.indexDetails {
		designDetails += nextDetail
	}
	thePanels.indexDetails = make([]string, 0)
	return
}

func assignDetails(designDetails string) {
	details.Text = designDetails
}