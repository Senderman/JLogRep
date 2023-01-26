//class for group of inputs
class inputForm {
	_data = []
	constructor(elem,elem2) {
		this._form = elem;
		this._date = elem.querySelector('[date]');
		this._year = elem.querySelector('[year]');
		this._interval = elem.querySelector('[interval]');
		this._show = elem.querySelector('[show]');
		this._filters = elem.querySelector('[filters]');
		this._tagboxes = elem.querySelector('[tags]');
		this._rules = elem.querySelector('[rule]');
		this._dateFormat = elem.querySelector('[dateFormat]');
		this._button = elem.querySelector('[scan]');
		this._summary = elem.querySelector('[title]');
		this._workspace = elem2;
		this._result = elem2.querySelector('[result]');
		this._menu = elem2.querySelector('[topmenu]');
		this._expand = elem2.querySelector('[expand]');
		this._save = elem2.querySelector('[savetofile]');
		this._saveselected = elem2.querySelector('[saveseltofile]');
		this._unselect = elem2.querySelector('[unselect]');
		this._load = elem2.querySelector('[load]');
	}
	//getters for fetch
	get date() {return this._date.value}
	get year() {return this._year.value}
	get interval() {return this._interval.value}
	get show() {return this._show.value}
	get filters() {
		let arr = Array.from(this._filters.querySelectorAll('input[type=checkbox]'))
		let checked = []
		for (i in arr) {
			arr[i].checked && checked.push(arr[i].getAttribute('tag'))
		}
		return checked.length != 0 ? checked : undefined;
	}
	get tags() {
		let arr = Array.from(this._tagboxes.querySelectorAll('input[type=checkbox]'))
		let checked = []
		for (i in arr) {
			!arr[i].checked && checked.push(arr[i].getAttribute('tag'))
		}
		return checked.length != 0 ? checked : undefined;
	}
	get rules() {return this._rules.querySelector('[file]').files[0]}
	get dateFormat() {return this._dateFormat.querySelector('[file]').files[0]}
	
	get tagboxes() {return this._tagboxes}
	get tagfilters() {return this._filters}
	get eyebutton() {return this._tagboxes.querySelector('[eye]')}
	get workspace() {return this._workspace}
	get result() {return this._result}
	get expand() {return this._expand.checked}
	
	// get data from server
	set data(value) {this._data = value}
	
	//refresh all
	refreshAll() {
		this._date.value = '';
		this._year.value = new Date().getFullYear();
		this._interval.value = '';
		this._show.value = '';
		sendRules(null,this);
		getFilters(this);
		this._rules.querySelector('[file]').value = '';
		this._rules.querySelector('[label]').innerText = "upload rules.yml";
		this._dateFormat.querySelector('[file]').value = '';
		this._dateFormat.querySelector('[label]').innerText = "upload dateformat.yml";
	}
	//add listeners
	start() {
		this._rules.querySelector('[file]').addEventListener("change",
		() => {
			this._rules.querySelector('[label]').innerText = this._rules.querySelector('[file]').files[0].name;
			sendRules(this._rules.querySelector('[file]').files[0],this);
		});
		this._dateFormat.querySelector('[file]').addEventListener("change",
		() => {
			this._dateFormat.querySelector('[label]').innerText = this._dateFormat.querySelector('[file]').files[0].name;
		});
		this._rules.querySelector('[clear]').addEventListener("click", // refresh rules button
		() => {
			this._rules.querySelector('[file]').value = '';
			this._rules.querySelector('[label]').innerText = "upload rules.yml";
			sendRules(null,this);
		})
		this._dateFormat.querySelector('[clear]').addEventListener("click", // refresh dateformat button
		() => {
			this._dateFormat.querySelector('[file]').value = '';
			this._dateFormat.querySelector('[label]').innerText = "upload rules.yml";
		})
		this._button.addEventListener("click", // big button to scan
		() => {
			sendScan(this);
			changeWorkspace(this._workspace);
			this._button.classList.add("active");
		})
		this._summary.querySelector('[goto]').addEventListener("click", // click on summary
		() => {
			event.preventDefault();
			changeWorkspace(this._workspace);
			this._button.classList.add("active");
		})
		this._expand.addEventListener("change",
			() => {
			let details = Array.from(this._result.getElementsByTagName("details"))
			if (this._expand.checked) {
				for (i in details) {
				details[i].open = "true"
				}
			} else {
				for (i in details) {
				details[i].removeAttribute('open')
				}
			}
		})
		this._save.addEventListener("click", // click on save
		() => {
			download(nameforsavedfile, parseSave(this._data));
		})
		this._saveselected.addEventListener("click", // click on saveselected
		() => {
			let errors = []
			let arr = Array.from(this._result.querySelectorAll('input[type=checkbox]'))
			for (i in arr) {
				arr[i].checked && errors.push(arr[i].getAttribute('saveerror'));
			}
			let cropdata = []
			for (i in errors) { cropdata.push(this._data[errors[i]])}
			download(nameforsavedfile, parseSave(cropdata));
		})
		this._unselect.addEventListener("click", // click on unselect
		() => {
			let arr = Array.from(this._result.querySelectorAll('input[type=checkbox]'))
			for (i in arr) {
				arr[i].checked = false;
			}
		})
	}
	toggleTags(bool) {
		const arr = Array.from(this._tagboxes.getElementsByClassName('tagbox'))
		for (i in arr) {
			arr[i].previousElementSibling.checked = bool;
		}
	}
	load() {
		this._load.classList.toggle("flex");
	}
}

class inputRegex {
	_data = []
	constructor(elem,elem2) {
		this._form = elem;
		this._date = elem.querySelector('[date]');
		this._year = elem.querySelector('[year]');
		this._interval = elem.querySelector('[interval]');
		this._filters = elem.querySelector('[filters]');
		this._dateFormat = elem.querySelector('[dateFormat]');
		this._button = elem.querySelector('[scan]');
		this._summary = elem.querySelector('[title]');
		this._workspace = elem2;
		this._result = elem2.querySelector('[result]');
		this._menu = elem2.querySelector('[topmenu]');
		this._expand = elem2.querySelector('[expand]');
		this._save = elem2.querySelector('[savetofile]');
		this._saveselected = elem2.querySelector('[saveseltofile]');
		this._unselect = elem2.querySelector('[unselect]');
		this._regexinput = elem2.querySelector('[regexinput]');
		this._regex = elem2.querySelector('[regex]');
		this._filepart = elem2.querySelector('[filepart]');
		this._scan = elem2.querySelector('[scan]');
		this._load = elem2.querySelector('[load]');
	}
	//getters for fetch
	get date() {return this._date.value}
	get year() {return this._year.value}
	get interval() {return this._interval.value}
	get filters() {
		let arr = Array.from(this._filters.querySelectorAll('input[type=checkbox]'))
		let checked = []
		for (i in arr) {
			arr[i].checked && checked.push(arr[i].getAttribute('tag'))
		}
		return checked.length != 0 ? checked : undefined;
	}
	get dateFormat() {return this._dateFormat.querySelector('[file]').files[0]}
	get tagfilters() {return this._filters}
	get workspace() {return this._workspace}
	get result() {return this._result}
	get expand() {return this._expand.checked}
	get regexinput() {return this._regexinput}
	get file() {return this._filepart.value}
	get regex() {return this._regex.value}
	
	// get data from server
	set data(value) {this._data = value}
	
	//refresh all
	refreshAll() {
		this._date.value = '';
		this._filepart.value = '';
		this._regex.value = '';
		this._year.value = new Date().getFullYear();
		this._interval.value = '';
		getFilters(this);
		this._dateFormat.querySelector('[file]').value = '';
		this._dateFormat.querySelector('[label]').innerText = "upload dateformat.yml";
	}
	//add listeners
	start() {
		this._dateFormat.querySelector('[file]').addEventListener("change",
		() => {
			this._dateFormat.querySelector('[label]').innerText = this._dateFormat.querySelector('[file]').files[0].name;
		});
		this._dateFormat.querySelector('[clear]').addEventListener("click", // refresh dateformat button
		() => {
			this._dateFormat.querySelector('[file]').value = '';
			this._dateFormat.querySelector('[label]').innerText = "upload rules.yml";
		})
		this._button.addEventListener("click", // goto
		() => {
			changeWorkspace(this._workspace);
			this._button.classList.add("active");
		})
		this._scan.addEventListener("click", // goto
		() => {
			sendRegex(this);
		})
		this._summary.querySelector('[goto]').addEventListener("click", // click om summary
		() => {
			event.preventDefault();
			changeWorkspace(this._workspace);
			this._button.classList.add("active");
		})
		this._expand.addEventListener("change",
			() => {
			const details = Array.from(this._result.getElementsByTagName("details"))
			if (this._expand.checked) {
				for (i in details) {
				details[i].open = "true"
				}
			} else {
				for (i in details) {
				details[i].removeAttribute('open')
				}
			}
		})
		this._save.addEventListener("click", // click on save
		() => {
			download(nameforsavedfile, parseSave(this._data));
		})
		this._saveselected.addEventListener("click", // click on saveselected
		() => {
			let errors = []
			let arr = Array.from(this._result.querySelectorAll('input[type=checkbox]'))
			for (i in arr) {
				arr[i].checked && errors.push(arr[i].getAttribute('saveerror'));
			}
			let cropdata = []
			for (i in errors) { cropdata.push(this._data[errors[i]])}
			download(nameforsavedfile, parseSave(cropdata));
		})
		this._unselect.addEventListener("click", // click on unselect
		() => {
			let arr = Array.from(this._result.querySelectorAll('input[type=checkbox]'))
			for (i in arr) {
				arr[i].checked = false;
			}
		})
	}
	toggleTags(bool) {
		const arr = Array.from(this._tagboxes.getElementsByClassName('tagbox'))
		for (i in arr) {
			arr[i].previousElementSibling.checked = bool;
		}
	}
	load() {
		this._load.classList.toggle("flex");
	}
}

window.onload = () => {
	document.getElementById("uploadlogs").value = ''
}

// some code for warning windows
function showMessage(json) {
	let mess = document.getElementById("error");
	mess.classList.remove("topmes")
	let html = []
	html.push(json.code && `<div>Code: <span style="color: var(--green4)">${json.code}</span></div>`);
	html.push(`<div>Error: <span style="color: var(--violet)">${json.error}</span></div>`);
	mess.innerHTML = html.join('');
	mess.offsetWidth
	mess.classList.add("topmes")
}
function handleErrors(r) {
	if (!r.ok) {
		r.json().then(text => showMessage(text))
	}
	return r
}

// main function to start scan and get output
function sendScan(iform){
	formData = new FormData()
	let bugreport = document.getElementById("uploadlogs").files[0]
	if (!bugreport) {
		showMessage({error: "Log file required!"});
		return
	}
	formData.append('bugreport', bugreport)
	appendIfDefined(formData, 'rules', iform.rules)
	appendIfDefined(formData, 'dateFormat', iform.dateFormat)
	appendIfDefined(formData, 'date', iform.date)
	appendIfDefined(formData, 'year', iform.year)
	appendIfDefined(formData, 'interval', iform.interval)
	appendIfDefined(formData, 'show', iform.show)
	appendIfDefined(formData, 'tags', iform.tags)
	appendIfDefined(formData, 'filters', iform.filters)
	iform.load();
	fetch('/scan', {
		method: 'POST',
		body: formData
	})
	.then(handleErrors)
	.then(r => {
		r.json().then(r => {
			iform.data = r;
			parseResult(r,iform)})
	})
	.catch(err => showMessage({error: err}))
	.finally(() => iform.load())
}

function sendRegex(iform){
	formData = new FormData()
	let bugreport = document.getElementById("uploadlogs").files[0]
	if (!bugreport) {
		showMessage({error: "Log file required!"});
		return
	}
	formData.append('bugreport', bugreport)
	appendIfDefined(formData, 'regex', iform.regex)
	appendIfDefined(formData, 'file', iform.file)
	appendIfDefined(formData, 'dateFormat', iform.dateFormat)
	appendIfDefined(formData, 'date', iform.date)
	appendIfDefined(formData, 'interval', iform.interval)
	appendIfDefined(formData, 'year', iform.year)
	appendIfDefined(formData, 'filters', iform.filters);
	iform.load();
	fetch('/regex', {
		method: 'POST',
		body: formData
	})
	.then(handleErrors)
	.then(r => {
		r.json().then(r => {
			iform.data = r;
			parseResult(r,iform)})
	})
	.catch(err => showMessage({error: err}))
	.finally(() => iform.load())
}

function appendIfDefined(formData, name, entry){
	entry && formData.append(name, entry)
}

// send rules.yml or null to get verified tags to parse
function sendRules(uploadedrules,iform){
	formData = new FormData()
	formData.append('rules', uploadedrules)
	fetch('/rules', {
		method: 'POST',
		body: formData
	})
	.then(handleErrors)
	.then(r => r.json().then(rj => parseRules(rj,iform)))
	.catch(err => console.log(err))
}

// get filters to parse
function getFilters(iform){
	fetch('/filters', {
		method: 'GET',
	})
	.then(handleErrors)
	.then(r => r.json().then(rj => parseFilters(rj,iform)))
	.catch(err => console.log(err))
}
