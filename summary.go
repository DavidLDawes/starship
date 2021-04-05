package main

import (
	"fmt"

	"fyne.io/fyne/widget"
)

var detailSummary *widget.Label

func summaryInit() {
	detailSummary = widget.NewLabel("<Not Set Yet>")

	thePanels.changes["summary"] = returnBBFalse
	thePanels.getTons["summary"] = getSummaryTons
	thePanels.getStaff["summary"] = getSummaryCrew
	thePanels.floatValues["summary"] = []float32{20.0}
	thePanels.boolValues["summary"] = make([]bool, 0)

	thePanels.detailBox["summary"] = widget.NewVBox(widget.NewLabel(""), detailSummary)
	thePanels.indexBox = append(thePanels.indexBox, thePanels.detailBox["summary"])
	thePanels.changes["summary"] = changesSummary
	updateSummary()
}

func changesSummary() (change1 bool, change2 bool) {
	updateSummary()
	// summary never triggers further changes
	return false, false
}

func updateSummary() {
	detailSummary.Text = getSummaryDetails()
	thePanels.details["summary"] = detailSummary.Text
}

func getSummaryDetails() string {
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

	return fmt.Sprintf("%sTotal Staff %d\nTonnage used %.1f, tonnage remaining %.1f",
		description, totalStaff, tonsUsed, float32(thePanels.intValues["hull"][hull])-tonsUsed)
}

func getSummaryTons() float32 {
	return 0
}

func getSummaryCrew() (int, string) {
	return 0, ""
}
