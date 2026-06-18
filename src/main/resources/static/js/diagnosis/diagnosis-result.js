document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".result-move-btn");

    buttons.forEach(function (button) {
        button.addEventListener("click", function () {
            button.style.opacity = "0.7";
        });
    });
});