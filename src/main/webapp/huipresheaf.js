/**
 * Functionality for presheaf.com
 * @author Vlad Patryshev
 * 3/16/2018
 */

const _ = (id) => document.getElementById(id) 

const skip = () => {}

const setState = (state) => {
  _("d_status").innerHTML = state
  document.title = state
}

const error = (msg) => {
  setState("error")
  _("d_error").innerHTML = msg.replace(/\n/g, "<br/>")
  hideResults()
}

const srcRef = (id) => "cache/" + id + ".src"
const pdfRef = (id) => "cache/" + id + ".pdf"
const imgRef = (id) => "cache/" + id + ".png"

function newImage(key) {
  var img = new Image()
  img.src = imgRef(key)
  return img
}

const hide = (id) => {
  const el = _(id)
  if (el) {
    const s = el.style
    s.display = "none"
    s.visibility = "hidden"
  }
}

hideResults = () => _("d_results").style.display="none"

const show = (id) => {
  const el = _(id)
  if (el) {
    const s = el.style
    s.visibility = "visible"
    s.display = "block"
  }
}

const quoteRef = (id) => ('<a href="' + getUrl() + '?d=' + id + '"><img src="http://presheaf.com/' + imgRef(id) +
         '" title="click to go to presheaf.com for editing"/></a>')

function justShow(id) {
  var ref = pdfRef(id)
  _("d_png").src  = imgRef(id)
  _("d_pdf").href = ref
  _("d_pdf_e").src = ref
  _("d_pdf_o").data = ref
  _("d_quote").value = quoteRef(id)
  getSrc(id)
  _("d_results").style.display="block"
}

idNumber = (i) => _("i."+i).src.match("/([^\\./]+)\\.png")[1]

function sortByDate(map) {
  var a = []
  for (key in map) {
    if (key && map.hasOwnProperty(key))
    a.push(key)
  }

  a.sort(function(x,y) { return map[y].date - map[x].date })

  return a
}

var MAX_HISTORY_LENGTH = 1000

function getHistory() {
  if (!localStorage.history) {
    localStorage.history = "{}"
  }
  var found = JSON.parse(localStorage.history)
  delete found[undefined]
  return found
}

var myHistory = getHistory()

const saveHistory = () => localStorage.history = JSON.stringify(myHistory)

const touch = (id) => {
  myHistory[id].date = new Date().getTime()
  saveHistory()
  showHistory()
}


const choose = (i) => {
  let id = idNumber(i)
  justShow(id)
  touch(id)
}


const addToHistory = (id, text) => {
  if (!myHistory[id]) myHistory[id] = {}
  myHistory[id].text = text
  touch(id)
  delete myHistory[id].deleted
  console.log("added " + id + "=>" + JSON.stringify(myHistory[id]))
  saveHistory()
  showHistory()
}

deleteFromHistory = (i) => {
  const id = idNumber(i)
  console.log("Will delete row " + i + "=>" + id)
  myHistory[id].deleted = new Date().getTime()
  saveHistory()
  showHistory()
}

function showHistory() {
  const sorted = sortByDate(myHistory)
  // kick out the last one if too many
  if (sorted.length > MAX_HISTORY_LENGTH) {
    for (i = MAX_HISTORY_LENGTH; i < sorted.length; i++) {
      delete myHistory[sorted[i]]
    }
    sorted = sorted.splice(MAX_HISTORY_LENGTH, sorted.length - MAX_HISTORY_LENGTH)
  }
  const validOnes = sorted.filter(id => !myHistory[id].deleted)
  fillImages(validOnes)
}

function fillImages(ids) {
  var loadedImages = []

  for (i = 0; i < ids.length; i++) {
    const id = ids[i]
    const hel = myHistory[id]
    if (id && !hel.deleted) {
      loadedImages[i] = newImage(id)
      loadedImages[i].id = "i." + i
      var ref = _("ai." + i)
      if (ref) {
        ref.title = hel.text
        loadedImages[i].onload = function() {
          const key = this.id
          const el = _(key)
          el.src = this.src
          el.width = Math.min(100, this.width)
          show("h"+key)
          show(key)
        }
      }
    }
  }
}

function showDiagram(diagram, sourceText) {
  setState("Here's your diagram.")
  justShow(diagram.id)
  addToHistory(diagram.id, sourceText)
}

function httpGet(uri, onwait, onload, onerror) {
  var xhr = new XMLHttpRequest()
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        try {
          onload(xhr.responseText)
        } catch (e) {
          onerror("oops, " + e)
        }
      } else {
        onerror("Got error " + xhr.status + " from the server.")
      }
    }
  }
  xhr.open("GET", uri, true)
  xhr.send()
  onwait()
}

function httpPost(uri, data, onwait, onload, onerror) {
  var xhr = new XMLHttpRequest()
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        try {
          onload(xhr.responseText)
        } catch (e) {
          onerror("oops, " + e)
        }
      } else {
        onerror("Got error " + xhr.status + " from the server.")
      }
    }
  }
  xhr.open("POST", uri)
  xhr.setRequestHeader("Content-Type", "application/json;charset=UTF-8")
  xhr.send(JSON.stringify(data))
  onwait()
}

function getSrc(id) {
  httpGet(srcRef(id), skip,
   (text) => { _("d_in").value = text }, error
  )
}

function send(input, format) {
  httpGet("dws?format=" + format + "&in=" + encodeURIComponent(input),
      () => {
        setState("please wait...")
        _("d_error").innerHTML = ""
      },
      (text) => {
        console.log("Got response <<<" + text + ">>>")
        var response = eval("(" + text + ")")
        if (response.error) {
          error("Error: " + response.error)
        } else {
          response.image = newImage(response.id)
          response.image.onload = () => showDiagram(response, input)
        }
      },
      error
  )
}

function commit() {
  const input = _("d_in").value
  const fc = _("d_format")
  const format =  fc.options[fc.selectedIndex].value
  send(input, format)
}

function fillSamples(sources) {
  var loadedImages = []
  for (i = 0; i < sources.length; i++) {
    const id = sources[i].id
    const toid = typeof(id)

    if (id && (toid != 'undefined')) {
      _("samples" + i % 2).innerHTML += "<div class='diagramEntry' id='s.i." + id + "'/>"
      loadedImages[i] = newImage(id)
      loadedImages[i].id = "i." + id
      loadedImages[i].alt = sources[i].source
      setListeners(loadedImages[i], id)
    } else {
//      console.log("#" + i + " is bad: " + id)
    }
  }
}

// separate function so the context does not leak into the closures
function setListeners(image, id) {
  image.onclick = Function('justShow("' + id + '")')
  image.onload = function() {
    this.width = Math.min(100, this.width)
    _('s.' + this.id).appendChild(this)
  }
}


function setListenersBad(image, key) {
  image.onclick = Function('justShow("' + key + '")')
  image.onload = () => {
    this.width = Math.min(100, this.width)
    const segKey = 's.i.' + key
    const seg = _(segKey)
    if (seg) {
      console.log("Appending " + key + " to " + seg)
      seg.appendChild(this)
    }
  }
}

function fillIn() {
  httpGet("dws?format=xy&in=X",
      skip,
      (text) => {
        _("d_version").innerHTML = eval("(" + text + ")").version
      },
      error
  )
  httpGet("dws?op=samples",
      function() {},
      function(text) {
        try {
          fillSamples(JSON.parse(text))
        } catch(e) {
          error(e)
        }
      },
      function(msg) {
        error(msg)
      }
  )
}

function getUrl() {
  if (x = new RegExp('([^?]+)').exec(location.href)) return x[1]
}

function getArg(name) {
  if (name = (new RegExp('[?&]' + name + '=([^&]+)')).exec(location.search)) return name[1]
}

historyFrag = (i) =>
                   "<div class=historyEntry id=\"hi." + i + "\"  style='display:none'>"
                 + "<a id=\"ai." + i + "\" onclick='choose(" + i + ")'> "  
                 + "<img id=\"i." + i + "\" width=100 style='visibility:hidden' />"
                 + "<div class='overlay'>"
                 + "<div class=deletion onclick='deleteFromHistory(" + i + ")'>"
                 + "&times;&nbsp;</div>"
                 + "</div></a>"
                 + "</div>"
                 
redrawHistory = () => {
  var historyHtml = ""
  for (var i = 0; i < MAX_HISTORY_LENGTH; i++) {
    historyHtml += historyFrag(i)
  }
  _("history").innerHTML = historyHtml
  showHistory()
}

window.onload = function() {
  fillIn()
  redrawHistory()
  var id = getArg('d')
  if (id) justShow(id)
}

console.log("presheaf.js ready.")