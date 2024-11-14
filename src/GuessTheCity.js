function chooseRandCountryKey(keys) {
    var i = 0
    keys.forEach(function(elem) {
        i++
    })
    return  $jsapi.random(i);
}

function isAFullNameOfCity() {
    return $jsapi.context().parseTree._City.name.toUpperCase() == $jsapi.context().parseTree.text.toUpperCase();
}

function stringConcat(string, insertion) {
    var res = string + insertion;
    return res;
}
