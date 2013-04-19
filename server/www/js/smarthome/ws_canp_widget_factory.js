const QUERY =  ws_canp_codec.query();

widgetFactory = {
    linearActuator: function (canpAddress, element) {
        const STOP =  ws_canp_codec.byte(0);
        const UP =  ws_canp_codec.byte(1);
        const DOWN =  ws_canp_codec.byte(2);
        var ws;

        var e_up_a = document.createElement('a');
        e_up_a.innerHTML = 'Up<small class="counter">&nbsp;</small>';
        e_up_a.onclick = function() {ws.send(UP)};
        var e_up_li = document.createElement('li');
        e_up_li.appendChild(e_up_a);
        element.append(e_up_li);

        var e_stop_a = document.createElement('a');
        e_stop_a.innerText = 'Stop';
        e_stop_a.onclick = function() {ws.send(STOP)};
        var e_stop_li = document.createElement('li');
        e_stop_li.appendChild(e_stop_a);
        element.append(e_stop_li);

        var e_down_a = document.createElement('a');
        e_down_a.innerHTML = 'Down<small class="counter">&nbsp;</small>';
        e_down_a.onclick = function() {ws.send(DOWN)};
        var e_down_li = document.createElement('li');
        e_down_li.appendChild(e_down_a);
        element.append(e_down_li);

        return function (activate) {
            if (activate) {
                ws = ws_canp_socket(canpAddress);
                ws.onopen = function () {
                    ws.send(QUERY);
                };
                ws.onmessage = function (evt) {
                    var message = ws_canp_codec.decode(evt.data);
                    e_up_li.className = (message[0] == UP[0]) ? "green on" : "";
                    e_down_li.className = (message[0] == DOWN[0]) ? "green on" : "";
                };
                ws.onerror = function (evt) {
                    console.log(evt);
                };
            }
            else {
                ws.close();
            }
        };
    },
    progressBar: function (canpAddress, element) {
        var ws;
        element.progressbar().height(10);

        return function (activate) {
            if (activate) {
                ws = ws_canp_socket(canpAddress);
                ws.onopen = function () {
                    ws.send(QUERY);
                };
                ws.onmessage = function (evt) {
                    element.progressbar({value: ws_canp_codec.decode(evt.data)[0]});
                };
                ws.onerror = function (evt) {
                    console.log(evt);
                };
            }
            else {
                ws.close();
            }
        };
    },
    binaryOutput: function (canpAddress, element, text) {
        const OFF =  ws_canp_codec.byte(0);
        const ON =  ws_canp_codec.byte(1);
        var ws;
        var state = "";
        var e_toggle_a = document.createElement('a');
        e_toggle_a.innerHTML = text + '<small class="counter">&nbsp;</small>';
        e_toggle_a.onclick = function() {
            if (state == "on") {
                state = "";
                ws.send(OFF);
                element.removeClass("on");
            }
            else {
                state = "on";
                ws.send(ON);
                element.addClass("on");
            }
        };
        element.append(e_toggle_a);

        return function (activate) {
            if (activate) {
                ws = ws_canp_socket(canpAddress);
                ws.onopen = function () {
                    ws.send(QUERY);
                };
                ws.onmessage = function (evt) {
                    var message = ws_canp_codec.decode(evt.data);
                    state = (message[0] == OFF[0]) ? "" : "on";
                    if (state == "")
                        element.removeClass("on");
                    else
                        element.addClass("on");
                };
                ws.onerror = function (evt) {
                    console.log(evt);
                };
            }
            else {
                ws.close();
            }
        };
    }
};
