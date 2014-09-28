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

function startPeriodicUsersCheck() {
    setInterval(function() {
        $.getJSON("/rest/users", function(data){
          if (data.length > 0) {
            usersHtml = "";
            for (i=0; i<data.length; i++) {
              usersHtml+=data[i] + "<br/>";
            }
            $("#usersList").html(usersHtml);
          }
        });
    }, 6000);
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
    startPeriodicUsersCheck();
    $("#accordion").accordion();
    $("#notLoggedInDialog").dialog({ autoOpen: false });
    $("#registerUser").click(registerUser);
}

function createMessagePopup(msgid,x,y,message)
{
    if (!eval($("#ui-id-1").attr("aria-expanded")))
    {
        return;
    }
    var worldMap = document.getElementById("worldMap");
    var baseX = worldMap.getBoundingClientRect().x;
    var baseY = worldMap.getBoundingClientRect().y;

    var myLayer = document.createElement('div');
    myLayer.id = 'event_'+msgid;
    myLayer.style.position = 'absolute';
    myLayer.style.left = (baseX+x)+'px';
    myLayer.style.top = (baseY+y)+'px';
    myLayer.style.padding = '2px';
    myLayer.style.border = '1px black solid';
    myLayer.style.background = '#ffffff';
    myLayer.innerHTML = message;
    document.body.appendChild(myLayer);

    console.log('Delete '+"#event_"+msgid);
    setTimeout(function() { $("#event_"+msgid).fadeOut("slow"); }, 1500);
}


var global_lastNewsNumber = 0;
function addNews(message) {
  newsSelect = document.getElementById('newsList');
  var toSelectLast = false
  if (newsSelect.options[newsSelect.options.length-1].selected) {
      toSelectLast=true
  }
  var startOfRequest = global_lastNewsNumber;
  $.getJSON('/history/since/' + global_lastNewsNumber, function(jsonData) {
    global_lastNewsNumber = jsonData[0]
    for (i=0; i<jsonData[1].length; i++) {
        data = jsonData[1][i];
        if (jsonData[1][i] && jsonData[1][i].pos)
        {
            var eventX = jsonData[1][i].pos.x;
            var eventY = jsonData[1][i].pos.y;
            if (eventX >= 0 && eventY >= 0 && eventX < 512 && eventY < 512)
            {
                createMessagePopup(i+startOfRequest,eventX,eventY,jsonData[1][i].msg);
            }
        }
        newsSelect.options[newsSelect.options.length] = new Option(data.msg, 'v_' + message);
    }
    if (toSelectLast) {
        newsSelect.options[newsSelect.options.length-1].selected = true
    }
  });
}
