// workspace management
let workspaces = new Set([])
let activeworkspace
const start = document.getElementById("start")
const icon = document.getElementById("icon")
function newWorkspace() {
    let newW = new Workspace(`Workspace #${workspaces.size + 1}`)
    w.center(newW.window.elem)
	workspaces.add(newW);
	newW.fullscreen();
	newW.elem.addEventListener("mouseover", () => {
		activeworkspace = newW
	})
    newW.delete.addEventListener("click", () => {
        newW.fullscreen()
        document.body.removeChild(newW.elem)
        workspaces.delete(newW)
    });
	[icon.innerText,start.innerText] = ["+","workspace"]
	start.parentElement.style = "--color:var(--violet-darker);--color2:var(--violet);"
}
start.parentElement.addEventListener("click", newWorkspace);

let shotrcuts = {
	openFileDialog: function() {
		try {activeworkspace.filedialog.showPicker()} catch {
			showMessage({error:"Your browser is blocking the shortcut..."})}
	},
	start: function() {
		activeworkspace ? activeworkspace.active.run() : start.parentElement.click()
	},
	shrinkLeft: function () {activeworkspace.elem.style.gridTemplateColumns = '0 4px 1fr'},
	restoreLeft: function () {activeworkspace.elem.style.gridTemplateColumns = '20em 4px 1fr'},
	fullscreenActive: function () {activeworkspace.fullscreen()},
	windowedActive: function () {activeworkspace.windowed()},
	toggleTags: function () {activeworkspace.active.input.tags&&activeworkspace.active.input.tags.eye.click()},
	openProblems: function () {activeworkspace.active.output.opencheckbox.click()},
	saveAll: function () {activeworkspace.active.output.saveall.click()},
	toggleGrid: function () {body.classList.toggle("grid")},
}
function Hotkey(action,key,superkey = null,highPriority = null) {
	window.addEventListener("keydown", function(event) {
		if (!highPriority&(document.activeElement.tagName == "INPUT")) {return}
		if (superkey?event[superkey]:(!event.ctrlKey&!event.shiftKey&!event.altKey)) {
			event.code === key && event.preventDefault() & shotrcuts[action]()
		}
})}

Hotkey("start",				"Enter",null,true)
Hotkey("openFileDialog",	"Enter","ctrlKey",true)
Hotkey("fullscreenActive",	"ArrowUp","ctrlKey")
Hotkey("windowedActive",	"ArrowDown","ctrlKey")
Hotkey("shrinkLeft",		"ArrowLeft","ctrlKey")
Hotkey("restoreLeft",		"ArrowRight","ctrlKey")
Hotkey("fullscreenActive",	"ArrowUp","shiftKey")
Hotkey("windowedActive",	"ArrowDown","shiftKey")
Hotkey("shrinkLeft",		"ArrowLeft","shiftKey")
Hotkey("restoreLeft",		"ArrowRight","shiftKey")
Hotkey("toggleTags",		"KeyE","ctrlKey")
Hotkey("openProblems",		"KeyO","ctrlKey")
Hotkey("saveAll",			"KeyS","ctrlKey")
Hotkey("toggleGrid",		"Space","ctrlKey")
Hotkey("toggleGrid",		"KeyG","ctrlKey")

// some browsers inject left-side panel which breaks everything
let sidePanel = document.getElementsByTagName("side-panel")
sidePanel.item(0)&&sidePanel.item(0).remove()
