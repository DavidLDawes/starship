package main

import (
	"fmt"
	"fyne.io/fyne/widget"
	"strconv"
)

// thePanels.intValues["drive"][] uses this iota as an index, but only the first 3 items for the actual
// drive settings, an array of {2, 3, 3} of is J-2, M-2, P3
//
// thePanels.floatValues["drive"][] uses this iota as an index as well, using all 6. The first 3 are the
// tonnages of the 3 drive types, then the next three are the prices for those drives
const (
	jump = iota
	maneuver
	power
	fuel
	jumpCost
	maneuverCost
	powerCost
	jumpCostFactor
	jumpDiscount
	maneuverCostFactor
	maneuverDiscount
	powerCostFactor
	powerDiscount
	fuelDiscount
	antiMatter = 0
)

var (
	driveSelections = []string{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}
	powerSelections = []string{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"}

	tech4MDrives = []int{0, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11}
	tech4JDrives = []int{0, 1, 3, 4, 5, 6, 7, 7, 8, 8, 8, 9, 9, 9, 9, 10, 10, 10, 11, 11, 11}
	tech4PDrives = []int{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 11, 11}

	jSelect *widget.Select
    mSelect *widget.Select
	pSelect *widget.Select
	amCheck *widget.Check

	// Power plants cost 3 MCr/ton
	pCost = float32(3)
	// Jump drives cost 4 MCr/ton
	jCost = float32(4)
	// Maneuver is cheaper per G for higher drives. Cost in MCr/ton
	mCost = []float32{1.5, 0.7, 0.5, 0.5, 0.5, 0.5, 0.4, 0.36, 0.33, 0.3, 0.29, 0.28, 0.27, 0.26, 0.25, 0.24, 0.23, 0.22, 0.21, 0.2}

	// Fraction of ship tonnage required
	mTon = []float32{0.01, 0.0125, 0.015, 0.0175, 0.025, 0.0325, 0.04, 0.0475, 0.055, 0.0625, 0.07, 0.0775, 0.0085, 0.00925, 0.01, 0.015, 0.02, 0.025, 0.03, 0.33333}
	jTon = []float32{0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.075, 0.08, 0.0833333, 0.0866666, 0.09, 0.0925, 0.095, 0.0975, 0.1, 0.102, 0.104, 0.106, 0.108, 0.11}

	pTonByTech = []float32{0.04, 0.03, 0.03, 0.03, 0.03, 0.02, 0.02, 0.01, 0.0075, 0.006, 0.005, 0.004, 0.003}
	detailDrives *widget.Label
)

func drivesInit() {
	thePanels.intValues["drives"] = make([]int, 3)  // using 8 so far
	thePanels.floatValues["drives"] = make([]float32, 20)  // using 8 so far
	thePanels.boolValues["drives"] = make([]bool, 1)
	thePanels.intValues["drives"][jump] = 2
	thePanels.floatValues["drives"][jump] = 1.0
	thePanels.floatValues["drives"][jumpDiscount] = 1.0
	thePanels.floatValues["drives"][jumpCostFactor] = 1.0

	thePanels.intValues["drives"][maneuver] = 2
	thePanels.floatValues["drives"][maneuver] = 1.0
	thePanels.floatValues["drives"][maneuverCost] = 1.0
	thePanels.floatValues["drives"][maneuverDiscount] = 1.0
	thePanels.floatValues["drives"][maneuverCostFactor] = 1.0

	thePanels.intValues["drives"][power] = 2
	thePanels.floatValues["drives"][power] = 1.0
	thePanels.floatValues["drives"][powerCost] = 1.0

	thePanels.boolValues["drives"][antiMatter] = false
	thePanels.details["drives"] = getDriveDetails()
	detailDrives = widget.NewLabel(thePanels.details["drives"])

	// creatin gthe widgets during startup, so we have to make the event handlers
	// effectively no-ops until all are defined.
	jSelect = widget.NewSelect(driveSelections, stringValuedNothing)
	jSelect.SetSelected("2")
	mSelect = widget.NewSelect(driveSelections, stringValuedNothing)
	mSelect.SetSelected("2")
	pSelect = widget.NewSelect(powerSelections, stringValuedNothing)
	pSelect.SetSelected("2")

	amCheck = widget.NewCheck("Antimatter", amChecked)
	amCheck.Checked = false

	thePanels.changes["drives"] = updateDrives
	thePanels.selects["drives"] = make([]*widget.Select, 3)
	thePanels.checks["drives"] = make([]*widget.Check, 1)

	// all defined, safe to apply select handlers now
	thePanels.selects["drives"][jump] = jSelect
	thePanels.selects["drives"][jump].OnChanged = jChange
	thePanels.selects["drives"][maneuver] =  mSelect
	thePanels.selects["drives"][maneuver].OnChanged = mChange
	thePanels.selects["drives"][power] = pSelect
	thePanels.selects["drives"][power].OnChanged = pChange
	thePanels.checks["drives"][antiMatter] = amCheck
	// safe to preset the boolean, unlike the selectors, when we are setting up,
	// so this was already set above when we created the NewSelect
	//thePanels.checks["drives"][antiMatter].OnChanged = amChecked

	thePanels.settings["drives"] = widget.NewForm(
		widget.NewFormItem("Jump", jSelect),
		widget.NewFormItem("Maneuver", mSelect),
		widget.NewFormItem("Power", pSelect),
		widget.NewFormItem("Antimatter Fuel", amCheck))

	thePanels.tons["drives"] = getDriveTons
	thePanels.detailBox["drives"] = widget.NewVBox(widget.NewLabel(""), detailDrives)
}

func jChange(newJump string) {
	jmChange(newJump, jump, tech4JDrives)
}

func mChange(newManeuver string) {
	jmChange(newManeuver, maneuver, tech4MDrives)
}

func jmChange(newValue string, dType int, techNeeded []int) {
	if len(newValue) > 0 && len(newValue) < 3 {
		for offset, nextValue := range driveSelections {
			if newValue == nextValue {
				for offset > 0 && techNeeded[offset] > thePanels.intValues["tech"][0] {
					offset--
				}
				thePanels.intValues["drives"][dType] = offset
				thePanels.selects["drives"][dType].Selected = driveSelections[offset]
				updateDrives()
				detailDrives.Text = getDriveDetails()
				thePanels.details["drives"] = detailDrives.Text
				changes()

				break
			}
		}
	}
}

func pChange(newPower string) {
	if len(newPower) > 0 && len(newPower) < 3 {
		for offset, nextPower := range powerSelections {
			if newPower == nextPower {
				thePanels.intValues["drives"][power] = offset + 1
				updateDrives()
				detailDrives.Text = getDriveDetails()
				thePanels.details["drives"] = detailDrives.Text
				changes()
				break
			}
		}
	}
}

func amChecked(antimatter bool) {
	if thePanels.intValues["tech"][0] > tlG {
		thePanels.boolValues["drives"][0] = antimatter
	} else {
		thePanels.boolValues["drives"][0] = false
		thePanels.checks["drives"][antiMatter].Checked = false
	}
	changes()
}

func getDriveDetails() (driveDetails string) {
	thePanels.floatValues["drives"][jumpCostFactor], thePanels.floatValues["drives"][jumpDiscount] = figureTechEffects(tech4JDrives[thePanels.intValues["drives"][jump]])

	// tonnage for Jump drives is discount * jump * tonnage / 10
	thePanels.floatValues["drives"][jump] = thePanels.floatValues["drives"][jumpDiscount] * float32(thePanels.intValues["hull"][0]) * jTon[thePanels.intValues["tech"][0]]
	// cost for Jump drives is CostMultiplier * Jump drive tonnage * 4 (cost per ton of Jump drive)
	thePanels.floatValues["drives"][jumpCost] = thePanels.floatValues["drives"][jumpCostFactor] * jCost * thePanels.floatValues["drives"][jump]

	thePanels.floatValues["drives"][maneuverCostFactor], thePanels.floatValues["drives"][maneuverDiscount] = figureTechEffects(tech4MDrives[thePanels.intValues["drives"][maneuver]])
	// tonnage for Maneuver drives is discount * maneuver * tonnage / 10
	thePanels.floatValues["drives"][maneuver] = thePanels.floatValues["drives"][maneuverDiscount] * float32(thePanels.intValues["hull"][0])*  float32(thePanels.intValues["drives"][maneuver])/100.0
	// cost for Maneuver drives is CostMultiplier * Maneuver drive tonnage * v (variable per tech level, see mCost[])
	thePanels.floatValues["drives"][maneuverCost] = thePanels.floatValues["drives"][maneuverCostFactor] * mCost[thePanels.intValues["tech"][0]] * thePanels.floatValues["drives"][maneuver]

	thePanels.floatValues["drives"][powerCostFactor], thePanels.floatValues["drives"][powerDiscount] = figureTechEffects(tech4MDrives[thePanels.intValues["drives"][power]])
	// tonnage for Maneuver drives is discount * maneuver * tonnage / 10
	thePanels.floatValues["drives"][power] = thePanels.floatValues["drives"][powerDiscount] * float32(thePanels.intValues["hull"][0]) * pTonByTech[thePanels.intValues["tech"][0]]
	// cost for Maneuver drives is CostMultiplier * Maneuver drive tonnage * v (variable per tech level, see mCost[])
	thePanels.floatValues["drives"][powerCost] = thePanels.floatValues["drives"][powerCostFactor] * mCost[thePanels.intValues["tech"][0]] * thePanels.floatValues["drives"][power]

	thePanels.floatValues["drives"][fuelDiscount] = float32(1.0)
	if thePanels.boolValues["drives"][antiMatter] {
		thePanels.floatValues["drives"][fuelDiscount] = float32(10.0)
	}

	if thePanels.intValues["drives"][jump] < 1 {
		thePanels.floatValues["drives"][fuel] =
			thePanels.floatValues["drives"][power] * 4.0 / (3.0) / thePanels.floatValues["drives"][fuelDiscount]

		if thePanels.intValues["drives"][maneuver] < 1 {
			driveDetails = fmt.Sprintf("P-%d, %.1f tons; %.1f tons of fuel; No jump or maneuver",
				thePanels.intValues["drives"][power],
				thePanels.floatValues["drives"][power],
				thePanels.floatValues["drives"][fuel])
		} else {
			driveDetails = fmt.Sprintf("M-%d, %.1f tons; P-%d, %.1f tons; %.1f tons of fuel; No jump",
				thePanels.intValues["drives"][maneuver], thePanels.floatValues["drives"][maneuver],
				thePanels.intValues["drives"][power], thePanels.floatValues["drives"][power],
				thePanels.floatValues["drives"][fuel])

		}
	} else if thePanels.intValues["drives"][maneuver] < 1 {
		thePanels.floatValues["drives"][fuel] =
			(thePanels.floatValues["drives"][power] * 4.0 / (3.0) +
			float32(thePanels.intValues["hull"][0]) * float32(thePanels.intValues["drives"][jump]))  *
			thePanels.floatValues["drives"][fuelDiscount]
		driveDetails = fmt.Sprintf("J-%d, %.1f tons; P-%d, %.1f tons; %.1f tons of fuel; No maneuver",
			thePanels.intValues["drives"][jump], thePanels.floatValues["drives"][jump],
			thePanels.intValues["drives"][power], thePanels.floatValues["drives"][power],
			thePanels.floatValues["drives"][power] * 4.0 / (3.0 * thePanels.floatValues["drives"][fuelDiscount]) +
				thePanels.floatValues["drives"][fuel])

		// tonnage for Jump drives is discount * jump * tonnage / 10

	} else {
			driveDetails = fmt.Sprintf("J-%d, %.1f tons; M-%d, %.1f tons; P-%d, %.1f tons; %.1f tons of fuel",
				thePanels.intValues["drives"][jump], thePanels.floatValues["drives"][jump],
				thePanels.intValues["drives"][maneuver], thePanels.floatValues["drives"][maneuver],
				thePanels.intValues["drives"][power], thePanels.floatValues["drives"][power],
				thePanels.floatValues["drives"][fuel])
	}

	return
}
 
func updateDrives() (jChanged bool, mChanged bool) {
	jChanged = false
	maxJ := 20
	maxM := 20
	//maxP := 20

	switch (techOffset) {
	case 0:
		maxJ = 0
		maxM = 5
	case 1:
	case 2:
		maxJ = 1
		maxM = 6
	case 3:
		maxJ = 2
		maxM = 6
	case 4:
		maxJ = 3
		maxM = 6
	case 5:
		maxJ = 4
		maxM = 6
	case 6:
		maxJ = 5
		maxM = 7
	case 7:
		maxJ = 7
		maxM = 8
	case 8:
		maxJ = 10
		maxM = 9
	case 9:
		maxJ = 12
		maxM = 12
	case 10:
		maxJ = 15
		maxM = 15
	case 11:
		maxJ = 18
		maxM = 18
	case 12:
		maxJ = 20
		maxM = 20
	}
	if maxJ > thePanels.intValues["drives"][power] {
		maxJ = thePanels.intValues["drives"][power]
	}
	if thePanels.intValues["drives"][jump] > maxJ {
		jChanged = true
		jSelect.Selected = strconv.Itoa(maxJ)
		thePanels.intValues["drives"][jump] = maxJ
	}


	if maxM > thePanels.intValues["drives"][power] {
		maxM = thePanels.intValues["drives"][power]
	}
	if thePanels.intValues["drives"][maneuver] > maxM {
		mChanged = true
		mSelect.Selected = strconv.Itoa(maxM)
		thePanels.intValues["drives"][maneuver] = maxM
	}

	detailDrives.Text = getDriveDetails()
	thePanels.details["drives"] = detailDrives.Text

	return
}

func figureTechEffects(tlMin int) (techCost float32, techDiscount float32) {
	techDiscount = 1.0
	techCost = 1.0
	switch thePanels.intValues["tech"][0] - tlMin {
	default:
	case 1:
		break
	case 2:
		techCost = 1.25
		techDiscount = 0.5
	case 3:
		techCost = 1.5
		techDiscount = 0.33333
	case 4:
		techCost = 2.0
		techDiscount = 0.25
	case 5:
		techCost = 2.0
		techDiscount = 0.2
	case 6:
		techCost = 2.2
		techDiscount = 0.166
	case 7:
		techCost = 2.3
		techDiscount = 0.14
	case 8:
		techCost = 2.3
		techDiscount = 0.13
	case 9:
	case 10:
	case 11:
	case 12:
		techCost = 2.3
		techDiscount = 0.125
	}

	return
}

func stringValuedNothing(_ string) {
}

func getDriveTons() float32 {
	return getJDriveTons() + getMDriveTons() + getPPlantTons()
}

func getMDriveTons() float32 {
	return 1
}

func getJDriveTons() float32 {
	return 10.0
}

func getPPlantTons() float32 {
	return 10.0
}
