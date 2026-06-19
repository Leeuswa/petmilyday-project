document.addEventListener("DOMContentLoaded", function () {
    const hospitalId = window.__PM_HOSPITAL_ID__;

    const reserveDateInput = document.getElementById("reserveDate");
    const slotContainer = document.getElementById("slotContainer");
    const reserveTimeInput = document.getElementById("reserveTimeInput");
    const selectedTimeText = document.getElementById("selectedTimeText");
    const reservationForm = document.getElementById("reservationForm");

    // 날짜 선택 시 슬롯 조회
    reserveDateInput.addEventListener("change", async function () {
        const date = this.value;

        if (!date) return;

        const response = await fetch(`/reservation/slots?hospitalId=${hospitalId}&date=${date}`);
        const slots = await response.json();

        slotContainer.innerHTML = "";

        if (slots.length === 0) {
            slotContainer.innerHTML = `
                <div class="text-danger">
                    예약 가능한 시간이 없습니다.
                </div>
            `;
            return;
        }

        slots.forEach(function (slot) {
            const button = document.createElement("button");

            button.type = "button";
            button.className = "btn slot-btn";

            const timeText = String(slot.time);
            const displayTime = timeText.substring(0, 5);

            button.innerText = `${displayTime} (${slot.currentCount}/${slot.maxCount})`;

            if (!slot.available) {
                button.disabled = true;
                button.classList.add("btn-outline-danger");
                button.innerText += " 마감";
            }

            button.addEventListener("click", function () {
                document.querySelectorAll(".slot-btn").forEach(function (btn) {
                    btn.classList.remove("selected-slot");
                });

                button.classList.add("selected-slot");

                reserveTimeInput.value = timeText;
                selectedTimeText.innerText = displayTime;
            });

            slotContainer.appendChild(button);
        });
    });

    // submit 검증
    reservationForm.addEventListener("submit", function (e) {
        if (!reserveTimeInput.value) {
            e.preventDefault();
            alert("예약 시간을 선택해주세요.");
        }
    });
});
