package main

import (
	"fmt"
)

func summaryInit() {
	thePanels.changes["summary"] = returnBBFalse
	thePanels.getTons["summary"] = getSummaryTons
	thePanels.getStaff["summary"] = getSummaryCrew
	thePanels.floatValues["summary"] = []float32{20.0}
	thePanels.boolValues["summary"] = make([]bool, 0)

	thePanels.changes["summary"] = changesSummary
	updateSummary()
}

func changesSummary() (change1 bool, change2 bool) {
	updateSummary()
	// summary never triggers further changes
	return false, false
}

func updateSummary() {
	summaryDetails := getSummaryDetails()
	thePanels.details["summary"] = summaryDetails
	thePanels.indexDetails = append(thePanels.indexDetails, summaryDetails)
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
