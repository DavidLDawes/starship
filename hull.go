package main

import (
	"fmt"
	"strconv"

	"fyne.io/fyne/widget"
)

const (
	defaultHull        = 1000
	defaultHullString  = "1000"
	defaultArmor       = 0
	defaultArmorString = "None"
	hull               = 0
	armor              = 1
)

var (
	hullSelect         *widget.Select
	armorSelect        *widget.Select
	armorTonCostByTech = []float32{
		0.025, 0.025, .0125, .0125, .0125, .0125, .00833333, .00833333, .00666666, .005, .004, .003, .002, .002,
	}
	maxArmorByTech = []int{
		8, 9, 10, 11, 12, 13, 14, 15, 24, 34, 36, 42, 46, 50,
	}

	hullSizes  = []string{
		"100", "120", "150", "200", "300", "330", "400", "500", "600", "700", "800", "900", "980",
		"1000", "1200", "1500", "2000", "2500", "2600", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6400", "6500", "7000", "7200", "7500", "8000", "8500", "9000", "9500",
		"10000", "11000", "12000", "13000", "14000", "14400", "15000", "16000", "18000", "20000", "22000", "22500", "25000", "27000", "30000", "35000", "40000", "45000", "50000", "55000", "60000", "64000", "65000", "70000", "72000", "75000", "80000", "85000", "90000", "95000",
		"100000", "110000", "120000", "125000", "130000", "133000", "140000", "144000", "150000", "156000", "160000", "170000", "175000", "180000", "190000", "200000", "210000", "220000", "225000", "250000", "300000", "320000", "350000", "375000", "400000", "450000", "480000", "500000", "550000", "600000", "640000", "650000", "700000", "720000", "750000", "800000", "850000", "900000", "950000",
		"1000000", "1100000", "1150000", "1200000", "1250000", "1300000", "1330000", "1400000", "1440000", "1500000", "1560000", "1600000", "1700000", "1750000", "1800000", "1900000", "2000000", "2100000", "2200000", "2250000", "2500000", "3000000", "3200000", "3500000", "4000000", "4500000", "4800000", "4900000", "5000000", "5500000", "6000000", "6400000", "6500000", "7000000", "7200000", "7500000", "8000000", "8500000", "9000000", "9500000",
		"10000000", "11000000", "12000000", "12500000", "13000000", "13300000", "14000000", "14400000", "15000000", "15600000", "16000000", "17000000", "17500000", "18000000", "19000000", "20000000", "21000000", "22000000", "22500000", "25000000", "30000000", "32000000", "35000000", "39900000", "40000000", "45000000", "48000000", "50000000", "55000000", "60000000", "64000000", "65000000", "70000000", "72000000", "75000000", "80000000", "85000000", "90000000", "95000000",
		"100000000", "115000000",
	}
)

func hullInit() {
	thePanels.details["hull"] = ""
	thePanels.intValues["hull"] = make([]int, 2)
	thePanels.intValues["hull"][hull] = defaultHull
	thePanels.intValues["hull"][armor] = defaultArmor
	hullSelect = widget.NewSelect(hullSizes, stringValuedNothing)
	hullSelect.SetSelected(defaultHullString)
	armorSelect = widget.NewSelect(getArmorRangeFromTech(), armorChange)
	armorSelect.SetSelected(defaultArmorString)

	thePanels.changes["hull"] = updateHullDetails
	thePanels.selects["hull"] = []*widget.Select{hullSelect, armorSelect}
	thePanels.intValues["hull"] = []int{defaultHull, defaultArmor}
	thePanels.boolValues["hull"] = make([]bool, 0)

	thePanels.settings["hull"] = widget.NewForm(widget.NewFormItem("Hull", hullSelect), widget.NewFormItem("Armor", armorSelect))
	updateHullDetails()
	hullSelect.OnChanged = hullChange
	thePanels.getTons["hull"] = getHullTons
}

func hullChange(hullSelected string) {
	for _, nextTonnage := range hullSizes {
		if hullSelected == nextTonnage {
			hTons, err := strconv.Atoi(hullSelected)
			if err != nil {
			} else {
				thePanels.intValues["hull"][hull] = hTons
				updateHullDetails()
				changes()
			}
			break
		}
	}
}

func armorChange(armorSelected string) {
	if armorSelected == "None" {
		thePanels.intValues["hull"][armor] = 0
		updateHullDetails()
		changes()
	} else {
		newArmor, err := strconv.Atoi(armorSelected)
		if err != nil {
		} else {
			thePanels.intValues["hull"][armor] = newArmor
			updateHullDetails()
			changes()
		}
	}
}

func getArmorRangeFromTech() (available []string) {
	available = make([]string, 1)
	available[0] = "None"
	for i := 1; i < maxArmorByTech[thePanels.intValues["tech"][0]]; i++ {
		available = append(available, strconv.Itoa(i))
	}
	return
}

func updateHullDetails() (bool, bool) {
	armorSelect.Options = getArmorRangeFromTech()

	if thePanels.intValues["hull"][armor] < 1 {
		thePanels.floatValues["hull"] = make([]float32, 1)
		thePanels.floatValues["hull"][0] = 0.0
		hullDetails := fmt.Sprintf("Hull tonnage %s, no armor\n",
			strconv.Itoa(thePanels.intValues["hull"][hull]))
		thePanels.details["hull"] = hullDetails
		thePanels.indexDetails = append(thePanels.indexDetails, hullDetails)
	} else {
		thePanels.floatValues["hull"] = make([]float32, 1)
		thePanels.floatValues["hull"][0] = float32(thePanels.intValues["hull"][armor]) *
			float32(thePanels.intValues["hull"][hull]) *
			armorTonCostByTech[thePanels.intValues["tech"][0]]
		hullDetails := fmt.Sprintf("Hull tonnage %s, armor AF-%s using %.1f tons\n",
			strconv.Itoa(thePanels.intValues["hull"][hull]),
			strconv.Itoa(thePanels.intValues["hull"][armor]),
			thePanels.floatValues["hull"][0])

		thePanels.details["hull"] = hullDetails
		thePanels.indexDetails = append(thePanels.indexDetails, hullDetails)
	}
	// Hull updates never trigger further re-updates
	return false, false
}

func getHullTons() float32 {
	return getArmorTons()
}

func getArmorTons() float32 {
	return thePanels.floatValues["hull"][0]
}
