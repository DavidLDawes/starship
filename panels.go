package main

import "fyne.io/fyne/widget"

type build func() (*widget.Box, *widget.Form)

type simple func()
type twoBoolReturn func() (bool, bool)
type floatReturn func() float32
type intReturn func() int

type takseString func(string)

type panels struct {
	changes     map[string]twoBoolReturn
	builds      map[string]build
	tons        map[string]floatReturn
	selects     map[string][]*widget.Select
	intValues   map[string][]int
	boolValues  map[string][]bool
	floatValues map[string][]float32
	settings    map[string]*widget.Form
	detailBox   map[string]*widget.Box
	details     map[string]string
}

var thePanels = panels {
		changes:    make(map[string]twoBoolReturn),
		builds:     make(map[string]build),
		tons:       make(map[string]floatReturn),
		selects:    make(map[string][]*widget.Select),
		intValues:  make(map[string][]int),
		boolValues: make(map[string][]bool),
		floatValues: make(map[string][]float32),
		settings:   make(map[string]*widget.Form),
		detailBox:  make(map[string]*widget.Box),
		details:    make(map[string]string),
}
