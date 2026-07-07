document.addEventListener("DOMContentLoaded", function () {
    const hospitalList = window.__PM_HOSPITAL_LIST__ || [];

    const mapContainer = document.getElementById("map");
    if (!mapContainer || typeof kakao === "undefined") {
        return;
    }

    const mapOption = {
        center: new kakao.maps.LatLng(37.5665, 126.9780),
        level: 5
    };

    const map = new kakao.maps.Map(mapContainer, mapOption);
    const bounds = new kakao.maps.LatLngBounds();

    hospitalList.forEach(function (hospital) {
        const position = new kakao.maps.LatLng(hospital.latitude, hospital.longitude);

        const marker = new kakao.maps.Marker({
            map: map,
            position: position
        });

        bounds.extend(position);

        const infowindow = new kakao.maps.InfoWindow({
            content:
                '<div style="padding:10px;font-size:13px;width:190px;">' +
                "<strong>" + hospital.name + "</strong><br>" +
                hospital.address +
                "</div>"
        });

        kakao.maps.event.addListener(marker, "mouseover", function () {
            infowindow.open(map, marker);
        });

        kakao.maps.event.addListener(marker, "mouseout", function () {
            infowindow.close();
        });

        kakao.maps.event.addListener(marker, "click", function () {
            location.href = "/hospital/" + hospital.id;
        });
    });

    if (hospitalList.length > 0) {
        map.setBounds(bounds);
    }
});
