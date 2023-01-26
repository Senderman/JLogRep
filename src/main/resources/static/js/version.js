const version = document.getElementsByTagName('meta')['_version'].getAttribute("content")

function reloadOnNew(verServer, verClient) {
	verServer != verClient && window.location.reload(true)
}

function checkVersion(){
	fetch('/version', {
		method: 'GET'
	})
	.then(r => r.json().then(rj => reloadOnNew(rj.error,version)))
	.catch(err => console.log(err))
}

checkVersion()
window.setInterval(checkVersion, 120000);
