document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("usedSearchForm");
    const searchBtn = document.getElementById("searchBtn");

    if (form && searchBtn) {
        form.addEventListener("submit", function () {
            searchBtn.disabled = true;
            searchBtn.innerText = "검색 중...";
        });
    }

    const page = document.getElementById("usedListPage");
    const memberId = page ? page.dataset.memberId : null;

    const wishButtons = document.querySelectorAll(".list-wish-btn");

    wishButtons.forEach(function (button) {
        const postId = button.dataset.postId;

        if (!postId) {
            return;
        }

        checkListWish(button, memberId, postId);

        button.addEventListener("click", function (event) {
            event.preventDefault();
            event.stopPropagation();

            toggleListWish(button, memberId, postId);
        });
    });
});

function toggleListWish(button, memberId, postId) {
    if (!memberId || memberId === "null" || memberId === "") {
        alert("로그인이 필요합니다.");
        location.href = "/member/login";
        return;
    }

    fetch("/wishlist/toggle", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "memberId=" + encodeURIComponent(memberId)
            + "&usedPostId=" + encodeURIComponent(postId)
    })
        .then(function (res) {
            if (!res.ok) {
                throw new Error("찜 요청 실패");
            }

            return res.json();
        })
        .then(function (data) {
            updateListWishUI(button, data.wished);
        })
        .catch(function () {
            alert("찜 처리 중 오류가 발생했습니다.");
        });
}

function checkListWish(button, memberId, postId) {
    if (!memberId || memberId === "null" || memberId === "") {
        updateListWishUI(button, false);
        return;
    }

    fetch("/wishlist/check?memberId=" + encodeURIComponent(memberId)
        + "&usedPostId=" + encodeURIComponent(postId))
        .then(function (res) {
            if (!res.ok) {
                throw new Error("찜 확인 실패");
            }

            return res.json();
        })
        .then(function (isWished) {
            updateListWishUI(button, isWished);
        })
        .catch(function () {
            updateListWishUI(button, false);
        });
}

function updateListWishUI(button, isWished) {
    if (!button) {
        return;
    }

    if (isWished) {
        button.classList.add("active");
        button.innerText = "♥";
        button.setAttribute("aria-label", "찜 취소");
    } else {
        button.classList.remove("active");
        button.innerText = "♡";
        button.setAttribute("aria-label", "찜하기");
    }
}