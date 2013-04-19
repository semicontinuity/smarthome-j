const ws_binary = true;

/** @param id   CANP endpoint id, format '{canp host}/{canp endpoint}'*/
function ws_canp_socket(id) {
    var webSocket = new WebSocket("ws://" + window.document.location.host + '/canp/' + id);
    if (ws_binary) webSocket.binaryType = 'arraybuffer';
    return webSocket;
}

var ws_canp_codec = function(binary) {
    if (binary) {
        return {
            query: function () { return new Uint8Array(0); },
            byte: function(v) { var b = new Uint8Array(1); b[0] = v; return b;},
            decode: function(data) { return new Uint8Array(data);}
        }
    }
    else {
        return {
            query: function() { return ''},
            byte: function(v) { return [v.toString(16)];},
            decode: function(data) { return [parseInt(data.substring(0,2), 16)];}
        }
    }
}(ws_binary);
