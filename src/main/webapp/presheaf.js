/**
 * Functionality for presheaf.com
 * @author Vlad Patryshev
 * 3/16/2018
 */

const _ = (id) => document.getElementById(id) 


function setState(state) {
  _("d_status").innerHTML = state
  document.title = state
}

function error(msg) {
  setState("error")
  _("d_error").innerHTML = msg.replace(/\n/g, "<br/>")
  hideD()
}

function srcRef(id) {
  return "cache/" + id + ".src"
}

function pdfRef(id) {
  return "cache/" + id + ".pdf"
}

function imgRef(id) {
  return "cache/" + id + ".png"
}

function image(id) {
  var img = new Image()
  img.src = imgRef(id)
  return img
}

function hideD() {
  _("d_results").style.display="none"
}

const hide = (id) => {
  const el = _(id)
  if (el) {
    const s = el.style
    s.display = "none"
    s.visibility = "hidden"
  }
}

const show = (id) => {
  const el = _(id)
  if (el) {
    const s = el.style
    s.visibility = "visible"
    s.display = "block"
  }
}

function quoteRef(id) {
  return '<a href="' + getUrl() + '?d=' + id + '"><img src="http://presheaf.com/' + imgRef(id) +
         '" title="click to go to presheaf.com for editing"/></a>'
}

function justShow(id) {
  var ref = pdfRef(id);
  _("d_png").src  = imgRef(id);
  _("d_pdf").href = ref;
  _("d_pdf_e").src = ref;
  _("d_pdf_o").data = ref;
  _("d_quote").value = quoteRef(id);
  getSrc(id);
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

var myHistory = getHistory();

saveHistory = () => localStorage.history = JSON.stringify(myHistory)

touch = (id) => {
  myHistory[id].date = new Date().getTime();
  saveHistory()
  showHistory()
}


choose = (i) => {
  let id = idNumber(i)
  justShow(id)
  touch(id)
}


function addToHistory(id, text) {
  if (!myHistory[id]) myHistory[id] = {};
  myHistory[id].text = text
  touch(id)
}

deleteFromHistory = (i) => {
  myHistory[idNumber(i)].deleted=new Date().getTime();
  saveHistory()
  showHistory()
}

function showHistory() {
  var sorted = sortByDate(myHistory);
  // now kick out the last one
  if (sorted.length > MAX_HISTORY_LENGTH) {
    for (i = MAX_HISTORY_LENGTH; i < sorted.length; i++) {
      delete myHistory[sorted[i]]
    }
    sorted = sorted.splice(MAX_HISTORY_LENGTH, sorted.length - MAX_HISTORY_LENGTH)
  }
  document.cookie = 'History=X;expires=May 01, 2027';
  fillImages(sorted)
}


function fillImages(ids) {
  var loadedImages = [];

  for (i = 0; i < ids.length; i++) {
    const id = ids[i]
    const hel = myHistory[id]
    if (id && !hel.deleted) {
      loadedImages[i] = image(id);
      loadedImages[i].id = "i." + i;
      var ref = _("ai." + i)
      if (ref) {
        ref.title = hel.text
        loadedImages[i].onload = function() {
          const key = this.id
          const el = _(key)
          el.src = this.src;
          el.width = Math.min(100, this.width);
          show("h"+key)
          show(key)
        }
      }
    }
  }
}

function showD(diagram, sourceText) {
  setState("Here's your diagram.")
  justShow(diagram.id)
  addToHistory(diagram.id, sourceText)
}

function xhr(uri, onwait, onload, onerror) {
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

function getSrc(id) {
  xhr(srcRef(id), function(){}, function(text) {
    _("d_in").value = text
  }, function(msg) { error(msg)}
  )
}

function send(input, format) {
  xhr("dws?format=" + format + "&in=" + encodeURIComponent(input),
      function() {
        setState("please wait...")
        _("d_error").innerHTML = ""
      },
      function(text) {
        console.log("Got response <<<" + text + ">>>")
        var response = eval("(" + text + ")")
        if (response.error) {
          error("Error: " + response.error)
        } else {
          response.image = image(response.id)
          response.image.onload = function() {
            showD(response, input)
          }
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
    var id = sources[i].id
    if (id) {
      _("samples" + i % 2).innerHTML += "<div class='diagramEntry' id='s.i." + id + "'/>"
      loadedImages[i] = image(id)
      loadedImages[i].id = "i." + id
      loadedImages[i].alt = sources[i].source
      setListeners(loadedImages[i], id)
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

function fillIn() {
  xhr("dws?format=xy&in=X",
      function() {},
      function(text) {
        _("d_version").innerHTML = eval("(" + text + ")").version
      },
      function(msg) {
        error(msg)
      }
  )
  xhr("dws?op=samples",
      function() {},
      function(text) {
        try {
          fillSamples(eval("(" + text + ")"))
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
