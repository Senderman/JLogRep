let version
const titleHead = document.getElementById("head-title")
const titlePlace = document.getElementById("title")
const versionPlace = document.getElementById("version")
const creditsPlace = document.getElementById("credits")

function reloadOnNew(verServer, verClient) {
	verServer != verClient && window.location.reload(true)
}

async function checkVersion(){
	try {
	    let info = await getAppInfo()
		reloadOnNew(info.version, version)
	} catch(error) {
		console.log(error)
	}
}

async function getAppInfo(){
    const resp = await fetch('/appinfo', { method: 'GET' })
    return resp.json()
}

async function initVersion(){
    info = await getAppInfo()
    version = info.version
}

async function showAppInfo(){
    let info = await getAppInfo()
    document.title = info.name
    titlePlace.innerText = info.name
    versionPlace.innerText = info.version
    creditsPlace.innerText = info.authors
}

initVersion()
showAppInfo()
window.setInterval(checkVersion, 120000);