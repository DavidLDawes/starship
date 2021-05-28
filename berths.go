package main

import (
	"fmt"
	"strconv"

	"fyne.io/fyne/widget"
)

type berthDetails struct {
	description string
	tonsPer     float32 // Tons per occupant
	costPer     float32 // Cost per opccupant
	military    bool    // Only on military?
	capital     bool    // Only on capital ships?
	small       bool    // Available on small ships?
}

var (
	milBarracks     = berthDetails{"Barracks", 0.5, 0.5, true, false, true}
	twoFer          = berthDetails{"Double Bunks", 1.5, 1.0, false, false, true}
	stateroom       = berthDetails{"Staterooms", 3.0, 1.0, false, false, true}
	luxuryStateRoom = berthDetails{"Luxury Staterooms", 5.0, 2.0, false, false, true}
	xStateroom      = berthDetails{"Xeno Staterooms", 6.0, 10.0, false, true, false}
	residence       = berthDetails{"Residence", 12.0, 8.0, false, false, false}
	xResidence      = berthDetails{"Xeno Residence", 15.0, 14.0, false, true, false}
	luxurySuite     = berthDetails{"Luxury Suite", 10.0, 6.0, false, false, false}
	luxuryXSuite    = berthDetails{"Xeno Luxury Suite", 18.0, 16.0, false, false, false}

	berths = []berthDetails{
		milBarracks, twoFer, stateroom, luxuryStateRoom, xStateroom, residence, xResidence, luxurySuite, luxuryXSuite,
	}

	staffBerths = []berthDetails{twoFer, stateroom, xStateroom,  luxuryStateRoom, xStateroom, residence, xResidence}
	passengerBerths = []berthDetails{stateroom, xStateroom,  luxuryStateRoom, xStateroom, luxurySuite, luxuryXSuite}

	defaultBerths         = stateroom.description
	latestBerthsSelection = stateroom
	latestBerth           = defaultBerths
	berthsSelect          *widget.Select

	defaultPassengers = 0
	latestPassengers  = 0
	passengerSelect   *widget.Select

	defaultBerthsStaff         = stateroom.description
	latestBerthsStaffSelection = stateroom
	latestBerthStaff           = defaultBerths
	berthsStaffSelect          *widget.Select
)

func berthsInit() {
	thePanels.intValues["berths"] = make([]int, 2)
	thePanels.intValues["berths"][0] = 10
	thePanels.intValues["berths"][1] = 0
	thePanels.floatValues["berths"] = make([]float32, 2)
	thePanels.floatValues["berths"][0] = 30.0
	thePanels.floatValues["berths"][1] = 0.0
	thePanels.getTons["berths"] = getBerthsTons

	berthsSelect = widget.NewSelect(getBerthsDecriptions(), stringValuedNothing)
	berthsSelect.SetSelected(defaultBerths)

	thePanels.changes["berths"] = changesBerthsAndPassengers
	berthsSelect.OnChanged = berthsChanged

	berthsStaffSelect = widget.NewSelect(getBerthsDecriptions(), stringValuedNothing)
	berthsStaffSelect.SetSelected(defaultBerths)

	berthsStaffSelect.OnChanged = berthsStaffChanged

	passengerSelect = widget.NewSelect(getPassengerSelections(), stringValuedNothing)
	passengerSelect.SetSelected("0")

	passengerSelect.OnChanged = passengersChanged

	thePanels.settings["berths"] = widget.NewForm(
		widget.NewFormItem("Passengers", passengerSelect),
		widget.NewFormItem("Berths", berthsSelect),
		widget.NewFormItem("Staff Berths", berthsStaffSelect))
}

func changesBerthsAndPassengers() (change1 bool, change2 bool) {
	berthsSelect = widget.NewSelect(getBerthsDecriptions(), berthsChanged)
	berthsStaffSelect = widget.NewSelect(getBerthsDecriptions(), berthsStaffChanged)
	passengerSelect = widget.NewSelect(getPassengerSelections(), passengersChanged)
	passengerSelect.Selected = "0"

	return false, false
}

func updateBerths(totalStaff int) {
	berthsDetails := fmt.Sprintf("Staff Berths: %d, Passenger Berths: %d occupants, %.1f tons\n",
		totalStaff,
		thePanels.intValues["berths"][1],
		latestBerthsStaffSelection.tonsPer*float32(totalStaff)+
			latestBerthsSelection.tonsPer*float32(thePanels.intValues["berths"][1]))
	thePanels.details["berths"] = berthsDetails
	thePanels.indexDetails = append(thePanels.indexDetails, berthsDetails)
	thePanels.intValues["berths"][0] = totalStaff
	thePanels.floatValues["berths"][0] = latestBerthsStaffSelection.tonsPer * float32(thePanels.intValues["berths"][0])
	thePanels.floatValues["berths"][1] = latestBerthsSelection.tonsPer * float32(thePanels.intValues["berths"][1])
}

func berthsChanged(newSelection string) {
	for _, nextBerth := range berths {
		if nextBerth.description == newSelection {
			latestBerthsSelection = nextBerth
			latestBerth = nextBerth.description
			changes()
			break
		}
	}
}

func berthsStaffChanged(newSelection string) {
	for _, nextBerth := range berths {
		if nextBerth.description == newSelection {
			latestBerthsStaffSelection = nextBerth
			latestBerthStaff = nextBerth.description
			changes()
			break
		}
	}
}

func passengersChanged(newSelection string) {
	passngersSelected, err := strconv.Atoi(newSelection)
	if err == nil {
		thePanels.intValues["berths"][1] = passngersSelected
	}
}

func getBerthsDecriptions() (available []string) {
	available = make([]string, 0)
	for _, nextBerths := range berths {
		if !isCapital() && !isSmall() {
			if !nextBerths.capital {
				available = append(available, nextBerths.description)
			}
		} else if isSmall() {
			if nextBerths.small {
				available = append(available, nextBerths.description)
			}
		} else {
			available = append(available, nextBerths.description)
		}
	}
	return
}

func getPassengerSelections() (results []string) {
	stepSize := 1
	if 1000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 10000 {
		stepSize = 10
	} else if 10000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 50000 {
		stepSize = 100
	} else if 50000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 100000 {
		stepSize = 200
	} else if 100000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 500000 {
		stepSize = 1000
	} else if 500000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 1000000 {
		stepSize = 2000
	} else if 1000000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 5000000 {
		stepSize = 10000
	} else if 5000000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 10000000 {
		stepSize = 20000
	} else if 10000000 < thePanels.intValues["hull"][0] && thePanels.intValues["hull"][0] <= 50000000 {
		stepSize = 100000
	} else if 50000000 < thePanels.intValues["hull"][0] {
		stepSize = 200000
	}

	for i := 0; float32(i)*latestBerthsStaffSelection.tonsPer < float32(thePanels.intValues["hull"][0]); i += stepSize {
		results = append(results, strconv.Itoa(i))
	}

	return
}

func getBerthsTons() (tons float32) {
	tons = thePanels.floatValues["berths"][0] + thePanels.floatValues["berths"][1]

	return
}

func getBerthsCrew(staff2support int) (crew int, description string) {
	crew = 1 + staff2support/8
	description = fmt.Sprintf("%d stewards", crew)

	return
}

func getBerthsDetails(selection string) (details berthDetails) {
	for _, match := range berths {
		if match.description == selection {
			details = match

			return
		}
	}

	return
}
