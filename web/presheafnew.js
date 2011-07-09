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

var history = {}

function justShow(id) {
  $("d_png").src=imgRef(id)
  $("d_pdf").href=pdfRef(id)
}

function choose(id) {
  getSrc(id)
  justShow(id)
}

/** todo: get rid of that content, we need to sort */
function addToHistory(id, image) {
  history[id] = {image : image}
  var s = ""
  for (i in history) {
    var entry = history[i]
    var image = entry.image
    s += "<div class=historyEntry><img src=\"" + image.src + "\" width=" + (image.width / 2) + " onclick=\"choose(\'" + i + "\')\"/>" + "</div>"
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