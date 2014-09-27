function log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}

function startPeriodicMapUpdate() {
    setInterval(function() {
        document.getElementById('worldMap').src = '/map.png?rand=' + Math.random();
    }, 18000);
}
function startPeriodicNewsUpdate() {
    setInterval(function() {
        addNews("n"+Math.random());
    }, 3000);
}

function initApp() {
    startPeriodicMapUpdate();
    startPeriodicNewsUpdate();
}

function addNews(message) {
  newsSelect = document.getElementById('newsList');
  newsSelect.options[newsSelect.options.length] = new Option(message, 'v_' + message);
  if (newsSelect.options[newsSelect.options.length-2].selected) {
    newsSelect.options[newsSelect.options.length-1].selected = true
  }
}
