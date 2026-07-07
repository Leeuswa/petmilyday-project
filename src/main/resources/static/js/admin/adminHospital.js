document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById("messageModal");

    if (modal) {
        modal.style.display = "block";
    }
});

function closeMessageModal() {
    const modal = document.getElementById("messageModal");

    if (modal) {
        modal.style.display = "none";
    }
}

function confirmDelete() {
    return confirm("정말 삭제하시겠습니까?");
}