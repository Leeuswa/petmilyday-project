function showCancelModal(reservationId) {
    document.getElementById("cancelForm").action =
        "/reservation/" + reservationId + "/cancel";

    new bootstrap.Modal(
        document.getElementById("cancelModal")
    ).show();
}

// 실시간 대기번호 변경 처리
// sseNotification.html에서 받은 알림을 petmily-notification 이벤트로 전달하면 여기서 받는다.
window.addEventListener("petmily-notification", function (event) {
    const data = event.detail;

    if (!data || data.type !== "WAITING_QUEUE_UPDATED") {
        return;
    }

    const waitNumberElement = document.getElementById("wait-number-" + data.reservationId);

    if (waitNumberElement) {
        waitNumberElement.textContent = data.waitNumber + "번";
    }
});
