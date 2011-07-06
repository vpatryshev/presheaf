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

function texRef(id) {
  return "cache/" + id + ".tex"
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

function choose(id) {
  var text = history[id].text
  $("d_in").value = text
  send(text, "xy")
}

function addToHistory(id, text, image) {
  history[id] = {text : text, image : image}
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
  $("d_png").src=diagram.image.src
  $("d_pdf").href=pdfRef(diagram.id)
//  $("d_txt").innerHTML=diagram.source
  $("d_version").innerHTML=diagram.version
  $("d_results").style.display="block"
  addToHistory(diagram.id, diagram.source, diagram.image)
}

function send(input, format) {
  var uri = "dws?format=" + format + "&in=" + encodeURIComponent(input)
  var xhr = new XMLHttpRequest()
  $("d_results").style.display="none"
  $("d_error").innerHTML = ""
  setState("Please wait...")
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        try {
          var response = eval("(" + xhr.responseText + ")")
          response.image = image(response.id)
          response.image.onload = function() {
            show(response)
          }
        } catch (e) {
          error("oops, " + e)
        }
      } else {
        error("Got error " + xhr.status + " from the server.")
      }
    }
  }
  xhr.open("GET", uri, true)
  xhr.send()
}

function commit() {
  send(getInput(), getFormat())
}
