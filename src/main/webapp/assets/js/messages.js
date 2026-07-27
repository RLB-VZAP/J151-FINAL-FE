(function () {
    "use strict";

    var container = document.getElementById("conversationMessages");
    if (!container) {
        return;
    }

    var contextPath = container.getAttribute("data-context-path") || "";
    var counterpart = container.getAttribute("data-counterpart");
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

    // Mirrors the server-rendered report form in messages.jsp so a message that
    // arrives via polling (rather than the initial page render) can still be
    // reported (Rule C). Confirmation happens before the POST, same as the
    // static markup, since reporting cannot be undone.
    function buildReportForm(messageId) {
        var form = document.createElement("form");
        form.method = "post";
        form.action = contextPath + "/messages";
        form.className = "report-form";
        form.addEventListener("submit", function (event) {
            if (!window.confirm("Report this message to an administrator? This cannot be undone.")) {
                event.preventDefault();
            }
        });

        form.appendChild(hiddenInput("csrfToken", csrfToken()));
        form.appendChild(hiddenInput("action", "report"));
        form.appendChild(hiddenInput("messageId", messageId));
        form.appendChild(hiddenInput("returnWith", counterpart));

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

    function appendMessage(message) {
        var bubble = document.createElement("div");
        bubble.className = "bubble " + (message.mine ? "mine" : "theirs");
        bubble.setAttribute("data-created-at", message.createdAt);

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

        bubble.appendChild(body);
        bubble.appendChild(footer);
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
