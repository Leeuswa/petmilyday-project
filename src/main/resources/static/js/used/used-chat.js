document.addEventListener("DOMContentLoaded", function () {
    const chatPage = document.getElementById("chatPage");

    if (!chatPage) {
        return;
    }

    const roomId = chatPage.dataset.roomId;
    const senderId = chatPage.dataset.senderId;

    const chatBox = document.getElementById("chatBox");
    const msgInput = document.getElementById("msgInput");
    const sendBtn = document.getElementById("sendBtn");

    let lastDateKey = null;
    let stomp = null;

    initLastDateKey();
    scrollToBottom();
    connectSocket();

    if (sendBtn) {
        sendBtn.addEventListener("click", function () {
            sendMessage();
        });
    }

    if (msgInput) {
        msgInput.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                sendMessage();
            }
        });
    }

    function connectSocket() {
        const sock = new SockJS("/ws-chat");
        stomp = Stomp.over(sock);

        stomp.connect({}, function () {
            stomp.subscribe("/topic/chat/room/" + roomId, function (res) {
                const msg = JSON.parse(res.body);
                const createdAt = msg.createdAt ? new Date(msg.createdAt) : new Date();

                appendDateDividerIfNeeded(createdAt);
                appendMessage(msg.message, msg.senderId, createdAt);
            });
        });
    }

    function sendMessage() {
        if (!msgInput || !stomp) {
            return;
        }

        const message = msgInput.value;

        if (message.trim() === "") {
            return;
        }

        stomp.send("/app/chat.send", {}, JSON.stringify({
            roomId: roomId,
            senderId: senderId,
            message: message
        }));

        msgInput.value = "";
        msgInput.focus();
    }

    function appendDateDividerIfNeeded(date) {
        const label = formatDateLabel(date);

        if (lastDateKey !== label) {
            const divider = document.createElement("div");
            divider.className = "date-divider";

            const span = document.createElement("span");
            span.innerText = label;

            divider.appendChild(span);
            chatBox.appendChild(divider);

            lastDateKey = label;
        }
    }

    function appendMessage(message, msgSenderId, date) {
        const row = document.createElement("div");
        row.classList.add("msg-row");

        const bubble = document.createElement("div");
        bubble.classList.add("msg");
        bubble.innerText = message;

        const time = document.createElement("div");
        time.classList.add("msg-time");
        time.innerText = formatTime(date);

        if (Number(msgSenderId) === Number(senderId)) {
            row.classList.add("me");
            bubble.classList.add("me");

            row.appendChild(time);
            row.appendChild(bubble);
        } else {
            row.classList.add("other");
            bubble.classList.add("other");

            row.appendChild(bubble);
            row.appendChild(time);
        }

        chatBox.appendChild(row);
        scrollToBottom();
    }

    function initLastDateKey() {
        const dividers = document.querySelectorAll(".date-divider span");

        if (dividers.length > 0) {
            lastDateKey = dividers[dividers.length - 1].innerText;
        }
    }

    function scrollToBottom() {
        if (chatBox) {
            chatBox.scrollTop = chatBox.scrollHeight;
        }
    }

    function formatTime(date) {
        const h = String(date.getHours()).padStart(2, "0");
        const m = String(date.getMinutes()).padStart(2, "0");

        return h + ":" + m;
    }

    function formatDateLabel(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, "0");
        const d = String(date.getDate()).padStart(2, "0");

        return y + "년 " + m + "월 " + d + "일";
    }
});