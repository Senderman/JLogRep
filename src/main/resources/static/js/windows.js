function dragElement(elmnt) {
	let [pos1,pos2,pos3,pos4] = [0,0,0,0];
	elmnt.children[0].onmousedown = dragMouseDown
	function dragMouseDown(e) {
		e = e || window.event;
		e.preventDefault();
		w.order(elmnt);
		[pos3,pos4] = [e.clientX,e.clientY];
		document.onmouseup = closeDragElement;
		document.onmousemove = elementDrag;
	}
	function elementDrag(e) {
		e = e || window.event;
		e.preventDefault();
		[pos1,pos2] = [pos3 - e.clientX,pos4 - e.clientY];
		[pos3,pos4] = [e.clientX,e.clientY];
		elmnt.style.top = `${elmnt.offsetTop - pos2}px`;
		elmnt.style.left = `${elmnt.offsetLeft - pos1}px`;
	}
	function closeDragElement() {
		document.onmouseup = null;
		document.onmousemove = null;
	}
}

var w = {
	order: function(win) {
		for (const i of document.getElementsByTagName('window')) {
			parseInt(i.style.zIndex) >= 3 && (i.style.zIndex = `${i.style.zIndex - 1}`);
		}
		win.style.zIndex = "5"
	},
	resize: function(win,width,height = null) {
		win.style.width = width
		win.style.height = height ? height : width
	},
	move: function(win,x,y = null) {
		win.style.left = x
		win.style.top = y ? y : x
	},
	center: function(win) {
		let b = document.getElementsByTagName('body')[0]
		win.style.left = `${(b.clientWidth - win.clientWidth)/2}px`
		win.style.top = `${(b.clientHeight - win.clientHeight)/2}px`
		win.style.zIndex = "5"
	}
}

class Window {
	_elem = {}
	constructor(text) {
		this._elem = {
			div: n.make({tag: "window"}),
			header: n.make({tag: "div", class: "header"},text),
			content: n.make({tag: "div", class: "content"}),
		}
		n.nest(this._elem.div,[
			this._elem.header,
			this._elem.content,
		])
		this._elem.div.style.zIndex = "4"
		dragElement(this._elem.div);
		this._elem.div.addEventListener("click", () => w.order(this._elem.div));
	}
	size(width,height = null) {
		this._elem.div.style.width = width
		this._elem.div.style.height = height ? height : width
	}
	position(x,y = null) {
		this._elem.div.style.top = y ? y : x
		this._elem.div.style.left = x
	}
	get elem() {return this._elem.div}
	get header() {return this._elem.header}
	get content() {return this._elem.content}
}

