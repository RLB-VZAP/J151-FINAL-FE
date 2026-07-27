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

    function csrfToken() {
        var meta = document.querySelector('meta[name="csrf-token"]');
        return meta ? meta.getAttribute("content") : "";
    }

    function hiddenInput(name, value) {
        var input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        input.value = value;
        return input;
    }

    // Mirrors the server-rendered report form in league-chat.jsp so a message
    // that arrives via polling can still be reported (Rule C). Confirmation
    // happens before the POST, same as the static markup.
    function buildReportForm(messageId) {
        var form = document.createElement("form");
        form.method = "post";
        form.action = contextPath + "/league-chat";
        form.className = "report-form";
        form.addEventListener("submit", function (event) {
            if (!window.confirm("Report this message to an administrator? This cannot be undone.")) {
                event.preventDefault();
            }
        });

        form.appendChild(hiddenInput("csrfToken", csrfToken()));
        form.appendChild(hiddenInput("action", "report"));
        form.appendChild(hiddenInput("leagueId", leagueId));
        form.appendChild(hiddenInput("messageId", messageId));

        var reason = document.createElement("input");
        reason.type = "text";
        reason.name = "reason";
        reason.maxLength = 200;
        reason.placeholder = "Reason (optional)";
        reason.className = "report-reason";
        form.appendChild(reason);

        var submit = document.createElement("button");
        submit.type = "submit";
        submit.className = "report-btn";
        submit.textContent = "Report";
        form.appendChild(submit);

        return form;
    }

    function lastCreatedAt() {
        var bubbles = container.querySelectorAll(".bubble");
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

    function appendMessage(message) {
        var mine = currentUser && currentUser === String(message.senderUserId);
        var bubble = document.createElement("div");
        bubble.className = "bubble " + (mine ? "mine" : "theirs");
        bubble.setAttribute("data-created-at", message.createdAt);

        var author = document.createElement("span");
        author.className = "bubble-author";
        author.textContent = message.senderUsername;

        var body = document.createElement("p");
        body.className = "bubble-body";
        body.textContent = message.body;

        var time = document.createElement("span");
        time.className = "bubble-time";
        time.textContent = message.createdAt;

        var footer = document.createElement("div");
        footer.className = "bubble-footer";
        footer.appendChild(time);
        footer.appendChild(buildReportForm(message.messageId));

        bubble.appendChild(author);
        bubble.appendChild(body);
        bubble.appendChild(footer);
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
