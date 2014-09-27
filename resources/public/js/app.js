function log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}

function startPeriodicMapUpdate(worldMap) {
    setInterval(function() {
        worldMap.src = '/map.png?rand=' + Math.random();
    }, 18000);
}
function startPeriodicNewsUpdate() {
    setInterval(function() {
        addNews("n"+Math.random());
    }, 3000);
}

function createDisaster(x, y) {
  $.get("/useractions/disaster/" + x + "/" + y + "/Vulcano", function( data ) {
    if (data == "true") {
      alert("Disaster close to vilage - damage caused");
    } else {
      alert("Disaster too far from any village - no damage")
    }
  });
}

function initApp() {
    var worldMap = document.getElementById("worldMap");
    worldMap.onmousedown = function (event) { external_getCoordinates(worldMap, event, createDisaster) };
    startPeriodicMapUpdate(worldMap);
    startPeriodicNewsUpdate();
}

var lastNewsNumber = 0;
function addNews(message) {
  newsSelect = document.getElementById('newsList');
  var toSelectLast = false
  if (newsSelect.options[newsSelect.options.length-1].selected) {
      toSelectLast=true
  }
  $.getJSON('/history/since/' + lastNewsNumber, function(jsonData) {
    lastNewsNumber = jsonData[0]
    for (i=0; i<jsonData[1].length; i++) {
        data = jsonData[1][i];
        newsSelect.options[newsSelect.options.length] = new Option(data.msg, 'v_' + message);
    }
    if (toSelectLast) {
        newsSelect.options[newsSelect.options.length-1].selected = true
    }
  });
}
