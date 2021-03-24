package main

import (
"fmt"
"fyne.io/fyne/widget"
	"strconv"
)

const (
	defaultHull = 1000
	defaultHullString = "1000"
)

var (
	hSelect *widget.Select
	hDetails = "1000 ton hull"
	detailHull *widget.Label
	hullSizes = []string {"100", "200", "300", "400", "500", "600", "700", "800", "900",
		"1000", "1200", "1500", "2000", "2500", "3000", "3500", "4000", "4500", "5000", "5500", "6000", "6400", "6500", "7000", "7200", "7500", "8000", "8500", "9000", "9500",
		"10000", "11000", "12000", "13000", "14000", "14400", "15000", "16000", "18000", "20000", "22000", "22500", "25000", "30000", "35000", "40000", "45000", "50000", "55000", "60000", "64000", "65000", "70000", "72000", "75000", "80000", "85000", "90000", "95000",
		"100000", "110000", "120000", "125000", "130000", "133000", "140000", "144000", "150000", "156000", "160000", "170000", "175000", "180000", "190000", "200000", "210000", "220000", "225000", "250000", "300000", "320000", "350000", "400000", "450000", "480000", "500000", "550000", "600000", "640000", "650000", "700000", "720000", "750000", "800000", "850000", "900000", "950000",
		"1000000", "1100000", "1200000", "1250000", "1300000", "1330000", "1400000", "1440000", "1500000", "1560000", "1600000", "1700000", "1750000", "1800000", "1900000", "2000000", "2100000", "2200000", "2250000", "2500000", "3000000", "3200000", "3500000", "4000000", "4500000", "4800000", "5000000", "5500000", "6000000", "6400000", "6500000", "7000000", "7200000", "7500000", "8000000", "8500000", "9000000", "9500000",
		"10000000", "11000000", "12000000", "12500000", "13000000", "13300000", "14000000", "14400000", "15000000", "15600000", "16000000", "17000000", "17500000", "18000000", "19000000", "20000000", "21000000", "22000000", "22500000", "25000000", "30000000", "32000000", "35000000", "40000000", "45000000", "48000000", "50000000", "55000000", "60000000", "64000000", "65000000", "70000000", "72000000", "75000000", "80000000", "85000000", "90000000", "95000000",
		"100000000",
	}
)


func hullInit() {
	thePanels.intValues["hull"] = make([]int, 1)
	thePanels.intValues["hull"][0] = defaultHull
	hSelect = widget.NewSelect(hullSizes, hullChange)
	hSelect.SetSelected(defaultHullString)
	detailHull = widget.NewLabel("Hull tonnage 1000")

	thePanels.changes["hull"] = returnBBFalse
	thePanels.selects["hull"] = []*widget.Select{hSelect}
	thePanels.intValues["hull"] = []int{defaultHull}
	thePanels.boolValues["hull"] = make([]bool, 0)

	thePanels.details["hull"] = fmt.Sprintf("Hull tonnage %s", defaultHullString)
	thePanels.settings["hull"] = widget.NewForm(widget.NewFormItem("Hull", hSelect))
	thePanels.detailBox["hull"] =  widget.NewVBox(widget.NewLabel(""), detailHull)
	thePanels.details["hull"] = hDetails
}

func hullChange(hullSelected string) {
	for _, nextTonnage := range hullSizes {
		if hullSelected == nextTonnage {
			hTons, err := strconv.Atoi(hullSelected)
			if err != nil {
			} else {
				thePanels.intValues["hull"][0] = hTons
				thePanels.details["hull"] = fmt.Sprintf("Hull tonnage %s", strconv.Itoa(hTons))
				changes()
			}
			break
		}
	}
}
