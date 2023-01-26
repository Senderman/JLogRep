// json parser
// error (name, count, tags, examples) -> example (name, content) -> content

// clipboard
function copyOnClick(data,type) {
	if (type == 'Wodate'){
		return function wodate(){
			let copy = []
			for (i in data.examples) {
				for (k in data.examples[i].contents) {
					copy.push(`${data.examples[i].contents[k].line.replaceAll('<br>', '\n')}`)
				}
			}
			navigator.clipboard.writeText(copy.join('\n'))
		}
	}
	return function parseCopy() {
		let codestart, codeend, bold
		switch(type) {
			case 'Jira':
				codestart = "{code:java}"
				codeend = "{code}"
				bold = "*"
			break;
			case 'MM':
				codestart = "```"
				codeend = "```"
				bold = "**"
			break;
			case 'Raw':
				codestart = ""
				codeend = "-----"
				bold = ""
			break
		}
		function parseExample(example) {
			let copy = []
			for (i in example) {
				copy.push(`File: ${example[i].file}`)
				copy.push(codestart)
				for (k in example[i].contents) {
					copy.push(`${example[i].contents[k].date} ${example[i].contents[k].line.replaceAll('<br>','\n')}`)
				}
				copy.push(codeend)
			}
			return copy.join('\n')
		}
		let text = `${bold}${data.name}${bold}\nFound: ${data.count}\n\n${parseExample(data.examples)}`;
		navigator.clipboard.writeText(text);
	}
}

// save
const fileurl = document.createElement('a');
document.body.appendChild(fileurl);
function download(filename, text) {
    fileurl.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    fileurl.setAttribute('download', filename);
    fileurl.style.display = 'none';
    document.body.appendChild(fileurl);
    fileurl.click();
} 
function parseSave(data) {
	let save = []
	function parseExample(example) {
		let copy = []
		for (i in example) {
			copy.push(`File: ${example[i].file}`)
			for (k in example[i].contents) {
				copy.push(`${example[i].contents[k].date} ${example[i].contents[k].line.replaceAll('<br>','\n')}`)
			}
			copy.push("----")
		}
		return copy.join('\n')
	}
	for (i in data) {
		save.push(`${data[i].name}\nFound: ${data[i].count}\n\n${parseExample(data[i].examples)}`)
	}
	return save.join('====================\n')
}

// for clicking on tag name inside the parsed problem
function tagClick(tag,iform) {
	return () => {
		let arr = Array.from(iform.tagboxes.getElementsByClassName('tagbox'))
		for (i in arr) {
			arr[i].previousElementSibling.checked = true;
		}
		iform.eyebutton.checked = true;
		iform.tagboxes.querySelectorAll(`input[tag=${tag}]`)[0].checked = false;
	}
}

// for result

function parseResult(data,iform) {
	let result = []

	function parseError(error,id) {
		return `<summary><label class="checkbox">
				<input saveerror=${id} class="openbox" type="checkbox">
				<div class="selector"></div>
			</label><span>${error.name}
	<span class='count'>${error.count}</span>${parseTags(error.tags)}${parseFilters(error.filters)}<span class='copy'><span></span>
	<div class='copymenu'>
	    <div copyerror=${id} type='MM'>Mattermost</div>
	    <div copyerror=${id} type='Jira'>Jira</div>
	    <div copyerror=${id} type='Raw'>Raw</div>
	    <div copyerror=${id} type='Wodate'>Raw w/o date</div>
	</div>
	</span>
	</span>
	</summary>
	${parseExamples(error.examples)}`
	}

	function parseExamples(example) {
		let html = []
		for (i in example) {
			html.push(`<div class='example'>
						<div class='filename'>${example[i].file}</div>
						<ul>${parseContents(example[i].contents)}</ul>
					</div>`)
		}
		return html.join('')
	}

	function parseTags(tags){
		let html = ["<span class='tag'>"]
		let arr = tags.join("</span><span class='tag'>");
		html.push(arr)
		html.push("</span>")
		return html.join('')
	}
	function parseFilters(filters){
		if (filters.length == 0) { return ''};
		let html = ["<span class='filter'>"]
		let arr = filters.join("</span><span class='filter'>");
		html.push(arr)
		html.push("</span>")
		return html.join('')
	}

	function parseContents(contents){
		let html = []
	    for (i in contents){
	        html.push(`<li>${contents[i].date} ${contents[i].line}</li>`)
	    }
	    return html.join('')
	}
	
	const preopen = iform.expand ? "open" : "";
	function parseShownAlways(shown) {
		return shown == true ? "urgent" : ""
	}
	for (i in data) {
		result.push(`<details class="${parseShownAlways(data[i].shownAlways)}" ${preopen}>${parseError(data[i],i)}</details>`)
	}
	iform.result.innerHTML = result.join('');
	//copy function
	inputs = Array.from(iform.result.querySelectorAll('[copyerror]'));
	for (i in inputs) {
		let copy = copyOnClick(
			data[inputs[i].getAttribute('copyerror')],
			inputs[i].getAttribute('type'));
		inputs[i].addEventListener("click",
		(event) => {
			event.preventDefault();
			copy();
		});
	}
	// tag check in details
	inputs = Array.from(iform.result.getElementsByClassName('tag'));
	for (i in inputs) {
		let tagcheck = tagClick(inputs[i].innerText,iform)
		inputs[i].addEventListener("click",
			(event) => {
			event.preventDefault();
			tagcheck();
		});
	}
}

// for rules

function parseRules(data,form) {
	for (i in data) {
		data[i] = `
		<label class='checkbox'>
			<input tag="${data[i]}" type="checkbox"/>
			<div class="tagbox" style="--tag: '${data[i]}'"></div>
		</label>`
	}
	data.unshift(`<label class="checkbox"><input eye type="checkbox"><div class="eyebox"></div></label>`)
	form.tagboxes.innerHTML = data.join('');
	form.eyebutton.addEventListener("change",
	() => {form.toggleTags(form.eyebutton.checked)}
	)
}

function parseFilters(data,form) {
	for (i in data) {
		data[i] = `
		<label class='checkbox'>
			<input tag="${data[i]}" type="checkbox"/>
			<div class="filterbox" style="--tag: '${data[i]}'"></div>
		</label>`
	}
	form.tagfilters.innerHTML = data.join('');
}

let nameforsavedfile = 'test.txt'
function parseLogName(name) { // parse logs file name
	let words = []
	let str
	if (name.includes('bugreport') == true) {
		str = name.substr(0,name.length - 4) // remove .zip
	} else {
		nameforsavedfile = `${name}_jlogrep.txt`
		return (`<p>${name}</p>`) }
	if (name.includes('_') == true) {
		words = str.split('_')
		words = [words[1],words.slice(2,5).join('.')]
	} else {
		words = str.split('-')
		words = [words[1],words[2]] 
	}
	nameforsavedfile = `${words[0]}_jlogrep.txt`
	return `<p>Device ID ${words[0]}<br/>
			Firmware Version ${words[1]}<br/>`;
}

document.getElementById("uploadlogs").addEventListener("change", // upload logs button
	() => {
	let name = document.getElementById("uploadlogs").files[0].name;
	let shortname = name.length >= 20 ? name.substr(0,20) + "..." : name;
	document.getElementById("uploadlogslabel").innerText = shortname;
	document.getElementById("logname").innerHTML = parseLogName(name);
})

