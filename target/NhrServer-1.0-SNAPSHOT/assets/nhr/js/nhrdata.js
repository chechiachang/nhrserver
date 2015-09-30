/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var nhr_devices_type = {
    "temp_humid": {"name": "temp_humid", "on": "fa fa-fw fa-spin fa-gear", "off": "fa fa-fw fa-gear fa-spin"},
    "motion_sensor": {"name": "motion_sensor", "on": "fa fa-fw fa-bell", "off": "fa fa-fw fa-bell-slash"},
    "door_seal": {"name": "door_seal", "on": "fa fa-fw fa-lock", "off": "fa fa-fw fa-unlock"},
    "bullhorn" : {"name": "bullhorn", "on": "fa fa-fw fa-bullhorn"}
};


$(function () {
    setInterval(getNhrData, 1000);
});

function getNhrData() {

    //cause screen flash, which is bad
    //$('div#floor1').empty();

    //$('.nhr').remove();

    $.get("NhrDataJsonServlet", {cmd: "getdata"}, function (jsonResponse) {
        var datas = $.parseJSON(jsonResponse);
        $.each(datas, function (k, v) {
            var icon = "", data = "", iconClass;
            //switch by devices type
            switch (v.type) {
                case "00":  //unknown;
                    break;
                case "02":  //STH-01ZB temp humidity sensor
                    data = v.data + "℃";
                    data += v.data2 + "%";
                    icon = nhr_devices_type.temp_humid.on;
                    iconClass = "sensor";
                    break;
                case "03":  //STH-Mo2ZB temp humidity sensor
                    data = v.data + "℃";
                    data += v.data2 + "%";
                    icon = nhr_devices_type.temp_humid.on;
                    iconClass = "sensor";
                    break;
                case "20":  //door electronic seal
                    data = v.data;
                    if (data == "on") {//on / off = alert / safe
                        data = "偵測";
                        iconClass = "alert";
                    } else {
                        data = "安全";
                        iconClass = "on";
                    }
                    icon = nhr_devices_type.door_seal.on;
                    break;
                case "26":
                    data = v.data;
                    if (data == "on") {//on / off = alert / safe
                        data = "偵測";
                        iconClass = "alert";
                    } else {
                        data = "安全";
                        iconClass = "on";
                    }
                    icon = nhr_devices_type.motion_sensor.on;
                    break;
                case "41":  //S05-ST PT100 / soil temp sensor
                    break;
                case "42":  //S05-SM soil moisture sensor
                    break;
                case "45":  //S05-TH air-temp humidity sensor
                    break;
                case "52":  //S05-LM leaf wetness sensor
                    break;
                case "80":  //power meter
                    break;
                case "a1":  //Single relay
                    break;
                case "b1":  //Single PMW
                    break;
                case "d0":  //A10 Siren
                    icon = nhr_devices_type.motion_sensor.on;
                    data = "報警";
                    break;
                default:
                    break;
            }

            if (v.address.length > 0) {
                $('div#' + v.address).remove();
            }
            var html = '<div id="' + v.address +
                    '" shortmac="' + v.shortMac +
                    '" clusterid= "' + v.clusterId +
                    '" class= "nhr ' + iconClass +
                    '" data="' + data + '">' +
                    ' <div><i class="fa fa-fw ' + icon + '"></i></div>';
            html += '</br>' + data;
            html += '</div>';
            //append to div
            if (v.position.length < 1) {
                $(html).addClass('ico-mode').addClass('nhr').css({position: "relative"}).appendTo('div#nhr-devices-remain');
            } else {
                var position = v.position.split(",");
                $(html).addClass('ico-mode').addClass('nhr').css({position: "relative", left: position[0] + 'px', top: position[1] + 'px'}).appendTo('div#floor1');
            }
        });
    });
}

function getNhrPosition() {
    //unsupported
}