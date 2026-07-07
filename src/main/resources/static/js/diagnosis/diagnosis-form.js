document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("diagnosisForm");
    const button = document.getElementById("diagnosisSubmitBtn");
    const imageInput = document.getElementById("imageInput");
    const imageHelp = document.getElementById("imageHelp");

    if (imageInput && imageHelp) {
        imageInput.addEventListener("change", function () {
            if (imageInput.files.length > 0) {
                imageHelp.innerText = imageInput.files[0].name + " 파일이 선택되었습니다.";
            } else {
                imageHelp.innerText = "증상 부위 사진이 있으면 함께 업로드할 수 있습니다.";
            }
        });
    }

    if (form && button) {
        form.addEventListener("submit", function () {
            button.disabled = true;
            button.innerText = "처리 중...";
        });
    }
});