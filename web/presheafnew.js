/**
 * Functionality for presheaf.com
 * @author Vlad Patryshev
 */
function $(id) {
  return document.getElementById(id) 
}

function getInput() {
  return $("d_in").value
}

function getFormat() {
  var s = $("d_format")
  return s.options[s.selectedIndex].value
}

function setState(state) {
  $("d_status").innerHTML = state
  document.title = state
}

function error(msg) {
  setState("error")
  $("d_error").innerHTML = msg
  hide()
}

function srcRef(id) {
  return "cache/" + id + ".src"
}

function pdfRef(id) {
  return "cache/" + id + ".pdf"
}

function permanentRef(id) {

}

function imgRef(id) {
  return "cache/" + id + ".png"
}

function image(id) {
  var img = new Image()
  img.src = imgRef(id)
  return img
}

function hide() {
  $("d_results").style.display="none"
}

function quoteRef(id) {
  return '<a href="' + getUrl() + '?d=' + id + '"><img src="http://presheaf.com/' + imgRef(id) +
         '" title="click to go to presheaf.com for editing"/></a>'
}

function justShow(id) {
  $("d_png").src  = imgRef(id)
  $("d_pdf").href = pdfRef(id)
  $("d_quote").value = quoteRef(id)
  getSrc(id)
  $("d_results").style.display="block"
}

function choose(imgsrc) {
  var id = imgsrc.match("/([^\./]+)\.png")[1]
  addToHistory(id)
  justShow(id)
}

function sortByValue(map) {
  var a = []
  for (key in map) {
    if (map.hasOwnProperty(key))
    a.push(key)
  }

  a.sort(function(x,y) { return map[x] < map[y] })

  return a
}

var MAX_HISTORY_LENGTH = 9

function getHistory() {
  var cookie = document.cookie
  if (!cookie) return {}
  var matches = cookie.match(/(^|;)\s*History=([^;]+)/)
  if (!matches) return {}
  var ids = matches[2].split(",")
  var history = {}
  for (i = 0; i < ids.length; i++) {
    if (ids[i] != 'length') {
      history[ids[i]] = MAX_HISTORY_LENGTH * 100 - i
    }
  }
  return history
}

var myHistory = getHistory()

function addToHistory(id) {
  myHistory[id] = new Date().getTime()
  showHistory()
}

function showHistory() {
  var sorted = sortByValue(myHistory)
  // now kick out the last one
  if (sorted.length > MAX_HISTORY_LENGTH) {
    for (i = MAX_HISTORY_LENGTH; i < sorted.length; i++) {
      delete myHistory[sorted[i]]
    }
    sorted = sorted.splice(MAX_HISTORY_LENGTH, sorted.length - MAX_HISTORY_LENGTH)
  }
  document.cookie = 'History=' + sorted.join(",") + ';expires=July 19, 2051'
  fillImages(sorted)
}


function fillImages(ids) {
  var loadedImages = []

  for (i = 0; i < ids.length; i++) {
    var id = ids[i]
    loadedImages[i] = image(id)
    loadedImages[i].id = "h." + i
    loadedImages[i].onload = function() {
      var key = this.id
      $(key).src = this.src
      $(key).width = Math.min(100, this.width)
      $(key).style.visibility='visible'
    }
  }
}

function show(diagram) {
  setState("Here's your diagram")
  justShow(diagram.id)
  addToHistory(diagram.id)
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
    $("d_in").value = text
  }, function(msg) { error(msg)}
  )
}

function send(input, format) {
  xhr("dws?format=" + format + "&in=" + encodeURIComponent(input),
      function() {
        setState("please wait...")
        $("d_error").innerHTML = ""
      },
      function(text) {
        var response = eval("(" + text + ")")
        if (response.error) {
          error("Error: " + response.error)
        } else {
          response.image = image(response.id)
          response.image.onload = function() {
            show(response)
          }
        }
      },
      function(msg) {
//        alert("eh..." + msg)
        error(msg)
      }
  )
}

function commit() {
  send(getInput(), getFormat())
}

function fillSamples(sources) {
  var loadedImages = []
  for (i = 0; i < sources.length; i++) {
    var id = sources[i].id
    $("samples" + i % 2).innerHTML += "<div class='diagramEntry' id='s.i." + id + "'/>"
    loadedImages[i] = image(id)
    loadedImages[i].id = "i." + id
    loadedImages[i].alt = sources[i].source
    setListeners(loadedImages[i], id)
  }
}

// separate function so the context does not leak into the closures
function setListeners(image, id) {
  image.onclick = Function('justShow("' + id + '")')
  image.onload = function() {
    this.width = Math.min(100, this.width)
    $('s.' + this.id).appendChild(this)
  }
}


function fillIn() {
  xhr("dws?format=xy&in=X",
      function() {},
      function(text) {
        $("d_version").innerHTML = eval("(" + text + ")").version
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

window.onload = function() {
  fillIn()
  var historyHtml = ""
  for (var i = 0; i < MAX_HISTORY_LENGTH; i++) {
    historyHtml += "<div class=diagramEntry>"
                 + "<img id=\"h." + i + "\" width=100 style='visibility:hidden' "
                 + "onclick='choose(this.src)'/>" + "</div>"
  }
  $("history").innerHTML = historyHtml
  showHistory()
  var id = getArg('d')
  if (id) justShow(id)
}
