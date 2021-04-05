package main

import (
	"fyne.io/fyne/app"
	"fyne.io/fyne/widget"
)

func main() {
	a := app.New()
	w := a.NewWindow("Designer")

	techInit()
	hullInit()
	drivesInit()
	operationsInit()
	summaryInit()

	ui := widget.NewHBox()
	left := widget.NewVBox()
	right := widget.NewVBox()
	for _, nextSetting := range thePanels.settings {
		left.Append(nextSetting)
	}
	for _, nextBox := range thePanels.indexBox {
		right.Append(nextBox)
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
}
