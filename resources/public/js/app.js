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

function initApp() {
    startPeriodicMapUpdate()
}
