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


function justShow(id) {
  $("d_png").src=imgRef(id)
  $("d_pdf").href=pdfRef(id)
}

function choose(id) {
  getSrc(id)
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

function getHistory() {
  var cookie = document.cookie
  if (!cookie) return {}
  var matches = cookie.match(/(^|;)\s*History=([^;]+)/)
  if (!matches) return {}
  var ids = matches[2].split(",")
  var history = {}
  for (i = 0; i < ids.length; i++) {
    history[ids[i]] = i
  }
  return history
}

var history = getHistory()

function addToHistory(id) {
  history[id] = new Date().getTime()
  showHistory()
}

var MAX_HISTORY_LENGTH = 42

function showHistory() {
  var s = ""
  var sorted = sortByValue(history)
  // now kick out the last one
  if (sorted.length > MAX_HISTORY_LENGTH) {
    for (i = MAX_HISTORY_LENGTH; i < sorted.length; i++) {
      delete history[sorted[i]]
    }
    sorted = sorted.splice(MAX_HISTORY_LENGTH, sorted.length - MAX_HISTORY_LENGTH)
  }
  document.cookie = 'History=' + sorted.join(",") + ';expires=July 19, 2051'

  for (i = 0; i < sorted.length; i++) {
    var id = sorted[i]
    var img = image(id)
    s += "<div class=historyEntry><img src=\"" + img.src + "\" width=" + Math.min(100, img.width) + " onclick=\"choose(\'" + id + "\')\"/>" + "</div>"
  }
  $("history").innerHTML = s
}

function show(diagram) {
  setState("Here's your diagram")
  justShow(diagram.id)
  $("d_results").style.display="block"
  addToHistory(diagram.id, diagram.image)
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
        $("d_error").innerHTML = ""
      },
      function(text) {
        var response = eval("(" + text + ")")
        response.image = image(response.id)
        response.image.onload = function() {
          show(response)
        }
      },
      function(msg) {
        error(msg)
      }
  )
}

function commit() {
  send(getInput(), getFormat())
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

}

window.onload=showHistory
