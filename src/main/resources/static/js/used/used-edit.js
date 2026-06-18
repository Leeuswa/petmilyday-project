document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("editForm");
    const submitBtn = document.getElementById("editSubmitBtn");

    if (form && submitBtn) {
        form.addEventListener("submit", function () {
            submitBtn.disabled = true;
            submitBtn.innerText = "수정 중...";
        });
    }
});