const body = document.body;
const year = new Date().getFullYear()
var nameforsavedfile = 'logs_jlogrep.txt'

// all new library
// add functionality to js
var n = { // n stands for Node
	make: function (obj,text = "") {
		let elem = document.createElement(obj.tag);
		function add(str) {for (const i of str.split(" ")) {elem.classList.add(i)}}
		obj.class && add(obj.class);
		function set(arr) {for (const i of arr) 
			{elem.setAttribute(Object.keys(i)[0],Object.values(i)[0])}}
		obj.attr && 
			(Array.isArray(obj.attr) ? set(obj.attr) :
				elem.setAttribute(Object.keys(obj.attr)[0],Object.values(obj.attr)[0]));
		elem.innerText = text
		return elem
	},
	nest: function (elem,arr) {
		if (arr.length) {
		for (const i of arr) {
			i.elem ? elem.appendChild(i.elem) : elem.appendChild(i);
		}} else { arr.length != 0 && (arr.elem ? elem.appendChild(arr.elem) : elem.appendChild(arr));}
		return elem
	},
	swap: function (elem,dest) {
		elem.elem ? dest.appendChild(elem.elem) : dest.appendChild(elem);
	},
	clear: function (elem) {
		while (elem.firstChild) elem.firstChild.remove();
	},
}
//elements library
var lib = {
	div: {tag: "div"},
	span: {tag: "span"},
	p: {tag: "p"},
	label: {tag: "label"},
	input: {
		text: {tag: "input",attr: {type:"text"}},
		checkbox: {tag: "input",attr: {type:"checkbox"}},
		file: {tag: "input",attr: {type:"file"}},
		datetime: {tag: "input",attr: {type:"datetime-local"}}
	},
	details: {tag: "details"},
	detailsopen: {tag: "details",attr: {open:""}},
	summary: {tag: "summary"},
	ul: {tag: "ul"},
	li: {tag: "li"}
}

//classes for inputform
class DateInput {
	_elem = {}
	constructor(text) {
		this._elem = {
			div: n.make({tag: "div", class: "dateinput"}),
			label: n.make(lib.label,text),
			input: n.make(lib.input.datetime),
		}
		n.nest(this._elem.div,[
			this._elem.label,
			this._elem.input])
	}
	get elem() {return this._elem.div}
	get output() {return this._elem.input.value}
}
class TextInput {
	_elem = {}
	constructor(text,style,initial,optional = false) {
		this._elem = {
      div: n.make({tag: "div", class: `${style}${optional?' optional':''}`}),
			label: n.make(lib.label, text),
			input: n.make({tag:"input",attr:[{type:"text"},{placeholder:initial}]}),
		}
		n.nest(this._elem.div,[
			this._elem.label,
			this._elem.input])
		
		this.restrictInput = (e) => {
			if(e.code!='Backspace'&e.which!=16&e.which!=17&(e.which<35||e.which>57)){
				e.preventDefault()}
		}
		this._elem.input.addEventListener("keydown",this.restrictInput)
	}
	remove() {
		this._elem.input.removeEventListener("keydown",this.restrictInput);
		this._elem.div.remove();
	}
	get elem() {return this._elem.div}
	get input() {return this._elem.input}
	get output() {return this._elem.input.value}
}
class UploadControl {
	_elem = {}
	constructor(text,optional = false) {
		this._elem = {
      div: n.make({tag: "div", class: `uploadcontrol${optional?' optional':''}`}),
			label: n.make({tag: "label", class: "upload"}),
			span: n.make(lib.span,text),
			input: n.make(lib.input.file),
			close: n.make({tag: "span", class: "close"},"")
		}
		n.nest(this._elem.div,[
			n.nest(this._elem.label,[
				this._elem.span,
				this._elem.input]),
			this._elem.close])
		
		this.updateValue = () => {
			if (!this._elem.input.value) {
				this._elem.span.innerText = text;
				return
			} else {
				this._elem.span.innerText = this._elem.input.files[0].name;
			}
		}
		this._elem.input.addEventListener("change",this.updateValue)

		this.clearFile = () => {
			this._elem.input.value = null;
			const event = new Event("change");
			this._elem.input.dispatchEvent(event);
		}
		this._elem.close.addEventListener("click",this.clearFile)
	}
	remove() {
		this._elem.input.removeEventListener("change",this.updateValue)
		this._elem.close.removeEventListener("click",this.clearFile)
		this._elem.div.remove()
	}
	get elem() {return this._elem.div}
	get input() {return this._elem.input}
	get output() {return this._elem.input.files[0]}
}

class Checkbox {
	_elem = {}
	constructor(text) {
		this._elem = {
			label: n.make({tag: "label", class: "checkbox"}),
			input: n.make(lib.input.checkbox),
			div: n.make({tag: "div", class: text})
		}
		n.nest(this._elem.label,[
			this._elem.input,
			this._elem.div])
	}
	get elem() {return this._elem.label}
	get checkbox() {return this._elem.input}
	get output() {return this._elem.input.checked}
	set output(bool) {this._elem.input.checked = bool}
}

class Filters {
	_elem = {}
	filters = {}
	update() {
		function parseFilters(json) {
			let [c,f] = [{},[]]
			for (const i of json) {
				let checkbox = n.make({tag:"input",attr:[{type:"checkbox"},{tag: i}]})
				c[i] = checkbox
				f.push(n.nest(n.make({tag:"label",class:"checkbox"}),[
					checkbox,
					n.make({tag:"div",class: "tagbox filterbox",attr:{style:`--tag: '${i}'`}})]))
			}
			return {checkboxes: c, elems: f}
		}
		async function getFilters() {
			try {
				const response = await fetch('/filters',{method:'GET'})
				const filters = await response.json()
				return parseFilters(filters)
			} catch(err) { console.log(err) }
		}
		getFilters()
		.then((f) =>{
			this.filters = f
			n.nest(this._elem.div,this.filters.elems)})
		.catch(err => console.log(err));
	}
	constructor() {
		this._elem.div = n.make({tag: "div", class: "textinput checkboxes"})
		this.update()
	}
	get elem() {return this._elem.div}
	get output() {
		let checked = []
		Object.entries(this.filters.checkboxes).map(([k,v]) => {
			v.checked && checked.push(k)
		})
		return checked.length != 0 ? checked : null;
	}
}

class Tags {
	_elem = {}
	eye = {}
	tags = []
	update(file) {
		function parseTags(json) {
			const eye = n.make({tag:"input",attr:{type:"checkbox"}})
			const elem = n.nest(n.make({tag:"label",class:"checkbox"}),[
					eye,n.make({tag:"div",class:"tagbox eyebox"})])
			let [c,t] = [{},[]]
			for (const i of json) {
				let checkbox = n.make({tag:"input",attr: [{type:"checkbox"},{tag: i}]})
				c[i] = checkbox;
				t.push(n.nest(n.make({tag:"label",class:"checkbox"}),[
					checkbox,
					n.make({tag:"div",class:"tagbox",attr:{style: `--tag: '${i}'`}})]))
			}
			return {eye: {checkbox: eye, elem: elem}, tags: {checkboxes: c, elems: t}}
		}
		async function getTags() {
			try {
				const request = new FormData()
				file && request.append('rules',file)
				const response = await fetch('/rules',{method:'POST',body:request})
				const responseJson = await response.json()
                if (responseJson.error) {
                    showMessage(responseJson)
					console.warn("can't get tags", responseJson)
					return null
                } else {
					return parseTags(responseJson)
				}
			} catch(err) { console.log(err) }
		}
		getTags()
		.then(tags => {
			n.clear(this._elem.div);
			if (!tags) return;
			this.eye = tags.eye
			this.tags = tags.tags
			n.nest(this._elem.div,tags.eye.elem);
			n.nest(this._elem.div,tags.tags.elems);
			tags.eye.listener = () => Object.values(tags.tags.checkboxes)
				.forEach(v =>{v.checked = tags.eye.checkbox.checked})
			tags.eye.checkbox.addEventListener("click", tags.eye.listener)
		})
		.catch(err => console.log(err));
	}
	constructor() {
		this._elem.div = n.make({tag: "div", class: "textinput checkboxes"})
		this.update()
	}
	remove() {
		this.eye.checkbox.removeEventListener("click", this.eye.listener)
		this._elem.div.remove()
	}
	get elem() {return this._elem.div}
	get output() {
		let checked = []
		Object.entries(this.tags.checkboxes).map(([k,v]) => {
			!v.checked && checked.push(k)
		})
		return checked.length != 0 ? checked : null;
	}
}

class CopyMenu {
	_elem = n.make({tag:"div",class:"copymenu"})
	_type = {
		mattermost: {elem:n.make(lib.div,"Mattermost")},
		jira: {elem:n.make(lib.div,"Jira")},
		plain: {elem:n.make(lib.div,"Plain text")},
		content: {elem:n.make(lib.div,"Content")}
	}
	constructor() {
		n.nest(this._elem,[
			this._type.mattermost,
			this._type.jira,
			this._type.plain,
			this._type.content
		])
	}
	get elem() {return this._elem}
	get type() {return this._type}
}

class InputMenu {
	_elem = n.make({tag: "div", class: "menu"})
	_buttons = {
    scan: n.make({tag:"div",class:"scan"},"Add scan"),
    expand: new Checkbox("expand"),
    options: new Checkbox("options")
	}
	constructor() {
		n.nest(this._elem,[
      this._buttons.expand,
			this._buttons.scan,
			this._buttons.options
    ])
    this._buttons.options._elem.div.innerText = 'optional'
	}
	get elem() {return this._elem}
	get buttons() {return this._buttons}
}

/* complex structures */
class InputForm {
	_elem = {}
	constructor(text) {
		this._elem = {
			div: n.make({tag: "div", class: "input-form",
				attr: {style: "right: 50%; opacity: 0;"}}),
			details: n.make({tag: "details", attr: {open: ""}}),
			summary: n.make({tag: "summary"}),
			name: n.make(lib.span,text),
			delete: n.make(lib.span,""),
			go: n.make(lib.span,"->"),
			uploadlogs: new UploadLogs(),
			date: new DateInput("date/time"),
			year: new TextInput("year","textinput",year,true),
			interval: new TextInput("interval","textinput","10"),
			filters: new Filters(),
			tags: new Tags(),
			rules: new UploadControl("upload rules.yml"),
			dateformat: new UploadControl("upload dateformat.yml",true),
			button: n.make({tag: "div", class: "button"},"BIG BUTTON TO SCAN")
		}
		n.nest(this._elem.div,[
			n.nest(this._elem.details,[
				n.nest(this._elem.summary,[
					this._elem.name,
					this._elem.go,
					this._elem.delete]),
				this._elem.uploadlogs,
				this._elem.date,
				this._elem.year,
				this._elem.interval,
				this._elem.filters,
				this._elem.tags,
				this._elem.rules,
				this._elem.dateformat]),
			this._elem.button])
		this.updateRules = () => this._elem.rules.input.value?
				this._elem.tags.update(this._elem.rules.output):
				this._elem.tags.update(null)
		this._elem.rules.input.addEventListener("change",this.updateRules)
	}
	appear() {
		this._elem.div.offsetWidth
		this._elem.div.style.right = 0;
		this._elem.div.style.opacity = 1;}
	remove() {
		this._elem.year.remove()
		this._elem.interval.remove()
		this._elem.tags.remove()
		this._elem.rules.remove()
		this._elem.dateformat.remove()
		this._elem.rules.input.removeEventListener("change",this.updateRules)
		this._elem.div.remove()
	}
	get elem() {return this._elem.div}
	get delete() {return this._elem.delete}
	get go() {return this._elem.go}
	get button() {return this._elem.button}
	get tags() {return this._elem.tags}
	get output() {return {
      bugreport: this._elem.uploadlogs.output,
			date: this._elem.date.output,
			year: this._elem.year.output || year,
			interval: this._elem.interval.output,
			filters: this._elem.filters.output,
			tags: this._elem.tags.output,
			rules: this._elem.rules.output,
			dateformat: this._elem.dateformat.output
	}}
}

//for outputs
class OutputMenu {
	_elem = {
		menu: n.make({tag: "div", class: "menu"}),
		open: new Checkbox("openclose"),
		show: new TextInput('show','show',' default'),
		save: n.make({tag: "div", class: "threemenu"},""),
		saveall: n.make(lib.span,"all"),
		saveselected: n.make(lib.span,"selected"),
		copy: n.make({tag: "div", class: "threemenu"},""),
		copyall: n.nest(n.make(lib.div),n.make(lib.span,"all")),
		copymenuall: new CopyMenu(),
		copyselected: n.nest(n.make(lib.div),n.make(lib.span,"selected")),
		copymenuselected: new CopyMenu(),
		erase: n.make({tag: "div", class: "erase"},''),
		expand: new Checkbox("expand"),
	}
	constructor(output) {
		n.nest(this._elem.menu,[
			this._elem.open,
			this._elem.show,
			n.nest(this._elem.save,[
				this._elem.saveall,
				this._elem.saveselected]),
			n.nest(this._elem.copy,[
				n.nest(this._elem.copyall,this._elem.copymenuall),
				n.nest(this._elem.copyselected,this._elem.copymenuselected)]),
			this._elem.erase,
			this._elem.expand,
		])
		this.openProblems = () => {
			this._elem.open.output?
			output._problems.forEach(i=>i.open()):output._problems.forEach(i=>i.close())
		}
		this._elem.open.elem.addEventListener("click",this.openProblems)
		this.applyShow = () => {
      //let test = [] /// WIP
      output._problems.forEach(i=>
			{
        //test = test.concat(i.getTime());
        this._elem.show.output?
				i.showExamples(parseInt(this._elem.show.output)):
				i.showExamples()
			}); 
      //let [start,end] = [Math.min(...test),Math.max(...test)]
      //let interval = (end - start + 1)/10
      //let count = [0,0,0,0,0,0,0,0,0,0]
      //function countTime(timestamp) {
      //  count[Math.floor((timestamp-start)/interval)] += 1
      //}
      //test.forEach(i=>countTime(i))
      //console.log(count)
      //console.log(parseTime(start),parseTime(end))
    }
		this._elem.show.input.addEventListener("keyup",this.applyShow)

		this.problemClear = () => output.problemClear()
		this._elem.erase.addEventListener("click",this.problemClear)

		this._elem.expand.output = true;
		this.changeWindowState = (event) => {
			event.preventDefault();
			this._elem.expand.output ? output.windowed() : output.fullscreen()	
		}
		this._elem.expand.elem.addEventListener("click",this.changeWindowState)

		this.saveAll = () => output.problems&&
			download(nameforsavedfile,parseForSave(output.problems,parseInt(this._elem.show.output)))
		this._elem.saveall.addEventListener("click",this.saveAll)

		this.saveChecked = () => output.checked&&
			download(nameforsavedfile,parseForSave(output.checked,parseInt(this._elem.show.output)))
		this._elem.saveselected.addEventListener("click",this.saveChecked)

		Object.entries(this._elem.copymenuall.type).map(([type,elem]) => {
			const copy = () => copyOnClick(output.problems,type,parseInt(this._elem.show.output))
			elem.listener = copy
			elem.elem.addEventListener("click",elem.listener)
		})
		Object.entries(this._elem.copymenuselected.type).map(([type,elem]) => {
			const copy = () => copyOnClick(output.checked,type,parseInt(this._elem.show.output))
			elem.listener = copy
			elem.elem.addEventListener("click",elem.listener)
		})
	}
	remove() {
		this._elem.open.elem.removeEventListener("click",this.openProblems)
		this._elem.show.input.removeEventListener("keyup",this.applyShow)
		this._elem.erase.removeEventListener("click",this.problemClear)
		this._elem.expand.elem.removeEventListener("click",this.changeWindowState)
		this._elem.saveall.removeEventListener("click",this.saveAll)
		this._elem.saveselected.removeEventListener("click",this.saveChecked)
		Object.values(this._elem.copymenuall.type).forEach(i =>
			i.elem.removeEventListener("click",i.listener)
		)
		Object.values(this._elem.copymenuselected.type).forEach(i =>
			i.elem.removeEventListener("click",i.listener)
		)
		this._elem.menu.remove()
	}
	get elem() {return this._elem.menu}
}

class OutputForm {
	_elem = {}
	_problems = []
	constructor(text) {
		this._elem = {
			div: n.make({tag: "div", class: "output"}),
			menu: new OutputMenu(this),
			output: n.make({tag: "div", class: "result"}),
			loading: n.nest(n.make({tag: "div", class: "load message"}),[
				n.make({tag: "div", class: "animation"}),
				n.make({tag: "div", class: "text"},"loading")]),
			searchbar: n.make({tag: "div", class: "regex"}),
			regex: n.make({tag: "input",attr:[{type:"text"},{placeholder:"RegEx (default .*)"}]}),
      fileContainer: n.make({tag:"div",class:"filehint"}),
			filename: n.make({tag: "input",attr:[{type:"text"},{placeholder:"filename part (default *)"}]}),
      fileHint: n.make(lib.div),
			search: n.make({tag: "div", class: "button active"},"search"),
			placeholder: n.make({tag: "div", class: "placeholder"}),
			window: new Window(text)
		}
		n.nest(this._elem.placeholder,
			n.nest(this._elem.div,[
				this._elem.menu,
				n.nest(this._elem.searchbar,[
					this._elem.regex,
          n.nest(this._elem.fileContainer,[
					  this._elem.filename,
            this._elem.fileHint
          ]),
					this._elem.search,
				]),
				this._elem.output,
				this._elem.loading]))
	}
	windowed() {
		n.nest(body,this._elem.window);
		n.swap(this._elem.div,this._elem.window.content);
		this._elem.menu._elem.expand.output = false;
	}
	fullscreen() {
		n.swap(this._elem.div,this._elem.placeholder);
		this._elem.window.elem.remove();
		this._elem.menu._elem.expand.output = true;
		return this._elem.placeholder
	}
	loadStart() {this._elem.loading.classList.add("flex");}
	loadEnd() {this._elem.loading.classList.remove("flex");}
	problemPush(x) {
		this._problems.push(x);
		this._elem.output.appendChild(x.elem)}
	problemUnshift(x) {
		this._problems.unshift(x);
		this._elem.output.insertBefore(x.elem,this._elem.output.firstChild)}
	problemClear() {
		this._problems.forEach(i=>i.remove())
		this._problems = [];
	}
	remove() {
		this.problemClear();
		this._elem.placeholder.remove();
		this._elem.menu.remove();
	}
	get checked() {
		let c = []
		for (const i in this._problems) {
			this._problems[i].checked&&c.push(this._problems[i])
		}
		return c
	}
	get show() {return parseInt(this._elem.menu._elem.show.output)}
	get elem() {return this._elem.placeholder}
	get result() {return this._elem.output}
	get saveall() {return this._elem.menu._elem.saveall}
	get problems() {return this._problems}
	get open() {return this._elem.menu._elem.open.output}
	get opencheckbox() {return this._elem.menu._elem.open.checkbox}
	get search() {return this._elem.search}
	get regex() {return this._elem.regex.value || ".*"}
	get filename() {return this._elem.filename.value}
}

// for both
class OutputPair {
	_elem
	constructor(name) {
		this._elem = {
			input: new InputForm(name),
			output: new OutputForm(name)
		}
		this.scan = () => sendScan(this)
		this.search = () => sendRegex(this)
    let upload = this._elem.input._elem.uploadlogs
    let filesearch = this._elem.output._elem.filename
    let hint = this._elem.output._elem.fileHint
    this.handleFile = () => {
      upload._elem.logname.remove()
	  	let name = upload._elem.input.files[0].name;
      upload._elem.span.innerText = name
	  	nameforsavedfile = `${name}_jlogrep.txt`
      let count = 0
      n.clear(upload._elem.fileslist)
      n.clear(hint)
      n.nest(upload._elem.div,upload._elem.files)
      upload._elem.filestitle.innerText = ''
      n.nest(upload._elem.filestitle,upload._elem.placeholder)
  		sendFile(upload._elem.input.files[0])
      .then(([c,files]) => {
        count = c
        n.nest(upload._elem.fileslist,parseFilesList(files))
        let hintslist = parseFilesHint(files)
        hintslist.forEach(i=>
          i.elem.addEventListener('click',()=>{
            filesearch.value = i.name
          })
        )
        filesearch.addEventListener('keyup',()=>{
          if (!filesearch.value) {
            hintslist.forEach(i=>i.appear())
            return
          }
          hintslist.forEach(i=>i.check(filesearch.value))
        })
        n.nest(hint,hintslist)
      })
      .catch(err=>console.log(err))
      .finally(()=>{
        upload._elem.placeholder.remove()
        upload._elem.filestitle.innerText = `file index (${count} files)`
      })
	  }
		this._elem.input.button.addEventListener("click",this.scan);
		this._elem.output.search.addEventListener("click",this.search);
    upload.input.addEventListener("change",this.handleFile) 
	}

	remove() {
		this._elem.input.button.removeEventListener("click",this.scan);
		this._elem.output.search.removeEventListener("click",this.search);
		this._elem.output.remove();
		this._elem.input.remove();
	}
	run() {
		(document.activeElement == this._elem.output._elem.regex ||
		document.activeElement == this._elem.output._elem.filename) ?
			this.search() : this.scan()
	}
	get input() {return this._elem.input}
  get upload() {return this._elem.input._elem.uploadlogs.input}
	get output() {return this._elem.output}
}

// most important input!
class UploadLogs {
	_elem = {}
	constructor() {
		this._elem = {
			div: n.make({tag:"div", class:"uploadlogs"}),
			label: n.make(lib.label),
			span: n.make(lib.span,"upload logs"),
			input: n.make({tag: "input",attr: [{type:"file"},{name:"bugreport"}]}),
			logname: n.make({tag: "div",class: "logname"}),
			sample: n.make(lib.p,"..."),
      files: n.make({tag:"details",class:"files"}),
      filestitle: n.make(lib.summary),
      fileslist: n.make({tag:"div",class:"fileslist"}),
      placeholder: n.make(lib.span,"loading")
		}
		n.nest(this._elem.div,
			n.nest(this._elem.label,[
				this._elem.span,
				this._elem.input]))
		n.nest(this._elem.logname,
			this._elem.sample)
    n.nest(this._elem.files,[
      this._elem.filestitle,
      this._elem.fileslist
    ])
	}
	get elem() {return this._elem.div}
	get input() {return this._elem.input}
	get output() {return this._elem.input.files[0]}
}

class Workspace {
	_elem = {}
	_scans = new Set([
		new OutputPair("Scan #1")
	]);
	_active
	constructor(text) {
		this._elem = {
			div: n.make({tag: "div", class: "workspace"}),
			left: n.make({tag: "div", class: "left"}),
			line: n.make({tag: "div", class: "line"}),
			right: n.make({tag: "div", class: "right"}),
			inputs: n.make({tag: "div", class: "input-forms"}),
			menu: new InputMenu(),
			window: new Window(text),
			delete: n.make({tag: "div", class: "deleteW"},``),
		}
		n.nest(this._elem.div,[
			n.nest(this._elem.left,[
				this._elem.menu,
				this._elem.inputs]),
			this._elem.line,
			this._elem.right
			])
		n.nest(this._elem.window.header,
			this._elem.delete)
		this._resizeLine();
		this._scans.forEach(i => {
			const gotoActive = (event) => {
				event.preventDefault();
				this.activate(i);
			}
			i.input.go.addEventListener("click",gotoActive);

			const activate = () => this.activate(i);
			i.input.button.addEventListener("click",activate);

			const deleteScan = (event) => {
				event.preventDefault();
				this._scans.delete(i);
				i.input.go.removeEventListener("click",gotoActive);
				i.input.button.removeEventListener("click",activate);
				i.input.delete.removeEventListener("click",deleteScan);
				i.remove()
				this.activate(this._scans.values().next().value);
			}
			i.input.delete.addEventListener("click",deleteScan);
		})
		this._elem.menu._buttons.expand.elem.addEventListener("click", (event) => {
			this._elem.menu._buttons.expand.output ? this.windowed() : this.fullscreen()
			event.preventDefault();
		})
		this._elem.menu._buttons.options.elem.addEventListener("click", (event) => {
			this._elem.menu._buttons.options.output ?
        this._elem.left.setAttribute('style','--optional: flex;') :
        this._elem.left.setAttribute('style','--optional: none;')
		})
		this.windowed();
		this._elem.menu._buttons.scan.addEventListener("click", () => {
				let newPair = new OutputPair(`Scan #${this._scans.size + 1}`)
				this._scans.add(newPair);
				newPair.input.delete.addEventListener("click",(event) => {
					event.preventDefault();
					newPair.remove()
					this._scans.delete(newPair);
					this.activate(this._scans.values().next().value);
				});
				newPair.input.go.addEventListener("click",(event) => {
					event.preventDefault();
					this.activate(newPair);
				});
				newPair.input.button.addEventListener("click",() => {
					this.activate(newPair);
				});
				n.nest(this._elem.inputs,newPair.input)
				newPair.input.appear()
				n.nest(this._elem.right,newPair.output)
				this.activate(newPair);
		})
		this.render();
		this.activate(this._scans.values().next().value);
	}
	_resizeLine() {
		this._elem.line.onmousedown = dragMouseDown;
		let [pos1,pos2] = [0,0],
			grid = this._elem.div,
			left = this._elem.left,
			line = this._elem.line,
			rem = parseFloat(getComputedStyle(document.body).fontSize)
		let minwidth = 16*rem,
			width = 20*rem
		function dragMouseDown(e) {
			e = e || window.event;
			e.preventDefault();
			pos2 = e.clientX;
			width = left.clientWidth;
			line.classList.add("drag");
			document.onmouseup = closeDragElement;
			document.onmousemove = elementDrag;
		}
		function elementDrag(e) {
			e = e || window.event;
			e.preventDefault();
			pos1 = pos2 - e.clientX;
			pos2 = e.clientX;
			width -= pos1;
			if (width<minwidth){
			(pos1>0)&&
				(grid.style.gridTemplateColumns = `0px 4px 1fr`);
			(pos1<0)&&
				(grid.style.gridTemplateColumns = `${minwidth}px 4px 1fr`);
			} else {grid.style.gridTemplateColumns = `${width}px 4px 1fr`;}
		}
		function closeDragElement() {
			line.classList.remove("drag");
			document.onmouse = null;
			document.onmousemove = null;
		}
	}
	render() {
		n.clear(this._elem.inputs)
		n.clear(this._elem.right)
		this._scans.forEach((i) => {
		n.nest(this._elem.inputs,i.input)
		i.input.appear()
		n.nest(this._elem.right,i.output)
		})
	}
	windowed() {
		if (this._elem.window.elem.parentElement == body) return;
		n.nest(body,this._elem.window);
		n.swap(this._elem.div,this._elem.window.content);
    this._elem.menu._buttons.expand.output = false;
	}
	fullscreen() {
		if (this._elem.div.parentElement == body) return;
		n.swap(this._elem.div,body);
		this._elem.window.elem.remove();
    this._elem.menu._buttons.expand.output = true;
	}
	activate(pair) {
		this._active = pair
		this._scans.forEach((i) => {
			i.input.button.classList.remove("active");
		})
		pair&&pair.input.button.classList.add("active")&
		this._elem.right.offsetWidth&
		this._elem.right.setAttribute("style",
			`--bottom: ${Array.from(this._elem.right.children).indexOf(pair.output.elem)}00%`)
	}
	get elem() {return this._elem.div}
	get scans() {return this._scans}
	get width() {return this._elem.left.clientWidth}
	get filedialog() {return this._active.upload}
	get active() {return this._active}
	get delete() {return this._elem.delete}
	get window() {return this._elem.window}
}
