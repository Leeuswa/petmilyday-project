document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".move-btn");

    buttons.forEach(function (button) {
        button.addEventListener("click", function () {
            button.classList.add("disabled");
            button.innerText = "이동 중...";
        });
    });
});