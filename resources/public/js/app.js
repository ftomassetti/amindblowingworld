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

var global_userName=""
function isCurrentUserRegistered() {
  return global_userName!=""
}

function registerUser() {
  var userName = $("#login").val().trim();
  if (userName == "") {
    alert("Username is empty!")
    return;
  }
  $.get("/rest/add-user/" + userName, function(data) {
    if (data.toString().indexOf("OK")>-1) {
      global_userName = userName;
      $("#login").val("Your username: " + userName);
      $("#login").attr("disabled","disabled");
      $("#registerUser").remove();
    } else {
      alert("User is not regitered. Username: " + userName + ", server response: " + data)
    }
  });
}

function createDisaster(x, y) {
  if (!isCurrentUserRegistered()) {
    $("#notLoggedInDialog").dialog("open");
    return;
  }
  var damageName = $('input[name=damage]:checked', '#damageReasons').val()
  $.get("/useractions/disaster/" + x + "/" + y + "/" + damageName, function( data ) {
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
    $("#accordion").accordion();
    $("#notLoggedInDialog").dialog({ autoOpen: false });
    $("#registerUser").click(registerUser);
}

var global_lastNewsNumber = 0;
function addNews(message) {
  newsSelect = document.getElementById('newsList');
  var toSelectLast = false
  if (newsSelect.options[newsSelect.options.length-1].selected) {
      toSelectLast=true
  }
  $.getJSON('/history/since/' + global_lastNewsNumber, function(jsonData) {
    global_lastNewsNumber = jsonData[0]
    for (i=0; i<jsonData[1].length; i++) {
        data = jsonData[1][i];
        newsSelect.options[newsSelect.options.length] = new Option(data.msg, 'v_' + message);
    }
    if (toSelectLast) {
        newsSelect.options[newsSelect.options.length-1].selected = true
    }
  });
}
