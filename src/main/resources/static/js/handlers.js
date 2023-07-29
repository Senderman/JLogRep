// some code for warning windows
const message = n.make({tag:"div",class:"message",attr:{id:"error"}});
n.nest(body,message);

async function showMessage(json) {
	message.style.display = "block";
	n.clear(message);
	json.code && n.nest(message,
		n.nest(n.make(lib.div,"Code: "),
			n.make({tag:"span",attr:{style:"color: var(--green4)"}},json.code)))
	n.nest(message,
		n.nest(n.make(lib.div,"Error: "),
			n.make({tag:"span",attr:{style:"color: var(--violet)"}},json.error)))
	const fadeIn = message.animate(
		[
			{ top: "-4em", opacity: 0},
			{ top: "2em", opacity: 1},
		],
		{ duration: 300, fill: "forwards" }
	);
	await fadeIn.finished;
	fadeIn.commitStyles();
	fadeIn.pause();
	window.addEventListener("click",hideMessage)
	window.addEventListener("keydown",hideMessage)
}

async function hideMessage() {
	const fadeOut = message.animate(
		[
			{ top: "2em", opacity: 1},
			{ top: "-4em", opacity: 0},
		],
		{ duration: 300, fill: "forwards" }
	);
	await fadeOut.finished;
	fadeOut.commitStyles();
	fadeOut.pause();
	message.style.display = "none";
	window.removeEventListener("click",hideMessage)
	window.removeEventListener("keydown",hideMessage)
}

function handleErrors(r) {
	if (!r.ok) {
		console.log(r)
		r.json().then(text => showMessage(text))
	}
	return r
}

function appendIfDefined(formData, name, entry){
	entry && formData.append(name, entry)
}

function wait(ms = 1000) {
    return new Promise(resolve => {
          setTimeout(resolve, ms);
    });
}

async function singlePollForTask(taskId, resultSuffix) {
    const response = await fetch(`/result-${resultSuffix}?taskId=${taskId}`)
    if (response.ok)
        return response
    else
        return Promise.reject(response)
}

async function pollUntilTaskIsComplete(url, resultSuffix, params) {
    let taskPromise = await fetch(url, params)
        if (!taskPromise.ok){
            return Promise.reject(taskPromise)
        }
    	let task = await taskPromise.json()
    	let taskIsReady = task.ready
    	let taskId = task.taskId;
    	let result = task.result
    	while (!taskIsReady) {
    	    try {
    	        taskPromise = await singlePollForTask(taskId,resultSuffix)
    	        task = await taskPromise.json()
    	        taskIsReady = task.ready
                result = task.result
    	    } catch (reason) {
    	        return Promise.reject(reason)
    	    }
    	    await wait()
    	}
    	return result
}

// main function to start scan and get output
async function sendScan(outputPair){
	let formData = new FormData()
	let input = outputPair.input.output
	if (!input.bugreport) {
		showMessage({error: "Log file required!"});
		return
	}

	formData.append('bugreport', input.bugreport)
	appendIfDefined(formData, 'rules', input.rules)
	appendIfDefined(formData, 'dateFormat', input.dateformat)

	let headers = new Headers()
	appendIfDefined(headers, 'when', input.date)
	appendIfDefined(headers, 'year', input.year)
	appendIfDefined(headers, 'interval', input.interval)
	appendIfDefined(headers, 'tags', input.tags)
	appendIfDefined(headers, 'filters', input.filters)
	outputPair.output.loadStart();

	let result
	try {
	    result = await pollUntilTaskIsComplete('/scan', 'problems', { method: 'POST', body: formData, headers: headers })
		if (!result) {
			showMessage({error: "Nothing was found"});
			return
		}
    n.clear(outputPair.output.result)
    parseResult(result,outputPair.output)
	} catch (reason) {
	    let err = await reason
	    handleErrors(err)
	} finally {
	    outputPair.output.loadEnd()
	}
}

async function sendRegex(outputPair){
	let formData = new FormData()
	let input1 = outputPair.output
	let input2 = outputPair.input.output
	if (!input2.bugreport) {
		showMessage({error: "Log file required!"});
		return
	}

	formData.append('bugreport', input2.bugreport)
	appendIfDefined(formData, 'dateFormat', input2.dateformat)

	let headers = new Headers()
	appendIfDefined(headers, 'file', input1.filename)
	appendIfDefined(headers, 'regex', input1.regex)
	appendIfDefined(headers, 'when', input2.date)
	appendIfDefined(headers, 'interval', input2.interval)
	appendIfDefined(headers, 'year', input2.year)
	appendIfDefined(headers, 'filters', input2.filters)
	input1.loadStart();

	let result
    try {
        result = await pollUntilTaskIsComplete('/regex', 'problems', { method: 'POST', body: formData, headers: headers })
		if (!result[0]) {
			showMessage({error: "Nothing was found"});
			return
		}
        parseResult(result,input1,false)
    } catch (reason) {
        let err = await reason
        handleErrors(err)
    } finally {
        input1.loadEnd()
    }
}

async function sendFile(bugreport){
	let formData = new FormData()
	formData.append('bugreport',bugreport)
	let result
	try {
	    result = await pollUntilTaskIsComplete('/files', 'files', { method: 'POST', body: formData})
	    return await parseFiles(result)
	} catch (reason) {
	    let err = await reason
	    console.log(err)
	    return [0, null]
	}
}
