document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("paymentReadyForm");
    const button = document.getElementById("paymentReadyBtn");

    if (form && button) {
        form.addEventListener("submit", function () {
            button.disabled = true;
            button.innerText = "결제창 여는 중...";
        });
    }
});