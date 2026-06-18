document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("usedSearchForm");
    const searchBtn = document.getElementById("searchBtn");

    if (form && searchBtn) {
        form.addEventListener("submit", function () {
            searchBtn.disabled = true;
            searchBtn.innerText = "검색 중...";
        });
    }
});