document.addEventListener("DOMContentLoaded", function () {
    const menuCards = document.querySelectorAll(".menu-card");

    menuCards.forEach(function (card) {
        card.addEventListener("click", function () {
            card.style.opacity = "0.7";
        });
    });
});