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

const editImageInput = document.getElementById("editImageInput");
const editImageFileName = document.getElementById("editImageFileName");

if (editImageInput && editImageFileName) {
    editImageInput.addEventListener("change", function () {
        const files = editImageInput.files;

        if (!files || files.length === 0) {
            editImageFileName.innerText = "선택된 이미지 없음";
            return;
        }

        if (files.length === 1) {
            editImageFileName.innerText = files[0].name;
            return;
        }

        editImageFileName.innerText = files.length + "개 이미지 선택됨";
    });
}