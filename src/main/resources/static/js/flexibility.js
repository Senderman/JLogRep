// change input's colum width

const vrt = document.getElementById('vrt');
const col = document.getElementById('grid');
function DragCol(elem) {
	let pos1 = 0;
	elem.onmousedown = dragMouseDown;
	function dragMouseDown(e) {
		e = e || window.event;
		e.preventDefault();
		document.onmouseup = closeDragElement;
		document.onmousemove = elementDrag;
	}
	function elementDrag(e) {
		e = e || window.event;
		e.preventDefault();
		pos1 = e.clientX;
		col.style.gridTemplateColumns = pos1 + "px 4px 1fr";
	}
	function closeDragElement() {
		document.onmouse = null;
		document.onmousemove = null;
	}
}
DragCol(vrt)

// workspace swap

function changeWorkspace(wrksp) {
	const rightpanel = document.getElementById("workspaces");
	const leftpanel = document.getElementById("left-panel");
	const arr = Array.from(rightpanel.children);
	function moveVh(count){rightpanel.style.bottom = `${count}00vh`};
	for (i in arr) {
		arr[i].isSameNode(wrksp) && moveVh(i);
	}
	const arr2 = Array.from(leftpanel.querySelectorAll('[scan]'));
	for (i in arr2) {
		arr2[i].classList.remove("active");
	}
}

//default two inputs
let scans = []
scans.push(new inputForm(document.getElementById('input-forms').getElementsByClassName('input-form')[0],document.getElementById('workspace')));
scans[0].refreshAll();
scans[0].start();
let regexes = []

// bind enter for main scan
window.addEventListener("keyup", function(event) {
if (!event.ctrlKey && event.key === "Enter") {
event.preventDefault();
sendScan(scans[0]);}
});
// ctrl to open file dialog
window.addEventListener("keyup", function(event) {
if (event.ctrlKey && event.key === "Enter") {
event.preventDefault();
document.getElementById("uploadlogs").click();}
});

//create new inputs
const adder = document.createElement("div");
adder.classList.add("add");
adder.innerHTML = `<div addscan class="button">add scan</div>
<div addsearch class="button">add search</div>`
document.getElementById('input-forms').appendChild(adder);

adder.addEventListener("click",
() => {document.getElementById('input-forms').classList.remove("oneonly")})

adder.querySelector('[addscan]').addEventListener("click",
() => {
let inputScan = document.createElement("div");
inputScan.classList.add("input-form");
inputScan.innerHTML = `<details open>
				<summary title><span>Scan #${scans.length + 1}</span><span goto>-></span></summary>
				<div class="dateinput">
					<label>date/time</label>
					<input date type="datetime-local">
				</div>
				<div class="textinput">
					<label>year</label>
					<input year class="year" type="text">
				</div>
				<div class="textinput">
					<label>interval</label>
					<input interval type="text">
				</div>
				<div class="textinput">
					<label>show</label>
					<input show type="text">
				</div>
				<div class="rulestags">
					<div filters class="textinput"></div>
					<div tags class="textinput">
						<label class="checkbox">
							<input eye type="checkbox">
							<div class="eyebox"></div>
						</label>
					</div>
					<div rule class="uploadcontrol">
						<label class="upload">
							<span label>upload rules.yml</span>
							<input file type="file">
						</label>
						<span clear class="close"></span>
					</div>
				</div>
				<div dateFormat class="uploadcontrol">
					<label class="upload">
						<span label>upload dateformat.yml</span>
						<input file type="file">
					</label>
					<span clear class="close"></span>
				</div>
			</details>
			<div scan class="button">
				BIG BUTTON TO SCAN
			</div>`
document.getElementById('input-forms').appendChild(inputScan);
let newWorkspace = document.createElement("div");
newWorkspace.classList.add("workspace");
newWorkspace.innerHTML = `<div topmenu class="menu">
			<label class="checkbox">
				<input expand class="openbox" type="checkbox">
				<div class="toggler open"></div>
			</label>
			<div savetofile class="toggler selectaction">save</div>
			<div saveseltofile class="toggler selectaction">save selected</div>
			<div unselect class="toggler selectaction">unselect all</div>
		</div>
		<div result class="result"></div>
		<div load class="load message">
			<div class="animation"></div>
			<div class="text">loading</div>
		</div>`
document.getElementById('workspaces').appendChild(newWorkspace);

scans.push(new inputForm(inputScan,newWorkspace));
scans[scans.length - 1].refreshAll();
scans[scans.length - 1].start();
document.getElementById('input-forms').appendChild(adder);
})

adder.querySelector('[addsearch]').addEventListener("click",
() => {
let inputSearch = document.createElement("div");
inputSearch.classList.add("input-regex");
inputSearch.innerHTML = `<details open>
				<summary title><span>Search #${regexes.length + 1}</span><span goto>-></span></summary>
				<div class="dateinput">
					<label>date/time</label>
					<input date type="datetime-local">
				</div>
				<div class="textinput">
					<label>year</label>
					<input class="year" year type="text">
				</div>
				<div class="textinput">
					<label>interval</label>
					<input interval type="text">
				</div>
				<div class="rulestags">
					<div filters class="textinput"></div>
				</div>
				<div dateFormat class="uploadcontrol">
					<label class="upload">
						<span label>upload dateformat.yml</span>
						<input file type="file">
					</label>
					<i clear class="fa fa-times close"></i>
				</div>
			</details>
			<div scan class="button">
				MOVE TO REGEX
			</div>`
document.getElementById('input-forms').appendChild(inputSearch);
let newWorkspace = document.createElement("div");
newWorkspace.classList.add("workspace");
newWorkspace.classList.add("sp");
newWorkspace.innerHTML = `<div topmenu class="menu">
			<label class="checkbox">
				<input expand class="openbox" type="checkbox">
				<div class="toggler open"></div>
			</label>
			<div savetofile class="toggler selectaction">save</div>
			<div saveseltofile class="toggler selectaction">save selected</div>
			<div unselect class="toggler selectaction">unselect all</div>
		</div>
		<label regexinput class="regex">
			<input class="expression" placeholder="type your regex..." regex type="text">
			<input class="searchfile" placeholder="filename part" filepart type="text">
			<div scan class="button active">scan</div>
		</label>
		<div result class="result"></div>
		<div load class="load message">
			<div class="animation"></div>
			<div class="text">loading</div>
		</div>`
document.getElementById('workspaces').appendChild(newWorkspace);

regexes.push(new inputRegex(inputSearch,newWorkspace));
regexes[regexes.length - 1].refreshAll();
regexes[regexes.length - 1].start();
document.getElementById('input-forms').appendChild(adder);
})

