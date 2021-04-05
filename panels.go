package main

import "fyne.io/fyne/widget"

type build func() (*widget.Box, *widget.Form)

type (
	simple          func()
	twoBoolReturn   func() (bool, bool)
	floatReturn     func() float32
	intReturn       func() int
	intStringReturn func() (int, string)
)

type takseString func(string)

type panels struct {
	changes     map[string]twoBoolReturn
	builds      map[string]build
	getTons     map[string]floatReturn
	getStaff    map[string]intStringReturn
	selects     map[string][]*widget.Select
	checks      map[string][]*widget.Check
	intValues   map[string][]int
	boolValues  map[string][]bool
	floatValues map[string][]float32
	settings    map[string]*widget.Form
	detailBox   map[string]*widget.Box
	indexBox    []*widget.Box
	details     map[string]string
}

var thePanels = panels{
	changes:     make(map[string]twoBoolReturn),
	builds:      make(map[string]build),
	getTons:     make(map[string]floatReturn),
	getStaff:    make(map[string]intStringReturn),
	selects:     make(map[string][]*widget.Select),
	checks:      make(map[string][]*widget.Check),
	intValues:   make(map[string][]int),
	boolValues:  make(map[string][]bool),
	floatValues: make(map[string][]float32),
	settings:    make(map[string]*widget.Form),
	detailBox:   make(map[string]*widget.Box),
	indexBox:    make([]*widget.Box, 0),
	details:     make(map[string]string),
}
