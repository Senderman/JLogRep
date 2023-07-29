// error (name, count, tags, examples) -> example (name, content) -> content

function parseTime(utime) {
    if (!utime) return ['','']
	let dt = new Date(utime)
	let [dd,mm,yyyy,hours,minutes,seconds,millisec] = [
		('0' + dt.getUTCDate()).slice(-2),
		('0' + (dt.getUTCMonth() + 1)).slice(-2),
		dt.getUTCFullYear(),
		('0' + dt.getUTCHours()).slice(-2),
		('0' + dt.getUTCMinutes()).slice(-2),
		('0' + dt.getUTCSeconds()).slice(-2),
		(dt.getUTCMilliseconds() + '00').slice(0,3)
	]
    return [
        `${dd}.${mm}.${yyyy} `,
        `${hours}:${minutes}:${seconds}.${millisec} `
    ]
}
function parseLog(date,content) {
	let [d,t] = parseTime(date)
	let joiner = content.length < 3 ? ', ' : '\n'
	let lines = content.join(joiner)
	return `${d}${t}${lines}`
}

// clipboard
function copyOnClick(problems,type,setShow = null) {
	let d = []
	problems.forEach(i=>d.push(setShow?i.getExamples(setShow):i.getExamples()))
	navigator.clipboard.writeText(parseForCopy(d,type))
}

function parseForCopy(data,type) {
	if (type == 'content'){
		let copy = []
		function parseContent(example) {
			example.examples.forEach((i) => {
				i.contents.forEach((k) => {
					copy.push(`${k.lines.join('\n')}`)
		})})}
		data.forEach((i)=>parseContent(i))
		return copy.join('\n')
	} else {
	let codestart = {
			jira: "{code:java}",
			mattermost: "```",
			plain: "",
		},
		codeend = {
			jira: "{code}",
			mattermost: "```",
			plain: "-----",
		},
		bold = {
			jira: "*",
			mattermost: "**",
			plain: ""
		}
	function parseExample(example) {
		let copy = []
		example.forEach((i)=>{
			copy.push(`File: ${i.file}`)
			copy.push(codestart[type])
			i.contents.forEach((k)=>{
				copy.push(parseLog(k.date,k.lines))
			})
			copy.push(codeend[type])
		})
		return copy.join('\n')
	}
	let copy = []
	data.forEach((i)=>copy.push(
//--- this is how report will looks like ---
`${bold[type]}${i.name}${bold[type]}
Found: ${i.count}

${parseExample(i.examples)}`
// -----------------------------------------
	))
	return copy.join('\n\n');
	}
}

// save
const fileurl = document.createElement('a');
document.body.appendChild(fileurl);
fileurl.style.display = 'none';
function download(filename, text) {
    fileurl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    fileurl.setAttribute('download', filename);
    fileurl.click();
} 
function parseForSave(problems,setShow=null) {
	let save = []
	function parseExample(example) {
		let copy = []
		example.forEach((i) => {
			copy.push("----")
			copy.push(`File: ${i.file}`)
			i.contents.forEach((k)=>{
				copy.push(parseLog(k.date,k.lines))})
		})
		return copy.join('\n')
	}
	problems.forEach(i=>{
		let e = setShow?i.getExamples(setShow):i.getExamples()
		save.push(`${e.name}\nFound: ${e.count}\n\n${parseExample(e.examples)}`)})
	return save.join('\n====================\n')
}

// display
class Example {
	_elem = {}
	_parseContent(conts) {
		let a = []
		conts.forEach((i) => {
			a.push(n.make(lib.li,parseLog(i.date,i.lines)))})
		return a
	}
	_update() {
		n.nest(this._elem.div,[this._elem.filename,this._elem.list])
		n.nest(this._elem.list,this._elem.content)
	}
	show(b=null) {
		n.clear(this._elem.div)
		n.clear(this._elem.list)
		if (!b||(b>=this._elem.content.length)){this._update();return this._elem.div}
		if (b==0)return this._elem.div;
		n.nest(this._elem.div,[this._elem.filename,this._elem.list])
		n.nest(this._elem.list,this._elem.content.slice(-b))
		return this._elem.div
	}
	constructor(data) {
		this._elem = {
			div: n.make({tag:"div",class:"example"}),
			filename: n.make(lib.div,data.file),
			list: n.make(lib.ul),
			content: this._parseContent(data.contents)
		}
	}
	get logCount() {return this._elem.content.length}
	get elem() {return this._elem.div}
}

class Summary {
	_elem = {}
	_parseFilters(ts) {
		let t = []
		ts.forEach((i) => t.push(n.make({tag:"span",class:"filter"},i)))
		return t
	}
	constructor(data) {
		this._elem = {
			summary: n.make(lib.summary),
			checkbox: new Checkbox("selector"),
			span: n.make(lib.span,data.name),
			count: n.make({tag:"span",class:"count"},data.count),
			tags: n.make({tag:"span",class:"tag"},data.tag),
			filters: this._parseFilters(data.filters),
			copy: n.nest(n.make({tag:"span",class:"copy"}),n.make(lib.span,'')),
			copymenu: new CopyMenu()
		}
		n.nest(this._elem.summary,[this._elem.checkbox,this._elem.span])
		n.nest(this._elem.span,this._elem.count)
		n.nest(this._elem.span,this._elem.tags)
		n.nest(this._elem.span,this._elem.filters)
		n.nest(this._elem.span,n.nest(this._elem.copy,this._elem.copymenu))
	}
	get elem() {return this._elem.summary}
	get checked() {return this._elem.checkbox.output}
	get copymenu() {return this._elem.copymenu.type}
}

class Problem {
	_elem = {}
	_data = {}
	_parseExample(examples) {
		let a = []
		examples.forEach((i) => a.push(new Example(i)))
		return a
	}
	getTime() {
		let t = []
		this._data.examples.forEach(i => {
			i.contents.forEach(j=>j.date&&t.push(j.date))
		});
		return t
	}
	getExamples(show = this.show) {
		if (show==0)return {};
		if (show>=this.count)return this._data;
		let data = {
			count: this._data.count,
			name: this._data.name,
			examples: null,
		}
		let dif = this.count-show
		let [count,examples] = [0,[]]
		this._data.examples.forEach(i=>{
			count += i.contents.length
			if (count>dif){
				examples.push({contents:i.contents.slice(dif-count),file:i.file})
			}
		})
		data.examples = examples
		return data
	}
	showExamples(show = this.show) {
		this.setShow = show
		n.clear(this._elem.details);
		n.nest(this._elem.details,this._elem.summary);
		if (show==0)return;
		if (show>=this.count){
			this._elem.examples.forEach(i=>n.nest(this._elem.details,i.show()))
		} else {
		let dif = this.count-show
		let count = 0
		this._elem.examples.forEach(i=>{
			count += i.logCount
			if (count>dif){n.nest(this._elem.details,i.show(count-dif))}
		})
	}}
	constructor(data,show,open) {
		this._data = data
		this.count = data.examplesSize
		this.show = data.show
		this.setShow = data.show
		this._elem = {
		details: n.make({tag:"details",
			class:`problem${data.shownAlways?" urgent":''}`,
			attr: open&&{open:''}}),
		summary: new Summary(data),
		examples: this._parseExample(data.examples)
		}
		show?this.showExamples(show):this.showExamples()
		Object.entries(this._elem.summary.copymenu).map(([type,elem]) => {
			const copyExamples = (event) => {
				event.preventDefault();
				navigator.clipboard.writeText(parseForCopy([this.getExamples(this.setShow)],type))
			}
			elem.listener = copyExamples
			elem.elem.addEventListener("click", elem.listener)
		})
	}
	open() {this._elem.details.setAttribute("open",'')}
	close() {this._elem.details.removeAttribute("open")}
	remove() {
		Object.values(this._elem.summary.copymenu).forEach(i=>
			i.elem.removeEventListener("click", i.listener))
		this.data = null
		this._elem.details.remove()
	}
	get elem() {return this._elem.details}
	get checked() {return this._elem.summary.checked}
}

function parseResult(data,output,clear=true) {
	clear&&output.problemClear()
	data.forEach(i => {
		let p = new Problem(i,output.show,output.open)
		clear?output.problemPush(p):output.problemUnshift(p)
	})
}

function parseFiles(files) {
	let categories = {}
  categories.__proto__.result = []
	categories.__proto__.assignPush = function(key,value) {
		this[key]?this[key].push(value):(this[key] = [value])
	}
	categories.__proto__.removeSingle = function(key,value) {
		if (value.length != 1) {
      this.result.push({name:key,single:false,files:value})
		  delete this[key]
      return
    }
		let file = value[0]
		delete this[key]
    let name = file.filename.split('_')
		if ((name.length != 1) && (key == name[0])) return
    this.result.push({name:file.filename,single:true,files:file})
	}
	files.forEach(i=>{
		let category = i.filename.split('_')
		category.length>=2 && categories.assignPush(category[0],i)
		categories.assignPush(i.filename.split('.')[0],i)
	})
	Object.entries(categories).forEach(([k,v])=>{
    categories.removeSingle(k,v)
  })
	return [files.length,categories.result]
}

class Files {
  constructor({name:name,single:single,files:files}) {
    if (single) {
      this.elem = n.make(lib.div,`${files.filename} (${this.parseSize(files.size)})`)
      return 
    }
    this.elem = n.make(lib.details)
    this.title = n.make(lib.summary,`${name}*`)
    this.list = []
    files.forEach(i=>{
      this.list.push(n.make(lib.div,`${i.filename} (${this.parseSize(i.size)})`))
    })
    n.nest(this.elem,this.title)
    n.nest(this.elem,this.list)
  }
  parseSize(bytes) {
    const kb = {abbr: 'KB',size:1024},
          mb = {abbr: 'MB',size:(1024*1024)}
    if (bytes < kb.size) return `${bytes}B`
    if (bytes >= mb.size) return `${Math.round(bytes/mb.size*10)/10}${mb.abbr}`
    if (bytes >= kb.size) return `${Math.round(bytes/kb.size*10)/10}${kb.abbr}`
  }
}

function parseFilesList(list) {
  let elements = []
  list.forEach(i=>elements.push(new Files(i)))
  return elements
}

class Hint {
  constructor(name) {
    this.elem = n.make(lib.div,name)
    this.name = name
  }
  check(userInput) {
    try {
        this.elem.style.display = (this.name.includes(userInput) || userInput == '*' ? 'block' : 'none')
    } catch {}
  }
  appear() {
    this.elem.style.display = 'block'
  }
}

function parseFilesHint(list) {
  let hints = []
  list.forEach(i=>hints.push(new Hint(i.name)))
  return hints
}
