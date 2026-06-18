document.addEventListener("DOMContentLoaded", function () {
    const page = document.getElementById("usedDetailPage");

    if (!page) {
        return;
    }

    const memberId = page.dataset.memberId;
    const usedPostId = page.dataset.usedPostId;

    const mainImage = document.getElementById("mainImage");
    const thumbs = document.querySelectorAll(".js-thumb");

    thumbs.forEach(function (thumb) {
        thumb.addEventListener("click", function () {
            changeMainImage(thumb, mainImage);
        });
    });

    const wishBtn = document.getElementById("wishBtn");

    if (wishBtn) {
        wishBtn.addEventListener("click", function () {
            toggleWish(memberId, usedPostId);
        });

        loadWishCount(usedPostId);
        checkWish(memberId, usedPostId);
    }

    const deleteForms = document.querySelectorAll(".delete-form");

    deleteForms.forEach(function (form) {
        form.addEventListener("submit", function (event) {
            const confirmed = confirm("정말 삭제하시겠습니까?");

            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
});

function changeMainImage(thumb, mainImage) {
    if (!thumb || !mainImage) {
        return;
    }

    mainImage.src = thumb.dataset.img;

    document.querySelectorAll(".js-thumb")
        .forEach(function (img) {
            img.classList.remove("active-thumb");
        });

    thumb.classList.add("active-thumb");
}

function toggleWish(memberId, usedPostId) {
    if (!memberId || memberId === "null") {
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
            + "&usedPostId=" + encodeURIComponent(usedPostId)
    })
        .then(function (res) {
            return res.json();
        })
        .then(function (data) {
            updateWishUI(data.wished);
            loadWishCount(usedPostId);
        })
        .catch(function () {
            alert("찜 처리 중 오류가 발생했습니다.");
        });
}

function loadWishCount(usedPostId) {
    const wishCount = document.getElementById("wishCount");

    if (!wishCount || !usedPostId) {
        return;
    }

    fetch("/wishlist/count?usedPostId=" + encodeURIComponent(usedPostId))
        .then(function (res) {
            return res.json();
        })
        .then(function (count) {
            wishCount.innerText = count;
        });
}

function checkWish(memberId, usedPostId) {
    if (!memberId || memberId === "null" || !usedPostId) {
        return;
    }

    fetch("/wishlist/check?memberId=" + encodeURIComponent(memberId)
        + "&usedPostId=" + encodeURIComponent(usedPostId))
        .then(function (res) {
            return res.json();
        })
        .then(function (isWished) {
            updateWishUI(isWished);
        });
}

function updateWishUI(isWished) {
    const btn = document.getElementById("wishBtn");

    if (!btn) {
        return;
    }

    if (isWished) {
        btn.innerText = "❤️ 찜취소";
        btn.classList.remove("btn-outline-danger");
        btn.classList.add("btn-danger");
    } else {
        btn.innerText = "🤍 찜하기";
        btn.classList.remove("btn-danger");
        btn.classList.add("btn-outline-danger");
    }
}