document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("guideForm");
    const button = document.getElementById("guideSubmitBtn");
    const questionBox = document.getElementById("questionBox");
    const toggleBtn = document.getElementById("exampleToggleBtn");
    const moreExamples = document.getElementById("moreExamples");
    const exampleItems = document.querySelectorAll(".example-item");

    if (form && button) {
        form.addEventListener("submit", function () {
            button.disabled = true;
            button.innerText = "처리 중...";
        });
    }

    if (toggleBtn && moreExamples) {
        toggleBtn.addEventListener("click", function () {
            moreExamples.classList.toggle("open");

            if (moreExamples.classList.contains("open")) {
                toggleBtn.innerText = "예시 접기";
            } else {
                toggleBtn.innerText = "예시 더보기";
            }
        });
    }

    exampleItems.forEach(function (item) {
        item.addEventListener("click", function () {
            const question = item.dataset.question;

            if (questionBox && question) {
                questionBox.value = question;
                questionBox.focus();
            }
        });
    });
});