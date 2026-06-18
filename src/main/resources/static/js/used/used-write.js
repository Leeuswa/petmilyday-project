document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("writeForm");
    const submitBtn = document.getElementById("writeSubmitBtn");
    const imageInput = document.getElementById("imageInput");
    const imageHelp = document.getElementById("imageHelp");

    if (imageInput && imageHelp) {
        imageInput.addEventListener("change", function () {
            const count = imageInput.files.length;

            if (count > 0) {
                imageHelp.innerText = count + "개의 이미지가 선택되었습니다.";
            } else {
                imageHelp.innerText = "이미지는 여러 장 선택할 수 있습니다.";
            }
        });
    }

    if (form && submitBtn) {
        form.addEventListener("submit", function () {
            submitBtn.disabled = true;
            submitBtn.innerText = "등록 중...";
        });
    }
});