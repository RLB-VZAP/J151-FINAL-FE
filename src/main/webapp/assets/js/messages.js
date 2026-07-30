(function () {
    "use strict";

    var container = document.getElementById("conversationMessages");
    if (!container) {
        return;
    }

    var contextPath = container.getAttribute("data-context-path") || "";
    var counterpart = container.getAttribute("data-counterpart");
    var POLL_INTERVAL_MS = 4000;

    function lastCreatedAt() {
        var bubbles = container.querySelectorAll(".msg-bubble");
        if (!bubbles.length) {
            return null;
        }
        return bubbles[bubbles.length - 1].getAttribute("data-created-at");
    }

    function scrollToBottom() {
        container.scrollTop = container.scrollHeight;
    }

    function clearPlaceholder() {
        var placeholder = container.querySelector("#noMessagesYetState");
        if (placeholder) {
            placeholder.remove();
        }
    }

    function appendMessage(message) {
        clearPlaceholder();
        var bubble = document.createElement("div");
        bubble.className = "msg-bubble " + (message.mine ? "is-mine" : "is-theirs");
        bubble.setAttribute("data-created-at", message.createdAt);

        var body = document.createElement("p");
        body.className = "msg-bubble-body";
        body.textContent = message.body;

        var time = document.createElement("span");
        time.className = "msg-bubble-time";
        time.textContent = String(message.createdAt).replace("T", " ").slice(0, 16);

        bubble.appendChild(body);
        bubble.appendChild(time);
        container.appendChild(bubble);
    }

    function poll() {
        var since = lastCreatedAt();
        var url = contextPath + "/messages?format=json&with=" + encodeURIComponent(counterpart);
        if (since) {
            url += "&since=" + encodeURIComponent(since);
        }

        fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
            .then(function (response) {
                if (response.status === 401) {
                    window.location.href = contextPath + "/login";
                    return null;
                }
                if (!response.ok) {
                    return null;
                }
                return response.json();
            })
            .then(function (messages) {
                if (!messages || !messages.length) {
                    return;
                }
                messages.forEach(appendMessage);
                scrollToBottom();
            })
            .catch(function () {
                /* transient network error: ignore and retry on next tick */
            });
    }

    scrollToBottom();
    setInterval(poll, POLL_INTERVAL_MS);
})();
