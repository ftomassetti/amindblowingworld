function log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}

function setSettlementPosition(element,x,y)
{
    var worldMap = document.getElementById("worldMap");
    var baseX = worldMap.getBoundingClientRect().left;
    var baseY = worldMap.getBoundingClientRect().top;

    if (element.style)
    {
        element.style.left = (baseX+x-6)+'px';
        element.style.top = (baseY+y-6)+'px';
    } else {
        element.css({"left":(baseX+x-6)+'px', "top":(baseY+y-6)+'px'})
    }
}

function showSettlementInfo(settlementId, x, y, name)
{
    if (!eval($("#ui-id-1").attr("aria-expanded")))
    {
        return;
    }

    if ($('#settlement_info_'+settlementId).length)
    {
        return;
    }

    var worldMap = document.getElementById("worldMap");
    var baseX = worldMap.getBoundingClientRect().left;
    var baseY = worldMap.getBoundingClientRect().top;

    var myLayer = document.createElement('div');
    myLayer.id = 'settlement_info_'+settlementId;
    myLayer.style.position = 'absolute';
    myLayer.style.left = (baseX+x-16)+'px';
    myLayer.style.top = (baseY+y+10)+'px';
    myLayer.style.padding = '2px';
    myLayer.style.margin = '0';
    myLayer.style.border = '1px black solid';
    myLayer.style.padding = '2px';
    myLayer.style.backgroundColor = '#CCC';
    myLayer.innerHTML = "Info on "+name+"...";
    document.body.appendChild(myLayer);

    $.getJSON("/rest/settlement/"+settlementId, function(data){
        console.log("Data "+data);
        myLayer.innerHTML = data;
    });

    //setTimeout(function() { $('#settlement_info_'+settlementId).fadeOut("slow"); }, 3500);
    //global_displayedSettlements[settlementId] = myLayer;
}

function createSettlementIcon(settlementId,x,y,name)
{
    if (!eval($("#ui-id-1").attr("aria-expanded")))
    {
        return;
    }
    var worldMap = document.getElementById("worldMap");
    var baseX = worldMap.getBoundingClientRect().left;
    var baseY = worldMap.getBoundingClientRect().top;

    var myLayer = document.createElement('div');
    myLayer.id = 'settlement_'+settlementId;
    myLayer.style.position = 'absolute';
    //myLayer.style.left = (baseX+x-6)+'px';
    //myLayer.style.top = (baseY+y-6)+'px';
    setSettlementPosition(myLayer,x,y);
    myLayer.style.padding = '2px';
    myLayer.style.background = 'url(/img/village.png)';
    myLayer.style.margin = '0';
    myLayer.style.padding = '0';
    myLayer.style.width = '12px';
    myLayer.style.height = '12px';
    myLayer.onmousedown = function (event) { external_getCoordinates(worldMap, event, createDisaster) };
    myLayer.onmouseover = function (event) { showSettlementInfo(settlementId, x, y, name); };
    myLayer.onmouseout = function (event) { $('#settlement_info_'+settlementId).fadeOut(300, function() { $(this).remove();}); };
    //myLayer.innerHTML = "Village "+name;
    document.body.appendChild(myLayer);

    global_displayedSettlements[settlementId] = myLayer;

    //myLayer.style.zIndex = '1000';
    //worldMap.appendChild(myLayer);
}

var global_displayedSettlements = {};

function shakeVillage(settlementId) {
	$('#settlement_'+settlementId).effect('shake', { times:8, distance:5 }, 900);
}

function startPeriodicMapUpdate(worldMap) {
    /*setInterval(function() {
        worldMap.src = '/map.png?rand=' + Math.random();
    }, 18000);*/
    setInterval(function() {
        $.getJSON("/rest/settlements", function(data){
            //var new_displayedSettlements = {};
            var idsToDelete = Object.keys(global_displayedSettlements);
            $.each(data, function( index, settlement ) {
                if (eval(settlement.pop) > 0)
                {
                    //console.log("* Settlement " + settlement.pop + " = "+settlement.name+", pos "+settlement.pos.x+", id "+settlement.id);
                    var existingIcon = $("#settlement_"+settlement.id);
                    if (existingIcon.length) {
                        setSettlementPosition(existingIcon,settlement.pos.x,settlement.pos.y);
                    } else {
                        //console.log("Icon not found for "+settlement.id);
                        createSettlementIcon(settlement.id, settlement.pos.x, settlement.pos.y, settlement.name);
                    }
                    var indexToRemove = idsToDelete.indexOf(settlement.id.toString());
                    if (indexToRemove > -1){
                        idsToDelete.splice(indexToRemove,1);
                    }
                }
            });
            // remove dead villages
            //console.log("remaining idsToDelete "+idsToDelete);
            $.each(idsToDelete, function(i, idToDelete) {
                $("#settlement_"+idsToDelete).remove();
            });
        });
    }, 3000);
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

var global_userName = "";

function isCurrentUserRegistered() {
  return global_userName!="";
}

function registerUser() {
  var userName = $("#login").val().trim();
  var passWord = $("#passwrd").val().trim();
  if (userName == "") {
    alert("Username is empty!")
    return;
  }
  if (passWord == "") {
    alert("Password is empty!")
    return;
  }
  $.get("/rest/add-user/" + userName + "/" + passWord, function(data) {
    if (data.toString().indexOf("OK")>-1) {
      global_userName = userName;
      $("#login").val(userName);
      $("#login").attr("disabled","disabled");
      $("#passwrd").val("Password ok!");
      $("#passwrd").attr("disabled","disabled");
      $("#registerUser").remove();
    } else {
      alert("User is not registered. Username: " + userName + ", server response: " + data);
    }
  });
}

function createDisaster(x, y) {
  if (!isCurrentUserRegistered()) {
    $("#notLoggedInDialog").dialog("open");
    return;
  }
  var damageName = $('input[name=damage]:checked', '#damageReasons').val()
  $.getJSON("/useractions/disaster/" + x + "/" + y + "/" + damageName, function( data ) {
    if (data.length) {
        $.each(data, function(i,settlementId){
            shakeVillage(settlementId);
        });
    } else {
      //alert("Disaster too far from any village - no damage")
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
    $('#tribesAndSettlements').dataTable( { "ajax": '/rest/tribes-and-settlements' } );
}

function createMessagePopup(msgid,x,y,message)
{
    if (!eval($("#ui-id-1").attr("aria-expanded")))
    {
        return;
    }
    var worldMap = document.getElementById("worldMap");
    var baseX = worldMap.getBoundingClientRect().left;
    var baseY = worldMap.getBoundingClientRect().top;

    var myLayer = document.createElement('div');
    myLayer.id = 'event_'+msgid;
    myLayer.style.position = 'absolute';
    myLayer.style.left = (baseX+x-55)+'px';
    myLayer.style.top = (baseY+y+15)+'px';
    myLayer.style.padding = '1px';
    myLayer.style.border = '1px black solid';
    myLayer.style.background = '#ffffff';
    myLayer.innerHTML = message;
    document.body.appendChild(myLayer);

    //console.log('Delete '+"#event_"+msgid);
    setTimeout(function() { $("#event_"+msgid).fadeOut(300, function() { $(this).remove();}); }, 1500);
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

function updateTable()
{
  var tableId = '#tribesAndSettlements';
  var urlData = '/rest/tribes-and-settlements';
  $.getJSON(urlData, null, function( json ) {
      table = $(tableId).dataTable();
      oSettings = table.fnSettings();

      table.fnClearTable(this);

      for (var i=0; i<json.data.length; i++) {
        table.oApi._fnAddData(oSettings, json.data[i]);
      }

      oSettings.aiDisplay = oSettings.aiDisplayMaster.slice();
      table.fnDraw();
  });
}

$(document).ready(function(){
    $("#accordion").accordion({
        beforeActivate: function( event, ui ) {
            var whichId = event.originalEvent.target.id;
            var hidingMap =  ($("#ui-id-1").attr("aria-expanded")=="true") && (whichId!="ui-id-1");
            var showingMap = ($("#ui-id-1").attr("aria-expanded")=="false") && (whichId=="ui-id-1");
            var showingTable = ($("#ui-id-2").attr("aria-expanded")=="false") && (whichId=="ui-id-2");
            $.each(Object.keys(global_displayedSettlements), function(i, settlementId){
                if (hidingMap) {
                    $("#settlement_"+settlementId).hide();
                } else if (showingMap){
                    $("#settlement_"+settlementId).show();
                }
            });
            if (showingTable) {
                updateTable();
            };
        }
    });

    setInterval(function() {
        $.getJSON("/rest/totalpop", function(data){
          $(".worldpopValue").html(data.toString());
        });
    }, 5000);
});
