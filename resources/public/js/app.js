function log(msg) {
    setTimeout(function() {
        throw new Error(msg);
    }, 0);
}

function startPeriodicMapUpdate() {
    setInterval(function() {
        document.getElementById('worldView').src = '/map.png?rand=' + Math.random();
    }, 1000);
}

function initApp() {
    startPeriodicMapUpdate()
}
