(function () {
    "use strict";

    var container = document.getElementById("leagueFeed");
    if (!container) {
        return;
    }

    var contextPath = container.getAttribute("data-context-path") || "";
    var leagueId = container.getAttribute("data-league-id");
    var currentUser = container.getAttribute("data-current-user");
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

    function removeEmptyState() {
        var empty = container.querySelector(".empty-state");
        if (empty) {
            empty.remove();
        }
    }

    function clearPlaceholder() {
        var placeholder = container.querySelector("#leagueFeedEmptyState");
        if (placeholder) {
            placeholder.remove();
        }
    }

    function appendMessage(message) {
        var mine = currentUser && currentUser === String(message.senderUserId);
        clearPlaceholder();

        var bubble = document.createElement("div");
        bubble.className = "msg-bubble " + (mine ? "is-mine" : "is-theirs");
        bubble.setAttribute("data-created-at", message.createdAt);

        var author = document.createElement("span");
        author.className = "msg-bubble-author";
        author.textContent = message.senderUsername;

        var body = document.createElement("p");
        body.className = "msg-bubble-body";
        body.textContent = message.body;

        var time = document.createElement("span");
        time.className = "msg-bubble-time";
        time.textContent = String(message.createdAt).replace("T", " ").slice(0, 16);

        bubble.appendChild(author);
        bubble.appendChild(body);
        bubble.appendChild(time);
        container.appendChild(bubble);
    }

    function poll() {
        var since = lastCreatedAt();
        var url = contextPath + "/league-chat?format=json&leagueId=" + encodeURIComponent(leagueId);
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
                removeEmptyState();
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
