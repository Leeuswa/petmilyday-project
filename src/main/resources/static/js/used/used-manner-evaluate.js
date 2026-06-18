document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("mannerForm");
    const submitBtn = document.getElementById("submitBtn");
    const scoreSelect = document.getElementById("scoreSelect");
    const scorePreview = document.getElementById("scorePreview");

    const messages = {
        "5": "매우 만족스러운 거래였어요.",
        "4": "좋은 거래였어요.",
        "3": "무난한 거래였어요.",
        "2": "조금 아쉬운 거래였어요.",
        "1": "매너가 아쉬운 거래였어요."
    };

    if (scoreSelect && scorePreview) {
        scoreSelect.addEventListener("change", function () {
            scorePreview.innerText = messages[scoreSelect.value] || "";
        });
    }

    if (form && submitBtn) {
        form.addEventListener("submit", function () {
            submitBtn.disabled = true;
            submitBtn.innerText = "평가 중...";
        });
    }
});