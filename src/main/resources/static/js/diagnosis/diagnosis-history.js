document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".history-card");

    cards.forEach(function (card) {
        card.addEventListener("click", function () {
            cards.forEach(function (item) {
                item.classList.remove("selected");
            });

            card.classList.add("selected");
        });
    });
});