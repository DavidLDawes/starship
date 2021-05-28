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
		_, staffDescription := staff()
		description += staffDescription + "\n"
	}
	totalStaff = getStaffCount()
	updateBerths(totalStaff)
	stewards, stewardsDescription := getBerthsCrew(totalStaff + thePanels.intValues["berths"][1])
	description += stewardsDescription + "\n"
	totalStaff += stewards

	return fmt.Sprintf("%s Total Staff %d\nTonnage used before passgengers %.1f, "+
		"tonnage remaining before passengers %.1f\n"+
		"%.1f tonnage after passengers and accommodations.",
		description, totalStaff, tonsUsed, float32(thePanels.intValues["hull"][hull])-tonsUsed,
		float32(thePanels.intValues["hull"][hull])-tonsUsed-thePanels.floatValues["berths"][0]-thePanels.floatValues["berths"][1])
}

func getStaffCount() (count int) {
	count = 0
	for _, staff := range thePanels.getStaff {
		staffCount, _ := staff()
		count += staffCount
	}

	return
}

func getSummaryTons() float32 {
	return 0
}

func getSummaryCrew() (int, string) {
	return 0, ""
}
